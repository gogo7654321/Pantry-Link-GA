package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryLinkDao {

    // --- Food Banks ---
    @Query("SELECT * FROM food_banks ORDER BY name ASC")
    fun getAllFoodBanks(): Flow<List<FoodBankEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodBank(foodBank: FoodBankEntity): Long

    @Query("SELECT * FROM food_banks WHERE id = :id LIMIT 1")
    suspend fun getFoodBankById(id: Int): FoodBankEntity?

    @Query("DELETE FROM food_banks WHERE email = :email")
    suspend fun deleteFoodBankByEmail(email: String)


    // --- Requests ---
    @Query("SELECT * FROM requests ORDER BY id DESC")
    fun getAllRequests(): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: Int): RequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: RequestEntity): Long

    @Update
    suspend fun updateRequest(request: RequestEntity)

    @Delete
    suspend fun deleteRequest(request: RequestEntity)


    // --- Claims ---
    @Query("SELECT * FROM claims ORDER BY claimTimestamp DESC")
    fun getAllClaimsDirect(): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claims WHERE donorUserId = :donorId ORDER BY claimTimestamp DESC")
    fun getClaimsForDonor(donorId: String): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claims WHERE requestId = :requestId")
    suspend fun getClaimsForRequest(requestId: Int): List<ClaimEntity>

    @Query("SELECT * FROM claims WHERE id = :claimId LIMIT 1")
    suspend fun getClaimById(claimId: Int): ClaimEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ClaimEntity): Long

    @Update
    suspend fun updateClaim(claim: ClaimEntity)


    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long


    // --- Transaction Safe Operations (The "Backend" simulation) ---
    @Transaction
    suspend fun claimRequestTransaction(
        donorId: String,
        requestId: Int,
        quantityToClaim: Int,
        timestamp: Long
    ): ClaimResult {
        val request = getRequestById(requestId) ?: return ClaimResult.Error("Request not found.")

        // A. Verify request is open (status is not Closed/Confirmed by Food Bank)
        if (request.status == "Closed" || request.status == "Confirmed by Food Bank") {
            return ClaimResult.Error("Acceptance blocked: This request is already concluded or closed.")
        }

        // B. Verify quantity is available & greater than remaining
        if (request.quantityRemaining <= 0) {
            return ClaimResult.Error("Acceptance blocked: This item request is already fully claimed.")
        }

        if (quantityToClaim <= 0) {
            return ClaimResult.Error("Quantity to claim must be greater than zero.")
        }

        if (quantityToClaim > request.quantityRemaining) {
            return ClaimResult.Error("Acceptance blocked: Only ${request.quantityRemaining} items remaining, cannot claim $quantityToClaim.")
        }

        // C. Update the request
        val newRemaining = request.quantityRemaining - quantityToClaim
        // If there are still items remaining, keep the request status as "Posted" so more neighbors can claim.
        // Otherwise, mark it as "Claimed".
        val newRequestStatus = if (newRemaining <= 0) "Claimed" else "Posted"
        val updatedRequest = request.copy(
            quantityRemaining = newRemaining,
            status = newRequestStatus
        )
        updateRequest(updatedRequest)

        // D. Create the Claim Entity
        val claim = ClaimEntity(
            requestId = requestId,
            requestTitle = request.title,
            foodBankName = request.foodBankName,
            donorUserId = donorId,
            quantityClaimed = quantityToClaim,
            claimTimestamp = timestamp,
            claimStatus = "Claimed"
        )
        val claimId = insertClaim(claim).toInt()

        // E. Log the action
        val log = AuditLogEntity(
            donorId = donorId,
            requestId = requestId,
            claimId = claimId,
            actionType = "CLAIM_ACCEPTED",
            timestamp = timestamp,
            oldStatus = request.status,
            newStatus = "Claimed"
        )
        insertAuditLog(log)

        return ClaimResult.Success(claimId)
    }

    @Transaction
    suspend fun cancelClaimTransaction(
        claimId: Int,
        donorId: String,
        timestamp: Long
    ): Boolean {
        val claim = getClaimById(claimId) ?: return false
        if (claim.donorUserId != donorId) return false

        // Block if already dropped off
        if (claim.claimStatus == "Dropped Off" || claim.claimStatus == "Accepted") {
            return false
        }

        // 1. Mark claim as Cancelled
        val updatedClaim = claim.copy(claimStatus = "Cancelled")
        updateClaim(updatedClaim)

        // 2. Restore quantity
        val request = getRequestById(claim.requestId)
        if (request != null) {
            val restoredRemaining = request.quantityRemaining + claim.quantityClaimed
            // If we restore quantity and remaining > 0, status goes back to "Posted"
            val restoredStatus = if (restoredRemaining > 0) "Posted" else request.status
            updateRequest(request.copy(
                quantityRemaining = restoredRemaining.coerceAtMost(request.quantityNeeded),
                status = restoredStatus
            ))
        }

        // 3. Log
        insertAuditLog(
            AuditLogEntity(
                donorId = donorId,
                requestId = claim.requestId,
                claimId = claimId,
                actionType = "CLAIM_CANCELLED",
                timestamp = timestamp,
                oldStatus = claim.claimStatus,
                newStatus = "Cancelled"
            )
        )
        return true
    }

    @Transaction
    suspend fun dropOffClaimTransaction(
        claimId: Int,
        timestamp: Long
    ): Boolean {
        val claim = getClaimById(claimId) ?: return false
        if (claim.claimStatus != "Claimed" && claim.claimStatus != "Ready for Drop-Off") return false

        // Update claim to "Dropped Off"
        val updatedClaim = claim.copy(
            claimStatus = "Dropped Off",
            dropoffConfirmationTimestamp = timestamp
        )
        updateClaim(updatedClaim)

        // Update main request status
        val request = getRequestById(claim.requestId)
        if (request != null) {
            // Main request transitions to "Dropped Off" only if there is no remaining quantity left to claim.
            // Otherwise, keep it as "Posted" so others can claim/fulfill what is still needed.
            val newRequestStatus = if (request.quantityRemaining <= 0) "Dropped Off" else "Posted"
            updateRequest(request.copy(status = newRequestStatus))
        }

        // Log
        insertAuditLog(
            AuditLogEntity(
                donorId = claim.donorUserId,
                requestId = claim.requestId,
                claimId = claimId,
                actionType = "CLAIM_DROPPED_OFF",
                timestamp = timestamp,
                oldStatus = claim.claimStatus,
                newStatus = "Dropped Off"
            )
        )
        return true
    }

    @Transaction
    suspend fun reviewClaimTransaction(
        claimId: Int,
        approved: Boolean,
        rejectionReason: String?,
        timestamp: Long
    ): Boolean {
        val claim = getClaimById(claimId) ?: return false
        if (claim.claimStatus != "Dropped Off") return false

        if (approved) {
            // A. Move donor claim to Accepted
            val updatedClaim = claim.copy(
                claimStatus = "Accepted",
                foodBankReviewResult = "Accepted"
            )
            updateClaim(updatedClaim)

            val request = getRequestById(claim.requestId)
            if (request != null) {
                // If sum of all "Accepted" claims for this request equals or exceeds needed (or remaining is 0)
                // Let's compute actual accepted quantity totals on the main request.
                val requestClaims = getClaimsForRequest(claim.requestId)
                val totalAccepted = requestClaims.fold(0) { sum, element ->
                    if (element.id == claimId) sum + claim.quantityClaimed
                    else if (element.claimStatus == "Accepted") sum + element.quantityClaimed
                    else sum
                }

                val fullySatisfied = totalAccepted >= request.quantityNeeded || request.quantityRemaining <= 0
                val newRequestStatus = if (fullySatisfied) "Confirmed by Food Bank" else {
                    if (request.quantityRemaining > 0) "Posted" else "Dropped Off"
                }
                
                // Keep the request in Confirmed by Food Bank until fully marked Closed
                updateRequest(request.copy(
                    status = newRequestStatus
                ))
            }

            // Log
            insertAuditLog(
                AuditLogEntity(
                    donorId = claim.donorUserId,
                    requestId = claim.requestId,
                    claimId = claimId,
                    actionType = "CLAIM_APPROVED_BY_FOOD_BANK",
                    timestamp = timestamp,
                    oldStatus = "Dropped Off",
                    newStatus = "Accepted"
                )
            )

        } else {
            // B. Move donor claim to Rejected
            val updatedClaim = claim.copy(
                claimStatus = "Rejected",
                foodBankReviewResult = "Rejected",
                rejectionReason = rejectionReason
            )
            updateClaim(updatedClaim)

            // Restore quantity back into request
            val request = getRequestById(claim.requestId)
            if (request != null) {
                val restoredRemaining = request.quantityRemaining + claim.quantityClaimed
                val restoredStatus = if (restoredRemaining > 0) "Posted" else "Claimed"
                updateRequest(request.copy(
                    quantityRemaining = restoredRemaining.coerceAtMost(request.quantityNeeded),
                    status = restoredStatus
                ))
            }

            // Log
            insertAuditLog(
                AuditLogEntity(
                    donorId = claim.donorUserId,
                    requestId = claim.requestId,
                    claimId = claimId,
                    actionType = "CLAIM_REJECTED_BY_FOOD_BANK",
                    timestamp = timestamp,
                    oldStatus = "Dropped Off",
                    newStatus = "Rejected"
                )
            )
        }
        return true
    }

    @Transaction
    suspend fun closeRequestTransaction(
        requestId: Int,
        timestamp: Long
    ): Boolean {
        val request = getRequestById(requestId) ?: return false
        val updatedRequest = request.copy(status = "Closed")
        updateRequest(updatedRequest)

        // Log
        insertAuditLog(
            AuditLogEntity(
                donorId = "SYSTEM_FOOD_BANK",
                requestId = requestId,
                claimId = 0,
                actionType = "REQUEST_CLOSED",
                timestamp = timestamp,
                oldStatus = request.status,
                newStatus = "Closed"
            )
        )
        return true
    }

    @Transaction
    suspend fun expireClaimTransaction(
        claimId: Int,
        timestamp: Long
    ): Boolean {
        val claim = getClaimById(claimId) ?: return false
        if (claim.claimStatus != "Claimed" && claim.claimStatus != "Ready for Drop-Off") return false

        // Set status to Cancelled / Expired
        updateClaim(claim.copy(claimStatus = "Cancelled", rejectionReason = "Claim expired (time limit elapsed)"))

        // Restore quantity
        val request = getRequestById(claim.requestId)
        if (request != null) {
            val restoredRemaining = request.quantityRemaining + claim.quantityClaimed
            val restoredStatus = if (restoredRemaining > 0) "Posted" else request.status
            updateRequest(request.copy(
                quantityRemaining = restoredRemaining.coerceAtMost(request.quantityNeeded),
                status = restoredStatus
            ))
        }

        // Log
        insertAuditLog(
            AuditLogEntity(
                donorId = claim.donorUserId,
                requestId = claim.requestId,
                claimId = claimId,
                actionType = "CLAIM_EXPIRED",
                timestamp = timestamp,
                oldStatus = claim.claimStatus,
                newStatus = "Cancelled"
            )
        )
        return true
    }
}

sealed class ClaimResult {
    data class Success(val claimId: Int) : ClaimResult()
    data class Error(val message: String) : ClaimResult()
}

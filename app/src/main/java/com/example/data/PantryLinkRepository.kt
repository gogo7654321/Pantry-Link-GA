package com.example.data

import kotlinx.coroutines.flow.Flow

class PantryLinkRepository(
    private val dao: PantryLinkDao,
    private val syncManager: FirebaseSyncManager? = null
) {

    val allFoodBanks: Flow<List<FoodBankEntity>> = dao.getAllFoodBanks()
    val allRequests: Flow<List<RequestEntity>> = dao.getAllRequests()
    val allClaims: Flow<List<ClaimEntity>> = dao.getAllClaimsDirect()
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()

    fun getClaimsForDonor(donorId: String): Flow<List<ClaimEntity>> =
        dao.getClaimsForDonor(donorId)

    suspend fun getRequestById(id: Int): RequestEntity? =
        dao.getRequestById(id)

    suspend fun getClaimById(id: Int): ClaimEntity? =
        dao.getClaimById(id)

    suspend fun insertRequest(request: RequestEntity): Long {
        val id = dao.insertRequest(request)
        val created = request.copy(id = id.toInt())
        syncManager?.pushRequest(created)
        return id
    }

    suspend fun updateRequest(request: RequestEntity) {
        dao.updateRequest(request)
        syncManager?.pushRequest(request)
    }

    suspend fun deleteRequest(request: RequestEntity) {
        dao.deleteRequest(request)
        syncManager?.deleteRequestOnFirebase(request.id)
    }

    suspend fun tryClaimRequest(
        donorId: String,
        requestId: Int,
        quantityToClaim: Int,
        timestamp: Long
    ): ClaimResult {
        val result = dao.claimRequestTransaction(donorId, requestId, quantityToClaim, timestamp)
        if (result is ClaimResult.Success) {
            // Push updated request
            getRequestById(requestId)?.let { syncManager?.pushRequest(it) }
            // Push created claim
            getClaimById(result.claimId)?.let { syncManager?.pushClaim(it) }
        }
        return result
    }

    suspend fun tryCancelClaim(
        claimId: Int,
        donorId: String,
        timestamp: Long
    ): Boolean {
        val success = dao.cancelClaimTransaction(claimId, donorId, timestamp)
        if (success) {
            getClaimById(claimId)?.let { claim ->
                syncManager?.pushClaim(claim)
                getRequestById(claim.requestId)?.let { syncManager?.pushRequest(it) }
            }
        }
        return success
    }

    suspend fun markClaimAsDroppedOff(
        claimId: Int,
        timestamp: Long
    ): Boolean {
        val success = dao.dropOffClaimTransaction(claimId, timestamp)
        if (success) {
            getClaimById(claimId)?.let { claim ->
                syncManager?.pushClaim(claim)
                getRequestById(claim.requestId)?.let { syncManager?.pushRequest(it) }
            }
        }
        return success
    }

    suspend fun reviewClaim(
        claimId: Int,
        approved: Boolean,
        rejectionReason: String?,
        timestamp: Long
    ): Boolean {
        val success = dao.reviewClaimTransaction(claimId, approved, rejectionReason, timestamp)
        if (success) {
            getClaimById(claimId)?.let { claim ->
                syncManager?.pushClaim(claim)
                getRequestById(claim.requestId)?.let { syncManager?.pushRequest(it) }
            }
        }
        return success
    }

    suspend fun closeRequest(
        requestId: Int,
        timestamp: Long
    ): Boolean {
        val success = dao.closeRequestTransaction(requestId, timestamp)
        if (success) {
            getRequestById(requestId)?.let { syncManager?.pushRequest(it) }
        }
        return success
    }

    suspend fun expireClaim(
        claimId: Int,
        timestamp: Long
    ): Boolean {
        val success = dao.expireClaimTransaction(claimId, timestamp)
        if (success) {
            getClaimById(claimId)?.let { claim ->
                syncManager?.pushClaim(claim)
                getRequestById(claim.requestId)?.let { syncManager?.pushRequest(it) }
            }
        }
        return success
    }

    suspend fun insertFoodBank(foodBank: FoodBankEntity): Long {
        val id = dao.insertFoodBank(foodBank)
        val created = foodBank.copy(id = id.toInt())
        syncManager?.pushFoodBank(created)
        return id
    }

    suspend fun deleteFoodBankByEmail(email: String) {
        dao.deleteFoodBankByEmail(email)
    }
}

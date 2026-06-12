package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirebaseSyncManager(
    private val dao: PantryLinkDao,
    private val scope: CoroutineScope
) {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.e("FirebaseSync", "FirebaseFirestore is not available: ${e.message}")
        null
    }
    private var foodBanksListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null
    private var claimsListener: ListenerRegistration? = null
    private var auditLogsListener: ListenerRegistration? = null

    init {
        startSync()
    }

    fun startSync() {
        val fStore = firestore ?: return
        // 1. Listen to Food Banks
        foodBanksListener?.remove()
        foodBanksListener = fStore.collection("food_banks")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("FirebaseSync", "Food banks sync error", error)
                    return@addSnapshotListener
                }
                if (snapshots != null && !snapshots.isEmpty) {
                    scope.launch(Dispatchers.IO) {
                        for (doc in snapshots.documents) {
                            try {
                                val foodBank = parseFoodBank(doc.data ?: continue, doc.id.toIntOrNull() ?: continue)
                                dao.insertFoodBank(foodBank)
                            } catch (e: Exception) {
                                Log.e("FirebaseSync", "Error parsing food_bank doc", e)
                            }
                        }
                    }
                }
            }

        // 2. Listen to Requests
        requestsListener?.remove()
        requestsListener = fStore.collection("requests")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("FirebaseSync", "Requests sync error", error)
                    return@addSnapshotListener
                }
                if (snapshots != null && !snapshots.isEmpty) {
                    scope.launch(Dispatchers.IO) {
                        for (doc in snapshots.documents) {
                            try {
                                val request = parseRequest(doc.data ?: continue, doc.id.toIntOrNull() ?: continue)
                                dao.insertRequest(request)
                            } catch (e: Exception) {
                                Log.e("FirebaseSync", "Error parsing request doc", e)
                            }
                        }
                    }
                }
            }

        // 3. Listen to Claims
        claimsListener?.remove()
        claimsListener = fStore.collection("claims")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("FirebaseSync", "Claims sync error", error)
                    return@addSnapshotListener
                }
                if (snapshots != null && !snapshots.isEmpty) {
                    scope.launch(Dispatchers.IO) {
                        for (doc in snapshots.documents) {
                            try {
                                val claim = parseClaim(doc.data ?: continue, doc.id.toIntOrNull() ?: continue)
                                dao.insertClaim(claim)
                            } catch (e: Exception) {
                                Log.e("FirebaseSync", "Error parsing claim doc", e)
                            }
                        }
                    }
                }
            }

        // 4. Listen to Audit Logs
        auditLogsListener?.remove()
        auditLogsListener = fStore.collection("audit_logs")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("FirebaseSync", "Audit logs sync error", error)
                    return@addSnapshotListener
                }
                if (snapshots != null && !snapshots.isEmpty) {
                    scope.launch(Dispatchers.IO) {
                        for (doc in snapshots.documents) {
                            try {
                                val log = parseAuditLog(doc.data ?: continue, doc.id.toIntOrNull() ?: continue)
                                dao.insertAuditLog(log)
                            } catch (e: Exception) {
                                Log.e("FirebaseSync", "Error parsing audit_log doc", e)
                            }
                        }
                    }
                }
            }
    }

    fun stopSync() {
        foodBanksListener?.remove()
        requestsListener?.remove()
        claimsListener?.remove()
        auditLogsListener?.remove()
    }

    // --- Push local modifications up to Firestore ---
    fun pushFoodBank(foodBank: FoodBankEntity) {
        val fStore = firestore ?: return
        val mappedId = foodBank.id
        if (mappedId > 0) {
            fStore.collection("food_banks").document(mappedId.toString())
                .set(foodBank.toMap())
                .addOnFailureListener { Log.e("FirebaseSync", "Failed to push food bank", it) }
        }
    }

    fun pushRequest(request: RequestEntity) {
        val fStore = firestore ?: return
        val mappedId = request.id
        if (mappedId > 0) {
            fStore.collection("requests").document(mappedId.toString())
                .set(request.toMap())
                .addOnFailureListener { Log.e("FirebaseSync", "Failed to push request", it) }
        }
    }

    fun deleteRequestOnFirebase(requestId: Int) {
        val fStore = firestore ?: return
        if (requestId > 0) {
            fStore.collection("requests").document(requestId.toString())
                .delete()
                .addOnFailureListener { Log.e("FirebaseSync", "Failed to delete request on Firestore", it) }
        }
    }

    fun pushClaim(claim: ClaimEntity) {
        val fStore = firestore ?: return
        val mappedId = claim.id
        if (mappedId > 0) {
            fStore.collection("claims").document(mappedId.toString())
                .set(claim.toMap())
                .addOnFailureListener { Log.e("FirebaseSync", "Failed to push claim", it) }
        }
    }

    fun pushAuditLog(log: AuditLogEntity) {
        val fStore = firestore ?: return
        val mappedId = log.id
        if (mappedId > 0) {
            fStore.collection("audit_logs").document(mappedId.toString())
                .set(log.toMap())
                .addOnFailureListener { Log.e("FirebaseSync", "Failed to push audit log", it) }
        }
    }

    // --- Parser Helper Functions ---
    private fun parseFoodBank(data: Map<String, Any>, id: Int): FoodBankEntity {
        val addressVal = data["address"] as? String ?: ""
        val zipVal = data["zipCode"] as? String ?: ""
        val rawLat = (data["latitude"] as? Number)?.toDouble() ?: 33.7490
        val rawLng = (data["longitude"] as? Number)?.toDouble() ?: -84.3880

        // If it's default Atlanta center, resolve using our geocoding/fallback helper so we find exact coordinates
        val (lat, lng) = if (rawLat == 33.7490 && rawLng == -84.3880) {
            LocationHelper.getCoordsForAddress(addressVal, zipVal)
        } else {
            Pair(rawLat, rawLng)
        }

        return FoodBankEntity(
            id = id,
            name = data["name"] as? String ?: "",
            address = addressVal,
            zipCode = zipVal,
            city = data["city"] as? String ?: "",
            state = data["state"] as? String ?: "GA",
            latitude = lat,
            longitude = lng,
            phone = data["phone"] as? String ?: "",
            email = data["email"] as? String ?: "",
            verified = data["verified"] as? Boolean ?: true,
            size = data["size"] as? String ?: "",
            operatingHours = data["operatingHours"] as? String ?: "",
            coldStorage = data["coldStorage"] as? Boolean ?: false
        )
    }

    private fun parseRequest(data: Map<String, Any>, id: Int): RequestEntity {
        return RequestEntity(
            id = id,
            foodBankId = (data["foodBankId"] as? Number)?.toInt() ?: 1,
            foodBankName = data["foodBankName"] as? String ?: "",
            title = data["title"] as? String ?: "",
            category = data["category"] as? String ?: "",
            itemDescription = data["itemDescription"] as? String ?: "",
            quantityNeeded = (data["quantityNeeded"] as? Number)?.toInt() ?: 0,
            quantityRemaining = (data["quantityRemaining"] as? Number)?.toInt() ?: 0,
            deadline = data["deadline"] as? String ?: "",
            dropOffLocation = data["dropOffLocation"] as? String ?: "",
            extraNotes = data["extraNotes"] as? String ?: "",
            status = data["status"] as? String ?: "Posted"
        )
    }

    private fun parseClaim(data: Map<String, Any>, id: Int): ClaimEntity {
        val dropoffVal = (data["dropoffConfirmationTimestamp"] as? Number)?.toLong()
        val dropoff = if (dropoffVal == 0L) null else dropoffVal

        val reviewVal = data["foodBankReviewResult"] as? String
        val review = if (reviewVal.isNullOrBlank()) null else reviewVal

        val rejVal = data["rejectionReason"] as? String
        val rejection = if (rejVal.isNullOrBlank()) null else rejVal

        return ClaimEntity(
            id = id,
            requestId = (data["requestId"] as? Number)?.toInt() ?: 0,
            requestTitle = data["requestTitle"] as? String ?: "",
            foodBankName = data["foodBankName"] as? String ?: "",
            donorUserId = data["donorUserId"] as? String ?: "",
            quantityClaimed = (data["quantityClaimed"] as? Number)?.toInt() ?: 0,
            claimTimestamp = (data["claimTimestamp"] as? Number)?.toLong() ?: 0L,
            claimStatus = data["claimStatus"] as? String ?: "Claimed",
            dropoffConfirmationTimestamp = dropoff,
            foodBankReviewResult = review,
            rejectionReason = rejection
        )
    }

    private fun parseAuditLog(data: Map<String, Any>, id: Int): AuditLogEntity {
        return AuditLogEntity(
            id = id,
            donorId = data["donorId"] as? String ?: "",
            requestId = (data["requestId"] as? Number)?.toInt() ?: 0,
            claimId = (data["claimId"] as? Number)?.toInt() ?: 0,
            actionType = data["actionType"] as? String ?: "",
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
            oldStatus = data["oldStatus"] as? String ?: "",
            newStatus = data["newStatus"] as? String ?: ""
        )
    }

    // --- Entity Mappers to Firestore structures ---
    private fun FoodBankEntity.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "address" to address,
        "zipCode" to zipCode,
        "city" to city,
        "state" to state,
        "latitude" to latitude,
        "longitude" to longitude,
        "phone" to phone,
        "email" to email,
        "verified" to verified,
        "size" to size,
        "operatingHours" to operatingHours,
        "coldStorage" to coldStorage
    )

    private fun RequestEntity.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "foodBankId" to foodBankId,
        "foodBankName" to foodBankName,
        "title" to title,
        "category" to category,
        "itemDescription" to itemDescription,
        "quantityNeeded" to quantityNeeded,
        "quantityRemaining" to quantityRemaining,
        "deadline" to deadline,
        "dropOffLocation" to dropOffLocation,
        "extraNotes" to extraNotes,
        "status" to status
    )

    private fun ClaimEntity.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "requestId" to requestId,
        "requestTitle" to requestTitle,
        "foodBankName" to foodBankName,
        "donorUserId" to donorUserId,
        "quantityClaimed" to quantityClaimed,
        "claimTimestamp" to claimTimestamp,
        "claimStatus" to claimStatus,
        "dropoffConfirmationTimestamp" to (dropoffConfirmationTimestamp ?: 0L),
        "foodBankReviewResult" to (foodBankReviewResult ?: ""),
        "rejectionReason" to (rejectionReason ?: "")
    )

    private fun AuditLogEntity.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "donorId" to donorId,
        "requestId" to requestId,
        "claimId" to claimId,
        "actionType" to actionType,
        "timestamp" to timestamp,
        "oldStatus" to oldStatus,
        "newStatus" to newStatus
    )
}

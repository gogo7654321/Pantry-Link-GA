package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "food_banks")
data class FoodBankEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val zipCode: String,
    val city: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val email: String,
    val verified: Boolean = true,
    val size: String = "Medium (100-500/wk)",
    val operatingHours: String = "Mon-Fri 9 AM - 5 PM",
    val coldStorage: Boolean = false
)

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodBankId: Int,
    val foodBankName: String,
    val title: String,
    val category: String, // approved list
    val itemDescription: String, // consistent description format
    val quantityNeeded: Int,
    val quantityRemaining: Int,
    val deadline: String,
    val dropOffLocation: String, // standardized address
    val extraNotes: String,
    val status: String // Posted -> Claimed -> Dropped Off -> Confirmed by Food Bank -> Closed
)

@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requestId: Int,
    val requestTitle: String,
    val foodBankName: String,
    val donorUserId: String,
    val quantityClaimed: Int,
    val claimTimestamp: Long,
    val claimStatus: String, // Claimed, Ready for Drop-Off, Dropped Off, Accepted, Rejected, Cancelled
    val dropoffConfirmationTimestamp: Long? = null,
    val foodBankReviewResult: String? = null,
    val rejectionReason: String? = null // wrong item, opened item, damaged item, expired item, unsafe item, incomplete quantity
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val donorId: String,
    val requestId: Int,
    val claimId: Int,
    val actionType: String,
    val timestamp: Long,
    val oldStatus: String,
    val newStatus: String
)

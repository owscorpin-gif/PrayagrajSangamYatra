package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room Database Entity representing a persisted ritual booking / spiritual schedule.
 */
@Serializable
@Entity(tableName = "ritual_bookings")
data class RitualBookingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "booking_code")
    val bookingCode: String,

    @ColumnInfo(name = "panda_id")
    val pandaId: String,

    @ColumnInfo(name = "panda_name")
    val pandaName: String,

    @ColumnInfo(name = "panda_hindi_name")
    val pandaHindiName: String = "",

    @ColumnInfo(name = "panda_phone")
    val pandaPhone: String = "",

    @ColumnInfo(name = "ghat_location")
    val ghatLocation: String,

    @ColumnInfo(name = "ritual_title")
    val ritualTitle: String,

    @ColumnInfo(name = "ritual_hindi_title")
    val ritualHindiTitle: String = "",

    @ColumnInfo(name = "ritual_category")
    val ritualCategory: String = "Snan & Sankalp",

    @ColumnInfo(name = "booking_date")
    val bookingDate: String,

    @ColumnInfo(name = "time_slot")
    val timeSlot: String,

    @ColumnInfo(name = "pilgrim_name")
    val pilgrimName: String,

    @ColumnInfo(name = "pilgrim_phone")
    val pilgrimPhone: String,

    @ColumnInfo(name = "gotra")
    val gotra: String = "",

    @ColumnInfo(name = "num_persons")
    val numPersons: Int = 1,

    @ColumnInfo(name = "estimated_dakshina")
    val estimatedDakshina: String = "₹501 - ₹1,100",

    @ColumnInfo(name = "samagri_required")
    val samagriRequired: Boolean = true,

    @ColumnInfo(name = "special_notes")
    val specialNotes: String = "",

    @ColumnInfo(name = "booking_timestamp")
    val bookingTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "status")
    val status: String = "CONFIRMED" // "CONFIRMED", "COMPLETED", "CANCELLED"
) {
    val isUpcoming: Boolean
        get() = status == "CONFIRMED"

    val isCompleted: Boolean
        get() = status == "COMPLETED"

    val isCancelled: Boolean
        get() = status == "CANCELLED"
}

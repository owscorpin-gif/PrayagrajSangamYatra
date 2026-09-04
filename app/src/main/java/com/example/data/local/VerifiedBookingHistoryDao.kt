package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for persisting and querying verified booking attempts history.
 */
@Dao
interface VerifiedBookingHistoryDao {

    @Query("SELECT * FROM verified_booking_history ORDER BY verificationTimestamp DESC")
    fun getAllHistory(): Flow<List<VerifiedBookingHistoryEntity>>

    @Query("SELECT * FROM verified_booking_history WHERE status = :status ORDER BY verificationTimestamp DESC")
    fun getHistoryByStatus(status: String): Flow<List<VerifiedBookingHistoryEntity>>

    @Query("SELECT * FROM verified_booking_history WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getByBookingId(bookingId: String): VerifiedBookingHistoryEntity?

    @Query("SELECT * FROM verified_booking_history WHERE verificationCode = :code LIMIT 1")
    suspend fun getByVerificationCode(code: String): VerifiedBookingHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: VerifiedBookingHistoryEntity)

    @Query("DELETE FROM verified_booking_history WHERE bookingId = :bookingId")
    suspend fun deleteByBookingId(bookingId: String)

    @Query("DELETE FROM verified_booking_history")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM verified_booking_history")
    suspend fun getHistoryCount(): Int
}

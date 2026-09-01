package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ritual bookings and spiritual schedules.
 */
@Dao
interface RitualBookingDao {

    @Query("SELECT * FROM ritual_bookings ORDER BY booking_timestamp DESC")
    fun getAllBookings(): Flow<List<RitualBookingEntity>>

    @Query("SELECT * FROM ritual_bookings WHERE status = 'CONFIRMED' ORDER BY booking_timestamp DESC")
    fun getUpcomingBookings(): Flow<List<RitualBookingEntity>>

    @Query("SELECT * FROM ritual_bookings WHERE status != 'CONFIRMED' ORDER BY booking_timestamp DESC")
    fun getPastBookings(): Flow<List<RitualBookingEntity>>

    @Query("SELECT * FROM ritual_bookings WHERE id = :id LIMIT 1")
    fun getBookingById(id: String): Flow<RitualBookingEntity?>

    @Query("SELECT COUNT(*) FROM ritual_bookings WHERE status = 'CONFIRMED'")
    fun getUpcomingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ritual_bookings")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: RitualBookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<RitualBookingEntity>)

    @Update
    suspend fun updateBooking(booking: RitualBookingEntity)

    @Query("UPDATE ritual_bookings SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM ritual_bookings WHERE id = :id")
    suspend fun deleteBookingById(id: String)

    @Query("DELETE FROM ritual_bookings")
    suspend fun deleteAllBookings()
}

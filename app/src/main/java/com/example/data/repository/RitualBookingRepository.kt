package com.example.data.repository

import com.example.data.local.RitualBookingDao
import com.example.data.local.RitualBookingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface RitualBookingRepository {
    val allBookings: Flow<List<RitualBookingEntity>>
    val upcomingBookings: Flow<List<RitualBookingEntity>>
    val pastBookings: Flow<List<RitualBookingEntity>>
    val upcomingCount: Flow<Int>
    val totalCount: Flow<Int>

    fun getBookingById(id: String): Flow<RitualBookingEntity?>
    suspend fun saveBooking(booking: RitualBookingEntity)
    suspend fun markCompleted(id: String)
    suspend fun cancelBooking(id: String)
    suspend fun deleteBooking(id: String)
    suspend fun clearAllBookings()
    suspend fun seedSampleBookingsIfEmpty()
}

class RitualBookingRepositoryImpl(
    private val ritualBookingDao: RitualBookingDao
) : RitualBookingRepository {

    override val allBookings: Flow<List<RitualBookingEntity>> = ritualBookingDao.getAllBookings()
    override val upcomingBookings: Flow<List<RitualBookingEntity>> = ritualBookingDao.getUpcomingBookings()
    override val pastBookings: Flow<List<RitualBookingEntity>> = ritualBookingDao.getPastBookings()
    override val upcomingCount: Flow<Int> = ritualBookingDao.getUpcomingCount()
    override val totalCount: Flow<Int> = ritualBookingDao.getTotalCount()

    override fun getBookingById(id: String): Flow<RitualBookingEntity?> {
        return ritualBookingDao.getBookingById(id)
    }

    override suspend fun saveBooking(booking: RitualBookingEntity) {
        ritualBookingDao.insertBooking(booking)
    }

    override suspend fun markCompleted(id: String) {
        ritualBookingDao.updateStatus(id, "COMPLETED")
    }

    override suspend fun cancelBooking(id: String) {
        ritualBookingDao.updateStatus(id, "CANCELLED")
    }

    override suspend fun deleteBooking(id: String) {
        ritualBookingDao.deleteBookingById(id)
    }

    override suspend fun clearAllBookings() {
        ritualBookingDao.deleteAllBookings()
    }

    override suspend fun seedSampleBookingsIfEmpty() {
        val existing = allBookings.firstOrNull()
        if (existing.isNullOrEmpty()) {
            val sampleBookings = listOf(
                RitualBookingEntity(
                    id = "PY-2026-8841",
                    bookingCode = "PRY-PUROHIT-8841",
                    pandaId = "panda-01",
                    pandaName = "Pt. Ramakant Mishra Shastri",
                    pandaHindiName = "पं. रमाकांत मिश्र शास्त्री",
                    pandaPhone = "+91 94150 28471",
                    ghatLocation = "Triveni Sangam Ghat No. 3",
                    ritualTitle = "Triveni Sangam Snan & Maha Sankalp Vidhi",
                    ritualHindiTitle = "त्रिवेणी संगम स्नान एवं महा संकल्प विधि",
                    ritualCategory = "Snan & Sankalp",
                    bookingDate = "Tomorrow, 05:30 AM",
                    timeSlot = "05:00 AM - 06:30 AM (Brahma Muhurta)",
                    pilgrimName = "Aditi Sharma",
                    pilgrimPhone = "+91 98765 43210",
                    gotra = "Kashyapa",
                    numPersons = 3,
                    estimatedDakshina = "₹501 - ₹1,100",
                    samagriRequired = true,
                    specialNotes = "Family peace and well-being sankalp with sacred deep daan",
                    bookingTimestamp = System.currentTimeMillis() - 3600000L,
                    status = "CONFIRMED"
                ),
                RitualBookingEntity(
                    id = "PY-2026-7219",
                    bookingCode = "PRY-PUROHIT-7219",
                    pandaId = "panda-03",
                    pandaName = "Pt. Raghavendra Tiwari",
                    pandaHindiName = "पं. राघवेन्द्र तिवारी",
                    pandaPhone = "+91 94501 89334",
                    ghatLocation = "Saraswati Ghat & Triveni Sangam",
                    ritualTitle = "Ganga-Yamuna Maha Aarti VIP Seva & Sankalp",
                    ritualHindiTitle = "गंगा-यमुना महा आरती संकल्प",
                    ritualCategory = "Ganga Aarti",
                    bookingDate = "Day After, 06:45 PM",
                    timeSlot = "06:30 PM - 07:30 PM (Sandhya Aarti)",
                    pilgrimName = "Aditi Sharma",
                    pilgrimPhone = "+91 98765 43210",
                    gotra = "Kashyapa",
                    numPersons = 2,
                    estimatedDakshina = "₹1,100 - ₹2,100",
                    samagriRequired = true,
                    specialNotes = "Sunset deepam offering at Yamuna bank",
                    bookingTimestamp = System.currentTimeMillis() - 7200000L,
                    status = "CONFIRMED"
                ),
                RitualBookingEntity(
                    id = "PY-2026-3104",
                    bookingCode = "PRY-PUROHIT-3104",
                    pandaId = "panda-02",
                    pandaName = "Acharya Devendra Shastri",
                    pandaHindiName = "आचार्य देवेन्द्र शास्त्री",
                    pandaPhone = "+91 98390 14522",
                    ghatLocation = "Daraganj Ganga Ghat",
                    ritualTitle = "Maha Rudrabhishek with Sangam Jal",
                    ritualHindiTitle = "संगम जल से महा रुद्राभिषेक",
                    ritualCategory = "Rudrabhishek",
                    bookingDate = "Last Week, Aug 24",
                    timeSlot = "08:00 AM - 09:30 AM (Pratah Kaal)",
                    pilgrimName = "Aditi Sharma",
                    pilgrimPhone = "+91 98765 43210",
                    gotra = "Kashyapa",
                    numPersons = 1,
                    estimatedDakshina = "₹1,501 - ₹3,500",
                    samagriRequired = true,
                    specialNotes = "Panchamrit abhishekam performed successfully",
                    bookingTimestamp = System.currentTimeMillis() - (7 * 24 * 3600000L),
                    status = "COMPLETED"
                )
            )
            ritualBookingDao.insertAll(sampleBookings)
        }
    }
}

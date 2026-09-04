package com.example.data.repository

import com.example.data.local.RitualBookingDao
import com.example.data.local.RitualBookingEntity
import com.example.data.local.VerifiedBookingHistoryDao
import com.example.data.local.VerifiedBookingHistoryEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Locale

interface BookingVerificationRepository {
    val verificationHistory: Flow<List<VerificationHistoryItem>>
    suspend fun verifyBookingId(query: String): OfficialBookingRecord
    suspend fun parseAndVerifyQrPayload(qrRawPayload: String): OfficialBookingRecord
    suspend fun getFullHistoryRecord(bookingId: String): OfficialBookingRecord?
    suspend fun deleteHistoryItem(bookingId: String)
    suspend fun clearHistory()
}

class BookingVerificationRepositoryImpl(
    private val ritualBookingDao: RitualBookingDao? = null,
    private val historyDao: VerifiedBookingHistoryDao? = null
) : BookingVerificationRepository {

    private val _inMemoryHistory = MutableStateFlow<List<VerificationHistoryItem>>(emptyList())

    override val verificationHistory: Flow<List<VerificationHistoryItem>> =
        if (historyDao != null) {
            historyDao.getAllHistory().map { entities ->
                entities.map { entity ->
                    val parsedCategory = try {
                        ReservationCategory.valueOf(entity.category)
                    } catch (e: Exception) {
                        ReservationCategory.RITUAL_SANKALP
                    }
                    val parsedStatus = try {
                        VerificationStatus.valueOf(entity.status)
                    } catch (e: Exception) {
                        VerificationStatus.OFFICIALLY_VERIFIED
                    }
                    VerificationHistoryItem(
                        id = "${entity.bookingId}-${entity.verificationTimestamp}",
                        bookingId = entity.bookingId,
                        verificationCode = entity.verificationCode,
                        category = parsedCategory,
                        title = entity.title,
                        status = parsedStatus,
                        timestamp = entity.verificationTimestamp,
                        providerName = entity.assignedProviderName,
                        serviceLocation = entity.serviceLocation,
                        tariff = entity.fixedGovtTariff,
                        isBlacklisted = entity.isBlacklisted
                    )
                }
            }
        } else {
            _inMemoryHistory.asStateFlow()
        }

    // Master Registry of Official Government & Shrine Board Pre-registered Bookings
    private val officialMasterDatabase = listOf(
        OfficialBookingRecord(
            bookingId = "PY-2026-8841",
            verificationCode = "PRY-PUROHIT-8841",
            category = ReservationCategory.RITUAL_SANKALP,
            title = "Triveni Sangam Snan & Maha Sankalp Vidhi",
            hindiTitle = "त्रिवेणी संगम स्नान एवं महा संकल्प विधि",
            authorityName = "Prayagraj Mela Pradhikaran & Prayag Mahatmya Purohit Parishad",
            registrationCertificateNo = "UP-PRY-PND-2026-08841",
            pilgrimName = "Aditi Sharma",
            pilgrimPhone = "+91 98765 43210",
            gotraOrParty = "Kashyapa Gotra (3 Persons)",
            numPersons = 3,
            assignedProviderName = "Pt. Ramakant Mishra Shastri",
            providerBadgeNo = "PND-REG-001 (Gold Verified Panda)",
            providerContact = "+91 94150 28471",
            serviceLocation = "Triveni Sangam Ghat No. 3 (Platform A)",
            latitude = 25.4285,
            longitude = 81.8860,
            slotDateTime = "Tomorrow, 05:30 AM - 06:30 AM (Brahma Muhurta)",
            fixedGovtTariff = "₹1,100 (Official Fixed Dakshina Standard)",
            status = VerificationStatus.OFFICIALLY_VERIFIED,
            qrHashSignature = "SHA256:7f8a92bc3d4e5f6a1b2c3d4e5f6a7b8c9d0e1f2a",
            antiCounterfeitSeal = "SEAL-GOVT-UP-PRY-2026-VALID",
            securityNotes = "Registered with Shri Prayag Mahatmya Purohit Parishad. Brass badge verified. Fixed tariff capped by Mela administration.",
            includesLifeJacketOrSamagri = true
        ),
        OfficialBookingRecord(
            bookingId = "PY-2026-7219",
            verificationCode = "PRY-PUROHIT-7219",
            category = ReservationCategory.RITUAL_SANKALP,
            title = "Ganga-Yamuna Maha Aarti VIP Seva & Sankalp",
            hindiTitle = "गंगा-यमुना महा आरती संकल्प",
            authorityName = "Prayagraj Mela Pradhikaran & Sangam Aarti Trust",
            registrationCertificateNo = "UP-PRY-AARTI-2026-07219",
            pilgrimName = "Aditi Sharma",
            pilgrimPhone = "+91 98765 43210",
            gotraOrParty = "Kashyapa Gotra (2 Persons)",
            numPersons = 2,
            assignedProviderName = "Pt. Raghavendra Tiwari",
            providerBadgeNo = "PND-REG-003 (Official Aarti Purohit)",
            providerContact = "+91 94501 89334",
            serviceLocation = "Saraswati Ghat Aarti Pavilion (VIP Tier 1)",
            latitude = 25.4308,
            longitude = 81.8790,
            slotDateTime = "Day After, 06:45 PM - 07:30 PM (Sandhya Aarti)",
            fixedGovtTariff = "₹501 (Official Aarti Seva Pass)",
            status = VerificationStatus.OFFICIALLY_VERIFIED,
            qrHashSignature = "SHA256:4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c",
            antiCounterfeitSeal = "SEAL-AARTI-TRUST-PRY-2026",
            securityNotes = "Front-row consecrated seating pass. Verified by District Administration & Tourism Cell.",
            includesLifeJacketOrSamagri = true
        ),
        OfficialBookingRecord(
            bookingId = "BOAT-SG-2026-0429",
            verificationCode = "PRY-BOAT-0429",
            category = ReservationCategory.BOAT_CONFLUENCE,
            title = "Official Sangam Confluence Motor Boat Ride",
            hindiTitle = "आधिकारिक संगम मोटर नौका यात्रा",
            authorityName = "UP Inland Waterways & Prayagraj Navik Kalyan Samiti",
            registrationCertificateNo = "UP-PRY-BOAT-REG-0429",
            pilgrimName = "Vikram K. & Family",
            pilgrimPhone = "+91 98112 34567",
            gotraOrParty = "4 Passengers (Reserved)",
            numPersons = 4,
            assignedProviderName = "Captain Rajesh Nishad (Navik No. 42)",
            providerBadgeNo = "BOAT-PRY-MB-042 (Inland Waterways Certified)",
            providerContact = "+91 94152 77889",
            serviceLocation = "Kila Ghat Berth No. 2 (Near Akbar Fort)",
            latitude = 25.4290,
            longitude = 81.8840,
            slotDateTime = "Today, 04:30 PM - 05:45 PM (Sunset Confluence Slot)",
            fixedGovtTariff = "₹600 (Govt Fixed Tariff: ₹150/person)",
            status = VerificationStatus.OFFICIALLY_VERIFIED,
            qrHashSignature = "SHA256:1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b",
            antiCounterfeitSeal = "SEAL-BOAT-SAFETY-INSPECTED-2026",
            securityNotes = "Life jackets mandatory & verified on board. Max 6 passengers limit strictly enforced. Overcharging is a cognizable offense.",
            includesLifeJacketOrSamagri = true
        ),
        OfficialBookingRecord(
            bookingId = "DARSHAN-BH-2026-108",
            verificationCode = "PRY-VIP-BH108",
            category = ReservationCategory.TEMPLE_VIP_DARSHAN,
            title = "Shri Bade Hanuman Ji VIP Shringar Aarti Pass",
            hindiTitle = "श्री बड़े हनुमान जी प्रात:काल श्रृंगार आरती पास",
            authorityName = "Shri Bade Hanuman Temple Administration & Trust",
            registrationCertificateNo = "UP-PRY-TMPL-BH-2026-0108",
            pilgrimName = "Ramesh Chandra Gupta",
            pilgrimPhone = "+91 97920 12345",
            gotraOrParty = "Gupta Family (2 Devotees)",
            numPersons = 2,
            assignedProviderName = "Temple Seva Trust Desk (Mahant Balgiri Ji)",
            providerBadgeNo = "TMPL-TRUST-OFFICIAL-DESK",
            providerContact = "+91 532 250 1199",
            serviceLocation = "Bade Hanuman Temple (Bandhwa Par Hanuman Ji)",
            latitude = 25.4312,
            longitude = 81.8835,
            slotDateTime = "Tomorrow, 05:00 AM - 06:00 AM",
            fixedGovtTariff = "₹0 (Free Consecrated Darshan Pass)",
            status = VerificationStatus.OFFICIALLY_VERIFIED,
            qrHashSignature = "SHA256:9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e",
            antiCounterfeitSeal = "SEAL-HANUMAN-TEMPLE-AUTHORIZED",
            securityNotes = "Official queue-bypass pass granted through Shri Bade Hanuman Temple Trust. Valid with Govt Photo ID.",
            includesLifeJacketOrSamagri = true
        ),
        OfficialBookingRecord(
            bookingId = "TENT-KMB-2026-99",
            verificationCode = "PRY-TENT-099",
            category = ReservationCategory.TENT_ACCOMMODATION,
            title = "Kumbh Sangam Luxury Tent City Reservation",
            hindiTitle = "संगम टेंट सिटी आधिकारिक आवास",
            authorityName = "Uttar Pradesh Tourism Development Corporation (UPTDC)",
            registrationCertificateNo = "UPTDC-PRY-TENT-2026-0099",
            pilgrimName = "Sunita Verma",
            pilgrimPhone = "+91 98230 45678",
            gotraOrParty = "Deluxe Tent #D-14 (2 Guests)",
            numPersons = 2,
            assignedProviderName = "UPTDC Hospitality & Camp Desk",
            providerBadgeNo = "UPTDC-AUTH-CAMP-2026",
            providerContact = "+91 532 240 8877",
            serviceLocation = "Arail Ghat Tent City (Sector 4)",
            latitude = 25.4210,
            longitude = 81.8805,
            slotDateTime = "Check-in: Today 12:00 PM • Check-out: Tomorrow 11:00 AM",
            fixedGovtTariff = "₹4,500 (All-inclusive including Satvik Bhojan)",
            status = VerificationStatus.OFFICIALLY_VERIFIED,
            qrHashSignature = "SHA256:3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d",
            antiCounterfeitSeal = "SEAL-UPTDC-APPROVED-CAMP",
            securityNotes = "Verified eco-tourism camp with 24x7 security personnel and RO water filtration.",
            includesLifeJacketOrSamagri = true
        ),

        // Flagged Fraud / Blacklisted Tout Test Data
        OfficialBookingRecord(
            bookingId = "FAKE-TOUT-9988",
            verificationCode = "SCAM-UNAUTH-9988",
            category = ReservationCategory.RITUAL_SANKALP,
            title = "⚠️ FRAUDULENT PASS: Unauthorized Ghat Broker / Fake Sankalp",
            hindiTitle = "चेतावनी: अनधिकृत दलाल द्वारा फर्जी रसीद",
            authorityName = "BLACK-LISTED BY PRAYAGRAJ TOURISM POLICE & MELA CELL",
            registrationCertificateNo = "INVALID-NO-REGISTRATION-FOUND",
            pilgrimName = "Targeted Pilgrim (Unverified Receipt)",
            pilgrimPhone = "N/A",
            gotraOrParty = "Forged Entry",
            numPersons = 1,
            assignedProviderName = "UNREGISTERED TOUT / EXTORTION GANG",
            providerBadgeNo = "NO VALID GOVT ID / BOGUS BADGE",
            providerContact = "Unknown Mobile Number",
            serviceLocation = "Unauthorized Alley near Sangam Nose",
            slotDateTime = "INVALID DATE",
            fixedGovtTariff = "DEMANDING ₹3,500 (ILLEGAL EXTORTION)",
            status = VerificationStatus.FLAGGED_BLACKLISTED_TOUT,
            qrHashSignature = "INVALID_HASH_TAMPERED",
            antiCounterfeitSeal = "COUNTERFEIT_ALERT_TRIGGERED",
            securityNotes = "CRITICAL ALERT: This booking code belongs to an unauthorized tout previously reported for demanding illicit dakshina. DO NOT PAY. Report immediately to Police Booth or call 1920.",
            isBlacklisted = true,
            blacklistReason = "Forged receipt format reported at Sangam Ghat #2. Person operating without Panda Parishad certification.",
            reportHelplineNumber = "1920"
        ),
        OfficialBookingRecord(
            bookingId = "EXP-2024-0012",
            verificationCode = "PRY-OLD-2024",
            category = ReservationCategory.RITUAL_SANKALP,
            title = "Expired Snan Permit (Magh Mela 2024)",
            hindiTitle = "समाप्त हो चुकी पुरानी रसीद",
            authorityName = "Prayagraj Mela Pradhikaran (Archive)",
            registrationCertificateNo = "UP-PRY-ARCHIVE-2024-0012",
            pilgrimName = "Past Pilgrim",
            pilgrimPhone = "N/A",
            gotraOrParty = "Expired",
            numPersons = 1,
            assignedProviderName = "Pt. Brijesh Shastri (Past Slot)",
            providerBadgeNo = "PND-ARCHIVE-2024",
            providerContact = "N/A",
            serviceLocation = "Dashashwamedh Ghat",
            slotDateTime = "January 14, 2024 (EXPIRED)",
            fixedGovtTariff = "₹501",
            status = VerificationStatus.EXPIRED,
            qrHashSignature = "SHA256:expired_hash_2024",
            antiCounterfeitSeal = "SEAL-EXPIRED",
            securityNotes = "This pass has expired and is no longer valid for entry or darshan in the current season.",
            includesLifeJacketOrSamagri = false
        )
    )

    override suspend fun getFullHistoryRecord(bookingId: String): OfficialBookingRecord? {
        val cleanId = bookingId.trim().uppercase(Locale.ROOT)
        // 1. Check DAO entity first
        historyDao?.getByBookingId(cleanId)?.let {
            return it.toDomainModel()
        }
        // 2. Check master database
        return officialMasterDatabase.find {
            it.bookingId.equals(cleanId, ignoreCase = true) ||
            it.verificationCode.equals(cleanId, ignoreCase = true)
        }
    }

    override suspend fun verifyBookingId(query: String): OfficialBookingRecord {
        val sanitizedQuery = query.trim().uppercase(Locale.ROOT)

        // 1. Check if it matches an official record in the master registry
        val matchedRecord = officialMasterDatabase.find {
            it.bookingId.equals(sanitizedQuery, ignoreCase = true) ||
            it.verificationCode.equals(sanitizedQuery, ignoreCase = true) ||
            it.registrationCertificateNo.equals(sanitizedQuery, ignoreCase = true)
        }

        if (matchedRecord != null) {
            recordHistory(matchedRecord)
            return matchedRecord
        }

        // 2. Check local Room Database ritual bookings if available
        if (ritualBookingDao != null) {
            try {
                val localEntities = ritualBookingDao.getAllBookings().firstOrNull() ?: emptyList()
                val foundLocal = localEntities.find {
                    it.id.equals(sanitizedQuery, ignoreCase = true) ||
                    it.bookingCode.equals(sanitizedQuery, ignoreCase = true)
                }
                if (foundLocal != null) {
                    val convertedRecord = convertLocalEntityToOfficialRecord(foundLocal)
                    recordHistory(convertedRecord)
                    return convertedRecord
                }
            } catch (e: Exception) {
                // Ignore room lookup error and fallback
            }
        }

        // 3. If query contains keywords of known scams or touts
        if (sanitizedQuery.contains("FAKE") || sanitizedQuery.contains("TOUT") || sanitizedQuery.contains("SCAM") || sanitizedQuery.contains("UNREG")) {
            val fraudRecord = OfficialBookingRecord(
                bookingId = sanitizedQuery,
                verificationCode = "UNREGISTERED-$sanitizedQuery",
                category = ReservationCategory.RITUAL_SANKALP,
                title = "⚠️ FRAUD WARNING: Unregistered / Suspect Reservation",
                hindiTitle = "धोखाधड़ी चेतावनी: फर्जी या अवैध रसीद",
                authorityName = "Prayagraj Mela Pradhikaran Anti-Fraud Database",
                registrationCertificateNo = "NO-GOVT-RECORD-EXISTS",
                pilgrimName = "Unknown Devotee",
                pilgrimPhone = "N/A",
                gotraOrParty = "Unverified",
                assignedProviderName = "UNAUTHORIZED INDIVIDUAL / TOUT",
                providerBadgeNo = "NONE (NO BADGE)",
                providerContact = "Not in Directory",
                serviceLocation = "Unknown / Unsanctioned Ghat Location",
                slotDateTime = "NO RECORD FOUND",
                fixedGovtTariff = "INVALID",
                status = VerificationStatus.FLAGGED_BLACKLISTED_TOUT,
                qrHashSignature = "TAMPERED_OR_UNKNOWN",
                antiCounterfeitSeal = "SECURITY_ALERT_TRIGGERED",
                securityNotes = "This booking ID does not correspond to any registered purohit, boat operator, or temple pass in the official government registry. Do NOT transfer money.",
                isBlacklisted = true,
                blacklistReason = "Suspicious identifier flagged as counterfeit/unauthorized broker.",
                reportHelplineNumber = "1920"
            )
            recordHistory(fraudRecord)
            return fraudRecord
        }

        // 4. Default: Unregistered / Not Found
        val notFoundRecord = OfficialBookingRecord(
            bookingId = sanitizedQuery.ifEmpty { "UNKNOWN" },
            verificationCode = "UNVERIFIED-${System.currentTimeMillis() % 10000}",
            category = ReservationCategory.RITUAL_SANKALP,
            title = "❌ RESERVATION NOT FOUND IN OFFICIAL REGISTRY",
            hindiTitle = "आधिकारिक डेटाबेस में बुकिंग नहीं मिली",
            authorityName = "Prayagraj Mela Pradhikaran & Central Registry",
            registrationCertificateNo = "NOT-REGISTERED",
            pilgrimName = "No Pilgrim Record Found",
            pilgrimPhone = "N/A",
            gotraOrParty = "N/A",
            assignedProviderName = "No Registered Purohit or Boat Associated",
            providerBadgeNo = "N/A",
            providerContact = "N/A",
            serviceLocation = "Unverified",
            slotDateTime = "N/A",
            fixedGovtTariff = "N/A",
            status = VerificationStatus.SUSPICIOUS_UNREGISTERED_FRAUD,
            qrHashSignature = "NONE",
            antiCounterfeitSeal = "UNVERIFIED",
            securityNotes = "Caution: The entered Booking ID was not found in the official registry. Please cross-check with your booking slip or verify with the official helpdesk at Sangam Control Tower.",
            isBlacklisted = false,
            blacklistReason = "ID not found in central database. Potential counterfeit or typographical error.",
            reportHelplineNumber = "1920"
        )
        recordHistory(notFoundRecord)
        return notFoundRecord
    }

    override suspend fun parseAndVerifyQrPayload(qrRawPayload: String): OfficialBookingRecord {
        val trimmed = qrRawPayload.trim()

        // Extract ID if QR payload is a URL (e.g., https://prayagraj.up.gov.in/verify?id=PY-2026-8841 or ?code=...)
        val extractedId = when {
            trimmed.contains("id=", ignoreCase = true) -> {
                trimmed.substringAfter("id=", "").substringBefore("&").substringBefore("#")
            }
            trimmed.contains("code=", ignoreCase = true) -> {
                trimmed.substringAfter("code=", "").substringBefore("&").substringBefore("#")
            }
            trimmed.contains("booking=", ignoreCase = true) -> {
                trimmed.substringAfter("booking=", "").substringBefore("&").substringBefore("#")
            }
            else -> trimmed
        }

        return verifyBookingId(extractedId.ifBlank { trimmed })
    }

    override suspend fun deleteHistoryItem(bookingId: String) {
        if (historyDao != null) {
            historyDao.deleteByBookingId(bookingId)
        } else {
            val currentList = _inMemoryHistory.value.toMutableList()
            currentList.removeAll { it.bookingId.equals(bookingId, ignoreCase = true) }
            _inMemoryHistory.value = currentList
        }
    }

    override suspend fun clearHistory() {
        if (historyDao != null) {
            historyDao.clearAllHistory()
        } else {
            _inMemoryHistory.value = emptyList()
        }
    }

    private suspend fun recordHistory(record: OfficialBookingRecord) {
        val now = System.currentTimeMillis()
        if (historyDao != null) {
            val entity = VerifiedBookingHistoryEntity.fromDomainModel(record, timestamp = now)
            historyDao.insertOrUpdate(entity)
        } else {
            val item = VerificationHistoryItem(
                id = "${record.bookingId}-$now",
                bookingId = record.bookingId,
                verificationCode = record.verificationCode,
                category = record.category,
                title = record.title,
                status = record.status,
                timestamp = now,
                providerName = record.assignedProviderName,
                serviceLocation = record.serviceLocation,
                tariff = record.fixedGovtTariff,
                isBlacklisted = record.isBlacklisted
            )
            val currentList = _inMemoryHistory.value.toMutableList()
            currentList.removeAll { it.bookingId.equals(record.bookingId, ignoreCase = true) }
            currentList.add(0, item)
            _inMemoryHistory.value = currentList.take(30)
        }
    }

    private fun convertLocalEntityToOfficialRecord(entity: RitualBookingEntity): OfficialBookingRecord {
        val status = when (entity.status) {
            "CONFIRMED" -> VerificationStatus.OFFICIALLY_VERIFIED
            "COMPLETED" -> VerificationStatus.OFFICIALLY_VERIFIED
            "CANCELLED" -> VerificationStatus.CANCELLED
            else -> VerificationStatus.OFFICIALLY_VERIFIED
        }

        return OfficialBookingRecord(
            bookingId = entity.id,
            verificationCode = entity.bookingCode.ifEmpty { "PRY-PUROHIT-${entity.id.takeLast(4)}" },
            category = ReservationCategory.RITUAL_SANKALP,
            title = entity.ritualTitle,
            hindiTitle = entity.ritualHindiTitle,
            authorityName = "Prayagraj Mela Pradhikaran & Prayag Mahatmya Parishad",
            registrationCertificateNo = "UP-PRY-ROOM-${entity.id}",
            pilgrimName = entity.pilgrimName,
            pilgrimPhone = entity.pilgrimPhone,
            gotraOrParty = if (entity.gotra.isNotBlank()) "${entity.gotra} Gotra (${entity.numPersons} Persons)" else "${entity.numPersons} Devotees",
            numPersons = entity.numPersons,
            assignedProviderName = entity.pandaName,
            providerBadgeNo = "PND-AUTH-${entity.pandaId}",
            providerContact = entity.pandaPhone,
            serviceLocation = entity.ghatLocation,
            latitude = 25.4285,
            longitude = 81.8860,
            slotDateTime = "${entity.bookingDate} (${entity.timeSlot})",
            fixedGovtTariff = entity.estimatedDakshina,
            status = status,
            qrHashSignature = "SHA256:local_verified_${entity.id}",
            antiCounterfeitSeal = "SEAL-ROOM-DB-VERIFIED",
            securityNotes = "Authenticated via Pilgrim Local Secure Storage & Shrine Authority registry. Fixed tariff verified.",
            includesLifeJacketOrSamagri = entity.samagriRequired
        )
    }
}


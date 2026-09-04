package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.OfficialBookingRecord
import com.example.data.model.ReservationCategory
import com.example.data.model.VerificationStatus

/**
 * Room Entity for caching verified booking attempts and validation results locally.
 * Enables offline access, quick cross-referencing, and anti-fraud auditing history.
 */
@Entity(tableName = "verified_booking_history")
data class VerifiedBookingHistoryEntity(
    @PrimaryKey
    val bookingId: String,
    val verificationCode: String,
    val category: String, // Stored as enum name
    val title: String,
    val hindiTitle: String = "",
    val authorityName: String = "",
    val registrationCertificateNo: String = "",
    val pilgrimName: String = "",
    val pilgrimPhone: String = "",
    val gotraOrParty: String = "",
    val numPersons: Int = 1,
    val assignedProviderName: String = "",
    val providerBadgeNo: String = "",
    val providerContact: String = "",
    val serviceLocation: String = "",
    val latitude: Double = 25.4300,
    val longitude: Double = 81.8845,
    val slotDateTime: String = "",
    val fixedGovtTariff: String = "",
    val status: String, // Stored as VerificationStatus.name
    val qrHashSignature: String = "",
    val antiCounterfeitSeal: String = "",
    val verificationTimestamp: Long = System.currentTimeMillis(),
    val securityNotes: String = "",
    val isBlacklisted: Boolean = false,
    val blacklistReason: String? = null,
    val reportHelplineNumber: String = "1920",
    val includesLifeJacketOrSamagri: Boolean = true
) {
    /**
     * Converts this Room Entity back into the domain [OfficialBookingRecord].
     */
    fun toDomainModel(): OfficialBookingRecord {
        val parsedCategory = try {
            ReservationCategory.valueOf(category)
        } catch (e: Exception) {
            ReservationCategory.RITUAL_SANKALP
        }

        val parsedStatus = try {
            VerificationStatus.valueOf(status)
        } catch (e: Exception) {
            VerificationStatus.OFFICIALLY_VERIFIED
        }

        return OfficialBookingRecord(
            bookingId = bookingId,
            verificationCode = verificationCode,
            category = parsedCategory,
            title = title,
            hindiTitle = hindiTitle,
            authorityName = authorityName,
            registrationCertificateNo = registrationCertificateNo,
            pilgrimName = pilgrimName,
            pilgrimPhone = pilgrimPhone,
            gotraOrParty = gotraOrParty,
            numPersons = numPersons,
            assignedProviderName = assignedProviderName,
            providerBadgeNo = providerBadgeNo,
            providerContact = providerContact,
            serviceLocation = serviceLocation,
            latitude = latitude,
            longitude = longitude,
            slotDateTime = slotDateTime,
            fixedGovtTariff = fixedGovtTariff,
            status = parsedStatus,
            qrHashSignature = qrHashSignature,
            antiCounterfeitSeal = antiCounterfeitSeal,
            verificationTimestamp = verificationTimestamp,
            securityNotes = securityNotes,
            isBlacklisted = isBlacklisted,
            blacklistReason = blacklistReason,
            reportHelplineNumber = reportHelplineNumber,
            includesLifeJacketOrSamagri = includesLifeJacketOrSamagri
        )
    }

    companion object {
        /**
         * Creates a Room Entity from an [OfficialBookingRecord].
         */
        fun fromDomainModel(record: OfficialBookingRecord, timestamp: Long = System.currentTimeMillis()): VerifiedBookingHistoryEntity {
            return VerifiedBookingHistoryEntity(
                bookingId = record.bookingId,
                verificationCode = record.verificationCode,
                category = record.category.name,
                title = record.title,
                hindiTitle = record.hindiTitle,
                authorityName = record.authorityName,
                registrationCertificateNo = record.registrationCertificateNo,
                pilgrimName = record.pilgrimName,
                pilgrimPhone = record.pilgrimPhone,
                gotraOrParty = record.gotraOrParty,
                numPersons = record.numPersons,
                assignedProviderName = record.assignedProviderName,
                providerBadgeNo = record.providerBadgeNo,
                providerContact = record.providerContact,
                serviceLocation = record.serviceLocation,
                latitude = record.latitude,
                longitude = record.longitude,
                slotDateTime = record.slotDateTime,
                fixedGovtTariff = record.fixedGovtTariff,
                status = record.status.name,
                qrHashSignature = record.qrHashSignature,
                antiCounterfeitSeal = record.antiCounterfeitSeal,
                verificationTimestamp = timestamp,
                securityNotes = record.securityNotes,
                isBlacklisted = record.isBlacklisted,
                blacklistReason = record.blacklistReason,
                reportHelplineNumber = record.reportHelplineNumber,
                includesLifeJacketOrSamagri = record.includesLifeJacketOrSamagri
            )
        }
    }
}

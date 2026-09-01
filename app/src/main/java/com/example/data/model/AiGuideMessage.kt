package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class MessageSender {
    USER, AI, SYSTEM_ALERT
}

data class DakshinaGuidanceItem(
    val ritualName: String,
    val hindiName: String,
    val standardMinFee: Int,
    val standardMaxFee: Int,
    val samagriIncluded: Boolean,
    val description: String,
    val warnings: String
)

val STANDARD_DAKSHINA_TARIFF = listOf(
    DakshinaGuidanceItem(
        ritualName = "Sankalpa (Basic Dip & Holy Oath)",
        hindiName = "संकल्प एवं स्नान",
        standardMinFee = 101,
        standardMaxFee = 251,
        samagriIncluded = true,
        description = "Sacred vow with flowers, kush grass, and Gangajal before holy bathing.",
        warnings = "Voluntary dakshina. Never pay thousands for simple Sankalpa."
    ),
    DakshinaGuidanceItem(
        ritualName = "Daily Puja / Aarti Assistance",
        hindiName = "दैनिक पूजा एवं आरती",
        standardMinFee = 251,
        standardMaxFee = 501,
        samagriIncluded = true,
        description = "Vedic chanting and flower offering assistance at Ghat temples.",
        warnings = "Ask priest to state all puja samagri requirements in advance."
    ),
    DakshinaGuidanceItem(
        ritualName = "Full Pind Daan / Shradh Karma",
        hindiName = "पिंड दान एवं श्राद्ध कर्म",
        standardMinFee = 1100,
        standardMaxFee = 3100,
        samagriIncluded = true,
        description = "Ancestral liberation rites with 16 Pind offerings, sesame, barley, and Vedic mantras.",
        warnings = "Varies by materials provided. Agree on complete package before starting."
    ),
    DakshinaGuidanceItem(
        ritualName = "Rudrabhishek Puja",
        hindiName = "रुद्राभिषेक पूजा",
        standardMinFee = 501,
        standardMaxFee = 1100,
        samagriIncluded = false,
        description = "Sacred Panchamrit abhishek of Lord Shiva with Vedic Suktas at Mankameshwar/Someshwar.",
        warnings = "Clarify whether milk and honey are supplied by pilgrim or priest."
    ),
    DakshinaGuidanceItem(
        ritualName = "Vedic Havan & Yagya",
        hindiName = "वैदिक हवन एवं यज्ञ",
        standardMinFee = 1500,
        standardMaxFee = 3500,
        samagriIncluded = true,
        description = "Navagraha and Gayatri Havan with sacred wood, ghee, and herbal offerings.",
        warnings = "Verify number of Brahmins and duration before beginning."
    ),
    DakshinaGuidanceItem(
        ritualName = "Asthi Visarjan Vidhi",
        hindiName = "अस्थि विसर्जन विधि",
        standardMinFee = 501,
        standardMaxFee = 1500,
        samagriIncluded = true,
        description = "Immersion rites in Triveni Sangam with Vedic mantras and boat facilitation.",
        warnings = "Boat charges should be fixed separately as per official Nagar Nigam rates."
    ),
    DakshinaGuidanceItem(
        ritualName = "Mundan Sanskar (Tonsure)",
        hindiName = "मुंडन संस्कार",
        standardMinFee = 251,
        standardMaxFee = 501,
        samagriIncluded = true,
        description = "First hair-cutting ceremony with Vedic blessings and Ganga snan sankalp.",
        warnings = "Barber charges are usually ₹50-₹100 separately."
    )
)

data class AiGuideMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
    val suggestedRitual: String? = null,
    val fairDakshinaRange: String? = null,
    val showVerifiedPurohitAction: Boolean = false,
    val showHelplineAction: Boolean = false
)

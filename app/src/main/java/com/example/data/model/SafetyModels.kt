package com.example.data.model

enum class ScamRiskLevel {
    HIGH,
    CRITICAL,
    MODERATE
}

enum class HelplineCategory {
    KUMBH_CENTRAL,
    POLICE,
    RIVER_RESCUE,
    MEDICAL,
    WOMEN_SAFETY,
    TOURIST_ASSISTANCE
}

data class EmergencyContact(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val number: String,
    val formattedDisplayNumber: String,
    val description: String,
    val category: HelplineCategory,
    val iconEmoji: String,
    val isTollFree: Boolean = true,
    val is24x7: Boolean = true
)

data class ScamAlert(
    val id: String,
    val categoryTitle: String,
    val title: String,
    val hindiTitle: String,
    val iconEmoji: String,
    val riskLevel: ScamRiskLevel,
    val warningSummary: String,
    val scamTechniqueDetails: String,
    val redFlags: List<String>,
    val officialRule: String,
    val preventionTips: List<String>,
    val reportingAuthority: String
)

data class SafetyChecklistItem(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val description: String,
    val isCritical: Boolean = false
)

data class OfficialTariffReference(
    val serviceName: String,
    val approvedCap: String,
    val regulatoryNote: String
)

val OFFICIAL_EMERGENCY_CONTACTS = listOf(
    EmergencyContact(
        id = "kumbh-1920",
        title = "Kumbh Mela Central Helpline",
        hindiTitle = "कुंभ मेला केंद्रीय 24x7 हेल्पलाइन",
        number = "1920",
        formattedDisplayNumber = "1920 (Toll Free)",
        description = "24x7 Multi-lingual pilgrim assistance, lost & found, crowd info, and dispute grievance.",
        category = HelplineCategory.KUMBH_CENTRAL,
        iconEmoji = "🏛️",
        isTollFree = true,
        is24x7 = true
    ),
    EmergencyContact(
        id = "police-112",
        title = "Police Emergency Response",
        hindiTitle = "यूपी पुलिस आपातकालीन सेवा",
        number = "112",
        formattedDisplayNumber = "112",
        description = "Immediate police dispatch, anti-touting squad, and dispute resolution across all ghats.",
        category = HelplineCategory.POLICE,
        iconEmoji = "👮",
        isTollFree = true,
        is24x7 = true
    ),
    EmergencyContact(
        id = "water-police-1077",
        title = "Water Police & SDRF River Rescue",
        hindiTitle = "जल पुलिस एवं आपदा मोचन दल (SDRF)",
        number = "1077",
        formattedDisplayNumber = "1077 / 0532-2500000",
        description = "Sangam deep-water emergency, boat safety violations, river patrol, and diving rescue.",
        category = HelplineCategory.RIVER_RESCUE,
        iconEmoji = "🛟",
        isTollFree = true,
        is24x7 = true
    ),
    EmergencyContact(
        id = "mela-control-room",
        title = "Sangam Mela Control Room",
        hindiTitle = "संगम मेला प्रशासनिक नियंत्रण कक्ष",
        number = "05322500000",
        formattedDisplayNumber = "0532-2500000",
        description = "Direct liaison with District Magistrate, Sector Magistrates, and Ghat in-charges.",
        category = HelplineCategory.POLICE,
        iconEmoji = "📡",
        isTollFree = false,
        is24x7 = true
    ),
    EmergencyContact(
        id = "medical-108",
        title = "Ambulance & Mela Field Hospital",
        hindiTitle = "एम्बुलेंस एवं मेला स्वास्थ्य केंद्र",
        number = "108",
        formattedDisplayNumber = "108 / 102",
        description = "Emergency triage, heatstroke recovery, mobile ICU boats, and nearest sector clinics.",
        category = HelplineCategory.MEDICAL,
        iconEmoji = "🚑",
        isTollFree = true,
        is24x7 = true
    ),
    EmergencyContact(
        id = "women-1090",
        title = "Women Safety Powerline",
        hindiTitle = "महिला सुरक्षा हेल्पलाइन (1090)",
        number = "1090",
        formattedDisplayNumber = "1090",
        description = "Dedicated female officer support, family harassment prevention, and lost child support.",
        category = HelplineCategory.WOMEN_SAFETY,
        iconEmoji = "🛡️",
        isTollFree = true,
        is24x7 = true
    ),
    EmergencyContact(
        id = "tourist-police",
        title = "Anti-Touting & Tourist Cell",
        hindiTitle = "पर्यटक सहायता एवं दलाल निरोधक प्रकोष्ठ",
        number = "18001805145",
        formattedDisplayNumber = "1800-180-5145",
        description = "Strict action against touts, overcharging boatmen, fake passes, and aggressive pandas.",
        category = HelplineCategory.TOURIST_ASSISTANCE,
        iconEmoji = "⚖️",
        isTollFree = true,
        is24x7 = true
    )
)

val COMMON_SCAM_ALERTS = listOf(
    ScamAlert(
        id = "scam-unauthorized-priests",
        categoryTitle = "Priests & Rituals",
        title = "Unauthorized Priests & Coercive Sankalp",
        hindiTitle = "अनधिकृत पंडे एवं जबरन संकल्प घोटाला",
        iconEmoji = "🕉️",
        riskLevel = ScamRiskLevel.CRITICAL,
        warningSummary = "Impostor priests intercepting devotees at ghat stairs, demanding ₹2,100 to ₹11,000 for 'mandatory' ancestral Sankalp.",
        scamTechniqueDetails = "Touts posing as senior Purohits approach devotees as soon as they step down to the ghats. They place holy water (Achaman) in your palm before asking and claim that dropping it breaks dharma unless you pledge huge gold/silver or cash donations (Gupt Daan).",
        redFlags = listOf(
            "Priest refuses to show official Prayagraj Administration QR badge (PRY- accreditation).",
            "Insists that Sankalp requires a minimum mandatory amount (e.g. ₹2,100 or ₹5,100).",
            "Attempts to pour water into your hand aggressively before explaining ritual cost.",
            "Claims ancestral lineage (Bahi-Khata) without producing actual historical records."
        ),
        officialRule = "As per District Administration guidelines, Dakshina is 100% voluntary. Standard suggested offerings range from ₹101 to ₹501. No priest has the legal right to force any payment.",
        preventionTips = listOf(
            "Verify the Purohit's PRY- ID card or book via the app's Verified Purohit Directory.",
            "Politely decline Achaman until terms and voluntary nature of Dakshina are mutually agreed.",
            "If pressured, dial 1920 or report immediately to the nearest Ghat Police Chowki."
        ),
        reportingAuthority = "Prayagraj Purohit Welfare Board & Mela Police (1920)"
    ),
    ScamAlert(
        id = "scam-boat-overcharging",
        categoryTitle = "Boats & Sangam Snan",
        title = "Mid-River Extortion & Boat Overcharging",
        hindiTitle = "नौका अधिक किराया वसूली एवं बीच धारा में दबाव",
        iconEmoji = "🛶",
        riskLevel = ScamRiskLevel.CRITICAL,
        warningSummary = "Middlemen charging ₹3,000–₹5,000 for regular boats or demanding extra tips mid-river to return to shore.",
        scamTechniqueDetails = "Unauthorized agents lure pilgrims at entry barriers, claiming all official boats are full. They charge 5x to 10x rates, or sail devotees to the confluence and threaten to leave them stranded unless high tips (Inam) are paid for the return journey.",
        redFlags = listOf(
            "Quoting more than ₹100 for shared boat or more than ₹1,200 for private rowboat.",
            "Refusing to provide an official counter receipt or administration token.",
            "Boatmen refusing to provide certified life jackets before casting off.",
            "Demanding extra 'waiting charges' mid-river for 15-minute Snan halting."
        ),
        officialRule = "Official fixed tariffs: Shared Rowboat: ₹50–₹100/person; Private Rowboat (up to 6 persons): ₹600–₹1,200 total; Motorboat: ₹1,500–₹2,500 total round-trip including 20–30 min halting at Sangam.",
        preventionTips = listOf(
            "Purchase tokens exclusively at Official Counters (Kila Ghat Counter #1 & #2, Saraswati Ghat Counter #3).",
            "Never board any boat without wearing an approved life jacket.",
            "Take a photo of the boat's painted identification number before boarding."
        ),
        reportingAuthority = "Water Police & SDRF Patrol (1077 / 1920)"
    ),
    ScamAlert(
        id = "scam-fake-vip-passes",
        categoryTitle = "Temples & Darshan",
        title = "Fake VIP Darshan Passes & Fast-Track Touts",
        hindiTitle = "फर्जी वीआईपी दर्शन पास एवं अक्षयवट दलाली",
        iconEmoji = "🎟️",
        riskLevel = ScamRiskLevel.HIGH,
        warningSummary = "Touts selling fake 'VIP Direct Entry' tokens for Bade Hanuman Ji temple, Akshayavat, and Patalpuri.",
        scamTechniqueDetails = "Fraudsters loiter around Hanuman Mandir and Fort gates selling printed cards claiming to bypass the 2-hour queue for ₹500/person. Security turns pilgrims away at the sanctum sanctorum.",
        redFlags = listOf(
            "Individuals selling printed tokens or wristbands on the roadside.",
            "Claims that Akshayavat inside Akbar Fort requires paid entry.",
            "Guarantees of touching the sacred deity during peak Shringar hours."
        ),
        officialRule = "All temple darshans and entry to sacred Akshayavat / Patalpuri inside Allahabad Fort are completely FREE. Official passes (when applicable) are issued exclusively by Military/Administration gates at ₹0.",
        preventionTips = listOf(
            "Never purchase any darshan token from unauthorized individuals outside the temple.",
            "Join the standard queue or use official temple trust counters for special assistance.",
            "Report touts selling passes to the Military Police or Mela Chowki."
        ),
        reportingAuthority = "Akshayavat Security Cell & Mela Administration (1920 / 112)"
    ),
    ScamAlert(
        id = "scam-fake-prashad-gems",
        categoryTitle = "Prashad & Souvenirs",
        title = "Synthetic Rudraksha & Overpriced Prashad Baskets",
        hindiTitle = "नकली रुद्राक्ष एवं अत्यधिक मूल्य का प्रसाद",
        iconEmoji = "📿",
        riskLevel = ScamRiskLevel.MODERATE,
        warningSummary = "Vendors handing 'free' flower/diya baskets, then demanding ₹500+, or selling fake plastic-carved Rudraksha.",
        scamTechniqueDetails = "Vendors forcefully thrust flower platters or Ganga Jal cans into pilgrims' hands saying 'Take this for Mother Ganga', and once the ritual starts, demand ₹300 to ₹800, threatening ill omen if returned.",
        redFlags = listOf(
            "Vendor thrusts puja items into your hand without mentioning the price beforehand.",
            "Selling 'Rare 14-Mukhi Rudraksha' or 'Certified Sangam Gemstones' for cheap cash prices.",
            "Packaging Ganga Jal in unsealed or dirty plastic containers."
        ),
        officialRule = "Standard puja samagri basket costs ₹30–₹50. Temple trust approved prashad has printed MRP.",
        preventionTips = listOf(
            "Always ask and fix the price of puja baskets before touching or accepting items.",
            "Buy certified spiritual items only from recognized institutional stalls like Gita Press or Temple Trusts.",
            "Firmly return any unasked items handed to you."
        ),
        reportingAuthority = "Market Inspector & Mela Grievance (1920)"
    ),
    ScamAlert(
        id = "scam-erickshaw-detours",
        categoryTitle = "Local Transport",
        title = "E-Rickshaw Detours & Commission Stalls",
        hindiTitle = "ई-रिक्शा मनमाना किराया एवं कमीशन की दुकानों पर भटकाव",
        iconEmoji = "🛺",
        riskLevel = ScamRiskLevel.MODERATE,
        warningSummary = "Drivers falsely claiming ghats are closed, taking long detours, or dropping passengers at expensive souvenir shops.",
        scamTechniqueDetails = "Drivers convince newcomers that Sangam entry is sealed for VIP movement, rerouting them to private souvenir shops, expensive ashrams, or distant unauthorized parking lots for commissions.",
        redFlags = listOf(
            "Driver insists that Saraswati Ghat or Kila Ghat is 'completely shut today'.",
            "Charging ₹200+ for standard 2-3 km city hops.",
            "Refusing to drop you at designated administration battery bus stands."
        ),
        officialRule = "Standard city e-rickshaw shared hop is ₹10–₹20/head (or ₹80–₹150 for full vehicle hire). Free administration shuttle buses operate between satellite parking lots and ghats.",
        preventionTips = listOf(
            "Use the Live Ghat Navigation screen in this app to verify actual route status and distances.",
            "Board official free Mela electric shuttle buses from satellite parking lots.",
            "Agree on total fare clearly before boarding."
        ),
        reportingAuthority = "Traffic Police Control (112 / 1920)"
    )
)

val PILGRIM_SAFETY_CHECKLIST = listOf(
    SafetyChecklistItem(
        id = "check-purohit-id",
        title = "Verify Purohit Accreditation",
        hindiTitle = "पंडा जी का पहचान पत्र जांचें",
        description = "Ensure your priest wears an official PRY- district badge or is listed on our verified directory.",
        isCritical = true
    ),
    SafetyChecklistItem(
        id = "check-boat-token",
        title = "Get Official Counter Boat Token",
        hindiTitle = "आधिकारिक काउंटर से नौका टोकन लें",
        description = "Always buy tickets from Counter #1 to #5 with standard printed receipts (Shared ₹50–₹100, Private ₹600–₹1200).",
        isCritical = true
    ),
    SafetyChecklistItem(
        id = "check-life-jacket",
        title = "Wear Certified Life Jacket",
        hindiTitle = "लाइफ जैकेट अनिवार्य रूप से पहनें",
        description = "Strictly mandatory before boat casting off. Do not board if life jackets are missing or damaged.",
        isCritical = true
    ),
    SafetyChecklistItem(
        id = "check-voluntary-dakshina",
        title = "Clarify Voluntary Dakshina in Advance",
        hindiTitle = "दक्षिणा की स्वैच्छिक सीमा पहले तय करें",
        description = "Agree beforehand that Dakshina is voluntary (₹101–₹501). Never hand over ATM cards or unverified cash.",
        isCritical = true
    ),
    SafetyChecklistItem(
        id = "check-emergency-number",
        title = "Save Kumbh Helpline (1920)",
        hindiTitle = "कुंभ हेल्पलाइन 1920 अपने फोन में सेव करें",
        description = "Keep 1920 and 112 on speed dial for immediate on-site assistance at all ghat sectors.",
        isCritical = false
    ),
    SafetyChecklistItem(
        id = "check-valuables-locker",
        title = "Secure Valuables During Snan",
        hindiTitle = "स्नान के दौरान कीमती सामान सुरक्षित रखें",
        description = "Use official administration cloakrooms (₹10/bag) or take turns bathing while a family member watches luggage.",
        isCritical = false
    )
)

val OFFICIAL_APPROVED_TARIFF_REFERENCE = listOf(
    OfficialTariffReference("Shared Rowboat (Round-trip to Sangam)", "₹50 – ₹100 per person", "Includes 20 min Sangam Snan halt + life jacket"),
    OfficialTariffReference("Private Rowboat (Up to 6 persons)", "₹600 – ₹1,200 per boat", "Includes dedicated boat + 30 min Sangam halt"),
    OfficialTariffReference("Motorboat Transit (Up to 10 persons)", "₹1,500 – ₹2,500 per boat", "High speed transit with licensed pilot"),
    OfficialTariffReference("Sankalp & Holy Dip Puja (Dakshina)", "₹101 – ₹501 (Voluntary)", "100% voluntary; no fixed compulsion"),
    OfficialTariffReference("Pind Daan / Shraddha Karma", "₹501 – ₹1,100 (Full Puja)", "Includes ritual samagri & certified Purohit"),
    OfficialTariffReference("E-Rickshaw Shared Hop", "₹10 – ₹20 per passenger", "Fixed city route hops"),
    OfficialTariffReference("Administration Cloakroom", "₹10 per bag / 4 hours", "Government secured luggage lockers")
)

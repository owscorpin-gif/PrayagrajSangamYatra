package com.example.data.model

enum class BoatType {
    SHARED_ROWBOAT,
    PRIVATE_ROWBOAT,
    MOTORBOAT
}

enum class CrowdDensityLevel {
    LOW,
    MODERATE,
    HIGH,
    SEVERE
}

data class BoatFareMatrix(
    val boatType: BoatType,
    val title: String,
    val hindiTitle: String,
    val maxCapacity: Int,
    val minRateInr: Int,
    val maxRateInr: Int,
    val isPerHead: Boolean,
    val inclusions: List<String>,
    val sangamStopDuration: String,
    val lifeJacketsIncluded: Boolean = true
)

data class VerifiedBoatmanProfile(
    val id: String,
    val name: String,
    val hindiName: String,
    val registrationNumber: String,
    val boatIdentificationNumber: String,
    val assignedGhatStation: String,
    val boatType: BoatType,
    val rating: Double,
    val totalTrips: Int,
    val contactPhone: String,
    val isSafetyCertified: Boolean = true,
    val verificationAuthority: String = "District Administration & Prayag Navik Sangh"
)

data class GhatNavigationInfo(
    val ghatId: String,
    val name: String,
    val hindiName: String,
    val distanceKm: Double,
    val walkingTimeMinutes: Int,
    val crowdDensity: CrowdDensityLevel,
    val crowdDescription: String,
    val parkingLocation: String,
    val batteryAutoPickupPoint: String,
    val officialBoatCounterAvailable: Boolean,
    val bestTimeToVisit: String
)

val SAMPLE_BOAT_FARES = listOf(
    BoatFareMatrix(
        boatType = BoatType.SHARED_ROWBOAT,
        title = "Shared Rowboat to Sangam",
        hindiTitle = "साझा नौका (प्रति व्यक्ति)",
        maxCapacity = 12,
        minRateInr = 50,
        maxRateInr = 100,
        isPerHead = true,
        inclusions = listOf(
            "Life jacket mandatory & provided",
            "Round-trip to Sangam Confluence",
            "Assistance with holy dip platform"
        ),
        sangamStopDuration = "20 – 30 minutes"
    ),
    BoatFareMatrix(
        boatType = BoatType.PRIVATE_ROWBOAT,
        title = "Private Rowboat (Family)",
        hindiTitle = "निजी पारिवारिक नौका",
        maxCapacity = 6,
        minRateInr = 600,
        maxRateInr = 1200,
        isPerHead = false,
        inclusions = listOf(
            "Exclusive boat for family / group",
            "Inspection-certified life vests",
            "Flexible stay at Sangam point",
            "Dedicated return drop-off"
        ),
        sangamStopDuration = "30 – 45 minutes"
    ),
    BoatFareMatrix(
        boatType = BoatType.MOTORBOAT,
        title = "Motorboat (Express)",
        hindiTitle = "मोटरबोट (तीव्र यात्रा)",
        maxCapacity = 10,
        minRateInr = 150,
        maxRateInr = 2500,
        isPerHead = false,
        inclusions = listOf(
            "Fastest transit across Yamuna/Ganga",
            "Licensed motor operator",
            "Full safety gear & rescue kit"
        ),
        sangamStopDuration = "25 – 35 minutes"
    )
)

val SAMPLE_VERIFIED_BOATMEN = listOf(
    VerifiedBoatmanProfile(
        id = "PRY-BOAT-101",
        name = "Kallu Nishad",
        hindiName = "कल्लू निषाद",
        registrationNumber = "PRY-ADM-NAVIK-8821",
        boatIdentificationNumber = "UP-70-BOAT-4019",
        assignedGhatStation = "Kila Ghat Stand #3",
        boatType = BoatType.SHARED_ROWBOAT,
        rating = 4.9,
        totalTrips = 1420,
        contactPhone = "+919839110001"
    ),
    VerifiedBoatmanProfile(
        id = "PRY-BOAT-102",
        name = "Munna Sahni",
        hindiName = "मुन्ना साहनी",
        registrationNumber = "PRY-ADM-NAVIK-5542",
        boatIdentificationNumber = "UP-70-BOAT-1128",
        assignedGhatStation = "Saraswati Ghat Stand #1",
        boatType = BoatType.PRIVATE_ROWBOAT,
        rating = 4.95,
        totalTrips = 2100,
        contactPhone = "+919839110002"
    ),
    VerifiedBoatmanProfile(
        id = "PRY-BOAT-103",
        name = "Rajesh Mallah",
        hindiName = "राजेश मल्लाह",
        registrationNumber = "PRY-ADM-NAVIK-9903",
        boatIdentificationNumber = "UP-70-MB-0082",
        assignedGhatStation = "Arail Ghat VIP Stand",
        boatType = BoatType.MOTORBOAT,
        rating = 4.88,
        totalTrips = 980,
        contactPhone = "+919839110003"
    )
)

val SAMPLE_GHAT_NAVIGATION = listOf(
    GhatNavigationInfo(
        ghatId = "GHAT-01",
        name = "Kila Ghat (Fort Side)",
        hindiName = "किला घाट",
        distanceKm = 0.8,
        walkingTimeMinutes = 10,
        crowdDensity = CrowdDensityLevel.HIGH,
        crowdDescription = "Peak morning rush; high boat availability",
        parkingLocation = "Akbar Fort East Gate Parking (400m)",
        batteryAutoPickupPoint = "Kila Crossing Stand",
        officialBoatCounterAvailable = true,
        bestTimeToVisit = "11:00 AM – 3:00 PM (Moderate crowd)"
    ),
    GhatNavigationInfo(
        ghatId = "GHAT-02",
        name = "Saraswati Ghat",
        hindiName = "सरस्वती घाट",
        distanceKm = 1.4,
        walkingTimeMinutes = 18,
        crowdDensity = CrowdDensityLevel.MODERATE,
        crowdDescription = "Paved promenade with evening Aarti seating",
        parkingLocation = "Yamuna Bank Multi-level Parking (200m)",
        batteryAutoPickupPoint = "Saraswati Ghat Rotary Gate",
        officialBoatCounterAvailable = true,
        bestTimeToVisit = "5:00 PM – 7:30 PM (Evening Aarti)"
    ),
    GhatNavigationInfo(
        ghatId = "GHAT-03",
        name = "Arail Ghat (South Bank)",
        hindiName = "अरैल घाट",
        distanceKm = 3.2,
        walkingTimeMinutes = 35,
        crowdDensity = CrowdDensityLevel.LOW,
        crowdDescription = "Clean, peaceful, 60% lower crowd density",
        parkingLocation = "Someshwar Mahadev Ground Parking (100m)",
        batteryAutoPickupPoint = "Arail Bund Road Stand",
        officialBoatCounterAvailable = true,
        bestTimeToVisit = "Sunrise (5:30 AM – 8:00 AM)"
    )
)

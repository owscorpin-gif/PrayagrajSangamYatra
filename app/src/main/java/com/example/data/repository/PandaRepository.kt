package com.example.data.repository

import com.example.data.model.PandaServiceOffering
import com.example.data.model.VerifiedPanda
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

interface PandaRepository {
    fun getAllPandas(): Flow<List<VerifiedPanda>>
    fun getPandaById(id: String): VerifiedPanda?
    fun filterPandas(
        query: String = "",
        ritualCategory: String? = null,
        ghatCategory: String? = null,
        language: String? = null,
        bahiKhataOnly: Boolean = false,
        minRating: Double = 0.0,
        sortBy: String = "rating" // "rating", "experience", "reviews", "name"
    ): Flow<List<VerifiedPanda>>
    fun getAvailableRitualCategories(): List<String>
    fun getAvailableGhats(): List<Pair<String, String>> // key to display name
    fun getAvailableLanguages(): List<String>
}

class PandaRepositoryImpl : PandaRepository {

    private val samplePandas: List<VerifiedPanda> = listOf(
        VerifiedPanda(
            id = "panda-01",
            name = "Pt. Ramakant Mishra Shastri",
            hindiName = "पं. रमाकांत मिश्र शास्त्री",
            title = "Chief Tirtha Purohit • Sangam Kshetra",
            accreditationId = "PRY-KMB-2025-0142",
            ghatLocation = "Triveni Sangam Ghat No. 3",
            ghatCategory = "sangam",
            yearsOfExperience = 32,
            clanLineage = "7th Generation Hereditary Tirtha Purohit of Sangam",
            bahiKhataAvailable = true,
            bahiKhataRegions = listOf("Varanasi", "Mithila", "Patna", "Indore", "Jaipur", "Lucknow"),
            rating = 4.95,
            totalReviews = 428,
            isVerified = true,
            verifiedAuthority = "Prayag Tirtha Purohit Maha Sabha & UP Tourism",
            phoneNumber = "+91 94150 28471",
            whatsappNumber = "+91 94150 28471",
            email = "ramakant.purohit@prayagraj.org",
            avatarUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136",
            languages = listOf("Hindi", "Sanskrit", "Bengali", "English"),
            bio = "Devoted to the sacred confluence for over three decades. Specializing in authentic Vedic Sangam Snan Sankalp, Pitru Tarpan, and Pind Daan with centuries-old family Bahi-Khata ancestry ledgers.",
            availability = "Available at Sangam Ghat (05:00 AM - 07:30 PM)",
            services = listOf(
                PandaServiceOffering(
                    id = "srv-101",
                    title = "Triveni Sangam Snan & Maha Sankalp Vidhi",
                    hindiTitle = "त्रिवेणी संगम स्नान एवं महा संकल्प विधि",
                    category = "Snan & Sankalp",
                    description = "Comprehensive Vedic Sankalp at the exact holy confluence of Ganga, Yamuna & invisible Saraswati with milk offering, deep daan, and Vedic blessings.",
                    dakshinaGuide = "₹501 - ₹1,100",
                    durationMinutes = 45,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-102",
                    title = "Vedic Pind Daan & Pitru Tarpan",
                    hindiTitle = "वैदिक पिंडदान एवं पितृ तर्पण",
                    category = "Pind Daan & Tarpan",
                    description = "Sacred ancestral offering ceremony following Garuda Purana rituals for peace and liberation of forefathers.",
                    dakshinaGuide = "₹1,501 - ₹3,100",
                    durationMinutes = 90,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-103",
                    title = "Navagraha Shanti & Rudra Maha Havan",
                    hindiTitle = "नवग्रह शांति एवं रुद्र महा हवन",
                    category = "Vedic Havan",
                    description = "Potent fire ritual invoking planetary harmonies and Lord Shiva with pure samagri and herbal dravya.",
                    dakshinaGuide = "₹2,100 - ₹5,100",
                    durationMinutes = 120,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-104",
                    title = "Asthi Visarjan & Antim Sanskar Shanti",
                    hindiTitle = "अस्थि विसर्जन एवं अंतिम संस्कार शांति",
                    category = "Asthi Visarjan",
                    description = "Solemn immersion of sacred mortal remains into Triveni Sangam with full Vedic mantra chantings and purification.",
                    dakshinaGuide = "₹1,100 - ₹2,500",
                    durationMinutes = 60,
                    samagriIncluded = true,
                    boatIncluded = true
                )
            )
        ),
        VerifiedPanda(
            id = "panda-02",
            name = "Acharya Devendra Shastri",
            hindiName = "आचार्य देवेन्द्र शास्त्री",
            title = "Ved Murti & Daraganj Purohit",
            accreditationId = "PRY-KMB-2025-0288",
            ghatLocation = "Daraganj Ganga Ghat",
            ghatCategory = "daraganj",
            yearsOfExperience = 24,
            clanLineage = "Daraganj Vedic Brahman Lineage • Shukla Yajurveda Tradition",
            bahiKhataAvailable = true,
            bahiKhataRegions = listOf("Gorakhpur", "Prayagraj", "Basti", "Ayodhya", "Bhopal", "Gwalior"),
            rating = 4.88,
            totalReviews = 312,
            isVerified = true,
            verifiedAuthority = "Prayag Tirtha Purohit Maha Sabha",
            phoneNumber = "+91 98390 14522",
            whatsappNumber = "+91 98390 14522",
            email = "devendra.shastri@prayagraj.org",
            avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d",
            languages = listOf("Hindi", "Sanskrit", "Gujarati", "Marathi"),
            bio = "Master of Shukla Yajurveda Shakha rituals with expertise in Asthi Visarjan, Pitru Dosh Shanti, and Kashi-Prayag Yatra Sankalp. Maintains records of Gujarati and Maharashtrian pilgrims.",
            availability = "Available (06:00 AM - 08:00 PM)",
            services = listOf(
                PandaServiceOffering(
                    id = "srv-201",
                    title = "Pitru Dosh Nivaran & Pind Daan",
                    hindiTitle = "पितृ दोष निवारण एवं पिंडदान",
                    category = "Pind Daan & Tarpan",
                    description = "Specialized remedy for ancestral peace, eliminating obstacles in family growth and health.",
                    dakshinaGuide = "₹2,100 - ₹4,500",
                    durationMinutes = 90,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-202",
                    title = "Maha Rudrabhishek with Sangam Jal",
                    hindiTitle = "संगम जल से महा रुद्राभिषेक",
                    category = "Rudrabhishek",
                    description = "Abhishekam with holy water of Triveni, Panchamrit, bilva leaves, and continuous recitation of Sri Rudram.",
                    dakshinaGuide = "₹1,501 - ₹3,500",
                    durationMinutes = 75,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-203",
                    title = "Snan Sankalp & Ganga Pujan",
                    hindiTitle = "स्नान संकल्प एवं गंगा पूजन",
                    category = "Snan & Sankalp",
                    description = "Auspicious Ganga Pujan with traditional 16 Upacharas followed by Tirtha Snan sankalp.",
                    dakshinaGuide = "₹501 - ₹1,000",
                    durationMinutes = 40,
                    samagriIncluded = true,
                    boatIncluded = false
                )
            )
        ),
        VerifiedPanda(
            id = "panda-03",
            name = "Pt. Raghavendra Tiwari",
            hindiName = "पं. राघवेन्द्र तिवारी",
            title = "Sangam Maha Aarti Chief Acharya",
            accreditationId = "PRY-KMB-2025-0391",
            ghatLocation = "Saraswati Ghat & Triveni Sangam",
            ghatCategory = "saraswati",
            yearsOfExperience = 18,
            clanLineage = "Yamuna-Ganga Aarti Seva Trust Trustee",
            bahiKhataAvailable = false,
            bahiKhataRegions = emptyList(),
            rating = 4.92,
            totalReviews = 275,
            isVerified = true,
            verifiedAuthority = "Prayag Tirtha Purohit Maha Sabha & Municipal Corp",
            phoneNumber = "+91 94501 89334",
            whatsappNumber = "+91 94501 89334",
            email = "raghavendra.tiwari@prayagraj.org",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
            languages = listOf("Hindi", "English", "Telugu", "Tamil"),
            bio = "Lead coordinator of the mesmerizing evening Saraswati Ghat Maha Aarti. Fluent in South Indian languages (Telugu, Tamil) assisting pilgrims from Andhra, Telangana, and Tamil Nadu.",
            availability = "Available for Aarti & Rituals (05:30 AM - 08:30 PM)",
            services = listOf(
                PandaServiceOffering(
                    id = "srv-301",
                    title = "Ganga-Yamuna Maha Aarti VIP Seva & Sankalp",
                    hindiTitle = "गंगा-यमुना महा आरती संकल्प",
                    category = "Ganga Aarti",
                    description = "Special personal deepam offering and family sankalp during the grand sunset riverside Aarti.",
                    dakshinaGuide = "₹1,100 - ₹2,100",
                    durationMinutes = 60,
                    samagriIncluded = true,
                    boatIncluded = true
                ),
                PandaServiceOffering(
                    id = "srv-302",
                    title = "Kumbh Snan & Vedic Gotra Sankalp",
                    hindiTitle = "कुंभ स्नान एवं वैदिक गोत्र संकल्प",
                    category = "Snan & Sankalp",
                    description = "Gotra uccharan, ancestral line invocation, and ritual bath in holy waters of Sangam.",
                    dakshinaGuide = "₹701 - ₹1,500",
                    durationMinutes = 45,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-303",
                    title = "Mundan Sanskar Ceremony (Head Tonsure)",
                    hindiTitle = "मुंडन संस्कार विधि",
                    category = "Mundan Sanskar",
                    description = "Traditional child first hair-offering sanskar with sacred rites at holy riverbank.",
                    dakshinaGuide = "₹1,501 - ₹2,500",
                    durationMinutes = 60,
                    samagriIncluded = true,
                    boatIncluded = false
                )
            )
        ),
        VerifiedPanda(
            id = "panda-04",
            name = "Acharya Vidyadhar Bhattacharya",
            hindiName = "आचार्य विद्याधर भट्टाचार्य",
            title = "Bangiya Tirtha Purohit • Sangam East",
            accreditationId = "PRY-KMB-2025-0455",
            ghatLocation = "Triveni Sangam Boat Ghat",
            ghatCategory = "sangam",
            yearsOfExperience = 29,
            clanLineage = "Bhattacharya Clan of Prayag • 5th Generation Bengali Registry",
            bahiKhataAvailable = true,
            bahiKhataRegions = listOf("Kolkata", "Howrah", "Bardhaman", "Midnapore", "Dhaka Lineage", "Tripura"),
            rating = 4.96,
            totalReviews = 510,
            isVerified = true,
            verifiedAuthority = "Prayag Tirtha Purohit Maha Sabha & Bangiya Samaj",
            phoneNumber = "+91 93351 04899",
            whatsappNumber = "+91 93351 04899",
            email = "vidyadhar.bhatt@prayagraj.org",
            avatarUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce",
            languages = listOf("Bengali", "Hindi", "Sanskrit", "English", "Odia"),
            bio = "Official hereditary registrar (Purohit) for Bengali and Eastern India pilgrims visiting Prayagraj for Tarpan, Pind Daan, and holy dip at Sangam. Maintains pristine ancestry ledgers spanning 180 years.",
            availability = "Available (05:00 AM - 07:00 PM)",
            services = listOf(
                PandaServiceOffering(
                    id = "srv-401",
                    title = "Tarpana & Sraddha as per Raghunandana Smriti",
                    hindiTitle = "तर्पण एवं श्राद्ध (स्मृति पद्धति)",
                    category = "Pind Daan & Tarpan",
                    description = "Authentic ancestral rites strictly following traditional Smriti texts for Bengali and Odia devotees.",
                    dakshinaGuide = "₹1,501 - ₹3,500",
                    durationMinutes = 90,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-402",
                    title = "Boat Sangam Snan & Saraswati Puja",
                    hindiTitle = "नौका संगम स्नान एवं सरस्वती पूजा",
                    category = "Snan & Sankalp",
                    description = "Boat journey to the center of Sangam, guided bathing, and holy offerings to Saraswati Devi.",
                    dakshinaGuide = "₹1,100 - ₹2,100",
                    durationMinutes = 60,
                    samagriIncluded = true,
                    boatIncluded = true
                ),
                PandaServiceOffering(
                    id = "srv-403",
                    title = "Bahi-Khata Ancestry Family Search & Recording",
                    hindiTitle = "बही-खाता वंश वृक्ष खोज एवं अंकन",
                    category = "Snan & Sankalp",
                    description = "Lookup of ancestral pilgrim signatures in centuries-old manuscripts and adding current generation pilgrimage record.",
                    dakshinaGuide = "₹501 - ₹1,000",
                    durationMinutes = 30,
                    samagriIncluded = false,
                    boatIncluded = false
                )
            )
        ),
        VerifiedPanda(
            id = "panda-05",
            name = "Pt. Chandrasekhar Joshi",
            hindiName = "पं. चंद्रशेखर जोशी",
            title = "Arail Sangam & Someshwar Purohit",
            accreditationId = "PRY-KMB-2025-0512",
            ghatLocation = "Arail Sangam Ghat",
            ghatCategory = "arail",
            yearsOfExperience = 21,
            clanLineage = "Arail Kshetra Someshwar Mahadev Peeth Purohit",
            bahiKhataAvailable = true,
            bahiKhataRegions = listOf("Ujjain", "Indore", "Nagpur", "Pune", "Nashik", "Jaipur"),
            rating = 4.87,
            totalReviews = 219,
            isVerified = true,
            verifiedAuthority = "Prayag Tirtha Purohit Maha Sabha",
            phoneNumber = "+91 94156 77123",
            whatsappNumber = "+91 94156 77123",
            email = "chandrasekhar.joshi@prayagraj.org",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e",
            languages = listOf("Hindi", "Marathi", "Gujarati", "Sanskrit"),
            bio = "Based on the serene southern bank at Arail Ghat overlooking the Triveni Sangam. Specialist in Someshwar Temple Rudrabhishek and peaceful riverside Pind Daan away from the rush.",
            availability = "Available at Arail Ghat (06:00 AM - 08:00 PM)",
            services = listOf(
                PandaServiceOffering(
                    id = "srv-501",
                    title = "Someshwar Mahadev Jyotirlinga Abhishek",
                    hindiTitle = "सोमेश्वर महादेव ज्योतिर्लिंग अभिषेक",
                    category = "Rudrabhishek",
                    description = "Special worship at ancient underground Someshwar Mahadev mandir on southern bank of Yamuna.",
                    dakshinaGuide = "₹1,100 - ₹2,500",
                    durationMinutes = 60,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-502",
                    title = "Arail Sunset Aarti & Deep Daan",
                    hindiTitle = "अड़ैल संध्या आरती एवं दीपदान",
                    category = "Ganga Aarti",
                    description = "Peaceful twilight prayer with floating clay lamps into Yamuna-Ganga waters.",
                    dakshinaGuide = "₹501 - ₹1,100",
                    durationMinutes = 45,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-503",
                    title = "Vedic Shanti Havan & Graha Yagya",
                    hindiTitle = "वैदिक शांति हवन एवं ग्रह यज्ञ",
                    category = "Vedic Havan",
                    description = "Sacred fire oblation at Arail hermitage grounds for health, prosperity, and spiritual peace.",
                    dakshinaGuide = "₹2,501 - ₹5,500",
                    durationMinutes = 110,
                    samagriIncluded = true,
                    boatIncluded = false
                )
            )
        ),
        VerifiedPanda(
            id = "panda-06",
            name = "Pt. Harinarayan Tripathi",
            hindiName = "पं. हरिनारायण त्रिपाठी",
            title = "Rasoolabad & Kakarha Ghat Senior Purohit",
            accreditationId = "PRY-KMB-2025-0628",
            ghatLocation = "Rasoolabad Ganga Ghat",
            ghatCategory = "rasoolabad",
            yearsOfExperience = 35,
            clanLineage = "Tripathi Vedic Purohit Sabha",
            bahiKhataAvailable = true,
            bahiKhataRegions = listOf("Kanpur", "Prayagraj", "Fatehpur", "Mirzapur", "Pratapgarh"),
            rating = 4.91,
            totalReviews = 360,
            isVerified = true,
            verifiedAuthority = "Prayag Tirtha Purohit Maha Sabha",
            phoneNumber = "+91 94505 11980",
            whatsappNumber = "+91 94505 11980",
            email = "harinarayan.tripathi@prayagraj.org",
            avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e",
            languages = listOf("Hindi", "Sanskrit", "Maithili"),
            bio = "Senior-most Vedic acharya at Rasoolabad Ghat. Known for meticulous traditional execution of Asthi Visarjan, Narayan Bali, and Shodasha Sraddha rituals.",
            availability = "Available (05:00 AM - 06:30 PM)",
            services = listOf(
                PandaServiceOffering(
                    id = "srv-601",
                    title = "Asthi Visarjan & Moksha Sankalp Vidhi",
                    hindiTitle = "अस्थि विसर्जन एवं मोक्ष संकल्प विधि",
                    category = "Asthi Visarjan",
                    description = "Comprehensive final immersion rites according to Vedic scriptures ensuring eternal peace for departed souls.",
                    dakshinaGuide = "₹1,100 - ₹2,500",
                    durationMinutes = 55,
                    samagriIncluded = true,
                    boatIncluded = true
                ),
                PandaServiceOffering(
                    id = "srv-602",
                    title = "Pind Daan & Tripindi Sraddha",
                    hindiTitle = "पिंडदान एवं त्रिपिंडी श्राद्ध",
                    category = "Pind Daan & Tarpan",
                    description = "Three-lineage ancestral propitiation freeing ancestors from preta yoni and bestowing blessings.",
                    dakshinaGuide = "₹2,100 - ₹4,500",
                    durationMinutes = 100,
                    samagriIncluded = true,
                    boatIncluded = false
                ),
                PandaServiceOffering(
                    id = "srv-603",
                    title = "Ganga Snan Sankalp with Vedic Hymns",
                    hindiTitle = "गंगा स्नान संकल्प एवं वेदमंत्र पाठ",
                    category = "Snan & Sankalp",
                    description = "Purification bath ritual at peaceful north-flowing Ganga riverbank with Sukta recitations.",
                    dakshinaGuide = "₹501 - ₹1,000",
                    durationMinutes = 35,
                    samagriIncluded = true,
                    boatIncluded = false
                )
            )
        )
    )

    private val pandasFlow = MutableStateFlow(samplePandas)

    override fun getAllPandas(): Flow<List<VerifiedPanda>> {
        return pandasFlow.asStateFlow()
    }

    override fun getPandaById(id: String): VerifiedPanda? {
        return samplePandas.find { it.id == id }
    }

    override fun filterPandas(
        query: String,
        ritualCategory: String?,
        ghatCategory: String?,
        language: String?,
        bahiKhataOnly: Boolean,
        minRating: Double,
        sortBy: String
    ): Flow<List<VerifiedPanda>> {
        val trimmed = query.trim().lowercase(Locale.ROOT)

        val filtered = samplePandas.filter { panda ->
            // Search Query matching name, hindi name, location, accreditation, bio, languages, services
            val matchesQuery = if (trimmed.isEmpty()) true else {
                panda.name.lowercase(Locale.ROOT).contains(trimmed) ||
                        panda.hindiName.lowercase(Locale.ROOT).contains(trimmed) ||
                        panda.ghatLocation.lowercase(Locale.ROOT).contains(trimmed) ||
                        panda.accreditationId.lowercase(Locale.ROOT).contains(trimmed) ||
                        panda.bio.lowercase(Locale.ROOT).contains(trimmed) ||
                        panda.languages.any { it.lowercase(Locale.ROOT).contains(trimmed) } ||
                        panda.services.any { srv ->
                            srv.title.lowercase(Locale.ROOT).contains(trimmed) ||
                                    srv.category.lowercase(Locale.ROOT).contains(trimmed) ||
                                    srv.description.lowercase(Locale.ROOT).contains(trimmed)
                        } ||
                        panda.bahiKhataRegions.any { it.lowercase(Locale.ROOT).contains(trimmed) }
            }

            // Ritual Category Filter
            val matchesRitual = if (ritualCategory.isNullOrBlank() || ritualCategory.equals("all", ignoreCase = true)) {
                true
            } else {
                panda.services.any { srv ->
                    srv.category.equals(ritualCategory, ignoreCase = true) ||
                            srv.title.contains(ritualCategory, ignoreCase = true)
                }
            }

            // Ghat Location Filter
            val matchesGhat = if (ghatCategory.isNullOrBlank() || ghatCategory.equals("all", ignoreCase = true)) {
                true
            } else {
                panda.ghatCategory.equals(ghatCategory, ignoreCase = true) ||
                        panda.ghatLocation.contains(ghatCategory, ignoreCase = true)
            }

            // Language Filter
            val matchesLanguage = if (language.isNullOrBlank() || language.equals("all", ignoreCase = true)) {
                true
            } else {
                panda.languages.any { it.equals(language, ignoreCase = true) }
            }

            // Bahi Khata Only Filter
            val matchesBahiKhata = if (bahiKhataOnly) panda.bahiKhataAvailable else true

            // Minimum Rating Filter
            val matchesRating = panda.rating >= minRating

            matchesQuery && matchesRitual && matchesGhat && matchesLanguage && matchesBahiKhata && matchesRating
        }

        val sorted = when (sortBy.lowercase(Locale.ROOT)) {
            "experience" -> filtered.sortedByDescending { it.yearsOfExperience }
            "reviews" -> filtered.sortedByDescending { it.totalReviews }
            "name" -> filtered.sortedBy { it.name }
            else -> filtered.sortedByDescending { it.rating }
        }

        return MutableStateFlow(sorted)
    }

    override fun getAvailableRitualCategories(): List<String> {
        return listOf(
            "All Rituals",
            "Snan & Sankalp",
            "Pind Daan & Tarpan",
            "Rudrabhishek",
            "Vedic Havan",
            "Ganga Aarti",
            "Asthi Visarjan",
            "Mundan Sanskar"
        )
    }

    override fun getAvailableGhats(): List<Pair<String, String>> {
        return listOf(
            "all" to "All Ghats",
            "sangam" to "Triveni Sangam",
            "daraganj" to "Daraganj Ghat",
            "arail" to "Arail Sangam Ghat",
            "saraswati" to "Saraswati Ghat",
            "rasoolabad" to "Rasoolabad Ghat"
        )
    }

    override fun getAvailableLanguages(): List<String> {
        return listOf(
            "All",
            "Hindi",
            "Sanskrit",
            "Bengali",
            "Gujarati",
            "Marathi",
            "Telugu",
            "Tamil",
            "English",
            "Odia"
        )
    }
}

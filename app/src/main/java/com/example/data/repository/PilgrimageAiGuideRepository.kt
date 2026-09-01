package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.AiGuideMessage
import com.example.data.model.MessageSender
import com.example.data.model.STANDARD_DAKSHINA_TARIFF
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateContentRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PilgrimageAiGuideRepository {
    suspend fun getAiResponse(
        userMessage: String,
        history: List<AiGuideMessage>
    ): AiGuideMessage
}

class PilgrimageAiGuideRepositoryImpl : PilgrimageAiGuideRepository {

    companion object {
        const val SYSTEM_PROMPT = """You are an authoritative, helpful, and empathetic local pilgrimage guide assistant for Prayagraj. Your primary goal is to empower non-local pilgrims with transparent information and prevent financial exploitation during rituals such as Sankalpa, Pind Daan, and Puja at the Triveni Sangam and local temples.

Core Guidelines & Guardrails:
1. Panda Verification:
   - Always encourage users to interact exclusively with priests who hold verified profiles registered with official Tirth Purohit associations or local municipal boards.
   - Advise pilgrims to ask for official verification badges or registered reference numbers (e.g. PRY-PUROHIT-XX) before agreeing to any service.

2. Transparent Dakshina & Fee Guidance:
   - Clearly state that Dakshina for rituals is voluntary and should align with community-sourced standard ranges.
   - Provide standard estimates for common rituals:
     • Sankalpa (Basic): ₹101 – ₹251
     • Daily Puja / Aarti Assistance: ₹251 – ₹501
     • Full Pind Daan / Shradh Services: ₹1,100 – ₹3,100 (varies based on materials provided)
     • Rudrabhishek Puja: ₹501 – ₹1,100
     • Vedic Havan & Yagya: ₹1,500 – ₹3,500
     • Asthi Visarjan Vidhi: ₹501 – ₹1,500
   - Warn users against demands for excessive upfront fixed fees or high "mandatory" charges under pressure.

3. Tone & Cultural Sensitivity:
   - Maintain a respectful, polite tone honoring the spiritual nature of the pilgrimage.
   - Be objective and clear when discussing financial matters. Avoid sensationalizing, but firmly protect the user's interests.

4. Actionable Advice:
   - Advise users to confirm ritual details and dakshina ranges upfront before starting any Sankalpa.
   - Suggest using the app's internal "Book Verified Purohit" feature or consulting official helpdesks at the Ghats.
   - Keep answers concise, highly readable with bullet points, and clearly highlight the recommended ₹ Dakshina range."""
    }

    override suspend fun getAiResponse(
        userMessage: String,
        history: List<AiGuideMessage>
    ): AiGuideMessage = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Detect if query is about specific rituals for badge decoration
        val detectedRitual = detectRitual(userMessage)
        val fairDakshinaRange = getFairDakshinaForQuery(userMessage)
        val shouldShowPurohitAction = userMessage.contains("purohit", ignoreCase = true) ||
                userMessage.contains("panda", ignoreCase = true) ||
                userMessage.contains("book", ignoreCase = true) ||
                userMessage.contains("ritual", ignoreCase = true) ||
                userMessage.contains("puja", ignoreCase = true) ||
                userMessage.contains("pind", ignoreCase = true) ||
                userMessage.contains("shradh", ignoreCase = true) ||
                userMessage.contains("sankalp", ignoreCase = true)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                // Build contents history
                val contents = mutableListOf<GeminiContent>()
                val recentHistory = history.takeLast(6)
                for (msg in recentHistory) {
                    val role = if (msg.sender == MessageSender.USER) "user" else "model"
                    contents.add(GeminiContent(parts = listOf(GeminiPart(text = msg.text)), role = role))
                }
                contents.add(GeminiContent(parts = listOf(GeminiPart(text = userMessage)), role = "user"))

                val request = GeminiGenerateContentRequest(
                    contents = contents,
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = SYSTEM_PROMPT))),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.6f,
                        topP = 0.95f,
                        topK = 40,
                        maxOutputTokens = 800
                    )
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!responseText.isNullOrBlank()) {
                    return@withContext AiGuideMessage(
                        sender = MessageSender.AI,
                        text = responseText.trim(),
                        suggestedRitual = detectedRitual,
                        fairDakshinaRange = fairDakshinaRange,
                        showVerifiedPurohitAction = shouldShowPurohitAction,
                        showHelplineAction = userMessage.contains("harass", ignoreCase = true) ||
                                userMessage.contains("cheat", ignoreCase = true) ||
                                userMessage.contains("scam", ignoreCase = true) ||
                                userMessage.contains("police", ignoreCase = true) ||
                                userMessage.contains("force", ignoreCase = true)
                    )
                }
            } catch (e: Exception) {
                // Fallback to intelligent local rules
            }
        }

        // Local Rule-Based Guidance (Always reliable offline or during prototype)
        val localResponse = generateLocalGuidance(userMessage)
        AiGuideMessage(
            sender = MessageSender.AI,
            text = localResponse,
            suggestedRitual = detectedRitual,
            fairDakshinaRange = fairDakshinaRange,
            showVerifiedPurohitAction = true,
            showHelplineAction = userMessage.contains("scam", ignoreCase = true) ||
                    userMessage.contains("cheat", ignoreCase = true) ||
                    userMessage.contains("force", ignoreCase = true)
        )
    }

    private fun detectRitual(query: String): String? {
        val lower = query.lowercase()
        return when {
            lower.contains("pind") || lower.contains("shradh") || lower.contains("tarpan") -> "Pind Daan & Shradh"
            lower.contains("sankalp") || lower.contains("snan") || lower.contains("bath") || lower.contains("dip") -> "Basic Sankalpa & Snan"
            lower.contains("rudra") || lower.contains("shiva") || lower.contains("abhishek") -> "Rudrabhishek Puja"
            lower.contains("havan") || lower.contains("yagya") || lower.contains("homa") -> "Vedic Havan & Yagya"
            lower.contains("aarti") || lower.contains("darshan") || lower.contains("daily puja") -> "Daily Puja & Aarti"
            lower.contains("asthi") || lower.contains("visarjan") || lower.contains("ashes") -> "Asthi Visarjan Vidhi"
            lower.contains("mundan") || lower.contains("hair") || lower.contains("tonsure") -> "Mundan Sanskar"
            else -> null
        }
    }

    private fun getFairDakshinaForQuery(query: String): String? {
        val lower = query.lowercase()
        return when {
            lower.contains("pind") || lower.contains("shradh") || lower.contains("tarpan") -> "₹1,100 – ₹3,100"
            lower.contains("sankalp") || lower.contains("snan") -> "₹101 – ₹251"
            lower.contains("rudra") || lower.contains("abhishek") -> "₹501 – ₹1,100"
            lower.contains("havan") || lower.contains("yagya") -> "₹1,500 – ₹3,500"
            lower.contains("aarti") || lower.contains("daily puja") -> "₹251 – ₹501"
            lower.contains("asthi") || lower.contains("visarjan") -> "₹501 – ₹1,500"
            lower.contains("mundan") -> "₹251 – ₹501"
            else -> null
        }
    }

    private fun generateLocalGuidance(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("pind") || q.contains("shradh") || q.contains("tarpan") -> {
                """🕉️ **Fair Guidance for Pind Daan & Shradh at Triveni Sangam:**

• **Standard Dakshina Range:** **₹1,100 – ₹3,100** (inclusive of 16 pind samagri, kush grass, barley, black sesame, and sacred cloth).
• **Guardrail:** Never agree to ambiguous or escalating charges during the mid-ritual Sankalpa.
• **Panda Verification:** Verify that the Purohit holds an official **Tirtha Purohit Sabha Accreditation ID (PRY-PUROHIT-XX)** or checks family Bahi-Khata records.
• **Action:** Confirm the complete all-inclusive fee beforehand or book a certified Purohit directly via the app's Verified Purohits tab."""
            }
            q.contains("sankalp") || q.contains("snan") || q.contains("bath") || q.contains("dip") -> {
                """🌊 **Sankalpa & Holy Bathing Guidance:**

• **Standard Dakshina Range:** **₹101 – ₹251** per family.
• **Important Principle:** Dakshina is strictly voluntary. A priest cannot mandate thousands of rupees for reciting a traditional Sankalpa.
• **Safety Tip:** Take flowers and Gangajal in hand only AFTER agreeing on the voluntary offering. Avoid aggressive touts on the Ghat ramps.
• **Boat Tip:** Boat rates are regulated by Prayagraj Nagar Nigam (approx. ₹100–₹150 per person for shared Sangam ride)."""
            }
            q.contains("verify") || q.contains("identification") || q.contains("fake") || q.contains("badge") -> {
                """🪪 **How to Verify a Tirth Purohit / Panda in Prayagraj:**

1. **Official ID Card:** Ask for their Prayagraj Tirth Purohit Sabha / Nagar Nigam registration card.
2. **Genealogical Record (Bahi-Khata):** Authentic Pandas maintain centuries-old handwritten family ledgers categorized by state, district, and village gotra.
3. **App Verification:** Browse our built-in **Verified Purohits Directory** where every priest's accreditation ID, lineage, and transparent ritual tariff is listed.
4. **Helpline:** If anyone intimidates you, report immediately to Sangam Mela Police Helpdesk (Dial 112 or 1920)."""
            }
            q.contains("overcharge") || q.contains("scam") || q.contains("exploit") || q.contains("force") || q.contains("threat") -> {
                """🛡️ **Anti-Exploitation Rules & Devotee Protection:**

• **Rule 1: Fixed Upfront Agreement:** Always specify the exact Dakshina and Puja Samagri inclusions BEFORE placing hands in the Sankalpa water.
• **Rule 2: Voluntary Nature:** Vedic tradition explicitly defines Dakshina as 'Yatha-Shakti' (according to one's capacity). High mandatory fees under emotional pressure are against Dharma.
• **Rule 3: Use Verified Channels:** Only engage priests carrying valid government / Tirtha Sabha QR badges.
• **Helpline:**
  - Prayagraj Pilgrim Emergency: **112**
  - Kumbh / Sangam Police Booth: **1920**
  - Ghat Tourist Assistance Booth: Located at Boat Ghat #1 & Saraswati Ghat."""
            }
            q.contains("rudra") || q.contains("abhishek") || q.contains("shiva") -> {
                """🔱 **Rudrabhishek Puja Guidance:**

• **Standard Dakshina:** **₹501 – ₹1,100** for Brahmin recitation of Sri Rudram.
• **Samagri:** Clarify if milk, honey, belpatra, and bhasma are provided or if you will purchase them from official stalls.
• **Recommended Locations:** Mankameshwar Temple, Someshwar Mahadev, or Triveni Sangam Ghats."""
            }
            q.contains("boat") || q.contains("boatman") || q.contains("rate") || q.contains("fare") -> {
                """🚣‍♂️ **Standard Boat Fare Guidelines (Triveni Sangam):**

• **Shared Boat Ride:** ₹100 – ₹150 per passenger round-trip to Triveni Sangam point.
• **Private Boat (Small 4-6 pax):** ₹600 – ₹1,000 for entire boat (negotiate and fix return time).
• **Life Jackets:** Mandatory for every passenger — ensure the boatman provides them before boarding."""
            }
            else -> {
                """🙏 **Welcome to Prayag AI Guide — Transparent Pilgrimage Advisor**

I am here to protect you from unfair pricing and help you conduct your sacred rituals peacefully:

• **Basic Sankalpa & Snan:** ₹101 – ₹251
• **Daily Puja / Aarti Assistance:** ₹251 – ₹501
• **Full Pind Daan / Shradh Karma:** ₹1,100 – ₹3,100
• **Rudrabhishek Puja:** ₹501 – ₹1,100
• **Vedic Havan & Yagya:** ₹1,500 – ₹3,500

💡 **Tip:** Always verify your Panda's accreditation ID and finalize Dakshina before beginning rituals. Tap below to browse all pre-verified Purohits!"""
            }
        }
    }
}

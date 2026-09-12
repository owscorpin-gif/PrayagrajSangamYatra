package com.prayagraj.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PriestDetails(
    val fullName: String = "",
    val specialization: String = "Vedic Karmakand",
    val experienceYears: Int = 5,
    val languages: List<String> = listOf("Hindi", "Sanskrit"),
    val phoneNumber: String = "",
    val registrationId: String = "",
    val isVerified: Boolean = false,
    val photoUri: String? = null
)

package com.prayagraj.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurohitServiceDto(
    @SerialName("id") val id: String? = null,
    @SerialName("purohit_id") val purohitId: String,
    @SerialName("pooja_name") val poojaName: String,
    @SerialName("category") val category: String,
    @SerialName("duration_hours") val durationHours: Double,
    @SerialName("base_dakshina") val baseDakshina: Double,
    @SerialName("samagri_included") val samagriIncluded: Boolean,
    @SerialName("samagri_extra_cost") val samagriExtraCost: Double = 0.0,
    @SerialName("languages_supported") val languagesSupported: List<String> = listOf("Hindi", "Sanskrit"),
    @SerialName("description") val description: String? = null
)

package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurohitService(
    val id: String = java.util.UUID.randomUUID().toString(),
    @SerialName("ritual_name") val ritualName: String,
    @SerialName("fixed_dakshina") val fixedDakshina: Double,
    val description: String = "",
    @SerialName("materials_included") val materialsIncluded: Boolean = true,
    @SerialName("duration_minutes") val durationMinutes: Int = 45,
    val category: String = "Vedic Ritual"
)

fun PurohitServiceDto.toDomain(category: String = "Vedic Ritual"): PurohitService = PurohitService(
    id = this.id ?: java.util.UUID.randomUUID().toString(),
    ritualName = this.ritualName,
    fixedDakshina = this.fixedDakshina,
    description = this.description,
    materialsIncluded = this.materialsIncluded,
    durationMinutes = this.durationMinutes,
    category = category
)

fun PurohitService.toDto(purohitId: String): PurohitServiceDto = PurohitServiceDto(
    id = this.id,
    purohitId = purohitId,
    ritualName = this.ritualName,
    description = this.description,
    fixedDakshina = this.fixedDakshina,
    durationMinutes = this.durationMinutes,
    materialsIncluded = this.materialsIncluded
)


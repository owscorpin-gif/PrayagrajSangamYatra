package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AccessibilityLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromTagsList(tags: List<String>?): String {
        return if (tags == null) "[]" else json.encodeToString(tags)
    }

    @TypeConverter
    fun toTagsList(tagsJson: String?): List<String> {
        if (tagsJson.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(tagsJson)
        } catch (e: Exception) {
            tagsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    @TypeConverter
    fun fromAccessibilityLevel(level: AccessibilityLevel?): String {
        return level?.name ?: AccessibilityLevel.EASY.name
    }

    @TypeConverter
    fun toAccessibilityLevel(value: String?): AccessibilityLevel {
        if (value.isNullOrBlank()) return AccessibilityLevel.EASY
        return try {
            AccessibilityLevel.valueOf(value.uppercase())
        } catch (e: Exception) {
            AccessibilityLevel.EASY
        }
    }
}

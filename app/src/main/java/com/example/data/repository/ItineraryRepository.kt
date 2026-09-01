package com.example.data.repository

import com.example.data.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ItineraryManager {
    private val _selectedPlaces = MutableStateFlow<List<Place>>(emptyList())
    val selectedPlaces: StateFlow<List<Place>> = _selectedPlaces.asStateFlow()

    fun togglePlaceSelection(place: Place) {
        val current = _selectedPlaces.value.toMutableList()
        if (current.any { it.id == place.id }) {
            current.removeAll { it.id == place.id }
        } else {
            current.add(place)
        }
        _selectedPlaces.value = current
    }

    fun isSelected(placeId: String): Boolean {
        return _selectedPlaces.value.any { it.id == placeId }
    }

    fun addPlace(place: Place) {
        val current = _selectedPlaces.value.toMutableList()
        if (!current.any { it.id == place.id }) {
            current.add(place)
            _selectedPlaces.value = current
        }
    }

    fun removePlace(placeId: String) {
        val current = _selectedPlaces.value.toMutableList()
        current.removeAll { it.id == placeId }
        _selectedPlaces.value = current
    }

    fun reorderPlaces(fromIndex: Int, toIndex: Int) {
        val current = _selectedPlaces.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _selectedPlaces.value = current
        }
    }

    fun moveUp(index: Int) {
        if (index > 0) {
            reorderPlaces(index, index - 1)
        }
    }

    fun moveDown(index: Int) {
        if (index < _selectedPlaces.value.size - 1) {
            reorderPlaces(index, index + 1)
        }
    }

    fun clearItinerary() {
        _selectedPlaces.value = emptyList()
    }
}

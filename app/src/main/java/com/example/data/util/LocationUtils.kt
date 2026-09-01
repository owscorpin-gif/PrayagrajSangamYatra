package com.example.data.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

object LocationUtils {

    /**
     * Checks if fine or coarse location permission is granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Calculates distance between two coordinates in meters using the Haversine formula.
     */
    fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0 // Mean radius of the Earth in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val originLatRad = Math.toRadians(lat1)
        val destLatRad = Math.toRadians(lat2)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(originLatRad) * Math.cos(destLatRad) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusMeters * c
    }

    /**
     * Calculates distance between user and place in meters.
     */
    fun calculateDistanceMeters(
        userLat: Double,
        userLng: Double,
        placeLat: Double,
        placeLng: Double
    ): Float {
        return calculateHaversineDistance(userLat, userLng, placeLat, placeLng).toFloat()
    }

    /**
     * Formats distance in meters to a human-readable string (e.g., "450 m", "1.8 km").
     */
    fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", meters / 1000f)
        }
    }

    /**
     * Asynchronously fetches current GPS location using Google Play Services Fused Location.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        return try {
            suspendCancellableCoroutine { continuation ->
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // Fallback to last known location if immediate GPS lock is null
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            continuation.resume(lastLoc)
                        }.addOnFailureListener {
                            continuation.resume(null)
                        }
                    }
                }.addOnFailureListener {
                    // Try last known location fallback
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                        continuation.resume(lastLoc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

package com.example.data.repository

import com.example.data.model.Place
import com.example.data.remote.DirectionsApiService
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DirectionsRepository(
    private val apiKey: String = ""
) {
    private val api: DirectionsApiService = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DirectionsApiService::class.java)

    suspend fun getRoadRoutePoints(places: List<Place>): List<LatLng> {
        if (places.size < 2) return emptyList()

        val origin = "${places.first().latitude},${places.first().longitude}"
        val destination = "${places.last().latitude},${places.last().longitude}"

        val waypoints = if (places.size > 2) {
            "via:" + places.subList(1, places.size - 1)
                .joinToString("|via:") { "${it.latitude},${it.longitude}" }
        } else null

        return try {
            val response = api.getDirections(
                origin = origin,
                destination = destination,
                waypoints = waypoints,
                apiKey = apiKey
            )

            val encodedPolyline = response.routes.firstOrNull()?.overviewPolyline?.points
            if (!encodedPolyline.isNullOrEmpty()) {
                // Decode Google's encoded polyline string to LatLng points
                PolyUtil.decode(encodedPolyline)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

package com.example.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<Route> = emptyList(),
    @SerializedName("status") val status: String? = null
)

data class Route(
    @SerializedName("overview_polyline") val overviewPolyline: OverviewPolyline? = null
)

data class OverviewPolyline(
    @SerializedName("points") val points: String = ""
)

interface DirectionsApiService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String? = null,
        @Query("key") apiKey: String
    ): DirectionsResponse
}

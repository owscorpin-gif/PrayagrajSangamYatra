package com.prayagraj.app.data.repository

import com.example.data.remote.SupabaseProvider
import com.prayagraj.app.data.model.FleetVehicleDto
import com.prayagraj.app.data.model.RouteFareDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class TransportRepository {
    private val client = SupabaseProvider.client

    private val localVehicles = mutableListOf(
        FleetVehicleDto(
            id = "boat-sangam-01",
            ownerId = "boatman-demo-01",
            title = "Maa Ganga Motor Cruiser",
            vehicleType = "MOTOR_BOAT",
            registrationNumber = "PRY-NAU-2025-089",
            capacity = 12,
            isVerified = true
        ),
        FleetVehicleDto(
            id = "boat-sangam-02",
            ownerId = "boatman-demo-01",
            title = "Yamuna Tarang Traditional Wooden Boat",
            vehicleType = "WOODEN_BOAT",
            registrationNumber = "PRY-NAU-2025-142",
            capacity = 8,
            isVerified = true
        ),
        FleetVehicleDto(
            id = "erick-sangam-01",
            ownerId = "boatman-demo-01",
            title = "Sangam Green Express E-Rickshaw",
            vehicleType = "E_RICKSHAW",
            registrationNumber = "UP70-EV-8842",
            capacity = 4,
            isVerified = true
        )
    )

    private val localRouteFares = mutableListOf(
        RouteFareDto(
            id = "fare-01",
            vehicleId = "boat-sangam-01",
            origin = "Kila Ghat / Fort Ghat",
            destination = "Triveni Sangam Snan Point",
            standardFare = 150.0,
            govCappedMaxFare = 250.0,
            isSharedService = true
        ),
        RouteFareDto(
            id = "fare-02",
            vehicleId = "boat-sangam-01",
            origin = "VIP Ghat",
            destination = "Sangam Confluence (Round Trip)",
            standardFare = 500.0,
            govCappedMaxFare = 800.0,
            isSharedService = false
        ),
        RouteFareDto(
            id = "fare-03",
            vehicleId = "boat-sangam-02",
            origin = "Saraswati Ghat",
            destination = "Triveni Sangam Ghat",
            standardFare = 100.0,
            govCappedMaxFare = 200.0,
            isSharedService = true
        ),
        RouteFareDto(
            id = "fare-04",
            vehicleId = "erick-sangam-01",
            origin = "Prayagraj Junction (Civil Lines Side)",
            destination = "Bandhwa Hanuman Mandir / Sangam Entry",
            standardFare = 60.0,
            govCappedMaxFare = 100.0,
            isSharedService = true
        )
    )

    fun getCurrentUserId(): String? = try {
        client.auth.currentUserOrNull()?.id ?: "boatman-demo-01"
    } catch (e: Exception) {
        "boatman-demo-01"
    }

    suspend fun getOwnerVehicles(ownerId: String): List<FleetVehicleDto> = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext localVehicles.filter { it.ownerId == ownerId || ownerId == "boatman-demo-01" }.toList()
        }
        try {
            val remote = client.from("fleet_vehicles")
                .select { filter { eq("owner_id", ownerId) } }
                .decodeList<FleetVehicleDto>()
            if (remote.isNotEmpty()) remote else localVehicles.toList()
        } catch (e: Exception) {
            localVehicles.toList()
        }
    }

    suspend fun getFaresForVehicle(vehicleId: String): List<RouteFareDto> = withContext(Dispatchers.IO) {
        if (!SupabaseProvider.isConfigured()) {
            return@withContext localRouteFares.filter { it.vehicleId == vehicleId }.toList()
        }
        try {
            val remote = client.from("route_fares")
                .select { filter { eq("vehicle_id", vehicleId) } }
                .decodeList<RouteFareDto>()
            if (remote.isNotEmpty()) remote else localRouteFares.filter { it.vehicleId == vehicleId }.toList()
        } catch (e: Exception) {
            localRouteFares.filter { it.vehicleId == vehicleId }.toList()
        }
    }

    suspend fun addVehicle(vehicle: FleetVehicleDto) = withContext(Dispatchers.IO) {
        val complete = if (vehicle.id.isNullOrBlank()) {
            vehicle.copy(id = UUID.randomUUID().toString())
        } else {
            vehicle
        }
        localVehicles.add(complete)

        if (SupabaseProvider.isConfigured()) {
            try {
                client.from("fleet_vehicles").insert(complete)
            } catch (e: Exception) {
                // Fallback maintains local reactive state
            }
        }
    }

    suspend fun addRouteFare(fareCard: RouteFareDto) = withContext(Dispatchers.IO) {
        val complete = if (fareCard.id.isNullOrBlank()) {
            fareCard.copy(id = UUID.randomUUID().toString())
        } else {
            fareCard
        }
        localRouteFares.add(complete)

        if (SupabaseProvider.isConfigured()) {
            try {
                client.from("route_fares").insert(complete)
            } catch (e: Exception) {
                // Fallback maintains local reactive state
            }
        }
    }
}

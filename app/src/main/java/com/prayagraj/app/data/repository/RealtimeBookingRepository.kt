package com.prayagraj.app.data.repository

import com.prayagraj.app.data.model.BookingRequestDto
import com.prayagraj.app.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class RealtimeBookingRepository {

    private val client = SupabaseProvider.client
    private val postgrest = SupabaseProvider.postgrest
    private val realtime = SupabaseProvider.realtime
    private val auth = SupabaseProvider.auth
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches existing booking requests and listens for incoming real-time requests.
     */
    fun observeBookingRequests(): Flow<List<BookingRequestDto>> = channelFlow {
        val currentVendorId = auth.currentUserOrNull()?.id ?: run {
            close()
            return@channelFlow
        }

        // 1. Initial Fetch of existing pending requests
        val initialList = fetchInitialBookings(currentVendorId)
        val bookings = initialList.toMutableList()
        send(bookings.toList())

        // 2. Set up Supabase Realtime Channel Subscription
        val channel = realtime.channel("vendor_bookings_$currentVendorId")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "booking_requests"
        }

        channel.subscribe()

        // 3. Listen to incoming inserts in real time
        changeFlow.collect { action ->
            val newBooking = json.decodeFromJsonElement(
                BookingRequestDto.serializer(),
                action.record
            )
            if (newBooking.vendorId == currentVendorId) {
                bookings.add(0, newBooking) // Prepend new request
                send(bookings.toList())
            }
        }
    }

    private suspend fun fetchInitialBookings(vendorId: String): List<BookingRequestDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                postgrest["booking_requests"]
                    .select {
                        filter { eq("vendor_id", vendorId) }
                    }
                    .decodeList<BookingRequestDto>()
            }.getOrDefault(emptyList())
        }

    suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                postgrest["booking_requests"]
                    .update({ set("status", newStatus) }) {
                        filter { eq("id", bookingId) }
                    }
                Unit
            }
        }
}


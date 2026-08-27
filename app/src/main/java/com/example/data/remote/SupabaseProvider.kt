package com.example.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import kotlin.time.Duration.Companion.seconds

/**
 * Singleton provider for the Supabase Client.
 * Connects the Prayagraj Spiritual Tourism Android app to Supabase PostgreSQL + PostGIS backend.
 */
object SupabaseProvider {

    // Configure your Supabase project URL and anon public key.
    // In production, these can be provided via BuildConfig or Android Secrets Gradle plugin.
    const val DEFAULT_SUPABASE_URL = "https://xyzcompany.supabase.co"
    const val DEFAULT_SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSJ9.mock_key"

    var supabaseUrl: String = DEFAULT_SUPABASE_URL
    var supabaseAnonKey: String = DEFAULT_SUPABASE_ANON_KEY

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(Postgrest)
            install(Auth) {
                // Configures Supabase Auth for phone OTP authentication
                alwaysAutoRefresh = true
            }
            install(Realtime) {
                reconnectDelay = 5.seconds
            }
        }
    }

    val postgrest: Postgrest
        get() = client.postgrest

    val auth: Auth
        get() = client.auth

    val realtime: Realtime
        get() = client.realtime
}

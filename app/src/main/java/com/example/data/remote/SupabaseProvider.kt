package com.example.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.seconds

/**
 * Singleton provider for the Supabase Client.
 * Connects the Prayagraj Spiritual Tourism Android app to Supabase PostgreSQL + PostGIS backend.
 */
object SupabaseProvider {

    // Default Supabase project URL and anon public key placeholder
    const val DEFAULT_SUPABASE_URL = "https://xyzcompany.supabase.co"
    const val DEFAULT_SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSJ9.mock_key"

    var supabaseUrl: String = DEFAULT_SUPABASE_URL
    var supabaseAnonKey: String = DEFAULT_SUPABASE_ANON_KEY

    @Volatile
    private var cachedClient: SupabaseClient? = null

    /**
     * Checks whether the Supabase connection URL is configured with a live user project
     * rather than an unresolvable default placeholder.
     */
    fun isConfigured(): Boolean {
        val url = supabaseUrl.trim().lowercase()
        return url.startsWith("https://") &&
                !url.contains("xyzcompany.supabase.co") &&
                !url.contains("example.com") &&
                url.endsWith(".supabase.co") &&
                supabaseAnonKey.isNotBlank() &&
                !supabaseAnonKey.contains("mock_key")
    }

    fun updateConfig(url: String, anonKey: String) {
        supabaseUrl = url.trim()
        supabaseAnonKey = anonKey.trim()
        cachedClient = null
    }

    val client: SupabaseClient
        get() {
            val existing = cachedClient
            if (existing != null) return existing
            return synchronized(this) {
                val current = cachedClient
                if (current != null) return current
                val newClient = createSupabaseClient(
                    supabaseUrl = if (supabaseUrl.isBlank()) DEFAULT_SUPABASE_URL else supabaseUrl,
                    supabaseKey = if (supabaseAnonKey.isBlank()) DEFAULT_SUPABASE_ANON_KEY else supabaseAnonKey
                ) {
                    install(Postgrest)
                    install(Auth) {
                        alwaysAutoRefresh = true
                    }
                    install(Realtime) {
                        reconnectDelay = 5.seconds
                    }
                    install(Storage)
                }
                cachedClient = newClient
                newClient
            }
        }

    val postgrest: Postgrest
        get() = client.postgrest

    val auth: Auth
        get() = client.auth

    val realtime: Realtime
        get() = client.realtime

    val storage: Storage
        get() = client.storage
}


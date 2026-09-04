package com.prayagraj.app.data.remote

import com.example.data.remote.SupabaseProvider as CoreSupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * SupabaseProvider for Prayagraj app package com.prayagraj.app.data.remote
 */
object SupabaseProvider {
    val client: SupabaseClient get() = CoreSupabaseProvider.client

    val auth: Auth get() = CoreSupabaseProvider.auth
    val postgrest: Postgrest get() = CoreSupabaseProvider.postgrest
    val realtime: Realtime get() = CoreSupabaseProvider.realtime
    val storage: Storage get() = CoreSupabaseProvider.storage

    fun isConfigured(): Boolean = CoreSupabaseProvider.isConfigured()
    fun updateConfig(url: String, anonKey: String) = CoreSupabaseProvider.updateConfig(url, anonKey)
}

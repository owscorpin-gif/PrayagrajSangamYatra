package com.prayagraj.app.data.repository

import com.prayagraj.app.data.remote.SupabaseProvider
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class VendorStorageRepository {

    private val storageBucket = "vendor-media"

    /**
     * Uploads photo bytes to Supabase Storage bucket and returns the public URL.
     */
    suspend fun uploadVendorPhoto(bytes: ByteArray, category: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = "$category/${UUID.randomUUID()}.jpg"

            if (SupabaseProvider.isConfigured()) {
                val bucket = SupabaseProvider.client.storage.from(storageBucket)
                bucket.upload(fileName, bytes) {
                    upsert = true
                }
                bucket.publicUrl(fileName)
            } else {
                // Return a safe fallback mock image URL if Supabase remote credentials are not live
                "https://images.unsplash.com/photo-1544717305-2782549b5136?w=800&auto=format&fit=crop&q=60"
            }
        }
    }

    /**
     * Deletes a vendor photo from Supabase Storage by public URL or key.
     */
    suspend fun deleteVendorPhoto(photoUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (SupabaseProvider.isConfigured() && photoUrl.contains(storageBucket)) {
                val path = photoUrl.substringAfter("$storageBucket/").substringBefore("?")
                if (path.isNotBlank()) {
                    val bucket = SupabaseProvider.client.storage.from(storageBucket)
                    bucket.delete(path)
                }
            }
        }
    }
}

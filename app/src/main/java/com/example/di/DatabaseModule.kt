package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseModule as LocalDatabaseModule
import com.example.data.local.ItineraryDao
import com.example.data.local.PlaceDao
import com.example.data.repository.PlacesRepository
import com.example.data.repository.PlacesRepositoryImpl

/**
 * Dependency injection module providing AppDatabase, DAOs, and repository bindings.
 */
object DatabaseModule {

    fun provideDatabase(context: Context): AppDatabase {
        return LocalDatabaseModule.provideDatabase(context)
    }

    fun providePlaceDao(database: AppDatabase): PlaceDao {
        return database.placeDao()
    }

    fun providePlaceDao(context: Context): PlaceDao {
        return provideDatabase(context).placeDao()
    }

    fun provideItineraryDao(database: AppDatabase): ItineraryDao {
        return database.itineraryDao()
    }

    fun provideItineraryDao(context: Context): ItineraryDao {
        return provideDatabase(context).itineraryDao()
    }

    fun providePlacesRepository(placeDao: PlaceDao): PlacesRepository {
        return PlacesRepositoryImpl(customDao = placeDao)
    }

    fun providePlacesRepository(context: Context): PlacesRepository {
        val placeDao = providePlaceDao(context)
        return PlacesRepositoryImpl(customDao = placeDao)
    }

    fun provideOfflineFirstPlacesRepository(context: Context): com.example.data.repository.OfflineFirstPlacesRepository {
        val placeDao = providePlaceDao(context)
        return PlacesRepositoryImpl(customDao = placeDao)
    }
}

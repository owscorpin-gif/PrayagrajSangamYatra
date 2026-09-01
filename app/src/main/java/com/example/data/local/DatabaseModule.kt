package com.example.data.local

import android.content.Context
import androidx.room.Room
import com.example.data.repository.PlacesRepository
import com.example.data.repository.PlacesRepositoryImpl
import com.example.data.repository.RitualBookingRepository
import com.example.data.repository.RitualBookingRepositoryImpl

/**
 * Dependency injection module for Room database and DAO instances.
 * Provides the [AppDatabase] singleton and links it directly to [PlaceDao], [ItineraryDao],
 * and [RitualBookingDao] for clean integration with the repository and viewmodel layers.
 */
object DatabaseModule {

    private const val DATABASE_NAME = "prayagraj_app_database"

    @Volatile
    private var databaseInstance: AppDatabase? = null

    /**
     * Provides the singleton [AppDatabase] instance.
     */
    fun provideDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            databaseInstance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration(dropAllTables = false)
                .build()
                .also { databaseInstance = it }
        }
    }

    /**
     * Provides the [PlaceDao] instance directly from the [AppDatabase].
     */
    fun providePlaceDao(database: AppDatabase): PlaceDao {
        return database.placeDao()
    }

    /**
     * Provides the [PlaceDao] instance using the application context.
     */
    fun providePlaceDao(context: Context): PlaceDao {
        return providePlaceDao(provideDatabase(context))
    }

    /**
     * Provides the [ItineraryDao] instance directly from the [AppDatabase].
     */
    fun provideItineraryDao(database: AppDatabase): ItineraryDao {
        return database.itineraryDao()
    }

    /**
     * Provides the [ItineraryDao] instance using the application context.
     */
    fun provideItineraryDao(context: Context): ItineraryDao {
        return provideItineraryDao(provideDatabase(context))
    }

    /**
     * Provides the [RitualBookingDao] instance directly from the [AppDatabase].
     */
    fun provideRitualBookingDao(database: AppDatabase): RitualBookingDao {
        return database.ritualBookingDao()
    }

    /**
     * Provides the [RitualBookingDao] instance using the application context.
     */
    fun provideRitualBookingDao(context: Context): RitualBookingDao {
        return provideRitualBookingDao(provideDatabase(context))
    }

    /**
     * Provides the [RitualBookingRepository] instance wired with the Room [RitualBookingDao].
     */
    fun provideRitualBookingRepository(context: Context): RitualBookingRepository {
        val dao = provideRitualBookingDao(context)
        return RitualBookingRepositoryImpl(ritualBookingDao = dao)
    }

    /**
     * Provides the [RitualBookingRepository] instance wired with a specific [RitualBookingDao].
     */
    fun provideRitualBookingRepository(ritualBookingDao: RitualBookingDao): RitualBookingRepository {
        return RitualBookingRepositoryImpl(ritualBookingDao = ritualBookingDao)
    }

    /**
     * Provides the [PlacesRepository] instance wired with the Room [PlaceDao].
     */
    fun providePlacesRepository(context: Context): PlacesRepository {
        val placeDao = providePlaceDao(context)
        return PlacesRepositoryImpl(customDao = placeDao)
    }

    /**
     * Provides the [PlacesRepository] instance wired with a specific [PlaceDao].
     */
    fun providePlacesRepository(placeDao: PlaceDao): PlacesRepository {
        return PlacesRepositoryImpl(customDao = placeDao)
    }
}

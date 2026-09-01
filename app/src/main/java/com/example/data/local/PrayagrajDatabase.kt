package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PlaceEntity::class, ItineraryStopEntity::class, RitualBookingEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PrayagrajDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun itineraryDao(): ItineraryDao
    abstract fun ritualBookingDao(): RitualBookingDao

    companion object {
        @Volatile
        private var INSTANCE: PrayagrajDatabase? = null

        fun getDatabase(context: Context): PrayagrajDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrayagrajDatabase::class.java,
                    "prayagraj_offline.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

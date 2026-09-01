package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseModule

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = DatabaseModule.provideDatabase(this)
    }

    companion object {
        lateinit var instance: MainApplication
            private set
        lateinit var database: AppDatabase
            private set
    }
}

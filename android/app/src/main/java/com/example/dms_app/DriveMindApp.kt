package com.example.dms_app

import android.app.Application

class DriveMindApp : Application() {
    companion object {
        lateinit var instance: DriveMindApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

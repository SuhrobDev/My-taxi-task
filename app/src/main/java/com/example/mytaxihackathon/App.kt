package com.example.mytaxihackathon

import android.app.Application
import android.content.res.Resources
import com.example.mytaxihackathon.di.activityModule
import com.example.mytaxihackathon.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    companion object {
        lateinit var resources: Resources
    }

    override fun onCreate() {
        super.onCreate()
        Companion.resources = resources
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule, activityModule)
        }
    }
}
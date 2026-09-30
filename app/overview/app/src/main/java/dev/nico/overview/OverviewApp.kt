package dev.nico.overview

import android.app.Application
import android.content.Intent
import dev.nico.overview.drivemode.DriveModeService

class OverviewApp : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
        startForegroundService(Intent(this, DriveModeService::class.java))
    }
}

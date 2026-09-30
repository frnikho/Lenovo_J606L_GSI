package dev.nico.overview.drivemode

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import androidx.car.app.connection.CarConnection
import androidx.lifecycle.LifecycleService
import dev.nico.overview.OverviewApp
import dev.nico.overview.R

/** Observe la projection Android Auto en permanence (service au premier plan discret). */
class DriveModeService : LifecycleService() {
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        val controller = (application as OverviewApp).graph.driveMode
        CarConnection(this).type.observe(this) { type ->
            controller.onCarConnection(type == CarConnection.CONNECTION_TYPE_PROJECTION)
        }
    }

    private fun notification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.drive_channel), NotificationManager.IMPORTANCE_MIN),
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_local_gas_station)
            .setContentTitle(getString(R.string.drive_notification))
            .setOngoing(true)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "drive_mode"
        const val NOTIFICATION_ID = 1
    }
}

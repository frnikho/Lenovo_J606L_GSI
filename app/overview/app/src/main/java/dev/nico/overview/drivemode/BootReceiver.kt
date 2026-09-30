package dev.nico.overview.drivemode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Le démarrage du processus suffit : OverviewApp lance le service. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}

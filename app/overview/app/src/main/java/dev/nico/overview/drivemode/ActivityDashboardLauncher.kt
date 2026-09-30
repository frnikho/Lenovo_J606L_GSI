package dev.nico.overview.drivemode

import android.content.Context
import android.content.Intent
import dev.nico.overview.DashboardActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Ouverture en arrière-plan autorisée par START_ACTIVITIES_FROM_BACKGROUND (priv-app). */
class ActivityDashboardLauncher(private val context: Context) : DashboardLauncher {
    private val _closeRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val closeRequests: SharedFlow<Unit> = _closeRequests.asSharedFlow()

    override fun show() {
        context.startActivity(
            Intent(context, DashboardActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
        )
    }

    override fun close() {
        _closeRequests.tryEmit(Unit)
    }
}

package dev.nico.overview.drivemode

import android.service.trust.TrustAgentService
import dev.nico.overview.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Garde la tablette déverrouillée pendant la projection Android Auto (comme Smart Lock). Si le processus meurt,
 * le système retire la confiance de lui-même.
 */
class OverviewTrustAgent : TrustAgentService() {
    private val scope = MainScope()

    override fun onCreate() {
        super.onCreate()
        setManagingTrust(true)
        scope.launch { TrustBridge.trusted.collect(::apply) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun apply(trusted: Boolean) {
        if (trusted) {
            grantTrust(getString(R.string.trust_message), 0L, FLAG_GRANT_TRUST_TEMPORARY_AND_RENEWABLE)
        } else {
            revokeTrust()
        }
    }
}

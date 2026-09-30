package android.service.trust;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Signatures de l'API système @SystemApi (AOSP frameworks/base). Jamais empaquetée : l'appli la voit en compileOnly,
 * le framework fournit la vraie classe à l'exécution.
 */
public class TrustAgentService extends Service {
    public static final String SERVICE_INTERFACE = "android.service.trust.TrustAgentService";
    public static final int FLAG_GRANT_TRUST_INITIATED_BY_USER = 1 << 0;
    public static final int FLAG_GRANT_TRUST_DISMISS_KEYGUARD = 1 << 1;
    public static final int FLAG_GRANT_TRUST_TEMPORARY_AND_RENEWABLE = 1 << 2;

    public final void grantTrust(CharSequence message, long durationMs, int flags) {
        throw new RuntimeException("Stub!");
    }

    public final void revokeTrust() {
        throw new RuntimeException("Stub!");
    }

    public final void setManagingTrust(boolean managingTrust) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public final IBinder onBind(Intent intent) {
        throw new RuntimeException("Stub!");
    }
}

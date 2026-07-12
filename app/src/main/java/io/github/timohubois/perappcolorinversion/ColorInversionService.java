package io.github.timohubois.perappcolorinversion;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;

public final class ColorInversionService extends AccessibilityService {
    private static final int APPLY_DELAY_MS = 120;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean inversionEnabled;
    private String pendingPackage = "";

    private final Runnable applyPendingState = () -> setInversion(
            AppSelection.isFeatureEnabled(this)
                    && AppSelection.getPackages(this).contains(pendingPackage)
    );

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }
        CharSequence packageName = event.getPackageName();
        if (packageName == null) {
            return;
        }
        String observedPackage = packageName.toString();
        // Changing the inversion setting itself can make SystemUI emit a window event.
        // Ignore those transient events so they cannot undo the selected-app state.
        if ("com.android.systemui".equals(observedPackage)
                || "android".equals(observedPackage)
                || getPackageName().equals(observedPackage)) {
            return;
        }
        pendingPackage = observedPackage;
        handler.removeCallbacks(applyPendingState);
        handler.postDelayed(applyPendingState, APPLY_DELAY_MS);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        inversionEnabled = readInversion();
        setInversion(false);
    }

    @Override
    public void onInterrupt() {
        setInversion(false);
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        handler.removeCallbacksAndMessages(null);
        setInversion(false);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        setInversion(false);
        super.onDestroy();
    }

    private boolean readInversion() {
        return Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED,
                0
        ) == 1;
    }

    private void setInversion(boolean enabled) {
        if (inversionEnabled == enabled && readInversion() == enabled) {
            return;
        }
        Settings.Secure.putInt(
                getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED,
                enabled ? 1 : 0
        );
        inversionEnabled = enabled;
    }
}

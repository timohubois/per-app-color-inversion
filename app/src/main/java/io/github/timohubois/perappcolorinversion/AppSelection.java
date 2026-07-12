package io.github.timohubois.perappcolorinversion;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class AppSelection {
    private static final String PREFS = "app_inverter";
    private static final String KEY_ENABLED = "feature_enabled";
    private static final String KEY_PACKAGES = "inverted_packages";

    private AppSelection() {}

    static boolean isFeatureEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, true);
    }

    static void setFeatureEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    static Set<String> getPackages(Context context) {
        return new HashSet<>(prefs(context).getStringSet(KEY_PACKAGES, Collections.emptySet()));
    }

    static void setPackageEnabled(Context context, String packageName, boolean enabled) {
        Set<String> packages = getPackages(context);
        if (enabled) {
            packages.add(packageName);
        } else {
            packages.remove(packageName);
        }
        prefs(context).edit().putStringSet(KEY_PACKAGES, packages).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}

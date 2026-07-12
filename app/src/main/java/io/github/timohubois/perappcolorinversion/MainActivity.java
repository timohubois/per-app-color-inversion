package io.github.timohubois.perappcolorinversion;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MainActivity extends Activity {
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The settings UI itself is never inverted.
        Settings.Secure.putInt(
                getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED,
                0
        );
        if (content != null) {
            showSettings();
        }
    }

    private void showSettings() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.WHITE);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(28, 50, 28, 40);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = text(getString(R.string.app_name), 28);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(title);

        TextView explanation = text(getString(R.string.explanation), 17);
        addWithMargins(explanation, 28, 20);

        Switch master = new Switch(this);
        master.setText(R.string.enable_automatic_inversion);
        master.setTextSize(19);
        master.setTextColor(Color.BLACK);
        master.setChecked(AppSelection.isFeatureEnabled(this));
        master.setOnCheckedChangeListener((button, enabled) -> {
            AppSelection.setFeatureEnabled(this, enabled);
            if (!enabled) {
                Settings.Secure.putInt(
                        getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED,
                        0
                );
            }
        });
        addWithMargins(master, 10, 22);

        TextView status = text(getString(
                isServiceEnabled() ? R.string.service_enabled : R.string.service_disabled
        ), 17);
        content.addView(status);

        Button accessibilityButton = new Button(this);
        accessibilityButton.setText(R.string.open_accessibility_settings);
        accessibilityButton.setTextSize(16);
        accessibilityButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addWithMargins(accessibilityButton, 12, 30);

        TextView appsTitle = text(getString(R.string.invert_these_apps), 22);
        addWithMargins(appsTitle, 6, 12);

        Set<String> selected = AppSelection.getPackages(this);
        for (LaunchableApp app : getLaunchableApps()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(app.label);
            checkBox.setTextSize(18);
            checkBox.setTextColor(Color.BLACK);
            checkBox.setPadding(4, 9, 4, 9);
            checkBox.setChecked(selected.contains(app.packageName));
            checkBox.setOnCheckedChangeListener((button, enabled) ->
                    AppSelection.setPackageEnabled(this, app.packageName, enabled));
            content.addView(checkBox, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        setContentView(scrollView);
    }

    private List<LaunchableApp> getLaunchableApps() {
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(launcher, 0);
        List<LaunchableApp> apps = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo match : matches) {
            String packageName = match.activityInfo.packageName;
            if (packageName.equals(getPackageName()) || !seen.add(packageName)) {
                continue;
            }
            CharSequence label = match.loadLabel(getPackageManager());
            apps.add(new LaunchableApp(
                    packageName,
                    label == null ? packageName : label.toString()
            ));
        }
        Collator collator = Collator.getInstance(Locale.getDefault());
        apps.sort((first, second) -> collator.compare(first.label, second.label));
        return apps;
    }

    private TextView text(String value, int size) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(Color.BLACK);
        return view;
    }

    private void addWithMargins(android.view.View view, int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, top, 0, bottom);
        content.addView(view, params);
    }

    private boolean isServiceEnabled() {
        String enabled = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabled == null) {
            return false;
        }
        ComponentName component = new ComponentName(this, ColorInversionService.class);
        String full = component.flattenToString();
        String shortName = component.flattenToShortString();
        for (String entry : enabled.split(":")) {
            if (entry.equalsIgnoreCase(full) || entry.equalsIgnoreCase(shortName)) {
                return true;
            }
        }
        return false;
    }

    private static final class LaunchableApp {
        final String packageName;
        final String label;

        LaunchableApp(String packageName, String label) {
            this.packageName = packageName;
            this.label = label;
        }
    }
}

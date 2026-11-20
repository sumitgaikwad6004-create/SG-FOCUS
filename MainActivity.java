package com.sg.focus;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(0, 0, 0);
    private static final int PANEL = Color.rgb(18, 18, 18);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(165, 165, 165);
    private EditText blockedApps, educationKeywords;
    private TextView usageValue, serviceStatus;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(24), dp(22), dp(28));
        scroll.addView(page);
        setContentView(scroll);

        TextView logo = text("SG", 38, WHITE, true);
        page.addView(logo);
        TextView tagline = text("DIGITAL DISCIPLINE", 11, MUTED, true);
        tagline.setLetterSpacing(0.18f);
        page.addView(tagline);
        addGap(page, 24);

        addSection(page, "TODAY'S SCREEN TIME");
        usageValue = text(getTodayUsage(), 34, WHITE, true);
        page.addView(usageValue);
        TextView usageHint = text("Total device usage since midnight", 12, MUTED, false);
        page.addView(usageHint);
        addGap(page, 14);
        Button refresh = button("REFRESH SCREEN TIME");
        refresh.setOnClickListener(v -> usageValue.setText(getTodayUsage()));
        page.addView(refresh);
        Button usageAccess = button("OPEN USAGE ACCESS SETTINGS");
        usageAccess.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        page.addView(usageAccess);
        addGap(page, 24);

        addSection(page, "FOCUS PROTECTION");
        serviceStatus = text(isAccessibilityEnabled() ? "ACCESSIBILITY SERVICE: ENABLED" :
                "ACCESSIBILITY SERVICE: NOT ENABLED", 12, MUTED, true);
        page.addView(serviceStatus);
        Button accessibility = button("CONFIGURE ACCESSIBILITY SERVICE");
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        page.addView(accessibility);
        TextView explanation = text("Enable “SG Focus Protection” in Android Accessibility settings. SG uses this permission only to detect selected apps and content labels, then applies your rules on-device.", 12, MUTED, false);
        page.addView(explanation);
        addGap(page, 22);

        addSection(page, "BLOCK DISTRACTING APPS");
        blockedApps = edit("One Android package name per line", getPrefs("blocked_apps",
                ""));
        blockedApps.setMinLines(3);
        page.addView(blockedApps);
        TextView appsHint = text("Apps listed here are blocked completely while protection is active. Remove an app's package name if you only want content-level rules.", 11, MUTED, false);
        page.addView(appsHint);
        addGap(page, 18);

        addSection(page, "CONTENT FILTERS");
        TextView filterInfo = text("The service checks visible labels inside supported apps. App interfaces change often, so detection cannot be guaranteed on every version.", 12, MUTED, false);
        page.addView(filterInfo);
        addGap(page, 8);
        TextView contentLabels = text("Instagram Reels • YouTube Shorts • WhatsApp Status / Updates", 13, WHITE, true);
        page.addView(contentLabels);
        addGap(page, 14);

        addSection(page, "EDUCATIONAL CHANNEL ALLOWLIST");
        educationKeywords = edit("Channel names or unique keywords, one per line",
                getPrefs("education_keywords", "Physics Wallah\nKhan Academy"));
        educationKeywords.setMinLines(3);
        page.addView(educationKeywords);
        TextView allowHint = text("If a visible YouTube screen contains one of these keywords, SG will not block it. This is a best-effort text match, not a verified channel identity check.", 11, MUTED, false);
        page.addView(allowHint);
        addGap(page, 18);

        Button save = button("SAVE MY RULES");
        save.setOnClickListener(v -> {
            getSharedPreferences("sg", MODE_PRIVATE).edit()
                    .putString("blocked_apps", blockedApps.getText().toString())
                    .putString("education_keywords", educationKeywords.getText().toString())
                    .apply();
            toast("Rules saved on this device");
        });
        page.addView(save);
        addGap(page, 16);

        TextView privacy = text("PRIVACY FIRST\nNo account. No analytics SDK. No cloud upload. Usage totals and rules stay on this device. Accessibility permission is powerful: enable it only if you understand and want these functions.", 11, MUTED, false);
        privacy.setPadding(dp(14), dp(14), dp(14), dp(14));
        privacy.setBackgroundColor(PANEL);
        page.addView(privacy);
        addGap(page, 10);
        TextView version = text("SG  •  VERSION 1.0.0  •  BLACK / WHITE", 10, MUTED, true);
        version.setGravity(Gravity.CENTER);
        page.addView(version);
    }

    private String getTodayUsage() {
        if (!hasUsagePermission()) return "—";
        try {
            UsageStatsManager manager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
            List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    c.getTimeInMillis(), System.currentTimeMillis());
            long total = 0;
            if (stats != null) for (UsageStats s : stats) total += s.getTotalTimeInForeground();
            long minutes = total / 60000;
            return String.format(Locale.getDefault(), "%dh %02dm", minutes / 60, minutes % 60);
        } catch (Exception e) { return "Unavailable"; }
    }

    private boolean hasUsagePermission() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) { return false; }
    }

    private boolean isAccessibilityEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabled != null && enabled.toLowerCase(Locale.ROOT).contains(getPackageName().toLowerCase(Locale.ROOT));
    }

    private String getPrefs(String key, String fallback) {
        return getSharedPreferences("sg", MODE_PRIVATE).getString(key, fallback);
    }

    private void addSection(LinearLayout parent, String value) {
        TextView t = text(value, 11, MUTED, true);
        t.setLetterSpacing(0.13f);
        parent.addView(t);
        addGap(parent, 8);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setLineSpacing(dp(3), 1f);
        return t;
    }

    private EditText edit(String hint, String value) {
        EditText e = new EditText(this);
        e.setText(value); e.setHint(hint); e.setHintTextColor(MUTED);
        e.setTextColor(WHITE); e.setTextSize(13);
        e.setGravity(Gravity.TOP | Gravity.START);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setBackgroundColor(PANEL);
        e.setSingleLine(false);
        return e;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label); b.setTextSize(11); b.setTextColor(Color.BLACK);
        b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(WHITE));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(7); lp.bottomMargin = dp(4);
        b.setLayoutParams(lp);
        return b;
    }

    private void addGap(LinearLayout p, int h) {
        View v = new View(this);
        p.addView(v, new LinearLayout.LayoutParams(1, dp(h)));
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private void toast(String s) {
        android.widget.Toast.makeText(this, s, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override protected void onResume() {
        super.onResume();
        if (usageValue != null) usageValue.setText(getTodayUsage());
        if (serviceStatus != null) serviceStatus.setText(isAccessibilityEnabled() ?
                "ACCESSIBILITY SERVICE: ENABLED" : "ACCESSIBILITY SERVICE: NOT ENABLED");
    }
}

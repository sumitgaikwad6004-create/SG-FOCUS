package com.sg.focus;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FocusAccessibilityService extends AccessibilityService {
    private long lastActionAt = 0L;
    private static final long ACTION_COOLDOWN_MS = 1200L;

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        if (pkg.equals(getPackageName())) return;

        String blocked = prefs("blocked_apps", "");
        if (containsLine(blocked, pkg)) {
            actHome();
            return;
        }

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return;

        String visible = collectVisibleText(getRootInActiveWindow());
        if (visible.isEmpty()) return;
        String lower = visible.toLowerCase(Locale.ROOT);

        if ("com.google.android.youtube".equals(pkg)) {
            if (matchesAnyLine(lower, prefs("education_keywords", ""))) return;
            if (containsAny(lower, "shorts", "short video")) actBack();
        } else if ("com.instagram.android".equals(pkg)) {
            if (containsAny(lower, "reels", "reel")) actBack();
        } else if ("com.whatsapp".equals(pkg)) {
            if (containsAny(lower, "status", "updates")) actBack();
        }
    }

    private String collectVisibleText(AccessibilityNodeInfo root) {
        if (root == null) return "";
        StringBuilder out = new StringBuilder();
        List<AccessibilityNodeInfo> stack = new ArrayList<>();
        stack.add(root);
        int visited = 0;
        while (!stack.isEmpty() && visited++ < 300) {
            AccessibilityNodeInfo node = stack.remove(stack.size() - 1);
            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();
            if (text != null) out.append(text).append(' ');
            if (desc != null) out.append(desc).append(' ');
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) stack.add(child);
            }
        }
        return out.toString();
    }

    private boolean containsLine(String lines, String value) {
        for (String line : lines.split("\\n")) if (line.trim().equalsIgnoreCase(value)) return true;
        return false;
    }

    private boolean matchesAnyLine(String text, String lines) {
        for (String line : lines.split("\\n")) {
            String term = line.trim().toLowerCase(Locale.ROOT);
            if (!term.isEmpty() && text.contains(term)) return true;
        }
        return false;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }

    private String prefs(String key, String fallback) {
        return getSharedPreferences("sg", MODE_PRIVATE).getString(key, fallback);
    }

    private void actBack() {
        if (readyForAction()) performGlobalAction(GLOBAL_ACTION_BACK);
    }
    private void actHome() {
        if (readyForAction()) performGlobalAction(GLOBAL_ACTION_HOME);
    }
    private boolean readyForAction() {
        long now = System.currentTimeMillis();
        if (now - lastActionAt < ACTION_COOLDOWN_MS) return false;
        lastActionAt = now;
        return true;
    }

    @Override public void onInterrupt() { }
}

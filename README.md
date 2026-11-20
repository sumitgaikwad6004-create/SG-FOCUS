# SG — Digital Discipline

A privacy-first Android focus-control starter app with a monochrome interface.

## Included in this project

- Daily screen-time total using Android `UsageStatsManager`
- App-level blocking by Android package name
- Best-effort content-label detection for Instagram Reels, YouTube Shorts, and WhatsApp Status/Updates
- A basic YouTube educational-keyword allowlist
- Local-only settings
- GitHub Actions workflow that builds and uploads a debug APK

## Important limitations

This is an initial working project, not a guarantee of flawless blocking.

1. Instagram, YouTube, and WhatsApp do not provide public Android APIs for independently hiding Reels, Shorts, or Status. SG uses Android Accessibility Service to inspect visible accessibility labels and apply a Back action. App updates, language, device skins, and screen layouts can change those labels.
2. The educational allowlist is a best-effort text match. It does not verify a channel's identity, reliably distinguish long videos from Shorts, or guarantee that every allowed video is educational.
3. App blocking applies to package names entered in the app. The default list includes Instagram, YouTube, and WhatsApp; remove a package to use only content-level filtering for that app.
4. Android may restrict usage statistics or accessibility behavior depending on OS version and manufacturer settings.
5. The app does not silently grant permissions. The user must enable Usage Access and Accessibility Service manually in Android Settings.

## Build on GitHub

1. Create a GitHub repository named `SG`.
2. Upload the contents of this ZIP to the repository root (not the ZIP file itself).
3. Open **Actions** and enable workflows if GitHub asks.
4. Run **Build SG Android APK** using **Run workflow**, or push a commit to `main`.
5. Open the successful workflow run and download the `SG-debug-apk` artifact.
6. Extract the artifact ZIP to get `app-debug.apk`.

The workflow uses Java 17, Android SDK 35, and Gradle 8.9. It builds a debug APK; it is not a Play Store release-signed APK.

## Build locally

Requires JDK 17, Android SDK platform 35, build-tools 35.0.0, and Gradle 8.9:

```bash
gradle clean assembleDebug
```

The APK is created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## First launch

1. Open SG.
2. Grant Usage Access from the button in the dashboard to show screen-time totals.
3. Open Accessibility settings and enable **SG Focus Protection**.
4. Review the default blocked-app package names. Remove apps you do not want blocked.
5. Add educational channel keywords, then tap **SAVE MY RULES**.

## Privacy

SG has no network permission, analytics SDK, account system, or cloud sync. Accessibility text inspection and saved rules are processed locally. Accessibility access is highly privileged; enable it only if you understand the trade-off.

## Project structure

```text
SG/
├── .github/workflows/build.yml
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/sg/focus/
│       │   ├── MainActivity.java
│       │   └── FocusAccessibilityService.java
│       └── res/
│           ├── values/strings.xml
│           └── xml/accessibility_service_config.xml
├── build.gradle
├── gradle.properties
└── settings.gradle
```

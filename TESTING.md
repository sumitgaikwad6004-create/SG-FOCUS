# SG test checklist

Run these checks on a real Android device after installing the debug APK.

- [ ] App launches without crashing.
- [ ] Usage Access settings open and the dashboard shows a daily total after permission is granted.
- [ ] Accessibility settings open and SG Focus Protection can be enabled.
- [ ] A package entered in Block Distracting Apps returns to Home when opened.
- [ ] Instagram Reels label detection is checked on the installed Instagram version.
- [ ] YouTube Shorts label detection is checked on the installed YouTube version.
- [ ] WhatsApp Status/Updates label detection is checked on the installed WhatsApp version.
- [ ] A configured educational keyword prevents the YouTube text rule from triggering.
- [ ] Rules remain after force-stop and relaunch.
- [ ] Battery optimization / background restrictions are reviewed for the device manufacturer.

## Known test gap

Automated GitHub Actions compilation does not validate OEM-specific accessibility behavior or real app screen labels. Those require device testing.

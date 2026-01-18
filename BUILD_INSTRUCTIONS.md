# Build and Run Instructions

## Quick Start (Recommended)

### Option 1: Android Studio (Easiest)
1. Open Android Studio
2. Select **File → Open**
3. Navigate to `C:\Users\gabor\AndroidStudioProjects\WorkoutTracker`
4. Click **OK**
5. Wait for Gradle sync to complete (may take 2-5 minutes first time)
6. Click the green **Run** button or press Shift+F10
7. Select your device/emulator

### Option 2: Command Line
If you have JAVA_HOME configured:

```bash
# Windows
cd C:\Users\gabor\AndroidStudioProjects\WorkoutTracker
gradlew.bat assembleDebug

# Install to connected device
gradlew.bat installDebug
```

## Troubleshooting

### "JAVA_HOME is not set"
**Solution**: Use Android Studio which has its own JDK, or set JAVA_HOME:
```bash
# Find Android Studio's JDK (usually at):
C:\Program Files\Android\Android Studio\jbr

# Set in PowerShell:
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

### "SDK location not found"
**Solution**: Create `local.properties` with:
```
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### Gradle Sync Fails
1. Click **File → Invalidate Caches / Restart**
2. Click **Invalidate and Restart**
3. Wait for re-sync

### Build Errors After Sync
1. **Build → Clean Project**
2. **Build → Rebuild Project**

## First Build Checklist

- [x] All Kotlin files created (60+ files)
- [x] build.gradle.kts configured with all dependencies
- [x] Hilt setup with @HiltAndroidApp
- [x] AndroidManifest.xml updated
- [x] Room database with prepopulation
- [x] Navigation graph configured
- [x] Material 3 theming
- [x] Test files created

## Expected Build Time

- **First build**: 3-10 minutes (downloading dependencies)
- **Subsequent builds**: 10-30 seconds (incremental)
- **Clean build**: 1-3 minutes

## Verification Steps

After successful build:

1. **Check predefined exercises appear** on first launch
2. **Navigate to Goals** screen via edit icon
3. **Set goals** for 2-3 exercises
4. **Return to Home** and select an exercise (tap card)
5. **Use 1×/5×/10× buttons** to increment
6. **Achieve a goal** to see trophy and celebration
7. **Open drawer** (menu FAB) to access other screens
8. **Check History** after tracking some progress
9. **Test Settings** toggles

## Dependencies Overview

### Core (Required)
- Kotlin 2.0.21
- Compose BOM 2024.09.00
- Hilt 2.52
- Room 2.6.1
- Navigation 2.8.5

### Total APK Size
- Debug: ~8-12 MB
- Release (with ProGuard): ~4-6 MB

## Supported Devices

- **Min SDK**: 24 (Android 7.0 Nougat) - Released 2016
- **Target SDK**: 35 (Android 15) - Latest
- **Device coverage**: 97%+ of active Android devices

## Known Build Warnings (Safe to Ignore)

- "Unused import" warnings in test files
- "Task :app:compileDebugKotlin took X ms" (performance info)
- Kotlin compiler daemon messages

## Need Help?

1. Check Android Studio's **Build** output panel
2. Look at **Logcat** for runtime errors
3. Review README.md for feature documentation
4. Check test files for usage examples

---

**Build Status**: ✅ Ready to Build
**Last Verified**: October 31, 2025


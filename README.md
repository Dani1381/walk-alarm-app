# 🚶 WalkAlarm — Alarmy-Style Step Mission Android App

A complete, production-ready Android alarm application built in **Kotlin** where the user **must walk a target number of steps** to dismiss the alarm sound.

---

## 🛠 Features & Architecture

1. **Exact Alarm Guarantee (`AlarmManager.setAlarmClock`):**
   - Bypasses deep Doze mode, Battery Saver, and system app-killing.
   - Guaranteed execution exact to the second.

2. **Foreground Service + WakeLock (`AlarmService`):**
   - Keeps the CPU awake (`PARTIAL_WAKE_LOCK`) and screen on when ringing.
   - Plays alarm sound on `STREAM_ALARM` at maximum volume in a loop.
   - Enforces device vibration.

3. **Dual Sensor Motion Detection (`StepDetectorManager`):**
   - **Primary:** Hardware Step Detector (`Sensor.TYPE_STEP_DETECTOR`) for precise step counting.
   - **Fallback:** High-pass Accelerometer magnitude peak detection (`Sensor.TYPE_ACCELEROMETER`) for devices lacking dedicated step sensors.

4. **Lock-Screen Mission Activity (`AlarmMissionActivity`):**
   - Displays full-screen above lock screen (`setShowWhenLocked(true)` and `setTurnScreenOn(true)`).
   - Real-time animated step countdown.
   - **Prevents dismiss/exit** until the step goal (15, 30, or 50 steps) is reached.

5. **Reboot Persistence (`BootReceiver`):**
   - Automatically reschedules configured alarms on device restart (`ACTION_BOOT_COMPLETED`).

---

## 🚀 How to Build & Run

### Option 1: Android Studio (Recommended)
1. Launch **Android Studio**.
2. Click **Open** and select the folder:
   `C:\Users\ARKA\Documents\Dani-space\WalkAlarmApp`
3. Wait for Gradle Sync to complete.
4. Connect an Android phone (or launch an Emulator with API 26+).
5. Click **Run (Shift + F10)**.

### Option 2: Command Line (Gradle)
If Gradle is installed on your system PATH:
```bash
cd WalkAlarmApp
gradle assembleDebug
```
The output APK will be generated at:
`WalkAlarmApp/app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Testing the Alarm
1. Open the app on your phone.
2. Grant permissions for **Physical Activity (Step Detection)** and **Notifications**.
3. Select your step count target (e.g., **15 steps**).
4. Tap **⚡ Quick Test (Ring in 5s)**.
5. Lock your screen immediately.
6. After 5 seconds, the screen will turn on, the alarm will ring loudly, and you **must walk 15 steps** across the room for the alarm to shut off!

# Unbreakable Alarm

A modern, highly resilient Android alarm application designed to guarantee that you wake up. Instead of offering a simple dismiss or snooze button, the Unbreakable Alarm requires you to complete challenging "missions" to prove that you are fully awake before the alarm will turn off.

## 🚀 Features

- **Mission-Based Dismissal**: Choose from multiple missions that challenge your cognitive and physical awareness:
  - **Math Mission**: Solve randomly generated math problems based on difficulty level.
  - **Typing Mission**: Retype motivational phrases accurately to stop the alarm.
  - **Shake Mission**: Shake your device vigorously a specific number of times.
  - **Step Mission**: Walk a certain number of steps using device motion sensors.
  - **Color Tiles Mission**: A memory game to repeat a sequence of highlighted color tiles.
- **Progressive Difficulty**: Tailor the difficulty of missions (Easy, Medium, Hard, etc.) and specify the number of repetitions.
- **Haptic Feedback**: Meaningful, tactile responses during missions and navigation. Long and short vibration cues help guide the user interactions even when half-awake.
- **Modern UI/UX**: Clean, engaging, and minimal views built specifically to reduce morning friction while enforcing wakefulness.

## 🛠️ Architecture & Tech Stack

This project is built using modern Android development principles and follows standard architectural practices for scalability and maintainability.

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a fully declarative, responsive, and fluid user interface.
- **Local Persistence**: **Room Database** for robust, offline local storage of alarm configurations and mission preferences.
- **Architecture**: **MVVM (Model-View-ViewModel)** providing clear separation between data logic, state management, and UI rendering.
  - `AlarmViewModel` manages state propagation and data retrieval.
  - Stateflows are used for reactive rendering across screens.
- **Concurrency**: **Kotlin Coroutines** and **Flows** handle asynchronous operations like fetching items from the database.
- **Scheduling**: Android `AlarmManager` handles precise alarm scheduling, ensuring alarms trigger reliably even if the app is killed or the device restarts. Includes boot receivers to re-register alarms upon device restart.
- **Sensors**: Hardware sensor integration (`SensorManager`) for step counting and accelerometer-based shake detection.
- **Audio & Haptics**: Uses `Vibrator` API and `HapticFeedbackType` to provide rich tactile feedback and `MediaPlayer` / `RingtoneManager` for ringtone playback.
- **Background Execution**: Features a Foreground `Service` (`AlarmService`) combined with a full-screen Intent Activity (`AlarmRingingActivity`) for proper lock-screen waking and uninterrupted alarm playback.

## 🎨 Design

The visual design is structured around **Material Design 3 (M3)** principles:

- **Edge-to-Edge Compatibility**: Accommodates deeply integrated system UI and transparent navigation bars for immersive viewing.
- **Dynamic Theming**: Utilizes Compose's Material 3 color schemes combined with thoughtfully paired typography constraints.
- **Component Specifics**: 
  - Generous hit targets (minimum 48dp) across mission interactive elements.
  - Visual depth is managed seamlessly via Surface elements, elevation, and card hierarchies.
- **Accessibility & UX**: Includes descriptive labels (`contentDescription`) for essential UI interactions and relies on contrasting states (failed vs. success feedback) in memory/color tile games.

## 📦 Building the App

This project uses Gradle with Kotlin DSL plugins. 

1. **Prerequisites**: Ensure you have Android Studio appropriately configured.
2. **Build**: You can assemble the debug APK using Gradle:
   ```bash
   ./gradlew :app:assembleDebug
   ```
3. **Tests**: (If applicable) Execute standard tests:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

## 📜 License

This project is open-source. Feel free to use it, modify it, or contribute to it!

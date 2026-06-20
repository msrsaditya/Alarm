# Unbreakable Alarm

An Android alarm app for people who turn off their alarm in their sleep and don't remember doing it. Instead of a dismiss button, you have to complete a short "mission" — solve some math, retype a phrase, shake the phone, walk around, repeat a color sequence — before it'll actually stop ringing.

Built with Kotlin and Jetpack Compose.

## Missions

- **Math** — solve a randomly generated problem; difficulty controls how nasty the numbers get
- **Typing** — retype a motivational phrase exactly, no shortcuts
- **Shake** — shake the device a set number of times
- **Steps** — walk a set number of steps, tracked via the device's motion sensor
- **Color tiles** — repeat back a sequence of flashing tiles, Simon-says style

Each mission has its own difficulty and rep count, so you can tune how rough you want your mornings to be.

## Other features

- Vibration cues on key interactions, since you're probably not reading the screen closely at 6am
- Full-screen alarm activity that wakes the device over the lock screen
- Alarms survive a reboot — there's a boot receiver that re-registers everything
- Material 3 UI with dynamic color support

## Built with

- Kotlin
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the UI
- Room for storing alarms and mission settings
- Kotlin Coroutines / Flow for async work and reactive state
- `AlarmManager` for scheduling
- `SensorManager` for the shake and step missions
- `Vibrator`, `MediaPlayer`, and `RingtoneManager` for the alarm itself

Architecture is MVVM — a ViewModel exposes StateFlows that the Compose screens collect, Room handles persistence, and a foreground Service paired with a full-screen Activity handles the actual ringing so it keeps going even if the app gets killed.

## Getting started

1. Clone the repo
2. Open it in Android Studio and let Gradle sync
3. Run on a device or emulator, or build the debug APK directly:

   ```bash
   ./gradlew :app:assembleDebug
   ```

Run unit tests with:

```bash
./gradlew :app:testDebugUnitTest
```

## Roadmap / known issues

- [ ] More mission types
- [ ] Custom ringtone picker
- [ ] Configurable snooze limits
- [ ] Step mission can be unreliable on devices without a hardware step counter

## Contributing

Bug reports and PRs are welcome, especially if you've got an idea for a mission that would actually get you out of bed.

1. Fork the repo
2. Create a branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Push and open a PR

## License

Open source — use it, fork it, take it apart. No LICENSE file yet; open an issue if you need one sorted before I get to it.

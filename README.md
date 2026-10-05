# Echoes

A 45–60 second minimalist micro-game: tap the glowing orb, hear your own voice echoed back, and let the organic shapes sync with your inner rhythm.

## Build

```bash
./gradlew :app:assembleDebug
```

## Run

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Structure

```
Echoes/
├── app/build.gradle.kts
├── AndroidManifest.xml
└── java/com/example/echoes/
    ├── MainActivity.kt
    ├── AudioManager.kt
    ├── engine/GameEngine.kt
    └── ui/GameScreen.kt
```

## Rules

- Tap to drift orb + echo tone (0.4s delay)
- Organic shapes spawn opposite to drift
- 10 perfect taps → radiant celebration
- 60s auto-restart

## Tech

Kotlin, Jetpack Compose, Canvas, SoundPool sine-wave audio. Zero external deps.
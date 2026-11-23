# PopReel-Movie-Android-App

PopReel is an Android movie discovery app built with Jetpack Compose and Material 3: browse and search a catalog of ~9,900 films, swipe to like, dislike or mark films as not seen, get explained recommendations from the Genie, and play movie trivia and poster puzzles in the Cinema Arcade. The catalog is cached locally, so browsing and your choices work offline after the first download.

## Tech

Kotlin, Jetpack Compose (Material 3), Hilt, Retrofit + kotlinx.serialization, Room, DataStore, Coil 3, coroutines/Flow. Single `:app` module organised into `core/` and `feature/` packages.

## Build

Open the project in Android Studio, or run `./gradlew :app:assembleDebug`. Unit tests: `./gradlew :app:testDebugUnitTest`.

The app reads the hosted catalog API by default. To use the local backend instead:

```bash
npm --prefix backend run setup   # downloads the catalog into data/
npm --prefix backend start       # serves http://127.0.0.1:3000
./gradlew :app:assembleDebug -Ppopreel.apiBaseUrl=http://10.0.2.2:3000/
```

## Privacy

See [docs/privacy-policy.md](docs/privacy-policy.md).

# PopReel-Movie-Android-App

PopReel is an Android movie discovery app built with Jetpack Compose and Material 3: browse and search a catalog of ~9,900 films, swipe to like, dislike or mark films as not seen, get explained recommendations from the Genie, and play movie trivia and poster puzzles in the Cinema Arcade. The catalog is cached locally, so browsing and your choices work offline after the first download.

## Tech

Kotlin, Jetpack Compose (Material 3), Hilt, Retrofit + kotlinx.serialization, Room, DataStore, Coil 3, coroutines/Flow. Single `:app` module organised into `core/` and `feature/` packages.

## Build

Open the project in Android Studio, or run `./gradlew :app:assembleDebug`. Unit tests: `./gradlew :app:testDebugUnitTest`.

The app syncs its catalog from the hosted API at `https://movie-api-delta-six.vercel.app/`. To point a build at another HTTPS deployment, pass `-Ppopreel.apiBaseUrl=https://…/`.

## Privacy

See [docs/privacy-policy.md](docs/privacy-policy.md).

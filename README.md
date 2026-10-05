# AlgorithmX Android SDK

Connect your Android app to [AlgorithmX](https://algorithmx.cloud), the campaign management and customer data platform. The SDK sends customer identity and events, shows AlgorithmX push notifications, routes campaign actions to your app, and shows in-app campaigns.

- minSdk 24 or later, compileSdk 34 or later, Java 17
- Works with your existing Firebase Cloud Messaging setup; the SDK does not add Firebase

## Installation

The SDK is on Maven Central. Add it to your app module:

```kotlin
dependencies {
    implementation("cloud.algorithmx:android-sdk:1.0.1")
}
```

Its dependencies and shrinker rules come with it.

## Quick start

```kotlin
import algorithmx.engage.core.AlgorithmX

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AlgorithmX.initialize(this, "https://api.example.com", "your-partner-id")
    }
}
```

AlgorithmX gives you the API base URL and your partner ID. The SDK sends the partner ID in the `x-partner-id` header of every request.

The integration guide covers the full setup: customer identity, events, your existing Firebase messaging service, deep links, custom actions, and notification buttons.

**[Android integration guide →](https://algorithmx.cloud/en/docs/integrations/android)**

## Building from source

```bash
./gradlew assembleRelease
```

`./gradlew publishReleasePublicationToStagingRepository` writes the Maven Central bundle to `build/staging-deploy` without uploading anything.

## License

MIT. See [LICENSE](LICENSE).

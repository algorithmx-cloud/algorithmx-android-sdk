# AlgorithmX Android SDK

> **Naming recommendation:** Use camelCase for custom event names and payload keys, for example `purchaseCompleted` and `productId`. This keeps your integration consistent with SDK-defined fields and built-in events. The SDK preserves custom names and payloads exactly as supplied; it does not enforce or convert their casing.

Connect your Android app to [AlgorithmX](https://algorithmx.cloud), the campaign management and customer data platform. The SDK sends customer identity and events, shows AlgorithmX push notifications, routes campaign actions to your app, and shows in-app campaigns.

- minSdk 24 or later, compileSdk 34 or later, Java 17
- Works with your existing Firebase Cloud Messaging setup; the SDK does not add Firebase

## Installation

The SDK is on Maven Central. Add it to your app module:

```kotlin
dependencies {
    implementation("cloud.algorithmx:android-sdk:1.0.3")
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

AlgorithmX gives you the API base URL and your partner ID. The SDK sends the partner ID in the `x-partner-id` header of every SDK API request.

```kotlin
AlgorithmX.trackEvent("purchaseCompleted", mapOf(
    "productId" to "sku123",
    "orderTotal" to 49.99
))
```

SDK-defined notification fields and action values use camelCase, such as `engageAction`, `algoCampaignId`, `algoNotificationId`, `actionType`, `actionButtons`, `actionText`, `algoShowNotification`, and `openWebPage`. Built-in WebView bridge events include `campaignClose`, `campaignClick`, `campaignSubmit`, `campaignCouponCopy`, and `copyCoupon`. The SDK still accepts their legacy snake_case forms in incoming notifications and built-in WebView events; canonical fields take precedence when both forms are present.

Campaign interaction requests use `fingerprintDevice`, `campaignId`, `variationId`, `interactionType`, and `payload`, with the default event endpoint `algoViewInteract`. WebView impressions place dynamic placeholder values in a `dynamicContent` object, preserving the original custom keys inside it.

## SDK HTTP endpoints

API URLs are the initialized `apiBaseUrl` plus the paths below. Every SDK API request includes the `x-partner-id` header containing the initialized partner ID.

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/v1/identify` | Identify the customer and send optional attributes. |
| `POST` | `/api/v1/Event/Log` | Send a custom event as an array with its original name and data. |
| `POST` | `/api/v1/tracks/algoViewInteract` | Report campaign and push interactions. |
| `PUT` | `/api/v1/inAppPushEvents/device/status` | Update notification delivery or open status. |
| `POST` | `/api/v1/notificationTokens` | Register a push token for the current customer. |

`trackEvent` keeps the custom name unchanged in `eventType`. Since 1.0.3, general events use `POST /api/v1/Event/Log`, a one-event array body, ISO 8601 UTC timestamps, and `X-Anonymous-Id` containing the current SDK fingerprint. `data` contains properties or `{}`; `x-partner-id` is also required. Campaign HTML and notification images are downloaded with `GET` from their supplied URLs, so those downloads have no fixed SDK path. Campaign HTML may also load its own resources. A caller-supplied campaign interaction `endpoint` overrides the default interaction path.

The integration guide covers the full setup: customer identity, events, your existing Firebase messaging service, deep links, custom actions, and notification buttons.

**[Android integration guide →](https://algorithmx.cloud/en/docs/integrations/android)**

## Building from source

```bash
./gradlew assembleRelease
```

`./gradlew publishReleasePublicationToStagingRepository` writes the Maven Central bundle to `build/staging-deploy` without uploading anything.

## License

MIT. See [LICENSE](LICENSE).

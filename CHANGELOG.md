# Changelog

## 1.0.1

- **Breaking:** `AlgorithmX.initialize` takes the partner ID that AlgorithmX gives you: `initialize(application, apiBaseUrl, partnerId)`. The SDK sends it in the `x-partner-id` header of every request.

## 1.0.0

- First public release of the AlgorithmX Android SDK on Maven Central (`cloud.algorithmx:android-sdk`): customer identity, event tracking, FCM forwarding and token registration, notification rendering with images and buttons, deep links and custom actions, and in-app campaigns with display rules.

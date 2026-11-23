# PurplePay Simulator

An original, purple-themed Android practice app. It is not affiliated with PhonePe or any payment provider.

## Safety and behavior
- Practice-only simulation; no real payment is initiated.
- The result screen is clearly marked NOT A REAL PAYMENT.
- No fake UPI transaction ID is generated.
- QR scanning reads UPI QR text locally and pre-fills available payee name, UPI ID and amount fields. QR contents are not verified.
- Practice history is stored locally on the device.

## Build with GitHub Actions
1. Upload the contents of this folder to the root of your GitHub repository.
2. The workflow at `.github/workflows/android.yml` runs on pushes to `main` or manually from Actions.
3. Download `PurplePaySimulator-debug-apk` from the successful workflow run's Artifacts section.

## Build locally
Requires JDK 17, Android SDK 35, and Gradle 8.9:
`gradle assembleDebug`

This is a starter project and should be tested on a device before wider use.

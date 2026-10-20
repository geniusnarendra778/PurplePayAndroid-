# PurplePay Simulator (Android starter project)

A purple-themed practice payment simulator. It does not connect to banks or UPI and does not transfer money. Simulated results should not be represented as real payments. It does not generate a fake UPI transaction ID.

## Build APK in GitHub Actions
1. Create a GitHub repository named `PurplePayAndroid` and set its default branch to `main`.
2. Upload the contents of this project folder to the repository, keeping the `.github/workflows/build-apk.yml` path intact.
3. Open the repository's **Actions** tab and select **Build PurplePay Simulator APK**.
4. Tap **Run workflow**. Wait for the workflow to finish successfully.
5. Open the completed workflow run and download the `PurplePay-Simulator-debug-apk` artifact. Extract the downloaded artifact ZIP to get `app-debug.apk`.
6. Open the APK on your Android phone to install it. Android may ask permission to install apps from that source.

This is a starter project and has not been verified on a physical device. QR scanning is not implemented in this starter version.

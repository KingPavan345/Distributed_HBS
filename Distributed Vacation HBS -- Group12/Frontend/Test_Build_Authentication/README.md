# Test Build Authentication

This project is an Android application for user authentication and vacation home management. It allows users to register, log in, and view vacation homes. The app communicates with a backend API for authentication and data.

## Prerequisites
- Android Studio (latest stable version recommended)
- Android SDK (API level 30 or above)
- Java Development Kit (JDK) 8 or above
- Internet connection for API requests
- Backend server running (update API URLs in code as needed)

## Setup Instructions
1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select `Open an existing project` and choose the cloned directory.
3. **Configure API URLs:**
   - Update the API endpoint URLs in `Config.kt` or relevant files to match your backend server address.
4. **Sync Gradle:**
   - Android Studio will prompt you to sync Gradle. Click `Sync Now` to download dependencies.
5. **Run the App:**
   - Connect an Android device or start an emulator.
   - Click the green 'Run' button or use `Shift+F10`.

## Usage
- **Register:** Enter your username, email, and password to create a new account.
- **Login:** Use your credentials to log in and access the app features.
- **Vacation Homes:** Browse, view details, and filter vacation homes.

## Troubleshooting
- **API Connection Issues:**
  - Ensure your backend server is running and accessible from your device/emulator.
  - For emulators, use `10.0.2.2` to refer to `localhost` on your development machine.
- **Gradle Sync Errors:**
  - Check your internet connection and proxy settings.
  - Try `File > Invalidate Caches / Restart` in Android Studio.
- **App Crashes:**
  - Check Logcat for error messages.
  - Ensure all required permissions are granted.

# Convertex

Convertex is a precise, real-time currency conversion utility built for Android. It specifically handles high-accuracy conversions between the United States Dollar (USD) and the Indian Rupee (INR), featuring a native-feel interface and localized numbering systems.

## Features

* **Real-time Rates:** Automatically fetches the latest USD to INR exchange rates daily using a REST API.
* **Bi-directional Conversion:** Toggle between USD-to-INR and INR-to-USD instantly with a single tap.
* **Intelligent Localized Formatting:** * Uses the American numbering system (Millions/Billions) for USD.
    * Uses the Indian numbering system (Lakhs/Crores) for INR.
* **Adaptive UI:** The interface dynamically scales font sizes and provides horizontal scrolling for extremely large figures to prevent layout overflow.
* **Offline Support:** Saves the last fetched rate locally to ensure the app remains functional even without an internet connection.
* **Custom Keypad:** Includes a built-in numeric keypad designed for fast, one-handed input without relying on the system keyboard.

## Technical Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose
* **Asynchronous Handling:** Kotlin Coroutines
* **Networking:** Standard Java URL/HTTP libraries with JSON parsing
* **Local Storage:** Android SharedPreferences

## Installation

1. Clone the repository:
   git clone https://github.com/vihaangautam/Convertex.git
2. Open the project in Android Studio.
3. Allow Gradle to sync and download necessary dependencies.
4. Build the project and run it on an emulator or a physical Android device.

## Usage

* Launch the app.
* Enter the amount using the custom keypad at the bottom.
* Tap the center swap icon to switch between USD and INR conversion modes.
* The exchange rate used for the calculation is displayed in the pill below the result area.

## License

This project is open-source and available under the MIT License.

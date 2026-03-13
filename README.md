# Variant - Morse Code Converter (Android Edition)

A native Android implementation of the **Variant Morse Code Converter**. This project brings the exact feature set and logic of the original Python desktop application to the Android platform using **Kotlin** and **Jetpack Compose**.

---

## 📱 About this Version

This app is a direct port of the [original Python Morse Converter](https://github.com/dejansekesan/Morse-Code-Translator). It maintains the exact same translation logic, conversion rules, and functional philosophy, but has been rebuilt from the ground up for a native mobile experience.

### Key Differences in the Android Edition

* **Native UI:** Built with **Jetpack Compose** for a modern, fluid Material Design interface.
* **Optimized Performance:** Uses Kotlin Coroutines instead of standard threads for smooth, non-blocking audio playback.
* **Platform Integration:** Replaces `pygame` with the native Android `SoundPool` API for efficient, low-latency audio.
* **Portability:** Compiled into an Android APK, allowing you to use your Morse tools anywhere.

---

## ✨ Features

* **Bidirectional Conversion:** Real-time translation between Text and Morse.
* **Interactive Playback:** Full control over Morse code audio playback.
* **Customizable Audio:** Adjustable speed, inter-character gap, and volume sliders.
* **Serbian Latin Support:** Maintains the normalization of extended Latin characters (`š`, `đ`, `č`, `ć`, `ž`).
* **Modern Android Interface:** Responsive design that adapts to various screen sizes.

---

## 🚀 Getting Started

### Prerequisites

* [Android Studio](https://developer.android.com/studio) (latest version recommended).
* Android SDK (Minimum API level 24).
* A physical Android device or an Emulator configured with Android 7.0 (Nougat) or higher.

### Building from Source

1. **Clone the Repository:**
```bash
git clone https://github.com/dejansekesan/Morse-Code-Translator-Android.git

```


2. **Open in Android Studio:** Open the project root folder.
3. **Sync Gradle:** Allow Android Studio to download the necessary dependencies.
4. **Run:** Select your device/emulator and click the green **Run** button.

---

## 📖 How to Use

### Main Interface

1. **Select Mode:** Choose between **Text to Morse** or **Morse to Text** using the selection controls.
2. **Input:** Type your content into the Input field.
3. **Convert:** Press the **Convert** button to display the results.

### Audio Playback

* **Play:** Initiates playback.
* **Stop:** Immediately halts playback and resets the position.
* **Sliders:** * **Speed:** Controls the duration of individual signals.
* **Gap:** Adjusts the silence between characters to suit your preference.



---

## 📝 Consistency with Desktop Version

This application strictly follows the syntax rules of the original Variant project:

> **Text to Morse:**
> * Spaces between words become `/`.
> * Serbian special characters are normalized (`č` -> `c`, etc.).
> 
> 
> **Morse to Text:**
> * Morse characters must be separated by a single space.
> * `/` represents a space between words.
> 
> 

---

## 🛠️ Built With

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Audio Engine:** Android `SoundPool`
* **Threading:** Kotlin Coroutines

---

## 📷 Screenshots

![Morse Code Sample](/app/src/main/morse-app.jpeg "Morse Code Sample")

---

*Based on the original [Variant - Morse Code Converter](https://github.com/dejansekesan/Morse-Code-Translator) project.*

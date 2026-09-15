# ChatGPT Audio Booster

Android app for testing and improving the audible volume of ChatGPT Voice on-device, without an API key or backend.

## Goal

Keep ChatGPT Voice in its normal Phone/earpiece mode while providing the loudest practical playback path available to the Android device.

## Important Android limitation

A normal Android app cannot universally capture and re-route another app's private audio output. This project therefore starts with safe, on-device audio controls and device-compatible playback/volume behavior rather than assuming unrestricted access to ChatGPT's audio stream.

Target device for initial testing: Samsung Galaxy A05s, Android 14.

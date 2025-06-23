# 🦵 Leg Curve Detection App

**Leg Curve Detection** is a native Android application built using **Kotlin** that uses AI-based image analysis to detect leg deformities from photos. The app determines whether a leg is **Normal**, **Bow Leg**, or **Knock Knee**, and helps users better understand their leg alignment.

## 🚀 Features

- 📷 **Upload or Capture Leg Photos**  
  Capture a new photo using the camera or upload from your gallery for analysis.

- 🧠 **AI-Powered Detection**  
  The app sends the leg image to a deep learning model hosted via an API and returns one of three classifications:
  - **Normal Leg**
  - **Bow Leg**
  - **Knock Knee**

- 🕓 **Detection History**  
  Stores past detection results locally, including:
  - Name of user
  - Date and time
  - Location
  - Detection result
  - The leg photo

- 📍 **Location Tracking**  
  Records the user's location during image detection (optional and based on permissions).

- ✨ **Modern UI/UX**  
  Clean and user-friendly interface with smooth navigation and feedback.

## 📱 Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM
- **Image Analysis:** Deep learning model via **FastAPI backend**
- **Storage:** SharedPreferences + Gson for local history
- **UI Components:** RecyclerView, Fragments, Navigation Component
- **Camera & Gallery Access**
- **Location Services**

## 🛠️ How It Works

1. User captures or selects an image of their legs.
2. The app sends the image to a prediction API.
3. The model analyzes the image and returns a label: `Normal`, `Bow Leg`, or `Knock Knee`.
4. The result, photo, and metadata are stored and displayed in the **History** section.

## 📷 Screenshots

> *(Add screenshots here to visualize the app features)*

## 🧪 AI Model

The image classification model was trained to detect leg curvature abnormalities. It's deployed via a **FastAPI** endpoint and integrated into the app using Retrofit.

## 🔒 Privacy & Permissions

- The app only accesses the camera, gallery, and location **with user permission**.
- All data stays on the device — **no cloud storage or personal tracking**.



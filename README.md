# MineSpray 🚛💧

### Smart Haul Road Dust Control

MineSpray is an Android application prototype for intelligent haul-road dust and water management in mining operations.

The system is designed around a simple idea:

**Environmental Data → Intelligent Decision → Watering Command**

Instead of applying the same amount of water across all mine roads, MineSpray aims to determine **when, where, and how much water should be sprayed** based on road and environmental conditions.

---

## 🎯 Project Goal

Water trucks are commonly used in mining operations to reduce dust by spraying water on haul roads.

A simple watering system may spray water without considering the actual condition of each road section. This can result in:

- Some areas remaining dry and dusty
- Some areas becoming excessively wet
- Unnecessary water consumption
- Inefficient water-truck operations

MineSpray aims to make this process more intelligent and targeted.

---

## 🧠 Smart Decision Concept

The system can consider multiple environmental and operational parameters, including:

- 🌡️ Temperature
- 💧 Road surface moisture
- 💨 Air humidity
- 🌫️ Dust level
- 🌬️ Wind speed
- 🚛 Traffic level
- 📍 Water-truck location
- ⏱️ Time since the last watering operation

Based on these inputs, the system can determine an appropriate watering action.

For example:

```text
Dry + High Dust
       ↓
   Water Now
       ↓
Higher Intensity
```

```text
Partially Wet
       ↓
   Water Soon
       ↓
Lower Intensity
```

```text
Sufficient Moisture
       ↓
   No Watering
```

The initial decision engine can use rule-based logic, while the architecture can later be extended with machine-learning models.

---

## 📱 Current Android Application

The current version focuses on the Android frontend and provides a management dashboard for the MineSpray system.

### Main Screens

- **Overview**
    - Current road condition
    - Dust risk
    - Average moisture
    - Critical zones
    - Water consumption
    - Active watering operations

- **Map**
    - Mine road visualization
    - Road zones
    - Water-truck locations
    - Critical and warning areas

- **Zones**
    - Road-zone monitoring
    - Dust level
    - Moisture
    - Temperature
    - Traffic
    - Sensor status
    - Recommended action

- **Fleet**
    - Water trucks
    - Truck status
    - Tank level
    - Speed
    - Water flow
    - Current zone

- **Smart Control**
    - Automatic mode
    - Manual override
    - Risk analysis
    - Watering recommendation
    - Spray intensity

- **Settings**
    - Language
    - Smart-control options
    - Notifications
    - Data settings
    - API configuration
    - Demo mode

---

## 🛠️ Technology Stack

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Android Navigation Compose
- AndroidX Lifecycle
- Compose BOM

### Current Build Configuration

```text
Android Gradle Plugin: 8.5.2
Kotlin: 1.9.24
Compose Compiler: 1.5.14
Compile SDK: 35
Target SDK: 35
Minimum SDK: 26
Java: 17
```

---

## 🏗️ Architecture

The project is currently focused on the Android frontend.

The planned architecture is:

```text
┌─────────────────────────────┐
│        Android App          │
│       Kotlin + Compose      │
└──────────────┬──────────────┘
               │
               │ API
               ▼
┌─────────────────────────────┐
│       Backend Service       │
│       FastAPI / Python      │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ Smart Decision Engine       │
│ Rules → ML / Optimization   │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ Mine / Sensors / Fleet      │
└─────────────────────────────┘
```

The Android frontend currently uses demo data so that the user interface and system workflow can be developed independently from the backend.

---

## 🚧 Project Status

**Current status: Android Frontend / UI Prototype**

The current version focuses on:

- Dashboard design
- Mine-road monitoring
- Zone management
- Water-truck fleet management
- Smart watering interface
- Manual and automatic control concepts
- Persian / English localization
- Demo data

Backend integration and real sensor/API communication are planned for future development.

---

## 🗺️ Roadmap

### Phase 1 — Android Frontend
- [x] Dashboard
- [x] Zone management
- [x] Fleet management
- [x] Smart control interface
- [x] Mine map interface
- [x] Settings
- [x] Demo data
- [x] English localization
- [x] Persian localization

### Phase 2 — Backend
- [ ] FastAPI backend
- [ ] REST API
- [ ] Database
- [ ] Authentication
- [ ] Real-time data

### Phase 3 — Smart System
- [ ] Sensor integration
- [ ] Real road-condition data
- [ ] Automated watering decisions
- [ ] Water-consumption optimization
- [ ] Fleet dispatch optimization

### Phase 4 — Machine Learning
- [ ] Dust prediction
- [ ] Road-moisture prediction
- [ ] Water-demand prediction
- [ ] Intelligent watering optimization

---

## 🌐 Localization

MineSpray supports both:

- 🇬🇧 English
- 🇮🇷 Persian

The application uses Android resource localization so the interface can be adapted for different languages.

---

## 🚀 Getting Started

### Requirements

- Android Studio
- JDK 17
- Android SDK 35
- Android device or emulator
- Git

### Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/MineSpray.git
cd MineSpray
```

Open the project in Android Studio and allow Gradle to synchronize.

Then select an Android emulator or physical device and run the `app` configuration.

---

## 📌 Demo

The current frontend operates with demo data and does not require a live mine sensor system.

This allows the UI and decision-making workflow to be demonstrated before connecting the application to real backend services.

---

## ⚠️ Disclaimer

MineSpray is currently a prototype and research/development project.

It is not intended to directly control real mining equipment or replace professional industrial safety and automation systems.

---

## 👨‍💻 Project

**MineSpray — Smart Haul Road Dust Control**

An experimental Android-based platform for intelligent mine-road dust management and water-truck operation.

---

## 📄 License

License information will be added in a future release.
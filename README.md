# Offline Doctor & Institution Tracker

A completely offline, mobile-centered Android native application to record and manage doctor and institution details. Supports multi-institution assignments for doctors, ward and department management, OPD and OT schedules, advanced filtering, and export/import capabilities.

## Features

- **Completely Offline**: No internet connection required at any point
- **Doctor Management**: Add, edit, and manage doctor information with qualifications
- **Institution Management**: Manage healthcare institutions with ward details
- **Multi-Institution Assignments**: Assign doctors to multiple institutions and wards
- **Advanced Search & Filtering**: Filter by speciality, location, schedule, and more
- **Export/Import**: Backup and restore data in JSON and CSV formats
- **Material3 Design**: Modern Android UI with light/dark theme support

## Technical Specifications

- **Platform**: Android Native (Minimum SDK 24)
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: SQLite with Room ORM
- **UI Framework**: Jetpack Compose with Material3
- **Dependency Injection**: Hilt
- **Performance**: Optimized for up to 50,000 records

## Project Structure

```
app/src/main/java/com/healthtracker/offline/
├── data/
│   ├── entities/        # Room database entities
│   ├── dao/            # Data Access Objects
│   ├── database/       # Room database configuration
│   └── repository/     # Repository pattern implementation
├── ui/
│   ├── screens/        # Jetpack Compose screens
│   ├── viewmodels/     # ViewModels for business logic
│   └── theme/          # Material3 theme configuration
└── utils/              # Utility classes and helpers
```

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Build and run the application

## Requirements

- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- Kotlin 1.9.10 or later

## License

This project is developed as part of a healthcare management solution.
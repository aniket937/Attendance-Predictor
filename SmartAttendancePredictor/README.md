# Smart Attendance Predictor

A complete Android application for tracking and predicting attendance.

## Features

- **Add Subjects**: Add subjects with name, total classes, and classes attended
- **View Attendance**: Display attendance percentage with color-coded status
- **Prediction Engine**:
  - Attend next class prediction
  - Skip next class prediction
  - Skip multiple classes prediction
  - Maximum safe bunk calculation
- **Notifications**: Alert when attendance drops below 75%
- **Study Timer**: 25-minute countdown timer
- **Data Export**: Export attendance data to CSV
- **Clean Architecture**: Modular code with proper separation

## Architecture

```
java/com/example/attendance/
├── activities/         # UI Activities
├── adapter/           # RecyclerView Adapter
├── model/             # Data Models
├── database/          # SQLite Helper
├── provider/          # Content Provider
└── utils/             # Utility Classes
```

## Requirements

- Android Studio (Bumblebee or later)
- JDK 8 or higher
- Android SDK 34

## How to Run

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to `SmartAttendancePredictor/`
4. Wait for Gradle sync to complete
5. Connect your Android device
6. Click Run (Shift + F10)

## Usage

1. **Add Subject**: Tap the + FAB button
2. **View Details**: Tap on any subject card
3. **Predictions**: Use buttons to simulate attending/skipping
4. **Delete Subject**: Long press on subject card

## Color Coding

- **Green**: Safe Zone (>80% attendance)
- **Yellow**: Be Careful (75-80% attendance)
- **Red**: Low Attendance (<75% attendance)

## Database

SQLite database is created automatically on first run.
Table: `subjects(id, name, total, attended)`

## Dependencies

- AndroidX AppCompat
- Material Components
- RecyclerView
- CardView
- CoordinatorLayout
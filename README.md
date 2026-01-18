# Daily Exercise Counter - Android App

A comprehensive daily exercise tracking application built with Kotlin and Jetpack Compose following clean architecture principles.

## 📱 Features

### Core Functionality
- **Daily Exercise Tracking**: Track your daily workout progress with intuitive increment/decrement controls (1×, 5×, 10×)
- **Goal Setting**: Set daily repetition goals for each exercise
- **Achievement Celebration**: Get congratulated with trophy icons and celebration dialogs when goals are achieved
- **Exercise Management**: Add, edit, and archive custom exercises
- **History**: View past workout summaries with detailed statistics
- **Settings**: Configure sound and haptic feedback preferences

### User Experience
- **Selection-Based Controls**: Select an exercise, then use global increment/decrement buttons
- **Quick Actions**: Each exercise card has quick +1/-1 buttons for convenience
- **Progress Visualization**: Linear progress bars show completion percentage
- **Haptic Feedback**: Tactile feedback on button taps and goal achievements
- **Material 3 Design**: Modern UI with light/dark theme support
- **Accessibility**: Full content descriptions and keyboard navigation support

### Predefined Exercises
On first launch, the app includes:
- Pull-up
- Push-up
- Sit-up
- Squat
- Kettlebell Swing

## 🏗️ Architecture

The app follows **Clean Architecture** with MVVM pattern:

```
app/
├── core/domain/          # Business logic (models, use cases, repository interfaces)
├── data/                 # Data layer (Room database, repositories, DataStore)
├── di/                   # Dependency injection (Hilt modules)
├── presentation/         # UI layer (ViewModels, Composables)
│   ├── home/            # Today's workout screen
│   ├── goals/           # Goal setting screen
│   ├── exercises/       # Exercise management screen
│   ├── history/         # History and statistics
│   ├── settings/        # App settings
│   └── navigation/      # Navigation graph
└── ui/theme/            # Material 3 theming
```

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

### Architecture Components
- **Dependency Injection**: Hilt/Dagger
- **Database**: Room (with Flow support)
- **Preferences**: DataStore (Preferences)
- **Navigation**: Navigation Compose
- **Async**: Kotlin Coroutines + StateFlow/SharedFlow
- **Date/Time**: Java Time API (with desugaring)

### Testing
- **Unit Tests**: JUnit 4, MockK
- **Flow Testing**: Turbine
- **Coroutines Testing**: kotlinx-coroutines-test
- **UI Tests**: Compose UI Testing

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer (2024.2.1+)
- JDK 11 or higher
- Android SDK with API 35

### Build Instructions

1. **Clone or open the project** in Android Studio

2. **Sync Gradle dependencies**:
   - Open the project in Android Studio
   - Click "Sync Project with Gradle Files"
   - Wait for dependencies to download

3. **Build the project**:
   ```
   ./gradlew build
   ```
   Or on Windows:
   ```
   gradlew.bat build
   ```

4. **Run on device/emulator**:
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio
   - Or use command line:
     ```
     ./gradlew installDebug
     ```

### Running Tests

**Unit Tests**:
```bash
./gradlew test
```

**Android Instrumented Tests**:
```bash
./gradlew connectedAndroidTest
```

## 📖 User Guide

### Getting Started
1. **First Launch**: The app pre-populates with 5 common exercises
2. **Set Goals**: Tap the edit icon (✏️) on the home screen to set your daily goals
3. **Track Progress**: 
   - Select an exercise by tapping its card
   - Use the increment/decrement buttons at the bottom (1×, 5×, 10×)
   - Or use quick +/- buttons on each card
4. **Celebrate**: When you reach a goal, you'll see a trophy 🏆 and celebration message!

### Screen Overview

#### Home (Today)
- View all active exercises with current progress
- Select exercises and adjust reps with 1×/5×/10× controls
- See progress bars and goal completion status
- Quick access to set goals

#### Goals
- Set target repetitions for each exercise
- Use +5/-5 buttons or type values directly
- Goals are saved for today

#### Exercises
- View all active exercises
- Add new custom exercises
- Edit existing exercise names
- Archive exercises (preserves history)

#### History
- Browse past workout dates
- Tap a date to see detailed summary
- View per-exercise completion and total reps

#### Settings
- Toggle sound effects
- Toggle haptic feedback
- View app information

## 🎯 Key Features Detail

### Selection Pattern
The home screen uses a **selection-based control pattern**:
1. Tap an exercise card to select it (highlighted with blue border)
2. Use the control panel at the bottom to adjust reps
3. If no exercise is selected, controls show "Select an exercise first"

### Goal Achievement
- **Individual Goals**: Trophy icon appears on card + celebration dialog
- **All Goals Complete**: Special celebration when all daily goals are achieved
- **Haptic Feedback**: Strong vibration on goal completion

### Data Persistence
- **Room Database**: Exercises, goals, and progress stored locally
- **DataStore**: User preferences (sound, haptics)
- **Date-Based**: Progress tracked by LocalDate using device timezone
- **History**: All past data preserved, even for archived exercises

## 🧪 Testing

The project includes comprehensive tests:

### Unit Tests
- **Use Cases**: Business logic validation (increment/decrement, goal achievement)
- **ViewModels**: State management and event emission
- **Repository**: Data layer operations

### Test Files
- `IncrementProgressUseCaseTest.kt`: Tests increment logic and boundary conditions
- `AddExerciseUseCaseTest.kt`: Tests exercise validation and uniqueness
- `HomeViewModelTest.kt`: Tests UI state and celebration events

### Running Specific Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.trainig.workouttracker.core.domain.usecase.IncrementProgressUseCaseTest"
```

## 📐 Design Decisions

### Why Selection Pattern?
Instead of putting increment buttons on every row, we use a selection model:
- **Cleaner UI**: Less clutter, especially with many exercises
- **Larger Touch Targets**: Bottom controls are bigger and easier to tap
- **Consistent UX**: Similar to many productivity apps
- **Quick Actions Available**: Each card still has +1/-1 for convenience

### Why Room + DataStore?
- **Room**: Structured relational data, type-safe queries, Flow support
- **DataStore**: Simple key-value preferences, type-safe, coroutine-first

### Why Clean Architecture?
- **Testability**: Business logic isolated from Android framework
- **Maintainability**: Clear separation of concerns
- **Scalability**: Easy to add features without breaking existing code

## 🔧 Configuration

### Changing Min SDK
Edit `app/build.gradle.kts`:
```kotlin
minSdk = 24  // Change this value
```

### Database Schema
Schema exported to `app/schemas/` for version control and migrations.

### Desugaring
The app uses Java Time API on older devices via core library desugaring. This is configured in `build.gradle.kts`.

## 📊 Database Schema

### Tables

**exercises**
- `id` (PK, auto-increment)
- `name` (unique, indexed)
- `isArchived` (boolean)
- `createdAt` (timestamp)

**daily_goals**
- `id` (PK, auto-increment)
- `date` (ISO-8601 string, indexed)
- `exerciseId` (FK to exercises)
- `targetReps` (integer)
- Unique constraint: (date, exerciseId)

**daily_progress**
- `id` (PK, auto-increment)
- `date` (ISO-8601 string, indexed)
- `exerciseId` (FK to exercises)
- `completedReps` (integer)
- Unique constraint: (date, exerciseId)

## 🎨 Customization

### Adding New Exercises to Defaults
Edit `DatabaseBuilder.kt`:
```kotlin
val defaultExercises = listOf(
    ExerciseEntity(name = "Pull-up"),
    // Add more here
)
```

### Changing Increment Values
Edit `HomeScreen.kt` in the `ControlPanel` composable to change 1×/5×/10× values.

## 🐛 Known Limitations

- No cloud sync (local-only storage)
- No export/import feature yet (infrastructure ready in settings)
- No workout templates or routines
- No rest timer
- No exercise instructions or videos

## 🔮 Future Enhancements

- [ ] Export/import workout history (JSON/CSV)
- [ ] Workout templates and routines
- [ ] Rest timer between sets
- [ ] Charts and statistics
- [ ] Cloud backup and sync
- [ ] Widget for quick tracking
- [ ] Notifications for daily reminders
- [ ] Exercise categories and filtering

## 📄 License

This project is a demonstration app built for educational purposes.

## 👤 Author

Built with ❤️ using Kotlin and Jetpack Compose

## 🙏 Acknowledgments

- Material 3 Design Guidelines
- Android Jetpack Documentation
- Clean Architecture by Robert C. Martin

---

**Version**: 1.0  
**Last Updated**: October 31, 2025


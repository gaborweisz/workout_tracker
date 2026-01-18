# App Features Walkthrough

## 🎯 User Journey

### First Time User Experience

#### 1. Launch App
- App opens to Home screen ("Today's Workout")
- Database automatically creates 5 predefined exercises:
  - Pull-up
  - Push-up
  - Sit-up
  - Squat
  - Kettlebell Swing
- Empty state message: "No exercises yet. Tap the edit icon to set goals!"

#### 2. Set Daily Goals
- Tap **Edit icon (✏️)** in top bar
- Navigate to "Set Daily Goals" screen
- See list of all exercises with input fields
- For each exercise:
  - Use **+5/-5 buttons** for quick adjustment
  - Or **type directly** in the input field
  - Example: Set Push-ups to 50, Sit-ups to 30
- Tap **Save** button
- Return to Home screen automatically

#### 3. Track Your Workout
- See your exercises with progress (0 / 50 for Push-ups)
- **Select an exercise** by tapping its card
  - Card gets blue border
  - Card background changes to primaryContainer color
- Use **Control Panel** at bottom:
  - **Decrease section**: 1×, 5×, 10× buttons with down arrows
  - **Increase section**: 1×, 5×, 10× buttons with up arrows
- Each button press:
  - Updates the counter
  - Provides haptic feedback (vibration)
  - Progress bar fills up

#### 4. Quick Actions
- Each card has **+1 and -1 buttons** on the right
- Tap for single increments without selecting
- Useful for quick adjustments

#### 5. Achieve a Goal! 🏆
- When completed reps ≥ target reps:
  - **Trophy icon (🏆)** appears on the card
  - **Celebration dialog** pops up: "🏆 Goal Achieved! Congratulations!"
  - **Strong haptic feedback**
  - Progress text turns primary color
  - Progress bar fills to 100%

#### 6. Complete All Goals 🎉
- When ALL exercises with goals are complete:
  - **Special celebration**: "🎉 All Goals Complete! Amazing work!"
  - Sense of accomplishment!

### Daily Usage Pattern

#### Morning Routine
1. Open app
2. Set today's goals (or keep yesterday's if using auto-carry forward)
3. Start workout

#### During Workout
1. Complete a set of push-ups (say 10 reps)
2. Select Push-ups card
3. Tap **10× Increase** button
4. Progress updates instantly
5. Continue with other exercises

#### After Workout
1. Open navigation drawer (tap Menu FAB)
2. Go to **History**
3. See today's summary
4. View total reps and goals achieved

### Exercise Management

#### Add Custom Exercise
1. Open drawer → **Exercises**
2. Tap **+ FAB** (Floating Action Button)
3. Enter exercise name (e.g., "Plank")
4. Tap **Save**
5. Exercise appears in list and on Home screen

#### Edit Exercise Name
1. Go to Exercises screen
2. Find exercise in list
3. Tap **Edit icon** (✏️)
4. Modify name
5. Tap **Save**

#### Archive Exercise
1. Go to Exercises screen
2. Tap **Archive icon** (📦)
3. Confirm in dialog
4. Exercise hidden from active list
5. History preserved

### View History

#### Browse Past Workouts
1. Open drawer → **History**
2. See list of dates with activity
3. Most recent at top

#### View Day Details
1. Tap a date card
2. Dialog shows:
   - Total reps for that day
   - Goals achieved / total goals
   - Per-exercise breakdown
   - Trophy icons for achieved goals

### Settings & Preferences

#### Configure Feedback
1. Open drawer → **Settings**
2. Toggle **Sound Effects**
   - Plays sounds on goal completion
3. Toggle **Haptic Feedback**
   - Vibration on button taps and achievements
4. Changes save automatically

## 🎨 UI Elements Guide

### Home Screen Components

**Top Bar**
- Title: "Today's Workout"
- Edit icon: Quick access to Goals screen

**Exercise Cards**
- Exercise name (title)
- Current progress: "25 / 50" or "10 (No goal set)"
- Trophy icon when goal achieved
- Quick +1/-1 buttons
- Progress bar (if goal is set)
- Blue border when selected

**Control Panel (Bottom)**
- Gray background (surfaceVariant)
- Instructions: "Select an exercise first" (when none selected)
- Two columns:
  - **Decrease**: 1×, 5×, 10× with down arrows
  - **Increase**: 1×, 5×, 10× with up arrows
- Buttons disabled when no selection

**Menu FAB**
- Floating Action Button with menu icon
- Opens navigation drawer

### Navigation Drawer

**Header**
- App name: "Workout Tracker"
- Divider

**Menu Items**
- Today (Home icon)
- Goals (Edit icon)
- Exercises (Fitness icon)
- History (Calendar icon)
- Settings (Gear icon)

**Behavior**
- Selected item highlighted
- Tapping item navigates and closes drawer
- Swipe from left edge to open

## 🔄 Data Flow Examples

### Example 1: Setting and Achieving a Goal

```
1. User opens Goals screen
2. Sets Push-ups to 50
3. GoalsViewModel.updateGoal(pushupId, 50)
4. Saves to database
5. User returns to Home
6. HomeViewModel observes getTodayRecordsUseCase()
7. Shows Push-ups: 0 / 50

... user works out ...

8. User selects Push-ups card
9. Taps 10× Increase button 5 times
10. HomeViewModel.incrementProgress(10) called 5 times
11. IncrementProgressUseCase updates database
12. Flow emits new progress: 50 / 50
13. UI updates with trophy icon
14. Celebration event emitted
15. CelebrationDialog appears
16. Haptic feedback triggered
```

### Example 2: Viewing History

```
1. User opens History screen
2. HistoryViewModel.loadHistoryDates()
3. GetHistoryDatesUseCase queries database
4. Returns list of dates with progress
5. UI shows date cards

... user taps October 30 ...

6. HistoryViewModel.loadSummaryForDate(Oct 30)
7. GetDailySummaryUseCase fetches:
   - All exercises
   - Goals for that date
   - Progress for that date
8. Combines into DailySummary
9. Dialog shows:
   - Total reps: 120
   - Goals: 3 / 4 achieved
   - Per-exercise details with trophies
```

## 💡 Tips & Tricks

1. **Double-tap cards** to quickly select and increment by 1
2. **Use 10× buttons** for high-rep exercises like jump rope
3. **Set goals to 0** to track without targeting
4. **Archive rarely-used exercises** to keep list clean
5. **Check history weekly** to track progress over time

## 🎭 Accessibility Features

- **Content descriptions** on all interactive elements
- **Semantic labels** for screen readers
- **Large touch targets** (48dp minimum)
- **High contrast** text and borders
- **Progress communicated** verbally: "Push-ups, 25 of 50 completed"

## 🐛 Edge Cases Handled

- **Negative counts**: Prevented (can't go below 0)
- **No selection increment**: Shows message "Select an exercise first"
- **Empty exercises**: Shows guidance to add exercises
- **No goals set**: Shows "No goal set" with counting still enabled
- **Duplicate names**: Validation prevents (case-insensitive)
- **Date rollover**: Automatic at midnight
- **Rapid tapping**: Atomic database transactions prevent race conditions

---

**Ready to track your fitness journey!** 💪


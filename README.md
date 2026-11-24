# StudyFlow AI

StudyFlow AI is an Android app for planning study work, tracking focus, and getting AI-assisted productivity guidance. It is built with Kotlin.

## Features

- **Dashboard:** View daily progress, completed and overdue tasks, remaining work, and a calendar for selecting a day.
- **Task management:** Create and edit tasks with a description, deadline, estimated duration, and priority. Mark tasks complete, track time, or undo a deletion.
- **Smart planning:** Review tasks for a selected day, see a daily planning health score, and request a suggested plan based on your energy level.
- **AI study coach:** Chat about study plans and productivity. The coach receives your unfinished task titles as context, and can turn a concrete plan into tasks in the app.
- **Focus timer:** Run a 25-minute Pomodoro session, with controls to pause, resume, or reset. Completed sessions earn XP.
- **Schedule view:** See unfinished tasks on a 24-hour Gantt-style timeline.
- **Analytics and achievements:** View task priority and progress charts, focus statistics, XP, levels, streaks, and achievement badges.
- **Task reminders:** Schedule notifications ahead of task deadlines. The app also includes a short-delay demo reminder.
- **Sign-in and preferences:** Continue with Google or use guest access. The welcome screen includes theme and language controls.

## Technology

- Kotlin and AndroidX
- Room for local task, statistics, achievement, and chat storage
- Firebase Authentication and Google Sign-In
- Groq API for AI chat and planning
- WorkManager for reminders and overdue-task rescheduling
- Retrofit, Kotlin coroutines, and MPAndroidChart

## Requirements

- Android Studio with Android SDK 34
- JDK 17
- Android 8.0 (API 26) or later

## Getting started

1. Clone the repository:

   ```bash
   git clone https://github.com/ayasadoq/Application-intelligente-de-gestion-et-d-optimisation-du-temps-pour-tudiants.git

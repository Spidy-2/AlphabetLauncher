# Alphabet Launcher — Android Developer Assignment

A minimal Android launcher-style home screen inspired by the supplied reference.

## Core features
- Live clock and date
- Favourite app list
- Real launchable-app discovery through PackageManager
- Android 11+ package visibility via `<queries>`
- Vertical A–Z interaction bar
- Finger-following curve using distance-based exponential falloff
- Selected-letter bubble
- Real-time app filtering and alphabetical sorting
- Empty-state for letters without apps
- Cached app list; PackageManager is not queried during dragging
- App launching through PackageManager

## Tech
- Kotlin
- Jetpack Compose
- AndroidX Activity
- Android SDK PackageManager

## Libraries
- `androidx.activity:activity-compose:1.9.2` — Compose Activity integration
- `androidx.compose:compose-bom:2024.09.00` — Compose dependency alignment
- `androidx.compose.ui` — Compose UI and drawing
- `androidx.compose.foundation` — gestures and lazy lists
- `androidx.compose.material3` — text/material components

The core curve animation is custom code rather than an animation library. Each letter's horizontal offset is calculated from its distance to the finger using an exponential falloff, producing a smooth bulge.

## Build
Open in Android Studio, allow Gradle sync, then run on a real Android phone.

## AI/tool disclosure
AI assistance was used during development for debugging, code scaffolding, and implementation guidance. The final code should be reviewed and understood by the submitter before the interview.

## Note
The assignment's optional HOME/default-launcher registration was not included in this core implementation.

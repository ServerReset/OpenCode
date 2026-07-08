# OpenCode Android

Material 3 Expressive phone app for the [OpenCode](https://opencode.ai) AI coding agent.

Built with Jetpack Compose and Material 3 Expressive.

## Features

- **AI Chat** — Streaming responses with code blocks, 10 models across 7 providers
- **Multi-Session** — Run multiple parallel sessions with different models
- **File Explorer** — Browse project files with expandable tree view
- **Code Editor** — View files with line numbers and syntax awareness
- **Terminal** — Interactive command execution with history
- **Model Selection** — Switch between 10 models (Claude, GPT, Gemini, DeepSeek, Llama, Codestral, Copilot)
- **Settings** — Dark/Light theme, dynamic color, model config, session management

## Tech Stack

- Kotlin 2.2.20
- Jetpack Compose 1.12.0-alpha03
- Material 3 Expressive (1.5.0-alpha21)
- AGP 9.1.0
- compileSdk 37 / minSdk 26

## Download

Grab the latest APK from the [Actions tab](https://github.com/ServerReset/OpenCode/actions) — download the `opencode-apk` artifact from the most recent workflow run.

## Building

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/OpenCode.apk`

Requires Android Studio Meerkat or newer (AGP 9.1, Kotlin 2.2.20, compileSdk 37).

## Architecture

```
app/
└── src/main/java/com/opencode/app/
    ├── MainActivity.kt          # Entry point, edge-to-edge, Compose host
    ├── ui/
    │   ├── OpenCodeApp.kt       # Root composable with AnimatedContent nav
    │   ├── theme/Theme.kt       # M3 Expressive theme, shapes, typography
    │   ├── screens/
    │   │   ├── HomeScreen.kt    # Feature overview & quick actions
    │   │   ├── ChatScreen.kt    # AI chat with streaming + model picker
    │   │   ├── FilesScreen.kt   # File tree + code viewer
    │   │   ├── TerminalScreen.kt # Shell command emulator
    │   │   └── SettingsScreen.kt # App configuration
    │   └── components/
    │       └── BottomNavBar.kt  # M3 Expressive navigation bar
    ├── data/
    │   └── Models.kt            # Domain types (Session, Message, ModelInfo)
    └── viewmodel/
        └── AppViewModel.kt      # Central state via StateFlow
```

State flows through `AppViewModel` → `StateFlow<AppState>` → `collectAsState()` → Compose recomposition. No DI framework needed — ViewModel is used directly with `by viewModels()`.

## License

MIT

Replace your CLAUDE.md with this — keeps the auto-generated stuff and adds everything we discussed:
markdown# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Vision

**SafeCircle (Nirbhaya Chakra)** — predictive women's safety Android app for hackathon (Track 1: AI for Social Good). Demo city: Bangalore.

Not just an SOS button — predicts danger before escalation using sensor fusion, route deviation, BLE follower detection, watch HR sync, and historical crime data.

## Build Commands

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew build
./gradlew clean
./gradlew lint
./gradlew test
./gradlew connectedAndroidTest
```

## Project Setup

- **Package:** `com.example.nirbhaya_chakra`
- **Min SDK:** 24 | **Target/Compile SDK:** 36
- **SDK path:** `D:\AndroidSDK`
- **Kotlin:** 2.0.21 with KSP 2.0.21-1.0.25

## Architecture (MVVM + Compose)
RiskForegroundService → RiskRepository (StateFlow) → RiskViewModel → HomeScreen (Compose)

1. `MainActivity` starts `RiskForegroundService`, sets Compose content
2. Service runs continuous sensor loop, pushes `RiskData` to `RiskRepository`
3. `RiskRepository` singleton holds `MutableStateFlow<RiskData?>` — single source of truth
4. `RiskViewModel` exposes repository flow; UI collects via `collectAsState()`
5. `HomeScreen` renders score, level, reasons, SOS button

**Coding style:** One model per file in `Data/`. Utils as extension functions in `utils/`. Compose only, no XML.

## Risk Score Formula (compute on phone)

| Factor | Points | Source |
|---|---|---|
| Late hour (10pm-5am) | +20 | Phone clock |
| Movement anomaly | +20 | Accelerometer |
| HR spike | +25 | Galaxy Watch via DataLayer |
| Route deviation | +20 | GPS vs Room corridor |
| Possible follower | +35 | BLE MAC tracking |
| Crime zone (backend) | up to +30 | Backend `/api/zone-danger` |

Sum → cap at 100 → push to `RiskRepository`. Run every 10s in service.

`Int.toRiskLevel()` in `utils/RiskCalculator.kt`: Safe (≤30) → Moderate (≤60) → High (≤75) → Critical.

## Implementation Roadmap
Add this as a new step in your CLAUDE.md:
markdown### Step 0 — UI/UX Design System (Build First, Polish Throughout)

The app must look **production-grade**, not a hackathon prototype. Judges judge what they see.

#### Design Philosophy
- Dark theme primary (stealth + battery + premium feel)
- Bold, high-contrast typography
- Smooth animations on state changes (risk score transitions)
- Color psychology: green=safe, amber=caution, red=danger — felt instantly

#### Color Palette
Background:    #0F0F1A (deep navy)
Surface card:  #1A1A2E
Surface dark:  #16162A
Primary:       #E63946 (alert red)
Accent:        #F4A261 (warm orange)
Safe:          #2D9E5F (green)
Moderate:      #FFD166 (yellow)
High:          #FF8C42 (orange)
Critical:      #E63946 (red)
Text primary:  #E0E0E0
Text muted:    #9E9E9E

#### Typography
- Use **Inter** or **Poppins** font (Google Fonts)
- Risk score: 64sp Bold
- Headers: 22sp Bold
- Body: 14sp Regular
- Labels: 11sp Medium uppercase, letter-spaced

#### Screens to Build

**1. Splash Screen**
- App logo (shield with check) animated entry
- "SafeCircle" wordmark
- Subtle gradient background
- 1.5s duration → auto navigate

**2. Onboarding (3 slides)**
- Slide 1: "Predict before it escalates" + sensor icon
- Slide 2: "Always-on, even offline" + BLE/mesh icon
- Slide 3: "Your circle, alerted instantly" + people icon
- Pager dots, Skip + Next buttons

**3. Login Screen** (currently bypass — backend not ready)
- Phone number input with country code
- "Send OTP" button (mock — directly proceed)
- Or "Continue as Guest" for demo
- Hardcode user: `userId = "demo_user", name = "Priya"`
- Comment: `// TODO: Wire to /api/auth/login when backend ready`

**4. OTP Screen** (bypass — auto fill 1234)
- 4 box OTP input
- 30s resend timer
- Auto-fill 1234 → success → home

**5. Home Screen** (main dashboard)
- Top: greeting + status badge
- Center: huge animated risk score circle (pulses if Critical)
- Below: explainability card with reasons (icons + text)
- Map preview (small) showing current location
- Bottom: 3 quick action buttons — Share Location, Trusted Circle, SOS
- SOS button: large, bottom, red, haptic on press

**6. Trusted Circle Screen**
- List of contacts as cards
- Add contact FAB
- Each card: name, phone, "remove" + "call now"

**7. Routes Screen**
- "Your Routes" — list of learned trips
- Each card: thumbnail map (Google Maps Static API), Home → College, "12 trips, 23 min avg"
- Toggle: monitor this route

**8. Alert Active Screen** (when SOS fires)
- Full-screen red overlay with pulse animation
- "ALERT SENT" big text
- Live status: "Officer Rajesh responding • ETA 4 min"
- Cancel button (requires PIN to prevent attacker dismissal)
- Auto-records location every 5s shown as growing line on mini-map

**9. Stealth Mode Screen** (calculator disguise)
- Looks like a real calculator
- Hidden trigger: enter PIN → unlocks real app
- Power button 3x while in stealth = silent SOS

#### Compose Components to Build

- `RiskScoreCircle` — animated, color-changing, pulses if critical
- `ExplainabilityCard` — list of reasons with leading icons
- `SOSButton` — large red, haptic feedback, hold-to-confirm
- `ContactCard` — for trusted circle
- `RouteCard` — with static map thumbnail
- `StatusBadge` — Safe/Moderate/High/Critical pill
- `BottomNavBar` — Home / Routes / Circle / Settings

#### Animation Library
- Add `androidx.compose.animation:animation` (already in BOM)
- Use `animateColorAsState`, `animateFloatAsState` for smooth transitions
- Risk score circle: pulse animation when Critical
- Score number: animated counter when value changes

#### Demo Login Bypass

In `MainActivity`:
```kotlin
// HACKATHON DEMO BYPASS
// TODO: Replace with real auth when backend ready
val isLoggedIn = true  // hardcoded
val demoUser = User(id = "demo_user", name = "Priya")

if (!isLoggedIn) {
    // Show login flow
} else {
    // Skip to HomeScreen
}
```

Store demo user in DataStore so it persists across app restarts but feels like real login.

#### Implementation Priority
1. Splash + onboarding (visual hook)
2. Home screen with risk circle (the moneyshot for judges)
3. SOS active screen (the dramatic moment)
4. Routes + Circle screens (functional completeness)
5. Stealth mode (wow factor if time permits)

#### Pitch Line for Judges
*"Every screen was designed to communicate trust at a glance — color tells you 
the status before you read a word. The app feels safer just to look at."*

### ✅ Step 1 — Foreground Service Architecture (DONE)
Service runs, mock data updates every 2s, persistent notification, FOREGROUND_SERVICE permission set.

### Step 2 — GPS Collection
- ACCESS_FINE_LOCATION runtime request
- FusedLocationProviderClient, every 10s, 5m displacement
- Speed: >0.8 m/s walking, >4 m/s vehicle
- Unfamiliar area: >500m from any saved location

### Step 3 — Accelerometer (Movement Anomaly)
- TYPE_ACCELEROMETER, magnitude = sqrt(x² + y² + z²)
- Rest ~9.8, walking ~10-12, struggle 15+, anomaly 18+
- Rolling window of 20 readings (~2s)
- Fall detection: freefall (~0) → spike (>20) → 30s SOS countdown

### Step 4 — Heart Rate from Galaxy Watch
- Same package + app_id in both manifests
- Watch: SensorManager TYPE_HEART_RATE
- Watch → Phone: MessageClient path `/heart_rate`
- Spike: current HR > baseline + 30 BPM

### Step 5 — Microphone (dB only, NO recording)
- MediaRecorder routed to /dev/null
- Read maxAmplitude every 500ms
- Sudden quiet→loud spike = scream proxy
- Optional: yamnet.tflite for on-device classification
- Do NOT use Gemini for audio



### Step 7 — Risk Score Computation (already in formula above)
Run every 10s in service, push to `RiskRepository`.

### Step 8 — Backend Communication
- POST `/api/scores/update` every 10s
- POST `/api/alerts/trigger` when score > 75
- `alertAlreadyFired` flag, resets at score < 50
- Offline: Room queue + WorkManager retry

### Step 9 — Safe Route Saving + Deviation
- Trip start: speed > walking for 60s → new sessionId
- Save `RoutePoint(sessionId, lat, lng, timestamp)` to Room
- Trip end: stationary 3min
- Deviation: >300m outside corridor → risk bump
- Demo: pre-seed 3 fake trips on install

### Step 10 — BLE Follower Detection + Mesh Relay (Offline Lifeline)

This step combines 3 powerful BLE features:

#### 10a — Follower Detection
- Every 60s, scan 10s, collect MAC addresses
- `Map<MAC, consecutiveScans>` in memory
- Same MAC in 4+ scans AND user moved 300m+ → follower
- Bump score +35, "Possible follower detected"

#### 10b — BLE SOS Broadcast (no connection needed)
When network is dead, broadcast SOS as a BLE advertisement packet — any 
nearby phone running SafeCircle can pick it up passively without pairing.

- Use `BluetoothLeAdvertiser.startAdvertising()`
- Pack into manufacturer data: `userId(8B) + lat(4B) + lng(4B) + timestamp(4B) + alertType(1B)`
- Custom UUID for SafeCircle service: identifies our app's packets
- Broadcast continuously while alert active (every 1s, ~100m range)
- Privacy-safe: only userId hash + coords, no personal info

#### 10c — Passive BLE Listener (relay node)
Every SafeCircle phone scans for SafeCircle UUID broadcasts.

- When detected: parse payload → check if alert is fresh (timestamp < 5min)
- If this phone has internet → upload alert to backend on behalf of sender
- If not → re-broadcast (max 3 hops to prevent infinite relay)
- Send haptic + notification: "Someone nearby needs help — alerting authorities"
- This turns every SafeCircle user into a passive helper node

#### 10d — Auto Hotspot Trigger (last resort)
If alert is critical (score > 90) AND no internet AND no nearby SafeCircle device:
- Programmatically enable mobile hotspot (requires WRITE_SETTINGS permission)
- Hotspot SSID set to: `SAFECIRCLE_SOS_<userIdHash>`
- Other SafeCircle phones detect this SSID via WifiManager scan
- Connect briefly → exchange alert payload → disconnect
- Note: Hotspot toggle requires reflection on some Android versions
- Fallback: prompt user to enable manually via QuickSettings shortcut

#### 10e — Combined Flow
SOS triggered
│
▼
Try cloud (Retrofit) ──── success → done
│ fail
▼
BLE broadcast (10b) ──── nearby SafeCircle device receives → relays via internet
│ no relay in 60s
▼
Auto hotspot (10d) ──── nearby device connects → relays
│ no connect in 90s
▼
SMS fallback via SmsManager ──── always works on cellular
### Step 11 — Map Integration (Critical for Demo Visual Impact)

The map is the centerpiece of your demo. Judges need to *see* movement, danger zones, route deviations, and safe paths in real time.
Add this section to your CLAUDE.md:
markdown## Watch App Scope (Important Architectural Decision)

**The Wear OS watch app is a SEPARATE Android Studio project**, not a module in this one.

### What This Project (Phone) Needs
- DataLayer API integration to RECEIVE messages from watch
- Listen for paths: `/heart_rate`, `/sos_trigger`, `/shake_detected`, `/fall_detected`
- Send paths to watch: `/risk_score`, `/alert_status`
- `MessageClient.OnMessageReceivedListener` registered in ForegroundService
- Parse incoming HR values, feed into risk score formula
- Push current risk score to watch every 10s for glanceable display

### What This Project Does NOT Need
- ❌ No Wear OS Gradle module
- ❌ No watch UI code, no Compose for Wear OS
- ❌ No watch sensor logic (HR monitoring, fall detection)
- ❌ No watch-side Room DB

### Pairing Requirements (only for connectivity to work)
- Both apps MUST share same `applicationId` package name
- Both apps MUST be signed with same debug/release key
- Both apps declare matching `<meta-data>` for Wearable in manifest
- Watch app installs through Wear OS pairing — handled outside this project

### DataLayer Implementation in Phone Project

In `RiskForegroundService`:
```kotlin
Wearable.getMessageClient(this).addListener { messageEvent ->
    when (messageEvent.path) {
        "/heart_rate" -> handleHrUpdate(messageEvent.data)
        "/sos_trigger" -> triggerEmergencyAlert()
        "/shake_detected" -> elevateRiskScore()
        "/fall_detected" -> startFallCountdown()
    }
}
```

Send to watch:
```kotlin
Wearable.getMessageClient(context)
    .sendMessage(nodeId, "/risk_score", scoreBytes)
```

### Dependencies Needed (phone side only)
```kotlin
implementation("com.google.android.gms:play-services-wearable:18.1.0")
```

### Demo Note
Watch app development happens in parallel by another teammate (or later by you).
For demo, if watch app isn't ready: hardcode HR spike injection into mock GPS 
sequence to simulate watch sending data — same effect for judges.

#### Map Library Choice
- **Google Maps Compose** (`com.google.maps.android:maps-compose`)
- Cleaner Compose integration than raw MapView
- Free tier covers demo usage easily
## Demo Script (memorize this flow)

1. Open app → map loads, blue dot at Koramangala (mock GPS)
2. Show saved routes: "I usually go Home → College these 3 ways"
3. Toggle danger heatmap → red zones appear from scraper data
4. Mock movement starts → blue dot moves smoothly toward HSR
5. At midpoint → inject deviation → polyline turns red → score spikes
6. Show explainability card updating: "Route deviation + late hour"
7. Score crosses 75 → SOS fires → red alert marker drops on map
8. Switch to dashboard → alert card appears live via Socket.io
9. (If watch demo) Shake watch → backup SOS trigger demonstrated
10. Show offline mode: airplane mode → BLE broadcast simulation

#### Required API Keys
- Google Maps SDK key (get from console.cloud.google.com)
- Enable: Maps SDK for Android, Geocoding API, Directions API
- Add to `local.properties`: `MAPS_API_KEY=AIzaSy...`
- Reference in manifest via meta-data placeholder

#### Map Layers (stacked on single map)

**Layer 1: User Location (live)**
- Blue pulsing dot at current GPS position
- Updates every 5-10s as service pushes new location
- Camera follows user (with toggle to lock/unlock follow mode)
- Trail behind user: last 30 seconds of path as fading polyline

**Layer 2: Danger Heatmap (from backend scraper data)**
- Fetch on app open: `GET /api/incidents/heatmap?city=bangalore`
- Returns array of `{lat, lng, weight, area, count}`
- Render using `Polygon` or custom `Circle` markers with alpha by severity
- Color: yellow (mild) → orange → red (severe)
- Toggle button: show/hide heatmap layer
- Cache in Room — works offline after first fetch

**Layer 3: Saved Route Corridors**
- Each saved route = polyline drawn on map
- Multiple routes between same A→B = multiple parallel lines (Google Maps style)
- Color them differently: route 1 = blue, route 2 = purple, route 3 = teal
- Width: 6dp, semi-transparent
- Tap a polyline → highlights it + shows "Home → College via Silk Board, 23 min avg, 14 trips"

**Layer 4: Active Trip Path (when traveling)**
- Solid bright line drawn as user moves
- Compared against saved corridors in real time
- If outside corridor by 300m+ → line turns RED + deviation marker drops
- This is your dramatic demo moment — judges see the line go red live

**Layer 5: Alert Markers**
- When SOS fires → big pulsing red marker at trigger point
- Officer assigned → blue marker for officer position (mock for demo)
- Animated line connecting them as officer "approaches"

#### How Saved Routes Work (your specific question)

**Concept:** Multiple paths between same origin-destination = corridors.

**Storage:** Each completed trip = list of GPS points in Room.
trip_sessions table:

sessionId, userId, startLat, startLng, endLat, endLng,
startLabel ("Home"), endLabel ("College"), durationMs, completedAt

route_points table:

sessionId, lat, lng, timestamp, sequenceIndex


**Clustering Logic:**
- Two trips = "same route" if start within 200m AND end within 200m
- Group all such trips → that's a route corridor
- 3+ trips with different mid-paths = 3 alternative routes between same A and B

**Fetching for Display:**
GET /api/routes/saved?userId=X
→ Returns:
[
{
"routeId": "home_college_via_silk_board",
"startLabel": "Home",
"endLabel": "College",
"tripCount": 14,
"avgDurationMin": 23,
"polyline": [{lat, lng}, ...] // simplified average path
},
{
"routeId": "home_college_via_koramangala",
"tripCount": 5,
...
}
]

**Backend simplification (Douglas-Peucker):**
- Don't return every GPS point — too heavy
- Server simplifies polylines to ~50 points max per route
- Phone gets lightweight payload, renders smooth lines

**Display on Map:**
- Loop through saved routes → for each, draw `Polyline` with that route's points
- User toggle: "Show saved routes" on/off
- Tap to focus: zooms to that route's bounds

#### Real-Time GPS → Map Flow
ForegroundService (every 5-10s)
│ new lat/lng
▼
Update RiskRepository
│
├──► Push to Map Composable (Compose recomposes marker position)
├──► Save to Room (RoutePoint if trip active)
├──► Send to backend (POST /api/scores/update)
└──► Check deviation against saved corridors
│ if deviating
▼
Mark "isDeviatingRoute = true" in RiskData
Polyline color flips to red
Risk score bumps +20

#### Map Screen Components

- `MapScreen` Composable — top-level
- `LiveLocationMarker` — pulsing dot
- `DangerHeatmap` — overlay layer
- `SavedRouteOverlay` — multiple polylines
- `ActiveTripPath` — current journey line
- `AlertMarker` — for SOS pins
- Bottom sheet: toggles for each layer + status

#### Direction API Usage (for "Safe Route Suggestion")

When user enters destination → fetch routes:
GET /api/safe-routes?from=lat,lng&to=lat,lng

Backend calls Google Directions API with `alternatives=true`, gets 2-3 route options, scores each by:
- Crime zone overlap (lower = safer)
- Average risk score along path
- Time of day adjustment

Returns ranked routes — phone displays them as colored polylines:
- Green polyline = safest route
- Yellow = moderate
- Red = avoid

User taps "Use safe route" → activates navigation mode → real-time deviation tracking begins.

#### Demo Script Using Map

1. **Open app** → map loads, user dot shows
2. **Saved routes appear** → "I usually go Home→College these 3 ways"
3. **Heatmap toggle on** → red zones appear in known crime areas
4. **Start moving** (simulated via mock GPS) → live dot moves smoothly
5. **Deviate from path** → line turns red, score jumps, reason: "Route deviation"
6. **Score crosses 75** → SOS fires, red marker drops, dashboard sees alert
7. **Officer marker** appears, line of approach drawn

This single demo moment communicates 5 features in 30 seconds.

#### Permissions
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` (already declared)
- Network state for tile loading

#### Mock GPS Demo Setup (Hackathon Critical)

Backend endpoints not finalized yet — for demo, hardcode everything on phone.

**Two demo points in Bangalore:**
```kotlin
// In RiskForegroundService or DemoConfig.kt
val DEMO_START = LatLng(12.9352, 77.6245)  // Koramangala 5th Block
val DEMO_END   = LatLng(12.9180, 77.6350)  // HSR Layout Sector 1
```

**Mock movement logic:**
- Service interpolates GPS points between START and END every 5s
- Simulates smooth movement (~30 points along route)
- Use linear interpolation: `lat = startLat + (endLat - startLat) * progress`
- Progress increments 0.0 → 1.0 over ~2.5 minutes
- Push each interpolated point to RiskRepository as if real GPS

**Demo Mode Toggle:**
```kotlin
object DemoConfig {
    var isDemoMode = true  // toggle in settings
    var demoSpeed = 1.0f   // 1x = normal, 2x = faster demo
}
```

When `isDemoMode = true`:
- Skip real FusedLocationProviderClient
- Use mock interpolator instead
- Map shows movement between Koramangala → HSR

**Mid-route deviation trigger (for dramatic demo):**
- At progress = 0.5 (midpoint), inject deviation: jump 400m off path
- This triggers route deviation detection live
- Risk score visibly spikes on screen
- Reason "Route deviation" appears in explainability card

**Pre-seed saved routes for deviation comparison:**
```kotlin
// On app first launch, insert 3 fake completed trips into Room
// All going Koramangala → HSR via slightly different paths
// This way deviation detection has corridors to compare against from day 1
```

**API endpoints — DO NOT WIRE YET**
All backend calls (`/api/scores/update`, `/api/alerts/trigger`, `/api/zone-danger`, 
`/api/routes/save`) remain commented out / mocked locally. User will provide real 
URLs and contracts later — only then restructure the network layer.

For now: everything runs offline on phone with mock data and pre-seeded routes.
#### Permissions Required
- `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT` (Android 12+)
- `ACCESS_FINE_LOCATION` (BLE scan needs it)
- `WRITE_SETTINGS` (for hotspot)
- `CHANGE_WIFI_STATE`, `ACCESS_WIFI_STATE` (for SSID scan)

#### Demo Pitch Line
*"Even if every network fails, your phone broadcasts SOS via Bluetooth to nearby 
SafeCircle users — turning every app user into a relay node. Worst case, the 
phone auto-enables a hotspot named SAFECIRCLE_SOS that other devices detect."*

### Watch Module (Wear OS XML, separate Gradle module)
- W1: New Module → Wear OS in same project
- W2: BoxInsetLayout root
- W3: Screens — Main (score+HR), SOS confirm, Alert status
- W4: 3 shakes in 2s = SOS via DataLayer
- W5: Vibrator: short-short-long = danger
- W6: Phone↔Watch via MessageClient (`/risk_score`, `/heart_rate`)

## Backend Integration

### Status
Teammate building Node.js + MongoDB Atlas. NOT ready yet. Use mock data until URL provided.

### Backend URL
TBD — update here once deployed.




### Auth
JWT Bearer token via POST `/api/auth/login`. Add interceptor in `ApiClient.kt`.

### When Backend Ready
User will say: "Backend is ready, URL: xxx, here are contract changes". Then:
1. Update `ApiClient.kt` baseUrl
2. Restructure `ApiService.kt` endpoints
3. Replace mock calls with real Retrofit
4. Add JWT interceptor
5. Test
## Permissions Declared

`FOREGROUND_SERVICE_LOCATION`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`.

Add when needed: `BODY_SENSORS`, `ACTIVITY_RECOGNITION`, `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `RECORD_AUDIO`, `INTERNET`.

## Key Libraries

| Library | Version | Purpose |
|---|---|---|
| Compose | BOM 2024.09.00 | UI |
| Material3 | via BOM | Design |
| Lifecycle ViewModel Compose | 2.8.7 | ViewModel integration |
| Room | 2.6.1 | Local DB (KSP) |
| KSP | 2.0.21-1.0.25 | Annotation processing |

To be added: Retrofit, OkHttp, Play Services Location, Play Services Wearable, WorkManager.

## Current Implementation State

- ✅ Foreground service running with mock data simulation
- ✅ Repository → ViewModel → Compose pipeline working
- ⏳ Room schema (`RoutePoint`) defined but not wired
- ⏳ No GPS, accelerometer, BLE, mic, watch sync yet
- ⏳ No network client integrated
- ⏳ Watch module not created

## Dependency Management

All versions in `gradle/libs.versions.toml`. Add there first, then reference in `app/build.gradle.kts`.
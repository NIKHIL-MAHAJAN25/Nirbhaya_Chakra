Here's the final clean CLAUDE.md — copy this entirely, replace your current file:
markdown# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

---

## Project Vision

**SafeCircle (Nirbhaya Chakra)** — predictive women's safety Android app for hackathon (Track 1: AI for Social Good). Demo city: Bangalore.

Not just an SOS button — predicts danger before escalation using sensor fusion, route deviation, BLE follower detection, watch HR sync, and historical crime data.

---

## Project Setup

- **Package:** `com.example.nirbhaya_chakra`
- **Min SDK:** 24 | **Target/Compile SDK:** 36
- **SDK path:** `D:\AndroidSDK`
- **Kotlin:** 2.0.21 with KSP 2.0.21-1.0.25

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

---

## Architecture (MVVM + Compose)
RiskForegroundService → RiskRepository (StateFlow) → RiskViewModel → HomeScreen (Compose)

1. `MainActivity` starts `RiskForegroundService`, sets Compose content
2. Service runs continuous sensor loop, pushes `RiskData` to `RiskRepository`
3. `RiskRepository` singleton holds `MutableStateFlow<RiskData?>` — single source of truth
4. `RiskViewModel` exposes repository flow; UI collects via `collectAsState()`
5. `HomeScreen` renders score, level, reasons, SOS button

**Coding style:** One model per file in `Data/`. Utils as extension functions in `utils/`. Compose only, no XML.

---

## Code Style Rules

- Always add inline comments for non-obvious logic
- Every function gets a doc comment: purpose, params, return value
- Mark incomplete sections with `// TODO: <description>`
- Mark mock/demo placeholders with `// MOCK: <description>` so they're easy to find later
- Prefer descriptive variable names over short ones (riskScore not rs)
- Group imports: Android, AndroidX, third-party, project — separated by blank lines

---

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

## Score Thresholds & Actions

| Score | Level | Action |
|---|---|---|
| 0-30 | Safe (Green) | Passive update only |
| 31-60 | Moderate (Orange) | Watch haptic pulse, family sees amber |
| 61-75 | High (Red) | Auto safe-route suggestion, family alerted |
| 76-100 | Critical | Auto alert, police notified, SMS sent |
| >85 for 60s | Persistent | Escalation: secondary contacts + supervisor |

---

## Implementation Roadmap

### ✅ Step 1 — Foreground Service Architecture (DONE)
Service runs, mock data updates every 2s, persistent notification, FOREGROUND_SERVICE permission set.

---

### Step 0 — UI/UX Design System (Build alongside everything)

The app must look **production-grade**, not a hackathon prototype.

#### Design Philosophy
- Dark theme primary (stealth + battery + premium feel)
- Bold, high-contrast typography
- Smooth animations on state changes
- Color psychology: green=safe, amber=caution, red=danger

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
- Inter or Poppins font (Google Fonts)
- Risk score: 64sp Bold
- Headers: 22sp Bold
- Body: 14sp Regular
- Labels: 11sp Medium uppercase, letter-spaced

#### Screens to Build
1. **Splash** — logo animated entry, 1.5s → auto navigate
2. **Onboarding** — 3 slides, pager dots, Skip + Next
3. **Login** — phone + OTP (BYPASS for demo, hardcode demo_user)
4. **OTP** — 4-box input, auto-fill 1234 (BYPASS)
5. **Home** — risk score circle, explainability card, map preview, SOS button
6. **Trusted Circle** — contact cards, add FAB
7. **Routes** — saved trips with map thumbnails
8. **Alert Active** — full-screen red overlay, officer status, PIN cancel
9. **Stealth Mode** — calculator disguise, hidden PIN unlock

#### Compose Components
- `RiskScoreCircle` — animated, color-changing, pulses if Critical
- `ExplainabilityCard` — reasons with leading icons
- `SOSButton` — large red, haptic, hold-to-confirm
- `ContactCard`, `RouteCard`, `StatusBadge`, `BottomNavBar`

#### Animation
- Use `animateColorAsState`, `animateFloatAsState`
- Risk score circle: pulse when Critical
- Score number: animated counter on change

#### Demo Login Bypass
```kotlin
// MOCK: Replace with real auth when backend ready
val isLoggedIn = true
val demoUser = User(id = "demo_user", name = "Priya")
```

#### Implementation Priority
1. Splash + onboarding (visual hook)
2. Home with risk circle (moneyshot for judges)
3. Alert Active screen (dramatic moment)
4. Routes + Circle (functional completeness)
5. Stealth mode (wow factor)

---

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

### Step 4 — Heart Rate from Watch (DataLayer Receiver Only)
See "Watch App Scope" section below. Phone only RECEIVES from watch.
- Listen on `/heart_rate` path
- Spike: current HR > baseline + 30 BPM

### Step 5 — Microphone (dB only, NO recording)
- MediaRecorder routed to /dev/null
- Read maxAmplitude every 500ms
- Sudden quiet→loud spike = scream proxy
- Optional: yamnet.tflite for on-device classification
- Do NOT use Gemini for audio

### Step 6 — Gemini API (Explainability Only)
- Only when score > 60
- Input: risk factors object → Output: natural language reason
- Display in HomeScreen explainability card
- Free tier from Google AI Studio

### Step 7 — Risk Score Computation
Run formula above every 10s in service, push to `RiskRepository`.

### Step 8 — Backend Communication (when URL provided)
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

---

### Step 10 — BLE Follower Detection + Mesh Relay (Offline Lifeline)

#### 10a — Follower Detection
- Every 60s, scan 10s, collect MAC addresses
- `Map<MAC, consecutiveScans>` in memory
- Same MAC in 4+ scans AND user moved 300m+ → follower
- Bump score +35

#### 10b — BLE SOS Broadcast (no connection needed)
When network is dead, broadcast SOS as BLE advertisement packet — any nearby phone running SafeCircle picks it up passively.
- `BluetoothLeAdvertiser.startAdvertising()`
- Manufacturer data: `userId(8B) + lat(4B) + lng(4B) + timestamp(4B) + alertType(1B)`
- Custom UUID for SafeCircle service
- Broadcast every 1s while active, ~100m range

#### 10c — Passive BLE Listener (relay node)
Every SafeCircle phone scans for SafeCircle UUID broadcasts.
- Detected → parse → check timestamp < 5min
- Has internet → upload alert on behalf of sender
- No internet → re-broadcast (max 3 hops)
- Notify user: "Someone nearby needs help"

#### 10d — Auto Hotspot Trigger (last resort)
If score > 90 AND no internet AND no nearby SafeCircle device:
- Enable hotspot programmatically (WRITE_SETTINGS)
- SSID: `SAFECIRCLE_SOS_<userIdHash>`
- Other phones detect SSID via WifiManager scan
- Connect briefly → exchange payload → disconnect

#### 10e — Combined Fallback Flow
SOS triggered
↓
Cloud (Retrofit) — success → done
↓ fail
BLE broadcast (10b) — nearby device receives → relays
↓ no relay in 60s
Auto hotspot (10d) — nearby connects → relays
↓ no connect in 90s
SMS via SmsManager — always works on cellular

#### Permissions
- `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT` (Android 12+)
- `ACCESS_FINE_LOCATION` (BLE scan needs it)
- `WRITE_SETTINGS` (hotspot)
- `CHANGE_WIFI_STATE`, `ACCESS_WIFI_STATE`

---

### Step 11 — Map Integration (Critical for Demo Visual Impact)

The map is the centerpiece of your demo.

#### Map Library
- `com.google.maps.android:maps-compose`
- Cleaner Compose integration than raw MapView

#### API Keys
- Google Maps SDK key from console.cloud.google.com
- Enable: Maps SDK for Android, Geocoding API, Directions API
- Add to `local.properties`: `MAPS_API_KEY=AIzaSy...`

#### Map Layers (stacked on single map)

**Layer 1: User Location (live)**
- Blue pulsing dot at current GPS position
- Updates every 5-10s as service pushes new location
- Camera follows user (toggle to lock/unlock)
- Trail behind: last 30s of path as fading polyline

**Layer 2: Danger Heatmap**
- Fetch on app open (when backend ready)
- Render circles with alpha by severity
- Yellow → orange → red
- Toggle button to show/hide
- Cache in Room — works offline

**Layer 3: Saved Route Corridors**
- Each route = polyline drawn on map
- Multiple A→B routes = parallel lines, different colors
- Tap polyline → highlights + shows "Home → College via Silk Board, 23 min avg"

**Layer 4: Active Trip Path**
- Solid bright line as user moves
- Compared to saved corridors live
- Outside by 300m+ → line turns RED + deviation marker drops
- Dramatic demo moment

**Layer 5: Alert Markers**
- SOS fires → big pulsing red marker
- Officer assigned → blue marker (mock for demo)
- Animated approach line

#### How Saved Routes Work

**Concept:** Multiple paths between same A→B = corridors.

**Storage (Room):**
trip_sessions: sessionId, userId, startLat/Lng, endLat/Lng,
startLabel, endLabel, durationMs, completedAt
route_points: sessionId, lat, lng, timestamp, sequenceIndex

**Clustering Logic:**
- Two trips = "same route" if start within 200m AND end within 200m
- Group all such trips → one corridor
- Different mid-paths → multiple corridors between same A and B

**Display:**
- Loop through saved routes → draw `Polyline` for each
- Toggle: "Show saved routes" on/off
- Tap to focus: zooms to route bounds

#### Real-Time GPS → Map Flow
ForegroundService (every 5-10s)
↓ new lat/lng
Update RiskRepository
├──► Push to Map Composable (recompose marker)
├──► Save to Room (if trip active)
└──► Check deviation against corridors
↓ if deviating
Mark isDeviatingRoute = true
Polyline color → red
Risk score +20

#### Map Screen Components
- `MapScreen` — top-level
- `LiveLocationMarker` — pulsing dot
- `DangerHeatmap` — overlay
- `SavedRouteOverlay` — multiple polylines
- `ActiveTripPath` — current journey
- `AlertMarker` — for SOS pins
- Bottom sheet: layer toggles + status

#### Mock GPS Demo Setup (Hackathon Critical)

**Two demo points in Bangalore:**
```kotlin
// In DemoConfig.kt or RiskForegroundService
val DEMO_START = LatLng(12.9352, 77.6245)  // Koramangala 5th Block
val DEMO_END   = LatLng(12.9180, 77.6350)  // HSR Layout Sector 1
```

**Mock movement logic:**
- Service interpolates GPS points between START and END every 5s
- ~30 points along route, smooth movement
- Linear interpolation: `lat = startLat + (endLat - startLat) * progress`
- Progress 0.0 → 1.0 over ~2.5 minutes
- Push each point to RiskRepository as if real GPS

**Demo Mode Toggle:**
```kotlin
object DemoConfig {
    var isDemoMode = true
    var demoSpeed = 1.0f
}
```

When `isDemoMode = true`:
- Skip real FusedLocationProviderClient
- Use mock interpolator
- Map shows movement Koramangala → HSR

**Mid-route deviation injection:**
- At progress = 0.5, jump 400m off path
- Triggers route deviation detection live
- Score visibly spikes
- Reason "Route deviation" appears

**Pre-seed saved routes:**
```kotlin
// MOCK: On first launch, insert 3 fake completed trips into Room
// All Koramangala → HSR via different paths
// Deviation detection has corridors to compare from day 1
```

**API endpoints — DO NOT WIRE YET**
All backend calls remain commented/mocked locally until URLs provided.

---

## Watch App Scope (Important Architectural Decision)

**The Wear OS watch app is a SEPARATE Android Studio project**, not a module in this one.

### What This Project (Phone) Needs
- DataLayer API integration to RECEIVE messages from watch
- Listen for paths: `/heart_rate`, `/sos_trigger`, `/shake_detected`, `/fall_detected`
- Send paths to watch: `/risk_score`, `/alert_status`
- `MessageClient.OnMessageReceivedListener` registered in ForegroundService
- Parse incoming HR values, feed into risk score formula
- Push current risk score to watch every 10s

### What This Project Does NOT Need
- ❌ No Wear OS Gradle module
- ❌ No watch UI code, no Compose for Wear OS
- ❌ No watch sensor logic
- ❌ No watch-side Room DB

### Pairing Requirements
- Both apps MUST share same `applicationId` package name
- Both apps MUST be signed with same debug/release key
- Both apps declare matching `<meta-data>` for Wearable in manifest

### DataLayer Implementation in Phone

In `RiskForegroundService`:
```kotlin
Wearable.getMessageClient(this).addListener { messageEvent ->
    when (messageEvent.path) {
        "/heart_rate"     -> handleHrUpdate(messageEvent.data)
        "/sos_trigger"    -> triggerEmergencyAlert()
        "/shake_detected" -> elevateRiskScore()
        "/fall_detected"  -> startFallCountdown()
    }
}
```

Send to watch:
```kotlin
Wearable.getMessageClient(context)
    .sendMessage(nodeId, "/risk_score", scoreBytes)
```

### Dependency Needed (phone side)
```kotlin
implementation("com.google.android.gms:play-services-wearable:18.1.0")
```

### Demo Note
Watch app built in parallel by teammate. If not ready for demo, hardcode HR spike injection into mock GPS sequence — same effect for judges.

---

## What Phone Sends to Backend (Outbound — Build This Side Ready)

Phone is the **producer** — every sensor reading, alert, and trip becomes an outbound payload. Build all sender code ready, even though backend isn't deployed. When backend goes live, only `baseUrl` flips and everything works.

### Continuous Stream (every 5-10s during active trip)
**POST /api/scores/update**
```json
{
  "userId": "demo_user",
  "riskScore": 45,
  "level": "MODERATE",
  "lat": 12.9352,
  "lng": 77.6245,
  "speed": 8.2,
  "reasons": ["Late hour", "Unfamiliar area"],
  "timestamp": 1730384521000,
  "isDeviatingRoute": false,
  "followerDetected": false
}
```

### Alert Trigger (score > 75)
**POST /api/alerts/trigger**
```json
{
  "userId": "demo_user",
  "riskScore": 87,
  "lat": 12.9210,
  "lng": 77.6280,
  "reasons": ["Route deviation", "HR spike", "Late hour"],
  "alertType": "AUTO",
  "timestamp": 1730384521000
}
```

### Trip Lifecycle (start + end)
**POST /api/trips/start**
```json
{ "userId": "demo_user", "sessionId": "uuid", "startLat": ..., "startLng": ..., "startedAt": ... }
```

**POST /api/trips/end** (with full GPS trail)
```json
{
  "userId": "demo_user",
  "sessionId": "uuid",
  "endLat": ..., "endLng": ...,
  "durationMs": 150000,
  "points": [{ "lat": ..., "lng": ..., "timestamp": ... }, ...]
}
```

### Watch Events Forwarded (when watch fires)
**POST /api/events/watch**
```json
{ "userId": "demo_user", "eventType": "SHAKE_SOS" | "FALL_DETECTED" | "HR_SPIKE", "value": 142, "timestamp": ... }
```

---

## What Phone Receives from Backend (Inbound — Be Ready to Consume)

These are different — phone listens but doesn't send. Build empty handlers now, wire later.

### Polled / Fetched
**GET /api/zone-danger?lat=X&lng=Y** — historical crime score for current location
**GET /api/routes/saved?userId=X** — corridors learned across trips
**GET /api/incidents/heatmap?city=bangalore** — heatmap layer data

### Pushed (Socket.io or FCM)
- `alertResponse` — officer assigned, ETA, status updates
- `familyMessage` — trusted contact sent message
- `safeRouteSuggestion` — backend suggests rerouting
- `zoneAlert` — broadcast to all users in zone (e.g. "incident reported nearby")

**Build receivers as no-op stubs that just log for now. When backend is ready, fill them in.**

---

## Hackathon Demo: Simulated Taxi Trip (Final Decided Approach)

For the demo, we're **simulating a taxi-like trip** between two fixed Bangalore points. The phone fakes GPS, the React dashboard receives the same data live (via mocked Socket.io or polling), so judges see synchronized movement on both screens — phone shows the user's view, dashboard shows the police/family view.

### Trip Definition
- **Pickup:** Koramangala 5th Block — `(12.9352, 77.6245)`
- **Drop:** HSR Layout Sector 1 — `(12.9180, 77.6350)`
- **Duration:** ~2.5 minutes simulated (configurable demo speed)
- **Vehicle profile:** Taxi/cab — speed varies 20-40 km/h, occasional stops

### What Happens During the Simulated Trip

| Time | Event | Phone Shows | Dashboard Shows |
|---|---|---|---|
| 0:00 | Trip starts | "On the way to HSR" | New live trip pin appears |
| 0:30 | Normal driving | Score 25 (Safe) | Green pin moving smoothly |
| 1:00 | Late hour factor kicks in | Score 45 (Moderate) | Pin turns amber |
| 1:15 | Mock route deviation injected | Score jumps to 70 | Pin turns red, deviation alert |
| 1:30 | Mock HR spike from "watch" | Score 85 (Critical) | Alert card pops on dashboard |
| 1:45 | Auto SOS fires | Red overlay on phone | Alert assigned, officer marker |
| 2:00 | (Demo) Officer responds | "Officer responding" banner | Officer marker moves toward pin |
| 2:30 | Trip ends | Returns to home screen | Trip closed, logged in history |

### Phone Side Implementation
- `DemoTripSimulator` class interpolates GPS between pickup/drop
- Each interpolated point flows through normal pipeline (RiskRepository → ViewModel → Map)
- At specific progress points, inject events (deviation, HR spike, follower)
- **Send same data to dashboard** via whatever channel teammate sets up (Socket.io / WebSocket / mocked endpoint)
- Demo toggle in settings: "Start Demo Trip" button kicks it off

### Dashboard Sync (teammate's responsibility, document the contract)
- Dashboard subscribes to `userId = "demo_user"` socket room
- Receives same `scoreUpdate` events phone produces
- Live pin moves on dashboard map in sync with phone
- Alerts appear simultaneously when triggered

### Why This Demo Works
- Single scripted flow → judges see all features in 2.5 min
- Phone + dashboard side-by-side on screen → "complete ecosystem" shown live
- Reproducible — works without WiFi, walking, real cab
- Dramatic — score climbing visibly, polyline turning red, alert firing on dashboard

### Demo Mode Toggle
```kotlin
object DemoConfig {
    var isDemoMode = true                    // disables real GPS
    var demoSpeed = 1.0f                     // 1x normal, 2x faster for short demo
    var injectDeviationAt = 0.5f             // progress fraction
    var injectHrSpikeAt = 0.7f
    var injectFollowerAt = 0.3f
}
```

## Complete Permissions List

Already declared:
- FOREGROUND_SERVICE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
- POST_NOTIFICATIONS, FOREGROUND_SERVICE

Add as needed per step:
- BODY_SENSORS, ACTIVITY_RECOGNITION (HR + accelerometer)
- BLUETOOTH_SCAN, BLUETOOTH_ADVERTISE, BLUETOOTH_CONNECT
- RECORD_AUDIO (mic dB only)
- CHANGE_WIFI_STATE, ACCESS_WIFI_STATE (hotspot scan)
- WRITE_SETTINGS (hotspot trigger)
- INTERNET, ACCESS_NETWORK_STATE
- SEND_SMS (offline SMS fallback)
- VIBRATE

---

## Demo Script (memorize this flow)

1. Open app → splash → bypass login → home loads with map at Koramangala
2. Show saved routes: "I usually go Home → College these 3 ways"
3. Toggle danger heatmap → red zones appear
4. Mock movement starts → blue dot moves smoothly toward HSR
5. At midpoint → inject deviation → polyline turns red → score spikes
6. Explainability card updates: "Route deviation + late hour"
7. Score crosses 75 → SOS fires → red alert marker drops on map
8. (If watch ready) Shake watch → backup SOS demonstrated
9. Show offline mode: airplane mode → BLE broadcast simulation
10. Switch to dashboard (teammate's screen) → alert appears live

---

## Judge Pitch Lines (use in pitch + UI copy)

- **Opening:** "SafeCircle doesn't wait for a woman to press SOS. It predicts danger before it happens."
- **Intelligence:** "Risk score combines live sensor fusion, time context, and real scraped Bangalore crime data."
- **Wear OS:** "If the phone is snatched, the watch keeps running."
- **Offline:** "Three fallback layers: cloud sync, peer mesh relay, SMS."
- **Closing:** "Phone, watch, command center — real data, real intelligence, real fallbacks."

---

## Key Libraries

| Library | Version | Purpose |
|---|---|---|
| Compose | BOM 2024.09.00 | UI |
| Material3 | via BOM | Design |
| Lifecycle ViewModel Compose | 2.8.7 | ViewModel integration |
| Room | 2.6.1 | Local DB (KSP) |
| KSP | 2.0.21-1.0.25 | Annotation processing |

To add: Retrofit, OkHttp, Play Services Location, Play Services Wearable, Maps Compose, WorkManager.

---

## Current Implementation State

- ✅ Foreground service running with mock data simulation
- ✅ Repository → ViewModel → Compose pipeline working
- ⏳ Room schema (`RoutePoint`) defined but not wired
- ⏳ No GPS, accelerometer, BLE, mic, watch sync yet
- ⏳ No network client integrated
- ⏳ Map screen not built
- ⏳ Login/onboarding screens not built
- ⏳ Watch DataLayer receiver not added

---

## Dependency Management

All versions in `gradle/libs.versions.toml`. Add there first, then reference in `app/build.gradle.kts`.
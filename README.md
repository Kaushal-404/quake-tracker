# Quake Tracker

An in-pocket companion that shows the last 24 hours of
earthquakes from the USGS feed, as a list and on a map, with distance from the user's
location when they allow it.

Kotlin, Jetpack Compose, Hilt, Room, Retrofit + kotlinx.serialization, Google Maps Compose.

## 1. Building and running

Requirements

- Android Studio Narwhal or newer (AGP 8.13.2, Gradle 8.13)
- JDK 17
- Android SDK platform 36
- A device or emulator with Google Play services (the map needs it); minSdk is 24

Steps

1. Clone the repository.
2. Add a Google Maps key (see section 6).
3. Build and install:

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and press Run.

Unit tests and lint:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug
```

## 2. Architecture and technical decisions

Three layers in one module, with dependencies pointing inward:

```
ui        Compose screens, ViewModels, UI state        (depends on domain)
domain    models, repository interfaces, one use case   (depends on nothing Android)
data      Retrofit API, Room database, mappers, repositories, location
di        Hilt modules wiring data implementations to domain interfaces
```

**Offline-first.** Screens only ever read from Room. A refresh downloads the feed and
replaces the table in one transaction; the UI updates because it observes the table.
A failed refresh therefore cannot blank the screen: the user sees the saved data plus
an error banner with a Retry action. Two refreshes never run in parallel (a `Mutex`
in the repository).

**Unidirectional data flow.** Each ViewModel exposes a single immutable `StateFlow`
of UI state, built with `combine` over private `MutableStateFlow`s and shared with
`stateIn(WhileSubscribed(5s))` so a rotation does not restart the database query but
backgrounding the app does stop it. Screens are stateless functions of that state and
report user actions through plain callbacks, which keeps them previewable and testable.

**Typed failures.** `refresh()` returns a sealed `RefreshResult` instead of throwing.
`DataError` distinguishes no connection, an HTTP error with its code, and unparseable
data. The UI maps each to a string in one `when` over the sealed type, so a new error
kind fails compilation until it is handled. Coroutine cancellation is deliberately not
caught so work stops when the screen goes away.

**One trust boundary.** DTOs are fully nullable because the USGS feed really does omit
fields (magnitude in particular). A single mapper turns a feature into a database row
or drops it when a field we cannot work without is missing. Everything past that point
is non-null and trusted.

**Three model types** (DTO, entity, domain) so a feed or schema change never forces a UI
change. The only real conversion is epoch millis to `Instant`.

**Type-safe navigation.** Destinations are `@Serializable` classes; the detail screen
reads its id from `SavedStateHandle`, so it survives process death.

**Location.** The fused provider at balanced-power priority, coarse permission only
(enough for "how far away", less to ask of the user). A 10-second timeout, with a
fall-back to the last known fix. Without permission the app is fully usable: the
distance rows are hidden and the "Nearest" sort is disabled.

**Map.** Markers are clustered so a busy region stays readable; tapping a cluster
zooms to fit it. Each pin is the same magnitude badge composable the list uses, so
colour and formatting have one source of truth.

## 3. Assumptions and tradeoffs

- **24-hour window.** I use the `all_day` feed and replace the table on every successful
  refresh. Saved data persists until the next successful refresh, which is what the
  brief's offline requirement needs. Events older than a day disappear by design.
- **Full replace, not merge.** Simpler and also removes events USGS withdraws. The cost is
  that a detail screen can turn into "no longer in the latest data" after a refresh,
  which the app handles explicitly.
- **Haversine distance** on a spherical Earth, accurate to about 0.5 percent. Good enough
  for "565 km away".
- **A single use case.** Only the list needs filtering, sorting and distance, so only the
  list has a use case. The map and detail ViewModels read the repository directly; a
  use case there would only forward the call.
- **Magnitude filter semantics.** An event with unknown magnitude only passes the "All"
  filter, because the app cannot claim it exceeds a threshold.
- **Library versions.** AGP 8.13 supports compileSdk 36 at most, and the newest
  AndroidX releases (navigation 2.10, lifecycle 2.11, core 1.19) require compileSdk 37
  and AGP 9.1. I pinned the newest versions compatible with the current toolchain and
  noted the ceiling in `gradle/libs.versions.toml`.
- **No pagination.** The daily feed is bounded (a few hundred events).

## 4. Known limitations and what I would do next

- No "last updated" timestamp when offline. The banner says saved data is shown but not
  how old it is. I would persist the refresh time and show it.
- Mapping, distance calculation and sorting run in the collector's context (Main). Fine
  for a few hundred rows; for thousands I would inject a dispatcher and `flowOn(Default)`.
- The relative time strings ("12 minutes ago") are computed at composition and do not tick.
- Sort and filter choices reset after process death. They belong in `SavedStateHandle`.
- No background refresh. `WorkManager` with a periodic constraint-aware job would be the
  platform-appropriate way to keep the cache warm.
- The detail screen's map sits inside a vertical scroll and competes for drag gestures.
- Only coarse location is requested, so "distance from you" is city-level accuracy.

### Next phase, in priority order

1. **Last-updated timestamp.** Persist the time of the last successful refresh and show
   it in the offline banner and list header. Small change, closes the biggest UX gap:
   offline users currently cannot tell how old the data is.

2. **Background refresh with WorkManager.** A periodic job (every 1 to 3 hours,
   network-constrained, battery-aware) keeps the cache warm so the app opens with
   fresh data.

3. **Significant-event notifications.** When a background refresh brings in an event
   above a magnitude threshold, or within a radius of the user, post a local
   notification that deep-links to the detail screen. The brief says "in-pocket
   companion"; this is the feature that makes it one. Firebase Cloud Messaging is not
   needed for this, since USGS has no push channel; WorkManager plus local
   notifications is enough and has no backend.

4. **Shared refresh state.** Refresh logic is duplicated in the List and Map ViewModels
   with separate spinners. Move it into one refresh use case or state holder so a
   refresh started on one tab shows on the other.

5. **Historical data and time windows.** USGS exposes week and month feeds plus a query
   API with date and magnitude parameters (archive back to the 1960s). Add a window
   selector (day, week, month) and a Room table keyed by window. This is where Paging3
   and a bounded cache policy start to matter, and where the map needs
   viewport-bounded queries rather than loading everything.

6. **Map improvements.** Fit the camera to the data on first load instead of a fixed
   world view. Add a "near me" camera button when location is available. Consider
   heat-map rendering from Maps Utils for the month view.

7. **Detail screen context.** Nearby-places or terrain imagery from a second public
   source (the brief's optional idea). Keep it as a separate, failure-tolerant section
   so the detail screen still renders if that source is down.
8. **Early warning and personal risk context.** Earthquakes cannot be predicted, so
   this is not a prediction feature. It is two data-backed additions:
   - _Early warning._ Systems like USGS ShakeAlert detect the fast P-wave at sensors
     near the epicentre and warn people further away seconds before the damaging
     S-wave arrives. Android already does this at the OS level (Android Earthquake
     Alerts, using phone accelerometers as a crowd-sourced sensor network). Our app
     cannot run its own network, but it can consume ShakeAlert-derived feeds where
     available and deep-link into the matching event when the OS alert fires.
     Technically: a notification deep link into `DetailDestination`, plus a feed poll
     in the WorkManager job from item 2.

## 5. Testing approach

I tested the behaviour that would hurt users if it broke, with plain JUnit and
`kotlinx-coroutines-test`, no mocking framework:

- **Mapper**: longitude and latitude are read from the right positions (the easiest thing
  to get backwards), broken features are dropped while the rest of the feed is kept,
  and a null magnitude stays null.
- **Distance**: a known city pair and the zero case.
- **Use case**: each sort order, unknown magnitudes and unknown distances go last, the
  stable order without a location, and each magnitude filter.
- **List ViewModel**, through fakes of both repositories: a failed refresh keeps the
  saved earthquakes and shows the error, a successful retry clears it, the location
  prompt appears and declining hides it, granting permission produces distances, and
  the magnitude filter hides and restores rows.

The fakes live in `app/src/test/.../fakes`. The domain layer has no Android
dependencies, so none of these tests need Robolectric or a device.

What I would add next: a `MockWebServer` test that feeds a real USGS JSON sample through
Retrofit and the mapper into an in-memory Room database, and a Compose UI test for the
list screen's states.

## 6. Setup required

**Google Maps API key.** The map screens need a key with the Maps SDK for Android
enabled. It is read at build time from `local.properties` (which is git-ignored) or
the `MAPS_API_KEY` environment variable, and injected into the manifest as a
placeholder:

```properties
# local.properties
sdk.dir=/path/to/Android/sdk
MAPS_API_KEY=your_key_here
```

The app builds and runs without a key; the map tiles are simply blank.

**Permissions.** The app declares `INTERNET` and `ACCESS_COARSE_LOCATION`. Location is
requested in-app with an explanation and can be declined.

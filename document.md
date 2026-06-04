# EcoAlert — Dokumentim i Plotë i Projektit

**Lënda:** Programim Mobile  
**Niveli:** Master — Computer Science  
**Platforma:** Android (Java)  
**Versioni i Aplikacionit:** 1.0  
**Package:** `com.programimmobile.ecoalert`

---

## Përmbajtja

1. [Përshkrimi i Projektit](#1-përshkrimi-i-projektit)
2. [Arkitektura MVVM](#2-arkitektura-mvvm)
3. [Konfigurimi i Firebase](#3-konfigurimi-i-firebase)
4. [Struktura e Skedarëve](#4-struktura-e-skedarëve)
5. [Konfigurimi i Projektit (build.gradle / AndroidManifest)](#5-konfigurimi-i-projektit)
6. [Shtresa e Modelit (Model Layer)](#6-shtresa-e-modelit-model-layer)
7. [Shtresa e Aksesit të të Dhënave (Repository Layer)](#7-shtresa-e-aksesit-të-të-dhënave-repository-layer)
8. [Shtresa e Logjikës (ViewModel Layer)](#8-shtresa-e-logjikës-viewmodel-layer)
9. [Sistemi i Autentifikimit](#9-sistemi-i-autentifikimit)
10. [Menaxhimi i Lokacionit](#10-menaxhimi-i-lokacionit)
11. [Aktivitetet (Activities)](#11-aktivitetet-activities)
12. [Fragmentet (Fragments)](#12-fragmentet-fragments)
13. [Adapterat (Adapters)](#13-adapterat-adapters)
14. [Klasat Ndihmëse (Helper Classes)](#14-klasat-ndihmëse-helper-classes)
15. [Utilitetet (Utils)](#15-utilitetet-utils)
16. [Sistemi i Njoftimeve (Notifications)](#16-sistemi-i-njoftimeve-notifications)
17. [Fluksi i të Dhënave (Data Flow)](#17-fluksi-i-të-dhënave-data-flow)
18. [Menaxhimi i Fotografive](#18-menaxhimi-i-fotografive)
19. [Navigimi dhe Rolet e Përdoruesve](#19-navigimi-dhe-rolet-e-përdoruesve)

---

## 1. Përshkrimi i Projektit

**EcoAlert** është një aplikacion Android i ndërtuar me Java që u lejon qytetarëve të raportojnë probleme mjedisore (ndotje, zhurmë, mbetje urbane) në komunitetin e tyre. Aplikacioni mundëson:

- **Raportim** të problemeve mjedisore me foto, lokacion GPS dhe kategori.
- **Vizualizim** të raporteve në hartë interaktive (OpenStreetMap).
- **Konfirmim komunitar** — çdo përdorues mund të konfirmojë raportet e të tjerëve.
- **Administrim** — administratorët mund të aprovojnë, refuzojnë ose t'ia dërgojnë raportet institucioneve.
- **Njoftime** në kohë reale kur raportet ndryshojnë status.

### Teknologjitë e Përdorura

| Teknologji | Versioni | Roli |
|---|---|---|
| Java | 11 | Gjuha e programimit |
| Android SDK | API 23–36 | Platforma |
| Firebase Authentication | BOM 32.7.0 | Autentifikim |
| Cloud Firestore | BOM 32.7.0 | Databazë në kohë reale |
| Firebase Storage | BOM 32.7.0 | Ruajtje skedarësh |
| OSMDroid | 6.1.17 | Harta OpenStreetMap |
| Google Play Location | 21.0.1 | GPS |
| Google Play Auth | 21.0.0 | Google Sign-In |
| AndroidX Lifecycle | 2.7.0 | ViewModel & LiveData |
| Material Design 3 | 1.14.0 | Komponentë UI |
| Glide | 4.16.0 | Ngarkimi i imazheve |
| MPAndroidChart | 3.1.0 | Grafika statistikore |

---

## 2. Arkitektura MVVM

Projekti ndjek modelin arkitekturor **MVVM (Model-View-ViewModel)** të rekomanduar nga Google për aplikacionet Android.

```
┌─────────────────────────────────────────────────────────┐
│                     VIEW LAYER                          │
│  Activities: Splash, Auth, Main, ReportDetail,          │
│              LocationPicker                             │
│  Fragments:  Report, Map, History, Profile,             │
│              Notifications, AdminReports, SentReports   │
└───────────────────────┬─────────────────────────────────┘
                        │ observe (LiveData)
                        ▼
┌─────────────────────────────────────────────────────────┐
│                  VIEWMODEL LAYER                        │
│  AuthViewModel     — gjendja e autentifikimit           │
│  ReportViewModel   — raportet dhe veprimet e tyre       │
└───────────────────────┬─────────────────────────────────┘
                        │ thirrje
                        ▼
┌─────────────────────────────────────────────────────────┐
│                 REPOSITORY LAYER                        │
│  ReportRepository        — CRUD për raportet            │
│  NotificationRepository  — njoftime                     │
│  UserRepository          — role përdoruesish            │
└───────────────────────┬─────────────────────────────────┘
                        │ Firestore SDK
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   DATA LAYER                            │
│  Cloud Firestore (Firebase) — databazë në cloud         │
│  Firebase Authentication   — menaxhim sesionesh         │
└─────────────────────────────────────────────────────────┘
```

### Parimet e MVVM-it në këtë projekt

- **Model** (`Report`, `Notification`, `SentReport`) — klasa POJO që përfaqësojnë të dhënat. Ato nuk kanë logjikë biznesi.
- **ViewModel** (`AuthViewModel`, `ReportViewModel`) — mbajnë gjendjen e UI-t, nuk kanë referencë drejtpërdrejt ndaj Activity/Fragment. Përdorin **LiveData** për komunikim reaktiv.
- **View** (Activities/Fragments) — vetëm shfaqin të dhëna dhe dërgojnë evente te ViewModel. Nuk prekin Firestore drejtpërdrejt.
- **Repository** — shtresë ndërmjetëse mes ViewModel dhe Firebase. Siguron enkapsulim të plotë të logjikës së aksesit të të dhënave.

---

## 3. Konfigurimi i Firebase

### 3.1 Krijimi i Projektit në Firebase Console

Projekti Firebase me të dhënat e mëposhtme:

| Parametri | Vlera |
|---|---|
| Project ID | `ecoalert-emdv` |
| Project Number | `361304586505` |
| Storage Bucket | `ecoalert-emdv.firebasestorage.app` |
| Package Android | `com.programimmobile.ecoalert` |

### 3.2 Skedari `google-services.json`

Ky skedar vendoset në direktorinë `app/` dhe përmban të gjitha konfigurimet e Firebase. Android Gradle Plugin (`com.google.gms.google-services`) e lexon automatikisht gjatë build-it dhe gjeneron burimet e nevojshme.

```
app/
└── google-services.json   ← vendoset këtu
```

Skedari `google-services.json` përmban:
- `project_id` — identifikuesi i projektit Firebase
- `project_number` — numri i projektit për Google Services
- `storage_bucket` — bucket-i i Firebase Storage
- `mobilesdk_app_id` — ID unike e aplikacionit Android
- `api_key` — çelësi API për lidhjen me shërbimet Google
- `oauth_client` — konfigurimet OAuth për Google Sign-In (web client ID)

### 3.3 Aktivizimi i Plugin-it në Gradle

**`build.gradle.kts` (root-level):**
```kotlin
plugins {
    id("com.google.gms.google-services") version "..." apply false
}
```

**`app/build.gradle.kts`:**
```kotlin
plugins {
    id("com.google.gms.google-services")   // aplikon plugin-in
}
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
}
```

**Firebase BoM (Bill of Materials)** — siguron që të gjitha bibliotekat Firebase të jenë në versione të kompatiblueshme me njëra-tjetrën pa pasur nevojë të specifikosh çdo version veç e veç.

### 3.4 Inicializimi i Firebase në Kod — `EcoAlertApp.java`

```java
public class EcoAlertApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Hap 1: Inicializo Firebase
        FirebaseApp.initializeApp(this);

        // Hap 2: Aktivizo cache offline për Firestore
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)   // ruan të dhënat lokalisht
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        // Hap 3: Inicializo OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
    }
}
```

`EcoAlertApp` është klasa e Application-it të Android — ekzekutohet para çdo Activity dhe është vendi ideal për inicializime globale.

**`setPersistenceEnabled(true)`** — aktivizon cache-in offline të Firestore. Kur dispositivi është pa internet, aplikacioni lexon të dhënat nga cache-i lokal dhe sinkronizon automatikisht kur rikthehet lidhja.

### 3.5 Shërbimet Firebase të Aktivizuara

#### Firebase Authentication
Mundëson tre mënyra hyrjeje:
1. **Email/Password** — regjistrim dhe hyrje me email dhe fjalëkalim
2. **Google Sign-In** — hyrje me llogari Google (OAuth 2.0)
3. **Anonymous** — hyrje pa llogari, mbajtja e userId-t unik

#### Cloud Firestore
Databazë NoSQL në kohë reale me 4 koleksione:

```
Firestore Database
├── reports/             ← raportet kryesore
│   └── {reportId}/
│       ├── userId       (string)
│       ├── category     (string)
│       ├── description  (string)
│       ├── latitude     (number)
│       ├── longitude    (number)
│       ├── status       (string: "E re" | "I aprovuar" | "I refuzuar")
│       ├── confirmations (number)
│       ├── photos       (array<string>)  ← Base64
│       └── timestamp    (timestamp)
│
├── notifications/       ← njoftime për përdoruesit
│   └── {notifId}/
│       ├── userId       (string)
│       ├── reportId     (string)
│       ├── reportCategory (string)
│       ├── type         (string: "APPROVED" | "REJECTED" | "SENT")
│       ├── message      (string)
│       ├── isRead       (boolean)
│       └── timestamp    (timestamp)
│
├── admins/              ← lista e adminëve
│   └── {userId}/        ← dokumenti ekziston → user është admin
│
└── sent_reports/        ← rekord raportesh të dërguara te institucione
    └── {sentId}/
        ├── reportId         (string)
        ├── category         (string)
        ├── description      (string)
        ├── institutionEmail (string)
        ├── institutionName  (string)
        ├── latitude         (number)
        ├── longitude        (number)
        ├── adminId          (string)
        └── sentAt           (timestamp)
```

#### Firebase Storage
Përdoret minimalisht — fotografitë ruhen si **Base64** brenda dokumenteve të Firestore (jo si skedarë të veçantë në Storage) për thjeshtësi.

### 3.6 Kontrolli i Rolit Admin

Roli admin kontrollohet duke verifikuar nëse userId-i i përdoruesit ekziston si dokument në koleksionin `admins`:

```java
// UserRepository.java
db.collection("admins")
  .document(userId)
  .get()
  .addOnSuccessListener(doc -> liveData.postValue(doc.exists()));
// doc.exists() == true  → është admin
// doc.exists() == false → është user i rregullt
```

---

## 4. Struktura e Skedarëve

```
Eco-Alert-master/
├── app/
│   ├── google-services.json              ← konfigurim Firebase
│   ├── build.gradle.kts                  ← varësitë e projektit
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml           ← konfigurim aplikacioni
│       └── java/com/programimmobile/ecoalert/
│           ├── EcoAlertApp.java          ← Application class
│           ├── auth/
│           │   └── AuthManager.java      ← logjika e autentifikimit
│           ├── location/
│           │   └── LocationManager.java  ← GPS & geocoding
│           ├── model/
│           │   ├── Report.java           ← entiteti raport
│           │   ├── Notification.java     ← entiteti njoftim
│           │   └── SentReport.java       ← entiteti raport i dërguar
│           ├── repository/
│           │   ├── ReportRepository.java     ← akses Firestore për raportet
│           │   ├── NotificationRepository.java ← akses Firestore njoftime
│           │   └── UserRepository.java        ← kontroll roli
│           ├── ui/
│           │   ├── SplashActivity.java
│           │   ├── AuthActivity.java
│           │   ├── MainActivity.java
│           │   ├── ReportDetailActivity.java
│           │   ├── LocationPickerActivity.java
│           │   ├── ReportFragment.java
│           │   ├── MapFragment.java
│           │   ├── HistoryFragment.java
│           │   ├── ProfileFragment.java
│           │   ├── NotificationsFragment.java
│           │   ├── AdminReportsFragment.java
│           │   ├── SentReportsFragment.java
│           │   ├── ReportAdapter.java
│           │   ├── AdminReportAdapter.java
│           │   ├── DetailPhotoAdapter.java
│           │   ├── PhotoPreviewAdapter.java
│           │   ├── MarkerBitmapHelper.java
│           │   └── ReportInfoWindow.java
│           ├── utils/
│           │   └── AppPreferences.java   ← SharedPreferences wrapper
│           └── viewmodel/
│               ├── AuthViewModel.java
│               └── ReportViewModel.java
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 5. Konfigurimi i Projektit

### 5.1 `app/build.gradle.kts`

```kotlin
android {
    namespace = "com.programimmobile.ecoalert"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.programimmobile.ecoalert"
        minSdk = 23          // Android 6.0 Marshmallow (minimumi)
        targetSdk = 36       // Android 16 (API më i ri)
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

**`minSdk = 23`** — aplikacioni funksionon në të gjitha pajisjet me Android 6.0+, duke mbuluar rreth 98% të tregut.

### 5.2 `AndroidManifest.xml`

#### Lejet e Deklaruara

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="29" />
```

| Leja | Arsyeja |
|---|---|
| `INTERNET` | Komunikimi me Firebase dhe ngarkimi i hartës |
| `ACCESS_FINE_LOCATION` | GPS me saktësi të lartë |
| `ACCESS_COARSE_LOCATION` | GPS me saktësi të ulët (fallback) |
| `ACCESS_NETWORK_STATE` | Kontrollo lidhjen para operacioneve rrjeti |
| `CAMERA` | Fotografim direkt nga kamera |
| `WRITE_EXTERNAL_STORAGE` | Ruajtja e fotove (vetëm API ≤ 29) |

#### Aktivitetet e Regjistruara

```xml
<!-- Pika e hyrjes në aplikacion -->
<activity android:name=".ui.SplashActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity android:name=".ui.AuthActivity" android:exported="false" />
<activity android:name=".ui.MainActivity" android:exported="false" />
<activity android:name=".ui.ReportDetailActivity" android:exported="false" />
<activity android:name=".ui.LocationPickerActivity" android:exported="false" />
```

`android:exported="false"` — aktivitetet nuk mund të hapen nga aplikacione të tjera (siguri).

#### FileProvider për Kamerën

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

FileProvider nevojitet për të krijuar URI të sigurta kur hapim kamerën — Android 7+ nuk lejon `file://` URI drejtpërdrejt, vetëm `content://`.

---

## 6. Shtresa e Modelit (Model Layer)

Modelet janë klasa POJO (Plain Old Java Objects) me annotacione Firestore.

### 6.1 `Report.java`

**Paketa:** `com.programimmobile.ecoalert.model`  
**Roli:** Entiteti kryesor — përfaqëson një raport mjedisore.

#### Fushat

| Fusha | Tipi | Annotacioni Firestore | Përshkrimi |
|---|---|---|---|
| `id` | `String` | `@DocumentId` | ID automatike nga Firestore |
| `userId` | `String` | — | ID e përdoruesit që e ka dërguar |
| `category` | `String` | — | Kategoria e ndotjes |
| `description` | `String` | — | Përshkrimi i problemit |
| `latitude` | `double` | — | Gjerësia gjeografike |
| `longitude` | `double` | — | Gjatësia gjeografike |
| `status` | `String` | — | Statusi aktual i raportit |
| `confirmations` | `int` | — | Sa herë është konfirmuar |
| `photoUrl` | `String` | — | URL foto (legacy, një foto) |
| `photos` | `List<String>` | — | Lista Base64 fotove (të reja) |
| `timestamp` | `Date` | `@ServerTimestamp` | Koha e krijimit (vendoset nga serveri) |

#### Vlerat e `status`

| Vlera | Kuptimi |
|---|---|
| `"E re"` | Raport i sapo dërguar, pret rishikim |
| `"I aprovuar"` | Admin e ka aprovuar |
| `"I refuzuar"` | Admin e ka refuzuar |

#### Konstruktorët

```java
// Konstruktor bosh — i detyrueshëm për deserializimin e Firestore
public Report() {}

// Konstruktor kryesor — përdoret kur krijohet raport i ri
public Report(String userId, String category, String description,
              double latitude, double longitude) {
    this.status       = "E re";    // statusi fillestar gjithmonë "E re"
    this.confirmations = 0;        // fillimisht 0 konfirmime
}
```

**`@DocumentId`** — Firestore shkruan automatikisht ID-në e dokumentit në këtë fushë gjatë deserializimit.

**`@ServerTimestamp`** — kur dokumenti dërgohet, Firebase vendos timestamp-in e serverit (jo të pajisjes lokale), duke siguruar konsistencë.

---

### 6.2 `Notification.java`

**Paketa:** `com.programimmobile.ecoalert.model`  
**Roli:** Entiteti i njoftimit — ruhet kur admin ndryshon statusin e raportit.

#### Konstantet e Tipit

```java
public static final String TYPE_APPROVED = "APPROVED";  // raport i aprovuar
public static final String TYPE_REJECTED = "REJECTED";  // raport i refuzuar
public static final String TYPE_SENT     = "SENT";      // raport dërguar institucionit
```

#### Fushat

| Fusha | Tipi | Annotacioni | Përshkrimi |
|---|---|---|---|
| `id` | `String` | `@DocumentId` | ID automatike |
| `userId` | `String` | — | Kujt i dërgohet njoftimi |
| `reportId` | `String` | — | Raporti i lidhur |
| `reportCategory` | `String` | — | Kategoria (për shfaqje) |
| `type` | `String` | — | Tipi: APPROVED/REJECTED/SENT |
| `message` | `String` | — | Teksti i njoftimit |
| `isRead` | `boolean` | — | A është lexuar (false = i ri) |
| `timestamp` | `Date` | `@ServerTimestamp` | Koha e dërgimit |

#### Konstruktori

```java
public Notification(String userId, String reportId,
                    String reportCategory, String type, String message) {
    this.isRead = false;  // çdo njoftim fillon si i palexuar
}
```

---

### 6.3 `SentReport.java`

**Paketa:** `com.programimmobile.ecoalert.model`  
**Roli:** Rekord auditimi — ruhet kur admin dërgon raportin te një institucion.

#### Fushat

| Fusha | Tipi | Annotacioni | Përshkrimi |
|---|---|---|---|
| `id` | `String` | `@DocumentId` | ID automatike |
| `reportId` | `String` | — | Raporti origjinal |
| `category` | `String` | — | Kategoria e raportit |
| `description` | `String` | — | Përshkrimi |
| `institutionEmail` | `String` | — | Email i institucionit |
| `institutionName` | `String` | — | Emri i institucionit |
| `latitude` | `double` | — | Koordinata |
| `longitude` | `double` | — | Koordinata |
| `adminId` | `String` | — | Admin që e ka dërguar |
| `sentAt` | `Date` | `@ServerTimestamp` | Koha e dërgimit |

---

## 7. Shtresa e Aksesit të të Dhënave (Repository Layer)

### 7.1 `ReportRepository.java`

**Paketa:** `com.programimmobile.ecoalert.repository`  
**Pattern:** Singleton  
**Roli:** I vetmi pikë komunikimi me koleksionin `reports` dhe `sent_reports` në Firestore.

#### Inicializimi (Singleton)

```java
private static ReportRepository instance;

public static ReportRepository getInstance() {
    if (instance == null) {
        instance = new ReportRepository();
    }
    return instance;
}

private ReportRepository() {
    this.db = FirebaseFirestore.getInstance();
    this.allReportsLiveData = new MutableLiveData<>();
    this.errorLiveData = new MutableLiveData<>();
}
```

Singleton-i siguron që ka vetëm **një instancë** të repository-t dhe vetëm **një listener aktiv** Firestore, duke evituar dublikimet e query-ve.

#### Metoda `getAllReports()` — Real-time Listener

```java
public LiveData<List<Report>> getAllReports() {
    startListening();
    return allReportsLiveData;
}

private void startListening() {
    if (listenerRegistration != null) return;  // nëse listener ekziston, mos shto tjetër

    listenerRegistration = db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    errorLiveData.postValue(error.getMessage());
                    return;
                }
                if (snapshots != null) {
                    List<Report> reports = snapshots.toObjects(Report.class);
                    allReportsLiveData.postValue(reports);
                }
            });
}
```

**`addSnapshotListener`** — krijon një lidhje të vazhdueshme me Firestore. Sa herë që ndryshon ndonjë dokument në koleksion, Firestore dërgon automatikisht të dhënat e reja pa pasur nevojë për polling.

**`postValue()`** — njëlloj si `setValue()` por i sigurt për thread-e jo-kryesore.

#### Metoda `stopListening()` — Pastrimi i Burimeve

```java
public void stopListening() {
    if (listenerRegistration != null) {
        listenerRegistration.remove();  // heq listener-in nga Firestore
        listenerRegistration = null;
    }
}
```

Thirret nga `ReportViewModel.onCleared()` kur ViewModel shkatërrohet, duke evituar rrjedhje memorie (memory leak).

#### Metoda `addReport()`

```java
public void addReport(Report report, ActionCallback callback) {
    db.collection("reports")
            .add(report)  // Firestore gjeneron ID automatikisht
            .addOnSuccessListener(ref -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
}
```

#### Metoda `deleteReport()`

```java
public void deleteReport(String reportId, ActionCallback callback) {
    db.collection("reports")
            .document(reportId)
            .delete()
            .addOnSuccessListener(unused -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
}
```

#### Metoda `confirmReport()` — Inkrement Atomic

```java
public void confirmReport(String reportId, ActionCallback callback) {
    db.collection("reports")
            .document(reportId)
            .update("confirmations", FieldValue.increment(1))
            // FieldValue.increment() bën inkrement atomic në server
            // pa race condition edhe me shumë përdorues njëkohësisht
```

**`FieldValue.increment(1)`** — operacion atomic i Firestore. Ndryshe nga `{confirmations: confirmations + 1}`, kjo operacion ndodh direkt në server, duke shmangur konflikte kur shumë përdorues konfirmojnë njëkohësisht.

#### Metoda `getReportsByUser()` — Query me Filter

```java
public void getReportsByUser(String userId, MutableLiveData<List<Report>> liveData) {
    db.collection("reports")
            .whereEqualTo("userId", userId)           // filter: vetëm raportet e këtij user-i
            .orderBy("timestamp", Query.Direction.DESCENDING)  // renditje: të rejat sipër
            .addSnapshotListener((snapshots, error) -> { ... });
}
```

#### Metoda `getReportsByCategory()` — Filter Kategorie

```java
public void getReportsByCategory(String category, MutableLiveData<List<Report>> liveData) {
    db.collection("reports")
            .whereEqualTo("category", category)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> { ... });
}
```

#### Metoda `getReportById()` — Get Dokumenti Specifik

```java
public void getReportById(String reportId, MutableLiveData<Report> liveData) {
    db.collection("reports")
            .document(reportId)
            .get()  // one-time fetch (jo real-time listener)
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Report report = documentSnapshot.toObject(Report.class);
                    liveData.postValue(report);
                }
            });
}
```

#### Metoda `updateReportStatus()`

```java
public void updateReportStatus(String reportId, String status, ActionCallback callback) {
    db.collection("reports")
            .document(reportId)
            .update("status", status)  // azhuro vetëm fushën "status"
            .addOnSuccessListener(unused -> callback.onSuccess());
}
```

#### Metoda `saveSentReport()` dhe `getSentReports()`

```java
// Ruan rekord auditimi kur admin dërgon raport te institucion
public void saveSentReport(SentReport sentReport, ActionCallback callback) {
    db.collection("sent_reports")
            .add(sentReport)
            .addOnSuccessListener(ref -> callback.onSuccess());
}

// Merr historikun e raporteve të dërguara nga ky admin
public void getSentReports(String adminId, MutableLiveData<List<SentReport>> liveData) {
    db.collection("sent_reports")
            .whereEqualTo("adminId", adminId)
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .addSnapshotListener(...);
}
```

#### Interface `ActionCallback`

```java
public interface ActionCallback {
    void onSuccess();
    void onFailure(String errorMessage);
}
```

Kjo interface e thjeshtë përdoret si callback për të gjitha operacionet write (shto, fshi, azhuro).

---

### 7.2 `NotificationRepository.java`

**Paketa:** `com.programimmobile.ecoalert.repository`  
**Pattern:** Singleton  
**Roli:** Menaxhon koleksionin `notifications` në Firestore.

#### Metoda `sendNotification()`

```java
public void sendNotification(Notification notification,
                             ReportRepository.ActionCallback callback) {
    db.collection("notifications")
            .add(notification)
            .addOnSuccessListener(ref -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
}
```

Thirret automatikisht nga `ReportViewModel.approveReport()` dhe `ReportViewModel.rejectReport()`.

#### Metoda `getUserNotifications()`

```java
public void getUserNotifications(String userId,
                                 MutableLiveData<List<Notification>> liveData) {
    db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                List<Notification> list = snapshots.toObjects(Notification.class);
                liveData.postValue(list);
            });
}
```

Listener real-time — çdo njoftim i ri shfaqet menjëherë pa reload.

#### Metoda `getUnreadCount()`

```java
public void getUnreadCount(String userId, MutableLiveData<Integer> liveData) {
    db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)   // vetëm ato të palexuara
            .addSnapshotListener((snapshots, error) -> {
                liveData.postValue(snapshots.size());  // numri i dokumenteve
            });
}
```

#### Metoda `markAllAsRead()`

```java
public void markAllAsRead(String userId) {
    db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener(snapshots -> {
                for (var doc : snapshots.getDocuments()) {
                    doc.getReference().update("isRead", true);
                }
            });
}
```

Kjo metodë bën **batch update** — merr të gjitha njoftime të palexuara dhe i shënon si të lexuara njëra pas tjetrës.

---

### 7.3 `UserRepository.java`

**Paketa:** `com.programimmobile.ecoalert.repository`  
**Pattern:** Singleton  
**Roli:** Kontrollon nëse një përdorues është admin.

#### Metoda `checkIsAdmin()`

```java
public void checkIsAdmin(String userId, MutableLiveData<Boolean> liveData) {
    db.collection("admins")
            .document(userId)
            .get()
            .addOnSuccessListener(doc -> liveData.postValue(doc.exists()))
            // doc.exists() == true  → dokumenti me këtë userId ekziston → është admin
            // doc.exists() == false → dokumenti nuk ekziston → user i rregullt
            .addOnFailureListener(e -> liveData.postValue(false));
}
```

Logjika: nëse userId-i ekziston si dokument në koleksionin `admins`, atëherë përdoruesi është admin. Kjo është mënyra më e thjeshtë dhe e sigurt për role-based access control me Firestore.

---

## 8. Shtresa e Logjikës (ViewModel Layer)

### 8.1 `AuthViewModel.java`

**Paketa:** `com.programimmobile.ecoalert.viewmodel`  
**Extends:** `AndroidViewModel` (ka akses te Application context)  
**Roli:** Menaxhon gjendjen e autentifikimit dhe ekspozon LiveData për UI.

#### Fushat LiveData

```java
private final MutableLiveData<FirebaseUser> currentUserLiveData;  // përdoruesi aktual
private final MutableLiveData<String>       errorLiveData;        // mesazhe gabimi
private final MutableLiveData<Boolean>      loadingLiveData;      // gjendja loading
```

#### Konstruktori

```java
public AuthViewModel(@NonNull Application application) {
    super(application);
    this.authManager = new AuthManager(application);

    // Nëse ka sesion aktiv, vendose menjëherë në LiveData
    if (authManager.isLoggedIn()) {
        currentUserLiveData.setValue(authManager.getCurrentUser());
    }
}
```

#### Metodat Kryesore

**`signInAnonymously()`** — hyrje anonime

```java
public void signInAnonymously() {
    loadingLiveData.setValue(true);
    authManager.signInAnonymously(new AuthManager.AuthCallback() {
        @Override
        public void onSuccess(FirebaseUser user) {
            loadingLiveData.postValue(false);
            currentUserLiveData.postValue(user);  // UI reagon automatikisht
        }
        @Override
        public void onFailure(String errorMessage) {
            loadingLiveData.postValue(false);
            errorLiveData.postValue(errorMessage);
        }
    });
}
```

**`registerWithEmail(email, password)`** — regjistrim me email

```java
public void registerWithEmail(String email, String password) {
    if (!validateEmailPassword(email, password)) return;  // validim para thirrjes
    loadingLiveData.setValue(true);
    authManager.registerWithEmail(email, password, callback);
}
```

**`signInWithEmail(email, password)`** — hyrje me email

**`handleGoogleSignInResult(data)`** — procesim i rezultatit të Google Sign-In

**`linkWithEmail(email, password)`** — upgrade llogari anonime → email

```java
public void linkWithEmail(String email, String password) {
    if (!validateEmailPassword(email, password)) return;
    loadingLiveData.setValue(true);
    authManager.linkAnonymousWithEmail(email, password, new AuthManager.LinkCallback() {
        @Override
        public void onSuccess(FirebaseUser user, boolean wasAnonymous) {
            // userId mbetet i njëjtë → raportet ruhen
            currentUserLiveData.postValue(user);
        }
    });
}
```

**`resetPassword(email)`** — dërgon email reset fjalëkalimi

**`signOut(activity)`** — del nga llogaria dhe paston sesionin

**`validateEmailPassword(email, password)`** — validim lokal (private)

```java
private boolean validateEmailPassword(String email, String password) {
    if (email == null || email.trim().isEmpty()) {
        errorLiveData.setValue("Email-i nuk mund të jetë bosh.");
        return false;
    }
    if (password == null || password.length() < 6) {
        errorLiveData.setValue("Fjalëkalimi duhet të ketë të paktën 6 karaktere.");
        return false;
    }
    return true;
}
```

---

### 8.2 `ReportViewModel.java`

**Paketa:** `com.programimmobile.ecoalert.viewmodel`  
**Extends:** `AndroidViewModel`  
**Roli:** Menaxhon të gjitha operacionet e raporteve.

#### Fushat LiveData

```java
private final MutableLiveData<String>       errorLiveData;
private final MutableLiveData<Boolean>      loadingLiveData;
private final MutableLiveData<Boolean>      reportSubmittedLiveData;  // sinjal pas dërgimit
private final MutableLiveData<List<Report>> userReportsLiveData;
private final MutableLiveData<List<SentReport>> sentReportsLiveData;
```

#### Metodat Kryesore

**`getAllReports()`** — të gjitha raportet (real-time, për hartën)

**`getUserReports(userId)`** — raportet e një përdoruesi (për historikun)

**`getReportsByCategory(category)`** — raportet sipas kategorisë (për filtrim në hartë)

**`addReport(report)`** — dërgon raport të ri te Firestore

```java
public void addReport(Report report) {
    loadingLiveData.setValue(true);
    repository.addReport(report, new ReportRepository.ActionCallback() {
        @Override
        public void onSuccess() {
            loadingLiveData.postValue(false);
            reportSubmittedLiveData.postValue(true);  // sinjalizon UI-n për sukses
        }
    });
}
```

**`deleteReport(reportId)`** — fshin raport

**`confirmReport(reportId)`** — inkrement konfirmimesh (atomic)

**`approveReport(report)`** — miraton raport + dërgon njoftim

```java
public void approveReport(Report report) {
    // Hap 1: Ndrysho status → "I aprovuar"
    repository.updateReportStatus(report.getId(), "I aprovuar", new ActionCallback() {
        @Override
        public void onSuccess() {
            // Hap 2: Krijo dhe dërgo njoftim te përdoruesi
            Notification notification = new Notification(
                    report.getUserId(),
                    report.getId(),
                    report.getCategory(),
                    Notification.TYPE_APPROVED,
                    "Raporti juaj për '" + report.getCategory()
                            + "' u aprovua nga admini. Faleminderit!"
            );
            notificationRepository.sendNotification(notification, ...);
        }
    });
}
```

**`rejectReport(report, reason)`** — refuzon raport + dërgon njoftim me arsye

```java
public void rejectReport(Report report, String reason) {
    repository.updateReportStatus(report.getId(), "I refuzuar", new ActionCallback() {
        @Override
        public void onSuccess() {
            String message = "Raporti juaj për '" + report.getCategory() + "' u refuzua."
                    + (reason != null && !reason.isEmpty() ? " Arsyeja: " + reason : "");
            Notification notification = new Notification(..., Notification.TYPE_REJECTED, message);
            notificationRepository.sendNotification(notification, ...);
        }
    });
}
```

**`getSentReports(adminId)`** — raportet e dërguara nga ky admin

**`saveSentReport(sentReport)`** — ruan rekord auditimi

**`resetSubmittedStatus()`** — rivendos sinjalizatorin pas dërgimit (shmang re-trigger)

**`onCleared()`** — thirret kur ViewModel shkatërrohet, heq listener-in nga Firestore

```java
@Override
protected void onCleared() {
    super.onCleared();
    repository.stopListening();  // evitan memory leak
}
```

---

## 9. Sistemi i Autentifikimit

### 9.1 `AuthManager.java`

**Paketa:** `com.programimmobile.ecoalert.auth`  
**Roli:** Klasa e vetme që komunikon me Firebase Authentication.

#### Interfaces të Definuara

```java
// Callback i thjeshtë për hyrje/regjistrim
public interface AuthCallback {
    void onSuccess(FirebaseUser user);
    void onFailure(String errorMessage);
}

// Callback për lidhjen e llogarisë anonime
// wasAnonymous = true → shpjegon UI-t që u bë upgrade
public interface LinkCallback {
    void onSuccess(FirebaseUser user, boolean wasAnonymous);
    void onFailure(String errorMessage);
}
```

#### Inicializimi

```java
public AuthManager(Context context) {
    this.firebaseAuth = FirebaseAuth.getInstance();

    // Konfiguro Google Sign-In me web client ID nga google-services.json
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
    this.googleSignInClient = GoogleSignIn.getClient(context, gso);
}
```

**`default_web_client_id`** — gjenerohet automatikisht nga plugini `google-services` bazuar në `google-services.json`.

#### Metodat e Kontrollit të Sesionit

| Metoda | Kthon | Përshkrimi |
|---|---|---|
| `isLoggedIn()` | `boolean` | A ka sesion aktiv |
| `isAnonymous()` | `boolean` | A është llogari anonime |
| `getCurrentUser()` | `FirebaseUser` | Objekti i përdoruesit aktual |
| `getCurrentUserId()` | `String` | UID unik i Firestore |

#### Hyrja Anonime

```java
public void signInAnonymously(AuthCallback callback) {
    firebaseAuth.signInAnonymously()
            .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
}
```

Firebase krijon një UID unik edhe për llogaritë anonime. Ky UID mbetet i njëjtë brenda sesionit dhe mund të lidhet me llogari të plotë më vonë.

#### Hyrja me Google

```java
// Hap 1: Merr Intent-in për ekranin e Google Sign-In
public Intent getGoogleSignInIntent() {
    return googleSignInClient.getSignInIntent();
}

// Hap 2: Proceso rezultatin e kthyer nga Google
public void handleGoogleSignInResult(Intent data, AuthCallback callback) {
    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
    try {
        GoogleSignInAccount account = task.getResult(ApiException.class);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        if (isAnonymous()) {
            // Nëse është anonim, bëj upgrade me ruajtje të userId-t
            linkAnonymousWithCredential(credential, callback);
        } else {
            // Hyrje normale me Google
            firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener(result -> callback.onSuccess(result.getUser()));
        }
    } catch (ApiException e) {
        callback.onFailure("Google Sign-In dështoi: " + e.getMessage());
    }
}
```

#### Upgrade: Llogari Anonime → Email

```java
public void linkAnonymousWithEmail(String email, String password, LinkCallback callback) {
    FirebaseUser user = firebaseAuth.getCurrentUser();
    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
    user.linkWithCredential(credential)
            .addOnSuccessListener(result -> callback.onSuccess(result.getUser(), true))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
}
```

**Rëndësia e `linkWithCredential()`**: Kur lidhet llogaria anonime me email, Firebase **ruan të njëjtin UID**. Kjo do të thotë që të gjitha raportet e dërguara si anonim vazhdojnë të jenë të lidhura me llogarinë e re — nuk humbasin.

#### Dalja

```java
public void signOut(Activity activity) {
    firebaseAuth.signOut();           // del nga Firebase Auth
    googleSignInClient.signOut();     // del nga Google Auth (pastron tokenin)
}
```

---

## 10. Menaxhimi i Lokacionit

### 10.1 `LocationManager.java`

**Paketa:** `com.programimmobile.ecoalert.location`  
**Roli:** Encapsulon të gjitha operacionet GPS dhe geocoding.

#### Interfaces

```java
// Callback kur lokacioni merret me sukses ose me gabim
public interface LocationCallback2 {
    void onLocationReceived(double latitude, double longitude);
    void onLocationError(String errorMessage);
}

// Callback kur adresa konvertohet nga koordinata
public interface AddressCallback {
    void onAddressReceived(String address);
    void onAddressError();
}
```

#### Inicializimi

```java
public LocationManager(Context context) {
    this.context = context;
    this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
}
```

**`FusedLocationProviderClient`** — Google Play Services ofron lokacionin duke kombinuar GPS, Wi-Fi dhe rrjet celular për të dhënë lokacionin më të saktë dhe efiçient.

#### Metoda `getCurrentLocation()` — Marrja e Lokacionit

```java
public void getCurrentLocation(LocationCallback2 callback) {
    // Kontroll leje para çdo operacioni
    if (!hasLocationPermission()) {
        callback.onLocationError("Leja e lokacionit nuk është dhënë.");
        return;
    }

    // Tentativa 1: Last Known Location (i shpejtë, nga cache)
    fusedLocationClient.getLastLocation()
            .addOnSuccessListener(location -> {
                if (location != null) {
                    callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                } else {
                    // Tentativa 2: Kërko lokacion të ri (i ngadalshëm, por i saktë)
                    requestFreshLocation(callback);
                }
            });
}
```

**Strategjia dyshtë**: fillimisht provon `getLastLocation()` (cache i GPS — i menjëhershëm) dhe nëse është null, kërkon lexim të ri me `requestLocationUpdates()`.

#### Metoda `requestFreshLocation()` — Lexim i Ri

```java
private void requestFreshLocation(LocationCallback2 callback) {
    LocationRequest locationRequest = new LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000)  // interval: 5 sekonda
            .setMinUpdateIntervalMillis(2000)        // min interval: 2 sekonda
            .setMaxUpdates(1)                        // vetëm 1 përditësim, pastaj ndalon
            .build();

    locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                callback.onLocationReceived(lat, lng);
                stopLocationUpdates();  // heq listener pas marrjes
            }
        }
    };

    fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback, Looper.getMainLooper());
}
```

**`Priority.PRIORITY_HIGH_ACCURACY`** — përdor GPS të plotë (konsumon më shumë bateri por jep saktësi të lartë).  
**`setMaxUpdates(1)`** — merr vetëm një lokacion dhe ndalon (one-shot), nuk mbetet aktiv.

#### Metoda `getAddressFromCoordinates()` — Geocoding i Anasjelltë

```java
public void getAddressFromCoordinates(double lat, double lng, AddressCallback callback) {
    new Thread(() -> {  // operacion IO → thread tjetër (nuk bllokon UI)
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            StringBuilder sb = new StringBuilder();
            if (address.getThoroughfare() != null) sb.append(address.getThoroughfare());
            if (address.getLocality() != null) sb.append(", ").append(address.getLocality());
            callback.onAddressReceived(sb.toString());  // p.sh.: "Rr. Dëshmorët, Tiranë"
        } else {
            callback.onAddressError();  // kthehet te koordinatat numerike
        }
    }).start();
}
```

Geocoding bëhet në **thread tjetër** (`new Thread()`) sepse është operacion rrjeti/IO dhe do të bllokonte UI thread nëse bëhej direkt.

---

## 11. Aktivitetet (Activities)

### 11.1 `SplashActivity.java`

**Layout:** `activity_splash.xml`  
**Roli:** Ekrani i nisjes — zgjedh destinacionin e parë.

#### Logjika e Navigimit

```java
new Handler(Looper.getMainLooper()).postDelayed(() -> {
    if (authViewModel.isLoggedIn() && appPreferences.isAuthCompleted()) {
        goToMain();    // sesion aktiv + zgjedhje e qëllimshme → hyr direkt
    } else {
        goToAuth();    // jo sesion ose sesion anonim pa zgjedhje → shfaq auth
    }
}, 2000);  // 2 sekonda vonë (shfaq logon)
```

**`isAuthCompleted()`** — kontrollon nëse përdoruesi ka bërë zgjedhje të qëllimshme (jo vetëm sesion automatik anonim). Kjo shmang rastin kur aplikacioni kalon te MainActivity me sesion anonim të padëshiruar.

---

### 11.2 `AuthActivity.java`

**Layout:** `activity_auth.xml`  
**Roli:** Ekrani i autentifikimit me tab Login/Register.

#### Komponentët UI

| Komponenti | ID | Roli |
|---|---|---|
| `TabLayout` | `tab_layout` | Ndërron mes Login/Register |
| `TextInputEditText` | `et_email` | Fusha e email-it |
| `TextInputEditText` | `et_password` | Fusha e fjalëkalimit |
| `TextInputLayout` | `til_email` | Container email (shfaq gabime) |
| `TextInputLayout` | `til_password` | Container fjalëkalim |
| `MaterialButton` | `btn_auth` | Butoni Hyr/Regjistrohu |
| `MaterialButton` | `btn_anonymous` | Butoni "Vazhdo pa llogari" |
| `TextView` | `tv_forgot_password` | Linku "Harrova fjalëkalimin" |
| `ProgressBar` | `progress_bar` | Indikator ngarkimi |

#### Metoda `initViews()`
Inicializon të gjitha referencat e view-ve duke përdorur `findViewById()`.

#### Metoda `setupViewModel()`
Lidhja me LiveData:

```java
// Kur user-i logohet → kalo te MainActivity
authViewModel.getCurrentUser().observe(this, user -> {
    if (user != null) {
        appPreferences.setAuthCompleted(true);  // shëno zgjedhjen e qëllimshme
        goToMain();
    }
});

// Kur ka gabim → shfaq Toast
authViewModel.getError().observe(this, error -> {
    if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
});

// Kur loading → disable butona, shfaq progress
authViewModel.getLoading().observe(this, isLoading -> {
    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    btnAuth.setEnabled(!isLoading);
    btnAnonymous.setEnabled(!isLoading);
});
```

#### Metoda `setupTabLayout()`
Ndryshon tekstin e butonit dhe vizibilitetin e linkut "Harrova fjalëkalimin" bazuar në tab-in aktiv.

#### Metoda `setupClickListeners()`
- **btnAuth** → nëse `isLoginMode` → `signInWithEmail()`, tjetër → `registerWithEmail()`
- **btnAnonymous** → `signInAnonymously()`
- **tvForgotPassword** → `resetPassword()` nëse email-i është i plotësuar

#### Metoda `goToMain()`

```java
private void goToMain() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    // FLAG_ACTIVITY_CLEAR_TASK: fshin back stack — nuk mund të kthehet te AuthActivity
    startActivity(intent);
    finish();
}
```

---

### 11.3 `MainActivity.java`

**Layout:** `activity_main.xml`  
**Roli:** Konteineri kryesor i aplikacionit — menaxhon fragmentet dhe navigimin.

#### Komponentët

| Komponenti | ID | Roli |
|---|---|---|
| `MaterialToolbar` | `toolbar` | Shiriti i sipërm me menu |
| `BottomNavigationView` | `bottom_navigation` | Navigimi i poshtëm |
| `FrameLayout` | `fragment_container` | Zona e fragmenteve |

#### Metoda `checkRoleAndSetup(savedInstanceState)`

```java
private void checkRoleAndSetup(Bundle savedInstanceState) {
    String userId = authViewModel.getCurrentUserId();
    MutableLiveData<Boolean> isAdminLiveData = new MutableLiveData<>();
    UserRepository.getInstance().checkIsAdmin(userId, isAdminLiveData);

    isAdminLiveData.observe(this, admin -> {
        isAdmin = Boolean.TRUE.equals(admin);
        setupBottomNavigation();          // menu sipas rolit
        updateToolbarSubtitle();          // shfaq "Admin" ose "Anonim"
        if (savedInstanceState == null) {
            // Fragmenti fillestar sipas rolit
            if (isAdmin) {
                loadFragment(new AdminReportsFragment());
            } else {
                loadFragment(new ReportFragment());
            }
        }
    });
}
```

#### Metoda `setupBottomNavigation()`

Ndryshon menunë dinamikisht sipas rolit:
- **User:** `R.menu.bottom_nav_menu` (Raport, Harta, Historia, Njoftime, Profili)
- **Admin:** `R.menu.bottom_nav_admin` (Raportet, Harta, Dërguar, Profili)

#### Metoda `showProfileMenu()`

Shfaq dialog të ndryshme sipas llogarisë:
- **Anonim**: paralajmëron se raportet do humbasin nëse del pa llogari, ofron opsionin "Krijo Llogari"
- **Me llogari**: ofron "Shiko Profilin" dhe "Dil nga llogaria"

#### Metoda `loadFragment(fragment)`

```java
public void loadFragment(Fragment fragment) {
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
}
```

Metoda publike — mund të thirret edhe nga fragment-et për navigim manual.

---

### 11.4 `ReportDetailActivity.java`

**Layout:** `activity_report_detail.xml`  
**Roli:** Shfaq detajet e plota të një raporti.

#### Komponentët Kryesor

- `RecyclerView` me `DetailPhotoAdapter` — karusel fotografish horizontal
- `TextView`-të për kategori, përshkrim, lokacion, konfirmime, status, datë
- `MapView` (OSMDroid) — mini-hartë me marker të lokacionit
- `MaterialButton` btnDelete — i dukshëm vetëm për pronarin
- `MaterialButton` btnConfirm — konfirmim komunitar
- `Chip` statusChip — shfaq statusin me ngjyrë

#### Logjika Kryesore

```java
// Fshirja — vetëm pronari i raportit
if (report.getUserId().equals(currentUserId)) {
    btnDelete.setVisibility(View.VISIBLE);
    btnDelete.setOnClickListener(v -> {
        new AlertDialog.Builder(this)
                .setTitle("Fshi raportin?")
                .setPositiveButton("Fshi", (d, w) ->
                        reportViewModel.deleteReport(report.getId()))
                .show();
    });
}

// Konfirmimi
btnConfirm.setOnClickListener(v -> reportViewModel.confirmReport(report.getId()));
```

---

### 11.5 `LocationPickerActivity.java`

**Layout:** `activity_location_picker.xml`  
**Roli:** Hartë interaktive ku përdoruesi zgjedh lokacionin me klik.

#### Konstante (Extra keys)

```java
public static final String EXTRA_LATITUDE  = "latitude";
public static final String EXTRA_LONGITUDE = "longitude";
public static final String EXTRA_ADDRESS   = "address";
public static final String EXTRA_INIT_LAT  = "init_lat";
public static final String EXTRA_INIT_LNG  = "init_lng";
```

#### Logjika Kryesore

```java
// Vendos marker ku klikon përdoruesi
mapView.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        selectedLat = p.getLatitude();
        selectedLng = p.getLongitude();
        updateMarker(p);
        getAddressForPoint(p);  // reverse geocoding
        return true;
    }
}));

// Konfirmim — kthe lokacionin te ReportFragment
btnConfirm.setOnClickListener(v -> {
    Intent result = new Intent();
    result.putExtra(EXTRA_LATITUDE, selectedLat);
    result.putExtra(EXTRA_LONGITUDE, selectedLng);
    result.putExtra(EXTRA_ADDRESS, currentAddress);
    setResult(RESULT_OK, result);
    finish();
});
```

---

## 12. Fragmentet (Fragments)

### 12.1 `ReportFragment.java`

**Layout:** `fragment_report.xml`  
**Roli:** Forma e plotë e krijimit dhe dërgimit të një raporti.

#### Konstantet

```java
private static final int MAX_PHOTOS = 3;  // maksimumi i fotove të lejuara
```

#### Kategoritë e Disponueshme

```java
private final String[] categories = {
    "Mbetje Urbane",   // mbetje, hedhurina
    "Zhurmë",          // ndotje akustike
    "Ndotje Ajri",     // tym, erë kimike
    "Ndotje Uji",      // lumenjtë, ujërat
    "Tjetër"           // probleme të tjera
};
```

#### ActivityResultLauncher-ët

Fragmenti regjistron 5 launcher-ë për operacione async:

| Launcher | Qëllimi |
|---|---|
| `locationPermissionLauncher` | Kërkon lejet GPS |
| `cameraPermissionLauncher` | Kërkon lejen e kamerës |
| `cameraLauncher` | Hap kamerën dhe merr foton |
| `galleryLauncher` | Hap galerinë dhe merr foton |
| `locationPickerLauncher` | Hap LocationPickerActivity |

#### Metoda `onViewCreated()`

```java
public void onViewCreated(View view, Bundle savedInstanceState) {
    initViews(view);          // referencat e view-ve
    setupViewModels();        // lidhja me ViewModel + observimi LiveData
    setupCategorySpinner();   // popullo dropdown-in e kategorive
    setupClickListeners();    // degjuesit e butonave
    checkAndGetLocation();    // merr lokacionin automatikisht
}
```

#### Metoda `submitReport()` — Dërgimi i Raportit

```java
private void submitReport() {
    // Validim: lokacioni është i detyrueshëm
    if (!locationObtained || (currentLatitude == 0.0 && currentLongitude == 0.0)) {
        Toast.makeText(requireContext(), "Lokacioni është i detyrueshëm.", ...).show();
        return;
    }

    // Konverto fotografitë në Base64 në thread tjetër (evito UI freeze)
    new Thread(() -> {
        List<String> base64Photos = new ArrayList<>();
        for (int i = 0; i < llPhotos.getChildCount(); i++) {
            // Nxjerr bitmap nga FrameLayout → konverto → shto në listë
            ImageView iv = (ImageView) ((FrameLayout) llPhotos.getChildAt(i)).getChildAt(0);
            Uri uri = (Uri) iv.getTag();
            Bitmap bmp = loadBitmapFromUri(uri);
            if (bmp != null) base64Photos.add(bitmapToBase64(bmp));
        }

        // Kthehu te UI thread për dërgim
        requireActivity().runOnUiThread(() -> {
            Report report = new Report(userId, category, description, lat, lng);
            if (!base64Photos.isEmpty()) report.setPhotos(base64Photos);
            reportViewModel.addReport(report);
        });
    }).start();
}
```

#### Metoda `bitmapToBase64()` — Kompresim i Fotografisë

```java
private String bitmapToBase64(Bitmap bitmap) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);  // cilësi 25%
    byte[] bytes = baos.toByteArray();

    if (bytes.length > 250000) {  // nëse > 250KB
        baos.reset();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 15, baos);  // ulje te 15%
        bytes = baos.toByteArray();
    }
    return Base64.encodeToString(bytes, Base64.DEFAULT);
}
```

Kompresimi agresiv (25%, 15%) zvogëlon madhësinë e fotove për ta ruajtur brenda kufijve të Firestore.

---

### 12.2 `MapFragment.java`

**Layout:** `fragment_map.xml`  
**Roli:** Hartë interaktive me të gjitha raportet e shënuara.

#### Logjika Kryesore

```java
// Ngarko të gjitha raportet (real-time)
reportViewModel.getAllReports().observe(getViewLifecycleOwner(), reports -> {
    mapView.getOverlays().clear();  // pastroj markerët e vjetër
    for (Report report : reports) {
        if (matchesFilter(report)) {  // apliko filtrin aktiv
            Marker marker = createMarker(report);
            mapView.getOverlays().add(marker);
        }
    }
    mapView.invalidate();  // rifreskio vizualizimin
});
```

#### Filtrimi me Chip-et

```java
// Chip "Të gjitha" → shfaq të gjitha
// Chip "Mbetje Urbane" → filtro vetëm atë kategori
chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
    String selectedCategory = getSelectedCategory(checkedIds);
    refreshMarkersWithFilter(selectedCategory);
});
```

#### Markerat me Ngjyra dhe Madhësi Dinamike

```java
private Marker createMarker(Report report) {
    Bitmap bitmap = MarkerBitmapHelper.createMarkerBitmap(
            requireContext(),
            report.getCategory(),
            report.getConfirmations()  // madhësia rritet me konfirmimet
    );
    Marker marker = new Marker(mapView);
    marker.setIcon(new BitmapDrawable(getResources(), bitmap));
    marker.setPosition(new GeoPoint(report.getLatitude(), report.getLongitude()));
    marker.setInfoWindow(new ReportInfoWindow(mapView, report, this));
    return marker;
}
```

---

### 12.3 `HistoryFragment.java`

**Layout:** `fragment_history.xml`  
**Roli:** Lista e raporteve të dërguara nga përdoruesi i loguar.

#### Logjika Kryesore

```java
@Override
public void onResume() {
    super.onResume();
    // Rifresko çdo herë që fragmenti bëhet i dukshëm
    loadReports();
}

private void loadReports() {
    String userId = authViewModel.getCurrentUserId();
    reportViewModel.getUserReports(userId).observe(getViewLifecycleOwner(), reports -> {
        adapter.setReports(reports);
        emptyState.setVisibility(reports.isEmpty() ? View.VISIBLE : View.GONE);
    });
}
```

#### Swipe-to-Delete

```java
ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
    new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            Report report = adapter.getReportAt(pos);
            // Dialog konfirmimi
            new AlertDialog.Builder(requireContext())
                    .setTitle("Fshi raportin?")
                    .setPositiveButton("Fshi", (d, w) ->
                            reportViewModel.deleteReport(report.getId()))
                    .setNegativeButton("Anulo", (d, w) ->
                            adapter.notifyItemChanged(pos))  // rivendos item-in
                    .show();
        }
    }
);
itemTouchHelper.attachToRecyclerView(recyclerView);
```

---

### 12.4 `ProfileFragment.java`

**Layout:** `fragment_profile.xml`  
**Roli:** Profili i përdoruesit me statistika.

#### Funksionaliteti

- Shfaq emrin/email-in dhe tipin e llogarisë (email, Google, Anonim)
- Statistika: numri total i raporteve dhe konfirmimeve
- Për llogaritë anonime: buton "Krijo Llogari" (upgrade)
- Dialog "Rreth aplikacionit"

#### Metoda e Upgrade-it (Anonim → Email)

```java
private void showUpgradeDialog() {
    View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_upgrade, null);
    // ... merr email + fjalëkalim
    authViewModel.linkWithEmail(email, password);
}
```

---

### 12.5 `NotificationsFragment.java`

**Layout:** `fragment_notifications.xml`  
**Roli:** Lista e njoftimeve të përdoruesit.

#### Logjika Kryesore

```java
@Override
public void onResume() {
    super.onResume();
    // Shëno si të lexuara kur fragment-i hapet
    notificationRepository.markAllAsRead(userId);
    loadNotifications();
}

private void loadNotifications() {
    notificationRepository.getUserNotifications(userId, notificationsLiveData);
    notificationsLiveData.observe(getViewLifecycleOwner(), notifications -> {
        adapter.setNotifications(notifications);
        emptyState.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
    });
}
```

Çdo njoftim shfaq ikonë sipas tipit (✓ aprovuar, ✗ refuzuar, → dërguar) dhe kohën relative.

---

### 12.6 `AdminReportsFragment.java`

**Layout:** `fragment_admin_reports.xml`  
**Roli:** Paneli i administrimit — shfaq të gjitha raportet me veprime.

#### Filtrimet e Statusit

```java
// Chip-et: Të gjitha | E re | I aprovuar | I refuzuar
chipAll.setOnClickListener(v -> filterByStatus(null));        // të gjitha
chipNew.setOnClickListener(v -> filterByStatus("E re"));
chipApproved.setOnClickListener(v -> filterByStatus("I aprovuar"));
chipRejected.setOnClickListener(v -> filterByStatus("I refuzuar"));
```

#### Veprimet e Adminit

**Aprovimi:**
```java
// AdminReportAdapter dërgon event te fragment
adapter.setOnApproveListener(report -> reportViewModel.approveReport(report));
```

**Refuzimi me arsye:**
```java
adapter.setOnRejectListener(report -> showRejectDialog(report));

private void showRejectDialog(Report report) {
    View dialogView = inflate(R.layout.dialog_reject_reason);
    EditText etReason = dialogView.findViewById(R.id.et_reason);
    new AlertDialog.Builder(requireContext())
            .setTitle("Arsyeja e refuzimit")
            .setView(dialogView)
            .setPositiveButton("Refuzo", (d, w) -> {
                String reason = etReason.getText().toString().trim();
                reportViewModel.rejectReport(report, reason);
            })
            .show();
}
```

**Dërgimi te Institucioni:**
```java
adapter.setOnSendListener(report -> showSendToInstitutionDialog(report));

private void showSendToInstitutionDialog(Report report) {
    // Dialog me institucione të paracaktuara + fushë email custom
    String[] institutions = {
        "Bashkia Tiranë", "Inspektoriati Mjedisor", "Ujësjellës Kanalizime", "Tjetër"
    };
    // Kur zgjidhet institucioni → krijo email intent
    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
    emailIntent.setData(Uri.parse("mailto:" + selectedEmail));
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Raport mjedisor: " + report.getCategory());
    emailIntent.putExtra(Intent.EXTRA_TEXT, buildEmailBody(report));

    // Fallback nëse nuk ka email client
    try {
        startActivity(emailIntent);
    } catch (ActivityNotFoundException e) {
        // Kopjo në clipboard
        ClipboardManager clipboard = getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("email", selectedEmail));
        Toast.makeText(requireContext(), "Email-i u kopjua!", ...).show();
    }

    // Ruan rekord auditimi
    SentReport sentReport = new SentReport(report.getId(), report.getCategory(), ...);
    reportViewModel.saveSentReport(sentReport);
}
```

---

### 12.7 `SentReportsFragment.java`

**Layout:** `fragment_sent_reports.xml`  
**Roli:** Historia e raporteve të dërguara te institucionet (vetëm për admin).

Shfaq listën e rekordeve `SentReport` të adminit aktual — read-only, vetëm për auditim.

---

## 13. Adapterat (Adapters)

### 13.1 `ReportAdapter.java`

**Extends:** `RecyclerView.Adapter<ReportAdapter.ViewHolder>`  
**Roli:** Shfaq listën e raporteve të përdoruesit në HistoryFragment.

#### ViewHolder

```java
class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvCategory, tvDescription, tvDate, tvStatus, tvConfirmations;
    ImageView ivCategoryIcon;
    Chip statusChip;
}
```

#### Metoda `onBindViewHolder()`

- Cakton ngjyrën e ikonës sipas kategorisë (e kuqe/portokalli/gri/blu/jeshile)
- Preson përshkrimin në 80 karaktere
- Formatojnë datën: `dd MMM yyyy`
- Cakton ngjyrën e statusChip sipas vlerës

---

### 13.2 `AdminReportAdapter.java`

**Extends:** `RecyclerView.Adapter<AdminReportAdapter.ViewHolder>`  
**Roli:** Shfaq raportet me butona veprimi për AdminReportsFragment.

#### Butonat e Veprimit

```java
// Disable butonat nëse raporti është procesuar
if (!report.getStatus().equals("E re")) {
    btnApprove.setEnabled(false);
    btnReject.setEnabled(false);
    btnSend.setEnabled(false);
}
```

#### Interface-t e Callback-ut

```java
public interface OnApproveListener { void onApprove(Report report); }
public interface OnRejectListener  { void onReject(Report report); }
public interface OnSendListener    { void onSend(Report report); }
```

---

### 13.3 `DetailPhotoAdapter.java`

**Extends:** `RecyclerView.Adapter<DetailPhotoAdapter.ViewHolder>`  
**Roli:** Karusel horizontal i fotografive në ReportDetailActivity.

#### Dekodimi i Base64 në Background Thread

```java
@Override
public void onBindViewHolder(ViewHolder holder, int position) {
    String base64 = photos.get(position);
    new Thread(() -> {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            // Kthehu te UI thread
            holder.imageView.post(() -> holder.imageView.setImageBitmap(bitmap));
        } catch (Exception e) {
            holder.imageView.post(() -> holder.imageView.setImageResource(R.drawable.ic_photo));
        }
    }).start();
}
```

Dekodimi bëhet në thread tjetër për të shmangur humbjet e frame-ve UI.

---

### 13.4 `PhotoPreviewAdapter.java`

**Extends:** `RecyclerView.Adapter<PhotoPreviewAdapter.ViewHolder>`  
**Roli:** Shfaq previstat e fotografive para dërgimit në ReportFragment.

Mban dy lista paralele: `List<Uri> uris` dhe `List<Bitmap> bitmaps`. Butoni "X" mbi çdo foto e heq nga adapter-i dhe azhuron numrin e fotove.

---

## 14. Klasat Ndihmëse (Helper Classes)

### 14.1 `MarkerBitmapHelper.java`

**Roli:** Krijon bitmap-e të personalizuara për markerët e hartës.

#### Metoda `createMarkerBitmap(context, category, confirmations)`

```java
public static Bitmap createMarkerBitmap(Context context, String category, int confirmations) {
    // Madhësia e markerit rritet me numrin e konfirmimeve
    int baseSize = 80;
    int size = baseSize + Math.min(confirmations * 5, 40);  // max +40px

    // Ngjyra sipas kategorisë
    int color;
    String letter;
    switch (category) {
        case "Mbetje Urbane": color = Color.RED;    letter = "M"; break;
        case "Zhurmë":        color = 0xFFFF8C00;  letter = "Z"; break;
        case "Ndotje Ajri":   color = Color.GRAY;  letter = "A"; break;
        case "Ndotje Uji":    color = Color.BLUE;  letter = "U"; break;
        default:              color = Color.GREEN; letter = "T"; break;
    }

    // Vizato marker-in me Canvas (formë pikë me bisht + shkronjë)
    Bitmap bitmap = Bitmap.createBitmap(size, size + size/3, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    // ... vizatim me Path dhe Paint
    return bitmap;
}
```

---

### 14.2 `ReportInfoWindow.java`

**Extends:** `InfoWindow` (OSMDroid)  
**Roli:** Popup-i që shfaqet kur klikohet marker-i në hartë.

```java
public class ReportInfoWindow extends InfoWindow {

    @Override
    public void onOpen(Object item) {
        Marker marker = (Marker) item;
        Report report = (Report) marker.getRelatedObject();

        // Populo me të dhënat e raportit
        tvCategory.setText(report.getCategory());
        tvDescription.setText(truncate(report.getDescription(), 60));
        tvConfirmations.setText(report.getConfirmations() + " konfirmime");
        tvDate.setText(formatDate(report.getTimestamp()));

        // Butoni "Shiko të plota" → hap ReportDetailActivity
        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailActivity.class);
            intent.putExtra("reportId", report.getId());
            context.startActivity(intent);
        });
    }
}
```

---

## 15. Utilitetet (Utils)

### 15.1 `AppPreferences.java`

**Paketa:** `com.programimmobile.ecoalert.utils`  
**Roli:** Wrapper mbi `SharedPreferences` — ruan preference lokale.

#### Konstantet

```java
private static final String PREF_NAME         = "ecoalert_prefs";
private static final String KEY_AUTH_COMPLETED = "auth_choice_made";
```

#### Metodat

```java
// Shëno që përdoruesi bëri zgjedhje të qëllimshme (login/register/anonim)
public void setAuthCompleted(boolean completed) {
    prefs.edit().putBoolean(KEY_AUTH_COMPLETED, completed).apply();
}

// Kontrollo nëse u bë zgjedhja
public boolean isAuthCompleted() {
    return prefs.getBoolean(KEY_AUTH_COMPLETED, false);
}
```

**Roli kritik**: Shmang ciklin bosh ku aplikacioni kalon tek MainActivity me sesion anonim automatik pa dëshirë të përdoruesit. Vendoset `true` vetëm kur përdoruesi klikon activisht një buton autentifikimi.

---

## 16. Sistemi i Njoftimeve (Notifications)

EcoAlert zbaton **njoftime in-app** (jo push notifications) duke përdorur Firestore si kanal komunikimi.

### Fluksi i Plotë

```
1. Admini klikon "Aprovo" ose "Refuzo" në AdminReportsFragment
          ↓
2. ReportViewModel.approveReport(report) / rejectReport(report, reason)
          ↓
3. repository.updateReportStatus(reportId, "I aprovuar") → Firestore
          ↓  [kur sukses]
4. Krijohet objekt Notification me:
   - userId   = pronari i raportit
   - reportId = ID e raportit
   - type     = "APPROVED" / "REJECTED"
   - message  = tekst informues
   - isRead   = false
          ↓
5. notificationRepository.sendNotification(notification) → Firestore (koleksioni "notifications")
          ↓
6. NotificationsFragment ka SnapshotListener aktiv →
   azhurohet automatikisht
          ↓
7. Kur përdoruesi hap NotificationsFragment →
   markAllAsRead() → isRead = true për të gjitha
```

### Tipet e Njoftimeve

| Tipi | Mesazhi |
|---|---|
| `TYPE_APPROVED` | "Raporti juaj për '[kategori]' u aprovua nga admini. Faleminderit!" |
| `TYPE_REJECTED` | "Raporti juaj për '[kategori]' u refuzua. Arsyeja: [arsye]" |
| `TYPE_SENT` | (opsionale) kur raporti dërgohet te institucioni |

---

## 17. Fluksi i të Dhënave (Data Flow)

### Shtimi i Raportit (User Flow)

```
ReportFragment (UI)
  → submitReport()
    → bitmapToBase64() [Thread]
      → new Report(...)
        → ReportViewModel.addReport(report)
          → ReportRepository.addReport(report)
            → Firestore.collection("reports").add(report)
              ← onSuccess()
            ← reportSubmittedLiveData.postValue(true)
          ← UI: "Raporti u dërgua me sukses!" + clearForm()
```

### Aprovimi i Raportit (Admin Flow)

```
AdminReportsFragment (UI)
  → adapter.onApproveListener.onApprove(report)
    → ReportViewModel.approveReport(report)
      → ReportRepository.updateReportStatus(id, "I aprovuar")
        → Firestore.document(id).update("status", "I aprovuar")
          ← onSuccess()
        → Notification.new(userId, reportId, TYPE_APPROVED, message)
          → NotificationRepository.sendNotification(notif)
            → Firestore.collection("notifications").add(notif)

    [Njëkohësisht, tek User-i:]
    SnapshotListener i NotificationsFragment
      ← Firestore dërgon ndryshimin automatikisht
      → notificationsLiveData.postValue(list)
        → RecyclerView azhurohet me njoftimin e ri
```

---

## 18. Menaxhimi i Fotografive

### Rruga e Fotografisë: Kamera → Firestore

```
1. openCamera()
   → createImageFile()  ← krijon skedar të përkohshëm në getCacheDir()
   → FileProvider.getUriForFile()  ← gjeneron content:// URI
   → Intent(ACTION_IMAGE_CAPTURE) + EXTRA_OUTPUT=uri
   → cameraLauncher.launch(intent)

2. [Përdoruesi fotografon]
   → cameraLauncher result: RESULT_OK
   → loadBitmapFromUri(cameraPhotoUri)
     → MediaStore.Images.Media.getBitmap()
     → Resize: max 600px × 600px
   → addPhotoToLayout(uri, bitmap)

3. submitReport() [Thread]
   → loadBitmapFromUri(uri)
   → bitmapToBase64(bitmap)
     → compress JPEG, quality=25%
     → nëse > 250KB: compress quality=15%
     → Base64.encodeToString()
   → report.setPhotos(base64List)
   → ReportRepository.addReport(report)
     → Firestore ruhet si fushë "photos": ["data:..."]
```

### Dekodimi i Fotografisë: Firestore → UI

```
Firestore → report.getPhotos() → List<String> base64
  → DetailPhotoAdapter.onBindViewHolder()
    → new Thread()
      → Base64.decode(base64)
      → BitmapFactory.decodeByteArray()
      → holder.imageView.post(() → setImageBitmap(bitmap))
```

---

## 19. Navigimi dhe Rolet e Përdoruesve

### Rolet

| Roli | Si identifikohet | Akses |
|---|---|---|
| **Anonim** | `FirebaseUser.isAnonymous() == true` | Raport, Hartë, Profil |
| **User** | E-mail/Google i loguar | Raport, Hartë, Histori, Njoftime, Profil |
| **Admin** | Dokumenti ekziston në `admins/{userId}` | Raportet (Admin), Hartë, Dërguar, Profil |

### Menuja e Navigimit

**`bottom_nav_menu.xml`** (User):
```
Raport (nav_report)  →  ReportFragment
Harta  (nav_map)     →  MapFragment
Historia (nav_history) →  HistoryFragment
Njoftime (nav_notifications) →  NotificationsFragment
Profili (nav_profile)  →  ProfileFragment
```

**`bottom_nav_admin.xml`** (Admin):
```
Raportet (nav_admin_reports) →  AdminReportsFragment
Harta    (nav_map)           →  MapFragment
Dërguar  (nav_sent)          →  SentReportsFragment
Profili  (nav_profile)       →  ProfileFragment
```

### Fluksi i Autentifikimit

```
SplashActivity (2 sek)
    ↓
┌─── isLoggedIn() && isAuthCompleted() ──→ MainActivity
│
└─── jo ──→ AuthActivity
               ├── Tab "Hyr" → signInWithEmail()
               ├── Tab "Regjistrohu" → registerWithEmail()
               ├── Butoni "Vazhdo pa llogari" → signInAnonymously()
               └── [sukses] → MainActivity

MainActivity
    ↓
checkIsAdmin(userId)
    ├── admin == true  → AdminReportsFragment (default)
    └── admin == false → ReportFragment (default)
```

---

## Statistika të Projektit

| Metrika | Vlera |
|---|---|
| Gjuha e programimit | Java 11 |
| Gjuha e ndërfaqes | Shqip |
| Skedarë Java | 32 |
| Aktivitete | 5 |
| Fragmente | 7 |
| Adapterat | 4 |
| Klasa Model | 3 |
| Klasa Repository | 3 |
| Klasa ViewModel | 2 |
| Klasa Utilitet | 3 |
| Koleksione Firestore | 4 |
| Min SDK | API 23 (Android 6.0) |
| Target SDK | API 36 |
| Linja kodi (Java) | ~4,500+ |

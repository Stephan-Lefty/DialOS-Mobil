import java.io.File
import java.net.URI
import java.util.Properties
import java.util.UUID
import java.util.zip.ZipFile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ---------------------------------------------------------------------------
// Vosk-Sprachmodell
//
// Das deutsche Modell (~46 MB) liegt bewusst NICHT im Git-Repo. Es wird beim
// ersten Build heruntergeladen und in die generierten Assets entpackt, damit
// die App vollstaendig offline arbeitet.
//
// Eigenes/lokales Modell verwenden:  ./gradlew assembleDebug -PvoskModelZip=/pfad/zum/modell.zip
// ---------------------------------------------------------------------------
val voskModelName = "vosk-model-small-de-0.15"
val voskModelUrl = "https://alphacephei.com/vosk/models/$voskModelName.zip"

/** Ordner, den AGP zusaetzlich als Asset-Quelle einliest. */
val generatedAssetsDir = layout.buildDirectory.dir("generated/assets")

val localModelZip = providers.gradleProperty("voskModelZip").orNull

val prepareVoskModel by tasks.registering {
    description = "Laedt das deutsche Vosk-Modell und entpackt es nach assets/model-de."
    group = "build setup"

    val modelDir = generatedAssetsDir.map { it.dir("model-de") }
    val cachedZip = layout.buildDirectory.file("vosk/$voskModelName.zip")
    val overrideZip = localModelZip

    outputs.dir(modelDir)

    doLast {
        val target = modelDir.get().asFile
        val marker = File(target, "uuid")
        if (marker.isFile && File(target, "am").isDirectory) {
            logger.lifecycle("Vosk-Modell bereits vorhanden: $target")
            return@doLast
        }

        val zip = if (overrideZip != null) {
            File(overrideZip).also {
                require(it.isFile) { "voskModelZip zeigt auf keine Datei: $overrideZip" }
            }
        } else {
            cachedZip.get().asFile.also { file ->
                if (!file.isFile || file.length() < 1_000_000L) {
                    file.parentFile.mkdirs()
                    logger.lifecycle("Lade Vosk-Modell herunter (~46 MB): $voskModelUrl")
                    URI(voskModelUrl).toURL().openStream().use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }

        target.deleteRecursively()
        target.mkdirs()

        ZipFile(zip).use { archive ->
            for (entry in archive.entries()) {
                // Oberste Ebene ("vosk-model-small-de-0.15/") abschneiden
                val relative = entry.name.substringAfter('/', "")
                if (relative.isEmpty()) continue
                val out = File(target, relative)
                require(out.canonicalPath.startsWith(target.canonicalPath)) {
                    "Verdaechtiger Pfad im Archiv: ${entry.name}"
                }
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile.mkdirs()
                    archive.getInputStream(entry).use { input ->
                        out.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }

        // Vosks StorageService entpackt die Assets beim ersten Start ins interne
        // Dateisystem und vergleicht dafuer diese uuid-Datei. Deterministisch aus
        // dem Modellnamen abgeleitet: sie aendert sich nur bei einem Modellwechsel,
        // nicht bei jedem Build.
        marker.writeText(UUID.nameUUIDFromBytes(voskModelName.toByteArray()).toString())
        logger.lifecycle("Vosk-Modell entpackt nach $target")
    }
}

// ---------------------------------------------------------------------------
// Signierung fuer die Veroeffentlichung
//
// Der Schluessel liegt bewusst NICHT im Repo. Anlegen mit:
//
//   keytool -genkeypair -v -keystore dialos-mobil-release.jks \
//           -alias dialos -keyalg RSA -keysize 4096 -validity 10000
//
// Danach keystore.properties im Projektwurzelverzeichnis anlegen
// (steht in .gitignore):
//
//   storeFile=/absoluter/pfad/dialos-mobil-release.jks
//   storePassword=...
//   keyAlias=dialos
//   keyPassword=...
//
// Ohne diese Datei baut der Release-Zweig unsigniert weiter - praktisch
// zum Pruefen, aber nicht hochladbar.
// ---------------------------------------------------------------------------
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
val hasReleaseKeystore = keystoreProperties.getProperty("storeFile") != null

android {
    namespace = "org.dialos.mobil"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.dialos.mobil"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "0.6.2"
        resourceConfigurations += setOf("de", "en")

        // Nur die Architekturen echter Telefone plus x86_64 für den Emulator.
        // Spart rund 20 MB gegenüber allen Varianten, die die Vosk- und
        // JNA-Bibliotheken mitbringen.
        ndk {
            abiFilters += setOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // Das Modell bleibt komprimiert im APK (spart ~45 MB). Vosks
    // StorageService kopiert die Dateien beim ersten Start ohnehin über einen
    // Stream in den internen Speicher - dabei entpackt Android transparent.

    sourceSets["main"].assets.srcDir(generatedAssetsDir)

    packaging {
        jniLibs.useLegacyPackaging = true
    }

    bundle {
        // Das Sprachmodell muss in jedem Fall mitkommen - es ist der Kern
        // der Offline-Erkennung und darf nicht sprachabhaengig
        // wegoptimiert werden.
        language { enableSplit = false }
    }
}

tasks.named("preBuild") { dependsOn(prepareVoskModel) }
tasks.withType<com.android.build.gradle.tasks.MergeSourceSetFolders>().configureEach {
    dependsOn(prepareVoskModel)
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Offline-Spracherkennung (dieselbe Engine wie im DialOS-Desktop)
    implementation("com.alphacephei:vosk-android:0.3.47@aar")
    implementation("net.java.dev.jna:jna:5.13.0@aar")

    testImplementation("junit:junit:4.13.2")
}

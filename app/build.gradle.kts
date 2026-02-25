import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

val gradleProperties = Properties()
val gradlePropsFile = rootProject.file("gradle.properties")

if (gradlePropsFile.exists()) {
    gradlePropsFile.inputStream().use { gradleProperties.load(it) }
}

val geminiApiKey = gradleProperties.getProperty("GEMINI_API_KEY")?.trim() ?: ""
val mapsApiKey = gradleProperties.getProperty("MAPS_API_KEY")?.trim() ?: ""

android {
    namespace = "com.example.foodathome"
    compileSdk = 35

    kotlinOptions {
        jvmTarget = "11"
    }

    configurations.all {
        resolutionStrategy {
            force("com.google.protobuf:protobuf-javalite:3.25.5")
            force("io.grpc:grpc-core:1.62.2")
            force("io.grpc:grpc-api:1.62.2")
            force("io.grpc:grpc-context:1.62.2")
            force("io.grpc:grpc-android:1.62.2")
            force("io.grpc:grpc-okhttp:1.62.2")
            force("io.grpc:grpc-protobuf-lite:1.62.2")
            force("io.grpc:grpc-stub:1.62.2")
            force("com.google.guava:guava:33.0.0-android")
            force("io.grpc:grpc-util:1.62.2")
        }
    }

    defaultConfig {
        applicationId = "com.example.foodathome"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Instead of BuildConfig, we use resValue (Strings)
        resValue("string", "gemini_api_key", geminiApiKey)
        resValue("string", "maps_api_key", mapsApiKey)

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/dev.aj00.protobuf.kotlin.package_name_map")
            excludes.add("META-INF/gradle/incremental.annotation.processors")
            excludes.add("META-INF/io.netty.versions.properties")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.genai:google-genai:1.16.0")

    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)

    implementation("io.grpc:grpc-core:1.62.2")
    implementation("io.grpc:grpc-api:1.62.2")
    implementation("io.grpc:grpc-android:1.62.2")
    implementation("io.grpc:grpc-okhttp:1.62.2")
    implementation("io.grpc:grpc-protobuf-lite:1.62.2")
    implementation("io.grpc:grpc-stub:1.62.2")
    implementation("io.grpc:grpc-util:1.62.2")
    implementation("com.google.guava:guava:33.0.0-android")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
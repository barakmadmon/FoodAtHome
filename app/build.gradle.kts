import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

val geminiProperties = Properties()
val geminiPropsFile = rootProject.file("gradle.properties")

if (geminiPropsFile.exists()) {
    geminiProperties.load(geminiPropsFile.inputStream())
}
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
        android.buildFeatures.buildConfig = true
        multiDexEnabled = true
        applicationId = "com.example.foodathome"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"${geminiProperties["GEMINI_API_KEY"]}\"")
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


    // 1. AndroidX & UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 2. Firebase - Use the BoM to ensure internal compatibility
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-firestore")

    // 3. Gemini AI
    implementation("com.google.genai:google-genai:1.16.0")

    // 4. THE CRITICAL FIX: Force gRPC and Guava versions
    // This prevents the "NoClassDefFoundError: io.grpc.InternalGlobalInterceptors"
    implementation("io.grpc:grpc-core:1.62.2")
    implementation("io.grpc:grpc-api:1.62.2")
    implementation("io.grpc:grpc-android:1.62.2")
    implementation("io.grpc:grpc-okhttp:1.62.2")
    implementation("io.grpc:grpc-protobuf-lite:1.62.2")
    implementation("io.grpc:grpc-stub:1.62.2")
    implementation("io.grpc:grpc-util:1.62.2")
    implementation("com.google.guava:guava:33.0.0-android")


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
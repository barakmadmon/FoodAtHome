import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)

}

val geminiProperties = Properties()
val geminiPropsFile = rootProject.file("gradle.properties")

if (geminiPropsFile.exists()) {
    geminiProperties.load(geminiPropsFile.inputStream())
}
android {
    namespace = "com.example.foodathome"
    compileSdk = 36

    compileSdk {
        version  = release(34)
    }

    defaultConfig {
        android.buildFeatures.buildConfig = true
        applicationId = "com.example.foodathome"
        minSdk = 34
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/*.SF")
            excludes.add("META-INF/*.DSA")
            excludes.add("META-INF/*.RSA")
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.common)
    //implementation(libs.firebase.ai)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    /*implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")*/

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // for gemini api
    /*implementation("com.google.guava:guava:31.0.1-android")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")*/
    implementation("com.google.genai:google-genai:1.16.0")
}
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.2.21"
}

android {
    namespace = "com.example.bloom"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bloom"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //set value part for supabase
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${properties.getProperty("SUPABASE_PUBLISHABLE_KEY")}\"")
        buildConfigField("String", "SECRET", "\"${properties.getProperty("SECRET")}\"")
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${properties.getProperty("GOOGLE_CLIENT_ID")}\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.animation.core.lint)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //icons
    implementation("androidx.compose.material:material-icons-extended-android:1.6.7") // Use the latest version

    // DataStore for local theme preference storage
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil for image loading (avatar display)
    implementation("io.coil-kt:coil-compose:2.5.0")

    //supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.5"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.2.6")

    //ktor
    implementation("io.ktor:ktor-client-android:3.3.1")
//    implementation("io.ktor:ktor-client-core:3.3.1")
//    implementation("io.ktor:ktor-utils:3.3.1")


//    implementation("io.ktor:ktor-client-okhttp:3.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    //hilt
//    implementation ("com.google.dagger:hilt-android:2.57.2")
//    annotationProcessor ("com.google.dagger:hilt-compiler:2.57.2")
//    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")


    //Retrofit dependencies

    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Gson converter for JSON serialization/deserialization
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp (usually included with Retrofit, but good to have explicit)
    // and its logging interceptor for debugging network requests
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // iText PDF generation for analytics reports
    implementation("com.itextpdf:itextpdf:5.5.13.3")

    //MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //google oAuth from supabase
    implementation("androidx.credentials:credentials:1.5.0")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.1")

}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.localfresh"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.localfresh"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    dataBinding {
        enable = true
    }
    viewBinding {
        enable = true
    }
}

dependencies {
    // AndroidX Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.paging.runtime.ktx)
    //worker
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Firebase
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.messaging)
    implementation("com.google.firebase:firebase-analytics:21.5.0")

    // Google Play Services
    implementation (libs.play.services.location)
    implementation(libs.play.services.auth)
    implementation(libs.credentials)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)

    // Network Libraries
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Image Loading
    implementation(libs.glide)
    implementation(libs.ucrop)

    // Date & Time Pickers
    implementation(libs.materialdatetimepicker)
    implementation(libs.countrycodepicker)

    // Kotlin Coroutines y Data Store
    implementation(libs.kotlinx.coroutines.android)
    // OpenStreetMap
    implementation(libs.osmdroid.android)
    implementation(libs.recyclerview)
    implementation(libs.osmbonuspack)
    implementation(libs.swiperefreshlayout)
    implementation(libs.androidx.preference)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    // Core Libraries
    implementation(libs.core.ktx)

    //Lottie
    implementation (libs.lottie)

    // Codigo qr
    implementation("com.journeyapps:zxing-android-embedded:4.2.0")
    implementation("com.google.zxing:core:3.4.1")

    // Estadisticas
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")}

    // google services plugin
    apply(plugin = "com.google.gms.google-services")

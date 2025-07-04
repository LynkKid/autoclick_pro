plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.auto.click"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hd.gba.psa.emulator"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.00"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation(files("libs/BaseRecyclerViewAdapterHelper-3.0.11.aar"))
    implementation("androidx.activity:activity:1.10.1")
    implementation("com.google.android.gms:play-services-ads-api:24.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.security:security-crypto:1.0.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.davemorrissey.labs:subsampling-scale-image-view-androidx:3.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.android.billingclient:billing:7.1.1")
    implementation("com.google.guava:guava:33.0.0-android")
}
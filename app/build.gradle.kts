import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)


}

android {
    namespace = "com.example.nirbhaya_chakra"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nirbhaya_chakra"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")

        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        val apiKey = properties.getProperty("GOOGLE_API_KEY")
            ?: throw GradleException("GOOGLE_API_KEY missing in local.properties")

        manifestPlaceholders["GOOGLE_API_KEY"] = apiKey


    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true

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
}

dependencies {
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // -------------------
    // Compose (REQUIRED)
    // -------------------

    implementation(platform(libs.androidx.compose.bom))
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")


    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)

    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.activity.compose)

    // -------------------
    // Room (REQUIRED)
    // -------------------
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)



}
plugins {
    id("com.android.application")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}
android {
    namespace = "com.example.leafdetectionapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.leafdetectionapp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.github.yalantis:ucrop:2.2.8")

    implementation("com.google.firebase:firebase-auth")



    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.22")

    implementation ("com.squareup.picasso:picasso:2.5.2")

    implementation("com.google.android.gms:play-services-auth:18.1.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.camera.core)
    implementation(libs.camera.view)
    implementation(libs.firebase.auth)
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("org.tensorflow:tensorflow-lite:2.10.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.2")




}
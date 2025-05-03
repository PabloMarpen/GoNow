plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // Plugin de Google Services
}

android {
    namespace = "com.example.gonow.tfg"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gonow.tfg"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.4.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true // HABILITA VIEW BINDING
    }

    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //google maps y location
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Firebase módulos específicos
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.analytics.ktx)

    // google sing in
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    //ViewModel compartido entre actividad y fragmentos
    implementation(libs.androidx.fragment.ktx)
    // para recargar la pagina
    implementation (libs.androidx.swiperefreshlayout)

    // ftp
    implementation(libs.jsch)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.glide)

    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.identity.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
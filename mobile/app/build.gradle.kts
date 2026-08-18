import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.spyrent.child"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.spyrent.child"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // overridable at build time: -PspyrentBaseUrl=https://your-deployment
        buildConfigField(
            "String",
            "DEFAULT_BASE_URL",
            "\"" + (project.findProperty("spyrentBaseUrl") ?: "https://spyrent-beta.vercel.app") + "\"",
        )
    }

    // Release signing is driven by keystore.properties, or by the matching
    // environment variables in CI. Neither the file nor the passwords belong in
    // the repository — keystore.properties is gitignored.
    val keystoreProperties = Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

    fun secret(key: String, env: String): String? =
        keystoreProperties.getProperty(key) ?: System.getenv(env)

    signingConfigs {
        create("release") {
            val storePath = secret("storeFile", "SPYRENT_KEYSTORE")
            if (storePath != null) {
                storeFile = file(storePath)
                storePassword = secret("storePassword", "SPYRENT_KEYSTORE_PASSWORD")
                keyAlias = secret("keyAlias", "SPYRENT_KEY_ALIAS")
                keyPassword = secret("keyPassword", "SPYRENT_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            // an unsigned release build is still useful for checking that
            // minification did not break anything
            if (secret("storeFile", "SPYRENT_KEYSTORE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}

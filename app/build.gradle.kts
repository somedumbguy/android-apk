import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Optional shared signing key, so builds install as updates and keep the same
// fingerprint for Google Sign-In. See keystore.properties.example.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.example.hello"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.hello"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "0.6"
    }

    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("shared") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            if (keystoreProps.isNotEmpty()) signingConfig = signingConfigs.getByName("shared")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core)

    testImplementation(libs.junit)
    testImplementation(testFixtures(project(":domain")))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}

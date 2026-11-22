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
        versionCode = 10
        versionName = "0.10"
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

    // Screenshot tests render real screens with Robolectric, which needs the app's resources.
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            // Robolectric reaches into these JDK internals on Java 17+.
            it.jvmArgs(
                "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
            )
        }
    }

    // Android Lint: any warning fails the build.
    lint {
        abortOnError = true
        warningsAsErrors = true
        checkDependencies = true
        // UI text is written in code for now; translations come later.
        disable += "SetTextI18n"
        // These depend on what's newly published online, so they'd make builds fail
        // on dates rather than on code. Report them without failing.
        informational += listOf("OldTargetApi", "GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core)

    testImplementation(libs.junit)
    testImplementation(libs.archunit)
    testImplementation(libs.robolectric)
    testImplementation(testFixtures(project(":domain")))
}

// Compiler warnings are errors.
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked", "-Werror"))
}

// Every Android build makes sure this clone uses the project's git hooks.
tasks.named("preBuild") { dependsOn(":installGitHooks") }

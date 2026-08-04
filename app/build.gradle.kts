import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

fun String.asBuildConfigLiteral(): String =
    "\"" + replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "") + "\""

fun prop(name: String, vararg aliases: String): String {
    val value = sequenceOf(name, *aliases)
        .mapNotNull { localProperties.getProperty(it)?.takeIf(String::isNotBlank) }
        .firstOrNull()
        .orEmpty()
    return value.asBuildConfigLiteral()
}

android {
    namespace = "seamain.org.typhoonEye"
    compileSdk = 36

    defaultConfig {
        applicationId = "seamain.org.typhoonEye"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject API keys from local.properties (see local.properties.example)
        buildConfigField("String", "JUHE_KEY", prop("JUHE_KEY"))
        buildConfigField("String", "QWEATHER_API_KEY", prop("QWEATHER_API_KEY"))
        buildConfigField("String", "QWEATHER_KID", prop("QWEATHER_KID", "QWEATHER_PUBLIC_ID"))
        buildConfigField("String", "QWEATHER_PROJECT_ID", prop("QWEATHER_PROJECT_ID"))
        buildConfigField(
            "String",
            "QWEATHER_PRIVATE_KEY",
            prop("QWEATHER_PRIVATE_KEY", "QWEATHER_PROJECT_KEY")
        )
        buildConfigField(
            "String",
            "QWEATHER_HOST",
            (localProperties.getProperty("QWEATHER_HOST")?.takeIf { it.isNotBlank() }
                ?: "https://pu6yvrgfbv.re.qweatherapi.com/").asBuildConfigLiteral()
        )
        // Empty = bundled asset://map_style.json (Carto raster). Override if needed.
        buildConfigField(
            "String",
            "MAPLIBRE_STYLE_URL",
            (localProperties.getProperty("MAPLIBRE_STYLE_URL")?.takeIf { it.isNotBlank() }
                ?: "").asBuildConfigLiteral()
        )
    }

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("RELEASE_STORE_FILE")
                ?: System.getenv("RELEASE_STORE_FILE")
            val keystoreFile = storeFilePath?.let { rootProject.file(it) } ?: rootProject.file("release.keystore")

            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                    ?: System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                    ?: System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
                    ?: System.getenv("RELEASE_KEY_PASSWORD")
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.eddsa)
    implementation(libs.maplibre.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    // Activity-scoped ViewModels: default viewModel() + @AndroidEntryPoint is enough.
    // (hilt-navigation-compose only needed for per-backStackEntry hiltViewModel().)
    implementation(libs.hilt.work)
    implementation(libs.play.services.location)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.androidx.compiler)

    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.json:json:20231013") // For testing JSON in unit tests
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.turbine)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

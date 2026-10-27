plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    kotlin("plugin.serialization") version "2.4.10" apply false
    id("app.cash.sqldelight") version "2.1.0" apply false
}
allprojects {
    configurations.configureEach {
        resolutionStrategy.force("com.squareup.okhttp3:okhttp:5.3.0", "com.squareup.okhttp3:okhttp-android:5.3.0")
    }
}

plugins { alias(libs.plugins.androidApplication); alias(libs.plugins.composeCompiler) }
android {
    namespace="pe.upeu.agrosierra"
    compileSdk=36
    defaultConfig { applicationId="pe.upeu.agrosierra"; minSdk=24; targetSdk=36; versionCode=1; versionName="1.0" }
    compileOptions { sourceCompatibility=JavaVersion.VERSION_11; targetCompatibility=JavaVersion.VERSION_11 }
    buildFeatures { compose=true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
kotlin { compilerOptions { jvmTarget=org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11 } }
dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation("app.cash.sqldelight:android-driver:2.1.0")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("io.ktor:ktor-client-okhttp:3.6.0")
}

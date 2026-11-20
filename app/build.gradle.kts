plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.plugin.compose")
}
android {
 namespace="com.photor.app"; compileSdk=37; buildFeatures{compose=true}
 defaultConfig{applicationId="com.photor.app";minSdk=35;targetSdk=37;versionCode=2;versionName="1.1"}
}
dependencies{
 implementation(platform("androidx.compose:compose-bom:2026.09.00"))
 implementation("androidx.activity:activity-compose:1.13.0")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.compose.foundation:foundation")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
 implementation("io.coil-kt.coil3:coil-compose:3.3.0")
 implementation("androidx.media3:media3-exoplayer:1.9.0")
 implementation("androidx.media3:media3-ui:1.9.0")
 debugImplementation("androidx.compose.ui:ui-tooling")
}
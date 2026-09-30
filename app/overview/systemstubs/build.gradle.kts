plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.nico.overview.systemstubs"
    compileSdk = 36
    defaultConfig {
        minSdk = 36
    }
}

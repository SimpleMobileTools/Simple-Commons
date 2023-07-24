plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinAndroidExtensions) //todo remove
}
android {
    compileSdk = libs.versions.app.build.compileSDKVersion.get().toInt()

    defaultConfig {
        applicationId = libs.versions.app.version.appId.get()
        minSdk = libs.versions.app.build.minimumSDK.get().toInt()
        targetSdk = libs.versions.app.build.targetSDK.get().toInt()
        versionName = libs.versions.app.version.versionName.get()
        versionCode = libs.versions.app.version.versionCode.get().toInt()
        vectorDrawables.useSupportLibrary = true
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
    namespace = "com.simplemobiletools.commons.samples"
}

dependencies {
    implementation(projects.commons)
    implementation(libs.material)
    implementation(libs.androidx.swiperefreshlayout)
}

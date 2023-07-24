plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinAndroidExtensions) //todo remove and add parcelize since we have 1 model with it
    `maven-publish`
}

android {
    compileSdk = libs.versions.app.build.compileSDKVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.app.build.minimumSDK.get().toInt()
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    compileOptions {
        val currentJavaVersionFromLibs = JavaVersion.valueOf(libs.versions.app.build.javaVersion.get().toString())
        sourceCompatibility = currentJavaVersionFromLibs
        targetCompatibility = currentJavaVersionFromLibs
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = project.libs.versions.app.build.kotlinJVMTarget.get()
        kotlinOptions.freeCompilerArgs = listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers"
        )
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
    namespace = libs.versions.app.version.groupId.get()
}

publishing.publications {
    create<MavenPublication>("release") {
        groupId = libs.versions.app.version.groupId.get()
        artifactId = name
        version = libs.versions.app.version.versionName.get()
        afterEvaluate {
            from(components["release"])
        }
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.ez.vcard)

    api(libs.joda.time)
    api(libs.recyclerView.fastScroller)
    api(libs.reprint)
    api(libs.rtl.viewpager)
    api(libs.patternLockView)
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.gson)

    api(libs.glide)
    kapt(libs.glide.compiler)

    api(libs.bundles.room)
    kapt(libs.androidx.room.compiler)
}

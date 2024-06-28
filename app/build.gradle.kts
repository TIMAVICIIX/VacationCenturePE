plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("D:\\Develop_Space\\Key\\TIMAVICIIX_KEY.jks")
            storePassword = "wzh741852963456"
            keyAlias = "TIMAVICIIX_KEY"
            keyPassword = "wzh741852963456"
        }
        create("release") {
            storeFile = file("C:\\Users\\TIMAVICIIX\\.android\\debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    namespace = "com.example.vacationventurepe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.vacationventurepe"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1正式版"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    dataBinding {
        enable = true
    }
}

dependencies {

    implementation("androidx.cardview:cardview:1.0.0+")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)



    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.material)

    //城市选择器
    implementation("com.github.crazyandcoder:citypicker:6.0.2")

    //Retrofit仓库化HTTP服务框架
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    //GSON数据转换与反射框架
    implementation("com.google.code.gson:gson:2.8.9")

    //Glide基于REST模式的图片部署API
    implementation("com.github.bumptech.glide:glide:4.12.0")

    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("org.jsoup:jsoup:1.14.2")

    //高德地图提供的逆地理编码解析服务
    implementation(fileTree(mapOf(
        "dir" to "libs",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))

    //附加：简单的权限请求
    implementation("pub.devrel:easypermissions:3.0.0")

    //刷新头-SmartRefresh
    implementation  ("io.github.scwang90:refresh-layout-kernel:2.1.0")      //核心必须依赖
    implementation  ("io.github.scwang90:refresh-header-classics:2.1.0")    //经典刷新头
    implementation  ("io.github.scwang90:refresh-header-radar:2.1.0" )      //雷达刷新头

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
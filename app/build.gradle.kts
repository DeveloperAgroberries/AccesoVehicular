plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.AgroberriesMX.accesovehicular"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.AgroberriesMX.accesovehicular"
        //minSdk = 21
        minSdk = 24
        targetSdk = 35
        versionCode = 17
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release"){
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            resValue("string", "AgroberriesMX", "Agroberries AccesoVehicular MX")
            buildConfigField("String", "BASE_URL", "\"http://54.165.41.23:5053/api/AgroAccessApp/\"")
            //buildConfigField("String", "BASE_URL", "\"http://192.168.1.37:5011/api/AgroAccessApp/\"")
        }

        getByName("debug"){
            isDebuggable = true
            resValue("string", "AgroberriesMX", "[DEBUG]Agroberries AccesoVehicular MX")
            buildConfigField("String", "BASE_URL", "\"http://54.165.41.23:5053/api/AgroAccessApp/\"")
            //buildConfigField("String", "BASE_URL", "\"http://192.168.1.37:5011/api/AgroAccessApp/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.activity:activity:1.10.1")
    val navVersion = "2.8.5"
    val daggerHiltVersion = "2.48"
    val retrofitVersion = "2.9.0"
    val okHttpVersion = "4.12.0"

    //NavComponent
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    //DaggerHilt
    implementation("com.google.dagger:hilt-android:$daggerHiltVersion")
    kapt("com.google.dagger:hilt-compiler:$daggerHiltVersion")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    //OkHttp
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    //SQLite
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    //Rondines
    implementation ("com.google.android.gms:play-services-location:21.0.1") // Asegúrate de usar la última versión estable

    //QR
    // Para el escaneo de QR (Zxing)
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0") // O la versión más reciente
    implementation ("com.google.zxing:core:3.5.3") // La librería core de Zxing
    // Si aún no tienes Material Design, ya que TextInputEditText lo usa
    implementation ("com.google.android.material:material:1.12.0") // Usa la versión más reciente


    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
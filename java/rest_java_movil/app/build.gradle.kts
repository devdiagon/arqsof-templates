plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "ec.edu.monster"
  compileSdk {
    version = release(37)
  }

  defaultConfig {
    applicationId = "ec.edu.monster"
    minSdk = 24
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes {
    release {
      // Desactivada: Gson lee los DTO por reflexión (campos por nombre) y R8 los renombraría o quitaría.
      // Si se activa, hay que conservar ec.edu.monster.dto.** en app/src/main/keepRules/rules.keep.
      optimization {
        enable = false
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.lifecycle.livedata)
  // JSON de la API REST (es Java puro: también corre en las pruebas locales)
  implementation(libs.gson)
  testImplementation(libs.junit)
  testImplementation(libs.androidx.arch.core.testing)
}

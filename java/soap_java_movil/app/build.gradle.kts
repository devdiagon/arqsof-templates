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
  testImplementation(libs.junit)
  testImplementation(libs.androidx.arch.core.testing)
}

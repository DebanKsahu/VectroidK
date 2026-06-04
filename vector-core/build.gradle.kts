import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "com.github.DebanKsahu"
version = "0.1.0"

kotlin {
    androidLibrary {
        namespace = "com.github.debanksahu.vectroidk"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "vectroidk", version.toString())

    pom {
        name = "VectroidK"
        description = "A On Device Memory Engine"
        inceptionYear = "2026"
        url = "https://github.com/DebanKsahu/VectroidK"
        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "DebanKsahu"
                name = "Deban Kumar Sahu"
                url = "https://github.com/DebanKsahu"
            }
        }
        scm {
            url = "https://github.com/DebanKsahu/VectroidK"
            connection = "scm:git:git://github.com/DebanKsahu/VectroidK.git"
            developerConnection = "scm:git:ssh://git@github.com:DebanKsahu/VectroidK.git"
        }
    }
}

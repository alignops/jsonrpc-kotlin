import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.2.41" apply false
}

allprojects {
    group = "io.busybusy.jsonrpc"
    version = "1.0"

    repositories { 
        jcenter()
    }

    tasks.withType(KotlinCompile::class.java).all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

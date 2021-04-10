import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    application
}

val coroutines_version = "1.3.8"
val serialization_version = "1.1.0"
val ktor_version = "1.5.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    api("io.ktor:ktor-client-core:$ktor_version")
    api("io.ktor:ktor-client-okhttp:$ktor_version")
    api("com.kohlschutter.junixsocket:junixsocket-native-common:2.0.4")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}
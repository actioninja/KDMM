import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.3.20"
    id("com.gradle.build-scan") version "2.2.1"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

group = "ninja.actio"
version = "0.1"

application {
    mainClassName = "ninja.actio.kdmm.KDMMKt"
}

sourceSets {
    test {
        java.srcDir("src/test/kotlin")
        resources.srcDir("src/test/resources")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.uchuhimo", "kotlinx-bimap", "1.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.3.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.2.70")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

repositories {
    mavenCentral()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
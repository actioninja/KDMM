import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    jacoco
    kotlin("jvm") version "1.3.31"
    id("com.gradle.build-scan") version "2.2.1"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

jacoco {
    reportsDir = file("$buildDir/test-results/jacoco")
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

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.uchuhimo", "kotlinx-bimap", "1.2")
    implementation("io.github.microutils", "kotlin-logging", "1.6.24")
    implementation("no.tornado", "tornadofx", "2.0.0-SNAPSHOT")
    implementation("ch.qos.logback", "logback-core", "1.2.3")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("ch.qos.logback", "logback-access", "1.2.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.3.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.3.31")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
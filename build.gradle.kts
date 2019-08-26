import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    id("me.champeau.gradle.jmh") version "0.4.8"
}

buildscript {
    repositories {
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("me.champeau.gradle:jmh-gradle-plugin:0.4.8")
    }
}

version = "1.0-SNAPSHOT"

tasks.test {
    maxHeapSize = "12G"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit:junit:4.12")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:1.3.41")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    groovy
    id("me.champeau.gradle.jmh") version "0.4.8"
}

buildscript {
    repositories {
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        flatDir {
            dirs = setOf(File("libs"))
        }
    }
    dependencies {
        classpath("me.champeau.gradle:jmh-gradle-plugin:0.4.8")
    }
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit:junit:4.12")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:1.3.41")
    compile("org.freemarker:freemarker:2.3.23")
    compile("commons-io:commons-io:2.4")
}

jmh {
    dependencies {
        implementation("org.organicdesign:Paguro-KF:3.5.6")
    }
}

tasks {
    test {
        maxHeapSize = "12G"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}


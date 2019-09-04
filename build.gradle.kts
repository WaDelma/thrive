import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties
import thrive.TemplateProcessor

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

//task("processTemplates") {
//    val fromDir = "$projectDir/src/templates"
//    val intoDir = "$buildDir/generated/sources/benchmarks"
//    doLast {
//        val properties = Properties()
//        properties.setProperty("TYPES", "[thrive.Trie1<Int>, thrive.Trie2<Int>]")
//        TemplateProcessor(fromDir, intoDir).execute(properties)
//    }
//
//    inputs.dir(fromDir)
////    inputs.file("env/${envName}.properties")
//    outputs.dir(intoDir)
//}

val fmpp by configurations.creating
tasks {
    test {
        maxHeapSize = "12G"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    register("fmpp") {
        group = "sample"
        val fromDir = "$projectDir/src/templates"
        val intoDir = "$buildDir/generated/sources/benchmarks"
        doLast {
            ant.withGroovyBuilder {
                "taskdef"(
                    "name" to "fmpp",
                    "classname" to "fmpp.tools.AntTask",
                    "classpath" to fmpp.asPath
                )
                "fmpp"("sourceRoot" to fromDir, "outputRoot" to intoDir)
            }
        }
    }
}


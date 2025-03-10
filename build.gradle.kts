import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    kotlin("plugin.serialization") version("2.1.0")
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.nbogdanov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    val ktor_version = "3.1.1"
    intellijPlatform {
        create("IC", "2024.2.5")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add necessary plugin dependencies for compilation here, example:
        // bundledPlugin("com.intellij.java")
    }
    implementation("io.ktor:ktor-client-jetty-jakarta:${ktor_version}")
    implementation("com.xemantic.ai:xemantic-ai-tool-schema:1.0.0")
    implementation("com.openai:openai-java:0.33.0")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
        }

        changeNotes = """
      Initial version
    """.trimIndent()
    }
}

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}
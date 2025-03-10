plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    kotlin("plugin.serialization") version("2.1.0")
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.nbogdanov"
version = "0.0.1"

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
        implementation("com.openai:openai-java:0.33.0")
        runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
        // Add necessary plugin dependencies for compilation here, example:
        // bundledPlugin("com.intellij.java")
    }

}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "251.*"
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
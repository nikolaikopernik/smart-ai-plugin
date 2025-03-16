import org.jetbrains.intellij.platform.gradle.TestFrameworkType

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
    intellijPlatform {
        create("IC", "2024.2.5")

        // lets get some support for Java and Kotlin compilation structures
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")

        implementation("com.openai:openai-java:0.33.0")

        testFramework(TestFrameworkType.Platform)
        testImplementation("org.mockito:mockito-core:5.16.0")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0") // Kotlin support
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
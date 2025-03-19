import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    kotlin("plugin.serialization") version ("2.1.0")
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
        intellijIdeaCommunity("2024.2.5")

        // lets get some support for Java and Kotlin compilation structures
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")

        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Bundled)
        testImplementation("org.mockito:mockito-core:5.16.0")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0") // Kotlin support
    }
    implementation("com.openai:openai-java:0.33.0")

    testRuntimeOnly("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
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

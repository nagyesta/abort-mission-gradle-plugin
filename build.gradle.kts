import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version libs.versions.kotlin.get()
    `maven-publish`
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.versioner)
    alias(libs.plugins.index.scan)
    alias(libs.plugins.owasp.dependencycheck)
}

group = "com.github.nagyesta.abort-mission"

versioner {
    startFrom {
        major = 0
        minor = 5
        patch = 0
    }
    match {
        major = "{major}"
        minor = "{minor}"
        patch = "{patch}"
    }
    pattern {
        pattern = "%M.%m.%p"
    }
    git {
        authentication {
            https {
                token = project.properties["githubToken"]?.toString()
            }
        }
    }
    tag {
        prefix = "v"
        useCommitMessage = true
    }
}

tasks {
    withType<KotlinCompile> { kotlinOptions { jvmTarget = "11" } }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.jupiter.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(gradleTestKit())
}

gradlePlugin {
    website.set("https://github.com/nagyesta/abort-mission-gradle-plugin")
    vcsUrl.set("https://github.com/nagyesta/abort-mission-gradle-plugin")
    plugins {
        create("abortMissionPlugin") {
            displayName = "Abort-Mission Gradle Plugin"
            description = "Adds Abort-Mission reporting support to your Gradle build"
            id = "com.github.nagyesta.abort-mission-gradle-plugin"
            implementationClass = "com.github.nagyesta.abortmission.gradle.AbortMissionPlugin"
            tags.set(listOf("testing", "jupiter", "testng", "abort-mission"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/nagyesta/abort-mission-gradle-plugin")
            credentials {
                username = project.properties["githubUser"]?.toString()
                password = project.properties["githubToken"]?.toString()
            }
        }
    }

    tasks.withType(GenerateModuleMetadata::class.java) {
        enabled = false
    }
}

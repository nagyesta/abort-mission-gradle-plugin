plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
    id("io.toolebox.git-versioner") version "1.6.5"
}

group = "com.github.nagyesta.abort-mission"
version = "0.2.1"

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
        pattern = "%M.%m.%p.1"
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

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.72")
}

gradlePlugin {
    plugins {
        create("abortMissionPlugin") {
            displayName = "Abort-Mission Gradle Plugin"
            description = "Adds Abort-Mission reporting support to your Gradle build"
            id = "com.github.nagyesta.abort-mission-gradle-plugin"
            implementationClass = "com.github.nagyesta.abortmission.gradle.AbortMissionPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/nagyesta/abort-mission-gradle-plugin"
    vcsUrl = "https://github.com/nagyesta/abort-mission-gradle-plugin"
    tags = listOf("testing", "jupiter", "testng", "abort-mission")
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc"))
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
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

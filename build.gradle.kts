import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version libs.versions.kotlin.get()
    `maven-publish`
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.versioner)
    alias(libs.plugins.index.scan)
    alias(libs.plugins.owasp.dependencycheck)
    alias(libs.plugins.cyclonedx.bom)
    alias(libs.plugins.licensee.plugin)
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
    withType<KotlinCompile> { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }
}

dependencies {
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.jupiter.core)
    testImplementation(gradleTestKit())

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.cyclonedxBom {
    setIncludeConfigs(listOf("runtimeClasspath"))
    setSkipConfigs(listOf("compileClasspath", "testCompileClasspath"))
    setSkipProjects(listOf())
    setProjectType("library")
    setSchemaVersion("1.5")
    setDestination(file("build/reports"))
    setOutputName("bom")
    setOutputFormat("json")
    //noinspection UnnecessaryQualifiedReference
    val attachmentText = org.cyclonedx.model.AttachmentText()
    attachmentText.setText(
        Base64.getEncoder().encodeToString(
            file("${project.rootProject.projectDir}/LICENSE").readBytes()
        )
    )
    attachmentText.encoding = "base64"
    attachmentText.contentType = "text/plain"
    //noinspection UnnecessaryQualifiedReference
    val license = org.cyclonedx.model.License()
    license.name = "MIT License"
    license.setLicenseText(attachmentText)
    license.url = "https://raw.githubusercontent.com/nagyesta/abort-mission-gradle-plugin/main/LICENSE"
    setLicenseChoice {
        it.addLicense(license)
    }
}

licensee {
    allow("Apache-2.0")
}

val copyLegalDocs = tasks.register<Copy>("copyLegalDocs") {
    group = "documentation"
    description = "Copies legal files and reports."
    from(file("${project.rootProject.projectDir}/LICENSE"))
    from(layout.buildDirectory.file("reports/licensee/artifacts.json").get().asFile)
    from(layout.buildDirectory.file("reports/bom.json").get().asFile)
    into(layout.buildDirectory.dir("resources/main/META-INF").get().asFile)
    rename("artifacts.json", "dependency-licenses.json")
    rename("bom.json", "SBOM.json")
}.get()
copyLegalDocs.dependsOn(tasks.licensee)
copyLegalDocs.dependsOn(tasks.cyclonedxBom)
tasks.javadoc.get().dependsOn(copyLegalDocs)
tasks.jar.get().dependsOn(copyLegalDocs)
tasks.pluginUnderTestMetadata.get().dependsOn(copyLegalDocs)
tasks.processResources.get().finalizedBy(copyLegalDocs)

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

plugins {
    java
    id("com.github.nagyesta.abort-mission-gradle-plugin")
}

group = "com.github.nagyesta.abort-mission.test"
version = "0.0.1"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("com.github.nagyesta.abort-mission.boosters:abort.booster-junit-jupiter:2.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

abortMission {
    skipTestAutoSetup = true
    toolVersion = "2.3.0"
}

repositories {
    maven {
        name = "BintrayNagyEstaMaven"
        url = uri("https://dl.bintray.com/nagyesta/releases-maven")
    }
    mavenCentral()
}

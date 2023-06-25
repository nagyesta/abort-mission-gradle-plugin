plugins {
    java
    id("com.github.nagyesta.abort-mission-gradle-plugin")
}

group = "com.github.nagyesta.abort-mission.test"
version = "0.0.1"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("com.github.nagyesta.abort-mission.boosters:abort.booster-junit-jupiter:4.0.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

abortMission {
    skipTestAutoSetup = true
    toolVersion = "3.3.0"
}

repositories {
    mavenCentral()
}

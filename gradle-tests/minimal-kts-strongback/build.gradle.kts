plugins {
    java
    id("com.github.nagyesta.abort-mission-gradle-plugin")
}

group = "com.github.nagyesta.abort-mission.test"
version = "0.0.1"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("com.github.nagyesta.abort-mission.boosters:abort.booster-junit-jupiter:2.6.1")
    testImplementation("com.github.nagyesta.abort-mission.strongback:abort.strongback-rmi-supplier:2.6.1")
}

abortMission {
    strongbackCoordinates = "com.github.nagyesta.abort-mission.strongback:abort.strongback-rmi-supplier"
    strongbackDelay = 10L
    strongbackPort = 30001
}

tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

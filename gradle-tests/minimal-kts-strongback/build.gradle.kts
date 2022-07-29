plugins {
    java
    id("com.github.nagyesta.abort-mission-gradle-plugin")
}

group = "com.github.nagyesta.abort-mission.test"
version = "0.0.1"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("com.github.nagyesta.abort-mission.boosters:abort.booster-junit-jupiter:3.3.0")
    testImplementation("com.github.nagyesta.abort-mission.strongback:abort.strongback-rmi-supplier:3.3.0")
}

abortMission {
    skipStrongbackConfig = false
    strongbackPort = 30001
}

tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

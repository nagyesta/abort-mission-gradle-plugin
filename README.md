![Abort-Mission](.github/assets/Abort-Mission-logo_export_transparent_640.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/abort-mission-gradle-plugin?color=informational)](https://raw.githubusercontent.com/nagyesta/abort-mission-gradle-plugin/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-17-yellow?logo=java)](https://img.shields.io/badge/Java%20version-17-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/abort-mission-gradle-plugin?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/abort-mission-gradle-plugin/releases)
[![Gradle Plugin](https://img.shields.io/badge/gradle-plugin-blue?logo=gradle)](https://plugins.gradle.org/plugin/com.github.nagyesta.abort-mission-gradle-plugin)
[![JavaCI](https://img.shields.io/github/actions/workflow/status/nagyesta/abort-mission-gradle-plugin/gradle.yml?logo=github&branch=main)](https://github.com/nagyesta/abort-mission-gradle-plugin/actions/workflows/gradle.yml)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=nagyesta_abort-mission-gradle-plugin&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=nagyesta_abort-mission-gradle-plugin)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=nagyesta_abort-mission-gradle-plugin&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=nagyesta_abort-mission-gradle-plugin)
[![last_commit](https://img.shields.io/github/last-commit/nagyesta/abort-mission-gradle-plugin?logo=git)](https://img.shields.io/github/last-commit/nagyesta/abort-mission-gradle-plugin?logo=git)
[![wiki](https://img.shields.io/badge/See-Wiki-informational)](https://github.com/nagyesta/abort-mission/wiki)

Abort-Mission is a lightweight Java library providing flexible test abortion support for test groups to allow fast
failures.

This project provides Gradle integration for Abort-Mission report generation.

## Installation

Abort-Mission can be downloaded from a few Maven repositories. Please head to
[this page](https://github.com/nagyesta/abort-mission/wiki/Configuring-our-repository-for-your-build-system)
to find out more.

### Minimal configuration

**build.gradle**

```groovy
plugins {
    id "com.github.nagyesta.abort-mission-gradle-plugin" version "5.0.0"
}

repositories {
    mavenCentral()
}
```

### Configuration properties

The plugin can be configured using the following DSL

**build.gradle**

```groovy
abortMission {
    //Set the version of the Abort-Mission report generator we want
    //to automatically download and run
    toolVersion "+"
    //Set whether we want Abort-Mission to automatically enhance the
    //"test" task by
    // - Adding the "abort-mission.report.directory" System.property
    //   to define where we want to store the JSON report
    // - Setting the "abortMissionReport" task to the "finalize" the
    //   test task
    skipTestAutoSetup false
    //Define whether we want to use relaxed schema for JSON validation.
    //Particularly useful when the Cucumber Booster is used or in case of
    //a very special class name that does not match the basic regexp used.
    relaxedValidation false
    //Sets the directory where we want to look for the JSON input file
    //and save the HTML output
    reportDirectory layout.buildDirectory.dir("reports/abort-mission/").get().getAsFile()
    //Controls whether the report generator should fail if any failed
    //test cases where in the report
    failOnError false
}
```

## About the reports

[Flight Evaluation Report explained](https://github.com/nagyesta/abort-mission/wiki/Flight-Evaluation-Report-explained)

## Limitations

- Strongbacks lifecycle is no longer supported while using 2.2.0 or later version of this plugin, or Gradle version above 7.2 (non-inclusive).
- Strongback functionality is completely removed in 4.1.x release as Strongbacks were very slow and hard to maintain.

package com.github.nagyesta.abortmission.gradle

import com.github.nagyesta.abortmission.gradle.AbortMissionPlugin.Constants.DEFAULT_RELAXED_VALIDATION
import com.github.nagyesta.abortmission.gradle.AbortMissionPlugin.Constants.DEFAULT_REPORT_DIRECTORY
import com.github.nagyesta.abortmission.gradle.AbortMissionPlugin.Constants.DEFAULT_SKIP_TEST_AUTO_SETUP
import com.github.nagyesta.abortmission.gradle.AbortMissionPlugin.Constants.DEFAULT_VERSION
import java.io.File

open class AbortMissionConfig(project: org.gradle.api.Project) {
    var skipTestAutoSetup = DEFAULT_SKIP_TEST_AUTO_SETUP
    var relaxedValidation = DEFAULT_RELAXED_VALIDATION
    var reportDirectory = File(project.buildDir.absolutePath + DEFAULT_REPORT_DIRECTORY)
    var toolVersion = DEFAULT_VERSION
}

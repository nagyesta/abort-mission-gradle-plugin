package com.github.nagyesta.abortmission.gradle

import org.gradle.api.Project
import java.io.File

open class AbortMissionConfig(project: Project) {
    var skipTestAutoSetup: Boolean
    var relaxedValidation: Boolean
    var reportDirectory: File
    var toolVersion: String

    companion object {
        const val DEFAULT_VERSION = "+"
        const val DEFAULT_RELAXED_VALIDATION = false
        const val DEFAULT_SKIP_TEST_AUTO_SETUP = false
        const val DEFAULT_REPORT_DIRECTORY = "/reports/abort-mission/"
    }

    init {
        skipTestAutoSetup = DEFAULT_SKIP_TEST_AUTO_SETUP
        relaxedValidation = DEFAULT_RELAXED_VALIDATION
        reportDirectory = File(project.buildDir, DEFAULT_REPORT_DIRECTORY)
        toolVersion = DEFAULT_VERSION
    }
}

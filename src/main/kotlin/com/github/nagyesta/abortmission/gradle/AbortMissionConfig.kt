package com.github.nagyesta.abortmission.gradle

import org.gradle.api.Project
import java.io.File

open class AbortMissionConfig(project: Project) {
    var skipTestAutoSetup: Boolean
    var relaxedValidation: Boolean
    var reportDirectory: File
    var toolVersion: String
    var failOnError: Boolean
    var strongbackCoordinates: String
    var strongbackPort: Int
    var strongbackPassword: String
    var strongbackUseExternal: Boolean
    var strongbackDelay: Long

    companion object {
        const val DEFAULT_VERSION = "+"
        const val DEFAULT_RELAXED_VALIDATION = false
        const val DEFAULT_SKIP_TEST_AUTO_SETUP = false
        const val DEFAULT_REPORT_DIRECTORY = "/reports/abort-mission/"
        const val DEFAULT_FAIL_ON_ERROR = false
        const val DEFAULT_STRONGBACK_COORDINATES = ""
        const val DEFAULT_STRONGBACK_PORT = 0
        const val DEFAULT_STRONGBACK_PASSWORD = ""
        const val DEFAULT_STRONGBACK_USE_EXTERNAL = false
        const val DEFAULT_STRONGBACK_DELAY = 50L
    }

    init {
        skipTestAutoSetup = DEFAULT_SKIP_TEST_AUTO_SETUP
        relaxedValidation = DEFAULT_RELAXED_VALIDATION
        reportDirectory = File(project.buildDir, DEFAULT_REPORT_DIRECTORY)
        toolVersion = DEFAULT_VERSION
        failOnError = DEFAULT_FAIL_ON_ERROR
        strongbackCoordinates = DEFAULT_STRONGBACK_COORDINATES
        strongbackPort = DEFAULT_STRONGBACK_PORT
        strongbackPassword = DEFAULT_STRONGBACK_PASSWORD
        strongbackUseExternal = DEFAULT_STRONGBACK_USE_EXTERNAL
        strongbackDelay = DEFAULT_STRONGBACK_DELAY
    }
}

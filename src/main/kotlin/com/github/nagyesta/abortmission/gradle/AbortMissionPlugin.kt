package com.github.nagyesta.abortmission.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import java.io.File

class AbortMissionPlugin : Plugin<Project> {

    companion object Constants {
        const val GROUP_ID = "com.github.nagyesta.abort-mission.reports"
        const val ARTIFACT_ID = "abort.flight-evaluation-report"
        const val EXTENSION_NAME = "abortMission"
        const val CONFIGURATION_NAME = "abortMissionReporting"
        const val TASK_NAME = "abortMissionReport"
        const val REPORT_DIR_PROPERTY = "abort-mission.report.directory"
        const val DEFAULT_VERSION = "+"
        const val DEFAULT_RELAXED_VALIDATION = false
        const val DEFAULT_SKIP_TEST_AUTO_SETUP = false
        const val DEFAULT_REPORT_DIRECTORY = "/reports/abort-mission/"
        const val DEFAULT_JSON_FILE_NAME = "abort-mission-report.json"
        const val DEFAULT_HTML_FILE_NAME = "abort-mission-report.html"
    }

    override fun apply(project: Project) {
        with(project) {
            extensions.create(EXTENSION_NAME, AbortMissionConfig::class.java, project)
            configurations.create(CONFIGURATION_NAME)
                .setVisible(false)
                .description = "Abort-Mission report generator"
            afterEvaluate(this@AbortMissionPlugin::afterEvaluateHandler)
        }
    }

    private fun afterEvaluateHandler(project: Project) {
        val abortMissionConfig = project.extensions
            .findByType(AbortMissionConfig::class.java) ?: AbortMissionConfig(project)
        val toolVersion = abortMissionConfig.toolVersion
        val skipTestPluginSetup = abortMissionConfig.skipTestAutoSetup
        project.dependencies.add(CONFIGURATION_NAME, "$GROUP_ID:$ARTIFACT_ID:$toolVersion")

        val abortMissionTask = defineAbortMissionReportTask(abortMissionConfig, project)
        if (!skipTestPluginSetup) {
            setupTestTask(project, abortMissionConfig, abortMissionTask)
        }
    }

    private fun setupTestTask(
        project: Project,
        abortMissionConfig: AbortMissionConfig,
        abortMissionTask: JavaExec
    ) {
        project.tasks.withType(Test::class.java).findByName("test")!!.apply {
            this.systemProperty(
                REPORT_DIR_PROPERTY, abortMissionConfig.reportDirectory.absolutePath
            )
            this.finalizedBy(abortMissionTask)
        }
    }

    private fun defineAbortMissionReportTask(
        abortMissionConfig: AbortMissionConfig,
        project: Project
    ): JavaExec {
        val htmlFile = File(abortMissionConfig.reportDirectory, DEFAULT_HTML_FILE_NAME)
        val jsonFile = File(abortMissionConfig.reportDirectory, DEFAULT_JSON_FILE_NAME)
        return project.tasks.create(TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.inputs.file(jsonFile)
            javaTask.outputs.file(htmlFile)
            javaTask.main = "org.springframework.boot.loader.JarLauncher"
            javaTask.classpath = project.configurations.findByName(CONFIGURATION_NAME)!!.asFileTree
            javaTask.args = listOf(
                "--report.input=$jsonFile",
                "--report.output=$htmlFile",
                "--report.relaxed=${abortMissionConfig.relaxedValidation}"
            )
            javaTask.logging.captureStandardOutput(LogLevel.INFO)
            javaTask.logging.captureStandardError(LogLevel.ERROR)
        }
    }
}

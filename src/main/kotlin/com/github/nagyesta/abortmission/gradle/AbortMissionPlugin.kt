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
        const val DEFAULT_JSON_FILE_NAME = "abort-mission-report.json"
        const val DEFAULT_HTML_FILE_NAME = "abort-mission-report.html"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, AbortMissionConfig::class.java, project)
        project.configurations.create(CONFIGURATION_NAME)
            .setVisible(false)
            .description = "Abort-Mission report generator"
        project.afterEvaluate(this@AbortMissionPlugin::afterEvaluateHandler)
    }

    private fun afterEvaluateHandler(project: Project) {
        val abortMissionConfig = project.extensions.findByType(AbortMissionConfig::class.java)
        val toolVersion = abortMissionConfig?.toolVersion ?: AbortMissionConfig.DEFAULT_VERSION
        val skipTestPluginSetup = abortMissionConfig?.skipTestAutoSetup
            ?: AbortMissionConfig.DEFAULT_SKIP_TEST_AUTO_SETUP
        project.dependencies.add(CONFIGURATION_NAME, "$GROUP_ID:$ARTIFACT_ID:$toolVersion")

        val reportDirectory = abortMissionConfig?.reportDirectory
            ?: File(project.buildDir, AbortMissionConfig.DEFAULT_REPORT_DIRECTORY)
        val abortMissionTask = defineAbortMissionReportTask(
            reportDirectory,
            abortMissionConfig?.relaxedValidation ?: AbortMissionConfig.DEFAULT_RELAXED_VALIDATION,
            project
        )
        if (!skipTestPluginSetup) {
            setupTestTask(project, reportDirectory, abortMissionTask)
        }
    }

    private fun setupTestTask(
        project: Project,
        reportDirectory: File,
        abortMissionTask: JavaExec
    ) {
        project.tasks.withType(Test::class.java).findByName("test")!!.apply {
            this.systemProperty(
                REPORT_DIR_PROPERTY, reportDirectory.absolutePath
            )
            this.finalizedBy(abortMissionTask)
        }
    }

    private fun defineAbortMissionReportTask(
        reportDirectory: File,
        relaxedValidation: Boolean,
        project: Project
    ): JavaExec {
        val htmlFile = File(reportDirectory, DEFAULT_HTML_FILE_NAME)
        val jsonFile = File(reportDirectory, DEFAULT_JSON_FILE_NAME)
        return project.tasks.create(TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.inputs.file(jsonFile)
            javaTask.outputs.file(htmlFile)
            javaTask.main = "org.springframework.boot.loader.JarLauncher"
            javaTask.classpath = project.configurations.findByName(CONFIGURATION_NAME)!!.asFileTree
            javaTask.args = listOf(
                "--report.input=$jsonFile",
                "--report.output=$htmlFile",
                "--report.relaxed=${relaxedValidation}"
            )
            javaTask.logging.captureStandardOutput(LogLevel.INFO)
            javaTask.logging.captureStandardError(LogLevel.ERROR)
        }
    }
}

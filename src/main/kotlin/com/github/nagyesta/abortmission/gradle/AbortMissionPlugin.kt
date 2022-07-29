package com.github.nagyesta.abortmission.gradle

import org.gradle.api.DefaultTask
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
        const val TEST_IMPLEMENTATION_CONFIGURATION_NAME = "testImplementation"
        const val CUCUMBER_BOOSTER_GROUP_ID = "com.github.nagyesta.abort-mission.boosters"
        const val CUCUMBER_BOOSTER_ARTIFACT_ID = "abort.booster-cucumber-jvm"

        const val TASK_NAME = "abortMissionReport"

        const val REPORT_DIR_PROPERTY = "abort-mission.report.directory"
        const val DEFAULT_JSON_FILE_NAME = "abort-mission-report.json"
        const val DEFAULT_HTML_FILE_NAME = "abort-mission-report.html"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, AbortMissionConfig::class.java, project)
        project.configurations.create(CONFIGURATION_NAME)
            .setVisible(true)
            .description = "Abort-Mission report generator"
        project.afterEvaluate(this@AbortMissionPlugin::afterEvaluateHandler)
    }

    private fun afterEvaluateHandler(project: Project) {
        val abortMissionConfig = project.extensions
            .findByType(AbortMissionConfig::class.java) ?: AbortMissionConfig(project)
        project.dependencies.add(CONFIGURATION_NAME, "$GROUP_ID:$ARTIFACT_ID:${abortMissionConfig.toolVersion}")

        val abortMissionTask = defineAbortMissionReportTask(abortMissionConfig, project)
        setupTestTask(abortMissionConfig, abortMissionTask, project)
    }

    private fun hasCucumberDependency(
        project: Project
    ): Boolean {
        val testImpl = project.configurations.findByName(TEST_IMPLEMENTATION_CONFIGURATION_NAME)
        return testImpl?.dependencies?.any { dependency ->
            dependency.group.equals(CUCUMBER_BOOSTER_GROUP_ID)
                    && dependency.name.equals(CUCUMBER_BOOSTER_ARTIFACT_ID)
        } == true
    }

    private fun setupTestTask(
        abortMissionConfig: AbortMissionConfig,
        abortMissionTask: JavaExec,
        project: Project
    ) {
        forEachTestTasks(project, abortMissionConfig.skipTestAutoSetup) {
            if (!abortMissionConfig.skipStrongbackConfig) {
                if (abortMissionConfig.strongbackPassword.isNotBlank()) {
                    this.systemProperty(
                        "abort-mission.telemetry.server.password",
                        abortMissionConfig.strongbackPassword
                    )
                }
                if (abortMissionConfig.strongbackPort > 0) {
                    this.systemProperty(
                        "abort-mission.telemetry.server.port",
                        abortMissionConfig.strongbackPort
                    )
                }
            }
            this.systemProperty(REPORT_DIR_PROPERTY, abortMissionConfig.reportDirectory.absolutePath)
            this.finalizedBy(abortMissionTask)
        }
    }

    private fun defineAbortMissionReportTask(
        abortMissionConfig: AbortMissionConfig,
        project: Project
    ): JavaExec {
        val htmlFile = File(abortMissionConfig.reportDirectory, DEFAULT_HTML_FILE_NAME)
        val jsonFile = File(abortMissionConfig.reportDirectory, DEFAULT_JSON_FILE_NAME)
        val relaxedValidation = abortMissionConfig.relaxedValidation || hasCucumberDependency(project)
        return project.tasks.create(TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.inputs.file(jsonFile)
            javaTask.outputs.file(htmlFile)
            javaTask.mainClass.set("com.github.nagyesta.abortmission.reporting.AbortMissionFlightEvaluationReportApp")
            javaTask.workingDir = project.projectDir
            javaTask.classpath = project.configurations.findByName(CONFIGURATION_NAME)!!.asFileTree
            javaTask.args = listOf(
                "--report.input=${jsonFile.relativeTo(project.projectDir)}",
                "--report.output=${htmlFile.relativeTo(project.projectDir)}",
                "--report.relaxed=$relaxedValidation",
                "--report.failOnError=${abortMissionConfig.failOnError}"
            )
            redirectLogs(javaTask)
        }
    }

    private fun forEachTestTasks(
        project: Project,
        skipTestPluginSetup: Boolean,
        block: Test.() -> Unit
    ) {
        if (!skipTestPluginSetup) {
            project.tasks.withType(Test::class.java).findByName("test")!!.apply(block)
        }
    }

    private fun redirectLogs(task: DefaultTask) {
        task.logging.captureStandardOutput(LogLevel.INFO)
        task.logging.captureStandardError(LogLevel.ERROR)
    }

}

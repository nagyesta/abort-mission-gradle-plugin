package com.github.nagyesta.abortmission.gradle

import org.gradle.api.*
import org.gradle.api.file.FileTree
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import java.io.File

class AbortMissionPlugin : Plugin<Project> {

    companion object Constants {
        const val GROUP_ID = "com.github.nagyesta.abort-mission.reports"
        const val ARTIFACT_ID = "abort.flight-evaluation-report"
        const val STRONGBACK_GROUP_ID = "com.github.nagyesta.abort-mission.strongback"
        const val STRONGBACK_BASE_COORDINATES = "$STRONGBACK_GROUP_ID:abort.strongback-base"

        const val EXTENSION_NAME = "abortMission"

        const val CONFIGURATION_NAME = "abortMissionReporting"
        const val STRONGBACK_CONFIGURATION_NAME = "abortMissionStrongback"

        const val TASK_NAME = "abortMissionReport"
        const val STRONGBACK_ERECT_TASK_NAME = "abortMissionStrongbackErect"
        const val STRONGBACK_ERECT_JAVA_TASK_NAME = "abortMissionStrongbackErectJava"
        const val STRONGBACK_RETRACT_TASK_NAME = "abortMissionStrongbackRetract"

        const val REPORT_DIR_PROPERTY = "abort-mission.report.directory"
        const val DEFAULT_JSON_FILE_NAME = "abort-mission-report.json"
        const val DEFAULT_HTML_FILE_NAME = "abort-mission-report.html"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, AbortMissionConfig::class.java, project)
        project.configurations.create(CONFIGURATION_NAME)
            .setVisible(true)
            .description = "Abort-Mission report generator"
        project.configurations.create(STRONGBACK_CONFIGURATION_NAME)
            .setVisible(true)
            .description = "Abort-Mission Strongback classpath"
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
            abortMissionConfig?.failOnError ?: AbortMissionConfig.DEFAULT_FAIL_ON_ERROR,
            project
        )

        val strongbackCoordinates = abortMissionConfig?.strongbackCoordinates
            ?: AbortMissionConfig.DEFAULT_STRONGBACK_COORDINATES
        addStrongbackDependencies(project, strongbackCoordinates, toolVersion)

        if (strongbackCoordinates.isNotBlank()) {
            val port = abortMissionConfig?.strongbackPort ?: AbortMissionConfig.DEFAULT_STRONGBACK_PORT
            val password = abortMissionConfig?.strongbackPassword ?: AbortMissionConfig.DEFAULT_STRONGBACK_PASSWORD
            val useExternal = abortMissionConfig?.strongbackUseExternal
                ?: AbortMissionConfig.DEFAULT_STRONGBACK_USE_EXTERNAL
            val delayMs = abortMissionConfig?.strongbackDelay ?: AbortMissionConfig.DEFAULT_STRONGBACK_DELAY
            val erectTask = defineAbortMissionStrongbackErectTask(
                skipTestPluginSetup,
                port,
                password,
                useExternal,
                delayMs,
                project
            )
            defineAbortMissionStrongbackRetractTask(
                reportDirectory,
                skipTestPluginSetup,
                abortMissionTask,
                port,
                password,
                useExternal,
                erectTask,
                project
            )
        } else if (!skipTestPluginSetup) {
            setupTestTask(project, reportDirectory, abortMissionTask)
        }
    }

    private fun addStrongbackDependencies(
        project: Project,
        strongbackCoordinates: String,
        toolVersion: String
    ) {
        if (strongbackCoordinates.isNotBlank()) {
            if (hasNoVersion(strongbackCoordinates)) {
                project.dependencies.add(STRONGBACK_CONFIGURATION_NAME, "$strongbackCoordinates:$toolVersion")
            } else {
                project.dependencies.add(STRONGBACK_CONFIGURATION_NAME, strongbackCoordinates)
            }
            project.dependencies.add(STRONGBACK_CONFIGURATION_NAME, "$STRONGBACK_BASE_COORDINATES:$toolVersion")
        }
    }

    private fun hasNoVersion(strongbackCoordinates: String) =
        (strongbackCoordinates.split(':').size == 2
                && strongbackCoordinates.startsWith(STRONGBACK_GROUP_ID))

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
        failOnError: Boolean,
        project: Project
    ): JavaExec {
        val htmlFile = File(reportDirectory, DEFAULT_HTML_FILE_NAME)
        val jsonFile = File(reportDirectory, DEFAULT_JSON_FILE_NAME)
        return project.tasks.create(TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.inputs.file(jsonFile)
            javaTask.outputs.file(htmlFile)
            javaTask.main = "org.springframework.boot.loader.JarLauncher"
            javaTask.workingDir = project.projectDir
            javaTask.classpath = project.configurations.findByName(CONFIGURATION_NAME)!!.asFileTree
            javaTask.systemProperty("report.input", jsonFile.relativeTo(project.projectDir))
            javaTask.systemProperty("report.output", htmlFile.relativeTo(project.projectDir))
            javaTask.systemProperty("report.relaxed", relaxedValidation)
            javaTask.systemProperty("report.failOnError", failOnError)
            redirectLogs(javaTask)
        }
    }

    private fun defineAbortMissionStrongbackErectTask(
        skipTestPluginSetup: Boolean,
        port: Int,
        password: String,
        useExternal: Boolean,
        delayMs: Long,
        project: Project
    ): AsyncJavaExecTask {
        val strongbackJars = project.configurations.findByName(STRONGBACK_CONFIGURATION_NAME)
        val classPath = strongbackJars!!.asFileTree
        val javaTask = project.tasks.create(STRONGBACK_ERECT_JAVA_TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.main = "com.github.nagyesta.abortmission.strongback.StrongbackErectorMain"
            addCommonStrongbackProperties(javaTask, useExternal, password, port, classPath, project.projectDir)
            redirectLogs(javaTask)
        }
        return project.tasks.create(STRONGBACK_ERECT_TASK_NAME, AsyncJavaExecTask::class.java) { task ->
            task.taskToExecute = javaTask
            task.delayMs = delayMs
            task.doFirst {
                strongbackJars.resolve()
            }
            redirectLogs(task)
            if (!skipTestPluginSetup) {
                project.tasks.withType(Test::class.java).findByName("test")!!.apply {
                    this.dependsOn(task)
                }
            }
        }
    }

    private fun defineAbortMissionStrongbackRetractTask(
        reportDirectory: File,
        skipTestPluginSetup: Boolean,
        reportTask: JavaExec,
        port: Int,
        password: String,
        useExternal: Boolean,
        erectTask: AsyncJavaExecTask,
        project: Project
    ): JavaExec {
        val jsonFile = File(reportDirectory, DEFAULT_JSON_FILE_NAME)
        val classPath = project.configurations.findByName(STRONGBACK_CONFIGURATION_NAME)!!.asFileTree
        return project.tasks.create(STRONGBACK_RETRACT_TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.outputs.file(jsonFile)

            javaTask.main = "com.github.nagyesta.abortmission.strongback.StrongbackRetractorMain"
            addCommonStrongbackProperties(javaTask, useExternal, password, port, classPath, project.projectDir)
            redirectLogs(javaTask)

            javaTask.systemProperty(REPORT_DIR_PROPERTY, reportDirectory.relativeTo(project.projectDir))

            if (!skipTestPluginSetup) {
                project.tasks.withType(Test::class.java).findByName("test")!!.apply {
                    this.finalizedBy(javaTask)
                }
                javaTask.finalizedBy(reportTask)
            }
            javaTask.doLast {
                erectTask.shutdown()
            }
        }
    }

    private fun redirectLogs(task: DefaultTask) {
        task.logging.captureStandardOutput(LogLevel.INFO)
        task.logging.captureStandardError(LogLevel.ERROR)
    }

    private fun addCommonStrongbackProperties(
        javaTask: JavaExec,
        useExternal: Boolean,
        password: String,
        port: Int,
        classPath: FileTree,
        workDir: File
    ) {
        javaTask.classpath = classPath
        javaTask.workingDir = workDir
        if (useExternal) {
            javaTask.systemProperty("abort-mission.telemetry.server.useExternal", useExternal)
        }
        if (password.isNotBlank()) {
            javaTask.systemProperty("abort-mission.telemetry.server.password", password)
        }
        if (port > 0) {
            javaTask.systemProperty("abort-mission.telemetry.server.port", port)
        }
    }
}

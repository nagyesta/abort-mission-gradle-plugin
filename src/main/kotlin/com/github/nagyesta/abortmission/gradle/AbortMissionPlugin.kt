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
        val abortMissionConfig = project.extensions
                .findByType(AbortMissionConfig::class.java) ?: AbortMissionConfig(project)
        project.dependencies.add(CONFIGURATION_NAME, "$GROUP_ID:$ARTIFACT_ID:${abortMissionConfig.toolVersion}")

        val abortMissionTask = defineAbortMissionReportTask(abortMissionConfig, project)

        addStrongbackDependencies(abortMissionConfig, project)

        if (abortMissionConfig.strongbackCoordinates.isNotBlank()) {
            val erectTask = defineAbortMissionStrongbackErectTask(abortMissionConfig, project)
            defineAbortMissionStrongbackRetractTask(
                    abortMissionConfig,
                    abortMissionTask,
                    erectTask,
                    project
            )
        } else {
            setupTestTask(abortMissionConfig, abortMissionTask, project)
        }
    }

    private fun addStrongbackDependencies(
            abortMissionConfig: AbortMissionConfig,
            project: Project
    ) {
        val strongbackCoordinates = abortMissionConfig.strongbackCoordinates
        val toolVersion = abortMissionConfig.toolVersion
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
            abortMissionConfig: AbortMissionConfig,
            abortMissionTask: JavaExec,
            project: Project
    ) {
        forEachTestTasks(project, abortMissionConfig.skipTestAutoSetup) {
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
        return project.tasks.create(TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.inputs.file(jsonFile)
            javaTask.outputs.file(htmlFile)
            javaTask.main = "org.springframework.boot.loader.JarLauncher"
            javaTask.workingDir = project.projectDir
            javaTask.classpath = project.configurations.findByName(CONFIGURATION_NAME)!!.asFileTree
            javaTask.systemProperty("report.input", jsonFile.relativeTo(project.projectDir))
            javaTask.systemProperty("report.output", htmlFile.relativeTo(project.projectDir))
            javaTask.systemProperty("report.relaxed", abortMissionConfig.relaxedValidation)
            javaTask.systemProperty("report.failOnError", abortMissionConfig.failOnError)
            redirectLogs(javaTask)
        }
    }

    private fun defineAbortMissionStrongbackErectTask(
            abortMissionConfig: AbortMissionConfig,
            project: Project
    ): AsyncJavaExecTask {
        val strongbackJars = project.configurations.findByName(STRONGBACK_CONFIGURATION_NAME)
        val classPath = strongbackJars!!.asFileTree
        val javaTask = project.tasks.create(STRONGBACK_ERECT_JAVA_TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.main = "com.github.nagyesta.abortmission.strongback.StrongbackErectorMain"
            addCommonStrongbackProperties(javaTask, abortMissionConfig, classPath, project.projectDir)
            redirectLogs(javaTask)
        }
        return project.tasks.create(STRONGBACK_ERECT_TASK_NAME, AsyncJavaExecTask::class.java) { task ->
            task.taskToExecute = javaTask
            task.delayMs = abortMissionConfig.strongbackDelay
            task.doFirst {
                strongbackJars.resolve()
            }
            redirectLogs(task)
            forEachTestTasks(project, abortMissionConfig.skipTestAutoSetup) {
                this.dependsOn(task)
            }
        }
    }

    private fun defineAbortMissionStrongbackRetractTask(
            abortMissionConfig: AbortMissionConfig,
            reportTask: JavaExec,
            erectTask: AsyncJavaExecTask,
            project: Project
    ): JavaExec {
        val classPath = project.configurations.findByName(STRONGBACK_CONFIGURATION_NAME)!!.asFileTree
        return project.tasks.create(STRONGBACK_RETRACT_TASK_NAME, JavaExec::class.java) { javaTask ->
            javaTask.outputs.file(File(abortMissionConfig.reportDirectory, DEFAULT_JSON_FILE_NAME))
            javaTask.main = "com.github.nagyesta.abortmission.strongback.StrongbackRetractorMain"
            addCommonStrongbackProperties(javaTask, abortMissionConfig, classPath, project.projectDir)
            disableUpToDateChecks(javaTask)
            redirectLogs(javaTask)
            javaTask.systemProperty(REPORT_DIR_PROPERTY,
                    abortMissionConfig.reportDirectory.relativeTo(project.projectDir))
            forEachTestTasks(project, abortMissionConfig.skipTestAutoSetup) {
                this.finalizedBy(javaTask)
            }
            javaTask.finalizedBy(reportTask)
            javaTask.doLast {
                erectTask.shutdown()
            }
        }
    }

    private fun disableUpToDateChecks(task: Task) {
        task.outputs.upToDateWhen {
            false
        }
    }

    private fun forEachTestTasks(project: Project,
                                 skipTestPluginSetup: Boolean,
                                 block: Test.() -> Unit) {
        if (!skipTestPluginSetup) {
            project.tasks.withType(Test::class.java).findByName("test")!!.apply(block)
        }
    }

    private fun redirectLogs(task: DefaultTask) {
        task.logging.captureStandardOutput(LogLevel.INFO)
        task.logging.captureStandardError(LogLevel.ERROR)
    }

    private fun addCommonStrongbackProperties(
            javaTask: JavaExec,
            abortMissionConfig: AbortMissionConfig,
            classPath: FileTree,
            workDir: File
    ) {
        javaTask.classpath = classPath
        javaTask.workingDir = workDir
        if (abortMissionConfig.strongbackUseExternal) {
            javaTask.systemProperty("abort-mission.telemetry.server.useExternal", true)
        }
        if (abortMissionConfig.strongbackPassword.isNotBlank()) {
            javaTask.systemProperty("abort-mission.telemetry.server.password",
                    abortMissionConfig.strongbackPassword)
        }
        if (abortMissionConfig.strongbackPort > 0) {
            javaTask.systemProperty("abort-mission.telemetry.server.port", abortMissionConfig.strongbackPort)
        }
    }
}

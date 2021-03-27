package com.github.nagyesta.abortmission.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

open class AsyncJavaExecTask : DefaultTask() {
    @get:Input
    lateinit var taskToExecute: JavaExec
    @get:Input
    var delayMs: Long = 1000L
    private val executorService: ExecutorService = Executors.newFixedThreadPool(1)

    @TaskAction
    fun startAsync() {
        logger.log(LogLevel.INFO, "Submitting async task: ${taskToExecute.name}")
        executorService.execute { ->
            logger.log(LogLevel.INFO, "Starting execution of async task: ${taskToExecute.name}")
            taskToExecute.exec()
        }
        Thread.sleep(delayMs)
        logger.log(LogLevel.INFO, "Async task, ${taskToExecute.name} is running...")
    }

    fun shutdown() {
        if (!executorService.isShutdown) {
            logger.log(LogLevel.INFO, "Initiating shutdown on executor for task: ${taskToExecute.name}...")
            executorService.shutdownNow()
        }
    }
}

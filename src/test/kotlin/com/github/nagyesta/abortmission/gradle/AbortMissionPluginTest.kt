package com.github.nagyesta.abortmission.gradle

import com.github.nagyesta.abortmission.strongback.rmi.RmiStrongbackController
import com.github.nagyesta.abortmission.strongback.rmi.server.RmiServerManager
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.Stream

internal class AbortMissionPluginTest {

    @ParameterizedTest
    @ValueSource(strings = ["gradle-tests/minimal-kts", "gradle-tests/minimal-groovy"])
    fun testApplyShouldApplyPluginAndDoConfigWhenCalledWithAutoConfiguration(path: String) {
        //given

        //when
        val result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(path))
            .withArguments("clean", "test")
            .build()

        //then
        val output = result.output
        assertTrue(output.contains("abortMissionReport"))
        assertFalse(output.contains("abortMissionStrongbackErect"))
        assertFalse(output.contains("abortMissionStrongbackRetract"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":abortMissionReport")?.outcome)
        assertValidFile("$path/build/reports/abort-mission/abort-mission-report.html")
        assertValidFile("$path/build/reports/abort-mission/abort-mission-report.json")
    }

    @ParameterizedTest
    @MethodSource("strongbackProvider")
    fun testApplyShouldApplyPluginWithStrongbackAndDoConfigWhenCalledWithAutoConfiguration(path: String, port: Int) {
        //given
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        try {
            val rmiServerManager = RmiServerManager(port)
            val rmiController = RmiStrongbackController(rmiServerManager)
            executorService.execute {
                rmiController.erect()
            }
            Thread.sleep(10L)

            //when
            val result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(File(path))
                .withArguments("clean", "test")
                .build()

            //then
            rmiController.retract()
            val output = result.output
            assertTrue(output.contains("abortMissionReport"))
            assertEquals(TaskOutcome.SUCCESS, result.task(":abortMissionReport")?.outcome)
        } finally {
            if (!executorService.isShutdown) {
                executorService.shutdownNow()
            }
        }
        assertValidFile("$path/build/reports/abort-mission/abort-mission-report.html")
        assertValidFile("$path/build/reports/abort-mission/abort-mission-report.json")
    }

    private fun assertValidFile(path: String) {
        val file = File(path)
        assertTrue(file.exists())
        assertTrue(file.readLines().stream().anyMatch {
            it.contains("HelloTest")
        })
    }

    @ParameterizedTest
    @ValueSource(strings = ["gradle-tests/minimal-kts-off", "gradle-tests/minimal-groovy-off"])
    fun testApplyShouldApplyPluginAndSkipConfigWhenCalledWithoutAutoConfiguration(path: String) {
        //given

        //when
        val result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(File(path))
            .withArguments("clean", "test")
            .build()

        //then
        assertFalse(result.output.contains("abortMissionReport"))
    }

    companion object {
        @JvmStatic
        fun `strongbackProvider`(): Stream<Arguments> {
            return Stream.builder<Arguments>()
                .add(Arguments.of("gradle-tests/minimal-kts-strongback", 30001))
                .add(Arguments.of("gradle-tests/minimal-groovy-strongback", 30000))
                .build()
        }
    }
}

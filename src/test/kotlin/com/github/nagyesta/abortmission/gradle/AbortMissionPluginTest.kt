package com.github.nagyesta.abortmission.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

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
        assertEquals(TaskOutcome.SUCCESS, result.task(":abortMissionReport")?.outcome)
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
}

package com.github.nagyesta.example;
import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.abortmission.core.annotation.LaunchSequence;
import org.junit.jupiter.api.Test;

@LaunchAbortArmed
@LaunchSequence(MissionOutlineDefinition.class)
public class HelloTest {

    @Test
    void testMethod() {
        new Hello();
    }
}

package com.github.nagyesta.example;

import com.github.nagyesta.abortmission.core.AbortMissionCommandOps;
import com.github.nagyesta.abortmission.core.MissionControl;
import com.github.nagyesta.abortmission.core.outline.MissionOutline;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class MissionOutlineDefinition extends MissionOutline {

    @Override
    protected Map<String, Consumer<AbortMissionCommandOps>> defineOutline() {
        return Collections.singletonMap("", ops -> ops
                .registerHealthCheck(MissionControl.reportOnlyEvaluator(MissionControl.matcher().anyClass().build()).build())
        );
    }
}

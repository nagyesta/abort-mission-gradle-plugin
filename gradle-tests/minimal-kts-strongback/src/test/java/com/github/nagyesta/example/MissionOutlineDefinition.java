package com.github.nagyesta.example;

import com.github.nagyesta.abortmission.core.AbortMissionCommandOps;
import com.github.nagyesta.abortmission.core.MissionControl;
import com.github.nagyesta.abortmission.core.healthcheck.StageStatisticsCollectorFactory;
import com.github.nagyesta.abortmission.core.matcher.MissionHealthCheckMatcher;
import com.github.nagyesta.abortmission.core.outline.MissionOutline;
import com.github.nagyesta.abortmission.strongback.rmi.server.RmiServiceProvider;
import com.github.nagyesta.abortmission.strongback.rmi.stats.RmiBackedStageStatisticsCollectorFactory;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class MissionOutlineDefinition extends MissionOutline {

    @Override
    protected Map<String, Consumer<AbortMissionCommandOps>> defineOutline() {
        MissionHealthCheckMatcher matcher = MissionControl.matcher().anyClass().build();
        final StageStatisticsCollectorFactory factory = new RmiBackedStageStatisticsCollectorFactory(
                "", RmiServiceProvider.lookupRegistry(30001));
        return Collections.singletonMap("", ops -> {
            ops.registerHealthCheck(MissionControl.reportOnlyEvaluator(matcher, factory).build());
        });
    }
}

package io.kestra.plugin.liquibase;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.kestra.core.queues.DispatchQueueInterface;
import org.junit.jupiter.api.*;

import com.google.common.collect.ImmutableMap;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.executions.LogEntry;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;
import io.kestra.plugin.scripts.exec.scripts.models.ScriptOutput;
import io.kestra.plugin.scripts.runner.docker.Docker;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@KestraTest
public class CLITest {
    @Inject
    RunContextFactory runContextFactory;

    @Inject
    private DispatchQueueInterface<LogEntry> logQueue;

    @Test
    void run() throws Exception {
        List<LogEntry> logs = new CopyOnWriteArrayList<>();
        logQueue.addListener(logs::add);

        CLI task = CLI.builder()
            .id("cli_snapshot")
            .type(CLI.class.getName())
            .taskRunner(Docker.builder().type(Docker.class.getName()).networkMode("dbnet").build())
            .commands(
                Property.ofValue(
                    List.of(
                        "liquibase snapshot " +
                            "--url=jdbc:postgresql://db1:5432/demo1 " +
                            "--username=user1 --password=pass1"
                    )
                )
            )
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, ImmutableMap.of());
        ScriptOutput run = task.run(runContext);

        assertThat(run.getExitCode(), is(0));

        TestsUtils.awaitLog(logs, log -> log.getMessage() != null && log.getMessage().contains("demo1"));
        assertThat(logs.stream().anyMatch(l -> l.getMessage() != null && l.getMessage().contains("demo")), is(true));
    }
}

package io.kestra.plugin.liquibase;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.executions.LogEntry;
import io.kestra.core.models.property.Property;
import io.kestra.core.queues.QueueFactoryInterface;
import io.kestra.core.queues.QueueInterface;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.utils.TestsUtils;
import io.kestra.plugin.core.runner.Process;
import io.kestra.plugin.scripts.exec.scripts.models.ScriptOutput;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import reactor.core.publisher.Flux;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@KestraTest
class BeforeCommandsTest {
    @Inject
    RunContextFactory runContextFactory;

    @Inject
    @Named(QueueFactoryInterface.WORKERTASKLOG_NAMED)
    private QueueInterface<LogEntry> logQueue;

    @Test
    void cliRunsBeforeCommands() throws Exception {
        List<LogEntry> logs = new CopyOnWriteArrayList<>();
        Flux<LogEntry> receive = TestsUtils.receive(logQueue, l -> logs.add(l.getLeft()));

        CLI task = CLI.builder()
            .id("cli_before")
            .type(CLI.class.getName())
            .taskRunner(Process.instance())
            .beforeCommands(Property.ofValue(List.of("echo BEFORE_RAN")))
            .commands(Property.ofValue(List.of("echo MAIN_RAN")))
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, ImmutableMap.of());
        ScriptOutput run = task.run(runContext);

        assertThat(run.getExitCode(), is(0));
        TestsUtils.awaitLog(logs, log -> log.getMessage() != null && log.getMessage().contains("MAIN_RAN"));
        receive.blockLast();
        assertThat(logs.stream().anyMatch(l -> l.getMessage() != null && l.getMessage().contains("BEFORE_RAN")), is(true));
    }

    @Test
    void diffRunsBeforeCommands() throws Exception {
        List<LogEntry> logs = new CopyOnWriteArrayList<>();
        Flux<LogEntry> receive = TestsUtils.receive(logQueue, l -> logs.add(l.getLeft()));

        Diff task = Diff.builder()
            .id("diff_before")
            .type(Diff.class.getName())
            .taskRunner(Process.instance())
            .url(Property.ofValue("jdbc:h2:mem:a"))
            .referenceUrl(Property.ofValue("jdbc:h2:mem:b"))
            .beforeCommands(Property.ofValue(List.of("echo BEFORE_RAN")))
            .commands(Property.ofValue(List.of("echo MAIN_RAN")))
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, ImmutableMap.of());
        ScriptOutput run = task.run(runContext);

        assertThat(run.getExitCode(), is(0));
        TestsUtils.awaitLog(logs, log -> log.getMessage() != null && log.getMessage().contains("MAIN_RAN"));
        receive.blockLast();
        assertThat(logs.stream().anyMatch(l -> l.getMessage() != null && l.getMessage().contains("BEFORE_RAN")), is(true));
    }
}

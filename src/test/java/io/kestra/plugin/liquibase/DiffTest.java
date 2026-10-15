package io.kestra.plugin.liquibase;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.*;

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
import io.kestra.plugin.scripts.runner.docker.Docker;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import reactor.core.publisher.Flux;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@KestraTest
public class DiffTest {
    @Inject
    RunContextFactory runContextFactory;

    @Inject
    @Named(QueueFactoryInterface.WORKERTASKLOG_NAMED)
    private QueueInterface<LogEntry> logQueue;

    @Test
    void run() throws Exception {
        List<LogEntry> logs = new CopyOnWriteArrayList<>();
        Flux<LogEntry> receive = TestsUtils.receive(logQueue, l -> logs.add(l.getLeft()));

        Diff task = Diff.builder()
            .id("db_diff")
            .type(Diff.class.getName())
            .taskRunner(Docker.builder().type(Docker.class.getName()).networkMode("dbnet").build())
            .url(Property.ofValue("jdbc:postgresql://db1:5432/demo1"))
            .username(Property.ofValue("user1"))
            .password(Property.ofValue("pass1"))
            .referenceUrl(Property.ofValue("jdbc:postgresql://db2:5432/demo2"))
            .referenceUsername(Property.ofValue("user2"))
            .referencePassword(Property.ofValue("pass2"))
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, ImmutableMap.of());
        ScriptOutput run = task.run(runContext);

        assertThat(run.getExitCode(), is(0));

        TestsUtils.awaitLog(logs, log -> log.getMessage() != null && log.getMessage().contains("public.customers.email"));
        receive.blockLast();
        assertThat(logs.stream().anyMatch(l -> l.getMessage() != null && l.getMessage().contains("public.customers.email")), is(true));
    }

    @Test
    void customCommandsOverrideDiff() throws Exception {
        assertCustomCommandsRun(null);
    }

    @Test
    void customCommandsOverrideDiffChangelog() throws Exception {
        assertCustomCommandsRun(Property.ofValue("diff.xml"));
    }

    private void assertCustomCommandsRun(Property<String> changelogFile) throws Exception {
        Diff task = Diff.builder()
            .id("custom_commands")
            .type(Diff.class.getName())
            .taskRunner(Process.instance())
            .url(Property.ofValue("jdbc:postgresql://unused:5432/target"))
            .referenceUrl(Property.ofValue("jdbc:postgresql://unused:5432/reference"))
            .changelogFile(changelogFile)
            .commands(
                Property.ofValue(
                    List.of(
                        "printf 'first-command\\n' > custom.txt",
                        "printf 'second-command\\n' >> custom.txt"
                    )
                )
            )
            .outputFiles(Property.ofValue(List.of("custom.txt")))
            .build();

        RunContext runContext = TestsUtils.mockRunContext(runContextFactory, task, ImmutableMap.of());
        ScriptOutput output = task.run(runContext);

        assertThat(output.getExitCode(), is(0));
        try (var file = runContext.storage().getFile(output.getOutputFiles().get("custom.txt"))) {
            assertThat(new String(file.readAllBytes(), StandardCharsets.UTF_8), is("first-command\nsecond-command\n"));
        }
    }
}

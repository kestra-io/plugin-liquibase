package io.kestra.plugin.liquibase;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.*;
import io.kestra.core.models.tasks.runners.TargetOS;
import io.kestra.core.models.tasks.runners.TaskRunner;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.scripts.exec.AbstractExecScript;
import io.kestra.plugin.scripts.exec.scripts.models.DockerOptions;
import io.kestra.plugin.scripts.exec.scripts.models.ScriptOutput;
import io.kestra.plugin.scripts.runner.docker.Docker;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Execute custom Liquibase CLI commands",
    description = "Runs the provided Liquibase CLI lines verbatim using the configured task runner. Defaults to ghcr.io/kestra-io/liquibase:latest and forces root user when not specified; include full connection flags and output paths in `commands`."
)
@Plugin(
    examples = {
        @Example(
            title = "Run a Liquibase diff between two Postgres databases",
            full = true,
            code = """
                id: liquibase_diff
                namespace: company.team

                tasks:
                  - id: diff
                    type: io.kestra.plugin.liquibase.CLI

                    taskRunner:
                      type: io.kestra.plugin.scripts.runner.docker.Docker
                      networkMode: dbnet
                    commands:
                      - >
                        liquibase diff
                        --url="jdbc:postgresql://pg1:5432/demo1"
                        --username=user1
                        --password=pass1
                        --reference-url="jdbc:postgresql://pg2:5432/demo2"
                        --reference-username=user2
                        --reference-password=pass2
                """
        ),
        @Example(
            title = "Take a snapshot of a Postgres database schema",
            full = true,
            code = """
                id: liquibase_snapshot
                namespace: company.team

                tasks:
                  - id: snapshot
                    type: io.kestra.plugin.liquibase.CLI
                    containerImage: liquibase/liquibase:latest
                    taskRunner:
                      type: io.kestra.plugin.scripts.runner.docker.Docker
                      networkMode: dbnet
                      user: root
                    outputFiles:
                      - snapshot.xml
                    commands:
                      - >
                        liquibase snapshot
                        --url=jdbc:postgresql://pg1:5432/demo1
                        --username=user1
                        --password=pass1
                        --output-file=snapshot.xml
                """
        ),
        @Example(
            title = "Apply a custom changelog to update schema",
            full = true,
            code = """
                id: liquibase_update
                namespace: company.team

                tasks:
                  - id: update
                    type: io.kestra.plugin.liquibase.CLI
                    taskRunner:
                      type: io.kestra.plugin.scripts.runner.docker.Docker
                      networkMode: dbnet
                      user: root
                    inputFiles:
                      changelog.yaml: |
                        databaseChangeLog:
                          - changeSet:
                              id: 1
                              author: malay
                              changes:
                                - addColumn:
                                    tableName: customers
                                    columns:
                                      - column:
                                          name: email
                                          type: varchar(100)
                    commands:
                      - >
                        liquibase update
                        --url="jdbc:postgresql://pg1:5432/demo1"
                        --username=user1
                        --password=pass1
                        --changeLogFile=changelog.yaml
                """
        )
    }
)
public class CLI extends AbstractExecScript implements RunnableTask<ScriptOutput>, NamespaceFilesInterface, InputFilesInterface, OutputFilesInterface {
    private static final String DEFAULT_IMAGE = "ghcr.io/kestra-io/liquibase:latest";

    @Schema(title = "CLI command list", description = "Commands passed directly to Liquibase; provide complete flags such as `--url`, credentials, and output targets")
    @NotNull
    private Property<List<String>> commands;

    @Schema(title = "Task runner", description = "Execution backend; defaults to Docker runner and sets user to root when unspecified for file write permissions")
    @PluginProperty
    @Builder.Default
    @Valid
    protected TaskRunner<?> taskRunner = Docker.instance();

    @Builder.Default
    protected Property<String> containerImage = Property.ofValue(DEFAULT_IMAGE);

    @Override
    public ScriptOutput run(RunContext runContext) throws Exception {
        TargetOS os = runContext.render(this.targetOS).as(TargetOS.class).orElse(null);

        return this.commands(runContext)
            .withTaskRunner(this.taskRunner)
            .withContainerImage(runContext.render(this.containerImage).as(String.class).orElse(DEFAULT_IMAGE))
            .withInterpreter(this.interpreter)
            .withTargetOS(os)
            .withCommands(this.commands)
            .run();
    }

    @Override
    protected DockerOptions injectDefaults(RunContext runContext, DockerOptions original) throws IllegalVariableEvaluationException {
        var builder = original.toBuilder();
        if (original.getImage() == null) {
            builder.image(runContext.render(this.getContainerImage()).as(String.class).orElse(DEFAULT_IMAGE));
        }

        if (original.getUser() == null) {
            builder.user("root");
        }

        return builder.build();
    }
}

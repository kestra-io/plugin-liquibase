package io.kestra.plugin.liquibase;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
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
import java.util.stream.Stream;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Run Liquibase diff to compare DB schemas."
)
@Plugin(
    examples = {
        @Example(
            title = "Compare two Postgres databases and generate diff file.",
            full = true,
            code = """
                id: liquibase_diff
                namespace: company.team

                tasks:
                  - id: db_diff
                    type: io.kestra.plugin.liquibase.Diff
                    url: jdbc:postgresql://pg1:5432/demo1
                    username: user1
                    password: pass1
                    referenceUrl: jdbc:postgresql://pg2:5432/demo2
                    referenceUsername: user2
                    referencePassword: pass2
                    changelogFile: diff.xml
                    taskRunner:
                      type: io.kestra.plugin.scripts.runner.docker.Docker
                      networkMode: dbnet
                      user: root
                    outputFiles:
                      - diff.xml
                """
        )
    }
)
public class Diff extends AbstractExecScript implements RunnableTask<ScriptOutput> {
    private static final String DEFAULT_IMAGE = "ghcr.io/kestra-io/liquibase";

    @Builder.Default
    protected Property<String> containerImage = Property.ofValue(DEFAULT_IMAGE);

    @Schema(title = "Target DB JDBC URL")
    @NotNull
    private Property<String> url;

    @Schema(title = "Target DB username")
    private Property<String> username;

    @Schema(title = "Target DB password")
    private Property<String> password;

    @Schema(title = "Reference DB JDBC URL")
    @NotNull
    private Property<String> referenceUrl;

    @Schema(title = "Reference DB username")
    private Property<String> referenceUsername;

    @Schema(title = "Reference DB password")
    private Property<String> referencePassword;

    @Schema(title = "Output changelog file (XML/SQL/JSON)")
    private Property<String> changelogFile;

    @Schema(
        title = "Optional raw commands to run. If provided, overrides automatic diff command construction."
    )
    @PluginProperty
    private Property<List<String>> commands;

    @PluginProperty
    @Builder.Default
    @Valid
    protected TaskRunner<?> taskRunner = Docker.instance();

    @Override
    public ScriptOutput run(RunContext runContext) throws Exception {
        TargetOS os = runContext.render(this.targetOS).as(TargetOS.class).orElse(null);
        boolean hasChangelog = runContext.render(changelogFile).as(String.class).isPresent();

        String command = Stream.concat(
            Stream.of("liquibase", hasChangelog ? "diff-changelog" : "diff"),
            Stream.of(
                "--url=" + runContext.render(url).as(String.class).orElseThrow(),
                "--username=" + runContext.render(username).as(String.class).orElse(""),
                "--password=" + runContext.render(password).as(String.class).orElse(""),
                "--reference-url=" + runContext.render(referenceUrl).as(String.class).orElseThrow(),
                "--reference-username=" + runContext.render(referenceUsername).as(String.class).orElse(""),
                "--reference-password=" + runContext.render(referencePassword).as(String.class).orElse("")
            )
        ).reduce((a, b) -> a + " " + b).orElseThrow();

        if (hasChangelog) {
            String file = runContext.render(changelogFile).as(String.class).orElseThrow();
            command += " --changelog-file=" + file;
        }

        return this.commands(runContext)
            .withTaskRunner(this.taskRunner)
            .withContainerImage(runContext.render(this.containerImage).as(String.class).orElse(DEFAULT_IMAGE))
            .withInterpreter(this.interpreter)
            .withCommands(Property.ofValue(List.of(command)))
            .withTargetOS(os)
            .run();
    }

    @Override
    protected DockerOptions injectDefaults(RunContext runContext, DockerOptions original) throws IllegalVariableEvaluationException {
        var builder = original.toBuilder();
        if (original.getImage() == null) {
            builder.image(runContext.render(this.getContainerImage()).as(String.class).orElse(DEFAULT_IMAGE));
        }
        return builder.build();
    }
}

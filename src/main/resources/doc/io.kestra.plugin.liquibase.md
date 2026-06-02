# How to use the Liquibase plugin

Run Liquibase database migrations and schema diffs from Kestra flows inside a container.

## Common properties

`containerImage` defaults to `ghcr.io/kestra-io/liquibase:latest` for `CLI` and `ghcr.io/kestra-io/liquibase` for `Diff`. `taskRunner` controls where the container runs — defaults to Docker.

## Tasks

`CLI` runs arbitrary Liquibase CLI `commands` (e.g. `liquibase update`, `liquibase rollback`). Pass changelog files and properties via `inputFiles` or pull them from [namespace files](https://kestra.io/docs/concepts/namespace-files). Use `beforeCommands` for setup steps.

`Diff` compares two database schemas — set `url` (target JDBC URL) and `referenceUrl` (reference JDBC URL), both required. Optionally set `username`/`password` and `referenceUsername`/`referencePassword` for each connection. Set `changelogFile` to write the diff output to a file (XML, SQL, or JSON format). Store secrets in [secrets](https://kestra.io/docs/concepts/secret) and apply connection properties globally with [plugin defaults](https://kestra.io/docs/workflow-components/plugin-defaults).

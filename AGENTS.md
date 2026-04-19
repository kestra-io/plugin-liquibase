# Kestra Liquibase Plugin

## What

- Provides plugin components under `io.kestra.plugin.liquibase`.
- Includes classes such as `CLI`, `Diff`.

## Why

- What user problem does this solve? Teams need to run Liquibase change management commands from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Liquibase steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Liquibase.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `liquibase`

Infrastructure dependencies (Docker Compose services):

- `db1`
- `db2`
- `dbnet`

### Key Plugin Classes

- `io.kestra.plugin.liquibase.CLI`
- `io.kestra.plugin.liquibase.Diff`

### Project Structure

```
plugin-liquibase/
├── src/main/java/io/kestra/plugin/liquibase/
├── src/test/java/io/kestra/plugin/liquibase/
├── build.gradle
└── README.md
```

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines

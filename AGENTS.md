# Kestra Liquibase Plugin

## What

- Provides plugin components under `io.kestra.plugin.liquibase`.
- Includes classes such as `CLI`, `Diff`.

## Why

- This plugin integrates Kestra with Liquibase.
- It provides tasks that run Liquibase change management commands.

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

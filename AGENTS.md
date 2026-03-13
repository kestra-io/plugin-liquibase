# Kestra Liquibase Plugin

## What

description = 'Plugin Liquibase for Kestra Exposes 2 plugin components (tasks, triggers, and/or conditions).

## Why

Enables Kestra workflows to interact with Liquibase, allowing orchestration of Liquibase-based operations as part of data pipelines and automation workflows.

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

### Important Commands

```bash
# Build the plugin
./gradlew shadowJar

# Run tests
./gradlew test

# Build without tests
./gradlew shadowJar -x test
```

### Configuration

All tasks and triggers accept standard Kestra plugin properties. Credentials should use
`{{ secret('SECRET_NAME') }}` — never hardcode real values.

## Agents

**IMPORTANT:** This is a Kestra plugin repository (prefixed by `plugin-`, `storage-`, or `secret-`). You **MUST** delegate all coding tasks to the `kestra-plugin-developer` agent. Do NOT implement code changes directly — always use this agent.

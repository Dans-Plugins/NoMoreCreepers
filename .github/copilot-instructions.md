# Copilot Instructions

This repository follows the DPC (Dans Plugins Community) conventions defined at
https://github.com/Dans-Plugins/dpc-conventions. Read those conventions before
making any changes.

## Technology Stack

- Language: Java
- Build tool: Maven
- Target platform: Spigot / Paper (Minecraft 1.13+)
- Dependency: Ponder (Preponderous framework)

## Project Structure

- `src/main/java/dansplugins/nomorecreepers/` – Plugin source code
  - `commands/` – Command executors (`HelpCommand`, `ConfigCommand`, `DefaultCommand`)
  - `listeners/` – Event listeners (`SpawnListener`)
  - `services/` – Service classes (`ConfigService`)
  - `bstats/` – bStats metrics integration
- `src/main/resources/` – `plugin.yml`
- `src/test/java/dansplugins/nomorecreepers/` – JUnit 5 tests, mirroring the main package layout

## Coding Conventions

- Follow the existing package structure when adding new classes.
- Use the `ConfigService` for all configuration access.
- Annotate every command executor and event listener with `@Override` where applicable.

## Contribution Workflow

- Branch from `main` for all changes.
- Open a pull request against `main`.
- Reference the related GitHub issue in every pull request description.

# Contributing

## Thank You

Thank you for your interest in contributing to NoMoreCreepers! This guide will help you get started.

## Links

- [Website](https://dansplugins.com)
- [Discord](https://discord.gg/xXtuAQ2)

## Requirements

- A GitHub account
- Git installed on your local machine
- A Java IDE or text editor
- A basic understanding of Java

## Getting Started

1. [Sign up for GitHub](https://github.com/signup) if you don't have an account.
2. Fork the repository by clicking **Fork** at the top right of the repo page.
3. Clone your fork: `git clone https://github.com/<your-username>/NoMoreCreepers.git`
4. Open the project in your IDE.
5. Build the plugin: `mvn clean package`
   If you encounter errors, please open an issue.

## Identifying What to Work On

### Issues

Work items are tracked as [GitHub issues](https://github.com/Dans-Plugins/NoMoreCreepers/issues).

### Milestones

Issues are grouped into [milestones](https://github.com/Dans-Plugins/NoMoreCreepers/milestones) representing upcoming releases.

## Making Changes

1. Make sure an issue exists for the work. If not, create one.
2. Switch to `main`: `git checkout main`
3. Create a branch: `git checkout -b <branch-name>`
4. Make your changes.
5. Test your changes: `mvn test`, plus the manual server validation below for anything the tests cannot reach.
6. Commit: `git commit -m "Description of changes"`
7. Push: `git push origin <branch-name>`
8. Open a pull request against `main`, link the related issue with `#<number>`.
9. Address review feedback.

### User-Facing Strings

User-facing strings are hard-coded in the command and service classes under `src/main/java/dansplugins/nomorecreepers/` — for example, the command list in `HelpCommand` and the config messages in `ConfigService`. `src/main/resources/plugin.yml` declares only plugin metadata, commands, and permission nodes.

When a command's syntax or a permission node changes, `COMMANDS.md` and `USER_GUIDE.md` must be updated to match. When a config option is added, changed or removed, `CONFIG.md` must be updated to match.

## Testing

Run the unit tests with `mvn test`. They live under `src/test/java`, mirroring the package layout of `src/main/java`, and are written with JUnit 5 and Mockito. Collaborators from the Bukkit API are mocked; no test may start a server, touch the network, or read or write a real config file.

A change to behaviour that can be exercised without a server should come with a test. Because the plugin's failure modes are silent — an inverted spawn check either lets creepers through or blocks spawns that should be permitted, with nothing in the log either way — the spawn listener in particular should keep its coverage.

Behaviour that needs the real Bukkit runtime is still verified by hand:

1. Build: `mvn clean package`
2. Place the JAR from `target/` into a local Spigot or Paper server's `plugins` folder.
3. Start the server, confirm the plugin loads, and confirm the changed behaviour works as intended.

The [Build](.github/workflows/build.yml) workflow runs `mvn clean package` on every pull request, which compiles the project and runs the unit tests. A green run does not cover anything on the manual list above.

## Questions

Ask in the [Discord server](https://discord.gg/xXtuAQ2).

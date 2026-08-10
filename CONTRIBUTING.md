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
5. Test your changes.
6. Commit: `git commit -m "Description of changes"`
7. Push: `git push origin <branch-name>`
8. Open a pull request against `main`, link the related issue with `#<number>`.
9. Address review feedback.

### User-Facing Strings

User-facing strings are hard-coded in the command and service classes under `src/main/java/dansplugins/nomorecreepers/` — for example, the command list in `HelpCommand` and the config messages in `ConfigService`. `src/main/resources/plugin.yml` declares only plugin metadata, commands, and permission nodes.

When a command's syntax or a permission node changes, `COMMANDS.md`, `USER_GUIDE.md`, and `CONFIG.md` must be updated to match.

## Testing

There is no automated test suite. Changes are verified by building the plugin and exercising them on a server:

1. Build: `mvn clean package`
2. Place the JAR from `target/` into a local Spigot or Paper server's `plugins` folder.
3. Start the server, confirm the plugin loads, and confirm the changed behaviour works as intended.

The [Build](.github/workflows/build.yml) workflow runs `mvn clean package` on every pull request. A green run means the project compiles; it does not mean the change was tested.

## Questions

Ask in the [Discord server](https://discord.gg/xXtuAQ2).

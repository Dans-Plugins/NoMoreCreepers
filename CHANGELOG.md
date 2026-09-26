# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- `COMMANDS.md` and `USER_GUIDE.md` no longer say that `/nmc config show` displays all configuration values. It lists `version`, `debugMode` and `allowSpawning` only; the `usage-reporting` options are not shown. `COMMANDS.md` now also states what `/nmc config set` accepts: an option name that already exists in `config.yml`, matched case-sensitively; `version` is refused; `debugMode` and `allowSpawning` are parsed as booleans, where any value other than `true` means `false`.
- `/nmc config set` now refuses the `usage-reporting` options and the `usage-reporting` section. They previously fell through to the string branch: `/nmc config set usage-reporting.enabled false` replied `String set.` but stored the string `"false"`, which `getBoolean` ignores in favour of the bundled default of `true`, so reporting stayed on. The same path let `usage-reporting.endpoint` and `usage-reporting.key` be overwritten in-game, and setting `usage-reporting` itself replaced the whole block with a string. These options are edited in `config.yml`, as `CONFIG.md` already stated.
- `CONFIG.md` no longer calls `version` a configuration schema version. It holds the `v`-prefixed plugin version that last wrote the file, and a mismatch is what triggers the missing-defaults pass. `CONFIG.md` and `USER_GUIDE.md` now also say when a change takes effect: `/nmc config set` applies immediately, while an edit to `config.yml` is only read on the next start and is overwritten by the next `/nmc config set`, since the plugin has no reload command. Both now note that setting `allowSpawning` back to `false` does not remove creepers already in the world. `COMMANDS.md` gains the missing entry for a bare `/nmc config`, which lists the sub-commands.

## [1.2.0] – 2026-09-19

### Added

- The plugin now reports usage events — `startup` on enable, `command` on each of its commands — to the author's trace server so it is known which plugins are in use. Events carry the plugin name, the event name, and the plugin version or command name; nothing about players or the server. Reporting runs off the main thread, never delays a tick, drops silently when the server is unreachable, and is turned off with `usage-reporting.enabled: false` in `config.yml`. The bundled config carries the plugin's key, so reporting is active out of the box unless turned off — including on servers upgraded from a version before the `usage-reporting` block existed, whose `config.yml` is never rewritten: the plugin reads the bundled defaults for any key the file lacks. The trace client is vendored under `dansplugins.nomorecreepers.trace`, the same way bStats is, with its own tests.
- A unit test suite under `src/test/java`, built on JUnit 5 and Mockito at test scope, run by `mvn test` and therefore by the `Build` workflow. It characterizes the current behaviour of the spawn listener, `ConfigService` and the three commands: that creeper spawns are cancelled only while `allowSpawning` is `false`, that non-creepers are never touched, how `/nmc config` dispatches its sub-commands, and what `/nmc` and `/nmc help` print. Nothing that needs a running server is covered, so manual validation is still required for plugin startup, listener registration and config file reading and writing.
- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get nomorecreepers --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Fixed

- The shaded plugin JAR no longer ships JUnit 4 and Hamcrest. `ponder` is published as an uber jar that bundles both frameworks inside its own artifact, and separately declares `junit:junit` at compile scope, so two copies of the same classes reached the shade plugin — 430 of the artifact's 594 entries were a test framework the plugin never calls. A shade filter now strips them from the ponder artifact, an exclusion drops the duplicate transitive dependency, and the bundled JUnit and Hamcrest license files go with the code they covered. `preponderous/ponder/tests/TestArgumentParser`, ponder's own test class and the only remaining class that referenced JUnit, is filtered out with them. The JAR falls from 594 entries to 159, keeping every `preponderous` and `dansplugins` class it had before.
- `DefaultCommand` no longer declares an `nmc.default` permission node. The node was undeclared in `plugin.yml`, absent from the `USER_GUIDE.md` permissions table and never checked — a bare `/nmc` is dispatched straight to `DefaultCommand.execute` without going through `CommandService`, so the declaration only described a restriction that did not exist. `/nmc` stays unrestricted, as `COMMANDS.md` already documents, and there is no longer a stray node waiting to silently gate the command should it ever be routed through the permission check.
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.
- Development documentation no longer describes tooling this repository does not have. `README.md` and `CONTRIBUTING.md` told contributors to verify changes with `mvn clean test` and to read `BUILD SUCCESS` as "the tests have passed", although no test sources exist; `README.md` also offered a Docker-based test server with plugin hot-reloading, where the only container in the repository is a VS Code dev container with no Minecraft server in it. Both files now describe the unit test suite added in this release, what the `Build` workflow actually checks, and the manual server validation steps that the tests cannot replace. `CONTRIBUTING.md` additionally pointed contributors at `plugin.yml` for user-facing strings, which live in the command and service classes.
- `CONFIG.md` no longer describes `debugMode` as enabling debug mode. The option is stored, settable and displayed, but no code path reads it, so setting it has no effect.

## [2.0.0-SNAPSHOT-8-8-2026] – 2026-08-08

### Changed
- NoMoreCreepers is now developed AI-first. Day-to-day feature work, grooming, review and maintenance run through AI agents working directly against this repository, with the maintainers setting direction and approving what lands. The major version bump marks that change in how the project is built — it is not a break in behaviour, configuration or stored data, and existing installations can upgrade in place. Released as `2.0.0-SNAPSHOT-8-8-2026`: the AI-first line has not yet been verified in live operation, and the dated snapshot designation stays until it has.

### Fixed

- `/nmc config set` usage message now shows the correct `/nmc` command prefix instead of the stale `/c` prefix
- `/nmc` plugin info now links to the current `Dans-Plugins/NoMoreCreepers` wiki instead of the old org's wiki

### Removed

- Dead `'A'` and `'C'` option branches in `ConfigService.setConfigOption` left over from a template; the plugin's only settable options are `debugMode` and `allowSpawning`, both handled by the boolean branch

## [1.1.0]

### Added

- Configuration command to view and modify settings in-game
- bStats metrics integration

## [1.0.0]

### Added

- Initial release
- Creeper spawn prevention via `EntitySpawnEvent` listener
- `allowSpawning` configuration option to toggle creeper spawns

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- The shaded plugin JAR no longer ships JUnit 4 and Hamcrest. `ponder` is published as an uber jar that bundles both frameworks inside its own artifact, and separately declares `junit:junit` at compile scope, so two copies of the same classes reached the shade plugin — 430 of the artifact's 594 entries were a test framework the plugin never calls. A shade filter now strips them from the ponder artifact, an exclusion drops the duplicate transitive dependency, and the bundled JUnit and Hamcrest license files go with the code they covered. `preponderous/ponder/tests/TestArgumentParser`, ponder's own test class and the only remaining class that referenced JUnit, is filtered out with them. The JAR falls from 594 entries to 159, keeping every `preponderous` and `dansplugins` class it had before.
- `DefaultCommand` no longer declares an `nmc.default` permission node. The node was undeclared in `plugin.yml`, absent from the `USER_GUIDE.md` permissions table and never checked — a bare `/nmc` is dispatched straight to `DefaultCommand.execute` without going through `CommandService`, so the declaration only described a restriction that did not exist. `/nmc` stays unrestricted, as `COMMANDS.md` already documents, and there is no longer a stray node waiting to silently gate the command should it ever be routed through the permission check.
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.

### Added

- A unit test suite under `src/test/java`, built on JUnit 5 and Mockito at test scope, run by `mvn test` and therefore by the `Build` workflow. It characterizes the current behaviour of the spawn listener, `ConfigService` and the three commands: that creeper spawns are cancelled only while `allowSpawning` is `false`, that non-creepers are never touched, how `/nmc config` dispatches its sub-commands, and what `/nmc` and `/nmc help` print. Nothing that needs a running server is covered, so manual validation is still required for plugin startup, listener registration and config file reading and writing.
- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get nomorecreepers --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Fixed

- Development documentation no longer describes tooling this repository does not have. `README.md` and `CONTRIBUTING.md` told contributors to verify changes with `mvn clean test` and to read `BUILD SUCCESS` as "the tests have passed", although no test sources exist; `README.md` also offered a Docker-based test server with plugin hot-reloading, where the only container in the repository is a VS Code dev container with no Minecraft server in it. Both files now state that there is no automated test suite, describe what the `Build` workflow actually checks, and give the manual server validation steps. `CONTRIBUTING.md` additionally pointed contributors at `plugin.yml` for user-facing strings, which live in the command and service classes.
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

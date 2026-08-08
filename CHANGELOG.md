# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

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

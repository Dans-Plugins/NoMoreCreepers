# Commands Reference

All commands are available under `/nomorecreepers` or the shorthand `/nmc`.

## General Commands

### /nmc

**Description:** Displays plugin version, developer, and wiki link.
**Permission:** None
**Usage:** `/nmc`

### /nmc help

**Description:** Lists all available commands and their descriptions.
**Permission:** `nmc.help`
**Usage:** `/nmc help`

## Admin Commands

### /nmc config show

**Description:** Displays the `version`, `debugMode` and `allowSpawning` values. The `usage-reporting` options are not listed; see them in `config.yml`.
**Permission:** `nmc.config`
**Usage:** `/nmc config show`

### /nmc config set \<option\> \<value\>

**Description:** Updates a configuration option to the specified value. The option name is case-sensitive and must already exist in `config.yml`. `version` cannot be set. `debugMode` and `allowSpawning` are booleans: `true` (in any case) sets `true`, and any other value sets `false`. The `usage-reporting` options are edited in `config.yml` rather than with this command (see [CONFIG.md](CONFIG.md)).
**Permission:** `nmc.config`
**Usage:** `/nmc config set <option> <value>`
**Example:** `/nmc config set allowSpawning true`

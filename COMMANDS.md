# Commands Reference

All commands are available under `/nomorecreepers` or the shorthand `/nmc`.

## General Commands

### /nmc

**Description:** Displays plugin version, developer, and wiki link.
**Permission:** None
**Usage:** `/nmc`

### /nmc help

**Description:** Lists the top-level commands — `/nmc`, `/nmc help` and `/nmc config` — with a one-line description of each. The `config` sub-commands are not listed; `/nmc config` lists them. All three entries are shown whatever the sender's permissions.
**Permission:** `nmc.help`
**Usage:** `/nmc help`

## Admin Commands

### /nmc config

**Description:** Lists the `show` and `set` sub-commands. The same list is shown when an unknown sub-command is given.
**Permission:** `nmc.config`
**Usage:** `/nmc config`

### /nmc config show

**Description:** Displays the `version`, `debugMode` and `allowSpawning` values. The `usage-reporting` options are not listed; see them in `config.yml`.
**Permission:** `nmc.config`
**Usage:** `/nmc config show`

### /nmc config set \<option\> \<value\>

**Description:** Updates a configuration option to the specified value. The option name is case-sensitive and must already exist in `config.yml`. `version` cannot be set. `debugMode` and `allowSpawning` are booleans: `true` (in any case) sets `true`, and any other value sets `false`. The `usage-reporting` options, and the `usage-reporting` section itself, are refused; they are edited in `config.yml` (see [CONFIG.md](CONFIG.md)). Given fewer than two arguments, the command replies with `Usage: /nmc config set (option) (value)` and changes nothing.

**Replies:**

| Reply | When | Saved? |
|-------|------|--------|
| `Boolean set.` | `debugMode` or `allowSpawning` was given. This reply is also shown when the value is not `true` — `yes` or `1`, for example — and `false` was stored. | Yes |
| `String set.` | Any other option that exists in `config.yml` was given; the value is stored as text. | Yes |
| `That config option wasn't found.` | The option is not in `config.yml`, including a known option typed in a different case, such as `allowspawning`. | No |
| `Cannot set version.` | The option is `version`. | No |
| `The usage-reporting options are edited in config.yml, not with this command.` | The option is `usage-reporting` or one of its options. | No |

**Permission:** `nmc.config`
**Usage:** `/nmc config set <option> <value>`
**Example:** `/nmc config set allowSpawning true`

# Configuration Guide

The configuration file is located at `plugins/NoMoreCreepers/config.yml`. Options can also be changed in-game using the `/nmc config set` command.

## version

**Type:** string
**Default:** Current plugin version
**Description:** Tracks the configuration schema version. Used internally to detect version mismatches and apply missing defaults. This value cannot be changed with the config command.

## debugMode

**Type:** boolean
**Default:** `false`
**Description:** Enables or disables debug mode for the plugin.

**Example:**

```yaml
debugMode: false
```

## allowSpawning

**Type:** boolean
**Default:** `false`
**Description:** Controls whether creepers are allowed to spawn. When set to `false` (the default), all creeper spawn events are cancelled. Set to `true` to allow creepers to spawn normally.

**Example:**

```yaml
allowSpawning: false
```

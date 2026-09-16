# Configuration Guide

The configuration file is located at `plugins/NoMoreCreepers/config.yml`. Options can also be changed in-game using the `/nmc config set` command.

## version

**Type:** string
**Default:** Current plugin version
**Description:** Tracks the configuration schema version. Used internally to detect version mismatches and apply missing defaults. This value cannot be changed with the config command.

## debugMode

**Type:** boolean
**Default:** `false`
**Description:** Reserved for debug output. The option is stored in the config file, can be changed with `/nmc config set`, and is listed by `/nmc config show`, but no plugin behaviour reads it — setting it to `true` currently has no effect.

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

## usage-reporting.enabled

**Type:** boolean
**Default:** `true`
**Description:** Whether the plugin reports usage events (see [Usage reporting](#usage-reporting) below). Set to `false` to turn it off. This option is edited in the file and takes effect on the next server start; it is not settable with `/nmc config set`.

**Example:**

```yaml
usage-reporting:
  enabled: false
```

## usage-reporting.endpoint

**Type:** string
**Default:** `https://trace.danielstephenson.dev`
**Description:** The trace server events are sent to.

## usage-reporting.key

**Type:** string
**Default:** the plugin's key
**Description:** Identifies this plugin to the trace server so reports are attributed to it. Not a secret: it ships in the default config and can only report as NoMoreCreepers. Empty means reporting is off regardless of `usage-reporting.enabled`.

## Usage reporting

When the plugin is enabled, and each time one of its commands is used, a small event is sent to the
author's [trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which
plugins are actually in use. An event carries the plugin's name, the event name (`startup` or
`command`), and either the plugin version or the command name — nothing about players, the world, or
the server. Sending happens off the main thread, never delays a tick, and is dropped silently if the
server cannot be reached. Set `usage-reporting.enabled` to `false` to turn it off.

A server upgraded from a version before usage reporting has no `usage-reporting` block in its
`config.yml`. The plugin reads the bundled defaults for any option the file lacks, so reporting is
active there too, and on the first start it writes the three options above into the file so the
switch is visible. The console says on every start whether reporting is on and how to turn it off.

Reporting can also be turned off for every plugin on the server that reports to trace, with
`enabled: false` in `plugins/trace/config.yml` (created on the first start), or for the whole server
process with the environment variable `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`. Details:
https://github.com/Stephenson-Software/trace#usage-reporting


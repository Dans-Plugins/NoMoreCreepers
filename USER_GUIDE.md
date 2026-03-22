# User Guide

## Prerequisites

- A Spigot or Paper Minecraft server (1.13 or newer)
- Server operator access

## First Steps

1. Install the plugin by placing the JAR file into your server's `plugins` folder.
2. Restart the server.
3. By default, creeper spawning is **blocked**. No further action is required if this is your desired behaviour.

## Common Scenarios

### Allowing Creeper Spawning

If you want to temporarily allow creepers to spawn:

1. Run `/nmc config set allowSpawning true` in-game or from the console.
2. Creepers will now spawn normally.
3. To block them again, run `/nmc config set allowSpawning false`.

### Viewing Current Configuration

Run `/nmc config show` to display all current configuration values.

### Viewing Plugin Information

Run `/nmc` with no arguments to see the plugin version, developer, and wiki link.

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `nmc.help` | `true` | Allows the player to use the `/nmc help` command |
| `nmc.config` | `op` | Allows the player to view and modify plugin configuration |

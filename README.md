# NoMoreCreepers

## Description

NoMoreCreepers is a Minecraft plugin that prevents creepers from spawning on your server. It provides a simple toggle to allow or block creeper spawns, making it easy for server administrators to protect builds and players from creeper explosions.

## Installation

### First Time Installation

1. Download the plugin from the [releases page](https://github.com/Dans-Plugins/NoMoreCreepers/releases).
2. Place the jar in the `plugins` folder of your server.
3. Restart your server.

## Usage

### Documentation

- [User Guide](USER_GUIDE.md) – Getting started and common scenarios
- [Commands Reference](COMMANDS.md) – Complete list of all commands
- [Configuration Guide](CONFIG.md) – Detailed configuration options

## Support

You can find the support Discord server [here](https://discord.gg/xXtuAQ2).

### Experiencing a bug?

Please fill out a bug report [here](https://github.com/Dans-Plugins/NoMoreCreepers/issues/new).

- [Known Bugs](https://github.com/Dans-Plugins/NoMoreCreepers/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

## Contributing

- [CONTRIBUTING.md](CONTRIBUTING.md)

## Testing

### Unit Tests

Linux:

    mvn clean test

Windows:

    mvn clean test

If you see `BUILD SUCCESS`, the tests have passed.

## Development

### Test Server with Plugin Hot-Reloading

A Docker-based test server can be used for development.

#### Setup

1. Build the plugin: `mvn clean package`
2. Copy the resulting JAR from `target/` into your test server's `plugins` folder.
3. Start the server and verify the plugin loads.

## Authors and Acknowledgement

### Developers

| Name | Main Contributions |
|------|--------------------|
| Daniel Stephenson | Creator and primary developer |

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE) (GPL-3.0).

You are free to use, modify, and distribute this software, provided that:

- Source code is made available under the same license when distributed.
- Changes are documented and attributed.
- No additional restrictions are applied.

See the [LICENSE](LICENSE) file for the full text of the GPL-3.0 license.

## Project Status

This project is in active development.

### bStats

You can view the bStats page for the plugin [here](https://bstats.org/plugin/bukkit/NoMoreCreepers/13432).

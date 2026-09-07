# XRayLogz

Advanced ore mining logger for Paper Minecraft servers with Discord integration.

## Features

- **Ore Mining Detection**: Tracks player mining of configurable ore types
- **Vein Tracking**: Detects ore veins and logs them when thresholds are met
- **Staff Notifications**: Sends alerts to staff members with the `xraylogz.staff` permission
- **Discord Integration**: Sends mining alerts to Discord via webhooks
- **Version Checking**: Automatically checks for updates via GitHub Releases
- **Configurable**: Extensive configuration options for ores, messages, and settings
- **In-Game Commands**: Commands for reloading, version checking, and toggling notifications

## Installation

1. Download the latest version from the [Releases](https://github.com/lyxxis/XRayLogz/releases) page
2. Place the JAR file in your server's `plugins` folder
3. Restart the server or load the plugin
4. Configure the `config.yml` file in `plugins/XRayLogz/`

## Configuration

Edit `plugins/XRayLogz/config.yml` to customize the plugin:

```yaml
# Discord webhook settings
discord:
  enabled: true
  webhook-url: "YOUR_WEBHOOK_URL_HERE"
  username: "XRayLogz"
  avatar-url: ""
  embed-color: "#FF5555"

# Ore mining detection settings
ores:
  tracked-ores:
    - "coal_ore"
    - "diamond_ore"
    - "ancient_debris"
    # Add more ores as needed

# Version checking settings
version-check:
  enabled: true
  github-repo: "lyxxis/XRayLogz"
  check-interval-minutes: 30

# Mining tracking settings
tracking:
  min-vein-size: 1
  log-to-staff: true
  log-to-discord: true
```

## Commands

- `/xraylogz` - Show available commands
- `/xraylogz reload` - Reload configuration
- `/xraylogz version` - Check for updates
- `/xraylogz toggle` - Toggle notifications for yourself

## Permissions

- `xraylogz.staff` - Receive ore mining notifications (default: op)
- `xraylogz.admin` - Use admin commands (default: op)
- `xraylogz.*` - Wildcard permission for all features (default: op)

## Building from Source

Requires Java 17 and Maven:

```bash
mvn clean package
```

The compiled JAR will be in the `target/` directory.

## Requirements

- Minecraft 1.20.4+
- Paper server
- Java 17+

## License

This project is licensed under the MIT License.

## Author

lyxxis
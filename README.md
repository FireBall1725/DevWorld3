# DevWorld3

DevWorld 3 puts three buttons on the title screen: New World, Load World and Delete World. New World makes a creative flat world with the daylight cycle, weather and mob griefing off, so you can start testing your own mod right away. Delete World stays greyed out until you press left shift twice.

It is meant to sit in your dev environment, not in a modpack.

## Versions

One jar per loader and Minecraft version, named `DevWorld3-{platform}-{mcVersion}-{version}`.

| Minecraft | Fabric | Forge | NeoForge |
|-----------|--------|-------|----------|
| 1.19.2    | yes    | yes   |          |
| 1.20.1    | yes    | yes   |          |
| 1.21.1    | yes    |       | yes      |

Fabric needs Fabric API. Versions look like `26.10.0` (year, month, revision).

## Usage

Add the jar to your dev run only. Releases are on the [releases page](https://github.com/FireBall1725/DevWorld3/releases).

## Configuration

The first launch writes `config/devworld.json`. Every key is optional; anything you leave out keeps its default.

```json
{
  "world_config": {
    "world_name": "DevWorld",
    "world_generation_preset": "minecraft:bedrock,3*minecraft:stone,116*minecraft:sandstone;minecraft:desert",
    "bonus_chest": false,
    "game_mode": "creative",
    "difficulty": "normal",
    "seed": 0,
    "generate_structures": false,
    "allow_commands": true,
    "time_of_day": 6000
  },
  "game_rules": {
    "doDaylightCycle": false,
    "doWeatherCycle": false,
    "doFireTick": false,
    "mobGriefing": false,
    "doMobSpawning": true,
    "disableRaids": true,
    "doInsomnia": false
  }
}
```

`game_rules` takes any vanilla game rule by its in-game name, including rules added by other mods. Unknown names and wrong value types are logged and skipped. `time_of_day` is applied when the world opens, only for the DevWorld and only while `doDaylightCycle` is off.

The old `rule_daylight` style keys and `daylight_time` are still read and get rewritten to the new names.

### Shared defaults

Set the `DEVWORLD_CONFIG` environment variable to the URL of a JSON file in the same format and it is used as the defaults, so a team can share one config. Your local `config/devworld.json` still wins. The title screen shows a green line in the top left when the file loaded, and a red one when it did not.

## Building

Gradle needs JDK 25 (Loom 1.18). The game itself builds against Java 17 or 21 depending on the version.

```
./gradlew buildAndCollect
```

Jars land in `build/libs/{version}/`. The project uses Stonecutter, so a new Minecraft version is one `match(...)` line in `settings.gradle.kts` and a block in `stonecutter.properties.toml`. Run `"Set active project to <node>"` to switch which version your IDE sees.

## License

Source is [AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.en.html). Assets are [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/).

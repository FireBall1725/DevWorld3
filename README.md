# DevWorld3
DevWorld 3 is a Minecraft Forge Mod that is designed to be an easy way to create a developer test world, this test world can be then used to test your development mod in. With one click you get a creative flat world that by default is set to the redstone ready preset, time set to noon and is frozen, and weather disabled. From the title screen you can also quickly load, delete and regenerate the DevWorld.

## Usage
Like all minecraft mods, this mod can be installed by dropping it into the mods folder, however this mod was not designed to be used this way, or included in packs. This mod was designed to be included into your gradle environment so you can use it while debugging the mod that you are developing.

To use this mod with gradle, make the following changes to your `build.gradle` file:

In the `repositories { }` section add the following:
```java
repositories {
    maven {
        name "Blame Jared"
        url "https://maven.blamejared.com"
    }
}
```

In the `dependencies { }` section add the following:
```java
dependencies {
    runtimeOnly fg.deobf("ca.fireball1725.devworld:DevWorld3-1.19.2:X.Y.Z:client")
}
```

**Note:** Replace `X.Y.Z` with the release version of the mod which can be found [here](https://github.com/FireBall1725/DevWorld3/releases)

Example: `v0.0.5` would be `ca.fireball1725.devworld:DevWorld3-1.19.2:0.0.5:client`
## Configuration
For configuration see the [wiki](https://github.com/FireBall1725/DevWorld3/wiki/Configuration)

## License

Mod source code is licensed under the [GNU AFFERO GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/agpl-3.0.en.html). Click the link for more information

Mod assets are licensed under the [Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)](https://creativecommons.org/licenses/by-nc-nd/4.0/). Click the link for more information
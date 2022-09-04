# DevWorld3
This is a forge mod for Minecraft 1.19 that adds a quick developer world to the title screen. 


## Usage
This mod is designed to be used in your build.gradle.

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
    runtimeOnly fg.deobf("ca.fireball1725.devworld:DevWorld3-1.19.2:0.0.5:client")
}
```

## Updating
In the example above the `0.0.5` is the version number, replace this with the version of DevWorld 3 that you wish to use

## License

Mod source code is licensed under the [GNU AFFERO GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/agpl-3.0.en.html). Click the link for more information

Mod assets are licensed under the [Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)](https://creativecommons.org/licenses/by-nc-nd/4.0/). Click the link for more information
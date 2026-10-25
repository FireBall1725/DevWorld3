plugins {
  id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-fabric"

stonecutter parameters {
  val (version, loader) = current.project.split('-', limit = 2)

  // Makes version- and loader-specific properties apply from `stonecutter.properties.toml`
  properties {
    tags(version, loader)
  }

  // Adds constants to Stonecutter comments, i.e. `//? if fabric {`
  constants {
    match(loader, "fabric", "neoforge", "forge")
  }

  swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
}

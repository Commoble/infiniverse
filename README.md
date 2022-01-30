Infiniverse is an API mod for Minecraft Forge that allows other mods to add and remove dimensions during server runtime.

Infiniverse is primarily a server mod and not required on clients (it is also compatible with vanilla clients). However, if the Infiniverse mod is present on your client, any new/removed dimensions will be immediately available in command suggestions for commands that take dimensions as arguments (such as `/execute in`). If Infiniverse is not present on your client, you will need to log out of the server and log in for these changes to be visible in command suggestions.

Built mod jars are available here: <https://www.curseforge.com/minecraft/mc-mods/infiniverse>

## Dependency Setup

To use infiniverse in your mod's development environment with full sources and javadocs, add the following gradle dependency to your gradle buildscript:

```gradle
repositories {
	maven { url "https://cubicinterpolation.net/maven/" }
}

dependencies {
	implementation fg.deobf("commoble.infiniverse:${infiniverse_branch}:${infiniverse_version}")
}
```
Where
* `${infiniverse_branch}` is e.g. infiniverse-1.18.1, indicating which version of minecraft the mod was compiled against. Other valid groups can be observed by browsing the maven above.
* `${infiniverse_version}` is e.g. 1.0.0.0

If you only need Infiniverse in your dev environment during runtime, you can alternatively use cursemaven to depend on a specific file: <https://www.cursemaven.com/>

Be aware that sources and javadocs are not able to be provided via cursemaven.

## Using the API

### InfiniverseAPI

Dependant mods can use `commoble.infiniverse.api.InfiniverseAPI.get()` to get the Infiniverse API, which allows dimensions to be added or removed during server runtime.

This should generally only be used for dimensions that need some sort of user input to determine how they should be created; static dimensions can be created using standard dimension jsons instead of using the Infiniverse API. <https://minecraft.fandom.com/wiki/Custom_dimension>

For an example of a mod that uses the Infiniverse API, see Infiniverse Utils: <https://github.com/Commoble/infiniverse_utils>

### UnregisterDimensionEvent

Fired on the forge bus when a dimension/level is about to be unregistered and removed from the server. Not cancellable.

## Versioning Semantics

Infiniverse's versions use the format A.B.C.D, where
* Increments to A indicate breaking changes to save format. Worlds saved with older versions of A may not be compatible with newer versions.
* Increments to B indicate breaking changes to APIs. Dependant mods compiled against older versions of B or A may not be compatible with newer versions.
* Increments to C indicate breaking changes to netcode. When a server updates to a newer version of C, B, or A, clients may need to update as well.
* Increments to D indicate changes to implementation details. These will generally not break anything, though depdendant mods using the internals rather than the API may need to update.
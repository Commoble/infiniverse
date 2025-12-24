Infiniverse is an API mod for Minecraft Forge that allows other mods to add and remove dimensions during server runtime.

Infiniverse is primarily a server mod and not required on clients (it is also compatible with vanilla clients). However, if the Infiniverse mod is present on your client, any new/removed dimensions will be immediately available in command suggestions for commands that take dimensions as arguments (such as `/execute in`). If Infiniverse is not present on your client, you will need to log out of the server and log in for these changes to be visible in command suggestions.

Built mod jars are available here:
* <https://www.curseforge.com/minecraft/mc-mods/infiniverse>

## Dependency Setup

To use infiniverse in your mod's development environment with full sources and javadocs, add the following gradle dependency to your gradle buildscript:

```gradle
repositories {
	maven { url "https://maven.commoble.net/" }
}

dependencies {
	implementation "net.commoble.infiniverse:infiniverse:${infiniverse_version}"
}
```

See https://maven.commoble.net/net/commoble/infiniverse/infiniverse/ for available artifacts.

Infiniverse versions in 1.21.9+ follow the schema MCMAJOR.MCMINOR.MODVERSION, e.g. 21.9.0 is the first release of infiniverse for MC 1.21.9. API breaks will not occur within a minor MC version.

If you only need Infiniverse in your dev environment during runtime, you can alternatively use cursemaven to depend on a specific file: <https://www.cursemaven.com/>

Be aware that sources and javadocs are not able to be provided via cursemaven.

## Using the API

### InfiniverseAPI

Dependant mods can use `net.commoble.infiniverse.api.InfiniverseAPI.get()` to get the Infiniverse API, which allows dimensions to be added or removed during server runtime.

This should generally only be used for dimensions that need some sort of user input to determine how they should be created; static dimensions can be created using standard dimension jsons instead of using the Infiniverse API. <https://minecraft.wiki/w/Custom_dimension>

For an example of a mod that uses the Infiniverse API, see the src/examplemod sourceset.

### UnregisterDimensionEvent

Fired on the forge bus when a dimension/level is about to be unregistered and removed from the server. Not cancellable.

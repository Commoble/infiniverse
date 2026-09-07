## 25.2.0.0
* Updated to MC 26.2 / neoforge 26.2.0.81

## 26.1.0.1
* Fixed a crash that could occur when dimensions are unloaded.
* Infiniverse no longer automatically saves levels before unloading them. Mods which were relying on this are recommended
to wait for all chunks to unload before marking the level for unregistration.

## 26.1.0.0
* Updated to MC 26.1 (requires neoforge 26.1.0.17-beta or higher)
* Dimensions can again be created with per-dimension seeds
	* This can be done by specifying the seed override in the LevelStem when using InfiniverseAPI#getOrCreateLevel
* Added several commands, including several from the old infiniverse_utils mod:
	* `/infiniverse copy_dimension <old_dimension_name: Dimension> <new_dimension_name: Dimension> [server_seed|copy_seed|random_seed|seed] [seed: String]`
		* Copies a dimension, with a new seed if requested
	* `/infiniverse remove <old_dimension_name: Dimension>`
		* Removes a dimension from the server registry; region files remain on disk so it may be restored at a later time if necessary
	* `infiniverse restore_missing_dimensions <world_preset: WorldPreset>`
		* Adds any missing dimensions from a specified world preset. May be useful if the world_gen_settings.dat file becomes corrupted or preset dimensions are accidentally deleted.
	

## 21.11.1
* Add missing jar manifest attributes (fixes version not displaying in mods list)

## 21.11.0
* Updated to MC 1.21.11 / neoforge 21.11.12-beta

## 21.9.0
* Updated to MC 1.21.9 / neoforge 21.9.11-beta

## 2.1.0.0
* Updated to MC 1.21.3 / neoforge 21.3.11-beta

## 2.0.1.0
* Updated to MC 1.21 / neoforge 21.0.40-beta

## 2.0.0.1
* Fix clients without infiniverse being unable to connect to servers with infiniverse

## 2.0.0.0
* Now uses the neoforge modloader instead of forge
* Moved all packages from commoble to net.commoble package
* Unregistering a dimension now saves the level and its chunks
* Dimension unregistering at end of server tick now happens at LOWEST event priority

## 1.0.0.5
* Updated to minecraft 1.20.1

## 1.0.0.4
* Updated to forge version 41.0.110

## 1.0.0.3
* Updated to 1.19

## 1.0.0.2
* Updated to 1.18.2

## 1.0.0.1
* Added UnregisterDimensionEvent

## 1.0.0.0
* Made exist
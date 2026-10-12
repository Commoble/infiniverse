package net.commoble.infiniverse.api;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.function.Consumers;
import org.jspecify.annotations.Nullable;

import net.commoble.infiniverse.internal.DimensionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

/**
 * API for creating and removing dimensions/levels during game runtime.<br>
 * Static (single/individual) dimensions like the nether should be declared in json
 * instead of using InfiniverseAPI to create them.<br>
 */
public interface InfiniverseAPI
{
	/**
	 * @return The API. For best results, avoid caching this, and only use while the server is running.
	 */
	public static InfiniverseAPI get()
	{
		return DimensionManager.INSTANCE;
	}
	
	/**
	 * Gets a level, dynamically creating and registering one if it doesn't exist.<br>
	 * The dimension registry is stored in the server's level file, all previously registered dimensions are loaded
	 * and recreated and reregistered whenever the server starts.<br>
	 * This can be used for making dynamic dimensions at runtime; static dimensions should be defined in json instead.<br>
	 * @param server a MinecraftServer instance (you can get this from a ServerPlayerEntity or ServerWorld)
	 * @param levelKey A ResourceKey for your level
	 * @param dimensionFactory A function that produces a new LevelStem (dimension) instance if necessary<br>
	 * If this factory is used, it should be assumed that intended dimension has not been created or registered yet,
	 * so making the factory attempt to get this dimension from the server's dimension registry will fail
	 * @return Returns a ServerLevel, creating and registering a world and dimension for it if the world does not already exist
	 * @deprecated As of Neoforge 26.3.0.76, will crash if invoked while levels are being ticked, use {@link InfiniverseAPI#getOrRequestLevel} instead
	 */
	@Deprecated(forRemoval=true)
	public abstract ServerLevel getOrCreateLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, final Supplier<LevelStem> dimensionFactory);
	
	/// Gets a level if it exists, or creates and registers one if it doesn't.
	/// 
	/// The dimension registry is stored in the server's level file;
	/// all previously registered dimension are loaded, recreated, and reregistered whenever the server starts.
	/// 
	/// This can be used for making dynamic dimensions at runtime; static dimensions should be defined in data files instead.
	/// 
	/// @param server MinecraftServer
	/// @param levelKey ResourceKey for the level being requested
	/// @param dimensionFactory Supplier to produce the new LevelStem (dimension) instance if it doesn't exist yet
	/// @return ServerLevel if the requested level exists; if null, the level will be created at the end of the current servertick
	public default @Nullable ServerLevel getOrRequestLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, final Supplier<LevelStem> dimensionFactory) {
		return getOrRequestLevel(server, levelKey, dimensionFactory, Consumers.nop());
	}

	/// Gets a level if it exists, or creates and registers one if it doesn't.
	/// 
	/// The dimension registry is stored in the server's level file;
	/// all previously registered dimension are loaded, recreated, and reregistered whenever the server starts.
	/// 
	/// This can be used for making dynamic dimensions at runtime; static dimensions should be defined in data files instead.
	/// 
	/// @param server MinecraftServer
	/// @param levelKey ResourceKey for the level being requested
	/// @param dimensionFactory Supplier to produce the new LevelStem (dimension) instance if it doesn't exist yet
	/// @param onCreated Consumer which will run on the new level at the end if the tick if and only if the level is created. Only one onCreated callback will run per level. 
	/// @return ServerLevel if the requested level exists; if null, the level will be created at the end of the current servertick
	public abstract @Nullable ServerLevel getOrRequestLevel(
		final MinecraftServer server,
		final ResourceKey<Level> levelKey,
		final Supplier<LevelStem> dimensionFactory,
		final Consumer<ServerLevel> onCreated);
	
	/**
	 * Schedules a non-vanilla level/dimension to be unregistered and removed at the end of the current server tick.<br>
	 * This will have the following effects:<br>
	 * <ul>
	 * <li>Unregistered levels will stop ticking.
	 * <li>Unregistered dimensions will not be loaded on server startup unless and until they are registered again (via {@link DimensionManager#getOrCreateLevel}.
	 * <li>Players still present in the given level will, when the level is removed, be ejected to their spawn points.
	 * <li>Players who have respawn points in levels being unloaded will have their spawn points reset to the overworld and respawned there.
	 * </ul>
	 * Unregistering a level does not delete the region files or other persistant data associated with the level.<br>
	 * If a level is reregistered after unregistering it, the level will retain all prior data (unless manually deleted by a server admin.)<br>
	 * This has no effect on the vanilla dimensions (The Overworld, The Nether, and The End);
	 * this is because vanilla will automatically reconstitute these anyway if we try to remove them,
	 * so we disallow their removal to avoid strangeness.<br>
	 * (datapack dimensions removed here will also be reconstituted at next server startup but we can't filter these so we don't check for them)<br>
	 * 
	 * This also has no effect if the level does not currently exist (if both a delete and create are specified for a nonexistant dimension in the same tick, it will be created and will not be deleted).
	 * @param server The server to remove the dimension from
	 * @param levelToRemove The resource key for the level to be unregistered 
	 */
	public abstract void markDimensionForUnregistration(final MinecraftServer server, final ResourceKey<Level> levelToRemove);
	
	/**
	 * @return An immutable copy of the dimensions that will be unregistered at the end of the current server tick.
	 * (returns an empty set if called while no server is running)
	 */
	public abstract Set<ResourceKey<Level>> getLevelsPendingUnregistration();
}

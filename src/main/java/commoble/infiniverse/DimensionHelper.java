package commoble.infiniverse;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;

import commoble.infiniverse.mixins.MinecraftServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

public class DimensionHelper
{
	@SuppressWarnings("deprecation")
	public static ServerWorld getOrCreateWorld(MinecraftServer server, RegistryKey<World> worldKey, Function<RegistryKey<World>, Dimension> dimensionFactory)
	{
		// TODO load world on separate thread? not sure if good or bad idea
		Map<RegistryKey<World>, ServerWorld> map = server.forgeGetWorldMap();
		if (map.containsKey(worldKey))
		{
			return map.get(worldKey);
		}
		else
		{
			// forge fires the world load event *after* the world is put into the map
			// we'll do the same for best results
			// (this is why we're not just using map::computeIfAbsent)
			ServerWorld overworld = server.getWorld(World.OVERWORLD);
			// this is the same order server init creates these worlds:
			// instantiate world, add border listener, add to map, fire world load event
			// (in server init, the dimension is already in the dimension registry,
				// that'll get registered here before the world is instantiated as well)
			ServerWorld newWorld = createWorldAndRegisterDimension(server, worldKey, dimensionFactory);
			overworld.getWorldBorder().addListener(new IBorderListener.Impl(newWorld.getWorldBorder()));
			map.put(worldKey, newWorld);
			MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(newWorld)); // event isn't cancellable
			return newWorld;
		}
	}
	
	private static ServerWorld createWorldAndRegisterDimension(MinecraftServer server, RegistryKey<World> worldKey, Function<RegistryKey<World>, Dimension> dimensionFactory)
	{
		
		RegistryKey<Dimension> dimensionKey = RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, worldKey.getLocation());
		Dimension dimension = dimensionFactory.apply(worldKey);
		MinecraftServerAccessor serverAccess = (MinecraftServerAccessor)server;
		IServerConfiguration serverConfig = server.getServerConfiguration();
		DimensionGeneratorSettings dimensionGeneratorSettings = serverConfig.getDimensionGeneratorSettings();
		dimensionGeneratorSettings.func_236224_e_().register(dimensionKey, dimension, Lifecycle.experimental());
		DerivedWorldInfo derivedWorldInfo = new DerivedWorldInfo(serverConfig, serverConfig.getServerWorldInfo());
		// the int in create() here is radius of chunks to watch, 11 is what the server uses when it initializes worlds
		IChunkStatusListener chunkListener = serverAccess.getChunkStatusListenerFactory().create(11);
		ServerWorld newWorld = new ServerWorld(
			server,
			serverAccess.getBackgroundExecutor(),
			serverAccess.getAnvilConverterForAnvilFile(),
			derivedWorldInfo,
			worldKey,
			dimension.getDimensionType(),
			chunkListener,
			dimension.getChunkGenerator(),
			dimensionGeneratorSettings.func_236227_h_(), // flag: is-debug-world
			BiomeManager.getHashedSeed(dimensionGeneratorSettings.getSeed()),
			ImmutableList.of(), // "special spawn list"
				// phantoms, raiders, travelling traders, cats are overworld special spawns
				// the dimension loader is hardcoded to initialize preexisting non-overworld worlds with no special spawn lists
				// so this can probably be left empty for best results and spawns should be handled via other means
			false); // "tick time", true for overworld, always false for everything else
		return newWorld;
	}
}

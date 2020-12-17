package commoble.infiniverse;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.server.ServerWorld;

public class DebugTeleporterItem extends Item
{

	public DebugTeleporterItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		
		if (player instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
			
			ServerWorld nextWorld = player.world.getDimensionKey() == World.OVERWORLD
				? DimensionHelper.getOrCreateWorld(serverPlayer.server, Infiniverse.TEST_WORLD, key -> makeDimension(serverPlayer.server, key))
				: serverPlayer.server.getWorld(World.OVERWORLD); 
			
			BlockPos destination = player.getPosition();
			
			nextWorld.getChunk(destination); // ensure destination chunk is loaded first
			
			serverPlayer.teleport(nextWorld,
				destination.getX(),	destination.getY(), destination.getZ(),
				player.rotationYaw, player.rotationPitch);
		}
		
		return ActionResultType.SUCCESS;
	}

	static Dimension makeDimension(MinecraftServer server, RegistryKey<World> worldKey)
	{
		long seed = server.getWorld(World.OVERWORLD).getSeed() + worldKey.getLocation().hashCode();
		DynamicRegistries registries = server.func_244267_aX();
		Registry<DimensionSettings> noiseRegistry = registries.getRegistry(Registry.NOISE_SETTINGS_KEY);
		Registry<Biome> biomeRegistry = registries.getRegistry(Registry.BIOME_KEY);
		return new Dimension(
			() -> registries.getRegistry(Registry.DIMENSION_TYPE_KEY).getValueForKey(DimensionType.OVERWORLD),
			new NoiseChunkGenerator(
				new OverworldBiomeProvider(seed, false, false, biomeRegistry),
				seed,
				() -> noiseRegistry.getOrThrow(DimensionSettings.field_242734_c)
			));
	}
}

package commoble.infiniverse.examplemod;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.DynamicOps;

import commoble.infiniverse.api.InfiniverseAPI;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(ExampleMod.MODID)
public class ExampleMod
{
	public static final String MODID = "infiniverse_examplemod";
	public static final ResourceKey<Level> LEVEL_KEY = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(MODID, "example_dimension"));
	
	public ExampleMod()
	{
		NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
	}
	
	void onRegisterCommands(RegisterCommandsEvent event)
	{
		event.getDispatcher().register(Commands.literal("infiniverse_examplemod")
			.then(Commands.literal("create_dimension")
				.executes(this::createDimension))
			.then(Commands.literal("remove_dimension")
				.executes(this::removeDimension)));
	}
	
	int createDimension(CommandContext<CommandSourceStack> context)
	{
		InfiniverseAPI.get().getOrCreateLevel(context.getSource().getServer(), LEVEL_KEY, () -> createLevel(context.getSource().getServer()));
		
		return 1;
	}
	
	int removeDimension(CommandContext<CommandSourceStack> context)
	{
		InfiniverseAPI.get().markDimensionForUnregistration(context.getSource().getServer(), LEVEL_KEY);
		
		return 1;
	}
	
	LevelStem createLevel(MinecraftServer server)
	{
		ServerLevel oldLevel = server.overworld();
		DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
		ChunkGenerator oldChunkGenerator = oldLevel.getChunkSource().getGenerator();
		ChunkGenerator newChunkGenerator = ChunkGenerator.CODEC.encodeStart(ops, oldChunkGenerator)
			.flatMap(nbt -> ChunkGenerator.CODEC.parse(ops, nbt))
			.getOrThrow(false, s ->
			{
				throw new CommandRuntimeException(Component.literal(String.format("Error copying dimension: %s", s)));			
			});
		Holder<DimensionType> typeHolder = oldLevel.dimensionTypeRegistration();
		return new LevelStem(typeHolder, newChunkGenerator);
	}
}

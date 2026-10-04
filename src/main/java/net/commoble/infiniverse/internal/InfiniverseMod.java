package net.commoble.infiniverse.internal;

import java.util.Collection;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;

import net.commoble.infiniverse.api.InfiniverseAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permission.HasCommandLevel;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(InfiniverseMod.MODID)
public final class InfiniverseMod
{
	public static final String MODID = "infiniverse";
	private static final String OLD_DIMENSION_NAME_ARG = "old_dimension_name";
	private static final String NEW_DIMENSION_NAME_ARG = "new_dimension_name";
	private static final String WORLD_PRESET_ARG = "world_preset";
	private static final String SEED_ARG = "seed";
	private static final Collection<String> DIMENSION_EXAMPLES = List.of("namespace:id", "super_nether", "12345");
	private static final Collection<String> SEED_EXAMPLES = List.of("12345", "a bunch of words", "\"words! with. symbols?\"");
	private static final DynamicCommandExceptionType ERROR_INVALID_WORLD_PRESET = new DynamicCommandExceptionType(value -> Component.literal("Invalid world preset: " + value));

	public static final CommonConfig COMMONCONFIG = Util.make(() -> {
		final var entry = new ModConfigSpec.Builder().configure(CommonConfig::create);
		ModList.get().getModContainerById(MODID).get().registerConfig(ModConfig.Type.LOCAL, entry.getRight());
		return entry.getLeft();
	});
	
	// constructor is invoked by forge at start of modloading due to @Mod
	public InfiniverseMod(IEventBus modBus)
	{	
		IEventBus gameBus = NeoForge.EVENT_BUS;
		
		modBus.addListener(this::onRegisterPayloadHandlers);
		
		gameBus.addListener(this::onRegisterCommands);
	}
	
	private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
	{
		event.registrar(MODID)
			.optional()
			.playToClient(UpdateDimensionsPacket.TYPE, UpdateDimensionsPacket.STREAM_CODEC, UpdateDimensionsPacket::handle);
	}
	
	private void onRegisterCommands(RegisterCommandsEvent event)
	{
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		Permission editDimsPerms = new HasCommandLevel(PermissionLevel.byId(COMMONCONFIG.editDimensionRegistryPermissionLevel().get()));
		
		dispatcher.register(Commands.literal("infiniverse")
			.requires(stack -> stack.permissions().hasPermission(editDimsPerms))
			// infiniverse copy_dimension <old_dimension_name: Dimension> <new_dimension_name: Dimension> [server_seed|copy_seed|random_seed|seed] [seed: String]
			.then(Commands.literal("copy_dimension")
				.then(Commands.argument(OLD_DIMENSION_NAME_ARG, DimensionArgument.dimension())
					.then(Commands.argument(NEW_DIMENSION_NAME_ARG, IdentifierArgument.id())
						.suggests((context,builder) -> SharedSuggestionProvider.suggest(DIMENSION_EXAMPLES, builder))
						.executes(this.makeCopyDimensionCommand(this::copySeed))
						.then(Commands.literal("server_seed")
							.executes(this.makeCopyDimensionCommand(this::useServerSeed)))
						.then(Commands.literal("copy_seed")
							.executes(this.makeCopyDimensionCommand(this::copySeed)))
						.then(Commands.literal("random_seed")
							.executes(this.makeCopyDimensionCommand(this::useRandomSeed)))
						.then(Commands.literal("seed")
							.then(Commands.argument(SEED_ARG, StringArgumentType.greedyString())
								.suggests((context,builder) -> SharedSuggestionProvider.suggest(SEED_EXAMPLES, builder))
								.executes(this.makeCopyDimensionCommand(this::useSeed)))))))
			// infiniverse remove <old_dimension_name: Dimension>
			.then(Commands.literal("remove")
				.then(Commands.argument(OLD_DIMENSION_NAME_ARG, DimensionArgument.dimension())
					.executes(this::removeDimension)))
			// infiniverse restore_missing_dimensions <world_preset: WorldPreset>
			.then(Commands.literal("restore_missing_dimensions")
				.then(Commands.argument(WORLD_PRESET_ARG, ResourceKeyArgument.key(Registries.WORLD_PRESET))
					.executes(this::restoreMissingDimensions)))			
			);
	}

	
	private Command<CommandSourceStack> makeCopyDimensionCommand(SeedFactory factory)
	{
		return context ->
		{
			Identifier newID = IdentifierArgument.getId(context, NEW_DIMENSION_NAME_ARG);
			CommandSourceStack stack = context.getSource();
			MinecraftServer server = stack.getServer();
			ResourceKey<Level> newKey = ResourceKey.create(Registries.DIMENSION, newID);
			
			// make sure new dimension's new name isn't already in use
			ServerLevel existingNewLevel = server.getLevel(newKey);
			if (existingNewLevel != null)
			{
				throw new SimpleCommandExceptionType(new LiteralMessage(String.format("Error copying dimension: ID %s is already in use", newID))).create();
			}
			
			ServerLevel oldLevel = DimensionArgument.getDimension(context, OLD_DIMENSION_NAME_ARG);
			Holder<DimensionType> typeHolder = oldLevel.dimensionTypeRegistration();
			ChunkGenerator chunkGeneratorCopy = copyChunkGenerator(server, oldLevel);
			
			Supplier<LevelStem> dimensionFactory = ()-> new LevelStem(typeHolder, chunkGeneratorCopy, factory.createSeed(server, context, oldLevel));

			InfiniverseAPI.get().getOrCreateLevel(server, newKey, dimensionFactory);
			
			stack.sendSuccess(() -> Component.literal("Created dimension with id " + newID), false);
			
			return 1;
		};
	}
	
	private ChunkGenerator copyChunkGenerator(MinecraftServer server, ServerLevel oldLevel) throws CommandSyntaxException
	{
		// deep-copy the chunk generator (in case mods are storing state on them for some reason)
		DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
		ChunkGenerator oldChunkGenerator = oldLevel.getChunkSource().getGenerator();
		return ChunkGenerator.CODEC.encodeStart(ops, oldChunkGenerator)
			.flatMap(nbt -> ChunkGenerator.CODEC.parse(ops, nbt))
			.getOrThrow(s -> {
				return new SimpleCommandExceptionType(new LiteralMessage(String.format("Error copying dimension: %s", s))).create();			
			});
	}

	
	private OptionalLong copySeed(MinecraftServer server, CommandContext<CommandSourceStack> context, ServerLevel oldLevel)
	{
		return oldLevel.getSeedOverride();
	}
	
	private OptionalLong useServerSeed(MinecraftServer server, CommandContext<CommandSourceStack> context, ServerLevel oldLevel)
	{
		return OptionalLong.empty();
	}
	
	private OptionalLong useRandomSeed(MinecraftServer server, CommandContext<CommandSourceStack> context, ServerLevel oldLevel)
	{
		ServerLevel overworld = server.overworld();
		long newSeed = overworld.getRandom().nextLong();
		return OptionalLong.of(newSeed);
	}
	
	private OptionalLong useSeed(MinecraftServer server, CommandContext<CommandSourceStack> context, ServerLevel oldLevel)
	{
		String seedString = StringArgumentType.getString(context, SEED_ARG);
		// vanilla behavior is to try to parse the string as a long first, otherwise use its hashcode
		long seed;
		try
		{
			seed = Long.parseLong(seedString);
		}
		catch(NumberFormatException e)
		{
			seed = seedString.hashCode();
		}
		return OptionalLong.of(seed);
	}
	
	private int removeDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerLevel level = DimensionArgument.getDimension(context, OLD_DIMENSION_NAME_ARG);
		ResourceKey<Level> key = level.dimension();
		InfiniverseAPI.get().markDimensionForUnregistration(context.getSource().getServer(), key);
		context.getSource().sendSuccess(() -> Component.literal("Removing dimension with id " + key.identifier()), false);
		return 1;
	}
	
	private int restoreMissingDimensions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		MinecraftServer server = context.getSource().getServer();
		var presetKey = ResourceKeyArgument.getRegistryKey(context, WORLD_PRESET_ARG, Registries.WORLD_PRESET, ERROR_INVALID_WORLD_PRESET);
		@Nullable WorldPreset preset = server.registryAccess().lookupOrThrow(Registries.WORLD_PRESET).getValue(presetKey);
		if (preset == null)
		{
			context.getSource().sendFailure(Component.literal("Invalid world preset " + presetKey.identifier()));
			return 0;
		}
		int created = 0;
		for (var entry : preset.createWorldDimensions().dimensions().entrySet())
		{
			ResourceKey<LevelStem> key = entry.getKey();
			ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, key.identifier());
			if (server.getLevel(levelKey) == null)
			{
				LevelStem levelStem = entry.getValue();
				InfiniverseAPI.get().getOrCreateLevel(server, levelKey, () -> levelStem);
				context.getSource().sendSuccess(() -> Component.literal("Created dimension " + levelKey.identifier()), false);
				created++;
			}
		}
		
		return created;
	}
	
	private static interface SeedFactory
	{
		public abstract OptionalLong createSeed(MinecraftServer server, CommandContext<CommandSourceStack> context, ServerLevel oldLevel);
	}
}

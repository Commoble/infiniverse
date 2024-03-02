package net.commoble.infiniverse.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * @param keys Keys to add or remove in the client's dimension list
 * @param add If true, keys are to be added; if false, keys are to be removed
 */
public record UpdateDimensionsPacket(Set<ResourceKey<Level>> keys, boolean add) implements CustomPacketPayload
{
	public static final ResourceLocation ID = new ResourceLocation(InfiniverseMod.MODID, "update_dimensions");
	
	public static UpdateDimensionsPacket read(FriendlyByteBuf buffer)
	{
		Set<ResourceKey<Level>> keys = buffer.readCollection(i->new HashSet<>(), buf->ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()));
		boolean add = buffer.readBoolean();
		
		return new UpdateDimensionsPacket(keys,add);
	}
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeCollection(this.keys(), (buf,key)->buf.writeResourceLocation(key.location()));
		buffer.writeBoolean(this.add());
	}
	
	public void handle(PlayPayloadContext context)
	{
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			context.workHandler().execute(() -> ClientHandler.handle(this));
		}
	}

	private static class ClientHandler // making client calls in the static class prevents classloading errors
	{
		private static void handle(UpdateDimensionsPacket packet)
		{
			@SuppressWarnings("resource")
			final LocalPlayer player = Minecraft.getInstance().player;
			if (player == null)
				return;
			
			final Set<ResourceKey<Level>> dimensionList = player.connection.levels();
			if (dimensionList == null)
				return;
			
			Consumer<ResourceKey<Level>> keyConsumer = packet.add()
				? dimensionList::add
				: dimensionList::remove;
			
			packet.keys().forEach(keyConsumer);
		}
	}
}

package net.commoble.infiniverse.internal;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(InfiniverseMod.MODID)
public final class InfiniverseMod
{
	public static final String MODID = "infiniverse";
	
	// constructor is invoked by forge at start of modloading due to @Mod
	public InfiniverseMod(IEventBus modBus)
	{		
		modBus.addListener(this::onRegisterPayloadHandlers);
	}
	
	void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
	{
		event.registrar(MODID)
			.optional()
			.playToClient(UpdateDimensionsPacket.TYPE, UpdateDimensionsPacket.STREAM_CODEC, UpdateDimensionsPacket::handle);
	}
}

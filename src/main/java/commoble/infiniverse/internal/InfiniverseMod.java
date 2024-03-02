package commoble.infiniverse.internal;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

@Mod(InfiniverseMod.MODID)
public final class InfiniverseMod
{
	public static final String MODID = "infiniverse";
	
	// constructor is invoked by forge at start of modloading due to @Mod
	public InfiniverseMod(IEventBus modBus)
	{		
		modBus.addListener(this::onRegisterPayloadHandlers);
	}
	
	void onRegisterPayloadHandlers(RegisterPayloadHandlerEvent event)
	{
		event.registrar(MODID)
			.play(new ResourceLocation(MODID, "update_dimensions"), UpdateDimensionsPacket::read, UpdateDimensionsPacket::handle);
	}
}

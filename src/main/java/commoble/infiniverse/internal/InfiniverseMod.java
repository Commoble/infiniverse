package commoble.infiniverse.internal;

import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

@Mod(InfiniverseMod.MODID)
public final class InfiniverseMod
{
	public static final String MODID = "infiniverse";
	
	public static final String PROTOCOL_VERSION = "1";
	public static final Predicate<String> NETWORK_TEST = protocol ->
		PROTOCOL_VERSION.equals(protocol) ||
		NetworkRegistry.ABSENT.equals(protocol) ||
		NetworkRegistry.ACCEPTVANILLA.equals(protocol);
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(MODID, "main"),
		() -> PROTOCOL_VERSION,
		NETWORK_TEST,
		NETWORK_TEST);
	
	// constructor is invoked by forge at start of modloading due to @Mod
	public InfiniverseMod(IEventBus modBus)
	{		
		int packetID = 0;
		CHANNEL.<UpdateDimensionsPacket>registerMessage(packetID++, UpdateDimensionsPacket.class,
			UpdateDimensionsPacket::write,
			UpdateDimensionsPacket::read,
			UpdateDimensionsPacket::handle);
	}
}

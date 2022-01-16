package commoble.infiniverse.internal;

import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

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
	public InfiniverseMod()
	{
		// mod is not required to be on both sides, greenlight mismatched servers in client's server list
		ModLoadingContext.get().registerExtensionPoint(DisplayTest.class,
			() -> new DisplayTest(
				() -> NetworkConstants.IGNORESERVERONLY,
				(s, networkBool) -> true));
		
		int packetID = 0;
		CHANNEL.registerMessage(packetID++, UpdateDimensionsPacket.class,
			UpdateDimensionsPacket::write,
			UpdateDimensionsPacket::read,
			UpdateDimensionsPacket::handle);
	}
}

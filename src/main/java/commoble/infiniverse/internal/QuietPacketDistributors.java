package commoble.infiniverse.internal;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Packet distributors and helpers for sending packets only to players who have the channel
 * being used to send packets.
 */
// we can't just wrap the existing distributors because of the way the functors are written
public final class QuietPacketDistributors
{
	private QuietPacketDistributors() {}
	
	// sends packets to all players but just the ones that have the provided channel
	private static final PacketDistributor<SimpleChannel> ALL = new PacketDistributor<>(
		(distributor, channelGetter) -> packet -> ServerLifecycleHooks.getCurrentServer()
			.getPlayerList()
			.getPlayers()
			.stream()
			.filter(player -> channelGetter.get().isRemotePresent(player.connection.connection))
			.forEach(player -> player.connection.connection.send(packet)),
		NetworkDirection.PLAY_TO_CLIENT);

	public static <PACKET> void sendToAll(SimpleChannel channel, PACKET packet)
	{
		channel.send(ALL.with(()->channel), packet);
	}
}

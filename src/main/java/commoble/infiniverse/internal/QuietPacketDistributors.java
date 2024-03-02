package commoble.infiniverse.internal;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Packet distributors and helpers for sending packets only to players who have the channel
 * being used to send packets.
 */
// we can't just wrap the existing distributors because of the way the functors are written
public final class QuietPacketDistributors
{
	private QuietPacketDistributors() {}

	public static <PACKET extends CustomPacketPayload> void sendToAll(MinecraftServer server, PACKET packet)
	{
		for (ServerPlayer player : server.getPlayerList().getPlayers())
		{
			if (player.connection.isConnected(packet))
			{
				PacketDistributor.PLAYER.with(player).send(packet);
			}
		}
	}
}

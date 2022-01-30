package commoble.infiniverse.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fires when a dimension/level is about to be unregistered by Infiniverse.<br>
 * This event fires on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS} and is not cancellable.<br>
 */
public class UnregisterDimensionEvent extends Event
{
	private final ServerLevel level;
	
	public UnregisterDimensionEvent(ServerLevel level)
	{
		this.level = level;
	}
	
	/**
	 * @return The level that is about to be unregistered by Infiniverse.
	 */
	public ServerLevel getLevel()
	{
		return this.level;
	}
}

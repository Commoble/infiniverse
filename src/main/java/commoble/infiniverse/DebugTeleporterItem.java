package commoble.infiniverse;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DebugTeleporterItem extends Item
{

	public DebugTeleporterItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		
		if (player instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
			
			RegistryKey<World> destinationKey = player.world.getDimensionKey() == World.OVERWORLD ? World.THE_NETHER : World.OVERWORLD; 
			
			ServerWorld nextWorld = serverPlayer.getServer().getWorld(destinationKey);
			
			BlockPos destination = player.getPosition();
			
			nextWorld.getChunk(destination); // ensure destination chunk is loaded first
			
			serverPlayer.teleport(nextWorld,
				destination.getX(),	destination.getY(), destination.getZ(),
				player.rotationYaw, player.rotationPitch);
		}
		
		return ActionResultType.SUCCESS;
	}

	
}

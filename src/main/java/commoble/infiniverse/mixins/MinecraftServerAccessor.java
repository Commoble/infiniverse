package commoble.infiniverse.mixins;

import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.storage.SaveFormat;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor
{
	@Accessor
	Executor getBackgroundExecutor();
	
	@Accessor
	SaveFormat.LevelSave getAnvilConverterForAnvilFile();
	
	@Accessor
	IChunkStatusListenerFactory getChunkStatusListenerFactory();
}

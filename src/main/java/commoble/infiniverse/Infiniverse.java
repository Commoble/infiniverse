package commoble.infiniverse;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(Infiniverse.MODID)
public class Infiniverse
{
	public static final String MODID = "infiniverse";
	
	public static Infiniverse INSTANCE = null;
	
	public static final RegistryKey<Dimension> TEST_DIM = RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, new ResourceLocation(Infiniverse.MODID, "test"));
	public static final RegistryKey<World> TEST_WORLD = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(Infiniverse.MODID, "test"));
	
	
	// constructor is invoked by forge at start of modloading due to @Mod
	public Infiniverse()
	{
		INSTANCE = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		DeferredRegister<Item> items = makeRegistry(modBus, ForgeRegistries.ITEMS);
		
		items.register(Names.DEBUG_TELEPORTER, () -> new DebugTeleporterItem(new Item.Properties().group(ItemGroup.MISC)));
	}
	
	// helper methods for registering things
	private static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> makeRegistry(IEventBus modBus, IForgeRegistry<T> registry)
	{
		DeferredRegister<T> register = DeferredRegister.create(registry, MODID);
		register.register(modBus);
		return register;
	}
}

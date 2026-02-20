package net.commoble.infiniverse.internal;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public record CommonConfig(IntValue editDimensionRegistryPermissionLevel)
{
	public static CommonConfig create(ModConfigSpec.Builder builder)
	{
		builder.comment("Minimum permission level needed to create/remove dimensions and use related commands.",
			"Defaults to 4 (server owner)");
		IntValue minCommandPermissionLevel = builder.defineInRange("editDimensionRegistryPermissionLevel", 4, 0, 4);
		return new CommonConfig(minCommandPermissionLevel);
	}
}
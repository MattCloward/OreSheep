package oresheepmod;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class CommonProxy
{	
	public void preInit(FMLPreInitializationEvent e)
	{
		int id = 1;
		ResourceLocation oreLoc = new ResourceLocation(ModOreSheep.modid + ":" + "Ore Sheep");
	    EntityRegistry.registerModEntity(oreLoc, EntityOreSheep.class, "Ore Sheep", id++, ModOreSheep.instance, 64, 3, true, 0x626262, 0xcb9090);
    }
}

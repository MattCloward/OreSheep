package oresheepmod;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
    public void preInit(FMLPreInitializationEvent e) 
	{
        super.preInit(e);
        RenderingRegistry.registerEntityRenderingHandler(EntityOreSheep.class, RenderOreSheep.FACTORY);
    }
}

package oresheepmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import oresheepmod.ServerBeBlocksPacket.BeBlocksHandler;

@Mod(modid= ModOreSheep.modid,name="Ore Sheep Mod",version="v4.0.0")

public class ModOreSheep 
{
	@SidedProxy(clientSide = "oresheepmod.ClientProxy", serverSide = "oresheepmod.CommonProxy")
	public static CommonProxy proxy;
	
	public static final String modid = "oresheepmod";
	
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(modid);
	
	@Mod.Instance
    public static ModOreSheep instance;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}
	
	//in post so that all blocks load before they are put into the possible blocks file
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		OreRegistryDraw.writeConfigAndBlocksFile();
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			OreRegistryDraw.readConfig();
			OreRegistryDraw.canRead = false;
		}
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		INSTANCE.registerMessage(BeBlocksHandler.class, ServerBeBlocksPacket.class, 0, Side.CLIENT);

		MinecraftForge.EVENT_BUS.register(new OreSheepEventHandler());
	}
	
	// register server commands
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandIgnoreMobGriefing());
	}
	
	//resets readability when a world is unloaded (integrated server only)
	@EventHandler
	public void integratedSeverClose(FMLServerStoppingEvent event)
	{
		if (event.getSide() == Side.CLIENT)
		{
			if (!OreRegistryDraw.canRead)
			{
				OreRegistryDraw.canRead = true;
			}
		}
	}
}

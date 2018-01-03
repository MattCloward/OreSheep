package oresheepmod;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ModOreSheep.modid)
@Config.LangKey("oresheepmod.config.title")
public class ModConfig 
{

	@Config.Comment("Ignores the mobGriefing game rule.")
	public static boolean IgnoreGameRuleMobGriefing = true;

	@Mod.EventBusSubscriber
	private static class EventHandler 
	{
		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
		{
			if (event.getModID().equals(ModOreSheep.modid))
			{
				ConfigManager.sync(ModOreSheep.modid, Config.Type.INSTANCE);
			}
		}
	}
}

package oresheepmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OreSheepEventHandler
{	
	//deletes all old ore sheep textures when textures are reloaded (resource pack compatible: yay!)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	@SideOnly(Side.CLIENT)
	public void handleTextureReload(TextureStitchEvent.Pre event)
	{
		OreRegistryDraw.deleteExistingTextures();
	}
	
	//sends the config information from the server to the clients
	@SubscribeEvent
	@SideOnly(Side.SERVER)
	public void onLogInServer(PlayerEvent.PlayerLoggedInEvent event)
	{
		ModOreSheep.INSTANCE.sendTo(new ServerBeBlocksPacket(OreRegistryDraw.getBeBlocks(), OreRegistryDraw.getEatBlocks(), ModConfig.IgnoreGameRuleMobGriefing), (EntityPlayerMP) event.player);
	}
	
	//reads the config when a world is loaded (we don't use PlayerLoggedInEvent because we want it to work when the game is first loaded)
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event)
	{
		if (!event.getWorld().isRemote && OreRegistryDraw.canRead)
		{
			OreRegistryDraw.readConfig();
			OreRegistryDraw.canRead = false;
		}
	}
	
	//logging out of an integrated or dedicated server lets RenderOreSheep know that it can delete old textures the next time it is called
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onLogOutClient(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
	{
		OreRegistryDraw.canRefreshTextures = true;
	}
	
	//called when the player right-clicks on an entity
	@SubscribeEvent
	public void itemInteractionForEntity(EntityInteract event) 
	{
		Entity target = event.getTarget();
		
		EntityPlayer entityPlayer = event.getEntityPlayer();
		ItemStack heldItem = entityPlayer.getHeldItemMainhand();
		//if the target exists...
		if (target != null && heldItem != null && heldItem.getItem() != Items.AIR)
		{
			//if target is a sheep
			if (target instanceof EntitySheep)
			{
				EntitySheep entitysheep = (EntitySheep)target;
				
				//if held item is a diamond and the sheep has the weakness effect
				if (heldItem.getItem() == Items.DIAMOND && entitysheep.isPotionActive(MobEffects.WEAKNESS) && !entitysheep.isDead && !entityPlayer.world.isRemote)
				{
					//if the player isn't in creative mode, subtract one from the stack
					if (!entityPlayer.capabilities.isCreativeMode)
					{
						heldItem.shrink(1);
					}
					//convert to ore sheep
					EntityOreSheep entityoresheep = new EntityOreSheep(entitysheep.world);
					entityoresheep.copyLocationAndAnglesFrom(entitysheep);
					entityoresheep.setGrowingAge(entitysheep.getGrowingAge());
					if (entitysheep.hasCustomName())
					{
						entityoresheep.setCustomNameTag(entitysheep.getCustomNameTag());
					}
					entitysheep.removePotionEffect(MobEffects.WEAKNESS);
					//gives new ore sheep all of the old sheeps potion effects
					if (!entitysheep.getActivePotionEffects().isEmpty())
					{
						ArrayList<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
						potionEffects.addAll(entitysheep.getActivePotionEffects());
						for(int i = 0; i < potionEffects.size(); i++)
						{
							entityoresheep.addPotionEffect(potionEffects.get(i));
						}
					}
					entitysheep.world.removeEntity(entitysheep);
					entityoresheep.setOreInteger(0);				
				    entityoresheep.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0F, 1.0F);
				    entityoresheep.world.spawnEntity(entityoresheep);
				}
						
			}
			//if the target is an ore sheep
			if (target instanceof EntityOreSheep)
			{
				EntityOreSheep entityoresheep = (EntityOreSheep)target;

				//if the sheep isn't sheared and the ore sheep isn't a child
				if (!entityoresheep.getSheared() && !entityoresheep.isChild())
				{
					if (isTool(heldItem) && !entityoresheep.world.isRemote)
					{
						if (canToolHarvest(entityoresheep, heldItem))
						{
							doShear(event, entityoresheep, heldItem);
						}
						else
						{
							//makes break sound if not in creative
							entityoresheep.playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8F, 0.8F + entityoresheep.world.rand.nextFloat() * 0.4F);
						}
					}
	
				}
				if (entityPlayer.capabilities.isCreativeMode)
				{
					//if sheared, grows sheep ore back with bonemeal or if child, makes child grown
					if ((entityoresheep.getSheared() || entityoresheep.isChild()) && heldItem.getItem() == Items.DYE && heldItem.getItem().getMetadata(heldItem) == 15)
					{
						if (!entityoresheep.world.isRemote)
						{
							entityoresheep.eatBlockBonus();
						}
						else
						{
							entityoresheep.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, entityoresheep.posX + (double)(entityoresheep.world.rand.nextFloat() * entityoresheep.width * 2.0F) - (double)entityoresheep.width, entityoresheep.posY + 0.5D + (double)(entityoresheep.world.rand.nextFloat() * entityoresheep.height), entityoresheep.posZ + (double)(entityoresheep.world.rand.nextFloat() * entityoresheep.width * 2.0F) - (double)entityoresheep.width, 0.0D, 0.0D, 0.0D, new int[0]);
						}
					}
				}
				
				//testing purposes
				if (entityPlayer.capabilities.isCreativeMode && (entityPlayer.getName().contains("Player") || entityPlayer.getName().equals("Icedice9")) && !entityoresheep.world.isRemote && entityoresheep.isPotionActive(MobEffects.WEAKNESS))
				{
					if (heldItem.getItem() == Items.STICK)
					{
						entityoresheep.setOreInteger(entityoresheep.getOreInt() + 1);
						if (entityoresheep.getOreInt() >= OreRegistryDraw.getBeBlocks().size())
						{
							entityoresheep.setOreInteger(0);
						}
						entityoresheep.removePotionEffect(MobEffects.WEAKNESS);
					}
					else if (heldItem.getItem() == Items.BLAZE_ROD)
					{
						if ((entityoresheep.getOreInt() - 1) >= 0)
						{
							entityoresheep.setOreInteger(entityoresheep.getOreInt() - 1);
						}
						else
						{
							entityoresheep.setOreInteger(OreRegistryDraw.getBeBlocks().size() - 1); 
						}
						entityoresheep.removePotionEffect(MobEffects.WEAKNESS);
					}
					else if (heldItem.getItem() == Items.APPLE)
					{
						System.out.println(entityoresheep.getOreBlock() + " " + entityoresheep.getOreBlock().getUnlocalizedName() + " " + entityoresheep.getOreMeta() + " " + entityoresheep.getDropItem().getUnlocalizedName());
						entityoresheep.removePotionEffect(MobEffects.WEAKNESS);
					}
				}
				
				//if the player is in creative mode
				if (entityPlayer.capabilities.isCreativeMode && !entityoresheep.world.isRemote)
				{
					//finds the BlockEntry that resembles the block used (using the meta and block of the BlockEntry for comparison) on the sheep and sets the sheep to that block
					//if the block used isn't an "ore", nothing happens
					if (OreRegistryDraw.getAllItemsFromBlocks(OreRegistryDraw.getAllBeBlocks(), entityoresheep.world.rand).contains(heldItem.getItem()));
					{
						for (int i = 0; i < OreRegistryDraw.getBeBlocks().size(); i++)
						{
							Item itemFromBlock = null;
							Item itemGetItem = null;
							IBlockState state = OreRegistryDraw.getBeBlocks().get(i).getState();
							Block block = state.getBlock();
							itemFromBlock = Item.getItemFromBlock(state.getBlock());
							int meta = OreRegistryDraw.getBeBlocks().get(i).getMeta();
							//TODO three places to find this code (OreSheepEventHandler- creating change, OreRegistryDraw- getAllItemsFromBlocks, and EntityOreSheep- getDropItem :merge these!
							if (block.hasTileEntity(state))
							{
								itemGetItem = new ItemStack(block, 1, block.getMetaFromState(state)).getItem();
								//flowerpot fix
								if (itemGetItem == null || itemGetItem == Item.getItemFromBlock(Blocks.AIR))
								{
									itemGetItem = state.getBlock().getItemDropped(state, entityoresheep.world.rand, 0);
								}
							}
							if (itemGetItem == null || itemGetItem == Item.getItemFromBlock(Blocks.AIR))
							{
								itemGetItem = block.getItem(null, null, state).getItem();
							}
							if ((heldItem.getItem().equals(itemFromBlock) || (itemGetItem != null && itemGetItem.equals(heldItem.getItem()))) && heldItem.getItemDamage() == meta)
							{
								if (entityoresheep.getOreInt() != i)
								{
									entityoresheep.setOreInteger(i);
									entityoresheep.setMetadata(meta);
								}
								break;
							}
						}
					}
				}
			}
		}
	}
	
	public boolean canToolHarvest(EntityOreSheep entityoresheep, ItemStack heldItem)
    {
		String toolType = null;
    	for (String k : heldItem.getItem().getToolClasses(heldItem))
    	{
    		toolType = k;
    	}
        return heldItem != null ? isToolEffective(toolType, entityoresheep.getOreState(), heldItem.getItem()) && heldItem.getItem().getHarvestLevel(heldItem, toolType, null, entityoresheep.getOreState()) >= entityoresheep.getOreBlock().getHarvestLevel(entityoresheep.getOreBlockEntry().getState()) : false;
    }
	/**
     * Checks if the specified tool type is efficient on this block,
     * meaning that it digs at full speed. Already assumes that held item is a tool.
     *
     * @param type
     * @param metadata
     * @return
     */
	public boolean isToolEffective(String type, IBlockState state, Item item)
    {
    	Block block = state.getBlock();
    	Material[] axeList = {Material.WOOD, Material.GOURD};
        Material[] shovelList = {Material.CLAY, Material.CRAFTED_SNOW, Material.GRASS, Material.GROUND, Material.SAND, Material.SNOW};
        Material[] pickaxeList = {Material.ANVIL, Material.CORAL, Material.DRAGON_EGG, Material.IRON, Material.PISTON, Material.ROCK};
        if (block == Blocks.HAY_BLOCK || block == Blocks.BED)
        {
        	return true;
        } 
        if (state.getMaterial() == Material.CLOTH || state.getBlock() == Blocks.CARPET)
		{
        	return item == Items.SHEARS;
        }
        if (block == Blocks.MONSTER_EGG)
    	{
        	return "pickaxe".equals(type);
    	}

        if (Arrays.asList(axeList).contains(state.getMaterial()) || block == Blocks.WOODEN_BUTTON || block == Blocks.LADDER)
        {
        	return "axe".equals(type);
        }
        else if (Arrays.asList(shovelList).contains(state.getMaterial()))
        {
        	return "shovel".equals(type);
        }
        else if (Arrays.asList(pickaxeList).contains(state.getMaterial()) || block == Blocks.STONE_BUTTON)
        {
        	return "pickaxe".equals(type);
        }
        else if (state.getMaterial().isToolNotRequired())
        {
        	return true;
        }
        return false;
    }
	
    /*
     * used for testing purposes
     * clientPrint("Block: " + entityoresheep.getOreBlock() + " Tool required: " + !entityoresheep.getOreBlock().getMaterial().isToolNotRequired());
     * clientPrint("String: " + k + " Level: " + heldItem.getItem().getHarvestLevel(heldItem, k));
     * clientPrint("Tool: "+ toolType + " Tool Level: " + heldItem.getItem().getHarvestLevel(heldItem, toolType) + " Block Level: " + entityoresheep.getOreBlock().getHarvestLevel(entityoresheep.getOreBlockEntry().getMeta()));
	
	@SideOnly(Side.CLIENT)
	public static void clientPrint(String str)
	{
		System.out.println(str);
	}
	*/


	//shears the sheep, giving the sheep's block
		public static void doShear(EntityInteract event, EntityOreSheep entityoresheep, ItemStack heldItem)
		{
			if (!entityoresheep.world.isRemote)
	        {
				//shears the ore sheep, dropping one of the ore sheep's ore
	            ArrayList<ItemStack> drops = entityoresheep.onSheared(heldItem, entityoresheep.world, (int)entityoresheep.posX, (int)entityoresheep.posY, (int)entityoresheep.posZ, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, heldItem));

	            if (drops != null)
	            {
	                Random rand = new Random();
	                for(ItemStack stack : drops)
	                {
	                    EntityItem entity = entityoresheep.entityDropItem(stack, 1.0F);
	                    if (entity != null)
	                    {
	                    	entity.motionY += rand.nextFloat() * 0.05F;
	                        entity.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
	                        entity.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
	                    }
	                }
	                //damages the tool if not in creative mode
	                if (!event.getEntityPlayer().capabilities.isCreativeMode)
	                {
	                	heldItem.damageItem(1, entityoresheep);  
	                }
	            }
	        }
		}
	
	//determines whether the item held is a pickaxe
	public static boolean isPick(ItemStack heldItem)
	{
		String heldItemName = heldItem.getUnlocalizedName().toLowerCase();
		if (heldItem.getItem() instanceof ItemPickaxe || (heldItemName.contains("pick") && heldItem.isItemEnchantable()))
		{
			return true;
		}
		return false;
	}
	//determines whether the item held is a shovel
	public static boolean isShovel(ItemStack heldItem)
	{
		String heldItemName = heldItem.getUnlocalizedName().toLowerCase();
		if (heldItem.getItem() instanceof ItemSpade || ((heldItemName.contains("spade") || heldItemName.contains("shovel")) && heldItem.isItemEnchantable()))
		{
			return true;
		}
		return false;
	}
	//determines whether the item held is an axe
	public static boolean isAxe(ItemStack heldItem)
	{
		String heldItemName = heldItem.getUnlocalizedName().toLowerCase();
		if (heldItem.getItem() instanceof ItemAxe || ((heldItemName.contains("axe") && !heldItemName.contains("pick") || heldItemName.contains("hatchet")) && heldItem.isItemEnchantable()))
		{
			return true;
		}
		return false;
	}
	
	//determines whether the item held is a pickaxe, shovel, or axe
	public static boolean isTool(ItemStack heldItem)
	{
		return isPick(heldItem) || isShovel(heldItem) || isAxe(heldItem) || heldItem.getItem() == Items.SHEARS;
	}
}

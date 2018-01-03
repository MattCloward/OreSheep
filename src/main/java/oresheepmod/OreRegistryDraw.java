package oresheepmod;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDoubleStoneSlab;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OreRegistryDraw
{
	private static ArrayList<BlockEntry> beBlocks = new ArrayList<BlockEntry>();
	private static ArrayList<Block> eatBlocks = new ArrayList<Block>();
	private static Scanner scan;
	private static ArrayList<TextureAtlasSprite> allSprites = new ArrayList<TextureAtlasSprite>();
	private static ArrayList<ResourceLocation> allResources = new ArrayList<ResourceLocation>();
	private static String fS = System.getProperty("file.separator");
	private static String str = new File("").getAbsolutePath();
	private static File thisFile = new File(str);
	private static File oreSheepAssets = new File(thisFile + fS +"config" + fS + "oresheep");
	private static File oreSheepLog = new File(oreSheepAssets + fS + "Log.txt");
	private static File config = new File(oreSheepAssets + fS + "OreConfig.txt");
	private static File log = new File(oreSheepAssets + fS + "Log.txt");
	//used so that config is read only once during world load
	public static boolean canRead = true;
	public static boolean canRefreshTextures = true;
	
	public static ArrayList<BlockEntry> getBeBlocks()
	{
		return beBlocks;
	}
	
	public static void setBeBlocks(ArrayList<BlockEntry> beBlocks2)
	{
		beBlocks = beBlocks2;
	}
	
	//gets all blocks from beBlocks ArrayList
	public static ArrayList<Block> getAllBeBlocks()
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		for (int i = 0; i < getBeBlocks().size(); i++)
		{
			blocks.add(getBeBlocks().get(i).getBlock());
		}
		return blocks;
	}
	
	public static ArrayList<Block> getEatBlocks()
	{
		return eatBlocks;
	}
	
	public static void setEatBlocks(ArrayList<Block> eatBlocks2)
	{
		eatBlocks = eatBlocks2;
	}
	
	public static void createDirectories()
	{
		try
		{
			// if directory doesn't exist, create it
			if (!oreSheepAssets.exists())
			{
				if (!oreSheepAssets.mkdirs())
				{
					System.out.println("Failed to create config directory! Contact oresheepmod author!");
				}
			}
			
			// if config doesn't exists, create it
			if (!config.exists())
			{
				//write to the file the default config text
				String content = "allOres\nhellrock\nblocks to eat:\nstairsStone\nstoneSlab\nbutton\npressurePlateStone\nstonebrick\ncobbleWall\nstoneMoss\ngravel\nstonebricksmooth\nstairsStoneBrickSmooth";
				FileWriter fw = new FileWriter(config.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(content);
				bw.close();
			}
			
			//reset the log file
			FileWriter fw = new FileWriter(log.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("");
			bw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void readConfig()
	{
		beBlocks.clear();
		eatBlocks.clear();
		try 
		{
			scan = new Scanner(config);
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		addAllSubBlocks(Blocks.STONE);
		boolean eat = false;
		writeToLog("\nReading config file... ");
		if (scan != null)
		{
			//while scanner has more
			while (scan.hasNextLine())
			{
				String scanNext = scan.nextLine();
				//exeptions
				if (scanNext.toLowerCase().contains("bedrock"))
				{
					writeToLog("Sheep can't eat " + scanNext + "! That would be dangerous!");
					continue;
				}
				if (scanNext.toLowerCase().contains("//") || scanNext.equals(""))
				{
					continue;
				}
				else if (scanNext.toLowerCase().contains("blocks to eat:"))
				{
					eat = true;
				}
				else if (scanNext.toLowerCase().contains("allore"))
				{
					getAllOres();
				}
				else
				{
					boolean found = false;
					String extra = "";
					Iterator blockIterator = Block.REGISTRY.iterator();
					int numFound = 0;
					if (!eat)
					{
						String out = "Looking for " + scanNext + ":";
					    while (blockIterator.hasNext())
						{
					    	Block block = (Block)blockIterator.next();
					    	String itName = block.getUnlocalizedName(); 
					    	if (itName.contains("tile."))
					    	{
						    	itName = itName.substring(5);
					    	}
					    	if (scanNext.equals(itName))
					    	{
					    		//prevents dupe blocks
					    		if (getAllBeBlocks().contains(block))
					    		{
					    			extra = " However, " + scanNext + " has already been added. Consider removing extra copies!";
					    			found = true;
					    			break;
					    		}
					    		//outlawed blocks
					    		else if (!isBlockLegal(block))
					    		{
					    			extra = " However, sheep can't eat " + scanNext + "!";
					    			found = true;
					    			break;
					    		}
					    		else
					    		{
					    			addAllSubBlocks(block);
						    		//doesn't break here because some blocks are named the same and must be included (snow block vs snow layer, both snow in config)
					    			numFound++;
						    		found = true;
					    		}
					    	}
						}
					    if (found)
					    {
					    	if (numFound <= 1)
					    	{
						    	out = out + " found!" + extra;
					    	}
					    	else
					    	{
					    		out = out + numFound + " of " + scanNext +" found!" + extra;
					    	}
					    }
					    else
					    {
					    	out = out + " not found! Recheck the config file!";
					    }
					    writeToLog(out);
					}
					else
					{
						String out = "Looking for " + scanNext + ":";
					    while (blockIterator.hasNext())
						{
					    	Block block = (Block)blockIterator.next();
					    	String unlocalizedName = block.getUnlocalizedName();
					    	String itName = unlocalizedName;
					    	if (unlocalizedName.contains("tile."))
					    	{
						    	itName = unlocalizedName.substring(5);
					    	}
					    	if (scanNext.equals(itName))
					    	{
					    		getEatBlocks().add(block);
					    		if (getAllBeBlocks().contains(block))
					    		{
					    			extra = " " + scanNext + " is in the be blocks and eat blocks sections. Please remove it from one of them.";
					    		}
					    		found = true;
					    	}
						}
					    if (found)
					    {
					    	out = out + " found!"  + extra;
					    }
					    else
					    {
					    	out = out + " not found! Recheck the config file!";
					    }
					    writeToLog(out);
					}
				}
			}
		}
		else
		{
			writeToLog("Scanner is null. Not a good sign!");
		}
	}

	//gets all items from blocks from beBlocks ArrayList. Used in OreSheepHandler to calculate creative block change
	public static ArrayList<Item> getAllItemsFromBlocks(ArrayList<Block> blocks, Random rand)
	{
		ArrayList<Item> items = new ArrayList<Item>();
		for (int i = 0; i < blocks.size(); i++)
		{
			Block block = blocks.get(i);
			IBlockState state = block.getDefaultState();
			if (block.hasTileEntity(state))
			{
				Item item = new ItemStack(block, 1, block.getMetaFromState(state)).getItem();
				if (item != null && item != Items.AIR)
				{
					items.add(item);
				}
				else
				{
					item = block.getItemDropped(state, rand, 0);
					if (item != null && item != Items.AIR)
					{
						items.add(item);
					}
				}
			}
			else
			{
				//TODO three places to find this code (OreSheepEventHandler- creating change, OreRegistryDraw- getAllItemsFromBlocks, and EntityOreSheep- getDropItem :merge these!
				Item itemGetItem = block.getItem(null, null, block.getDefaultState()).getItem();
				if (itemGetItem != null && itemGetItem != Items.AIR)
				{
					items.add(itemGetItem);
				}
				else
				{
					Item item = Item.getItemFromBlock(block);
					if (item != null && item != Items.AIR)
					{
						items.add(item);
					}
				}
			}
		}
		return items;
	}
	
	private static void writeToLog(String content)
	{
		FileWriter fw;
		try 
		{
			fw = new FileWriter(log.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content + "\n");
			bw.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private static void getAllOres()
	{
		Iterator blockIterator = Block.REGISTRY.iterator();
		while (blockIterator.hasNext())
		{
			Block block = (Block)blockIterator.next();
			if (isBlockLegal(block))
			{
				String blockName = block.getUnlocalizedName().toLowerCase();
				if ((block instanceof BlockOre || blockName.contains("ore"))
					/*mod checks for specific blocks*/	|| blockName.contains("tile.artifice.sulfur") || blockName.contains("tile.artifice.uranium") || blockName.contains("tile.for.resources") || blockName.contains("tile.artifice.niter") || blockName.contains("tile.biomeblock")
					/*blocks that are ignored*/			&& !blockName.contains("core"))
			
				{
					if (!getAllBeBlocks().contains(block))
					{
				    	addAllSubBlocks(block);
						String name = block.getUnlocalizedName();
						if (name.contains("tile."))
				    	{
							name = name.substring(5);
				    	}
						writeToLog("found " + name + "!");
					}
					else
					{
						continue;
					}
				}
			}
		}
	}

	public static void printBlockNamesToFile()
	{
		try 
		{
			// if directory doesn't exist, create it
			if (!oreSheepAssets.exists())
			{
				if (!oreSheepAssets.mkdirs())
				{
					System.out.println("Failed to create directory! Contact oresheepmod author!");
				}
			}
			
			File file = new File(oreSheepAssets + fS + "PossibleBlocks.txt");
			
			if (!file.exists())
			{
				file.createNewFile();
			}
			
			String content = "allOres\n";
			Iterator blockIterator = Block.REGISTRY.iterator();
			String previousItName = "";
			while (blockIterator.hasNext())
			{
				Block block = (Block)blockIterator.next();
				if (isBlockLegal(block))
				{
					String unlocalizedName = block.getUnlocalizedName();
			    	String itName = unlocalizedName;
			    	if (unlocalizedName.contains("tile."))
			    	{
				    	itName = unlocalizedName.substring(5, unlocalizedName.length());
			    	}
			    	//prevents dupes of similar blocks
			    	if (!itName.equals(previousItName))
			    	{
					    content = content + itName + "\n";
			    	}
				    previousItName = itName;
				}
			}
			
			//write to the file
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	//deletes old textures to make space for new ones; resets for other values
	@SideOnly(Side.CLIENT)
	public static void deleteExistingTextures()
	{
		TextureManager manager = Minecraft.getMinecraft().getTextureManager();
		for (ResourceLocation i : allResources)
		{
			manager.deleteTexture(i);
		}
		for (int i = 0; i < beBlocks.size(); i++)
		{
			beBlocks.get(i).setBeBlockResource(null);
			beBlocks.get(i).setBaseResource(null);
		}
		allResources.clear();
		allSprites.clear();

		writeToLog("\nRefreshed textures due to texture reload.");
	}
	
	//creates resources for beBlocks at loc
	@SideOnly(Side.CLIENT)
	public static void drawOne(int loc)
	{
		BlockEntry blockEntry = getBeBlocks().get(loc);
		Block block = blockEntry.getBlock();;
		
		BufferedImage sheepBase = null;
		int bM = 1;
		try 
		{
			sheepBase = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft:textures/entity/sheep/sheep.png")).getInputStream());
			//multiplier for sheep base
			bM = sheepBase.getHeight() / 32;
			sheepBase = holyBufferedImage(sheepBase, bM);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		TextureAtlasSprite sprite;
		//these blocks use a different texture than the normal .getParticleTexture
		if (block == Blocks.GRASS)
		{
			 sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(block.getDefaultState()).getQuads(block.getDefaultState(), EnumFacing.NORTH, 0L).get(0).getSprite();
		}
		else if (block == Blocks.CAKE || block.getUnlocalizedName().toLowerCase().contains("cake"))
		{
			 sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(block.getDefaultState()).getQuads(block.getDefaultState(), null, 0L).get(0).getSprite();
		}
		else
		{
			sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(blockEntry.getState()).getParticleTexture();
		}
		String textureName = block.getUnlocalizedName();
		if (textureName.contains("tile."))
    	{
			textureName = textureName.substring(5);
    	}
		try
		{
			String out = "Getting texture for " + textureName + " with meta " + getBeBlocks().get(loc).getMeta() + ": ";
			//if the resource has already been made, set this block's texture to the existing texture
			if (allSprites.contains(sprite))
			{
				blockEntry.setBeBlockResource(allResources.get(allSprites.indexOf(sprite)));
				blockEntry.setBaseResource(allResources.get(allSprites.indexOf(sprite) + 1));
				out = out + "Already drawn. Success!";
			}
			else
			{
			//creates the new wool texture for the sheep
				BufferedImage img = getBufferedImage(sprite);
				//m is a multiplier for blocks that are higher resolution textures (width because some textures may be animated)
				int m = img.getWidth() / 16;
				BufferedImage bi = new BufferedImage(64 * m, 32 * m, BufferedImage.TYPE_INT_ARGB);						    
				Graphics2D ig2 = bi.createGraphics();
				ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
				//this is a fix of texture to make these special blocks look better than the default ones
				if (block == Blocks.CAKE || block.getUnlocalizedName().toLowerCase().contains("cake") || block.getUnlocalizedName().toLowerCase().contains("glass"))
				{
					Collections.addAll(coords, new Coordinate(-1,-1), new Coordinate(13,-1), new Coordinate(27,-1), new Coordinate(41,-1), new Coordinate(32,-1), new Coordinate(-1,13), new Coordinate(19,13), new Coordinate(13,13), new Coordinate(19,13), new Coordinate(19,17), new Coordinate(13,13), new Coordinate(33,17), new Coordinate(33,13), new Coordinate(47,13), new Coordinate(41,17));
				}
				else
				{
					Collections.addAll(coords, new Coordinate(0,0), new Coordinate(16,0), new Coordinate(25,0), new Coordinate(36,3), new Coordinate(44,14), new Coordinate(28,14), new Coordinate(0,16));
				}
				//used because tconstruct.ore is just an overlay of the netherrack texture
				if (textureName.equals("tconstruct.ore"))
				{
					TextureAtlasSprite netherrack = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(Blocks.NETHERRACK.getDefaultState()).getParticleTexture();
					BufferedImage netherrackImg = getBufferedImage(netherrack);
					bi = drawBufferedImage(bi, netherrackImg, ig2, m, coords);
				}
				bi = drawBufferedImage(bi, img, ig2, m, coords);
				DynamicTexture beTexture = new DynamicTexture(bi);
				ResourceLocation woolLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("ore_sheep_" + textureName, beTexture);
			//bases
				BufferedImage biBase = new BufferedImage(64 * bM, 32 * bM, BufferedImage.TYPE_INT_ARGB);	
				//if the block texture has transparent pixels, set the base to the normal sheep texture
				if (doesTextureHole(img))
				{
					biBase = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft:textures/entity/sheep/sheep.png")).getInputStream());;
				}
				else
				{
					BufferedImage imgBase = bi;
					Graphics2D oreSheep = biBase.createGraphics();
					oreSheep.drawImage(imgBase, null, 0, 0);
					oreSheep.drawImage(sheepBase, null, 0, 0);
				}
				DynamicTexture baseTexture = new DynamicTexture(biBase);
				ResourceLocation baseLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("base_" + textureName, baseTexture);
				
				blockEntry.setBeBlockResource(woolLocation);
				blockEntry.setBaseResource(baseLocation);
				//used to find textures that have already been made; sprite is added twice so that both ArrayLists have a 1:1 ratio
				allResources.add(woolLocation);
				allSprites.add(sprite);
				allResources.add(baseLocation);
				allSprites.add(sprite);
				if (sprite.getIconName().contains("missingno"))
				{
					out = out + "Failed! Contact oresheepmod author!";
				}
				else
				{
					out = out + "Success!";	
				}
			}
			writeToLog(out);
		}
		catch (IOException ie)
		{
			//blockEntry.setBeBlockResource(new ResourceLocation("missingno"));
			//blockEntry.setBaseResource(new ResourceLocation("minecraft:textures/entity/sheep/sheep.png"));
			ie.printStackTrace();
		}
	}
	
	//3.1.0
	public static int getNumSubBlocks(Block block) 
	{
		String blockName = block.getUnlocalizedName().toLowerCase();
		String domainName = block.getRegistryName().getResourceDomain();
		//all exeptions
		//forestry mod only
		if (blockName.contains("for.greenhouse"))
		{
			return 1;
		}
		//t-construct only
		else if (domainName.contains("tconstruct"))
		{
			if (blockName.contains("tconstruct.tooltables"))
			{
				return 6;
			}
			else if (blockName.contains("tconstruct.ore"))
			{
				return 2;
			}
			return normalGetNumBsubBlocks(block);
		}
		//mekanism mod only
		else if (domainName.contains("mekanism"))
		{
			if (blockName.contains("tile.energycube"))
			{
				return 1;
			}
			else if (blockName.contains("tile.basicblock2"))
			{
				return 10;
			}
			else if (blockName.contains("tile.machineblock3"))
			{
				return 2;
			}
			else if (blockName.contains("tile.transmitter"))
			{
				return 6;
			}
			return normalGetNumBsubBlocks(block);
		}
		//normal blocks (eveything else)
		else
		{
			return normalGetNumBsubBlocks(block);
		}
	}
	
	private static int normalGetNumBsubBlocks(Block block)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		block.getSubBlocks(null, list);
		return list.size();
	}
	
	public static void addAllSubBlocks(Block block)
	{
		//the metadata to begin with (default = 0)
		int begin = 0;
		//the metadata to end on + 1 (default = getNumSubBlocks(block))
		int end = getNumSubBlocks(block);
		String name = block.getUnlocalizedName().toLowerCase();
		//these blocks have a defective first metadata
		if (name.contains("tile.generator")/*mechanism mod*/ || name.equals("tile.for.bee_house") /*forestry mod*/)
		{
			begin = 1;
		}
		//removes wood stone slab
		else if (block instanceof BlockStoneSlab || block instanceof BlockDoubleStoneSlab)
		{
			for (int i = 0; i < end; i++)
			{
				if (i != 2)
				{
                	beBlocks.add(new BlockEntry(block.getStateFromMeta(i)));
				}
				else
				{
					end++;
				}
			}
			//doesn't reach while statement at end
			return;
		}
		//t-construct ore berries
		else if (name.contains("tile.ore.berries"))
		{
			if (name.contains("one"))
			{
				begin = 8;
				end = 12;
			}
			else
			{
				begin = 8;
				end = 10;
			}
		}
		while (begin < end)
		{
			beBlocks.add(new BlockEntry(block.getStateFromMeta(begin)));
			begin++;
		}
	}
	
	/**Returns a buffered image of "base with "add" drawn at the specified coordinates*/
	public static BufferedImage drawBufferedImage(BufferedImage base, BufferedImage add, Graphics2D graphics, int m, ArrayList<Coordinate> coords)
	{
		for (int i = 0; i < coords.size(); i++)
		{
			Coordinate coord = coords.get(i);
			graphics.drawImage(add, null, coord.getX() * m, coord.getY() * m);
		}
		return base;
	}

	public static BufferedImage holyBufferedImage(BufferedImage bufferedImage, int bM)
	{
		BufferedImage negative = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
		BufferedImage sheepBase = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
		try 
		{
			negative = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(ModOreSheep.modid + ":textures/entity/ore_sheep/ore_sheep_base_mask.png")).getInputStream());
			sheepBase = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft:textures/entity/sheep/sheep.png")).getInputStream());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		negative = enlarge(negative, bM);
		for (int y = 0; y < negative.getHeight(); y++)
		{
			for (int x = 0; x < negative.getWidth(); x++)
			{
				if (negative.getRaster().getSample(x, y, 3) == 255)
				{
					bufferedImage = setTransparentPixel(bufferedImage, x, y);
				}
			}
		 }
		return bufferedImage;
	}
	
	public static BufferedImage setTransparentPixel(BufferedImage bufferedImage, int x, int y)
	{
        bufferedImage.getRaster().setSample(x, y, 3, 0.0);
        return bufferedImage;
	}
	
	public static BufferedImage enlarge(BufferedImage image, int n) 
	{
		int w = n * image.getWidth();
	    int h = n * image.getHeight();
	    BufferedImage enlargedImage = new BufferedImage(w, h, image.getType());
	    for (int y = 0; y < h; ++y)
	    {
	    	for (int x = 0; x < w; ++x)
	    	{
	    		enlargedImage.setRGB(x, y, image.getRGB(x / n, y / n));
	    	}
	    }
	    return enlargedImage;
	}

	public static void writeConfigAndBlocksFile() 
	{
		createDirectories();
		printBlockNamesToFile();
	}
	
	//returns true if some of the pixels on the  image are clear, false otherwise
	public static boolean doesTextureHole(BufferedImage image)
	{
		if (image.getRaster().getNumBands() == 4)
		{
			for (int i = 0; i < image.getWidth(); i++)
			{
				for (int k = 0; k < image.getHeight(); k++)
				{
					if(image.getRaster().getSample(i, k, 3) == 0)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	 
	public static boolean isBlockLegal(Block block)
	{
		//exeptions
		String name = block.getUnlocalizedName().toLowerCase();
		if (block.getRenderType(block.getDefaultState()) != EnumBlockRenderType.MODEL)
		{
			return false;
		}
		else if (name.contains("null") || name.contains("bedrock") || name.contains("vine") || name.contains("sapling") || block instanceof BlockBush || block instanceof BlockLeaves || name.contains("leaves") || block instanceof BlockFire || block instanceof BlockRedstoneWire || block instanceof BlockWeb || block instanceof BlockCocoa || name.contains("mushroom") || name.contains("fluid") || name.contains("liquid")
		/*mod checks for specific blocks*/  )
		{
			return false;
		}
		/*forestry mod*/
		else if (name.contains("tile.for."))
		{
			if (name.contains("tile.for.ffarm") || name.contains("tile.for.database") || name.contains("tile.for.apiary") || name.contains("tile.for.database") || name.contains("for.greenhouse.window") || name.contains("for.fabricator") || name.contains("for.raintank") || name.contains("tile.for.mailbox") || name.contains("for.trade_station") || name.contains("for.stamp_collector") || name.contains("for.cocoon") || name.contains("for.worktable"))
			{
				return false;
			}
			return true;
		}
		return true;
	}
	
	//TODO wooden slabs?
    public static boolean isSlab(Block block)
    {
    	if (block.getUnlocalizedName().toLowerCase().contains("slab") && block != Blocks.WOODEN_SLAB && block != Blocks.DOUBLE_WOODEN_SLAB)
    	{
    		return true;
    	}
    	return false;
    }
	
	//thanks mezz for the code! (Found in JustEnoughItems mod)
	private static BufferedImage getBufferedImage(TextureAtlasSprite textureAtlasSprite)
	{
		final int iconWidth = textureAtlasSprite.getIconWidth();
		final int iconHeight = textureAtlasSprite.getIconHeight();
		final int frameCount = textureAtlasSprite.getFrameCount();
		if (iconWidth <= 0 || iconHeight <= 0 || frameCount <= 0)
		{
			return null;
		}
		BufferedImage bufferedImage = new BufferedImage(iconWidth, iconHeight * frameCount, BufferedImage.TYPE_4BYTE_ABGR);
		for (int i = 0; i < frameCount; i++)
		{
			int[][] frameTextureData = textureAtlasSprite.getFrameTextureData(i);
			int[] largestMipMapTextureData = frameTextureData[0];
			bufferedImage.setRGB(0, i * iconHeight, iconWidth, iconHeight, largestMipMapTextureData, 0, iconWidth);
		}

		return bufferedImage;
	}


}

package oresheepmod;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityOreSheep extends EntityAnimal
{
	private static final DataParameter<Integer> ORE_INT = EntityDataManager.<Integer>createKey(EntitySheep.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> IS_SHEARED = EntityDataManager.<Boolean>createKey(EntitySheep.class, DataSerializers.BOOLEAN);
    /**
     * Used to control movement as well as wool regrowth. Set to 40 on handleHealthUpdate and counts down with each
     * tick.
     */
    private int sheepTimer;
    private EntityAIEatBlock eatBlock = new EntityAIEatBlock(this);

    public ArrayList<Block> stones = createArrayList();    
    private int meta;

    public EntityOreSheep(World worldIn)
    {
        super(worldIn);
        this.setSize(0.9F, 1.3F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITemptPlus(this, 1.1D, stones, false, false));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, this.eatBlock);
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
    }
    
    protected void updateAITasks()
    {
        this.sheepTimer = this.eatBlock.getEatingGrassTimer();
        super.updateAITasks();
    }
    
    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
    	super.onLivingUpdate();
        if (this.world.isRemote)
        {
            this.sheepTimer = Math.max(0, this.sheepTimer - 1);
        }
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
    }
    
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(ORE_INT, Integer.valueOf((int)0));
        this.dataManager.register(IS_SHEARED, false);
    }
    
    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LootTableList.ENTITIES_SHEEP;
    }
    
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id)
    {
        if (id == 10)
        {
            this.sheepTimer = 40;
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public float getHeadRotationPointY(float p_70894_1_)
    {
        return this.sheepTimer <= 0 ? 0.0F : (this.sheepTimer >= 4 && this.sheepTimer <= 36 ? 1.0F : (this.sheepTimer < 4 ? ((float)this.sheepTimer - p_70894_1_) / 4.0F : -((float)(this.sheepTimer - 40) - p_70894_1_) / 4.0F));
    }

    @SideOnly(Side.CLIENT)
    public float getHeadRotationAngleX(float p_70890_1_)
    {
        if (this.sheepTimer > 4 && this.sheepTimer <= 36)
        {
            float f = ((float)(this.sheepTimer - 4) - p_70890_1_) / 32.0F;
            return ((float)Math.PI / 5F) + ((float)Math.PI * 7F / 100F) * MathHelper.sin(f * 28.7F);
        }
        else
        {
            return this.sheepTimer > 0 ? ((float)Math.PI / 5F) : this.rotationPitch * 0.017453292F;
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        super.writeEntityToNBT(p_70014_1_);
        p_70014_1_.setBoolean("Sheared", this.getSheared());
        p_70014_1_.setInteger("Ore", (this.getOreBlock().getIdFromBlock(this.getOreBlock()) * 100) + meta);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
	    super.readEntityFromNBT(p_70037_1_);
	    this.setSheared(p_70037_1_.getBoolean("Sheared"));
	    boolean exists = false;
	    Block block = Block.getBlockById(p_70037_1_.getInteger("Ore")/ 100);
	    byte met = (byte) (p_70037_1_.getInteger("Ore") % 100);
	    //if the beBlock id exists, set the ore integer to the index of that ore, otherwise, set it to stone
	    for (int i = 0; i < OreRegistryDraw.getBeBlocks().size(); i++)
	    {
	    	BlockEntry k = OreRegistryDraw.getBeBlocks().get(i);
	    	if (block.equals(k.getState().getBlock()) && met == k.getMeta())
	        {
	            this.setOreInteger(i);
	            this.setMetadata(met);
	            exists = true;
	            break;
	        }
	    }
	    if (!exists)
	    {
	    	this.setOreInteger(0);
	    }
	}
	
	protected SoundEvent getAmbientSound()
	{
	    return SoundEvents.ENTITY_SHEEP_AMBIENT;
	}
	
	protected SoundEvent getHurtSound(DamageSource p_184601_1_)
	{
	    return SoundEvents.ENTITY_SHEEP_HURT;
	}
	
	protected SoundEvent getDeathSound()
	{
	    return SoundEvents.ENTITY_SHEEP_DEATH;
	}
	
	protected void playStepSound(BlockPos pos, Block blockIn)
	{
	    this.playSound(SoundEvents.ENTITY_SHEEP_STEP, 0.15F, 1.0F);
	}
	
	/**
	 * returns true if a sheeps wool has been sheared
	 */
	public boolean getSheared()
	{
	    return this.dataManager.get(IS_SHEARED);
	}
	    
	    /**
	     * make a sheep sheared if set to true
	     */
	public void setSheared(boolean sheared)
	{
		boolean bol = this.dataManager.get(IS_SHEARED);
		
	    if (sheared)
	    {
	        this.dataManager.set(IS_SHEARED, true);
	    }
	    else
	    {
	    	this.dataManager.set(IS_SHEARED, false);
	    }
	}
	
	public EntityOreSheep createChild(EntityAgeable p_90011_1_)
	{
	  EntityOreSheep otherparent = (EntityOreSheep)p_90011_1_;
	  EntityOreSheep entityoresheep1 = new EntityOreSheep(this.world);
	  int i = getOreBetweenParents(this, otherparent);
	  entityoresheep1.setOreInteger(i);
	  return entityoresheep1;
	}
	
	/**
	 * This function applies the benefits of growing back wool and faster growing up to the acting entity. (This
	 * function is used in the AIEatBlock)
	 */
	public void eatBlockBonus()
	{
	    this.setSheared(false);
	
	    if (this.isChild())
	    {
	        this.addGrowth(60);
	    }
	}
	
	/**
	 * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
	 * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
	 */
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
	{
	    livingdata = super.onInitialSpawn(difficulty, livingdata);
	    //TODO change to fix mob spawner exploit //this.setOreInteger(0);
	    int i = this.world.rand.nextInt(OreRegistryDraw.getBeBlocks().size());
	    this.setOreInteger(i);
	    return livingdata;
	}
    
  //if parent ores are identical, returns one of their ores, otherwise, half chance of getting ore from either parent
    private int getOreBetweenParents(EntityAnimal p_90014_1_, EntityAnimal p_90014_2_)
    {
    	EntityOreSheep parent = (EntityOreSheep)p_90014_1_;
    	EntityOreSheep otherparent = (EntityOreSheep)p_90014_2_;
        int k = parent.getOreInt();
        if (parent.getOreInt() != otherparent.getOreInt())
        {
            if(Math.random()*2 >= 1)
            {
            	k = parent.getOreInt();
            }
            else
            {
            	k = otherparent.getOreInt();
            }
        }
        return k;
    }
    
    public float getEyeHeight()
    {
        return 0.95F * this.height;
    }
    
    public Block getOreBlock()
    {
    	return getOreBlockEntry().getBlock();
    }
    
    public IBlockState getOreState()
    {
    	return getOreBlockEntry().getState();
    }
    
    public int getOreMeta()
    {
    	return getOreBlockEntry().getMeta();
    }
    
    public BlockEntry getOreBlockEntry()
    {
        return OreRegistryDraw.getBeBlocks().get(getOreInt());
    }
    
    public ArrayList<Block> createArrayList()
    {
    	ArrayList<Block> list = new ArrayList<Block>(); 
    	  for (BlockEntry k : OreRegistryDraw.getBeBlocks()) 
    	  {
    	    list.add(k.getState().getBlock());
    	  }
    	  for (Block i : OreRegistryDraw.getEatBlocks()) 
    	  {
    	    list.add(i);
    	  }
    	  return list;
    }
    
    public int getMetadata()
    {
    	return this.meta;
    }

    public int getOreInt()
    {
    	return this.dataManager.get(ORE_INT);
    }

    public void setOreInteger(int oreInt)
    {
        this.dataManager.set(ORE_INT, Integer.valueOf(oreInt));
    	if (getOreInt() >= OreRegistryDraw.getBeBlocks().size() || getOreInt() < 0)
    	{
    		this.dataManager.set(ORE_INT, 0);
    	}
        setMetadata(getOreBlockEntry().getMeta());
    }
    
    public void setMetadata(int meta2)
    {
        this.meta = meta2;
    }

    /** possible ways to get the correct block to drop:
     * getItemFromBlock,
     * getItem,
     * getItemDropped
     */
    protected Item getDropItem()
    {
    	//if tileentity and item to return is still null or air, tries other block drop, otherwise returns
    	Item itemToReturn = null;
    	if (getOreBlock().hasTileEntity(getOreBlockEntry().getState()))
    	{
    		itemToReturn = new ItemStack(getOreBlock(), 1, getOreBlock().getMetaFromState(getOreState())).getItem();
    		//flower pot fix
    		if (itemToReturn == null || itemToReturn == Item.getItemFromBlock(Blocks.AIR))
    		{
    			return getOreBlock().getItemDropped(getOreState(), rand, 0);
    		}
    		return itemToReturn;
    	}
    	else
    	{
			//TODO three places to find this code (OreSheepEventHandler- creating change, OreRegistryDraw- getAllItemsFromBlocks, and EntityOreSheep- getDropItem :merge these!
    		Item item = getOreBlock().getItem(this.world, null, getOreState()).getItem();
            if (item != null)
            {
            	return item;
          	}
            else
            {
                item = Item.getItemFromBlock(getOreBlock());
                if (item != null)
                {
                	return item;
                }
                else
                {
                  	item = getOreBlock().getItemDropped(getOreState(), rand, 0);
               		return item;
            	}
        	}
    	}
    }

    public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune)
    {
        this.setSheared(true);
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
    	ret.add(new ItemStack(this.getDropItem(), 1, this.getOreMeta()));
        //plays the shear sound and the break sound for the sheep's block
        this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
        this.playSound(this.getOreBlock().getSoundType().getBreakSound(), 1.0F, 1.0F);
        return ret;
    }
    
    /**
     * Checks if the parameter is an item which this animal can be fed to breed it
     */
    public boolean isBreedingItem(ItemStack stack)
    {
        int i = 0;
        while (stack != null && i < stones.size())
        {
        	if (stack.getItem() == Item.getItemFromBlock(stones.get(i)))
        	{
        		return true;
        	}
        	i++;
        }
        
        return false;
    }
}
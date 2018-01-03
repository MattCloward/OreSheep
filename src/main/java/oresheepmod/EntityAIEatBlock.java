package oresheepmod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIEatBlock extends EntityAIBase
{
    private EntityOreSheep blockEaterEntity;
    private World entityWorld;
    int eatingBlockTimer;

    public EntityAIEatBlock(EntityOreSheep blockEaterEntityIn)
    {
        this.blockEaterEntity = blockEaterEntityIn;
        this.entityWorld = blockEaterEntityIn.world;
        this.setMutexBits(7);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (this.blockEaterEntity.getRNG().nextInt(this.blockEaterEntity.isChild() ? 50 : 1000) != 0)
        {
            return false;
        }
        else
        {
            if (this.entityWorld.getGameRules().getBoolean("mobGriefing") || (!this.entityWorld.getGameRules().getBoolean("mobGriefing") && ModConfig.IgnoreGameRuleMobGriefing))
            {
            	int x = MathHelper.floor(this.blockEaterEntity.posX);
                int y = MathHelper.floor(this.blockEaterEntity.posY);
                int z = MathHelper.floor(this.blockEaterEntity.posZ);
                BlockPos posThis = new BlockPos(x, y, z);
                BlockPos posDownOne = new BlockPos(x, y-1, z);
                IBlockState stateThis = this.entityWorld.getBlockState(posThis);
                IBlockState stateDownOne = this.entityWorld.getBlockState(posDownOne);
                Block blockThis = stateThis.getBlock();
                Block blockDownOne = stateDownOne.getBlock();
                byte blockMetaThis = getMetaForBlock(stateThis, posThis);
                byte blockMetaDownOne = this.getMetaForBlock(stateDownOne, posDownOne);
                //if the block can be eaten and isn't what this sheep is, return true
                if (canBlockBeEaten(this.entityWorld.getBlockState(posThis), blockMetaThis) || (canBlockBeEaten(this.entityWorld.getBlockState(posDownOne), blockMetaDownOne)))
                {
                	return true;
                }
            }
        }
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.eatingBlockTimer = 40;
        this.entityWorld.setEntityState(this.blockEaterEntity, (byte)10);
        this.blockEaterEntity.getNavigator().clearPath();
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.eatingBlockTimer = 0;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     * true if counter is greater than 0 and the block under or in it is a block to eat
     */
    public boolean shouldContinueExecuting()
    {
    	return this.eatingBlockTimer > 0;
    }

    public int getEatingGrassTimer()
    {
        return this.eatingBlockTimer;
    }
    
    private byte getMetaForBlock(IBlockState state, BlockPos pos)
    {
    	if (OreRegistryDraw.getNumSubBlocks(state.getBlock()) == 1)
        	return 0;
    	//because slabs use meta for both different sub blocks and for location, they must be % by 8 to get meta equivalent to type
    	else if (OreRegistryDraw.isSlab(state.getBlock()))
    	{
    		return (byte) (state.getBlock().getMetaFromState(this.entityWorld.getBlockState(pos)) % 8);
    	}
        else
            return (byte) state.getBlock().getMetaFromState(this.entityWorld.getBlockState(pos));
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        this.eatingBlockTimer = Math.max(0, this.eatingBlockTimer - 1);
        if (this.eatingBlockTimer == 4)
        {
            int x = MathHelper.floor(this.blockEaterEntity.posX);
            int y = MathHelper.floor(this.blockEaterEntity.posY);
            int z = MathHelper.floor(this.blockEaterEntity.posZ);
            BlockPos posThis = new BlockPos(x, y, z);
            BlockPos posDownOne = new BlockPos(x, y-1, z);
            IBlockState stateThis = this.entityWorld.getBlockState(posThis);
            IBlockState stateDownOne = this.entityWorld.getBlockState(posDownOne);
            Block blockThis = stateThis.getBlock();
            Block blockDownOne = stateDownOne.getBlock();
            byte blockMetaThis = getMetaForBlock(stateThis, posThis);
            byte blockMetaDownOne = this.getMetaForBlock(stateDownOne, posDownOne);
            //if 1, blockThis, if 2, blockDownOne
            int blockEaten = 0;
            if(this.blockEaterEntity.stones.contains(blockThis) || this.blockEaterEntity.stones.contains(blockDownOne))
            {
                if (this.blockEaterEntity.stones.contains(blockThis))
                {
                	//not included in if statemente above so that  the else statement below isn't confused (if sheep is slab and slab eaten in blockThis position caused else statement to be triggered, making block below air - THIS IS BAD!) 
            		if (this.blockEaterEntity.getOreBlock() != blockThis)
            		{
                		this.changeBlockEatenTo(Blocks.AIR, posThis);
                        blockEaten = 1;
            		}
            		else
            		{
            			//if the sheep is a slab sheep, and it's meta isn't the meta of the slab below, do eat
            			if (OreRegistryDraw.isSlab(blockThis) && this.blockEaterEntity.getMetadata() != blockMetaThis)
            			{
                			this.changeBlockEatenTo(Blocks.AIR, posThis);
                            blockEaten = 1;
            			}
            		}
                }
                else
                {
                	blockEaten = 2;
                    if (blockDownOne == Blocks.STONE)
                    {
                    	this.changeBlockEatenTo(Blocks.COBBLESTONE, posDownOne);
                    }
                    else if (blockDownOne == Blocks.QUARTZ_ORE)
                	{
                    	this.changeBlockEatenTo(Blocks.NETHERRACK, posDownOne);
                	}
                    else if (blockDownOne == Blocks.GRAVEL || blockDownOne == Blocks.NETHERRACK)
                    {
                    	this.changeBlockEatenTo(Blocks.AIR, posDownOne);
                    }
                    else if (blockDownOne.getUnlocalizedName().toLowerCase().contains("ore"))
         			{
                    	if (blockDownOne.getUnlocalizedName().toLowerCase().contains("nether") || blockDownOne.getUnlocalizedName().toLowerCase().contains("tconstruct.ore"))
                    	{
                    		this.changeBlockEatenTo(Blocks.NETHERRACK, posDownOne);
                    	}
                    	else if (blockDownOne.getUnlocalizedName().toLowerCase().contains("end"))
                    	{
                    		this.changeBlockEatenTo(Blocks.END_STONE, posDownOne);
                    	}
                    	else
                    	{
                    		this.changeBlockEatenTo(Blocks.STONE, posDownOne);
                    	}
               		}
                    else if (blockDownOne == Blocks.STONE_BRICK_STAIRS)
                    {
                		this.changeBlockEatenTo(Blocks.STONE_STAIRS, posDownOne);
                    }
                    else if (blockDownOne == Blocks.COBBLESTONE || blockDownOne == Blocks.MOSSY_COBBLESTONE || blockDownOne == Blocks.STONE_STAIRS || blockDownOne == Blocks.COBBLESTONE_WALL)
                    {
                		this.changeBlockEatenTo(Blocks.GRAVEL, posDownOne);
                    }
                    else
                    {
                    	this.changeBlockEatenTo(Blocks.AIR, posDownOne);
                    }
                }
            	
            	this.blockEaterEntity.eatBlockBonus();
                
                //notice that if the block is stone or netherrack, the sheep doesn't change
                //sets the ore integer of a sheep to reflect the ore it just ate
                if (blockEaten == 1 || (blockEaten == 2 && blockDownOne != Blocks.STONE && blockDownOne != Blocks.NETHERRACK))
                {
                	for (int l = 0; l < OreRegistryDraw.getBeBlocks().size(); l++)
                	{
                		BlockEntry k = OreRegistryDraw.getBeBlocks().get(l);
                		if (blockEaten == 1 && k.getBlock().equals(blockThis))
                		{
                			this.blockEaterEntity.setOreInteger(l + blockMetaThis);
                			this.blockEaterEntity.setMetadata(blockMetaThis);
                            break;
                		}
                		//isSlab check is for double slab blocks (causes the sheep to become that block)
                		else if (blockEaten == 2 && (k.getBlock().equals(blockDownOne) || (OreRegistryDraw.isSlab(blockDownOne) && OreRegistryDraw.isSlab(k.getState().getBlock()))))
                		{
                            this.blockEaterEntity.setOreInteger(l + blockMetaDownOne);
                            this.blockEaterEntity.setMetadata(blockMetaDownOne);
                            break;
                		}
                	}
                }	           
            }
        }
    }
    
    private boolean canBlockBeEaten(IBlockState state, int meta)
    {
    	Block block = state.getBlock();
    	if (this.blockEaterEntity.stones.contains(block))
    	{
    		if (this.blockEaterEntity.getOreBlock().equals(Blocks.STONE) || this.blockEaterEntity.getOreBlock().equals(Blocks.NETHERRACK) || block != this.blockEaterEntity.getOreBlock())
    		{
    			return true;
    		}
    		//slab sheep don't eat slabs of same type, even at different heights
    		else if (OreRegistryDraw.isSlab(state.getBlock()) && this.blockEaterEntity.getMetadata() != meta)
    		{
    			return true;
    		}
    		else if (block == this.blockEaterEntity.getOreBlock() && meta != this.blockEaterEntity.getMetadata())
    		{
    			return true;
    		}
    	}
		 return false;    
    }
    
    private void changeBlockEatenTo(Block newBlock, BlockPos pos)
    {
    	changeBlockEatenTo(newBlock.getDefaultState(), pos);
    }
    
    private void changeBlockEatenTo(IBlockState newState, BlockPos pos)
    {
    	if (newState.getBlock() == Blocks.AIR)
    	{
    		this.entityWorld.destroyBlock(pos, false);
    	}
    	else
    	{
    		IBlockState iblockstate = this.entityWorld.getBlockState(pos);
    		//plays the break block sound and spawns the breaking block particle effects 
    		this.entityWorld.playEvent(2001, pos, Block.getStateId(iblockstate));
    		//changes the block to the newBlock at pos
    		this.entityWorld.setBlockState(pos, newState, 3);
    	}
    }
}

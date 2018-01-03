package oresheepmod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class BlockEntry 
{
	private IBlockState state;
	private ResourceLocation beBlockResource;
	private ResourceLocation baseResource;
	
	public BlockEntry(IBlockState state)
	{
		this.state = state;
	}
	
	public BlockEntry(IBlockState state, ResourceLocation beBlock, ResourceLocation base)
	{
		this.state = state;
		beBlockResource = beBlock;
		baseResource = base;
	}
	
	public IBlockState getState()
	{
		return state;
	}
	
	public Block getBlock()
	{
		return getState().getBlock();
	}
	
	public int getMeta()
	{
		//for blocks that can't have a larger metadata (blocks with meta that represent direction fall into this category).
		if (OreRegistryDraw.getNumSubBlocks(this.getBlock()) == 1)
		{
			return 0;
		}
		//slabs meta is calculate taking the state meta and % by the number of different sub blocks
		else if (OreRegistryDraw.isSlab(getBlock()))
		{
			return this.getBlock().getMetaFromState(state) % 8;
		}
		return this.getBlock().getMetaFromState(state);
	}
	
	public ResourceLocation getBeBlockResource()
	{
		return beBlockResource;
	}
	
	public ResourceLocation getBaseResource()
	{
		return baseResource;
	}
	
	public void setBeBlockResource(ResourceLocation beBlockResource)
	{
		this.beBlockResource = beBlockResource;
	}
	
	public void setBaseResource(ResourceLocation baseResource)
	{
		this.baseResource = baseResource;
	}
	
	public void setResources(ResourceLocation beBlockResource, ResourceLocation baseResource)
	{
		setBeBlockResource(beBlockResource);
		setBaseResource(baseResource);
	}
	
	public String toString()
	{
		return getBlock() + " " + getMeta() + " " + beBlockResource + " " + baseResource; 
	}
}

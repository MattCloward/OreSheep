package oresheepmod;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderOreSheep extends RenderLiving
{
	//No hard-coded textures!  Isn't that amazing?!
	
	public RenderOreSheep(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn)
    {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
        this.addLayer(new LayerOreSheepWool(this));
    }                  
    
    public ResourceLocation getBoundTexture(EntityOreSheep entityOreSheep)
    {
    	return entityOreSheep.getOreBlockEntry().getBeBlockResource();
    }

    /**
     * Queries whether should render the specified pass or not.
	*/
    protected int shouldRenderPass(EntityOreSheep entityOreSheep, int p_77032_2_, float p_77032_3_)
    {
        if (!entityOreSheep.getSheared())
        {
            this.bindTexture(getBoundTexture(entityOreSheep));
            return 1;
        }
        else
        {
            return -1;
        }
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityOreSheep entityOreSheep)
    {
    	//TODO bad code: onLogOutClient (see OreSheepEventHandler), canRefreshTextures is set to true and the next sheep 
    	//looked at deletes old textures to prepare for new ones (done this way because in event handler, there is no openGL)
    	if (OreRegistryDraw.canRefreshTextures)
    	{
    		OreRegistryDraw.deleteExistingTextures();
    	}
    	//draws new texture if it does not exist
    	if (OreRegistryDraw.getBeBlocks().get(entityOreSheep.getOreInt()).getBaseResource() == null)
    	{
    		OreRegistryDraw.drawOne(entityOreSheep.getOreInt());
    		OreRegistryDraw.canRefreshTextures = false;
    	}
    	return entityOreSheep.getOreBlockEntry().getBaseResource();
    }

    /**
     * Queries whether should render the specified pass or not.
	*/
    protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_)
    {
        return this.shouldRenderPass((EntityOreSheep)p_77032_1_, p_77032_2_, p_77032_3_);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	*/
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return this.getEntityTexture((EntityOreSheep)entity);
    }
    
    public static final Factory FACTORY = new Factory();

    public static class Factory implements IRenderFactory<EntityOreSheep> 
    {
        public Render<? super EntityOreSheep> createRenderFor(RenderManager manager)
        {
            return new RenderOreSheep(manager, new ModelOreSheep2(), 0.7F);
        }
    }
}

package oresheepmod;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Items;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerOreSheepWool implements LayerRenderer<EntityOreSheep>
{
    private final RenderOreSheep oreSheepRenderer;
    private final ModelOreSheep1 oreSheepModel = new ModelOreSheep1();

    public LayerOreSheepWool(RenderOreSheep renderOreSheep)
    {
        this.oreSheepRenderer = renderOreSheep;
    }

    public void doRenderLayer(EntityOreSheep entityoresheep, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (!entityoresheep.getSheared() && !entityoresheep.isInvisible())
        {
            if (entityoresheep.hasCustomName() && "Icedice9".equals(entityoresheep.getCustomNameTag()))
			{
            	int i = entityoresheep.ticksExisted / 10 + entityoresheep.getEntityId();
            	int j = OreRegistryDraw.getBeBlocks().size();
            	int k = i % j;
            	if (OreRegistryDraw.getBeBlocks().get(k).getBeBlockResource() == null)
            	{
            		OreRegistryDraw.drawOne(k);
            	}
            	//draws new texture if it does not exist
            	if (OreRegistryDraw.getBeBlocks().get(entityoresheep.getOreInt()).getBeBlockResource() == null)
            	{
            		OreRegistryDraw.drawOne(entityoresheep.getOreInt());
            	}
                this.oreSheepRenderer.bindTexture(OreRegistryDraw.getBeBlocks().get(k).getBeBlockResource());
			}
            else
            {
            	//draws new texture if it does not exist
            	if (OreRegistryDraw.getBeBlocks().get(entityoresheep.getOreInt()).getBeBlockResource() == null)
            	{
            		OreRegistryDraw.drawOne(entityoresheep.getOreInt());
            	}
                this.oreSheepRenderer.bindTexture(entityoresheep.getOreBlockEntry().getBeBlockResource());
            }
            this.oreSheepModel.setModelAttributes(this.oreSheepRenderer.getMainModel());
            this.oreSheepModel.setLivingAnimations(entityoresheep, limbSwing, limbSwingAmount, partialTicks);
            this.oreSheepModel.render(entityoresheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    public boolean shouldCombineTextures()
    {
        return true;
    }
}

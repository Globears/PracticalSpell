package com.glo.practicalspell.client;

import com.glo.practicalspell.entity.MagicBolt;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MagicBoltRenderer extends EntityRenderer<MagicBolt> {

    public MagicBoltRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(MagicBolt entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
    }

    @Override
    public ResourceLocation getTextureLocation(MagicBolt entity) {
        return null;
    }
}

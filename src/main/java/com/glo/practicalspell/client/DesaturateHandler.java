package com.glo.practicalspell.client;

import com.glo.practicalspell.Practicalspell;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.io.IOException;

public class DesaturateHandler {

    private static boolean active;
    private static PostChain postChain;

    static {
        NeoForge.EVENT_BUS.register(new DesaturateHandler());
    }

    public static void toggle() {
        active = !active;
        if (!active && postChain != null) {
            postChain.close();
            postChain = null;
        }
    }

    public static boolean isActive() {
        return active;
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (!active || event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        Minecraft mc = Minecraft.getInstance();
        if (postChain == null) {
            try {
                postChain = new PostChain(
                        mc.getTextureManager(),
                        mc.getResourceManager(),
                        mc.getMainRenderTarget(),
                        ResourceLocation.fromNamespaceAndPath(Practicalspell.MODID, "shaders/post/desaturate.json")
                );
            } catch (IOException e) {
                Practicalspell.LOGGER.warn("Failed to load desaturate shader", e);
                return;
            }
        }
        postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        postChain.process(event.getPartialTick().getGameTimeDeltaTicks());
        mc.getMainRenderTarget().bindWrite(true);
    }
}

package com.glo.practicalspell.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientTrailRenderer {

    private static final double TRAIL_DISTANCE = 5.0;
    private boolean drawing;

    public ClientTrailRenderer() {
        NeoForge.EVENT_BUS.register(this);
    }

    public void setDrawing(boolean drawing) {
        this.drawing = drawing;
    }

    @SubscribeEvent
    public void onFrame(RenderFrameEvent.Post event) {
        if (!drawing) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 look = player.getViewVector(1.0f);
        Vec3 target = eye.add(look.scale(TRAIL_DISTANCE));

        Vec3 vel = player.getDeltaMovement();

        player.clientLevel.addParticle(
                new DustColorTransitionOptions(new Vector3f(1f, 0.2f, 0f), new Vector3f(1f, 0.8f, 0f), 1.5f),
                target.x, target.y, target.z,
                vel.x, vel.y, vel.z
        );
    }
}

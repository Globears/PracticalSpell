package com.glo.practicalspell.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ClientTrailRecorder {

    public record TrailPoint(double dYaw, double dPitch) {
        
    }

    private static final int MAX_POINTS = 5000;

    private boolean drawing;
    private Float startYaw;
    private Float startPitch;
    private final List<TrailPoint> points = new ArrayList<>();

    public ClientTrailRecorder() {
        NeoForge.EVENT_BUS.register(this);
    }

    public void start() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        drawing = true;
        startYaw = player.getYRot();
        startPitch = player.getXRot();
        points.clear();
    }

    public List<TrailPoint> stop() {
        drawing = false;
        var snapshot = List.copyOf(points);
        points.clear();
        startYaw = null;
        startPitch = null;
        return snapshot;
    }

    public boolean isDrawing() {
        return drawing;
    }

    @SubscribeEvent
    public void onFrame(RenderFrameEvent.Post event) {
        if (!drawing) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        float dYaw = normalizeYaw(player.getYRot() - startYaw);
        float dPitch = player.getXRot() - startPitch;

        points.add(new TrailPoint(dYaw, dPitch));
        if (points.size() > MAX_POINTS) {
            points.removeFirst();
        }
    }

    private static float normalizeYaw(float yaw) {
        return ((yaw + 180) % 360 + 360) % 360 - 180;
    }
}

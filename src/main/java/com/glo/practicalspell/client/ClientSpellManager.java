package com.glo.practicalspell.client;

import com.glo.practicalspell.CastSpellPayload;
import com.glo.practicalspell.recognizer.DollarRecognizer;
import com.glo.practicalspell.recognizer.GesturesLoader;
import com.glo.practicalspell.recognizer.Point;
import com.glo.practicalspell.recognizer.Result;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ClientSpellManager {

    private final KeyMapping drawKey;
    private final ClientTrailRecorder recorder;
    private final ClientTrailRenderer renderer;
    private final DollarRecognizer recognizer;

    public ClientSpellManager(KeyMapping drawKey) {
        this.drawKey = drawKey;

        this.recognizer = new DollarRecognizer();
        GesturesLoader.load(recognizer);

        this.recorder = new ClientTrailRecorder();
        this.renderer = new ClientTrailRenderer();

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Post event) {
        boolean down = drawKey.isDown();
        if (down && !recorder.isDrawing()) {
            recorder.start();
            renderer.setDrawing(true);;
        } else if (!down && recorder.isDrawing()) {
            renderer.setDrawing(false);;
            handleStroke(recorder.stop());
        }
    }
    
    private static final int QUICK_CAST_THRESHOLD = 20;

    private void handleStroke(List<TrailPoint> trailPoints) {
        if (trailPoints.isEmpty()) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        if (trailPoints.size() < QUICK_CAST_THRESHOLD) {
            // Short press: quick cast triangle
            player.displayClientMessage(
                    Component.literal("Quick cast: triangle"),
                    true
            );
            PacketDistributor.sendToServer(new CastSpellPayload("triangle"));
            return;
        }

        // Long press: gesture recognition
        List<Point> pts = new ArrayList<>(trailPoints.size());
        for (TrailPoint tp : trailPoints) {
            pts.add(new Point(tp.dYaw(), tp.dPitch()));
        }

        List<Result> results = recognizer.recognize(pts);
        if (results.isEmpty()) return;

        Result best = results.get(0);

        if ("zig-zag".equals(best.name())) {
            DesaturateHandler.toggle();
            player.displayClientMessage(
                    Component.literal(DesaturateHandler.isActive() ? "Ether: ON" : "Ether: OFF"),
                    true
            );
            return;
        }

        player.displayClientMessage(
                Component.literal("Spell: " + best.name() + "  score: " + String.format("%.3f", best.score())),
                true
        );

        PacketDistributor.sendToServer(new CastSpellPayload(best.name()));
    }
}

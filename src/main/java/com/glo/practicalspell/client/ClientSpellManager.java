package com.glo.practicalspell.client;

import com.glo.practicalspell.Practicalspell;
import com.glo.practicalspell.client.ClientTrailRecorder.TrailPoint;
import com.glo.practicalspell.network.CastSpellPayload;

import net.neoforged.api.distmarker.Dist;

import com.glo.practicalspell.recognizer.DollarRecognizer;
import com.glo.practicalspell.recognizer.GesturesLoader;
import com.glo.practicalspell.recognizer.Point;
import com.glo.practicalspell.recognizer.Result;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Practicalspell.MODID, value = Dist.CLIENT)
public class ClientSpellManager {

    private static ClientTrailRecorder recorder = new ClientTrailRecorder();
    private static ClientTrailRenderer renderer = new ClientTrailRenderer();
    private static DollarRecognizer recognizer = new DollarRecognizer();

    @SubscribeEvent
    public static void onSetup(FMLClientSetupEvent event){
        GesturesLoader.load(recognizer);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {

        boolean down = KeyMappings.conjureKey.isDown();
        if (down && !recorder.isDrawing()) {
            recorder.start();
            renderer.setDrawing(true);
        } else if (!down && recorder.isDrawing()) {
            renderer.setDrawing(false);
            handleStroke(recorder.stop());
        }
    }

    private static final int QUICK_CAST_THRESHOLD = 20;

    private static void handleStroke(List<TrailPoint> trailPoints) {
        if (trailPoints.isEmpty()) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        if (trailPoints.size() < QUICK_CAST_THRESHOLD) {
            // 短按：快速施放三角形
            player.displayClientMessage(
                    Component.literal("Quick cast: triangle"),
                    true
            );
            PacketDistributor.sendToServer(new CastSpellPayload("triangle"));
            return;
        }

        // 长按：手势识别
        List<Point> pts = new ArrayList<>(trailPoints.size());
        for (TrailPoint tp : trailPoints) {
            pts.add(new Point(tp.dYaw(), tp.dPitch()));
        }

        List<Result> results = recognizer.recognize(pts);
        if (results.isEmpty()) return;

        Result best = results.get(0);

        player.displayClientMessage(
                Component.literal("Spell: " + best.name() + "  score: " + String.format("%.3f", best.score())),
                true
        );

        PacketDistributor.sendToServer(new CastSpellPayload(best.name()));
    }
}
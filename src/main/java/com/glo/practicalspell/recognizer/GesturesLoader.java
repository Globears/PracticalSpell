package com.glo.practicalspell.recognizer;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GesturesLoader {

    private static final String GESTURES_PATH = "/assets/practicalspell/gestures/gestures.json";

    // Gson DTOs: mutable so Gson can set fields directly
    private static class GestureEntry {
        String name;
        List<PointEntry> points;
    }

    private static class PointEntry {
        @SerializedName("x")
        double x;
        @SerializedName("y")
        double y;
    }

    public static void load(DollarRecognizer recognizer) {
        InputStream stream = GesturesLoader.class.getResourceAsStream(GESTURES_PATH);
        if (stream == null) {
            System.err.println("[GesturesLoader] Template file not found: " + GESTURES_PATH);
            return;
        }

        var reader = new InputStreamReader(stream);
        GestureEntry[] entries = new Gson().fromJson(reader, GestureEntry[].class);

        for (GestureEntry entry : entries) {
            List<Point> pts = new ArrayList<>(entry.points.size());
            for (PointEntry pe : entry.points) {
                pts.add(new Point(pe.x, pe.y));
            }
            recognizer.addTemplate(entry.name, pts);
        }

        System.out.println("[GesturesLoader] Loaded " + entries.length + " gesture templates.");
    }
}

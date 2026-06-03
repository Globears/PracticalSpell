package com.glo.practicalspell.recognizer;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GesturesLoader {

    private static final String BASE_PATH = "/assets/practicalspell/gestures/";
    private static final String INDEX_FILE = BASE_PATH + "index.json";
    private static final Gson gson = new Gson();

    // 单个手势模板的内部结构
    private static class GestureEntry {
        String name;
        List<PointEntry> points;
    }

    private static class PointEntry {
        @SerializedName("x") double x;
        @SerializedName("y") double y;
    }

    /**
     * 加载所有手势模板。
     * 1. 从 index.json 读取手势名称列表。
     * 2. 依次加载每个名称对应的 .json 文件，每个文件是一个模板数组。
     * 3. 将数组中的每一个模板注册到 DollarRecognizer。
     */
    public static void load(DollarRecognizer recognizer) {
        List<String> gestureNames = loadIndex();
        if (gestureNames == null || gestureNames.isEmpty()) {
            System.err.println("[GesturesLoader] index.json 为空或不存在，未加载任何手势");
            return;
        }

        int totalTemplates = 0;
        for (String gestureName : gestureNames) {
            List<GestureEntry> templates = loadGestureFile(gestureName);
            if (templates == null) {
                System.err.println("[GesturesLoader] 跳过手势 '" + gestureName + "'");
                continue;
            }
            for (GestureEntry template : templates) {
                List<Point> points = new ArrayList<>(template.points.size());
                for (PointEntry pe : template.points) {
                    points.add(new Point(pe.x, pe.y));
                }
                recognizer.addTemplate(template.name, points);
                totalTemplates++;
            }
        }
        System.out.println("[GesturesLoader] 成功加载 " + totalTemplates + " 个手势模板");
    }

    /**
     * 读取 index.json，返回手势名称列表。
     */
    private static List<String> loadIndex() {
        try (InputStream stream = GesturesLoader.class.getResourceAsStream(INDEX_FILE)) {
            if (stream == null) {
                System.err.println("[GesturesLoader] 找不到索引文件: " + INDEX_FILE);
                return null;
            }
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(json, List.class);
        } catch (Exception e) {
            System.err.println("[GesturesLoader] 解析 index.json 失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 加载指定手势名称的模板文件（数组格式）。
     *
     * @param gestureName 索引中的手势名称，对应文件名（不含扩展名）
     * @return 模板数组，若失败返回 null
     */
    private static List<GestureEntry> loadGestureFile(String gestureName) {
        String resourcePath = BASE_PATH + gestureName + ".json";
        try (InputStream stream = GesturesLoader.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                System.err.println("[GesturesLoader] 找不到手势文件: " + resourcePath);
                return null;
            }
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return Arrays.asList(gson.fromJson(json, GestureEntry[].class)) ;
        } catch (Exception e) {
            System.err.println("[GesturesLoader] 加载手势文件失败 (" + resourcePath + "): " + e.getMessage());
            return null;
        }
    }
}
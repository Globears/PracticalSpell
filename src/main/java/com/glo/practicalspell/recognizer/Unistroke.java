package com.glo.practicalspell.recognizer;

import java.util.List;

public class Unistroke {
    public final String name;
    final List<Point> points;
    final double[] vector;

    public Unistroke(String name, List<Point> rawPoints) {
        this.name = name;
        List<Point> pts = DollarRecognizer.resample(rawPoints, DollarRecognizer.NUM_POINTS);
        double radians = DollarRecognizer.indicativeAngle(pts);
        pts = DollarRecognizer.rotateBy(pts, -radians);
        pts = DollarRecognizer.scaleTo(pts, DollarRecognizer.SQUARE_SIZE);
        pts = DollarRecognizer.translateTo(pts, new Point(0, 0));
        this.points = pts;
        this.vector = DollarRecognizer.vectorize(pts);
    }
}

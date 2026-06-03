package com.glo.practicalspell.recognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal Java port of the $1 Unistroke Recognizer.
 * Wobbrock, Wilson & Li (2007-2011). Uses Golden Section Search for matching.
 */
public class DollarRecognizer {

    public static final int NUM_POINTS = 64;
    static final double SQUARE_SIZE = 250.0;
    static final double HALF_DIAGONAL = 0.5 * Math.sqrt(SQUARE_SIZE * SQUARE_SIZE + SQUARE_SIZE * SQUARE_SIZE);

    // Golden Section Search constants
    private static final double PHI = 0.5 * (-1.0 + Math.sqrt(5.0)); // ~0.618
    private static final double ANGLE_RANGE = Math.toRadians(45.0);
    private static final double ANGLE_PRECISION = Math.toRadians(2.0);

    private final List<Unistroke> templates = new ArrayList<>();

    public void addTemplate(String name, List<Point> rawPoints) {
        templates.add(new Unistroke(name, rawPoints));
    }

    public void addTemplate(Unistroke unistroke){
        templates.add(unistroke);
    }

    public void clearTemplates() {
        templates.clear();
    }

    /** Returns results sorted by score descending (best first). */
    public List<Result> recognize(List<Point> rawPoints) {
        List<Point> pts = resample(rawPoints, NUM_POINTS);
        double radians = indicativeAngle(pts);
        pts = rotateBy(pts, -radians);
        pts = scaleTo(pts, SQUARE_SIZE);
        pts = translateTo(pts, new Point(0, 0));

        List<Result> results = new ArrayList<>();
        for (Unistroke template : templates) {
            double[] best = goldenSectionSearch(pts, template.points,
                    -ANGLE_RANGE, +ANGLE_RANGE, ANGLE_PRECISION);
            double score = 1.0 - best[0] / HALF_DIAGONAL;
            results.add(new Result(template.name, score, best[0], best[1]));
        }
        results.sort((a, b) -> Double.compare(b.score(), a.score()));
        return results;
    }

    // ---- Preprocessing -------------------------------------------------------

    static List<Point> resample(List<Point> points, int n) {
        double pathLen = pathLength(points);
        if (pathLen == 0) {
            List<Point> copy = new ArrayList<>(n);
            Point p = points.get(0);
            for (int i = 0; i < n; i++) copy.add(p);
            return copy;
        }
        double I = pathLen / (n - 1);
        double D = 0.0;
        List<Point> src = new ArrayList<>(points);
        List<Point> dst = new ArrayList<>(n);
        dst.add(src.get(0));
        for (int i = 1; i < src.size(); i++) {
            Point p1 = src.get(i - 1);
            Point p2 = src.get(i);
            double d = distance(p1, p2);
            if (D + d >= I) {
                double t = (I - D) / d;
                Point q = new Point(p1.x() + t * (p2.x() - p1.x()), p1.y() + t * (p2.y() - p1.y()));
                dst.add(q);
                src.add(i, q);
                D = 0.0;
            } else {
                D += d;
            }
        }
        if (dst.size() == n - 1) {
            dst.add(src.get(src.size() - 1));
        }
        return dst;
    }

    static double indicativeAngle(List<Point> points) {
        Point c = centroid(points);
        return Math.atan2(points.get(0).y() - c.y(), points.get(0).x() - c.x());
    }

    static List<Point> rotateBy(List<Point> points, double radians) {
        Point c = centroid(points);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        List<Point> result = new ArrayList<>(points.size());
        for (Point p : points) {
            double dx = p.x() - c.x();
            double dy = p.y() - c.y();
            result.add(new Point(dx * cos - dy * sin + c.x(), dx * sin + dy * cos + c.y()));
        }
        return result;
    }

    static List<Point> scaleTo(List<Point> points, double size) {
        Bounds b = bounds(points);
        double sx = b.width != 0 ? size / b.width : 1.0;
        double sy = b.height != 0 ? size / b.height : 1.0;
        List<Point> result = new ArrayList<>(points.size());
        for (Point p : points) {
            result.add(new Point(p.x() * sx, p.y() * sy));
        }
        return result;
    }

    static List<Point> translateTo(List<Point> points, Point target) {
        Point c = centroid(points);
        double tx = target.x() - c.x();
        double ty = target.y() - c.y();
        List<Point> result = new ArrayList<>(points.size());
        for (Point p : points) {
            result.add(new Point(p.x() + tx, p.y() + ty));
        }
        return result;
    }

    static double[] vectorize(List<Point> points) {
        double[] v = new double[points.size() * 2];
        double sum = 0.0;
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            v[i * 2] = p.x();
            v[i * 2 + 1] = p.y();
            sum += p.x() * p.x() + p.y() * p.y();
        }
        double mag = Math.sqrt(sum);
        for (int i = 0; i < v.length; i++) {
            v[i] /= mag;
        }
        return v;
    }

    // ---- Matching (Golden Section Search) ------------------------------------

    static double pathDistance(List<Point> pts1, List<Point> pts2) {
        double d = 0.0;
        for (int i = 0; i < pts1.size(); i++) {
            d += distance(pts1.get(i), pts2.get(i));
        }
        return d / pts1.size();
    }

    /**
     * Golden Section Search for the best rotation angle within [a, b].
     * Returns [distance, angleInDegrees, iterations].
     */
    static double[] goldenSectionSearch(List<Point> pts1, List<Point> pts2,
                                         double a, double b, double threshold) {
        double x1 = PHI * a + (1.0 - PHI) * b;
        double fx1 = pathDistance(rotateBy(pts1, x1), pts2);

        double x2 = (1.0 - PHI) * a + PHI * b;
        double fx2 = pathDistance(rotateBy(pts1, x2), pts2);

        double i = 2.0;
        while (Math.abs(b - a) > threshold) {
            if (fx1 < fx2) {
                b = x2;
                x2 = x1;
                fx2 = fx1;
                x1 = PHI * a + (1.0 - PHI) * b;
                fx1 = pathDistance(rotateBy(pts1, x1), pts2);
            } else {
                a = x1;
                x1 = x2;
                fx1 = fx2;
                x2 = (1.0 - PHI) * a + PHI * b;
                fx2 = pathDistance(rotateBy(pts1, x2), pts2);
            }
            i++;
        }
        return new double[]{Math.min(fx1, fx2), Math.toDegrees((b + a) / 2.0), i};
    }

    // ---- Matching (Protractor, kept for reference) ----------------------------


    static double[] optimalCosineDistance(double[] v1, double[] v2) {
        double a = 0.0, b = 0.0;
        for (int i = 0; i < v1.length; i += 2) {
            a += v1[i] * v2[i] + v1[i + 1] * v2[i + 1];
            b += v1[i] * v2[i + 1] - v1[i + 1] * v2[i];
        }
        double angle = Math.atan(b / a);
        double dist = Math.acos(a * Math.cos(angle) + b * Math.sin(angle));
        return new double[]{dist, Math.toDegrees(angle)};
    }

    // ---- Geometry helpers ----------------------------------------------------

    static double distance(Point a, Point b) {
        double dx = b.x() - a.x();
        double dy = b.y() - a.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    static double pathLength(List<Point> points) {
        double len = 0.0;
        for (int i = 1; i < points.size(); i++) {
            len += distance(points.get(i - 1), points.get(i));
        }
        return len;
    }

    static Point centroid(List<Point> points) {
        double sx = 0.0, sy = 0.0;
        for (Point p : points) {
            sx += p.x();
            sy += p.y();
        }
        return new Point(sx / points.size(), sy / points.size());
    }

    private record Bounds(double minX, double maxX, double minY, double maxY, double width, double height) {}

    private static Bounds bounds(List<Point> points) {
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Point p : points) {
            if (p.x() < minX) minX = p.x();
            if (p.x() > maxX) maxX = p.x();
            if (p.y() < minY) minY = p.y();
            if (p.y() > maxY) maxY = p.y();
        }
        return new Bounds(minX, maxX, minY, maxY, maxX - minX, maxY - minY);
    }
}

package probes;

import java.util.ArrayList;
import java.util.List;

/** Deterministic diagnostic-math checks; not an Android product test. */
public final class VisualCycleMathTest {
    public static void main(String[] args) {
        List<Double> times = new ArrayList<>();
        List<double[]> frames = new ArrayList<>();
        for (int i = 0; i < 160; i++) {
            times.add(i * .25);
            double phase = (i % 32) / 32.0 * Math.PI * 2;
            frames.add(new double[] { 100 + 60 * Math.sin(phase), 100 + 60 * Math.cos(phase) });
        }
        List<VisualCycleMath.Candidate> periods = VisualCycleMath.search(times, frames);
        if (periods.isEmpty() || Math.abs(periods.get(0).seconds - 8) > .2)
            throw new AssertionError("synthetic 8-second period not found");
        frames.clear();
        for (int i = 0; i < times.size(); i++) frames.add(new double[] { 42, 42 });
        if (!VisualCycleMath.search(times, frames).isEmpty()) throw new AssertionError("static false period");
        boolean rejected = false;
        try { VisualCycleMath.distance(new double[0], new double[0]); }
        catch (IllegalArgumentException expected) { rejected = true; }
        if (!rejected) throw new AssertionError("empty features accepted");
        System.out.println("VISUAL_MATH_TESTS: 3 PASS (synthetic periodic, static, invalid)");
    }
}

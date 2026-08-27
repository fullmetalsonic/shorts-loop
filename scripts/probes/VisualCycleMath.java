package probes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Diagnostic period search, deliberately not a production end-of-video detector. */
public final class VisualCycleMath {
    public static final class Candidate {
        public final double seconds, error;
        public final int pairs;
        Candidate(double seconds, double error, int pairs) {
            this.seconds = seconds; this.error = error; this.pairs = pairs;
        }
    }

    public static double distance(double[] a, double[] b) {
        if (a.length == 0 || a.length != b.length) throw new IllegalArgumentException("feature length");
        double sum = 0;
        for (int i = 0; i < a.length; i++) sum += Math.abs(a[i] - b[i]);
        return sum / a.length;
    }

    public static double motion(List<double[]> frames) {
        double sum = 0;
        for (int i = 1; i < frames.size(); i++) sum += distance(frames.get(i), frames.get(i - 1));
        return frames.size() < 2 ? 0 : sum / (frames.size() - 1);
    }

    public static List<Candidate> search(List<Double> times, List<double[]> frames) {
        if (times.size() != frames.size()) throw new IllegalArgumentException("sample mismatch");
        List<Candidate> all = new ArrayList<>(), best = new ArrayList<>();
        if (frames.size() < 20 || motion(frames) < 0.8) return best;
        double span = times.get(times.size() - 1) - times.get(0);
        for (double period = 2; period <= span / 2.1; period += .05) {
            int j = 0, pairs = 0;
            double sum = 0;
            for (int i = 1; i < times.size(); i++) {
                double target = times.get(i) - period;
                if (target < times.get(0)) continue;
                while (j + 1 < i && Math.abs(times.get(j + 1) - target) < Math.abs(times.get(j) - target)) j++;
                if (Math.abs(times.get(j) - target) > .2) continue;
                sum += distance(frames.get(i), frames.get(j)); pairs++;
            }
            if (pairs >= 12) all.add(new Candidate(period, sum / pairs, pairs));
        }
        all.sort(Comparator.comparingDouble(c -> c.error));
        for (Candidate candidate : all) {
            boolean close = false;
            for (Candidate selected : best) if (Math.abs(candidate.seconds - selected.seconds) < .7) close = true;
            if (!close) best.add(candidate);
            if (best.size() == 5) break;
        }
        return best;
    }
}

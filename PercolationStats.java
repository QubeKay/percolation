import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;

public class PercolationStats {

    private static final double CONFIDENCE_95 = 1.96;
    private final int totalTrials;
    private final double[] x;

    // perform independent trials on an n-by-n grid
    public PercolationStats(int n, int totalTrials) {
        if (n <= 0 || totalTrials <= 0) {
            throw new IllegalArgumentException(
                    String.format("Cannot perform '%d trials' on a(n) '%dx%d grid'!", totalTrials,
                                  n, n));
        }
        this.totalTrials = totalTrials;
        x = new double[totalTrials];

        Percolation percolation;
        int row, col;
        for (int i = 0; i < totalTrials; i++) {
            percolation = new Percolation(n);
            while (!percolation.percolates()) {
                // row = 1 + (int) Math.floor(Math.random() * n);
                row = StdRandom.uniformInt(1, n + 1);
                col = StdRandom.uniformInt(1, n + 1);
                percolation.open(row, col);
            }

            add((double) percolation.numberOfOpenSites() / (n * n), i);
        }
    }

    // sample mean of percolation threshold
    public double mean() {
        return StdStats.mean(x);
    }

    // sample standard deviation of percolation threshold
    public double stddev() {
        return StdStats.stddev(x);
    }

    // low endpoint of 95% confidence interval
    public double confidenceLo() {
        return mean() - (CONFIDENCE_95 * Math.sqrt(stddev())) / Math.sqrt(totalTrials);
    }

    // high endpoint of 95% confidence interval
    public double confidenceHi() {
        return mean() + (CONFIDENCE_95 * Math.sqrt(stddev())) / Math.sqrt(totalTrials);
    }

    private void add(double xi, int i) {
        this.x[i] = xi;
    }

    // test client (see below)
    public static void main(String[] args) {

        int n = Integer.parseInt(args[0]);
        int totalTrials = Integer.parseInt(args[1]);

        PercolationStats stats = new PercolationStats(n, totalTrials);

        System.out.printf("mean %20s %f%n", "=", stats.mean());
        System.out.printf("stddev  %17s %f%n", "=", stats.stddev());
        System.out.printf("95%% confidence interval = [%f, %f]%n", stats.confidenceLo(),
                          stats.confidenceHi());
    }

}

public class Percolation {

    // percolation tree
    private final int[][][] sites;
    private final int[][] size;
    private int openSites = 0;
    private int[] virtualTopSite = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };
    private int[] virtualBottomSite = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };

    // full sites
    private final int[][][] sitesFull;
    private final int[][] sizeFull;
    private int[] virtualTopSiteFull = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };

    // creates n-by-n grid, with all sites initially blocked
    public Percolation(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.format("%d is not a valid grid size!", n));
        }

        // percolation tree
        sites = new int[n][n][2];
        size = new int[n][n];

        // full site tree
        sitesFull = new int[n][n][2];
        sizeFull = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // blocked site has no roots, an open site can have itself as its root

                // percolation tree
                sites[i][j] = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };
                size[i][j] = 0;

                // full site tree
                sitesFull[i][j] = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE };
                sizeFull[i][j] = 0;
            }
        }
    }

    private int[] root(int row, int col) {
        int temp;

        while ((sites[row][col][0] != row || sites[row][col][1] != col)) {
            temp = sites[row][col][0];
            col = sites[row][col][1];
            row = temp;

            // with path compression
            temp = sites[row][col][0];
            col = sites[row][col][1];
            row = temp;
        }

        return new int[] { row, col };
    }

    private int[] rootFull(int row, int col) {
        int temp;

        while ((sitesFull[row][col][0] != row || sitesFull[row][col][1] != col)) {
            temp = sitesFull[row][col][0];
            col = sitesFull[row][col][1];
            row = temp;

            // with path compression
            temp = sitesFull[row][col][0];
            col = sitesFull[row][col][1];
            row = temp;
        }

        return new int[] { row, col };
    }

    private void union(int row1, int col1, int row2, int col2) {
        int[] root1 = root(sites[row1][col1][0], sites[row1][col1][1]);
        int[] root2 = root(sites[row2][col2][0], sites[row2][col2][1]);

        if (root1[0] != root2[0] || root1[1] != root2[1]) {
            // if size of tree w/ root 1 < size of tree with root 2, make root2 root1's root
            if (size[root1[0]][root1[1]] < size[root2[0]][root2[1]]) {
                sites[root1[0]][root1[1]][0] = root2[0];
                sites[root1[0]][root1[1]][1] = root2[1];
                size[root2[0]][root2[1]] += size[root1[0]][root1[1]];
            }
            else { // make root1 root2's root
                sites[root2[0]][root2[1]][0] = root1[0];
                sites[root2[0]][root2[1]][1] = root1[1];
                size[root1[0]][root1[1]] += size[root2[0]][root2[1]];
            }
        }
    }

    private void unionFull(int row1, int col1, int row2, int col2) {
        int[] root1 = rootFull(sitesFull[row1][col1][0], sitesFull[row1][col1][1]);
        int[] root2 = rootFull(sitesFull[row2][col2][0], sitesFull[row2][col2][1]);

        if (root1[0] != root2[0] || root1[1] != root2[1]) {
            // if size of tree w/ root 1 < size of tree with root 2, make root2 root1's root
            if (sizeFull[root1[0]][root1[1]] < sizeFull[root2[0]][root2[1]]) {
                sitesFull[root1[0]][root1[1]][0] = root2[0];
                sitesFull[root1[0]][root1[1]][1] = root2[1];
                sizeFull[root2[0]][root2[1]] += sizeFull[root1[0]][root1[1]];
            }
            else { // make root1 root2's root
                sitesFull[root2[0]][root2[1]][0] = root1[0];
                sitesFull[root2[0]][root2[1]][1] = root1[1];
                sizeFull[root1[0]][root1[1]] += sizeFull[root2[0]][root2[1]];
            }
        }
    }

    // opens the site (row, col) if it is not open already
    public void open(int row, int col) {
        if (row < 1 || row > sites.length) {
            throw new IllegalArgumentException(
                    String.format("Row index out of range: 1 \u2264 rowIndex \u2264 %d",
                                  sites.length));
        }

        if (col < 1 || col > sites[0].length) {
            throw new IllegalArgumentException(
                    String.format("Column index out of range: 1 \u2264 colIndex \u2264 %d",
                                  sites[0].length));
        }

        row--;
        col--;

        if (!isSiteOpen(row, col)) {
            // percolation tree
            openSites++;
            sites[row][col][0] = row;
            sites[row][col][1] = col;
            size[row][col] = 1;

            // full sites tree
            sitesFull[row][col][0] = row;
            sitesFull[row][col][1] = col;
            sizeFull[row][col] = 1;
        }
        else {
            return; // already open
        }

        // Is site above open? Connect.
        if (isSiteOpen(row - 1, col)) {
            union(row, col, row - 1, col);
            unionFull(row, col, row - 1, col);
        }

        // Is site below open? Connect.
        if (isSiteOpen(row + 1, col)) {
            union(row, col, row + 1, col);
            unionFull(row, col, row + 1, col);
        }

        // Is site to the left open? Connect.
        if (isSiteOpen(row, col - 1)) {
            union(row, col, row, col - 1);
            unionFull(row, col, row, col - 1);
        }

        // Is site to the right open? Connect.
        if (isSiteOpen(row, col + 1)) {
            union(row, col, row, col + 1);
            unionFull(row, col, row, col + 1);
        }

        // if opening a top row site in percolation tree, connect to top virtual element
        if (row == 0 && virtualTopSite[0] != Integer.MIN_VALUE) {
            union(row, col, virtualTopSite[0], virtualTopSite[1]);
        }
        else if (row == 0) {
            virtualTopSite = root(row, col);
        }

        // if opening a top row site in full tree, connect to top virtual element
        if (row == 0 && virtualTopSiteFull[0] != Integer.MIN_VALUE) {
            unionFull(row, col, virtualTopSiteFull[0], virtualTopSiteFull[1]);
        }
        else if (row == 0) {
            virtualTopSiteFull = rootFull(row, col);
        }

        // if opening a bottom row site, connect to bottom virtual element
        if (row == sites.length - 1 && virtualBottomSite[0] != Integer.MIN_VALUE) {
            union(row, col, virtualBottomSite[0], virtualBottomSite[1]);
        }
        else if (row == sites.length - 1) {
            virtualBottomSite = root(row, col);
        }
    }

    private boolean isSiteOpen(int row, int col) {
        if (row < 0 || row >= sites.length || col < 0 || col >= sites[0].length) {
            return false;
        }

        return sites[row][col][0] != Integer.MIN_VALUE && sites[row][col][1] != Integer.MIN_VALUE;
    }

    // is the site (row, col) open?
    public boolean isOpen(int row, int col) {
        if (row < 1 || row > sites.length) {
            throw new IllegalArgumentException(
                    String.format("Row index out of range: 1 \u2264 rowIndex \u2264 %d",
                                  sites.length));
        }

        if (col < 1 || col > sites[0].length) {
            throw new IllegalArgumentException(
                    String.format("Column index out of range: 1 \u2264 colIndex \u2264 %d",
                                  sites[0].length));
        }

        row--;
        col--;

        return isSiteOpen(row, col);
    }

    // is the site (row, col) full?
    public boolean isFull(int row, int col) {
        if (row < 1 || row > sitesFull.length) {
            throw new IllegalArgumentException(
                    String.format("Row index out of range: 1 \u2264 rowIndex \u2264 %d",
                                  sitesFull.length));
        }

        if (col < 1 || col > sitesFull[0].length) {
            throw new IllegalArgumentException(
                    String.format("Column index out of range: 1 \u2264 colIndex \u2264 %d",
                                  sitesFull[0].length));
        }

        row--;
        col--;

        if (!isSiteOpen(row, col))
            return false;

        // is site connected to top row sites?
        int[] siteRoot = rootFull(row, col);

        int[] virtualTopSiteFullRoot =
                virtualTopSiteFull[0] == Integer.MIN_VALUE
                        || virtualTopSiteFull[1] == Integer.MIN_VALUE ?
                virtualTopSiteFull : rootFull(virtualTopSiteFull[0], virtualTopSiteFull[1]);
        return siteRoot[0] == virtualTopSiteFullRoot[0] && siteRoot[1] == virtualTopSiteFullRoot[1];
    }

    // returns the number of open sites
    public int numberOfOpenSites() {
        return openSites;
    }

    // does the system percolate?
    public boolean percolates() {

        if (virtualBottomSite[0] == Integer.MIN_VALUE || virtualTopSite[0] == Integer.MIN_VALUE)
            return false;

        int[] virtualBottomSiteRoot = root(virtualBottomSite[0], virtualBottomSite[1]);
        int[] virtualTopSiteRoot = root(virtualTopSite[0], virtualTopSite[1]);

        return virtualBottomSiteRoot[0] == virtualTopSiteRoot[0]
                && virtualBottomSiteRoot[1] == virtualTopSiteRoot[1];
    }

    // test client (optional)
    public static void main(String[] args) {
        boolean thatFATBug = false;
        int tries = 1;
        boolean oneMoreAfterNSites = false;

        while (!thatFATBug) {
            int n = Integer.parseInt(args[0]);
            Percolation percolation = new Percolation(n);

            int row, col;

            while (!percolation.percolates()) {
                row = 1 + (int) Math.floor(Math.random() * n);
                col = 1 + (int) Math.floor(Math.random() * n);

                if (!percolation.isOpen(row, col)) {
                    percolation.open(row, col);
                }

                if (!oneMoreAfterNSites && percolation.numberOfOpenSites() == n * n) {
                    oneMoreAfterNSites = true;
                }
                else if (percolation.numberOfOpenSites() == n * n) {
                    thatFATBug = true;
                    System.out.printf("THAT FAT BUG [%d TRIES]! YOU KNOW IT!!!!!!!!%n", tries);
                    //                    printThatFatBug(percolation);
                    break;
                }
            }
            tries++;

            System.out.printf("%n%n%n%nOpen Sites: %d / %d = %.3f%n",
                              percolation.numberOfOpenSites(), n * n,
                              (double) percolation.numberOfOpenSites() / (n * n));
            System.out.printf("Percolates? %b%n", percolation.percolates());
        }
    }

    //    private static void printThatFatBug(Percolation percolation) {
    //        int[][][] sites = percolation.getGrid();
    //        int[] vTS = percolation.getVirtualTopSite();
    //        int[] vBS = percolation.getVirtualBottoSite();
    //        System.out.printf("%n%n%n%nPercolates? %b%n", percolation.percolates());
    //        System.out.println("\nVirtual Top Site: ");
    //        System.out.printf("[%2d, %2d] %n", vTS[0], vTS[1]);
    //
    //        System.out.print("C\\R");
    //        for (int j = 0; j < sites[0].length; j++) {
    //            System.out.printf("%9d", j);
    //        }
    //
    //        System.out.println();
    //
    //        for (int i = 0; i < sites.length; i++) {
    //            System.out.printf("%2d: ", i);
    //            for (int j = 0; j < sites[0].length; j++) {
    //                System.out.printf("[%2d, %2d] ", sites[i][j][0], sites[i][j][1]);
    //            }
    //            System.out.println();
    //        }
    //        System.out.printf("[%2d, %2d] ", vBS[0], vBS[1]);
    //        System.out.println("\nVirtual Bottom Site ^");
    //    }
}

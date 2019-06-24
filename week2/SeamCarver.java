import edu.princeton.cs.algs4.Picture;

/**
 * Class to provide methods to resize any given image while preserving the content of the picture
 */
public class SeamCarver {

    private static final double BORDER_ENERGY = 1000.0;

    private Picture picture;
    /* data variable to store rgb values for each pixel inside the picture */
    private int[][] rgbMatrix;

    public SeamCarver(Picture picture) {
        if (picture == null) { throw new IllegalArgumentException("Picture input is null!"); }
        this.picture = new Picture(picture);
        rgbMatrix = new int[picture.width()][picture.height()];
        getRGBMatrix();
    }

    /**
     * Method to allocate rgb values to the matrix
     */
    private void getRGBMatrix() {
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                rgbMatrix[x][y] = this.picture.getRGB(x, y);
            }
        }
    }

    /**
     * Returns the width of image
     */
    public int width() {
        return this.picture.width();
    }

    /**
     * Returns the height of image
     */
    public int height() {
        return this.picture.height();
    }

    /**
     * Returns the picture object
     */
    public Picture picture() {
        return new Picture(this.picture);
    }

    /**
     * Method to remove any given seam from the image width-wise
     * <p>
     * Check:
     * 1. length of seam is equal to width
     * 2. height of image is greater than 1
     * 3. adjacent pixels in the seam are not separated by distance greater than 1
     * <p>
     * Algo:
     * seam is represented as {1, 0, 1, 2, 3}
     * 1. the value of the pixel is changed to Integer max_val
     * 2. create a new matrix removing those pixels with Integer max_val
     * 3. create a new picture object with new width and height
     *
     * @param seam
     */
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) { throw new IllegalArgumentException("Null argument for removing horizontal seam!"); }
        if (seam.length != width()) { throw new IllegalArgumentException("Seam length greater than width!"); }
        if (height() <= 1) { throw new IllegalArgumentException("Picture height is less than 1"); }

        for (int i = 0; i < seam.length; i++) {
            int y = seam[i];
            if (y < 0 || y > height() - 1) {
                throw new IllegalArgumentException("Y-axis outside range while removing seam!");
            }
            if (i > 0) {
                int nextY = seam[i - 1];
                if (Math.abs(y - nextY) > 1) { throw new IllegalArgumentException("Adjacent y's differ more than 1"); }
            }
            rgbMatrix[i][y] = Integer.MAX_VALUE;
        }
        rgbMatrix = arrayCopy(rgbMatrix, false);
        updatePicture();
    }

    /**
     * Method to create a new matrix by removing pixels with Integer max_val
     *
     * @param array
     * @param vertical
     */
    private int[][] arrayCopy(int[][] array, boolean vertical) {
        int[][] matrix =
                vertical ? new int[array.length - 1][array[0].length] : new int[array.length][array[0].length - 1];
        boolean[][] marked = new boolean[array.length][array[0].length];
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                if (!marked[x][y] && array[x][y] != Integer.MAX_VALUE) {
                    matrix[x][y] = array[x][y];
                } else if (vertical) {
                    matrix[x][y] = array[x + 1][y];
                    marked[x + 1][y] = true;
                } else {
                    matrix[x][y] = array[x][y + 1];
                    marked[x][y + 1] = true;
                }
                marked[x][y] = true;
            }
        }
        return matrix;
    }

    /**
     * Method to create a new picture with updated width and height
     */
    private void updatePicture() {
        Picture newPic = new Picture(rgbMatrix.length, rgbMatrix[0].length);
        for (int x = 0; x < rgbMatrix.length; x++) {
            for (int y = 0; y < rgbMatrix[0].length; y++) {
                newPic.setRGB(x, y, rgbMatrix[x][y]);
            }
        }
        this.picture = newPic;
    }

    /**
     * Method to remove any given seam from the image height-wise
     * <p>
     * Check:
     * 1. length of seam is equal to height
     * 2. width of image is greater than 1
     * 3. adjacent pixels in the seam are not separated by distance greater than 1
     * <p>
     * Algo:
     * seam is represented as {1, 0, 1, 2, 3}
     * 1. the value of the pixel is changed to Integer max_val
     * 2. create a new matrix removing those pixels with Integer max_val
     * 3. create a new picture object with new width and height
     *
     * @param seam
     */
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) { throw new IllegalArgumentException("Null argument for removing vertical seam!"); }
        if (seam.length != height()) { throw new IllegalArgumentException("Seam length greater than height!"); }
        if (width() <= 1) { throw new IllegalArgumentException("Picture width is less than 1"); }

        for (int i = 0; i < seam.length; i++) {
            int x = seam[i];
            if (x < 0 || x > width() - 1) {
                throw new IllegalArgumentException("X-axis outside range while removing seam!");
            }
            if (i > 0) {
                int nextX = seam[i - 1];
                if (Math.abs(x - nextX) > 1) { throw new IllegalArgumentException("Adjacent x's differ more than 1"); }
            }
            rgbMatrix[x][i] = Integer.MAX_VALUE;
        }
        rgbMatrix = arrayCopy(rgbMatrix, true);
        updatePicture();
    }

    /**
     * Method to get seam array with minimum sum of energy
     * The crux of the algo is relax mathod for each pixel which follows a topological sort idea to relax the energy
     * of each pixel as we traverse the array
     */
    public int[] findVerticalSeam() {
        double[][] energy = calculateEnergy();

        double[][] simulator = getSimulator();
        int[][] edgeTo = new int[width()][height()];

        for (int j = 0; j < simulator[0].length; j++) {
            for (int i = 0; i < simulator.length; i++) {
                relaxVerticalSeam(i, j, simulator, energy, edgeTo);
            }
        }

        double minEnergy = Double.POSITIVE_INFINITY;
        int pos = -1;
        int height = simulator[0].length - 1;
        for (int x = 0; x < simulator.length; x++) {
            if (minEnergy > simulator[x][height]) {
                minEnergy = simulator[x][height];
                pos = x;
            }
        }
        return verticalSeam(edgeTo, pos);
    }

    /**
     * Method to create a energy matrix for each pixel
     */
    private double[][] calculateEnergy() {
        double[][] energyMatrix = new double[width()][height()];
        for (int x = 0; x < energyMatrix.length; x++) {
            for (int y = 0; y < energyMatrix[0].length; y++) {
                energyMatrix[x][y] = energy(x, y);
            }
        }
        return energyMatrix;
    }

    /**
     * Method to get a simulator matrix with each pixel of energy positive infinity
     */
    private double[][] getSimulator() {
        double[][] simulator = new double[width()][height()];

        for (int i = 0; i < simulator.length; i++) {
            for (int j = 0; j < simulator[i].length; j++) {
                simulator[i][j] = Double.POSITIVE_INFINITY;
            }
        }
        return simulator;
    }

    /**
     * Method to reduce the energy of adjoining pixels for given x, y
     *
     * @param x
     * @param y
     * @param simulator
     * @param energy
     * @param edgeTo
     */
    private void relaxVerticalSeam(int x, int y, double[][] simulator, double[][] energy, int[][] edgeTo) {
        if (y == 0) {
            simulator[x][y] = energy[x][y];
            edgeTo[x][y] = x;
        }
        if (withinLimits(x + 1, y + 1) && simulator[x + 1][y + 1] > simulator[x][y] + energy[x + 1][y + 1]) {
            simulator[x + 1][y + 1] = simulator[x][y] + energy[x + 1][y + 1];
            edgeTo[x + 1][y + 1] = x;
        }
        if (withinLimits(x, y + 1) && simulator[x][y + 1] > simulator[x][y] + energy[x][y + 1]) {
            simulator[x][y + 1] = simulator[x][y] + energy[x][y + 1];
            edgeTo[x][y + 1] = x;
        }
        if (withinLimits(x - 1, y + 1) && simulator[x - 1][y + 1] > simulator[x][y] + energy[x - 1][y + 1]) {
            simulator[x - 1][y + 1] = simulator[x][y] + energy[x - 1][y + 1];
            edgeTo[x - 1][y + 1] = x;
        }
    }

    /**
     * Method to trace back the steps for minEnergy seam
     *
     * @param edgeTo
     * @param pos
     */
    private int[] verticalSeam(int[][] edgeTo, int pos) {
        int height = edgeTo[0].length;
        int[] seam = new int[height];
        while (height > 0) {
            seam[height - 1] = pos;
            pos = edgeTo[pos][height - 1];
            height--;
        }
        return seam;
    }

    /**
     * Method to calculate the energy for a given pixel of picture
     *
     * @param x
     * @param y
     */
    public double energy(int x, int y) {
        if (x < 0 || x > width() - 1) { throw new IllegalArgumentException("X-axis outside range!"); }
        if (y < 0 || y > height() - 1) { throw new IllegalArgumentException("Y-axis outside range!"); }
        if (x == 0 || x == width() - 1) { return BORDER_ENERGY; }
        if (y == 0 || y == height() - 1) { return BORDER_ENERGY; }

        int argbXminusOne = rgbMatrix[x - 1][y];
        int rXminusOne = (argbXminusOne >> 16) & 0xFF;
        int gXminusOne = (argbXminusOne >> 8) & 0xFF;
        int bXminusOne = (argbXminusOne) & 0xFF;

        int argbXplusOne = rgbMatrix[x + 1][y];
        int rXplusOne = (argbXplusOne >> 16) & 0xFF;
        int gXplusOne = (argbXplusOne >> 8) & 0xFF;
        int bXplusOne = (argbXplusOne) & 0xFF;
        double deltaX = Math.pow(rXplusOne - rXminusOne, 2) + Math.pow(gXplusOne - gXminusOne, 2) + Math
                .pow(bXplusOne - bXminusOne, 2);

        int argbYminusOne = rgbMatrix[x][y - 1];
        int rYminusOne = (argbYminusOne >> 16) & 0xFF;
        int gYminusOne = (argbYminusOne >> 8) & 0xFF;
        int bYminusOne = (argbYminusOne) & 0xFF;

        int argbYplusOne = rgbMatrix[x][y + 1];
        int rYplusOne = (argbYplusOne >> 16) & 0xFF;
        int gYplusOne = (argbYplusOne >> 8) & 0xFF;
        int bYplusOne = (argbYplusOne) & 0xFF;
        double deltaY = Math.pow(rYplusOne - rYminusOne, 2) + Math.pow(gYplusOne - gYminusOne, 2) + Math
                .pow(bYplusOne - bYminusOne, 2);

        return Math.sqrt(deltaX + deltaY);
    }

    /**
     * Method to test if the given pixel is within the coordinates of picture
     *
     * @param x
     * @param y
     */
    private boolean withinLimits(int x, int y) {
        boolean xLimit = x >= 0 && x <= width() - 1;
        boolean yLimit = y >= 0 && y <= height() - 1;
        return xLimit && yLimit;
    }

    /**
     * Method to get seam array with minimum sum of energy
     * The crux of the algo is relax mathod for each pixel which follows a topological sort idea to relax the energy
     * of each pixel as we traverse the array
     */
    public int[] findHorizontalSeam() {
        double[][] energy = calculateEnergy();

        double[][] simulator = getSimulator();
        int[][] edgeTo = new int[width()][height()];

        for (int i = 0; i < simulator.length; i++) {
            for (int j = 0; j < simulator[i].length; j++) {
                relaxHorizontalSeam(i, j, simulator, energy, edgeTo);
            }
        }

        double minEnergy = Double.POSITIVE_INFINITY;
        int pos = -1;
        int width = simulator.length - 1;
        for (int y = 0; y < simulator[0].length; y++) {
            if (minEnergy > simulator[width][y]) {
                minEnergy = simulator[width][y];
                pos = y;
            }
        }

        return horizontalSeam(edgeTo, pos);
    }

    /**
     * Method to reduce the energy of adjoining pixels for given x, y
     *
     * @param x
     * @param y
     * @param simulator
     * @param energy
     * @param edgeTo
     */
    private void relaxHorizontalSeam(int x, int y, double[][] simulator, double[][] energy, int[][] edgeTo) {
        if (x == 0) {
            simulator[x][y] = energy[x][y];
            edgeTo[x][y] = y;
        }
        if (withinLimits(x + 1, y - 1) && simulator[x + 1][y - 1] > simulator[x][y] + energy[x + 1][y - 1]) {
            simulator[x + 1][y - 1] = simulator[x][y] + energy[x + 1][y - 1];
            edgeTo[x + 1][y - 1] = y;
        }
        if (withinLimits(x + 1, y) && simulator[x + 1][y] > simulator[x][y] + energy[x + 1][y]) {
            simulator[x + 1][y] = simulator[x][y] + energy[x + 1][y];
            edgeTo[x + 1][y] = y;
        }
        if (withinLimits(x + 1, y + 1) && simulator[x + 1][y + 1] > simulator[x][y] + energy[x + 1][y + 1]) {
            simulator[x + 1][y + 1] = simulator[x][y] + energy[x + 1][y + 1];
            edgeTo[x + 1][y + 1] = y;
        }
    }

    /**
     * Method to trace back the steps for minEnergy seam
     *
     * @param edgeTo
     * @param pos
     */
    private int[] horizontalSeam(int[][] edgeTo, int pos) {
        int width = edgeTo.length;
        int[] seam = new int[width];
        while (width > 0) {
            seam[width - 1] = pos;
            pos = edgeTo[width - 1][pos];
            width--;
        }
        return seam;
    }
}
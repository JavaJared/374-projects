import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.AcyclicSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;

class SeamCarver {
    private EdgeWeightedDigraph graph;
    private Picture picture;

    SeamCarver(Picture picture) {
        this.picture = new Picture(picture);
        buildGraph();
    }

    public Picture picture() {
        return new Picture(picture);
    }

    private int index(int row, int col) {
        return row * width() + col;
    }

    private int row(int index){
        return index / width();
    }

    private int col(int index) {
        return index % width();
    }

    public int height() {
        return picture.height();
    }

    public int width() {
        return picture.width();
    }

    private Picture transpose(Picture picture) {
    	int width = picture.width();
    	int height = picture.height();
        Picture transposed = new Picture(height, width);
        // TODO: Transpose the image
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                transposed.set(row, col, picture.get(col, row));
            }
        }
        return transposed;
    }

    private void buildGraph() {
        //TODO: build the graph
    	int numVertices = width() * height() + 2;
    	graph = new EdgeWeightedDigraph(numVertices);
    	int source = 0;
        int sink = numVertices - 1;
        
     // Connect source to top row pixels
        for (int col = 0; col < width(); col++) {
            int pixelIndex = getPixelIndex(0, col, width());
            double energy = picture.energy(col, 0);
            graph.addEdge(new DirectedEdge(source, pixelIndex, energy));
        }
        
        // Connect bottom row pixels to sink
        for (int col = 0; col < width(); col++) {
            int pixelIndex = getPixelIndex(height() - 1, col, width());
            graph.addEdge(new DirectedEdge(pixelIndex, sink, 0));
        }
        
        // Connect non-border pixels to pixels in the row below
        for (int row = 0; row < height() - 1; row++) {
            for (int col = 0; col < width(); col++) {
                int pixelIndex = getPixelIndex(row, col, width());
                double energy = picture.energy(col, row);

                // Connect to left diagonal pixel
                if (col > 0) {
                    int leftDiagonalIndex = getPixelIndex(row + 1, col - 1, width());
                    graph.addEdge(new DirectedEdge(pixelIndex, leftDiagonalIndex, energy));
                }

                // Connect to directly below pixel
                int belowIndex = getPixelIndex(row + 1, col, width());
                graph.addEdge(new DirectedEdge(pixelIndex, belowIndex, energy));

                // Connect to right diagonal pixel
                if (col < width() - 1) {
                    int rightDiagonalIndex = getPixelIndex(row + 1, col + 1, width());
                    graph.addEdge(new DirectedEdge(pixelIndex, rightDiagonalIndex, energy));
                }
            }
        }
    }
    
    private static int getPixelIndex(int row, int col, int width) {
        return row * width + col + 1; // add 1 to skip source vertex
    }

    public double energy(int col, int row) {
        if (col < 0 || col >= picture.width() || row < 0 || row >= picture.height()) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
        double deltaX = energyDelta(col - 1, row, col + 1, row);
        double deltaY = energyDelta(col, row - 1, col, row + 1);
        return Math.sqrt(deltaX + deltaY) ;
    }

    private double energyDelta(int col1, int row1, int col2, int row2) {
        // Wrap the rows an columns
        col1 = (col1 + width()) % width();
        col2 = (col2 + width()) % width();
        row1 = (row1 + height()) % height();
        row2 = (row2 + height()) % height();
        
        double energy = 0.0;
        //TODO: Compute the sum of the squared differences of red, green, and blue.
        Color pixel1 = new Color(picture.get(col1, row1));
        Color pixel2 = new Color(picture.get(col2, row2));
        
        int redDiff = pixel1.getRed() - pixel2.getRed();
        int greenDiff = pixel1.getGreen() - pixel2.getGreen();
        int blueDiff = pixel1.getBlue() - pixel2.getBlue();
        energy += redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff;
        
        return energy;
    }


    private Picture removeVerticalSeam(Picture picture, int[] seam) {
        if (seam == null || seam.length != picture.height()) {
            throw new IllegalArgumentException("Invalid seam");
        }        
        Picture result;
        // Create a new picture with the seam removed. 
        return result;
    }

    public int[] findVerticalSeam() {
        int[] seam = new int[height()];
        //TODO: Find the seam
        
     // Initialize the energy and distTo arrays
        double[][] energy = new double[width()][height()];
        double[][] distTo = new double[width()][height()];
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                energy[x][y] = energyDelta(x, y, x, y + 1);
                distTo[x][y] = Double.POSITIVE_INFINITY;
            }
        }

        // Initialize the distTo array for the first row
        for (int x = 0; x < width(); x++) {
            distTo[x][0] = energy[x][0];
        }

        // Compute the shortest paths from the top row to the bottom row
        AcyclicSP sp = new AcyclicSP(distTo, energy, height() - 1);
        Iterable<Integer> path = sp.pathTo(seam[height() - 1]);

        // Convert the path to a seam
        int i = 0;
        for (int x : path) {
            seam[i++] = x;
        }
        
        return seam;
    }


    public void removeVerticalSeam(int[] verticalSeam) {
        picture = removeVerticalSeam(picture, verticalSeam);
        buildGraph();
    }

    public int[] findHorizontalSeam() {
        return (new SeamCarver(transpose(picture))).findVerticalSeam();
    }

    
    public void removeHorizontalSeam(int[] horizontalSeam) {
        picture = transpose(removeVerticalSeam(transpose(picture), horizontalSeam));
        buildGraph();
    }

    public static void main(String[] args) {
        // Example usage
        Picture picture = new Picture(args[0]);
        SeamCarver seamCarver = new SeamCarver(picture);

        int[] verticalSeam = seamCarver.findVerticalSeam();
        seamCarver.removeVerticalSeam(verticalSeam);

        int[] horizontalSeam = seamCarver.findHorizontalSeam();
        seamCarver.removeHorizontalSeam(horizontalSeam);
    }
}


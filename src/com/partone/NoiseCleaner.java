package com.partone;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class NoiseCleaner {
	
	static SimplexSolver solver = new SimplexSolver();
	static ImageExample imageFactory = new ImageExample();
	static Double lambda = 1.0;
	
	public static class Pixel {
		Double red;
		Double green;
		Double blue;
		Double cls;
		int[] up = null;
		int[] down = null;
		int[] right = null;
		int[] left = null;
		String type;
		
		public Pixel(Double red, Double green, Double blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		public Double getClassification() {
			if (this.cls == null) {
				Double blueDistance = Math.sqrt(Math.pow(red - 0.0, 2) + Math.pow(green - 0.0, 2) + Math.pow(blue - 255.0, 2));
				Double yellowDistance = Math.sqrt(Math.pow(red - 255.0, 2) + Math.pow(green - 255.0, 2) + Math.pow(blue - 0.0, 2));
				
				this.cls =  (blueDistance > yellowDistance) ? 0.0 : 1.0;				
			}
			return this.cls;
		}
	}
	
	/*
	 * Function that converts classification (1 or 0) into
	 * a rgb value (blue or yellow)
	 */
	static Double[] T(Double x) {
		if (x.equals(1.0)) { // blue
			return new Double[]{0.0, 0.0, 255.0};
		} else { // yellow
			return new Double[]{255.0, 255.0, 0.0};
		}
	}
	
	/**
	 * Data term function.
	 */
	static Double fd(Double x, Pixel pixel) {
		Double[] convertedX = T(x);
		Double distance = Math.sqrt(Math.pow(convertedX[0] - pixel.red, 2) + 
				Math.pow(convertedX[1] - pixel.green, 2) + Math.pow(convertedX[2] - pixel.blue, 2));
		return distance;
	}
	
	/*
	 * Given the matrix entry A[i][j] with width w,
	 * calculate its position in vector of length w * h
	 */
	static int getLinearPosition(int i, int j, int w) {
		return i * w + j;
	}
	
	/*
	 * The inverse of the above function. Given a linear position,
	 * return its matrix position in A
	 */
	static int[] getMatrixPosition(int pos, int w) {
		return new int[]{pos/w, pos % w};
	}
	
	/*
	 * Given the array of pixel edge numbers, the pixel's linear position, and the direction of the 
	 * fp edge, get the fp edge's linear position. This is used for the indexing of the A matrix
	 * 
	 * The order of the edge direction is up -> left -> right -> down
	 * 
	 * We must offset the position when the pixel has null edges. For example, if we have an upper left corner
	 * edge, it has no up/left edge, meaning the right edge comes first.
	 * This in fact only applies to pixels with null edges for up, left, and right 
	 */	
	static int getFPLinearPosition(int[] pixelEdges, int pixelLinearPosition, String direction, Pixel pixel) {		
		
		int position = 0;
		
		// Find the pixel location, adding the edges along the way
		for (int i = 0; i < pixelLinearPosition; i++) {
			position += pixelEdges[i];
		}
		
		// Now offset based on which edges are null while checking direction
		if (direction.equals("up")) {
			return position;
		}
		
		if (pixel.up == null) {
			position -= 1;
		}
		
		if (direction.equals("left")) {
			return position + 1;
		}
		
		if (pixel.left == null) {
			position -= 1;
		}
		
		if (direction.equals("right")) {
			return position + 2;
		}
		
		if (pixel.right == null) {
			position -= 1;
		}
		
		// Final case - direction equals down
		return position + 3;
	}

	
	public static void main(String[] args) {
		// First get the image
		BufferedImage image = imageFactory.getImage();
		int w = image.getWidth();
		int h = image.getHeight();
		
		// Now march through the image and create a copy matrix with Pixel objects
		Pixel[][] imagePixels = new Pixel[h][w];
		
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int pixel = image.getRGB(j, i);
				// Pixel values in 256 in arraylist
				ArrayList<Double> pixelRGB = imageFactory.getPixelARGB(pixel);
				Pixel pixelObject = new Pixel(pixelRGB.get(0), pixelRGB.get(1), pixelRGB.get(2));
				// Now assigns the type to the pixel - corner, edge, or middle
				// Also assign the coordinates of the neighboring pixels
				if ((i == 0 && j == 0)) {
					pixelObject.down = new int[]{i+1, j};
					pixelObject.right = new int[]{i, j+1};
					pixelObject.type = "corner";
				} else if (i == 0 && j == w - 1) {
					pixelObject.left = new int[]{i, j - 1};
					pixelObject.down = new int[]{i+1, j};
					pixelObject.type = "corner";
				} else if (i == h - 1 && j == 0) {
					pixelObject.up = new int[]{i-1, j};
					pixelObject.right = new int[]{i, j+1};
					pixelObject.type = "corner";
				} else if (i == h - 1 && j == w - 1) {
					pixelObject.left = new int[]{i, j - 1};
					pixelObject.up = new int[]{i-1, j};
					pixelObject.type = "corner";
				} else if (i == 0) {
					pixelObject.left = new int[]{i, j - 1};
					pixelObject.right = new int[]{i, j + 1};
					pixelObject.down = new int[]{i+1, j};
					pixelObject.type = "edge";
				} else if (i == h-1) {
					pixelObject.left = new int[]{i, j - 1};
					pixelObject.right = new int[]{i, j + 1};
					pixelObject.up = new int[]{i-1, j};
					pixelObject.type = "edge";
				} else if (j == 0) {
					pixelObject.right = new int[]{i, j + 1};
					pixelObject.up = new int[]{i-1, j};
					pixelObject.down = new int[]{i+1, j};					
					pixelObject.type = "edge";
				} else if (j == w - 1) {
					pixelObject.left = new int[]{i, j - 1};
					pixelObject.up = new int[]{i-1, j};
					pixelObject.down = new int[]{i+1, j};										
					pixelObject.type = "edge";
				} else {
					pixelObject.left = new int[]{i, j - 1};
					pixelObject.up = new int[]{i-1, j};
					pixelObject.down = new int[]{i+1, j};										
					pixelObject.right = new int[]{i, j + 1};
					pixelObject.type = "middle";
				}
				imagePixels[i][j] = pixelObject;
			}
		}
		
		// Now towards creating the matrices and vectors for the simplex algorithm
		
		// The width of the A matrix - The number of non basic variables
		int n = 2 * h * w + 8 + 6 * (w - 2) + 6 * (h - 2) + 4 * (w - 2) * (h - 2);
		// The height of the A matrix - The number of basic variables
		int m = n + h * w;
		
		// Initialize A, b, c
		System.out.println(m);
		System.out.println(n);
		Double[][] A = new Double[m][n];
		ArrayList<Double> b = new ArrayList<Double>(); // should have length of m
		ArrayList<Double> c = new ArrayList<Double>(); // should have length of n
		
		
		// Fill the matrix with zeroes
		for (Double[] row: A) {
			Arrays.fill(row, 0.0);
		}
				
		/*
		 * Initialize the b vector with all zeroes. Later we will add ones and lambdas
		 */
		for (int i = 0; i < m; i++) {
			b.add(0.0);
		}
		// All the fp terms have a constant of lambda -  They live in the index range
		// of 2hw and n
		for (int i = 2 * w * h; i < n; i++) {
			b.set(i, lambda);			
		}
		// The flow constraints live in the indices afterwards, all of which have constant of zero
		for (int j = n; j < m; j++) {
			b.set(j, 0.0);
		}

		/*
		 * Setting the cells for the fd terms in A and b.
		 * fd(x, d) gets the weighted distance of the pixel and either of 
		 * yellow and blue, then sets the values in the relevant positions.
		 */
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Pixel pixel = imagePixels[y][x];
				int position = getLinearPosition(y, x, w);
				Double blueDataTerm = fd(1.0, pixel);
				Double yellowDataTerm = fd(0.0, pixel);
				A[position][position] = 1.0;
				b.set(position, blueDataTerm);
				A[position + h*w][position + h*w] = 1.0;
				b.set(position + h*w,  yellowDataTerm);
			}
		}
		// The diagonals of the section of A corresponding to the fp terms are all lambda
		// They live in the range 2 * h * w to n
		for (int i = 2 * h * w; i < n; i++) {
			A[i][i] = 1.0;
		}
		
		/*
		 * c, the vector for the objective function, is easy to generate. The first h * w terms are 1,
		 * followed by zeroes
		 */
		for (int i = 0; i < h * w; i++) {
			c.add(1.0);
		}
		for (int j = h * w; j < n; j++) {
			c.add(0.0);
		}
		
		/*
		 * This step is necessary for getting the nth linear position of an fp edge
		 * We have a vector of length h * w, with each index corresponding to each pixel
		 * The value of each location denotes the number of edges we have.
		 */
		int[] pixelEdges = new int[h * w];
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				Pixel pixel = imagePixels[i][j];
				switch (pixel.type) {
					case "corner":
						pixelEdges[getLinearPosition(i, j, w)] = 2;
						break;
					case "edge":
						pixelEdges[getLinearPosition(i, j, w)] = 3;
						break;
					case "middle":
						pixelEdges[getLinearPosition(i, j, w)] = 4;
						break;
					default:
						break;
				}
			}
		}
		
		/*
		 * Now calculate the coefficients in A for each flow constraint equation
		 * This starts from row n in A
		 * The y indices of the flow constraint basic variables range from n to m
		 * At first, we set the diagonals of fd(1, d) to 1, and fd(0, d) to -1
		 * for the flow constraints
		 */
		for (int i = n; i < m; i++) {
			A[i][i - n] = -1.0;
			A[i][i - n + h * w] = 1.0;
		}
		
		/*
		 * Now set the coefficients of the corresponding fp terms
		 * The challenge is getting the linear positions of each fp edge connected to each pixel
		 */
		for (int i = n; i < m; i++) {
			int pixelLinearPosition = i - n;
			int[] pixelImagePosition = getMatrixPosition(pixelLinearPosition, w);
			int y = pixelImagePosition[0]; int x = pixelImagePosition[1];
			Pixel pixel = imagePixels[y][x];
			int nOffset = 2 * h * w - 1;
			// Now for each pixel type (corner, edge, middle), get the coordinates of all fp edges
			if (pixel.type.equals("middle")) {
				Pixel upPixel = imagePixels[y-1][x];
				Pixel leftPixel = imagePixels[y][x-1];
				Pixel rightPixel = imagePixels[y][x+1];
				Pixel downPixel = imagePixels[y+1][x];
				// Get the incoming edge positions. They will all be assigned lambda
				int inUp = getFPLinearPosition(pixelEdges, getLinearPosition(y-1, x, w), "down", upPixel) + nOffset;
				int inLeft = getFPLinearPosition(pixelEdges, getLinearPosition(y, x-1, w), "right", leftPixel) + nOffset;
				int inRight = getFPLinearPosition(pixelEdges, getLinearPosition(y, x+1, w), "left", rightPixel) + nOffset;
				int inDown = getFPLinearPosition(pixelEdges, getLinearPosition(y+1, x, w), "up", downPixel) + nOffset;
				
				A[i][inUp] = 1.0;
				A[i][inLeft] = 1.0;
				A[i][inRight] = 1.0;
				A[i][inDown] = 1.0;
				
				// Get the outgoing edge positions. They will all be assigned negative lambda
				int outUp = getFPLinearPosition(pixelEdges, pixelLinearPosition, "up", pixel) + nOffset;
				int outLeft = getFPLinearPosition(pixelEdges, pixelLinearPosition, "left", pixel) + nOffset;
				int outRight = getFPLinearPosition(pixelEdges, pixelLinearPosition, "right", pixel) + nOffset;
				int outDown = getFPLinearPosition(pixelEdges, pixelLinearPosition, "down", pixel) + nOffset;
				A[i][outUp] = -1.0;
				A[i][outLeft] = -1.0;
				A[i][outRight] = -1.0;
				A[i][outDown] = -1.0;
				continue;
			} else if (pixel.type.equals("edge")) {
				if (pixel.up == null) {
					Pixel leftPixel = imagePixels[y][x-1];
					Pixel rightPixel = imagePixels[y][x+1];
					Pixel downPixel = imagePixels[y+1][x];
					// Get the incoming edge positions. They will all be assigned lambda
					int inLeft = getFPLinearPosition(pixelEdges, getLinearPosition(y, x-1, w), "right", leftPixel) + nOffset;
					int inRight = getFPLinearPosition(pixelEdges, getLinearPosition(y, x+1, w), "left", rightPixel) + nOffset;
					int inDown = getFPLinearPosition(pixelEdges, getLinearPosition(y+1, x, w), "up", downPixel) + nOffset;
					A[i][inLeft] = 1.0;
					A[i][inRight] = 1.0;
					A[i][inDown] = 1.0;
					
					// Get the outgoing edge positions. They will all be assigned negative lambda
					int outLeft = getFPLinearPosition(pixelEdges, pixelLinearPosition, "left", pixel) + nOffset;
					int outRight = getFPLinearPosition(pixelEdges, pixelLinearPosition, "right", pixel) + nOffset;
					int outDown = getFPLinearPosition(pixelEdges, pixelLinearPosition, "down", pixel) + nOffset;
					A[i][outLeft] = -1.0;
					A[i][outRight] = -1.0;
					A[i][outDown] = -1.0;
					continue;
				} else if (pixel.left == null) {
					Pixel upPixel = imagePixels[y-1][x];
					Pixel rightPixel = imagePixels[y][x+1];
					Pixel downPixel = imagePixels[y+1][x];
					// Get the incoming edge positions. They will all be assigned lambda
					int inUp = getFPLinearPosition(pixelEdges, getLinearPosition(y-1, x, w), "down", upPixel) + nOffset;
					int inRight = getFPLinearPosition(pixelEdges, getLinearPosition(y, x+1, w), "left", rightPixel) + nOffset;
					int inDown = getFPLinearPosition(pixelEdges, getLinearPosition(y+1, x, w), "up", downPixel) + nOffset;
					
					A[i][inUp] = 1.0;
					A[i][inRight] = 1.0;
					A[i][inDown] = 1.0;
					
					// Get the outgoing edge positions. They will all be assigned negative lambda
					int outUp = getFPLinearPosition(pixelEdges, pixelLinearPosition, "up", pixel) + nOffset;
					int outRight = getFPLinearPosition(pixelEdges, pixelLinearPosition, "right", pixel) + nOffset;
					int outDown = getFPLinearPosition(pixelEdges, pixelLinearPosition, "down", pixel) + nOffset;
					A[i][outUp] = -1.0;
					A[i][outRight] = -1.0;
					A[i][outDown] = -1.0;
					continue;
				} else if (pixel.right == null) {
					Pixel leftPixel = imagePixels[y][x-1];
					Pixel upPixel = imagePixels[y-1][x];
					Pixel downPixel = imagePixels[y+1][x];
					// Get the incoming edge positions. They will all be assigned lambda
					int inUp = getFPLinearPosition(pixelEdges, getLinearPosition(y-1, x, w), "down", upPixel) + nOffset;
					int inLeft = getFPLinearPosition(pixelEdges, getLinearPosition(y, x-1, w), "right", leftPixel) + nOffset;
					int inDown = getFPLinearPosition(pixelEdges, getLinearPosition(y+1, x, w), "up", downPixel) + nOffset;
					
					A[i][inUp] = 1.0;
					A[i][inLeft] = 1.0;
					A[i][inDown] = 1.0;
					
					// Get the outgoing edge positions. They will all be assigned negative lambda
					int outUp = getFPLinearPosition(pixelEdges, pixelLinearPosition, "up", pixel) + nOffset;
					int outLeft = getFPLinearPosition(pixelEdges, pixelLinearPosition, "left", pixel) + nOffset;
					int outDown = getFPLinearPosition(pixelEdges, pixelLinearPosition, "down", pixel) + nOffset;
					A[i][outUp] = -1.0;
					A[i][outLeft] = -1.0;
					A[i][outDown] = -1.0;
					continue;
				} else { // down is null
					Pixel leftPixel = imagePixels[y][x-1];
					Pixel upPixel = imagePixels[y-1][x];
					Pixel rightPixel = imagePixels[y][x+1];
					// Get the incoming edge positions. They will all be assigned lambda
					int inUp = getFPLinearPosition(pixelEdges, getLinearPosition(y-1, x, w), "down", upPixel) + nOffset;
					int inLeft = getFPLinearPosition(pixelEdges, getLinearPosition(y, x-1, w), "right", leftPixel) + nOffset;
					int inRight = getFPLinearPosition(pixelEdges, getLinearPosition(y, x+1, w), "left", rightPixel) + nOffset;
					
					A[i][inUp] = 1.0;
					A[i][inLeft] = 1.0;
					A[i][inRight] = 1.0;
					
					// Get the outgoing edge positions. They will all be assigned negative lambda
					int outUp = getFPLinearPosition(pixelEdges, pixelLinearPosition, "up", pixel) + nOffset;
					int outLeft = getFPLinearPosition(pixelEdges, pixelLinearPosition, "left", pixel) + nOffset;
					int outRight = getFPLinearPosition(pixelEdges, pixelLinearPosition, "right", pixel) + nOffset;

					A[i][outUp] = -1.0;
					A[i][outLeft] = -1.0;
					A[i][outRight] = -1.0;
					continue;
				}
			} else { // it's a corner node
				if (pixel.up == null) {
					if (pixel.left == null) {
						Pixel rightPixel = imagePixels[y][x+1];
						Pixel downPixel = imagePixels[y+1][x];
						// Get the incoming edge positions. They will all be assigned lambda
						int inRight = getFPLinearPosition(pixelEdges, getLinearPosition(y, x+1, w), "left", rightPixel) + nOffset;
						int inDown = getFPLinearPosition(pixelEdges, getLinearPosition(y+1, x, w), "up", downPixel) + nOffset;
						
						A[i][inRight] = 1.0;
						A[i][inDown] = 1.0;
						
						// Get the outgoing edge positions. They will all be assigned negative lambda
						int outRight = getFPLinearPosition(pixelEdges, pixelLinearPosition, "right", pixel) + nOffset;
						int outDown = getFPLinearPosition(pixelEdges, pixelLinearPosition, "down", pixel) + nOffset;

						A[i][outRight] = -1.0;
						A[i][outDown] = -1.0;
						continue;

					} else { // right is null
						Pixel leftPixel = imagePixels[y][x-1];
						Pixel downPixel = imagePixels[y+1][x];
						// Get the incoming edge positions. They will all be assigned lambda
						int inLeft = getFPLinearPosition(pixelEdges, getLinearPosition(y, x-1, w), "right", leftPixel) + nOffset;
						int inDown = getFPLinearPosition(pixelEdges, getLinearPosition(y+1, x, w), "up", downPixel) + nOffset;
						
						A[i][inLeft] = 1.0;
						A[i][inDown] = 1.0;
						
						// Get the outgoing edge positions. They will all be assigned negative lambda
						int outLeft = getFPLinearPosition(pixelEdges, pixelLinearPosition, "left", pixel) + nOffset;
						int outDown = getFPLinearPosition(pixelEdges, pixelLinearPosition, "down", pixel) + nOffset;
						A[i][outLeft] = -1.0;
						A[i][outDown] = -1.0;
						continue;
						
					}
				} else if (pixel.down == null) {
					if (pixel.left == null) {
						Pixel upPixel = imagePixels[y-1][x];
						Pixel rightPixel = imagePixels[y][x+1];
						// Get the incoming edge positions. They will all be assigned lambda
						int inUp = getFPLinearPosition(pixelEdges, getLinearPosition(y-1, x, w), "down", upPixel) + nOffset;
						int inRight = getFPLinearPosition(pixelEdges, getLinearPosition(y, x+1, w), "left", rightPixel) + nOffset;
						
						A[i][inUp] = 1.0;
						A[i][inRight] = 1.0;
						
						// Get the outgoing edge positions. They will all be assigned negative lambda
						int outUp = getFPLinearPosition(pixelEdges, pixelLinearPosition, "up", pixel) + nOffset;
						int outRight = getFPLinearPosition(pixelEdges, pixelLinearPosition, "right", pixel) + nOffset;
						A[i][outUp] = -1.0;
						A[i][outRight] = -1.0;
						continue;

					} else { // right is null
						Pixel leftPixel = imagePixels[y][x-1];
						Pixel upPixel = imagePixels[y-1][x];
						// Get the incoming edge positions. They will all be assigned lambda
						int inUp = getFPLinearPosition(pixelEdges, getLinearPosition(y, x-1, w), "down", upPixel) + nOffset;
						int inLeft = getFPLinearPosition(pixelEdges, getLinearPosition(y-1, x, w), "right", leftPixel) + nOffset;
						
						A[i][inUp] = 1.0;
						A[i][inLeft] = 1.0;
						
						// Get the outgoing edge positions. They will all be assigned negative lambda
						int outUp = getFPLinearPosition(pixelEdges, pixelLinearPosition, "up", pixel) + nOffset;
						int outLeft = getFPLinearPosition(pixelEdges, pixelLinearPosition, "left", pixel) + nOffset;
						A[i][outUp] = -1.0;
						A[i][outLeft] = -1.0;
						continue;
					}
				}
			}
		}
		
//		solver.print2DArray(A);
//		System.out.println(b);
//		System.out.println(c);
		
		Double[] results = solver.Simplex(A, b, c);
		Double[] truncatedResults = Arrays.copyOfRange(results, 0, 2 * h * w);
		
		
		Double[] maxCapacity = new Double[2 * h * w];
		Double[] residual = new Double[2 * h * w];
		
		for (int i = 0; i < 2 * h * w; i++) {
			maxCapacity[i] = b.get(i);
		}
		
		// Now subtract to get the residuals
		for (int j = 0; j < 2 * h * w; j++) {
			residual[j] = maxCapacity[j] - truncatedResults[j];
		}
		
		System.out.println(Arrays.asList(truncatedResults));
		System.out.println(Arrays.asList(maxCapacity));
		System.out.println(Arrays.asList(residual));
		
		Double[][] result = new Double[h][w];
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int sIndex = getLinearPosition(i, j, w);
				int tIndex = sIndex + h * w;
				// Case for when both residual locations are zero
				if (residual[sIndex].equals(0.0) && residual[tIndex].equals(0.0)) {
					double randInt = Math.floor(Math.random() + 0.5);
					if (randInt == 1.0) { // choose blue
						result[i][j] = 1.0;
					} else { // choose yellow
						result[i][j] = 0.0;
					}
				} else if (residual[sIndex].equals(0.0)) {
					result[i][j] = 1.0;
				} else {
					result[i][j] = 0.0;
				}
			}
		}
		
		image2.createImage(result, "result.png");
		
	}
	
}

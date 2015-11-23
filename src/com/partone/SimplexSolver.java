package com.partone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Simplex solver for MRF Optimazation
 * 
 * This class takes the parameters required for solving a linear program using the Simplex algorithm.
 * 
 * @author SeanSaito
 * @version 1.0
 * All rights reserved.
 * 
 */

public class SimplexSolver {
	
	/**
	 * A return class for Pivot. It takes multiple arrays and Double
	 * values as input
	 * 
	 */
	public class PivotReturn {
		
		Double[][] A; // The matrix of coefficients for linear equations
		ArrayList<Double> b; // The constants in the linear equations
		ArrayList<Double> c; // The coefficients for the non basic variables
		ArrayList<Integer> N; // The non-basic variables
		ArrayList<Integer> B; // The basic variables
		Double v; // The constant in the objective value equation
		
		public PivotReturn(Double[][] aHat, ArrayList<Double> b, ArrayList<Double> c,
				ArrayList<Integer> nHat, ArrayList<Integer> bHat, Double v) {
			this.A = aHat;
			this.b = b;
			this.c = c;
			this.N = nHat;
			this.B = bHat;
			this.v = v;
		}
	}
	
	/**
	 * Pivot function.
	 * @param N 		: A list of non basic variables
	 * @param B			: A list of basic variables
	 * @param A			: The matrix with the coefficients for the equations of each basic variable
	 * @param b			: The constants in the above equations
	 * @param c			: The coefficients of the non basic variables in the objective function
	 * @param v			: The constant in the objective function
	 * @param l			: The leaving variable
	 * @param e			: The entering variable
	 * @return A new PivotReturn object with the modified versions of the above parameters.
	 */
	
	public PivotReturn pivot(ArrayList<Integer> N, ArrayList<Integer> B, Double[][] A,
			ArrayList<Double> b, ArrayList<Double> c, Double v, Integer l, Integer e) {
		int m = B.size();
		int n = N.size();
		
		Double[][] AHat = new Double[m][n];
		// Initialize the matrix with zeroes
		for (Double[] row: AHat) {
			Arrays.fill(row,  0.0);
		}
		
		ArrayList<Double> bHat = new ArrayList<Double>();
		for (Double x: b) {
			bHat.add(x);
		}
		ArrayList<Double> cHat = new ArrayList<Double>();
		for (Double y: c) {
			cHat.add(y);
		}

		int oldLIndex = B.indexOf(l);
		int oldEIndex = N.indexOf(e);
		
		/*
		 * The difference between this implementation and Cormen's is that we calculate
		 * BHat and NHat now rather than later to get the new indices of each variable
		 */
		
		// Compute the new sets of basic and nonbasic variables
		ArrayList<Integer> NHat = new ArrayList<Integer>();
		for (Integer k: N) {
			NHat.add(k);
		}
		NHat.remove(e);
		NHat.add(l);
//		Collections.sort(NHat);
		
		ArrayList<Integer> BHat = new ArrayList<Integer>();
		for (Integer o: B) {
			BHat.add(o);
		}
		BHat.remove(l);
		BHat.add(e);
//		Collections.sort(BHat);
		
		int newLIndex = NHat.indexOf(l);
		int newEIndex = BHat.indexOf(e);
				
		// Line 3
		bHat.set(newEIndex, b.get(oldLIndex)/A[oldLIndex][oldEIndex]);
		
		// Lines 4-6
		for (Integer i: N) {
			if (!i.equals(e)) {
				int iIndex = NHat.indexOf(i);
				int oldIIndex = N.indexOf(i);
				AHat[newEIndex][iIndex] = A[oldLIndex][oldIIndex] / A[oldLIndex][oldEIndex];
			}
		}
		AHat[newEIndex][newLIndex] = 1.0 / A[oldLIndex][oldEIndex];
		
		// Lines 8-12
		for (Integer i: B) {
			if (!i.equals(l)) {
				int iIndex = BHat.indexOf(i);
				int oldIIndex = B.indexOf(i);
				bHat.set(iIndex, b.get(oldIIndex) - A[oldIIndex][oldEIndex] * bHat.get(newEIndex));
				for (Integer j: N) {
					if (!j.equals(e)) {
						int jIndex = NHat.indexOf(j);
						int oldJIndex = N.indexOf(j);
						AHat[iIndex][jIndex] = A[oldIIndex][oldJIndex] - A[oldIIndex][oldEIndex] * AHat[newEIndex][jIndex];
					} 
				}
				// Line 12
				AHat[iIndex][newLIndex] = -1 * A[oldIIndex][oldEIndex] * AHat[newEIndex][newLIndex];
			}
		}
		
		// Line 14
		Double vHat = v + c.get(oldEIndex) * bHat.get(newEIndex);
		
		// Lines 15-17
		for (Integer j: N) {
			if (!j.equals(e)) {
				int jIndex = NHat.indexOf(j);
				int oldJIndex = N.indexOf(j);
				cHat.set(jIndex, c.get(oldJIndex) - c.get(oldEIndex) * AHat[newEIndex][jIndex]);
			}
		}
		cHat.set(newLIndex, -1 * c.get(oldEIndex) * AHat[newEIndex][newLIndex]);
		
		PivotReturn toReturn = new PivotReturn(AHat, bHat, cHat, NHat, BHat, vHat);
		printDebug(toReturn);
		return toReturn;
	}
	
	/**
	 * Function that initializes a Simplex.
	 * 
	 * N.size will be the number of consecutive zeroes in b.
	 * B.size will be the number of the rest.
	 * Obviously, b.size = N.size + B.size
	 * 
	 * For example:
	 * b = [1, 2, 0, 4]
	 * 
	 * Then
	 * N = [1,2,3,4] 	: Non-basic variables
	 * B = [5,6,7,8]	: Basic variables
	 * 
	 * @param A			: The matrix with coefficients for the basic equations
	 * @param b			: Constants of the basic equations
	 * @param c			: The coefficients of the non basic variables in the objective function
	 */
	public PivotReturn InitializeSimplex(Double[][] A, ArrayList<Double> b, ArrayList<Double>c) {
		ArrayList<Integer> B = new ArrayList<Integer>();
		ArrayList<Integer> N = new ArrayList<Integer>();
		Double v = 0.0;
		
		int numRows = A.length; // Number of rows correspond to the number of basic variables and thus is length of B
		int numCols = A[0].length; // Length of N
		
		int variableCount = 0;
		for (int i = 0; i < numCols; i++) {
			N.add(variableCount);
			variableCount++;
		}
		for (int j = 0; j < numRows; j++) {
			B.add(variableCount);
			variableCount++;
		}
		
		PivotReturn toReturn = new PivotReturn(A, b, c, N, B, v);
		printDebug(toReturn);
		return toReturn;		
	}
	
	/**
	 * Prints the attributes of a PivotReturn object
	 * @param pivotObject
	 */
	public void printDebug(PivotReturn pivotObject) {
		// Debug code
		print2DArray(pivotObject.A);
		System.out.println("N: " + Arrays.asList(pivotObject.N));
		System.out.println("B: " + Arrays.asList(pivotObject.B));
		System.out.println("b: " + Arrays.asList(pivotObject.b));
		System.out.println("c: " + Arrays.asList(pivotObject.c));
		System.out.println("v: " + pivotObject.v);		
	}
	
	/**
	 * The Simplex algorithm.
	 * The function outputs the solution to the linear program as a list of values for 
	 * each variable. For example, with an initial N = [0, 1, 2] and result of 
	 * [10, 20, 0, 4, 0, 40],
	 * Then the linear program is optimized when x_0 = 10, x_1 = 20, and x_2 = 0. 
	 * 
	 * @param A			: The matrix with the coefficients for the non basic variables
	 * 					  for each basic variable equation
	 * @param b			: The constants in the equations
	 * @param c			: The coefficients of the non basic variables in the objective
	 * 					  function
	 * @return	A list of solutions with the indices corresponding to the variables
	 */
	public Double[] Simplex(Double[][] A, ArrayList<Double> b, ArrayList<Double> c) {
		PivotReturn pivotObject = InitializeSimplex(A, b, c);
		int numVariables = pivotObject.B.size() + pivotObject.N.size();
		int n = pivotObject.B.size();
		
		Double[] d = new Double[n];
		
		int iteration = 0;
		while (true) {
			if (!hasPositiveVariable(pivotObject.N, pivotObject.c)) {
				break;
			}
			
			// Choose the entering variable
			System.out.println("Iteration: " + iteration);
			
			Integer e = choosePositiveVariable(pivotObject.N, c);
			int eIndex = pivotObject.N.indexOf(e);
			System.out.println("Entering variable: " + e + " at index: " + eIndex);
			
			for (Integer i: pivotObject.B) {
				int iIndex = pivotObject.B.indexOf(i);
				if (pivotObject.A[iIndex][eIndex] > 0) {
					d[iIndex] = pivotObject.b.get(iIndex) / pivotObject.A[iIndex][eIndex];
				} else {
					d[iIndex] = Double.MAX_VALUE;
				}
			}
			
			// Now find the smallest value in d among indices of B. 
			// The index becomes our leaving variable.			
			Integer l = chooseLeavingVariable(pivotObject.B, d);
			int lIndex = pivotObject.B.indexOf(l);
			System.out.println("Leaving variable: " + l + " at index: " + lIndex);
			
			if (Double.compare(d[lIndex], Double.MAX_VALUE) == 0) {
				return null;
			} else {
				pivotObject = pivot(pivotObject.N, pivotObject.B, pivotObject.A,
						pivotObject.b, pivotObject.c, pivotObject.v, l, e);
			}
			iteration++;
		}
		
		System.out.println("Number of iterations: " + iteration);
		
		
		Double[] toReturn =  new Double[numVariables];
		
		for (Integer i = 0; i < numVariables; i++) {
			if (pivotObject.B.contains(i)) {
				toReturn[i] = pivotObject.b.get(pivotObject.B.indexOf(i));
			} else {
				toReturn[i] = 0.0;
			}
		}
		
		return toReturn;
	}
	
	private Integer chooseLeavingVariable(ArrayList<Integer> b, Double[] d) {
		Double minValue = Double.MAX_VALUE;
		Integer minIndex = 0;
		
		for (Integer i: b) {
			int iIndex = b.indexOf(i);
			if (d[iIndex] < minValue) {
				minValue = d[iIndex];
				minIndex = i;
			}
		}
		return minIndex;
	}

	private Integer choosePositiveVariable(ArrayList<Integer> n, ArrayList<Double> c) {
		for (Integer e: n) {
			int eIndex = n.indexOf(e);
			if (c.get(eIndex) > 0) {
				return e;
			}
		}
		return null;
	}

	/*
	 * Checks to see whether any variable in N has a positive constant.
	 */
	private boolean hasPositiveVariable(ArrayList<Integer> n, ArrayList<Double> c) {
		for (Integer i: n) {
			int iIndex = n.indexOf(i);
			if (c.get(iIndex) > 0.0) {
				return true;
			}
		}
		return false;
	}
	
	public void print2DArray(Double[][] array) {
		int numCols = array[0].length;
		int numRows = array.length;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				System.out.print(array[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		// Sample inputs
		Double[][] A = new Double[8][6];
		ArrayList<Double> b = new ArrayList<Double>();
		ArrayList<Double> c = new ArrayList<Double>();

		// Initialize the matrix with zeroes
		for (Double[] row: A) {
			Arrays.fill(row,  0.0);
		}
		
		SimplexSolver solver = new SimplexSolver();
				
		A[0][0] = 1.0;
		A[1][1] = 1.0;
		A[2][2] = 1.0;
		A[3][3] = 1.0;
		A[4][4] = 1.0;
		A[5][5] = 1.0;
		A[6][0] = -1.0;
		A[6][2] = 1.0;
		A[6][3] = -1.0;
		A[6][4] = 1.0;
		A[7][1] = 1.0;
		A[7][2] = 1.0;
		A[7][3] = -1.0;
		A[7][5] = -1.0;
		
		b.addAll(Arrays.asList(2.0, 9.0, 1.0, 2.0, 5.0, 4.0, 0.0, 0.0));
		c.addAll(Arrays.asList(1.0, 1.0, 0.0, 0.0, 0.0, 0.0));
		
//		A[0][0] = 1.0;
//		A[0][1] = 1.0;
//		A[0][2] = 3.0;
//		A[1][0] = 2.0;
//		A[1][1] = 2.0;
//		A[1][2] = 5.0;
//		A[2][0] = 4.0;
//		A[2][1] = 1.0;
//		A[2][2] = 2.0;
//		
//		b.addAll(Arrays.asList(30.0, 24.0, 36.0));
//		c.addAll(Arrays.asList(3.0, 1.0, 2.0));
		
		Double[] results = solver.Simplex(A, b, c);
		System.out.println(Arrays.asList(results));
		
	}
	
}

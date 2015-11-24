package com.partone;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class image2 {
	
	public static void printPixelARGB(int pixel) {
	    //int alpha = (pixel >> 24) & 0xff;
	    int red = (pixel >> 16) & 0xff;
	    int green = (pixel >> 8) & 0xff;
	    int blue = (pixel) & 0xff;
	    //System.out.println("argb: " + alpha + ", " + red + ", " + green + ", " + blue);
	    System.out.println(red + ", " + green + ", " + blue);
	}
	
	public BufferedImage getImage(String s){
		BufferedImage image = null;
		try{
			image = ImageIO.read(this.getClass().getResource(s));
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		return image;
	}
	
	public static void write(String s, BufferedImage image){
		try{
			File f = new File(s);
			ImageIO.write(image, "PNG", f);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void createImage(Double[][] pixels, String s){
		BufferedImage imout = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_INT_RGB);
		int w = imout.getWidth();
		int h = imout.getHeight();
		System.out.println("width, height: " + w + ", " + h);
		Color blue = Color.blue;
		Color yellow = Color.yellow;
		
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				if (pixels[i][j].equals(1.0)) {
					imout.setRGB(j, i, blue.getRGB());
				} else {
					imout.setRGB(j, i, yellow.getRGB());
				}
		    }
		}
		
		write(s, imout);
	}
	
//	public static void main(String[] foo) {
//
//		image2 obj = new image2();
//		String s = "BBYB.png";
//		BufferedImage image = obj.getImage(s);
//		int w = image.getWidth();
//		int h = image.getHeight();
//		//System.out.println("width, height: " + w + ", " + h);
//		double[][] fds = new double[h][w];
//		double[][] fdt = new double[h][w];
//		
//		for (int i = 0; i < h; i++) {
//			for (int j = 0; j < w; j++) {
//				System.out.println("x,y: " + j + ", " + i);
//		        int pixel = image.getRGB(j, i);
//		        //printPixelARGB(pixel);
//		        
//		        int red = (pixel >> 16) & 0xff;
//		        int green = (pixel >> 8) & 0xff;
//		        int blue = (pixel) & 0xff;
//		        System.out.println(red + ", " + green + ", " + blue);
//			    fds[j][i] = simplexMethods.distanceFrom((double)red, (double)green, (double)blue, 
//			    		0.0, 0.0, 255.0);
//			    fdt[j][i] = simplexMethods.distanceFrom((double)red, (double)green, (double)blue, 
//			    		255.0, 255.0, 0.0);
//		    }
//		}
//		System.out.println("FDS:");
//		for (int i = 0; i < h; i++) {
//			for (int j = 0; j < w; j++) {
//				System.out.print(fds[j][i]+" ");
//			}
//			System.out.println("");
//		}
//		
//		System.out.println("FDT:");
//		for (int i = 0; i < h; i++) {
//			for (int j = 0; j < w; j++) {
//				System.out.print(fdt[j][i]+" ");
//			}
//			System.out.println("");
//		}
//		//createImage();
//		
//	}
}

package com.partone;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class ImageExample {
	
	private String imageFileName = "bayes_in.jpg";
	
	/*
	public static void main(String[] args) {
		ImageExample imageExample = new ImageExample();
		imageExample.parseImage();
	}
	*/
	
	public ArrayList<Double> getPixelARGB(int pixel) {
		Double alpha = (double) ((pixel >> 24) & 0xff);
		Double red = (double) ((pixel >> 16) & 0xff);
		Double green = (double) ((pixel >> 8) & 0xff);
		Double blue = (double) ((pixel) & 0xff);
		return new ArrayList<Double>(Arrays.asList(red, green, blue));
	}
	
	private void marchThroughImage(BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println("width, height: " + w + ", " + h);
		
		for (int i = 0; i < 420; i++) {
			for (int j = 0; j < w; j++) {
				System.out.println("x,y: " + j + ", " + i);
				int pixel = image.getRGB(j, i);
				ArrayList<Double> rgb = getPixelARGB(pixel);
				System.out.print("rgb: " + rgb.get(0)  + ", " + rgb.get(1) + ", " + rgb.get(2));
				System.out.println("");
			}
		}
	}
	
	public void parseImage() {
		try {
			// get the BufferedImage, using the ImageIO class
			BufferedImage image = 
					ImageIO.read(this.getClass().getResource(imageFileName));
			marchThroughImage(image);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public BufferedImage getImage() {
		try {
			BufferedImage image =
					ImageIO.read(this.getClass().getResourceAsStream(imageFileName));
			return image;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
	
}

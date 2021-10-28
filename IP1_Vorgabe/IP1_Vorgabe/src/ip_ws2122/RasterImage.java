// IP Ue1 2021/22 Vorgabe
//
// Copyright (C) 2020 by Klaus Jung
// All rights reserved.
// Date: 2020-10-02

package ip_ws2122;

import java.io.File;
import java.util.Arrays;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class RasterImage {

	private static final int gray = 0xffa0a0a0;

	public int[] argb; // pixels represented as ARGB values in scanline order
	public int width; // image width in pixels
	public int height; // image height in pixels

	public RasterImage(int width, int height) {
		// creates an empty RasterImage of given size
		this.width = width;
		this.height = height;
		argb = new int[width * height];
		Arrays.fill(argb, gray);
	}

	public RasterImage(RasterImage image) {
		// copy constructor
		width = image.width;
		height = image.height;
		argb = image.argb.clone();
	}

	public RasterImage(File file) {
		// creates an RasterImage by reading the given file
		Image image = null;
		if (file != null && file.exists()) {
			image = new Image(file.toURI().toString());
		}
		if (image != null && image.getPixelReader() != null) {
			width = (int) image.getWidth();
			height = (int) image.getHeight();
			argb = new int[width * height];
			image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
		} else {
			// file reading failed: create an empty RasterImage
			this.width = 256;
			this.height = 256;
			argb = new int[width * height];
			Arrays.fill(argb, gray);
		}
	}

	public RasterImage(ImageView imageView) {
		// creates a RasterImage from that what is shown in the given ImageView
		Image image = imageView.getImage();
		width = (int) image.getWidth();
		height = (int) image.getHeight();
		argb = new int[width * height];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
	}

	public void setToView(ImageView imageView) {
		// sets the current argb pixels to be shown in the given ImageView
		if (argb != null) {
			WritableImage wr = new WritableImage(width, height);
			PixelWriter pw = wr.getPixelWriter();
			pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
			imageView.setImage(wr);
		}
	}

	// image point operations to be added here

	public void binarizeWithThreshold(int threshold) {
		// TODO: binarize the image with the given threshold
		int[] newArr = new int[this.argb.length];

		for (int i = 0; i < this.argb.length; i++) {
			int currentArgb = this.argb[i];
			int alpha = (currentArgb >> 24) & 0xff;
			int red = (currentArgb >> 16) & 0xff;
			int green = (currentArgb >> 8) & 0xff;
			int blue = currentArgb & 0xff;
			int greyScale = (red + green + blue) / 3;
			if (greyScale < threshold) {
				this.argb[i] = (alpha << 24) | (0 << 16) | (0 << 8) | 0;
			} else {	

				this.argb[i] = (alpha << 24) | (255 << 16) | (255 << 8) | 255;
			}
		}
	}

	/**
	 * 
	 * @return the threshold computed by iso-data
	 */
	public int binarizeWithIsoData() {
		int threshold = recursiveISOData(128);
		binarizeWithThreshold(threshold);
		// TODO: binarize the image using the iso-data algorithm
//		Arrays.fill(argb, gray);
		return threshold; // TODO: return the computed threshold
	}
	
	private int recursiveISOData(int threshold) {
//		int threshold = 128;
		double[] probGrey= new double[256];
		double pA =0;
		double schwerpunktA = 0;
		double sumSchwerpunktA = 0;
		
		double pB =0;
		double schwerpunktB= 0;
		double sumSchwerpunktB = 0;
		
		int newThreshold = 0;
		// get the probablity of greyscale
		for (int j = 0; j < this.argb.length; j++) {
			int currentArgb = this.argb[j];
			int alpha = (currentArgb >> 24) & 0xff;
			int red = (currentArgb >> 16) & 0xff;
			int green = (currentArgb >> 8) & 0xff;
			int blue = currentArgb & 0xff;
			int greyScale = (red + green + blue) / 3;
			probGrey[greyScale]+= 1.0 /  this.argb.length;
		}
		
		for(int j=0; j < 256; j++) {
			if(j < threshold) {
				pA += probGrey[j];
			}else {
				pB += probGrey[j];
			}
		}
		
		for(int j=0; j < 256; j++) {
			if(j < threshold) {
				sumSchwerpunktA += j * probGrey[j];
			}else {
				sumSchwerpunktB += j* probGrey[j];
			}
		}
		schwerpunktA = (1/pA) * sumSchwerpunktA;
		schwerpunktB = (1/pB) * sumSchwerpunktB;
		
		newThreshold = (int) (schwerpunktA + schwerpunktB) / 2;
		
		if(threshold == newThreshold) {
			return threshold;
		}
		else{
			newThreshold = recursiveISOData(newThreshold);
		}
		System.out.println(newThreshold);
		return newThreshold;
	}
	

}

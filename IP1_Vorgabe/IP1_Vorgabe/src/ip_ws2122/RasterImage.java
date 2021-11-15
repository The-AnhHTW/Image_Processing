// IP Ue1 2021/22 Vorgabe
//
// Copyright (C) 2020 by Klaus Jung
// All rights reserved.
// Date: 2020-10-02

package ip_ws2122;

import java.awt.Color;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class RasterImage {

	private static final int gray = 0xffa0a0a0;

	private static List<Integer[]> colors = new ArrayList<Integer[]>() {
		{
			add(new Integer[] { 255, 242, 117 });
			add(new Integer[] { 255, 140, 66 });
			add(new Integer[] { 255, 60, 56 });

			add(new Integer[] { 162, 62, 72 });
			add(new Integer[] { 108, 142, 173 });
			add(new Integer[] { 74, 111, 165 });

			add(new Integer[] { 110, 136, 148 });
			add(new Integer[] { 133, 186, 161 });
			add(new Integer[] { 206, 237, 219 });
		}
	};

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
		for (int i = 0; i < this.argb.length; i++) {
			int currentArgb = this.argb[i];
			int alpha = (currentArgb >> 24) & 0xff;
			int red = (currentArgb >> 16) & 0xff;
			int green = (currentArgb >> 8) & 0xff;
			int blue = currentArgb & 0xff;
			int greyScale = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
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
		double[] probGrey = new double[256];
		double pA = 0;
		double schwerpunktA = 0;
		double sumSchwerpunktA = 0;

		double pB = 0;
		double schwerpunktB = 0;
		double sumSchwerpunktB = 0;

		int newThreshold = 0;
		// get the probablity of greyscale
		for (int j = 0; j < this.argb.length; j++) {
			int currentArgb = this.argb[j];
			int red = (currentArgb >> 16) & 0xff;
			int green = (currentArgb >> 8) & 0xff;
			int blue = currentArgb & 0xff;
			int greyScale = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
			probGrey[greyScale] += 1.0 / this.argb.length;
		}

		for (int j = 0; j < 256; j++) {
			if (j < threshold) {
				pA += probGrey[j];
			} else {
				pB += probGrey[j];
			}
		}

		for (int j = 0; j < 256; j++) {
			if (j < threshold) {
				sumSchwerpunktA += j * probGrey[j];
			} else {
				sumSchwerpunktB += j * probGrey[j];
			}
		}
		schwerpunktA = (1 / pA) * sumSchwerpunktA;
		schwerpunktB = (1 / pB) * sumSchwerpunktB;

		newThreshold = (int) (schwerpunktA + schwerpunktB) / 2;

		if (threshold == newThreshold) {
			return threshold;
		} else {
			newThreshold = recursiveISOData(newThreshold);
		}
		return newThreshold;
	}

	public void floodFill() {
		for (int i = 0; i < this.argb.length; i++) {
			int currentArgb = this.argb[i];
			int srcRed = (currentArgb >> 16) & 0xff;
			int x = i % this.width;
			int y = i / this.width;

			if (srcRed == 0) {
				Integer[] coordinates = { x, y };
				Integer[] color = colors.get(0);
				colors.add(color);
				colors.remove(0);
				breadthFirstFloodFilling(coordinates, color);
			}
		}
	}

	public void sequentialLabeling() {
		Set<int[]> set = new HashSet<int[]>();
		Integer[] color = colors.get(0);
		for (int i = 0; i < this.argb.length; i++) {
			int currentArgb = this.argb[i];
			int srcRed = (currentArgb >> 16) & 0xff;
			int alpha = (currentArgb >> 24) & 0xff;
			int x = i % this.width;
			int y = i / this.width;
			int[] neighbourIndexes = getAllNeighbours(x, y);
			int pos = y * this.width + x;

			int neighbourAmount = 8;
			int count = 0;
			int countColor = 0;
			int neighbourLabel = 0;
			HashMap<Integer, Integer> coloredNeighbours = new HashMap<Integer, Integer>();
			for (int index : neighbourIndexes) {
				if (index > 0 && index < this.argb.length) {
					int neighbourARGB = this.argb[index];
					int neighbourAlhpa = (neighbourARGB >> 24) & 0xff;
					int neighbourRed = (neighbourARGB >> 16) & 0xff;
					int neighbourGreen = (neighbourARGB >> 8) & 0xff;
					int neighbourBlue = neighbourARGB & 0xff;
					if (neighbourRed == 255 && neighbourGreen == 255 && neighbourBlue == 255) {
						count++;
					} else if (neighbourRed != 0) {
						countColor++;
						neighbourLabel = (neighbourAlhpa & 0xff) << 24 | (neighbourRed & 0xff) << 16
								| (neighbourGreen & 0xff) << 8 | (neighbourBlue & 0xff);
						coloredNeighbours.put(index, neighbourLabel);
					};
				} else {
					neighbourAmount--;
				}
			}

			if (neighbourAmount == count) {
				this.argb[i] = (alpha & 0xff) << 24 | (color[0] & 0xff) << 16 | (color[1] & 0xff) << 8
						| (color[2] & 0xff);
				colors.add(color);
				colors.remove(0);
			} else if (countColor == 1) {
				this.argb[i] = neighbourLabel;
			} else if (countColor > 1) {
				this.argb[i] = neighbourLabel;
				for (int key : coloredNeighbours.keySet()) {
					int specificColor = coloredNeighbours.get(key);
					int ni = this.argb[key];
					int k = specificColor;
					if(ni != k) { 
						set.add(new int[] {ni, k});
					}}
			}
		}
	}

	private int[] getAllNeighbours(int y, int x) {
		int[] neighbourIndexes = new int[] { (this.width * (x - 1) + y), (this.width * (x + 1) + y),
				(this.width * x + (y - 1)), (this.width * x + (y + 1)),

				(this.width * (x - 1) + (y + 1)), (this.width * (x - 1) + (y - 1)), (this.width * (x + 1) + (y - 1)),
				(this.width * (x + 1) + (y + 1)) };

		return neighbourIndexes;
	}

	private void depthFirstFloodFilling(Integer[] coordinate, Integer[] color) {
		Stack<Integer[]> stack = new Stack<Integer[]>();
		stack.push(coordinate);

		while (!stack.empty()) {
			Integer[] coordinates = stack.pop();
			int x = coordinates[0];
			int y = coordinates[1];
			int pos = y * this.width + x;

			if (pos > 0 && pos < this.argb.length) {
				int currentArgb = this.argb[pos];
				int red = (currentArgb >> 16) & 0xff;
				int green = (currentArgb >> 8) & 0xff;
				int blue = currentArgb & 0xff;
//				System.out.println("red: " + red);
//				System.out.println("green: " + green);
//				System.out.println("blue: " + blue);
				if (red == 0) {
//					System.out.println("red: " + red);
					int alpha = (this.argb[pos] >> 24) & 0xff;
					this.argb[pos] = (alpha & 0xff) << 24 | (color[0] & 0xff) << 16 | (color[1] & 0xff) << 8
							| (color[2] & 0xff);
					;
					stack.push(new Integer[] { x, y - 1 });//
					stack.push(new Integer[] { x - 1, y + 1 });// war falsch: x-1 y-1
					stack.push(new Integer[] { x, y + 1 });//
					stack.push(new Integer[] { x - 1, y + 1 }); //
					stack.push(new Integer[] { x - 1, y }); //
					stack.push(new Integer[] { x + 1, y }); //
					stack.push(new Integer[] { x + 1, y + 1 });//
					stack.push(new Integer[] { x - 1, y - 1 }); //
				}
			}
		}
	}

	private void breadthFirstFloodFilling(Integer[] coordinate, Integer[] color) {
		Deque<Integer[]> queue = new ArrayDeque<Integer[]>();
		queue.add(coordinate);

		while (!queue.isEmpty()) {
			Integer[] coordinates = queue.pop();

			int x = coordinates[0];
			int y = coordinates[1];
			int pos = y * this.width + x;

			if (pos > 0 && pos < this.argb.length) {
				int currentArgb = this.argb[pos];
				int red = (currentArgb >> 16) & 0xff;
				int green = (currentArgb >> 8) & 0xff;
				int blue = currentArgb & 0xff;

				if (red == 0 && green == 0 && blue == 0) {
//                    System.out.println("red: " + red);
					int alpha = (this.argb[pos] >> 24) & 0xff;
					this.argb[pos] = (alpha & 0xff) << 24 | (color[0] & 0xff) << 16 | (color[1] & 0xff) << 8
							| (color[2] & 0xff);
					;
					queue.add(new Integer[] { x, y - 1 });//
					queue.add(new Integer[] { x - 1, y + 1 });// war falsch: x-1 y-1
					queue.add(new Integer[] { x, y + 1 });//
					queue.add(new Integer[] { x - 1, y + 1 }); //
					queue.add(new Integer[] { x - 1, y }); //
					queue.add(new Integer[] { x + 1, y }); //
					queue.add(new Integer[] { x + 1, y + 1 });//
					queue.add(new Integer[] { x - 1, y - 1 }); //
				}
			}
		}
	}

}

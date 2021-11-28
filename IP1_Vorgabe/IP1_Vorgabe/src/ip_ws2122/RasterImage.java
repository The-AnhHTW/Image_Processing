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
import java.util.stream.IntStream;
import java.util.Arrays;
import java.util.Collections;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class RasterImage {

	private static final int gray = 0xffa0a0a0;
	private String lastMove = "";
	private static List<List<int[]>> pathList = new ArrayList<List<int[]>>();
	private static List<String> typeOfPaths = new ArrayList<String>();

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
	public List<Integer> deptSizes = new ArrayList<Integer>();

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

	public void findContour(String type) {
		List<int[]> path = new ArrayList<int[]>();
		boolean firstFound = true;
		for (int i = 0; i < this.argb.length; i++) {
			int currentArgb = this.argb[i];
			int srcRed = (currentArgb >> 16) & 0xff;
			int x = i % this.width;
			int y = i / this.width;

			// finde den ersten Vordergrundpixel mit vorg�nger
			if (srcRed == 0 && i != 0 && firstFound) {
				firstFound = false;
				int[] coordinates = { x, y };
				path.add(coordinates);
				path.add(moveDirection(x, y));

				while (!Arrays.equals(path.get(0), path.get(path.size() - 1))) {
					int[] currentPixel = path.get(path.size() - 1);
					int[] possibleEdgePoint = moveDirection(currentPixel[0], currentPixel[1]);
					if(getDistance(currentPixel[0], currentPixel[1], possibleEdgePoint[0], possibleEdgePoint[1]) == 1) {
						path.add(possibleEdgePoint);
					}					
				}
				
				for(int j=0; j < path.size(); i++) {
					int[] current = path.get(j);
					int[] next;
					
					if(j == path.size()) {
						break;
					}else {
						next = path.get(j+1);
					}
					
					if(next[1] != current[1]) {
						for(int l=current[0]; l <= this.width; l++) {
							int index = (current[1])* this.width + l;
							int rowArgb = this.argb[index];
							int red = (rowArgb >> 16) & 0xff;
							int alpha = (rowArgb>> 24) & 0xff;
							int color =  red== 255 ? 0: 255;
							this.argb[index] =  (alpha << 24) | (color << 16) | (color << 8) | color;
						}
					}
					
				}
				pathList.add(path);
				typeOfPaths.add(type);
				if(type == "outer") {
					this.findContour("inner");
				}
				
				
//				List<Integer> pixelboudaries = new ArrayList<Integer>();
//				//Alle Pixel im Innern invertieren
//				for (int[] obj : path) {
//					pixelboudaries.add(obj[1]*this.width + obj[0]);
//				}
//				Collections.sort(pixelboudaries);
//				int[] range = new int[] {pixelboudaries.get(0), pixelboudaries.get(pixelboudaries.size()-1)};
//				for (int z = range[0]; z <= range[1]; z++) {
//					int currentpixel= this.argb[z];
//					int alpha = (currentpixel >> 24) & 0xff;
//					int color = (currentpixel >> 16) & 0xff;
//					if (color == 255) {
//						this.argb[z] =  (alpha << 24) | (0 << 16) | (0 << 8) | 0;
//					} else if (color == 0) {
//						this.argb[z] =  (alpha << 24) | (255 << 16) | (255 << 8) | 255;
//					}
//				}
//				
				
			}
		}
//		for (int[] obj : path) {
//			System.out.println(Arrays.toString(obj));
//		}

	}

	private int[] moveDirection(int x, int y) {
		int pos = y * this.width + x;
		int srcRed;
		int topPixelLeftRed;
		int topPixelRed;
		int leftPixelRed;
		int topPixelLeft = (y + 1) * this.width + (x - 1);
		int topPixel = (y + 1) * this.width + (x);
		int leftPixel = (y) * this.width + (x - 1);

		// Behandle Rand wie wei�en Pixel
		if (pos > this.argb.length - 1 || pos < 0) {
			srcRed = 255;
		} else {
			int currentArgb = this.argb[pos];
			srcRed = (currentArgb >> 16) & 0xff;
		}

		if (topPixelLeft > this.argb.length - 1 || topPixelLeft < 0) {
			topPixelLeftRed = 255;
		} else {
			int topPixelLeftArgb = this.argb[topPixelLeft];
			topPixelLeftRed = (topPixelLeftArgb >> 16) & 0xff;
		}
		if (topPixel > this.argb.length - 1 || topPixel < 0) {
			topPixelRed = 255;
		} else {
			int topPixelArgb = this.argb[topPixel];
			topPixelRed = (topPixelArgb >> 16) & 0xff;
		}
		if (leftPixel > this.argb.length - 1 || leftPixel < 0) {
			leftPixelRed = 255;
		} else {
			int leftPixelArgb = this.argb[leftPixel];
			leftPixelRed = (leftPixelArgb >> 16) & 0xff;
		}

		// check Abbiegevorschrift immer rechts
		if (this.lastMove.equals("GoBottom")) {
			// go-left
			if (topPixelLeftRed == 255 && leftPixelRed == 0) {
				lastMove = "GoLeft";
				System.out.println("Go left");
				return new int[] { x - 1, y };
			} else
				return move(x, y, srcRed, topPixelLeftRed, topPixelRed, leftPixelRed);
		} else if (this.lastMove.equals("GoRight")) {
			// go-bottom
			if (srcRed == 0 && leftPixelRed == 255) {
				lastMove = "GoBottom";
				System.out.println("Go bottom");
				return new int[] { x, y - 1 };
			} else
				return move(x, y, srcRed, topPixelLeftRed, topPixelRed, leftPixelRed);
		} else if (this.lastMove.equals("GoLeft")) {
			// go-top
			if (topPixelLeftRed == 0 && topPixelRed == 255) {
				lastMove = "GoTop";
				System.out.println("Go top");
				return new int[] { x, y + 1 };
			} else
				return move(x, y, srcRed, topPixelLeftRed, topPixelRed, leftPixelRed);
		} else if (this.lastMove.equals("GoTop")) {
			// go-right
			if (topPixelRed == 0 && srcRed == 255) {
				lastMove = "GoRight";
				System.out.println("Go right");
				return new int[] { x + 1, y };
			} else
				return move(x, y, srcRed, topPixelLeftRed, topPixelRed, leftPixelRed);
		} else {
			return move(x, y, srcRed, topPixelLeftRed, topPixelRed, leftPixelRed);
		}
	}

	private int[] move(int x, int y, int srcRed, int topPixelLeftRed, int topPixelRed, int leftPixelRed) {
		// go-top
		if (topPixelLeftRed == 0 && topPixelRed == 255) {
			lastMove = "GoTop";
			System.out.println("Go top");
			return new int[] { x, y + 1 };
		}

		// go-bottom
		if (srcRed == 0 && leftPixelRed == 255) {
			lastMove = "GoBottom";
			System.out.println("Go bottom");
			return new int[] { x, y - 1 };
		}

		// go-left
		if (topPixelLeftRed == 255 && leftPixelRed == 0) {
			lastMove = "GoLeft";
			System.out.println("Go left");
			return new int[] { x - 1, y };
		}

		// go-right
		if (topPixelRed == 0 && srcRed == 255) {
			lastMove = "GoRight";
			System.out.println("Go right");
			return new int[] { x + 1, y };
		}
		return new int[] { 0, 0 };
	}

	// Methode f�r die Berechnung der Distanz zwischen zwei Punkten.
	public static int getDistance(int xP1, int yP1, int xP2, int yP2) {
		return (int) Math.sqrt(Math.pow((xP2 - xP1), 2) + Math.pow((yP2 - yP1), 2));
	}

//	public void floodFill(String type) {
//		for (int i = 0; i < this.argb.length; i++) {
//			int currentArgb = this.argb[i];
//			int srcRed = (currentArgb >> 16) & 0xff;
//			int x = i % this.width;
//			int y = i / this.width;
//
//			if (srcRed == 0) {
//				Integer[] coordinates = { x, y };
//				Integer[] color = colors.get(0);
//				colors.add(color);
//				colors.remove(0);
//				if (type == "BFS") {
//					breadthFirstFloodFilling(coordinates, color);
//				} else if (type == "DFS") {
//					depthFirstFloodFilling(coordinates, color);
//				}
//
//			}
//		}
//
////		int highestNumber = Collections.max(deptSizes);
//
//	}

//	public void sequentialLabeling2() {
//		Integer[] color = colors.get(0);
//		HashMap<Integer, Integer> equivalent = new HashMap<Integer, Integer>();
//		for (int i = 0; i < this.argb.length; i++) {
//			int currentArgb = this.argb[i];
//			int srcRed = (currentArgb >> 16) & 0xff;
//			int alpha = (currentArgb >> 24) & 0xff;
//			int x = i % this.width;
//			int y = i / this.width;
//			int pos = y * this.width + x;
//
//			if (srcRed == 0) {
//				List<Integer> allNeighbours = getAllNeighbours2(x, y);
//				boolean allWhite = allNeighbours.stream().allMatch(I -> {
//					int neighbourArgb = this.argb[I];
//					int neighbourRed = (neighbourArgb >> 16) & 0xff;
//					int neighbourGreen = (neighbourArgb >> 8) & 0xff;
//					int neighbourBlue = neighbourArgb & 0xff;
//
//					if (neighbourRed == 255 && neighbourGreen == 255 && neighbourBlue == 255) {
//						return true;
//					}
//					return false;
//				});
//
//				boolean exactlyOne = false;
//				boolean moreThanOne = false;
//
//				List<int[]> multiLabels = new ArrayList<int[]>();
//				for (int index : allNeighbours) {
//					int neighbourArgb = this.argb[index];
//					int neighbourRed = (neighbourArgb >> 16) & 0xff;
//					if (neighbourRed > 0) {
//						multiLabels.add(new int[] { index, neighbourArgb });
//					}
//				}
//
//				if (multiLabels.size() == 1) {
//					exactlyOne = true;
//				} else if (multiLabels.size() > 0) {
//					moreThanOne = true;
//				}
//
//				if (allWhite) {
//					this.argb[pos] = (alpha & 0xff) << 24 | (color[0] & 0xff) << 16 | (color[1] & 0xff) << 8
//							| (color[2] & 0xff);
//					colors.add(color);
//					colors.remove(0);
//					color = colors.get(0);
//				} else if (exactlyOne) {
//					this.argb[pos] = multiLabels.get(0)[1];
//				} else if (moreThanOne) {
//					int randomIndex = (int) (Math.random() * (multiLabels.size() - 1) + 1);
//					int[] keyValue = multiLabels.get(randomIndex);
//					int k = keyValue[1];
//					int kRed = (k >> 16) & 0xff;
//					this.argb[keyValue[0]] = keyValue[1];
//					for (int[] neighbourKeyValue : multiLabels) {
//						int neighbourArgb = this.argb[neighbourKeyValue[0]];
//						int neighbourRed = (neighbourArgb >> 16) & 0xff;
//						if (neighbourRed != kRed) {
//							equivalent.put(neighbourArgb, k);
//						}
//					}
//				}
//			}
//
//		}
//		resolveCollision(equivalent);
//	}
//
////	add(new Integer[] { 255, 242, 117 });
////	add(new Integer[] { 255, 140, 66 });
////	add(new Integer[] { 255, 60, 56 });
////
////	add(new Integer[] { 162, 62, 72 });
////	add(new Integer[] { 108, 142, 173 });
////	add(new Integer[] { 74, 111, 165 });
////
////	add(new Integer[] { 110, 136, 148 });
////	add(new Integer[] { 133, 186, 161 });
////	add(new Integer[] { 206, 237, 219 });
//
//	private void resolveCollision(HashMap<Integer, Integer> equivalent) {
//		Set<Set<Integer>> R = new HashSet<Set<Integer>>() {
//			{
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (255 & 0xff) << 16 | (242 & 0xff) << 8 | (117 & 0xff));
//					}
//				});
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (255 & 0xff) << 16 | (140 & 0xff) << 8 | (66 & 0xff));
//					}
//				});
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (255 & 0xff) << 16 | (60 & 0xff) << 8 | (56 & 0xff));
//					}
//				});
//
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (162 & 0xff) << 16 | (62 & 0xff) << 8 | (72 & 0xff));
//					}
//				});
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (108 & 0xff) << 16 | (142 & 0xff) << 8 | (173 & 0xff));
//					}
//				});
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (74 & 0xff) << 16 | (111 & 0xff) << 8 | (165 & 0xff));
//					}
//				});
//
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (110 & 0xff) << 16 | (136 & 0xff) << 8 | (148 & 0xff));
//					}
//				});
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (133 & 0xff) << 16 | (186 & 0xff) << 8 | (161 & 0xff));
//					}
//				});
//				add(new HashSet<Integer>() {
//					{
//						add((255 & 0xff) << 24 | (206 & 0xff) << 16 | (237 & 0xff) << 8 | (219 & 0xff));
//					}
//				});
//			}
//
//		};
//
//		for (int key : equivalent.keySet()) {
//			int aArgb = key;
//			int aRed = (aArgb >> 16) & 0xff;
//			int aGreen = (aArgb >> 8) & 0xff;
//			int aBlue = aArgb & 0xff;
//
//			int bArgb = equivalent.get(key);
//			int bRed = (bArgb >> 16) & 0xff;
//			int bGreen = (bArgb >> 8) & 0xff;
//			int bBlue = bArgb & 0xff;
//
//			Set<Integer> RA = new HashSet();
//			Set<Integer> RB = new HashSet();
//
//			for (Set<Integer> s : R) {
//				if (s.contains((255 & 0xff) << 24 | (aRed & 0xff) << 16 | (aGreen & 0xff) << 8 | (aBlue & 0xff))) {
//					RA = s;
//				} else if (s
//						.contains((255 & 0xff) << 24 | (bRed & 0xff) << 16 | (bGreen & 0xff) << 8 | (bBlue & 0xff))) {
//
//					RB = s;
//				}
//				;
//			}
//
//			if (!RA.equals(RB)) {
//				RA.addAll(RB);
//				RB.clear();
//			}
//		}
//
//		for (int i = 0; i < this.argb.length; i++) {
//			int aArgb = this.argb[i];
//			int aRed = (aArgb >> 16) & 0xff;
//			int alpha = (aArgb >> 24) & 0xff;
//			int aGreen = (aArgb >> 8) & 0xff;
//			int aBlue = aArgb & 0xff;
//			int x = i % this.width;
//			int y = i / this.width;
//			int pos = y * this.width + x;
//			if (aRed == 255 && aGreen == 255 && aBlue == 255) {
//			} else if (aRed > 0) {
//				for (Set<Integer> s : R) {
//					if (s.contains((255 & 0xff) << 24 | (aRed & 0xff) << 16 | (aGreen & 0xff) << 8 | (aBlue & 0xff))) {
//						int[] array = s.stream().mapToInt(Number::intValue).toArray();
//						int min = IntStream.of(array).min().orElse(Integer.MAX_VALUE);
//						int minRed = (min >> 16) & 0xff;
//						int minGreen = (min >> 8) & 0xff;
//						int minBlue = min & 0xff;
//
//						this.argb[pos] = (alpha & 0xff) << 24 | (minRed & 0xff) << 16 | (minGreen & 0xff) << 8
//								| (minBlue & 0xff);
//					}
//				}
//			}
//		}
//
//	}
//
//	private List<Integer> getAllNeighbours2(int x, int y) {
//		ArrayList<Integer> neighbourIndexes = new ArrayList<Integer>();
//		neighbourIndexes.add((this.width * (y - 1) + x));
//		neighbourIndexes.add((this.width * (y + 1) + x));
//		neighbourIndexes.add((this.width * y + (x - 1)));
//		neighbourIndexes.add((this.width * y + (x + 1)));
//
//		neighbourIndexes.add((this.width * (y - 1) + (x + 1)));
//		neighbourIndexes.add((this.width * (y - 1) + (x - 1)));
//		neighbourIndexes.add((this.width * (y + 1) + (x - 1)));
//		neighbourIndexes.add((this.width * (y + 1) + (x + 1)));
//
//		for (int i = 0; i < neighbourIndexes.size(); i++) {
//
//			if (neighbourIndexes.get(i) < 0 || neighbourIndexes.get(i) > this.argb.length) {
//				System.out.println("REMOVING");
//				neighbourIndexes.remove(i);
//			}
//		}
//
//		if (neighbourIndexes.size() < 8) {
//
//			System.out.println("JIAJIJI");
//		}
//
//		return neighbourIndexes;
//	}
//
//	public void sequentialLabeling() {
//		Set<int[]> set = new HashSet<int[]>();
//		Integer[] color = colors.get(0);
//		for (int i = 0; i < this.argb.length; i++) {
//			int currentArgb = this.argb[i];
//			int srcRed = (currentArgb >> 16) & 0xff;
//			int alpha = (currentArgb >> 24) & 0xff;
//			int x = i % this.width;
//			int y = i / this.width;
//			int[] neighbourIndexes = getAllNeighbours(x, y);
//			int pos = y * this.width + x;
//
//			int neighbourAmount = 8;
//			int count = 0;
//			int countColor = 0;
//			int neighbourLabel = 0;
//			HashMap<Integer, Integer> coloredNeighbours = new HashMap<Integer, Integer>();
//			for (int index : neighbourIndexes) {
//				if (index > 0 && index < this.argb.length) {
//					int neighbourARGB = this.argb[index];
//					int neighbourAlhpa = (neighbourARGB >> 24) & 0xff;
//					int neighbourRed = (neighbourARGB >> 16) & 0xff;
//					int neighbourGreen = (neighbourARGB >> 8) & 0xff;
//					int neighbourBlue = neighbourARGB & 0xff;
//					if (neighbourRed == 255 && neighbourGreen == 255 && neighbourBlue == 255) {
//						count++;
//					} else if (neighbourRed != 0) {
//						countColor++;
//						neighbourLabel = (neighbourAlhpa & 0xff) << 24 | (neighbourRed & 0xff) << 16
//								| (neighbourGreen & 0xff) << 8 | (neighbourBlue & 0xff);
//						coloredNeighbours.put(index, neighbourLabel);
//					}
//					;
//				} else {
//					neighbourAmount--;
//				}
//			}
//
//			if (neighbourAmount == count) {
//				this.argb[i] = (alpha & 0xff) << 24 | (color[0] & 0xff) << 16 | (color[1] & 0xff) << 8
//						| (color[2] & 0xff);
//				colors.add(color);
//				colors.remove(0);
//			} else if (countColor == 1) {
//				this.argb[i] = neighbourLabel;
//			} else if (countColor > 1) {
//				this.argb[i] = neighbourLabel;
//				for (int key : coloredNeighbours.keySet()) {
//					int specificColor = coloredNeighbours.get(key);
//					int ni = this.argb[key];
//					int k = specificColor;
//					if (ni != k) {
//						set.add(new int[] { ni, k });
//					}
//				}
//			}
//		}
//	}
//
//	private int[] getAllNeighbours(int y, int x) {
//		int[] neighbourIndexes = new int[] { (this.width * (x - 1) + y), (this.width * (x + 1) + y),
//				(this.width * x + (y - 1)), (this.width * x + (y + 1)),
//
//				(this.width * (x - 1) + (y + 1)), (this.width * (x - 1) + (y - 1)), (this.width * (x + 1) + (y - 1)),
//				(this.width * (x + 1) + (y + 1)) };
//
//		return neighbourIndexes;
//	}
//
//	private void depthFirstFloodFilling(Integer[] coordinate, Integer[] color) {
//		Stack<Integer[]> stack = new Stack<Integer[]>();
//
//		stack.push(coordinate);
//
//		while (!stack.empty()) {
//			deptSizes.add(stack.size());
//			Integer[] coordinates = stack.pop();
//			int x = coordinates[0];
//			int y = coordinates[1];
//			int pos = y * this.width + x;
//
//			if (pos > 0 && pos < this.argb.length) {
//				int currentArgb = this.argb[pos];
//				int red = (currentArgb >> 16) & 0xff;
//				int green = (currentArgb >> 8) & 0xff;
//				int blue = currentArgb & 0xff;
////				System.out.println("red: " + red);
////				System.out.println("green: " + green);
////				System.out.println("blue: " + blue);
//				if (red == 0) {
////					System.out.println("red: " + red);
//					int alpha = (this.argb[pos] >> 24) & 0xff;
//					this.argb[pos] = (alpha & 0xff) << 24 | (color[0] & 0xff) << 16 | (color[1] & 0xff) << 8
//							| (color[2] & 0xff);
//					;
//
//					List<Integer> allNeighbours = getAllNeighbours2(x, y);
//					for (Integer i : allNeighbours) {
//						int neighbourX = i.intValue() % this.width;
//						int neighbourY = i.intValue() / this.width;
//						stack.push(new Integer[] { neighbourX, neighbourY });
//					}
//
////					stack.push(new Integer[] { x, y - 1 });//
////					stack.push(new Integer[] { x - 1, y + 1 });// war falsch: x-1 y-1
////					stack.push(new Integer[] { x, y + 1 });//
////					stack.push(new Integer[] { x - 1, y + 1 }); //
////					stack.push(new Integer[] { x - 1, y }); //
////					stack.push(new Integer[] { x + 1, y }); //
////					stack.push(new Integer[] { x + 1, y + 1 });//
////					stack.push(new Integer[] { x - 1, y - 1 }); //
//				}
//			}
//		}
//	}
//
//	private void breadthFirstFloodFilling(Integer[] coordinate, Integer[] color) {
//		Deque<Integer[]> queue = new ArrayDeque<Integer[]>();
//		queue.add(coordinate);
//
//		while (!queue.isEmpty()) {
////			deptSizes.add(queue.size());
//			Integer[] coordinates = queue.pop();
//
//			int x = coordinates[0];
//			int y = coordinates[1];
//			int pos = y * this.width + x;
//
//			if (pos > 0 && pos < this.argb.length) {
//				int currentArgb = this.argb[pos];
//				int red = (currentArgb >> 16) & 0xff;
//				int green = (currentArgb >> 8) & 0xff;
//				int blue = currentArgb & 0xff;
//
//				if (red == 0 && green == 0 && blue == 0) {
////                    System.out.println("red: " + red);
//					int alpha = (this.argb[pos] >> 24) & 0xff;
//					this.argb[pos] = (alpha & 0xff) << 24 | (color[0] & 0xff) << 16 | (color[1] & 0xff) << 8
//							| (color[2] & 0xff);
//					;
//
//					List<Integer> allNeighbours = getAllNeighbours2(x, y);
//					for (Integer i : allNeighbours) {
//						int neighbourX = i.intValue() % this.width;
//						int neighbourY = i.intValue() / this.width;
//						queue.add(new Integer[] { neighbourX, neighbourY });
//					}
//
//				}
//			}
//		}
//	}

}

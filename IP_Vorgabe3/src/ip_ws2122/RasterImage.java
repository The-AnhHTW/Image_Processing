// IP WS2021/22 Zoom Template
//
// Copyright (C) 2021 by Klaus Jung
// All rights reserved.
// Date: 2021-11-12

package ip_ws2122;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class RasterImage {
	
	private static final int gray  = 0xffa0a0a0;

	public int[] argb;	// pixels represented as ARGB values in scanline order
	public int[] originalArgb;
	public int width;	// image width in pixels
	public int height;	// image height in pixels
	private String lastMove = "";
	private static List<Path> pathList = new ArrayList<Path>();
	
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
		this.originalArgb = argb.clone();
	}
	
	public RasterImage(File file) {
		// creates an RasterImage by reading the given file
		Image image = null;
		if(file != null && file.exists()) {
			image = new Image(file.toURI().toString());
		}
		if(image != null && image.getPixelReader() != null) {
			width = (int)image.getWidth();
			height = (int)image.getHeight();
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
		width = (int)image.getWidth();
		height = (int)image.getHeight();
		argb = new int[width * height];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
	}
	
	public void setToView(ImageView imageView) {
		// sets the current argb pixels to be shown in the given ImageView
		if(argb != null) {
			WritableImage wr = new WritableImage(width, height);
			PixelWriter pw = wr.getPixelWriter();
			pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
			imageView.setImage(wr);
		}
	}
	
	
	
	public void findContour() {
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

				// find path
				while (!Arrays.equals(path.get(0), path.get(path.size() - 1))) {
				
					int[] currentPixel = path.get(path.size() - 1);
					System.out.println(currentPixel[0] + " | " +  currentPixel[1]);
					int[] possibleEdgePoint = moveDirection(currentPixel[0], currentPixel[1]);
					if (getDistance(currentPixel[0], currentPixel[1], possibleEdgePoint[0],
							possibleEdgePoint[1]) == 1) {
						path.add(possibleEdgePoint);
					}
				
				}
			
				//define dataType for path;
				Path newPath;
				int pixelToCheckY= path.get(0)[1];
				int pixelToCheckX = path.get(0)[0];
				//getLeft
				int posToCheck = pixelToCheckY * this.width + (pixelToCheckX -1);
				int colorOfPixel;
				if(posToCheck <0 || posToCheck > this.argb.length) {
					colorOfPixel = 255;
				} else {
					colorOfPixel = (this.originalArgb[posToCheck] >> 16) & 0xff;
				}
			
				if (colorOfPixel == 255) {
					//path is outer contour
					 newPath = new Path(path,false,true);
				} else {
					//path is inner contour
					 newPath = new Path(path,true,false);
				}
				
				pathList.add(newPath);
				

				// Innere Pixels invertieren
				List<int[]> pathToInvert = pathList.get(pathList.size() - 1).getEckPunkte();
				for (int j = 0; j < pathToInvert.size(); j++) {
					int[] current = path.get(j);
					int[] next;

					if (j == path.size() - 1) {
						break;
					} else {
						next = path.get(j + 1);
					}

					if (next[1] != current[1]) {
						int[] pointer;
						//check if gobottom
						if (next[1] > current[1]) {
							pointer = current.clone();
						} else {
							pointer = next.clone();
						}
						
						
						for (int l = pointer[0]; l < this.width; l++) {
							
							int index = (pointer[1]) * this.width + l;
							if (index > 0 && index < this.argb.length) {
								int rowArgb = this.argb[index];
								int red = (rowArgb >> 16) & 0xff;
								int alpha = (rowArgb >> 24) & 0xff;
								int color = red == 255 ? 0 : 255;
								this.argb[index] = (alpha << 24) | (color << 16) | (color << 8) | color;
							}
						}
					}
				}
//					for(int a : this.argb) {
//						int color= (a >> 16) & 0xff;
//						System.out.println(color);
//					}
				findContour();

//				pathList.add(path);
//				typeOfPaths.add(type);
//				if(type == "outer") {
//					this.findContour("inner");
//				}

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

	public List<Path> getPath() {
//		for (Path path : pathList) {
//				for (int[] i : path.getEckPunkte()) {
//						int pos = i[1] * this.width + i[0];
//						int alpha = (this.argb[pos] >> 24) & 0xff;
//						this.argb[pos] =  (alpha << 24) | (0 << 16) | (0 << 8) | 0;
//				}
//			
//		}
		return pathList;
	}

	private int[] moveDirection(int x, int y) {
		int pos = y * this.width + x;
		int srcRed;
		int topPixelLeftRed;
		int topPixelRed;
		int leftPixelRed;
		int topPixelLeft = (y - 1) * this.width + (x - 1);
		int topPixel = (y - 1) * this.width + (x);
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
				return new int[] { x, y + 1 };
			} else
				return move(x, y, srcRed, topPixelLeftRed, topPixelRed, leftPixelRed);
		} else if (this.lastMove.equals("GoLeft")) {
			// go-top
			if (topPixelLeftRed == 0 && topPixelRed == 255) {
				lastMove = "GoTop";
				System.out.println("Go top");
				return new int[] { x, y - 1 };
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
			return new int[] { x, y - 1 };
		}

		// go-bottom
		if (srcRed == 0 && leftPixelRed == 255) {
			lastMove = "GoBottom";
			System.out.println("Go bottom");
			return new int[] { x, y + 1 };
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
	
}

package ip_ws2122;

import java.util.HashMap;
import java.util.Stack;

public class FloodFill {

	public static void floodFill(RasterImage binarizedImage, RasterImage dst) {

		for (int i = 0; i < binarizedImage.argb.length; i++) {
			int currentArgb = binarizedImage.argb[i];
			int alpha = (currentArgb >> 24) & 0xff;
			int srcRed = (currentArgb >> 16) & 0xff;
			int x = i % binarizedImage.width;
			int y = i / binarizedImage.width;

			if (srcRed == 0) {
				Integer[] coordinates = {x,y};
				depthFirstFloodFilling(binarizedImage,dst, coordinates);
			}
		}
	}
	
	private static RasterImage depthFirstFloodFilling(RasterImage binarizedImage, RasterImage dst, Integer[] coordinate) {
		Stack<Integer[]> stack = new Stack<Integer[]>();
		stack.add(coordinate);

		while (!stack.empty()) {
			Integer[] coordinates = stack.pop();
			int x = coordinates[0];
			int y = coordinates[1];
			int i = y * binarizedImage.width;
			
			if(x >= 0 && x <= i && y >=0 && y <= i) {
				int alpha = (binarizedImage.argb[i] >> 24) & 0xff;
				int red = ( binarizedImage.argb[i] >> 16 ) & 0xff;
				
				if (red == 0) {
					dst.argb[i] = (alpha << 24) | (0 << 16) | (0 << 8) | 0;
				stack.add(new Integer[] {x, y-1});
				stack.add(new Integer[]{x-1, y-1});
				stack.add(new Integer[] {x,y+1});
				stack.add(new Integer[] {x-1, y+1});
				stack.add(new Integer[] {x-1,y});
				stack.add(new Integer[] {x+1,y});
				stack.add(new Integer[] {x+1,y+1});
				stack.add(new Integer[] {x-1,y-1});
				}
			}			
		}
		return dst;
	}
}

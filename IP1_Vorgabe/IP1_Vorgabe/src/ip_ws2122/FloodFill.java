package ip_ws2122;

import java.util.HashMap;
import java.util.Stack;

public class FloodFill {

	public static void floodFill(RasterImage binarizedImage, RasterImage dst) {
		Stack<Integer[]> stack = new Stack<Integer[]>();
		for (int i = 0; i < binarizedImage.argb.length; i++) {
			int currentArgb = binarizedImage.argb[i];
			int alpha = (currentArgb >> 24) & 0xff;
			int srcRed = (currentArgb >> 16) & 0xff;
			int x = i % binarizedImage.width;
			int y = i / binarizedImage.width;

			if (srcRed == 1) {
				Integer[] coordinates = {x,y};
				stack.add(coordinates);
			}
		}

		while (!stack.empty()) {
			Integer[] coordinates = stack.pop();
			int x = coordinates[0];
			int y = coordinates[1];
			
//			int[] neighbourIndex = { 
//					(binarizedImage.width * (y - 1) + x), // left
//					(binarizedImage.width * (y - 1) + x -1), // left-top
//					(binarizedImage.width * (y + 1) + x), // right
//					(binarizedImage.width * (y + 1) + x -1), // right-top
//					(binarizedImage.width * y + (x - 1)), // top
//					(binarizedImage.width * y + (x +1)), // bottom
//					(binarizedImage.width * (y+1)+ (x +1)), // bottom-right
//					(binarizedImage.width * (y-1)+ (x - 1)), // bottom-left
//			};
			
//			for (int index : neighbourIndex) {
//				if (index > 0 && index <binarizedImage.argb.length) {
//					int neighbourARGB = binarizedImage.argb[index];
//					int neighbourRed = (neighbourARGB >> 16) & 0xff;
//					if(neighbourRed==1) {
//						
//					}
//				}
//			}

			if() {
				
			}
			
			
		}

	}
}

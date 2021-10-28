// IP Ue1 WS 2021/22 Vorgabe
//
// Copyright (C) 2020 by Klaus Jung
// All rights reserved.
// Date: 2020-10-02

package ip_ws2122;

import java.util.Arrays;

public class Filter {

	public static void outline(RasterImage src, RasterImage dst) {
		RasterImage imageEroded = src;
		for (int i = 0; i < src.argb.length; i++) {
			int currentArgb = src.argb[i];
			int alpha = (currentArgb >> 24) & 0xff;
			int red = (currentArgb >> 16) & 0xff;
			int green = (currentArgb >> 8) & 0xff;
			int blue = currentArgb & 0xff;

			int x = i % src.width; // % is the "modulo operator", the remainder of i / width;
			int y = i / src.width;

			int[] LRTBIndex = { (src.width * (y - 1) + x), (src.width * (y - 1) + x), (src.width * y + (x - 1)),
					(src.width * y + (x + 1)) };

			int allBlack = 0;
			int blackThreshold = 4;
			
			for (int index : LRTBIndex) {
				if (index > 0 && index < src.argb.length) {
					int neighbourARGB = src.argb[index];
					int neighbourRed = (neighbourARGB >> 16) & 0xff;
					int neighbourGreen = (neighbourARGB >> 8) & 0xff;
					int neighbourBlue = neighbourARGB & 0xff;
					if (neighbourRed == 0 && neighbourGreen == 0 && neighbourBlue == 0) allBlack += 1;
				}
			}

			if (allBlack == blackThreshold) {
				dst.argb[i] = (alpha << 24) | (0 << 16) | (0 << 8) | 0;
			} else {
				dst.argb[i] = (alpha << 24) | (255 << 16) | (255 << 8) | 255;
			}
		}

		int[] inverted = invertImage(dst);
		for (int i = 0; i < src.argb.length; i++) {
			int currentArgb = src.argb[i];
			int alpha = (currentArgb >> 24) & 0xff;
			int srcRed = (currentArgb >> 16) & 0xff;

			int invertedArgb = inverted[i];
			int invertedRed = (invertedArgb >> 16) & 0xff;

			if (srcRed == invertedRed && srcRed == 0) {
				dst.argb[i] = (alpha << 24) | (0 << 16) | (0 << 8) | 0;
			} else {
				dst.argb[i] = (alpha << 24) | (255 << 16) | (255 << 8) | 255;
			}
		}
	}

	private static int[] invertImage(RasterImage dst) {
		int[] inverted = new int[dst.argb.length];
		for (int i = 0; i < dst.argb.length; i++) {
			int currentArgb = dst.argb[i];
			int alpha = (currentArgb >> 24) & 0xff;
			int red = (currentArgb >> 16) & 0xff;
			int green = (currentArgb >> 8) & 0xff;
			int blue = currentArgb & 0xff;

			if (red == 0 && green == 0 && blue == 0) {
				inverted[i] = (alpha << 24) | (255 << 16) | (255 << 8) | 255;
			} else {
				inverted[i] = (alpha << 24) | (0 << 16) | (0 << 8) | 0;
			}

		}
		return inverted;
	}

}

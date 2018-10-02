package com.displee.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A class containing utilities used throught the application.
 * @author Displee
 */
public class Utilities {

	private static final int[] HSL_2_RGB = new int[65536];

	static {
		double d = 0.7D;
		int i = 0;
		for (int i1 = 0; i1 != 512; ++i1) {
			float f = ((i1 >> 3) / 64.0F + 0.0078125F) * 360.0F;
			float f1 = 0.0625F + (0x7 & i1) / 8.0F;
			for (int i2 = 0; i2 != 128; ++i2) {
				float f2 = i2 / 128.0F;
				float f3 = 0.0F;
				float f4 = 0.0F;
				float f5 = 0.0F;
				float f6 = f / 60.0F;
				int i3 = (int) f6;
				int i4 = i3 % 6;
				float f7 = f6 - i3;
				float f8 = f2 * (-f1 + 1.0F);
				float f9 = f2 * (-(f7 * f1) + 1.0F);
				float f10 = (1.0F - f1 * (-f7 + 1.0F)) * f2;
				if (i4 == 0) {
					f3 = f2;
					f5 = f8;
					f4 = f10;
				} else if (i4 == 1) {
					f5 = f8;
					f3 = f9;
					f4 = f2;
				} else if (i4 == 2) {
					f3 = f8;
					f4 = f2;
					f5 = f10;
				} else if (i4 == 3) {
					f4 = f9;
					f3 = f8;
					f5 = f2;
				} else if (i4 == 4) {
					f5 = f2;
					f3 = f10;
					f4 = f8;
				} else {
					f4 = f8;
					f5 = f9;
					f3 = f2;
				}
				HSL_2_RGB[i++] = ((int) ((float) Math.pow(f3, d) * 256.0F) << 16) | ((int) ((float) Math.pow(f4, d) * 256.0F) << 8) | (int) ((float) Math.pow(f5, d) * 256.0F);
			}
		}
	}

	public static int forHSBColor(int hsb) {
		return HSL_2_RGB[hsb & 0xFFFF];
	}

}

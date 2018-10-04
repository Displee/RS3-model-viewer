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
 *
 * @author Displee
 */
public class Utilities {

	private static final int[] HSL_2_RGB = new int[65536];

	static {
		double var1 = 0.7D + (Math.random() * 0.03D - 0.015D);
		int var3 = 0;

		for (int var4 = 0; var4 < 512; ++var4) {
			float var5 = (0.0078125F + (float) (var4 >> 3) / 64.0F) * 360.0F;
			float var6 = 0.0625F + (float) (var4 & 7) / 8.0F;

			for (int var7 = 0; var7 < 128; ++var7) {
				float var8 = (float) var7 / 128.0F;
				float var9 = 0.0F;
				float var10 = 0.0F;
				float var11 = 0.0F;
				float var12 = var5 / 60.0F;
				int var13 = (int) var12;
				int var14 = var13 % 6;
				float var15 = var12 - (float) var13;
				float var16 = (1.0F - var6) * var8;
				float var17 = var8 * (1.0F - var6 * var15);
				float var18 = var8 * (1.0F - var6 * (1.0F - var15));
				if (var14 == 0) {
					var9 = var8;
					var10 = var18;
					var11 = var16;
				} else if (var14 == 1) {
					var9 = var17;
					var10 = var8;
					var11 = var16;
				} else if (2 == var14) {
					var9 = var16;
					var10 = var8;
					var11 = var18;
				} else if (var14 == 3) {
					var9 = var16;
					var10 = var17;
					var11 = var8;
				} else if (4 == var14) {
					var9 = var18;
					var10 = var16;
					var11 = var8;
				} else if (5 == var14) {
					var9 = var8;
					var10 = var16;
					var11 = var17;
				}

				var9 = (float) Math.pow((double) var9, var1);
				var10 = (float) Math.pow((double) var10, var1);
				var11 = (float) Math.pow((double) var11, var1);
				int var19 = (int) (var9 * 256.0F);
				int var20 = (int) (256.0F * var10);
				int var21 = (int) (256.0F * var11);
				int var22 = (var19 << 16) + -16777216 + (var20 << 8) + var21;
				HSL_2_RGB[var3++] = var22;
			}
		}
	}

	public static int forHSBColor(int hsb) {
		return HSL_2_RGB[hsb & 0xFFFF] & 16777215;
	}

}

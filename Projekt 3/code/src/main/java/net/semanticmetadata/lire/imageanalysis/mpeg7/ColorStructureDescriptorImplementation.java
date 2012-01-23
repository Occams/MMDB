package net.semanticmetadata.lire.imageanalysis.mpeg7;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ColorStructureDescriptorImplementation {

	public static final int[] HUE256_QUANT = { 1, 4, 16, 16, 16 };
	public static final int[] SUM256_QUANT = { 32, 8, 4, 4, 4 };
	public static final int[] HUE128_QUANT = { 1, 4, 8, 8, 8 };
	public static final int[] SUM128_QUANT = { 16, 4, 4, 4, 4 };
	public static final int[] HUE64_QUANT = { 1, 4, 4, 8, 8 };
	public static final int[] SUM64_QUANT = { 8, 4, 4, 2, 1 };
	public static final int[] HUE32_QUANT = { 1, 4, 4, 4 };
	public static final int[] SUM32_QUANT = { 8, 4, 1, 1 };
	public static final int HUE_MAX_VALUE = 359, RGB_MAX_VALUE = 255;
	public static final int BIN256 = 256, BIN128 = 128, BIN64 = 64, BIN32 = 32;

	public static void extractCSD(BufferedImage img, int binnum) {
		if (img == null)
			throw new NullPointerException();

		if (binnum != 256 && binnum != 128 && binnum != 64 && binnum != 32)
			throw new IllegalArgumentException("Wrong number of bins");

		int width = img.getWidth(), height = img.getHeight();
		int[] rgb = new int[height * width];
		img.getRGB(0, 0, width, height, rgb, 0, width);

		/*
		 * TODO: upsample images smaller than 8x8 by the smallest power of 2 (in
		 * both directions) such that the minimum of the width and height of the
		 * resulting image is greater than or equal to 8
		 */

		if (width < 8 || height < 8) {

		}

		/* Convert to HMMD color space */
		float[][] hmmd = new float[width * height][5];

		for (int i = 0; i < rgb.length; i++) {
			hmmd[i] = HMMD.rgb2hmmd((rgb[i] >> 16) & 0xFF,
					(rgb[i] >> 8) & 0xFF, rgb[i] & 0xFF);
		}

		/* Determine spatial extent of structuring element */
		long p, k, e;
		if (width < 256 || height < 256) {
			k = 1;
			e = 8;
		} else {
			p = Math.max(0, Math.round(0.5f * log2(width * height) - 8));
			k = (long) Math.pow(2, p);
			e = 8 * k;
		}

		/*
		 * Accumulate CS histogram by sliding the structuring element across the
		 * image. The stride size is determined by the subsampling factor
		 */
		for (int i = 0; i < height; i += k) {
			for (int j = 0; j < width; j += k) {

			}
		}

	}

	/*
	 * Bin ma ned ganz sicher ob ma hier durch den subsampling factor teilen
	 * sollte
	 */
	private static int normalizeFactor(int width, int height, int k) {
		return (width / k - 7) * (height / k - 7);
	}

	private static double log2(double x) {
		return Math.log(x) / Math.log(2.0f);
	}

	private static int binIndex(float[] hmmd, int binnum) {
		float hue = hmmd[0], sum = hmmd[4];
		int index = 0, subspace = subspace(hmmd, binnum);

		if (binnum == BIN256) {
			for (int i = 0; i < subspace; i++)
				index += HUE256_QUANT[i] * SUM256_QUANT[i];
			index += (sum / RGB_MAX_VALUE) * SUM256_QUANT[subspace]
					+ (hue / HUE_MAX_VALUE) * HUE256_QUANT[subspace];
		} else if (binnum == BIN128) {
			for (int i = 0; i < subspace; i++)
				index += HUE128_QUANT[i] * SUM128_QUANT[i];
			index += (sum / RGB_MAX_VALUE) * SUM128_QUANT[subspace]
					+ (hue / HUE_MAX_VALUE) * HUE128_QUANT[subspace];
		} else if (binnum == BIN64) {
			for (int i = 0; i < subspace; i++)
				index += HUE64_QUANT[i] * SUM64_QUANT[i];
			index += (sum / RGB_MAX_VALUE) * SUM64_QUANT[subspace]
					+ (hue / HUE_MAX_VALUE) * HUE64_QUANT[subspace];
		} else {
			for (int i = 0; i < subspace; i++)
				index += HUE32_QUANT[i] * SUM32_QUANT[i];
			index += (sum / RGB_MAX_VALUE) * SUM32_QUANT[subspace]
					+ (hue / HUE_MAX_VALUE) * HUE32_QUANT[subspace];
		}

		return index;
	}

	private static int subspace(float[] hmmd, int binnum) {
		float diff = hmmd[3];

		/* There are only 4 subspaces if the number of bins equals 32 */
		if (binnum != BIN32) {
			if (diff < 6)
				return 0;
			else if (diff < 20)
				return 1;
			else if (diff < 60)
				return 2;
			else if (diff < 110)
				return 3;
			else
				return 4;
		} else {
			if (diff < 6)
				return 0;
			else if (diff < 60)
				return 1;
			else if (diff < 110)
				return 2;
			else
				return 3;
		}
	}

	public static class HMMD {

		/**
		 * Returns a HMMD color space representation.
		 * 
		 * @param r
		 *            - RED component
		 * @param g
		 *            - GREEN component
		 * @param b
		 *            - BLUE component
		 * @return <code>float[]</code> array containing
		 *         <code>[0] = hue, [1] = max, [2] = min, [3] = diff, [4] = sum</code>
		 *         according to HMMD
		 * @throws IllegalArgumentException
		 */
		public static float[] rgb2hmmd(int r, int g, int b) {

			if (r < 0 | g < 0 | b < 0 | r > RGB_MAX_VALUE | g > RGB_MAX_VALUE
					| b > RGB_MAX_VALUE)
				throw new IllegalArgumentException();

			float hmmd[] = new float[5];

			float max = Math.max(Math.max(r, g), Math.max(g, b)), min = Math
					.min(Math.min(r, g), Math.min(g, b));

			float diff = max - min, sum = (max + min) / 2f, hue = 0f;

			/* According to HSV color space ( standard page 34) */
			if (max == min)
				hue = 0f;
			else {
				if (max == r && g >= b)
					hue = 60 * (g - b) / diff;
				else if (max == r && g < b)
					hue = 360 + 60 * (g - b) / diff;
				else if (max == g)
					hue = 60 * (2f + (b - r) / diff);
				else
					hue = 60 * (4f + (r - g) / diff);
			}

			hmmd[0] = hue;
			hmmd[1] = max;
			hmmd[2] = min;
			hmmd[3] = diff;
			hmmd[4] = sum;

			return hmmd;
		}
	}

	public static void main(String args[]) throws Exception {
		BufferedImage img = ImageIO.read(new File("image.orig/1.jpg"));
		new ColorStructureDescriptorImplementation().extractCSD(img, 256);

		// for (int i = 0; i < hmmd.length; i++)
		// System.out.println(hmmd[i]);
	}

}

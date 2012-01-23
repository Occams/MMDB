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
	public static final int HUE_MAX_VALUE = 360, RGB_MAX_VALUE = 255;
	public static final int BIN256 = 256, BIN128 = 128, BIN64 = 64, BIN32 = 32;
	public static final int STRUCT_ELEM_SIZE = 8;

	public static float[] extractCSD(BufferedImage img, int binnum) {
		if (img == null)
			throw new NullPointerException();

		if (binnum != 256 && binnum != 128 && binnum != 64 && binnum != 32)
			throw new IllegalArgumentException("Wrong number of bins");

		int width = img.getWidth(), height = img.getHeight();

		if (width == 0 || height == 0)
			throw new IllegalArgumentException();

		int[] rgb = new int[height * width];
		img.getRGB(0, 0, width, height, rgb, 0, width);

		/*
		 * Upsample images smaller than 8x8 by the smallest power of 2 (in both
		 * directions) such that the minimum of the width and height of the
		 * resulting image is greater than or equal to 8
		 */

		if (width < 8 || height < 8) {
			int nWidth = width, nHeight = height;
			int upFactor = 1;

			while (nWidth < 8 || nHeight < 8) {
				nWidth *= 2;
				nHeight *= 2;
				upFactor *= 2;
			}

			int[] nRGB = new int[nHeight * nWidth];

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {

					for (int yy = 0; yy < upFactor; yy++) {
						for (int xx = 0; xx < upFactor; xx++) {
							int idx = (y * upFactor + yy) * nWidth + x
									* upFactor + xx;
							nRGB[idx] = rgb[y * width + x];
						}
					}
				}
			}

			rgb = nRGB;
			width = nWidth;
			height = nHeight;
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
		float[] csd = new float[binnum];

		for (int y = 0; y < height - STRUCT_ELEM_SIZE + 1; y += k) {
			for (int x = 0; x < width - STRUCT_ELEM_SIZE + 1; x += k) {
				int[] tmp = new int[binnum];

				/* Traverse structuring element */
				for (int yy = y; yy < y + STRUCT_ELEM_SIZE * k; yy += k) {
					for (int xx = x; xx < x + STRUCT_ELEM_SIZE * k; xx += k) {

						int r = 0, g = 0, b = 0;

						/* Subsampling */
						for (int i = yy; i < yy + k; i++) {
							for (int j = xx; j < xx + k; j++) {
								int idx = i * width + j;
								r += (rgb[idx] >> 16) & 0xFF;
								g += (rgb[idx] >> 8) & 0xFF;
								b += rgb[idx] & 0xFF;
							}
						}

						r /= k * k;
						g /= k * k;
						b /= k * k;
						// System.out.println("Red: "+r+
						// " Green: "+g+" Blue: "+b);

						/* Convert to HMMD and increment bin */
						float[] hmmd = HMMD.rgb2hmmd(r, g, b);
						tmp[binIndex(hmmd, binnum)] += 1;
					}
				}

				/* Increment color structure descriptor bins */
				for (int i = 0; i < tmp.length; i++) {
					if (tmp[i] > 0)
						csd[i] += 1;
				}
			}
		}

		/* Normalize color structure bin values */
		float normFac = (width / k - 7) * (height / k - 7);
		for (int i = 0; i < csd.length; i++) {
			csd[i] /= normFac;
		}

		return csd;
	}

	private static double log2(double x) {
		return Math.log(x) / Math.log(2.0f);
	}

	public static float distance(float[] first, float[] second) {
		if (first.length < second.length)
			second = requant(second, first.length);
		else
			first = requant(first, second.length);

		float distance = 0;

		for (int i = 0; i < first.length; i++) {
			distance += Math.abs(first[i] - second[i]);
		}

		return distance /= first.length;
	}

	public static float[] requant(float[] csd, int binsize) {
		if ((binsize != BIN256 && binsize != BIN128 && binsize != BIN64 && binsize != BIN32)
				|| binsize > csd.length)
			throw new IllegalArgumentException();

		/* TODO: implement requantization */
		return csd;
	}

	private static int binIndex(float[] hmmd, int binnum) {
		float hue = hmmd[0], sum = hmmd[4];
		int index = 0, subspace = subspace(hmmd, binnum);

		if (binnum == BIN256) {
			for (int i = 0; i < subspace; i++)
				index += HUE256_QUANT[i] * SUM256_QUANT[i];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM256_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE256_QUANT[subspace]);
		} else if (binnum == BIN128) {
			for (int i = 0; i < subspace; i++)
				index += HUE128_QUANT[i] * SUM128_QUANT[i];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM128_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE128_QUANT[subspace]);
		} else if (binnum == BIN64) {
			for (int i = 0; i < subspace; i++)
				index += HUE64_QUANT[i] * SUM64_QUANT[i];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM64_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE64_QUANT[subspace]);
		} else {
			for (int i = 0; i < subspace; i++)
				index += HUE32_QUANT[i] * SUM32_QUANT[i];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM32_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE32_QUANT[subspace]);
		}

		// System.out.println("Hue: "+ hue+" Sum: "+sum
		// +" subspace: "+subspace+" Index: "+index);
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
		BufferedImage img1 = ImageIO.read(new File("image.orig/5.jpg"));
		BufferedImage img2 = ImageIO.read(new File("image.orig/4.jpg"));
		BufferedImage small = ImageIO.read(new File("image.orig/small.png"));
		float[] csd1 = ColorStructureDescriptorImplementation.extractCSD(img1,
				256);
		float[] csd2 = ColorStructureDescriptorImplementation.extractCSD(img2,
				256);
		float[] csd3 = ColorStructureDescriptorImplementation.extractCSD(small,
				32);

		for (int i = 0; i < csd3.length; i++) {
			System.out.print(csd3[i] + " ");
		}
		System.out.println();
		System.out.println(ColorStructureDescriptorImplementation.distance(
				csd1, csd2));

		// for (int i = 0; i < hmmd.length; i++)
		// System.out.println(hmmd[i]);
	}

}

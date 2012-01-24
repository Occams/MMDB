package net.semanticmetadata.lire.imageanalysis.mpeg7;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Provides means to extract and compare MPEG-7 Color Structure Descriptors
 * (CSD) from an image.
 * 
 * @author Huber Bastian and Daniel Watzinger
 * 
 */
public class ColorStructureDescriptorImplementation {

	public static final int[] BIN_QUANT_LEVELS_SIZES = { 1, 25, 20, 35, 35, 140 };
	public static final int[] BIN_QUANT_LEVELS = { 0, 1, 26, 46, 81, 116, 256 };
	public static final float[] BIN_QUANT_REGION = { 0, 0.000000001f, 0.037f,
			0.08f, 0.195f, 0.32f, 1 };
	public static final float[] BIN_QUANT_REGION_SIZES = { 0.000000001f,
			0.036999999f, 0.043f, 0.115f, 0.125f, 0.68f };
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

	/**
	 * Extracts the MPEG-7 Color Structure Descriptor (CSD) with the desired
	 * number of bins (<code>binnum</code>, one of <code>{32,64,128,256}</code>
	 * )from <code>img</code>.
	 * 
	 * @param img
	 *            the image from which a CSD should be extracted
	 * @param binnum
	 *            the desired number of bins
	 * @return a CSD of the image
	 */
	public static int[] extractCSD(BufferedImage img, int binnum) {
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

		/* Return 8-bit quantized bin values */
		return quant(csd);
	}

	private static double log2(double x) {
		return Math.log(x) / Math.log(2.0f);
	}

	/**
	 * Computes the distance between two color structure descriptors.
	 * Requantizes one of the color structure descriptors if needed (in case
	 * <code>first</code> and <code>second</code> feature different number of
	 * bins). <br />
	 * Distance function is based on a novel weighted city distance (L1-norm)
	 * approach introduced in <br />
	 * <code>Using the MPEG-7 Colour Structure Descriptor for 
	 * Human Identification in the POLYMNIA System </code> <br/>
	 * <code>Authors: Andreas Kriechbaum, Werner Bailer, Helmut Neuschmied, Georg Thallinger</code>
	 * 
	 * @param first
	 *            a color structure descriptor
	 * @param second
	 *            a color structure descriptor
	 * @return the distance between <code>first</code> and <code>second</code>
	 */
	public static float distance(int[] first, int[] second) {
		if (first.length < second.length)
			second = requant(second, first.length);
		else
			first = requant(first, second.length);

		float distance = 0;
		float sum = 0;

		for (int i = 0; i < first.length; i++) {
			sum += first[i] + second[i];
		}

		for (int i = 0; i < first.length; i++) {
			distance += ((first[i] + second[i]) / sum)
					* Math.abs(first[i] - second[i]);
		}

		return distance;
	}

	/**
	 * Invokes a requantize operation on the color structure descriptor
	 * <code>csd</code> based on the desired new number of bins (
	 * <code>binsize</code>, one of {32,64,128,256}).
	 * 
	 * @param csd
	 *            color structure descriptor, whereas csd.length > binsize
	 * @param binsize
	 *            the new number of bins
	 * @return a color structure descriptor with the new desired number of bins
	 */
	public static int[] requant(int[] csd, int binsize) {
		if ((binsize != BIN256 && binsize != BIN128 && binsize != BIN64 && binsize != BIN32)
				|| binsize > csd.length)
			throw new IllegalArgumentException();

		if (csd.length == binsize)
			return csd;

		int quant[] = new int[binsize];

		for (int i = 0; i < csd.length; i++) {
			int region = quantRegion(csd[i]);
			/*
			 * Recalc the value between [0,1] and then represent this value by
			 * 20 bits. TODO: midpoint reconstruction...
			 */
			float amplitude = BIN_QUANT_REGION_SIZES[region]
					* (csd[i] - BIN_QUANT_LEVELS[region])
					/ (BIN_QUANT_LEVELS_SIZES[region])
					+ BIN_QUANT_REGION[region];
			int amp = (int) (amplitude * ((2 << 20) - 1));

			/*
			 * Calc the new index and add the amplitude
			 */
			int newindex = convertIndex(i, csd.length, binsize);
			quant[newindex] += amp;
			quant[newindex] = clip(quant[newindex], 0, (2 << 20) - 1);

		}

		/*
		 * Calc float values from the quant values
		 */
		float tmp[] = new float[binsize];
		for (int i = 0; i < quant.length; i++)
			tmp[i] = (float) quant[i] / ((2 << 20) - 1);

		return quant(tmp);
	}

	/*
	 * Quantises the given array uniformly.
	 */
	private static int[] quant(float[] arr) {
		int uniform[] = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			int region = quantRegion(arr[i]);
			/* TODO: Use math round to get midpoint quantisation */
			uniform[i] = (int) ((arr[i] - BIN_QUANT_REGION[region])
					* BIN_QUANT_LEVELS_SIZES[region]
					/ BIN_QUANT_REGION_SIZES[region] + BIN_QUANT_LEVELS[region]);
		}
		return uniform;
	}

	/*
	 * Region of the amplitude f.
	 */
	private static int quantRegion(float f) {
		if (f < BIN_QUANT_REGION[1])
			return 0;
		else if (f < BIN_QUANT_REGION[2])
			return 1;
		else if (f < BIN_QUANT_REGION[3])
			return 2;
		else if (f < BIN_QUANT_REGION[4])
			return 3;
		else if (f < BIN_QUANT_REGION[5])
			return 4;
		else
			return 5;
	}

	/*
	 * Region of the given quantised value.
	 */
	private static int quantRegion(int i) {
		if (i < BIN_QUANT_LEVELS[1])
			return 0;
		else if (i < BIN_QUANT_LEVELS[2])
			return 1;
		else if (i < BIN_QUANT_LEVELS[3])
			return 2;
		else if (i < BIN_QUANT_LEVELS[4])
			return 3;
		else if (i < BIN_QUANT_LEVELS[5])
			return 4;
		else
			return 5;
	}

	private static int binIndex(float[] hmmd, int binnum) {
		float hue = hmmd[0], sum = hmmd[4];
		int index = 0, subspace = subspace(hmmd, binnum);

		if (binnum == BIN256) {
			for (int i = 0; i < subspace; i++)
				index += HUE256_QUANT[i] * SUM256_QUANT[i];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM256_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE256_QUANT[subspace])
					* SUM256_QUANT[subspace];
		} else if (binnum == BIN128) {
			for (int i = 0; i < subspace; i++)
				index += HUE128_QUANT[i] * SUM128_QUANT[i];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM128_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE128_QUANT[subspace])
					* SUM128_QUANT[subspace];
		} else if (binnum == BIN64) {
			for (int i = 0; i < subspace; i++)
				index += HUE64_QUANT[i] * SUM64_QUANT[i];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM64_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE64_QUANT[subspace])
					* SUM64_QUANT[subspace];
		} else {
			for (int i = 0; i < subspace; i++)
				index += HUE32_QUANT[i] * SUM32_QUANT[i];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM32_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE32_QUANT[subspace])
					* SUM32_QUANT[subspace];
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

	private static int convertIndex(int index, int quantLevelsFrom,
			int quantLevelsTo) {
		int[] fromHueQuant = HUE_QUANT(quantLevelsFrom);
		int[] toHueQuant = HUE_QUANT(quantLevelsTo);
		int[] fromSumQuant = SUM_QUANT(quantLevelsFrom);
		int[] toSumQuant = SUM_QUANT(quantLevelsTo);

		int subspace = subspaceOfIndex(index, quantLevelsFrom);
		/*
		 * TODO re-evaluate the following line
		 */
		int newsubspace = (quantLevelsTo == BIN32 && quantLevelsFrom != BIN32 && subspace >= 2) ? subspace - 1
				: subspace;
		int subspaceIx = subspaceStart(subspace, quantLevelsFrom);
		int subspaceNewIx = subspaceStart(newsubspace, quantLevelsTo);

		/*
		 * Now get the corresponding hue and sum level.
		 */
		int hue = (index - subspaceIx) / fromSumQuant[subspace];
		int sum = (index - subspaceIx) % fromSumQuant[subspace];

		/*
		 * Map to new hue and new sum level.
		 */
		int newhue = (hue * toHueQuant[newsubspace]) / fromHueQuant[subspace];
		int newsum = (sum * toSumQuant[newsubspace]) / fromSumQuant[subspace];

		/*
		 * Return new index
		 */
		return subspaceNewIx + newhue * toSumQuant[newsubspace] + newsum;
	}

	private static int subspaceOfIndex(int index, int quantLevels) {
		int sum = 0;
		int[] hueQuant = HUE_QUANT(quantLevels);
		int[] sumQuant = SUM_QUANT(quantLevels);

		for (int i = 0; i < sumQuant.length; i++) {
			int newsum = sum + hueQuant[i] * sumQuant[i];
			if (sum <= index && index < newsum) {
				return i;
			}
			sum = newsum;
		}
		return sumQuant.length - 1;
	}

	private static int subspaceStart(int subspace, int quantLevels) {
		int[] hueQuant = HUE_QUANT(quantLevels);
		int[] sumQuant = SUM_QUANT(quantLevels);
		int sum = 0;
		for (int i = 0; i < subspace; i++)
			sum += hueQuant[i] * sumQuant[i];
		return sum;
	}

	private static int[] HUE_QUANT(int quantLevels) {
		if (quantLevels == BIN32)
			return HUE32_QUANT;
		else if (quantLevels == BIN64)
			return HUE64_QUANT;
		else if (quantLevels == BIN128)
			return HUE128_QUANT;
		else
			return HUE256_QUANT;
	}

	private static int[] SUM_QUANT(int quantLevels) {
		if (quantLevels == BIN32)
			return SUM32_QUANT;
		else if (quantLevels == BIN64)
			return SUM64_QUANT;
		else if (quantLevels == BIN128)
			return SUM128_QUANT;
		else
			return SUM256_QUANT;
	}

	private static int clip(int data, int min, int max) {
		return Math.max(min, Math.min(data, max));
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

		BufferedImage img1 = ImageIO.read(new File("image.orig/123.jpg"));
		BufferedImage img2 = ImageIO.read(new File("image.orig/345.jpg"));
		BufferedImage small = ImageIO.read(new File("image.orig/small.png"));
		int[] csd1 = ColorStructureDescriptorImplementation.extractCSD(img2,
				256);
		int[] csd2 = ColorStructureDescriptorImplementation
				.extractCSD(img2, 128);
		// int[] csd3 = ColorStructureDescriptorImplementation.extractCSD(img2,
		// 64);

		printArray(csd1);
		System.out.println();
		printArray(requant(csd1, 128));
		System.out.println();
		printArray(csd2);
		System.out.println();
		System.out.println(ColorStructureDescriptorImplementation.distance(
				csd1, csd2));
	}

	public static void printArray(int[] arr) {
		for (int i = 0; i < arr.length; i++) {
			System.out.printf("%3d ", arr[i]);
		}
	}

}

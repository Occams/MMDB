package net.semanticmetadata.lire.imageanalysis.mpeg7;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Provides means to extract and compare MPEG-7 Color Structure Descriptors
 * (CSD) from an image.
 * 
 * @author Huber Bastian, Daniel Watzinger
 * 
 */
public class ColorStructureDescriptorImplementation {

	public static final int[] BIN_QUANT_LEVELS = { 0, 1, 26, 46, 81, 116, 256 };
	public static final float[] BIN_QUANT_REGION = { 0, 0.000000001f, 0.037f,
			0.08f, 0.195f, 0.32f, 1 };
	public static final float[] BIN_QUANT_REGION_SIZES = { 0.000000001f,
			0.036999999f, 0.043f, 0.115f, 0.125f, 0.68f };
	public static final int[] BIN_QUANT_LEVELS_SIZES = { 1, 25, 20, 35, 35, 140 };
	public static final int[] HUE256_QUANT = { 1, 4, 16, 16, 16 };
	public static final int[] SUM256_QUANT = { 32, 8, 4, 4, 4 };
	public static final int[] HUE128_QUANT = { 1, 4, 8, 8, 8 };
	public static final int[] SUM128_QUANT = { 16, 4, 4, 4, 4 };
	public static final int[] HUE64_QUANT = { 1, 4, 4, 8, 8 };
	public static final int[] SUM64_QUANT = { 8, 4, 4, 2, 1 };
	public static final int[] HUE32_QUANT = { 1, 4, 4, 4 };
	public static final int[] SUM32_QUANT = { 8, 4, 1, 1 };
	public static final int[] HUE256_QUANT_MULT_SUM256_QUANT = { 0, 32, 64,
			128, 192, 256 };
	public static final int[] HUE128_QUANT_MULT_SUM128_QUANT = { 0, 16, 32, 64,
			96, 128 };
	public static final int[] HUE64_QUANT_MULT_SUM64_QUANT = { 0, 8, 24, 40,
			56, 64 };
	public static final int[] HUE32_QUANT_MULT_SUM32_QUANT = { 0, 8, 24, 28, 32 };
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
	public int[] extractCSD(BufferedImage img, int binnum) {
		if (img == null)
			throw new NullPointerException();

		if (binnum != 256 && binnum != 128 && binnum != 64 && binnum != 32)
			throw new IllegalArgumentException("Wrong number of bins");

		int width = img.getWidth(), height = img.getHeight();

		if (width == 0 || height == 0)
			throw new IllegalArgumentException("Bad image dimension");

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
		int p, k, e;
		if (width < 256 || height < 256) {
			k = 1;
			e = STRUCT_ELEM_SIZE;
		} else {
			p = (int) Math.max(0, Math.round(0.5f * log2(width * height) - 8));
			k = (int) Math.pow(2, p);
			e = STRUCT_ELEM_SIZE * k;
		}

		/* Extract RGB channels */
		int[] r = new int[rgb.length], g = new int[rgb.length], b = new int[rgb.length];

		for (int i = 0; i < rgb.length; i++) {
			r[i] = (rgb[i] >> 16) & 0xFF;
			g[i] = (rgb[i] >> 8) & 0xFF;
			b[i] = rgb[i] & 0xFF;
		}

		/* Cache HMMD and subsampling values */
		float[][] hmmdCache = new float[rgb.length][5];
		boolean[] hmmdCacheMask = new boolean[rgb.length];

		/*
		 * Accumulate CS histogram by sliding the structuring element across the
		 * image. The stride size is determined by the subsampling factor
		 */
		float[] csd = new float[binnum];
		int procNum = Runtime.getRuntime().availableProcessors();
		Thread[] threads = new Thread[procNum];

		/* Create computing threads */
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new LoopThread(height, width, binnum, k, e, procNum,
					i, r, g, b, csd, hmmdCache, hmmdCacheMask);
			threads[i].start();
		}

		/* Wait for threads to finish */
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e1) {
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

	private class LoopThread extends Thread {
		private int height, width, binnum, k, kSquare, e, procNum, id;
		private int[] r, g, b;
		private float[] csd;
		private float[][] hmmdCache;
		private boolean[] hmmdCacheMask;

		public LoopThread(int height, int width, int binnum, int k, int e,
				int procNum, int id, int[] r, int[] g, int[] b, float[] csd,
				float[][] hmmdCache, boolean[] hmmdCacheMask) {
			this.binnum = binnum;
			this.height = height;
			this.width = width;
			this.csd = csd;
			this.e = e;
			this.k = k;
			this.g = g;
			this.b = b;
			this.r = r;
			this.kSquare = k * k;
			this.procNum = procNum;
			this.id = id;
			this.hmmdCache = hmmdCache;
			this.hmmdCacheMask = hmmdCacheMask;
		}

		public void run() {

			for (int y = id * k; y < height - e + 1; y += procNum * k) {
				for (int x = 0; x < width - e + 1; x += k) {
					int[] tmp = new int[binnum];

					/* Traverse structuring element */
					for (int yy = y; yy < y + e; yy += k) {
						for (int xx = x; xx < x + e; xx += k) {

							/* Subsampling */
							int idx = yy * width + xx;
							float[] hmmd;

							if (!hmmdCacheMask[idx]) {
								int sR = 0, sG = 0, sB = 0;
								for (int i = yy; i < yy + k; i++) {
									for (int j = xx; j < xx + k; j++) {
										int idxx = i * width + j;
										sR += r[idxx];
										sG += g[idxx];
										sB += b[idxx];
									}
								}

								sR /= kSquare;
								sG /= kSquare;
								sB /= kSquare;

								/* Convert to HMMD color space and increment bin */
								hmmd = HMMD.rgb2hmmd(sR, sG, sB);

								/* Update cache value */
								synchronized (hmmdCache) {
									synchronized (hmmdCacheMask) {
										hmmdCache[idx] = hmmd;
										hmmdCacheMask[idx] = true;
									}
								}
							} else {
								hmmd = hmmdCache[idx];
							}

							tmp[binIndex(hmmd, binnum)] += 1;
						}
					}

					/* Increment color structure descriptor bins */
					synchronized (csd) {
						for (int i = 0; i < tmp.length; i++) {
							if (tmp[i] > 0)
								csd[i] += 1;
						}
					}
				}
			}
		}
	}

	private static double log2(double x) {
		return Math.log(x) / Math.log(2.0f);
	}

	private static int binIndex(float[] hmmd, int binnum) {
		float hue = hmmd[0], sum = hmmd[4];
		int index = 0, subspace = subspace(hmmd, binnum);

		if (binnum == BIN256) {
			index += HUE256_QUANT_MULT_SUM256_QUANT[subspace];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM256_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE256_QUANT[subspace])
					* SUM256_QUANT[subspace];
		} else if (binnum == BIN128) {
			index += HUE128_QUANT_MULT_SUM128_QUANT[subspace];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM128_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE128_QUANT[subspace])
					* SUM128_QUANT[subspace];
		} else if (binnum == BIN64) {
			index += HUE64_QUANT_MULT_SUM64_QUANT[subspace];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM64_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE64_QUANT[subspace])
					* SUM64_QUANT[subspace];
		} else {

			index += HUE32_QUANT_MULT_SUM32_QUANT[subspace];
			index += (int) ((sum / RGB_MAX_VALUE) * SUM32_QUANT[subspace])
					+ (int) ((hue / HUE_MAX_VALUE) * HUE32_QUANT[subspace])
					* SUM32_QUANT[subspace];
		}

		return index;
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
			 * 20 bits.
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

		BufferedImage img2 = ImageIO.read(new File("image.orig/5.jpg"));
		BufferedImage img1 = ImageIO.read(new File("image.orig/6.jpg"));
		ColorStructureDescriptorImplementation csdImp = new ColorStructureDescriptorImplementation();
		int[] csd1 = csdImp.extractCSD(img2, 256);
		int[] csd2 = csdImp.extractCSD(img1, 256);
		// int[] csd3 = ColorStructureDescriptorImplementation.extractCSD(img2,
		// 64);

		System.out.println(csdImp.distance(csd1, csd2));
	}

	public static void printArray(int[] arr) {
		for (int i = 0; i < arr.length; i++) {
			System.out.printf("%3d ", arr[i]);
		}
	}

}

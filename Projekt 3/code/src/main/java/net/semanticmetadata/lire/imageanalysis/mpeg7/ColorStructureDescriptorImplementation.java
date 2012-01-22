package net.semanticmetadata.lire.imageanalysis.mpeg7;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ColorStructureDescriptorImplementation {

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

			if (r < 0 | g < 0 | b < 0 | r > 255
					| g > 255 | b > 255)
				throw new IllegalArgumentException();

			float hmmd[] = new float[5];

			float max = Math.max(Math.max(r, g), Math.max(g, b)), min = Math
					.min(Math.min(r, g), Math.min(g, b));

			float diff = max - min, sum = (max + min) / 2f, hue = 0f;

			/* According to HSV color space */
			if (max == min)
				hue = 0f;
			else if (max == r)
				hue = 60 * (g - b) / diff;
			else if (max == g)
				hue = 60 * (2f + (b - r) / diff);
			else if (max == b)
				hue = 60 * (4f + (r - g) / diff);

			if (hue < 0)
				hue += 360f;

			hmmd[0] = hue;
			hmmd[1] = max;
			hmmd[2] = min;
			hmmd[3] = diff;
			hmmd[4] = sum;

			return hmmd;
		}
	}

	public static void main(String args[]) throws Exception {
		//BufferedImage img = ImageIO.read(new File("image.orig/1.jpg"));
		//float[] hmmd = HMMD.rgb2hmmd(156, 206, 239);

		//for (int i = 0; i < hmmd.length; i++)
		//	System.out.println(hmmd[i]);
	}

}

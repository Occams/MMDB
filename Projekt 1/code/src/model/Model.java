package model;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.imageio.ImageIO;

public class Model extends Observable {

	private File currentFile;
	private BufferedImage image;

	public void readImage(File file) throws IOException {
		this.image = ImageIO.read(file);
		this.currentFile = file;
		setChanged();
		notifyObservers();
	}

	public BufferedImage getImage() {
		return image;
	}

	public void saveImage() throws IOException {
		ImageIO.write(image, getExtension(currentFile), currentFile);
	}

	public void saveImage(File file) throws IOException {
		ImageIO.write(image, getExtension(file), file);
	}

	/**
	 * Applies linear filtering to the currently loaded image. </br>
	 * <code>Pixel := Pixel * alpha + beta</code>
	 * 
	 * @param alpha
	 *            filter parameter
	 * @param beta
	 *            filter parameter
	 * @throws NoImageLoadedException
	 *             if no image was loaded beforehand, i.e. {@code readImage()}
	 *             has never been invoked successfully.
	 */
	public void hkImage(int alpha, int beta) throws NoImageLoadedException {

		if (image == null)
			throw new NoImageLoadedException();

		/* Use the standard library for Non-sRGB images */
		if (!image.getColorModel().getColorSpace().isCS_sRGB()) {
			new RescaleOp(alpha, beta, null).filter(image, image);
		} else {

			/* Extract RGB values */
			int w = image.getWidth(), h = image.getHeight();
			int[] pixmap = new int[w * h];
			image.getRGB(0, 0, w, h, pixmap, 0, w);

			/* Apply filter to each pixel value */
			for (int i = 0; i < pixmap.length; i++) {

				// int alphaC = hkOp8bit(alpha, beta, (pixmap[i] >> 24) & 0xff);
				int alphaC = (pixmap[i] >> 24) & 0xff;
				int red = hkOp8bit(alpha, beta, (pixmap[i] >> 16) & 0xff);
				int green = hkOp8bit(alpha, beta, (pixmap[i] >> 8) & 0xff);
				int blue = hkOp8bit(alpha, beta, pixmap[i] & 0xff);
				pixmap[i] = (alphaC << 24) + (red << 16) + (green << 8) + blue;
			}

			image.setRGB(0, 0, w, h, pixmap, 0, w);
		}

		setChanged();
		notifyObservers();
	}

	/**
	 * Applies linear filtering to a 8-Bit Pixel value.
	 * 
	 * @param alpha
	 *            linear filter parameter
	 * @param beta
	 *            linear filter parameter
	 * @param pixel
	 *            pixel value
	 * @return <code> Math.min(Math.max(pixel * alpha + beta, 0), 255)</code>
	 */
	private int hkOp8bit(int alpha, int beta, int pixel) {
		return Math.min(Math.max(pixel * alpha + beta, 0), 255);
	}

	public void smootheImage(String type) throws NoImageLoadedException {
		if (image == null)
			throw new NoImageLoadedException();

		// Non-RGB images
		if (!image.getColorModel().getColorSpace().isCS_sRGB()) {
			BufferedImageOp op = new ColorConvertOp(
					ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
			image = op.filter(image, null);
		}

		// Init kernel
		Kernel kernel;

		if (type.equals("Mittelwert")) {
			kernel = Kernel.getInstance(Kernel.SMOOTHING_AVERAGE);
		} else if (type.equals("Konisch")) {
			kernel = Kernel.getInstance(Kernel.SMOOTHING_KONE);
		} else if (type.equals("Pyramide")) {
			kernel = Kernel.getInstance(Kernel.SMOOTHING_PYRAMID);
		} else {
			throw new IllegalArgumentException();
		}

		// Apply kernel
		int w = image.getWidth(), h = image.getHeight();
		int[] pixmap = new int[w * h];
		image.getRGB(0, 0, w, h, pixmap, 0, w);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				pixmap[y * w + x] = kernel.apply(pixmap, y * w + x, w);
			}
		}

		image.setRGB(0, 0, w, h, pixmap, 0, w);
		setChanged();
		notifyObservers();
	}

	public void transformImageToGreyScale() throws NoImageLoadedException {
		if (image == null) {
			throw new NoImageLoadedException();
		} else {
			BufferedImageOp op = new ColorConvertOp(
					ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			image = op.filter(image, null);

			setChanged();
			notifyObservers();
		}
	}

	public void negativImage() throws NoImageLoadedException {
		hkImage(-1, 255);
	}

	public String getExtension(File file) {
		String suffix = "";
		String[] comps = file.getName().split("\\.");
		suffix = comps[comps.length - 1];
		if (suffix.equals("jpg")) {
			suffix = "jpeg";
		}
		return suffix;
	}
}

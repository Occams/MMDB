public void hkImage(int alpha, int beta) throws NoImageLoadedException {
	if (image == null)
		throw new NoImageLoadedException();

		// Non-RGB images
	if (!image.getColorModel().getColorSpace().isCS_sRGB()) {
		new RescaleOp(alpha, beta, null).filter(image, image);
	} else {
		int w = image.getWidth(), h = image.getHeight();
		int[] pixmap = new int[w * h];
		image.getRGB(0, 0, w, h, pixmap, 0, w);
		
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

private int hkOp8bit(int alpha, int beta, int pixel) {
	return Math.min(Math.max(pixel * alpha + beta, 0), 255);
}


public void negativImage() throws NoImageLoadedException {
	hkImage(-1, 255);
}
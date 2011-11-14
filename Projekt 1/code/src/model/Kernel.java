package model;

public class Kernel {

	private int[] kernel;
	private int size;
	public static final int FOLDING = 0, SMOOTHING_PYRAMID = 1,
			SMOOTHING_KONE = 2, SMOOTHING_AVERAGE = 3;
	private static final int ALPHA = -2, PYRAMID_K = 4, KONE_K = 6;

	public Kernel(int n) {
		if (n < 1 || n % 2 == 0)
			throw new IllegalArgumentException();

		size = n;
		kernel = new int[n * n];
	}

	public int getSize() {
		return size;
	}

	public int apply(int[] img, int i, int w) {
		int sumC = 0;

		// TODO: Watch out for div zero
		for (int j = 0; j < kernel.length; j++) {
			sumC += kernel[j];
		}

		int sumA = 0, sumR = 0, sumG = 0, sumB = 0;
		for (int y = -size / 2; y <= size / 2; y++)
			for (int x = -size / 2; x <= size / 2; x++) {
				int pixel = 0;

				if (i % w + x < w && i % w + x >= 0 && (i + x + y * w) >= 0
						&& (i + x + y * w) < img.length) {
					pixel = img[i + x + y * w];
				} else {
					pixel = img[i];
				}

				int alphaC = (pixel >> 24) & 0xff;
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = pixel & 0xff;

				sumA += alphaC * this.getCoefficient(x, y);
				sumR += red * this.getCoefficient(x, y);
				sumG += green * this.getCoefficient(x, y);
				sumB += blue * this.getCoefficient(x, y);
			}

		sumA = sumA / sumC;
		sumR = sumR / sumC;
		sumG = sumG / sumC;
		sumB = sumB / sumC;

		return (sumA << 24) + (sumR << 16) + (sumG << 8) + sumB;

	}

	public static Kernel getInstance(int type) {
		Kernel kernel;
		switch (type) {

		case FOLDING:
			kernel = new Kernel(3);
			kernel.setCoefficient(-1, -1, -1);
			kernel.setCoefficient(-1, 0, -1);
			kernel.setCoefficient(-1, 1, -1);
			kernel.setCoefficient(1, -1, 1);
			kernel.setCoefficient(1, 0, 1);
			kernel.setCoefficient(1, 1, 1);
			break;
		case SMOOTHING_PYRAMID:
			kernel = new Kernel(5);
			for (int x = -2; x <= 2; x++)
				for (int y = -2; y <= 2; y++) {
					float c = ALPHA * Math.max(Math.abs(x), Math.abs(y))
							+ PYRAMID_K;
					kernel.setCoefficient(x, y, Math.round(c));
				}
			break;

		case SMOOTHING_KONE:
			kernel = new Kernel(5);
			for (int x = -2; x <= 2; x++)
				for (int y = -2; y <= 2; y++) {
					float c = (float) (ALPHA
							* Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) + KONE_K);
					kernel.setCoefficient(x, y, Math.round(c));
				}
			break;

		case SMOOTHING_AVERAGE:
			kernel = new Kernel(5);
			for (int x = -2; x <= 2; x++)
				for (int y = -2; y <= 2; y++) {
					kernel.setCoefficient(x, y, 1);
				}
			break;
		default:
			kernel = new Kernel(1);
			kernel.setCoefficient(0, 0, 1);
			break;
		}

		return kernel;
	}

	public void setCoefficient(int x, int y, int val) {
		if (Math.abs(x) > size / 2 || Math.abs(y) > size / 2)
			throw new IndexOutOfBoundsException("Index out of bounds: x:" + x
					+ " y:" + y);

		kernel[(y + size / 2) * size + (x + size / 2)] = val;

	}

	public float getCoefficient(int x, int y) {
		if (Math.abs(x) > size / 2 || Math.abs(y) > size / 2)
			throw new IndexOutOfBoundsException("Index out of bounds: x:" + x
					+ " y:" + y);

		return kernel[(y + size / 2) * size + (x + size / 2)];

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				sb.append(kernel[i * size + j] + " ");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(Kernel.getInstance(Kernel.FOLDING));
		System.out.println(Kernel.getInstance(Kernel.SMOOTHING_KONE));
		System.out.println(Kernel.getInstance(Kernel.SMOOTHING_PYRAMID));
		System.out.println(Kernel.getInstance(Kernel.SMOOTHING_AVERAGE));
		System.out.println(Kernel.getInstance(-10));
	}
}

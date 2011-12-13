package dmms.jpeg;

import dmms.jpeg.spec.BlockI;
import dmms.jpeg.spec.DCTBlockI;
import dmms.jpeg.spec.DCTI;

public class StandardDCT implements DCTI {
	/*
	 * Matrixmult
	 */
	private static final float[][] DCT = new float[][] {
			{ 0.3536f, 0.3536f, 0.3536f, 0.3536f, 0.3536f, 0.3536f, 0.3536f,
					0.3536f },
			{ 0.4904f, 0.4157f, 0.2778f, 0.0975f, -0.0975f, -0.2778f, -0.4157f,
					-0.4904f },
			{ 0.4619f, 0.1913f, -0.1913f, -0.4619f, -0.4619f, -0.1913f,
					0.1913f, 0.4619f },
			{ 0.4157f, -0.0975f, -0.4904f, -0.2778f, 0.2778f, 0.4904f, 0.0975f,
					-0.4157f },
			{ 0.3536f, -0.3536f, -0.3536f, 0.3536f, 0.3536f, -0.3536f,
					-0.3536f, 0.3536f },
			{ 0.2778f, -0.4904f, 0.0975f, 0.4157f, -0.4157f, -0.0975f, 0.4904f,
					-0.2778f },
			{ 0.1913f, -0.4619f, 0.4619f, -0.1913f, -0.1913f, 0.4619f,
					-0.4619f, 0.1913f, },
			{ 0.0975f, -0.2778f, 0.4157f, -0.4904f, 0.4904f, -0.4157f, 0.2778f,
					-0.0975f, } };

	private static final double one_div_sqrt_two = 1d / Math.sqrt(2);

	@Override
	public DCTBlockI forward(BlockI b) {
		return forwardMatrixMult(b);
	}

	/*
	 * Given src as 8x8 matrix we calc dst = DCT * src * DCT^T
	 */
	private DCTBlockI forwardMatrixMult(BlockI b) {
		int[][] src = b.getData();
		int blocksize = src[0].length;

		if (blocksize != src.length)
			throw new IllegalArgumentException("Wrong block size");

		double[][] tmp = new double[blocksize][blocksize];
		double[][] dst = new double[blocksize][blocksize];

		/*
		 * tmp = DCT*src
		 */
		for (int y = 0; y < blocksize; y++) {
			for (int x = 0; x < blocksize; x++) {
				double sum = 0;
				for (int i = 0; i < blocksize; i++) {
					sum += DCT[y][i] * (src[i][x] - 128); // level shift
				}
				tmp[y][x] = sum;
			}
		}

		/*
		 * dst = tmp*DCT^T
		 */
		for (int y = 0; y < blocksize; y++) {
			for (int x = 0; x < blocksize; x++) {
				double sum = 0;
				for (int i = 0; i < blocksize; i++) {
					sum += tmp[y][i] * DCT[x][i];
				}
				dst[y][x] = sum;
			}
		}

		return new DCTBlock(dst);
	}

	private DCTBlockI forwardNaive(BlockI b) {
		int[][] src = b.getData();
		int blocksize = src[0].length;

		if (blocksize != src.length)
			throw new IllegalArgumentException("Wrong block size");

		double[][] dst = new double[blocksize][blocksize];

		for (int u = 0; u < blocksize; u++) {
			for (int v = 0; v < blocksize; v++) {
				double pre = c(u) * c(v) / 4;
				double sum = 0;
				for (int i = 0; i < blocksize; i++) {
					for (int j = 0; j < blocksize; j++) {
						sum += (src[i][j] - 128)
								* Math.cos((2 * i + 1) * u * Math.PI / 16)
								* Math.cos((2 * j + 1) * v * Math.PI / 16);
					}
				}
				dst[u][v] = pre * sum;
			}
		}

		return new DCTBlock(dst);
	}

	private double c(int i) {
		return i == 0 ? one_div_sqrt_two : 1;
	}

}

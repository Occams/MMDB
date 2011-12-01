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
		double[][] tmp = new double[BlockI.N][BlockI.N];
		double[][] dst = new double[BlockI.N][BlockI.N];

		/*
		 * tmp = DCT*src
		 */
		for (int y = 0; y < BlockI.N; y++) {
			for (int x = 0; x < BlockI.N; x++) {
				double sum = 0;
				for (int i = 0; i < BlockI.N; i++) {
					sum += DCT[y][i] * (src[i][x] - 128);
				}
				tmp[y][x] = sum;
			}
		}

		/*
		 * dst = tmp*DCT^T
		 */
		for (int y = 0; y < BlockI.N; y++) {
			for (int x = 0; x < BlockI.N; x++) {
				double sum = 0;
				for (int i = 0; i < BlockI.N; i++) {
					sum += tmp[y][i] * DCT[x][i];
				}
				dst[y][x] = sum;
			}
		}

		return new DCTBlock(dst);
	}

	private DCTBlockI forwardNaive(BlockI b) {
		int[][] src = b.getData();
		double[][] dst = new double[BlockI.N][BlockI.N];

		for (int u = 0; u < BlockI.N; u++) {
			for (int v = 0; v < BlockI.N; v++) {
				double pre = c(u) * c(v) / 4;
				double sum = 0;
				for (int i = 0; i < BlockI.N; i++) {
					for (int j = 0; j < BlockI.N; j++) {
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

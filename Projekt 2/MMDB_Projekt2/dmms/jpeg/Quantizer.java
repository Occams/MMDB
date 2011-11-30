package dmms.jpeg;

import dmms.jpeg.spec.BlockI;
import dmms.jpeg.spec.DCTBlockI;
import dmms.jpeg.spec.QuantizationI;
import dmms.jpeg.spec.YUVImageI;

public class Quantizer implements QuantizationI {
	private int qualityFactor = 80;

	public Quantizer(int qualityFactor) {
		this.qualityFactor = qualityFactor;
	}

	@Override
	public int[] getQuantumLuminance() {
		return scale(QUANTUM_LUMINANCE, getQualityScalingFactor());
	}

	@Override
	public int[] getQuantumChrominance() {
		return scale(QUANTUM_CHROMINANCE, getQualityScalingFactor());
	}

	@Override
	public BlockI quantizeBlock(DCTBlockI dctBlock, int compType) {
		int[] scaling;
		if (compType == YUVImageI.Y) {
			scaling = getQuantumLuminance();
		} else {
			scaling = getQuantumChrominance();
		}

		double[][] src = dctBlock.getData();
		int[][] dst = new int[BlockI.N][BlockI.N];
		for (int y = 0; y < BlockI.N; y++) {
			for (int x = 0; x < BlockI.N; x++) {
				int pos = y * BlockI.N + x;
				dst[y][x] = (int) (src[y][x] / scaling[pos]);
			}
		}
		return new Block(dst);
	}

	@Override
	public void setQualityFactor(int qualityFactor) {
		this.qualityFactor = qualityFactor;
	}

	private int[] scale(int[] arr, int quality_scaling_factor) {
		int[] lum = new int[arr.length];
		for (int i = 0; i < lum.length; i++) {
			lum[i] = clamp((int) ((arr[i] * quality_scaling_factor) / 100), 1,
					255);
		}
		return lum;
	}

	private int getQualityScalingFactor() {
		if (qualityFactor < 50) {
			return 5000 / qualityFactor;
		} else {
			return 200 - 2 * qualityFactor;
		}
	}

	private static int clamp(int val, int min, int max) {
		return Math.min(max, Math.max(min, val));
	}

}

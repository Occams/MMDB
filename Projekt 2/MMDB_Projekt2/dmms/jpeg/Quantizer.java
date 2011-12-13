package dmms.jpeg;

import dmms.jpeg.spec.BlockI;
import dmms.jpeg.spec.DCTBlockI;
import dmms.jpeg.spec.QuantizationI;
import dmms.jpeg.spec.YUVImageI;

public class Quantizer implements QuantizationI {
	private int qualityFactor = QuantizationI.DEFAULT_QUALITY_FACTOR;

	public Quantizer(int qualityFactor) {
		this.qualityFactor = qualityFactor;
	}

	@Override
	public int[] getQuantumLuminance() {
		return scale(QuantizationI.QUANTUM_LUMINANCE, getQualityScalingFactor());
	}

	@Override
	public int[] getQuantumChrominance() {
		return scale(QuantizationI.QUANTUM_CHROMINANCE,
				getQualityScalingFactor());
	}

	@Override
	public BlockI quantizeBlock(DCTBlockI dctBlock, int compType) {
		int[] scaling = compType == YUVImageI.Y ? getQuantumLuminance() : getQuantumChrominance();
		
		double[][] src = dctBlock.getData();
		int blocksize = src[0].length;

		if (blocksize != src.length)
			throw new IllegalArgumentException("Wrong block size");

		
		int[][] dst = new int[blocksize][blocksize];
		
		for (int y = 0; y < blocksize; y++) {
			for (int x = 0; x < blocksize; x++) {
				int pos = y * blocksize + x;
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
		int[] quantum = new int[arr.length];
		
		for (int i = 0; i < quantum.length; i++)
			quantum[i] = clamp((int) ((arr[i] * quality_scaling_factor + 50) / 100), 1,
					255);
		
		return quantum;
	}

	private int getQualityScalingFactor() {
			return (qualityFactor < 50) ? 5000 / qualityFactor : 200 - 2 * qualityFactor;

	}

	private static int clamp(int val, int min, int max) {
		return Math.min(max, Math.max(min, val));
	}

}

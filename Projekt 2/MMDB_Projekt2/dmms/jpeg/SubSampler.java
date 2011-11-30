package dmms.jpeg;

import java.awt.Dimension;

import dmms.jpeg.spec.ComponentI;
import dmms.jpeg.spec.SubSamplerI;
import dmms.jpeg.spec.YUVImageI;

public class SubSampler implements SubSamplerI {

	@Override
	public YUVImageI downSample(YUVImageI yuvImg, int samplingRatio) {
		YUVImageI image;
		if (samplingRatio == YUV_444) {
			image = yuvImg;
		} else {
			Component y = new Component(yuvImg.getComponent(YUVImageI.Y)
					.getData(), YUVImageI.Y);
			Component cb = sampleCX(yuvImg.getComponent(YUVImageI.Cb),
					samplingRatio, YUVImageI.Cb);
			Component cr = sampleCX(yuvImg.getComponent(YUVImageI.Cr),
					samplingRatio, YUVImageI.Cr);
			image = new YUVImage(y, cb, cr, samplingRatio);
		}
		return image;
	}

	private Component sampleCX(ComponentI component, int samplingRatio,
			int compType) {
		Dimension src_size = component.getSize();
		Dimension dst_size = resize(src_size, samplingRatio);
		Dimension stride = getSampleDistances(samplingRatio);

		int[][] src_data = component.getData();
		int[][] dst_data = new int[dst_size.height][dst_size.width];

		for (int y = 0; y < dst_size.height; y++) {
			for (int x = 0; x < dst_size.width; x++) {
				dst_data[y][x] = src_data[y * stride.height][x * stride.width];
			}
		}

		return new Component(dst_data, compType);
	}

	private Dimension resize(Dimension dim, int samplingRatio) {
		Dimension ret;
		switch (samplingRatio) {
		case YUV_444:
			ret = dim;
			break;
		case YUV_420:
			ret = new Dimension(dim.width / 2, dim.height / 2);
			break;
		default:
			ret = new Dimension(dim.width / 2, dim.height);
			break;
		}
		return ret;
	}

	private Dimension getSampleDistances(int samplingRatio) {
		Dimension ret;
		switch (samplingRatio) {
		case YUV_444:
			ret = new Dimension(1, 1);
			break;
		case YUV_420:
			ret = new Dimension(2, 2);
		default:
			ret = new Dimension(2, 1);
			break;
		}
		return ret;
	}
}

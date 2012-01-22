package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.imageanalysis.mpeg7.ColorStructureDescriptorImplementation;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

public class ColorStructureHistogram extends
		ColorStructureDescriptorImplementation implements LireFeature {

	@Override
	public byte[] getByteArrayRepresentation() {
		return SerializationUtils.toByteArray(ColorHistogram);
	}

	@Override
	public void setByteArrayRepresentation(byte[] in) {
		ColorHistogram = SerializationUtils.toFloatArray(in);
	}

	@Override
	public double[] getDoubleHistogram() {
		return ConversionUtils.toDouble(ColorHistogram);
	}
}

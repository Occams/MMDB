package net.semanticmetadata.lire.imageanalysis;

import java.awt.image.BufferedImage;

import net.semanticmetadata.lire.imageanalysis.mpeg7.ColorStructureDescriptorImplementation;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

public class ColorStructureDescriptor implements LireFeature {
	private int[] csd;

	@Override
	public void extract(BufferedImage bimg) {
		this.csd = ColorStructureDescriptorImplementation.extractCSD(bimg,
				ColorStructureDescriptorImplementation.BIN256);
	}

	public byte[] getByteArrayRepresentation() {
		return SerializationUtils.toByteArray(csd);
	}

	public void setByteArrayRepresentation(byte[] in) {
		csd = SerializationUtils.toIntArray(in);
	}

	public double[] getDoubleHistogram() {
		return ConversionUtils.toDouble(csd);
	}

	@Override
	public float getDistance(LireFeature feature) {
		if (feature != null && feature instanceof ColorStructureDescriptor) {
			return ColorStructureDescriptorImplementation.distance(
					((ColorStructureDescriptor) feature).csd, csd);
		} else {
			return 0;
		}
	}

	@Override
	public String getStringRepresentation() {
		StringBuilder builder = new StringBuilder("colorstructuredescriptor;");
		builder.append(csd.length + ";");
		for (int c : csd) {
			builder.append(c + " ");
		}
		return builder.toString();
	}

	@Override
	public void setStringRepresentation(String s) {
		String[] parts = s.split(";");
		if (parts.length != 3 && parts[0].equals("colorstructuredescriptor")) {
			throw new IllegalArgumentException(
					"This representation cannot be converted to this descriptor");
		} else {
			int length = Integer.parseInt(parts[1]);
			csd = new int[length];
			String[] elements = parts[2].split(" ");
			for (int i = 0; i < Math.min(length, elements.length); i++) {
				csd[i] = Integer.parseInt(elements[i]);
			}
		}
	}
}

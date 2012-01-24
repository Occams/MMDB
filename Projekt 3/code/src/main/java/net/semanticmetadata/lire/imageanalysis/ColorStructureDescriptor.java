package net.semanticmetadata.lire.imageanalysis;

import java.awt.image.BufferedImage;

import javax.naming.OperationNotSupportedException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import net.semanticmetadata.lire.imageanalysis.mpeg7.ColorStructureDescriptorImplementation;

public class ColorStructureDescriptor implements LireFeature {
	private int[] csd;

	@Override
	public void extract(BufferedImage bimg) {
		this.csd = ColorStructureDescriptorImplementation.extractCSD(bimg, ColorStructureDescriptorImplementation.BIN256);
	}

	@Override
	public byte[] getByteArrayRepresentation() {
		throw new NotImplementedException();
	}

	@Override
	public void setByteArrayRepresentation(byte[] in) {
		throw new NotImplementedException();
	}

	@Override
	public double[] getDoubleHistogram() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getDistance(LireFeature feature) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getStringRepresentation() {
		throw new NotImplementedException();
	}

	@Override
	public void setStringRepresentation(String s) {
		throw new NotImplementedException();
	}

}

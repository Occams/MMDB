public class ColorStructureDescriptor implements LireFeature {
	private int[] csd;
	
	@Override
	public String getStringRepresentation() {
		StringBuilder builder = new StringBuilder("colorstructuredescriptor;");
		builder.append(csd.length + ";");
		builder.append(csd.join(" "));
	}

	@Override
	public void setStringRepresentation(String s) {
		// Inverse Funktion von getStringRepresentation()
	}
}

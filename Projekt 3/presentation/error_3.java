private float[] reQuantization(float[] colorHistogramTemp) {

	float[] uniformCSD = new float[colorHistogramTemp.length];

	for (int i=0; i < colorHistogramTemp.length; i++)
	{
		// System.out.print(colorHistogramTemp[i] + " ");
		// System.out.println(" --- ");

		if (colorHistogramTemp[i] == 0) uniformCSD[i] = 0; //
		else if (colorHistogramTemp[i] < 0.000000001f) uniformCSD[i] = (int)Math.round( ( ((float)colorHistogramTemp[i] - 0.32f ) / ( 1f - 0.32f ) ) * 140 + (115 - 35 - 35 - 20 - 25 - 1) );	 // (int)Math.round((1f / 0.000000001f) * (float)colorHistogramTemp[i]);
		else if (colorHistogramTemp[i] < 0.037f) uniformCSD[i] = (int)Math.round( ( ((float)colorHistogramTemp[i] - 0.32f ) / ( 1f - 0.32f ) ) * 140 + (115 - 35 - 35 - 20 - 25));
		else if (colorHistogramTemp[i] < 0.08f) uniformCSD[i] = (int)Math.round( ( ((float)colorHistogramTemp[i] - 0.32f ) / ( 1f - 0.32f ) ) * 140 + (115 - 35 - 35 - 20));
		else if (colorHistogramTemp[i] < 0.195f) uniformCSD[i] = (int)Math.round( ( ((float)colorHistogramTemp[i] - 0.32f ) / ( 1f - 0.32f ) ) * 140 + (115 - 35 - 35));
		else if (colorHistogramTemp[i] < 0.32f) uniformCSD[i] = (int)Math.round( ( ((float)colorHistogramTemp[i] - 0.32f ) / ( 1f - 0.32f ) ) * 140 + (115 - 35));
		else if (colorHistogramTemp[i] > 0.32f) uniformCSD[i] = (int)Math.round( ( ((float)colorHistogramTemp[i] - 0.32f ) / ( 1f - 0.32f ) ) * 140 + 115);
		else uniformCSD[i] = (int)Math.round((255f / 1f) * (float)colorHistogramTemp[i]);

	}

	return uniformCSD;
}

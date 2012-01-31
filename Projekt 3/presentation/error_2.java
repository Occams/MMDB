int ir[][] = temp;
int ig[][] = temp;
int ib[][] = temp;

int iH[][]    = temp;
int iMax[][]  = temp;
int iMin[][]  = temp;
int iDiff[][] = temp;
int iSum[][]  = temp;

...

for (int ch = 0; ch < (int)height - 1; ch++) {
	for (int cw = 0; cw < (int)width - 1; cw++) {
		ir[ch][cw] = mf.getPixelAt(ch,cw).getComponent(0); // RED
		ig[ch][cw] = mf.getPixelAt(ch,cw).getComponent(1); // GREEN
		ib[ch][cw] = mf.getPixelAt(ch,cw).getComponent(2); // BLUE

		int[] tempHMMD = RGB2HMMD(ir[ch][cw],ig[ch][cw],ib[ch][cw]);
		iH[ch][cw]   = tempHMMD[0];						// H
		iMax[ch][cw] = tempHMMD[1];						// Max
		iMin[ch][cw] = tempHMMD[2]; 						// Min
		iDiff[ch][cw]= tempHMMD[3]; 						// Diff
		iSum[ch][cw] = tempHMMD[4]; 						// Sum
		}
}

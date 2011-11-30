package dmms.jpeg;

import dmms.jpeg.spec.*;

/**
 * Pattern implementation of interface YUVImageI.
 * @author Roland Tusch
 * @version 1.0
 */

public class YUVImage implements YUVImageI {

    Component Y;
    Component Cb;
    Component Cr;
    int samplingRatio;

    public YUVImage(Component Y, Component Cb, Component Cr, int samplingRatio) {
        if (samplingRatio >= SubSamplerI.YUV_444 && samplingRatio <= SubSamplerI.YUV_420) {
            this.Y = Y;
            this.Cb = Cb;
            this.Cr = Cr;
            this.samplingRatio = samplingRatio;
        }
        else
            throw new IllegalArgumentException("Invalid sampling ratio.");
    }

    public ComponentI getComponent(int compType) {
        switch (compType) {
            case YUVImageI.Y:   return Y;
            case YUVImageI.Cb:  return Cb;
            case YUVImageI.Cr:  return Cr;
            default:            throw new IllegalArgumentException("Invalid component type.");
        }
    }

    public int getSamplingRatio() {
        return samplingRatio;
    }
}

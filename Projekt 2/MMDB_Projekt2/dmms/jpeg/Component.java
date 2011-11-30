package dmms.jpeg;

import dmms.jpeg.spec.*;
import java.awt.*;

/**
 * Pattern implementation of interface ComponentI.
 * @author Roland Tusch
 * @version 1.0
 */

public class Component implements ComponentI {

    int[][] compData;
    int compType;

    /**
     * Constructs a component with the given component data and component type.
     * @param compData the pixel data of the component. Must not be null.
     * @param compType the component type. Must be one of YUVImageI.Y, YUVImageI.Cb
     * or YUVImageI.Cr.
     * @exception IllegalArgumentException if one of the arguments fails its
     *            requirements
     */
    public Component(int[][] compData, int compType) {
        if (compData != null) {
            if (compType >= YUVImageI.Y && compType <= YUVImageI.Cr) {
                this.compData = compData;
                this.compType = compType;
            }
            else
                throw new IllegalArgumentException("Invalid component type.");
        }
        else
            throw new IllegalArgumentException("Component data must not be null.");
    }

    public Dimension getSize() {
        return new Dimension(compData[0].length,compData.length);
    }

    public int[][] getData() {
        return compData;
    }

    public int getType() {
        return compType;
    }
}

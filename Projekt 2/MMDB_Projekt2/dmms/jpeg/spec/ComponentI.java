package dmms.jpeg.spec;

import java.awt.*;

/**
 * A component represents a color component in the YUV (or YCbCr) color space.
 * @author Roland Tusch
 * @version 1.0
 */

public interface ComponentI {

    /**
     * Delivers the dimension of this component.
     */
    Dimension getSize();

    /**
     * Returns the component's pixel values.
     * @return the pixel values of this component as an integer matrix.
     */
    int[][] getData();

    /**
     * Delivers the type of this component.
     * @return the component type. Must be a value equal to YUVImageI.Y,
     *         YUVImageI.Cb or YUVImageI.Cr.
     */
    int getType();

}

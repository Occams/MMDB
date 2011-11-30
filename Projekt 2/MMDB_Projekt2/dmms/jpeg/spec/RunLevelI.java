package dmms.jpeg.spec;

/**
 * Defines the functionality, a RUN-LEVEL object has to provide.
 * @author Roland Tusch
 * @version 1.0
 */

public interface RunLevelI {

    /**
     * Delivers the RUN of this RUN-LEVEL.
     * @return the RUN of zeros
     */
    int getRun();

    /**
     * Delivers the LEVEL of this RUN-LEVEL.
     * @return the value following the RUN of zeros
     */
    int getLevel();
}
package dbridge.analysis.region.api;

import dbridge.analysis.region.exceptions.RegionAnalysisException;
import dbridge.analysis.region.regions.ARegion;

/**
 * Created by ek on 4/5/16.
 */
public interface RegionAnalysis<T> {
    /**
     * @param region The region on which analysis is to done. It is passed implicitly from inside Region.analyze().
     *               So, the region passed can be assumed safely to be of the correct type.
     * @return the result of analysis (of type T)
     * Note that this function is not to be called explicitly, it is called from inside Region.analyze()
     */
    T run(ARegion region) throws RegionAnalysisException;
}

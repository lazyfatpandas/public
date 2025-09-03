package eqsql.dbridge.analysis.eqsql.analysis;

import dbridge.analysis.region.api.RegionAnalysis;
import dbridge.analysis.region.exceptions.RegionAnalysisException;
import dbridge.analysis.region.regions.ARegion;
import eqsql.dbridge.analysis.eqsql.expr.DIR;

/**
 * Created by K. Venkatesh Emani on 12/18/2016.
 * Abstract class that is used to contain logic which is common across all
 * "*RegionAnalyzer" classes. For example: dir.updateRegion(region)
 */
public abstract class AbstractDIRRegionAnalyzer implements RegionAnalysis<DIR>{
    @Override
    public DIR run(ARegion region) throws RegionAnalysisException {
        DIR dir = constructDIR(region);
        dir.updateRegion(region);
        return dir;
    }

    public abstract DIR constructDIR(ARegion region) throws RegionAnalysisException;
}

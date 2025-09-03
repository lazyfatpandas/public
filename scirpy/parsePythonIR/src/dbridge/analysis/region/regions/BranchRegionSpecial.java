package dbridge.analysis.region.regions;

import soot.Unit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchRegionSpecial extends ARegion {

    public BranchRegionSpecial(ARegion theOnlyPred, ARegion theSibling, ARegion region) {
        super(theOnlyPred, region);
        theOnlyPred.changeSuccessorOfPreds(this);
        Set<ARegion> newSuccessors = new HashSet<ARegion>();
        newSuccessors.add(theSibling);
        List<ARegion> reg = new ArrayList<ARegion>();
        reg.add(region);
        reg.add(theOnlyPred);
        this.setSuccRegionsSpecial(new ArrayList(newSuccessors), reg);
        this.regionType = RegionType.BranchSpecialRegion;
    }

    @Override
    public Unit firstStmt() {
        return getSubRegions().get(0).firstStmt();
    }

    @Override
    public Unit lastStmt() {
        return getSubRegions().get(1).lastStmt();
    }

    @Override
    public List<Unit> getUnits() {
        List<Unit> units = new ArrayList<>();
        units.addAll(getSubRegions().get(0).getUnits());
        units.addAll(getSubRegions().get(1).getUnits());
        return units;
    }
}

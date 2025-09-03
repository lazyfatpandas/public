package dbridge.analysis.region.regions;

import soot.Unit;

import java.util.ArrayList;
import java.util.List;

public class SequentialRegion extends ARegion {

    public SequentialRegion(ARegion first, ARegion second) {
        super(first, second);
        first.changeSuccessorOfPreds(this);
        this.setSuccRegionsFrom(second);
        this.regionType = RegionType.SequentialRegion;
        //bhu - alittle hack
        if(first.isTrueRegion || second.isTrueRegion){
            this.isTrueRegion=true;
        }
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

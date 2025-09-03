package dbridge.analysis.region.regions;

import soot.Unit;

import java.util.ArrayList;
import java.util.List;

public class LoopRegion extends ARegion {
    public LoopRegion(ARegion loopHead, ARegion loopBody) {
        super(loopHead, loopBody);
        ARegion newPredecessor = loopHead.getPredRegions().get(0).equals(loopBody) ? loopHead.getPredRegions().get(1)
                : loopHead.getPredRegions().get(0);
        newPredecessor.getSuccRegions().remove(loopHead);
        newPredecessor.addSuccRegion(this);

        ARegion newSuccessor = loopHead.getSuccRegions().get(0).equals(loopBody) ? loopHead.getSuccRegions().get(1) :
                loopHead.getSuccRegions().get(0);
        newSuccessor.getPredRegions().remove(loopHead);
        this.addSuccRegion(newSuccessor);
        this.regionType = RegionType.LoopRegion;
    }

    @Override
    public Unit firstStmt() {
        return getSubRegions().get(1).firstStmt();
    }

    @Override
    public Unit lastStmt() {
        return getSubRegions().get(0).lastStmt();
    }

    @Override
    public List<Unit> getUnits() {
        List<Unit> units = new ArrayList<>();
        units.addAll(getSubRegions().get(1).getUnits());
        units.addAll(getSubRegions().get(0).getUnits());
        return units;
    }
}

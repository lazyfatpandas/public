package regions;
//import dbridge.analysis.region.api.RegionAnalysis ;
import dbridge.analysis.region.regions.ARegion;
import dbridge.analysis.region.regions.RegionGraph;

import soot.Body;

public class RegionDriver {
    ARegion topRegion;//

    public void buildRegion(Body body){
    RegionGraph regionGraph = new RegionGraph(body);
    topRegion = regionGraph.getHeads().get(0);
    //System.out.println(topRegion.toString2());
    //System.out.println(topRegion.toString());

    }

    public ARegion getTopRegion() {
        return topRegion;
    }

    public void setTopRegion(ARegion topRegion) {
        this.topRegion = topRegion;
    }
}

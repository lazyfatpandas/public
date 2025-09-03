package dbridge.analysis.region.regions;

import ir.Stmt.FunctionDefStmt;
import soot.Body;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.*;
import soot.jimple.internal.JIfStmt;
import soot.toolkits.graph.*;

import java.util.*;

public class RegionGraph implements DirectedGraph<ARegion> {

    private BlockGraph blockGraph;
    public UnitGraph ug;
    private List<ARegion> regions = new ArrayList<ARegion>();

    Map<Integer, Block> indexToBlock = new HashMap<Integer, Block>();
    Map<Integer, Block> indexToBlockOriginal = new HashMap<Integer, Block>();
    private UnitGraph ugClone;
    private BlockGraph bgClone;
    private Body bodyClone;

    Map<Block, Block> loopExitToHead = new HashMap<Block, Block>();

    public RegionGraph(Body b) {
        this.blockGraph = new BriefBlockGraph(b);
        UnitGraph unitGraph = new BriefUnitGraph(b);
        this.ug = unitGraph;

        bodyClone = (Body) b.clone();
        ugClone = new BriefUnitGraph(bodyClone);
        bgClone = new BriefBlockGraph(bodyClone);

        for (int i = 0; i < blockGraph.getBlocks().size(); i++) {
            indexToBlockOriginal.put(i, blockGraph.getBlocks().get(i));
        }

        for (int i = 0; i < blockGraph.getBlocks().size(); i++) {
            Block curBlock = blockGraph.getBlocks().get(i);

            if (curBlock.getTail() instanceof GotoStmt) {
                if (curBlock.getSuccs().size() == 1) {
                    Block onlySucc = curBlock.getSuccs().get(0);
                    if (loopExitToHead.containsKey(onlySucc)) {
                        Block loopHead = loopExitToHead.get(onlySucc);
                        List<Block> blks = new ArrayList<Block>();
                        blks.add(loopHead);
                        curBlock.setSuccs(blks);

                        blks = new ArrayList<Block>();
                        List<Block> preds = loopHead.getPreds();
                        blks.addAll(preds);
                        blks.add(curBlock);
                        loopHead.setPreds(blks);

                        blks = new ArrayList<Block>();
                        preds = onlySucc.getPreds();
                        blks.addAll(preds);
                        blks.remove(curBlock);
                        onlySucc.setPreds(blks);

                    }
                }
            }
        }

        for (int i = 0; i < blockGraph.getBlocks().size(); i++) {
            indexToBlock.put(i, bgClone.getBlocks().get(i));
        }

        List<Block> blocks = blockGraph.getBlocks();

        for (int i = 0; i < blocks.size(); ) {
            Block blk = blocks.get(i);
            Block succBlock = getSuccBlockToMerge(blk);
            if (succBlock == null) {
                i++;
                continue;
            }
            /****/
            //succ block should be loop head
            if (succBlock.getSuccs().size() == 2) {
                if (succBlock.getSuccs().get(0).getSuccs().size() == 1) {
                    if (succBlock.getSuccs().get(0).getSuccs().get(0).equals(succBlock)) {
                        i++;
                        continue;
                    }
                }
                if (succBlock.getSuccs().get(0).getSuccs().size() > 1) {
                    if (succBlock.getSuccs().get(0).getSuccs().contains(succBlock)) {
                        i++;
                        continue;
                    }
                }
                if (succBlock.getSuccs().get(1).getSuccs().size() == 1) {
                    if (succBlock.getSuccs().get(1).getSuccs().get(0).equals(succBlock)) {
                        i++;
                        continue;
                    }
                }
                if (succBlock.getSuccs().get(1).getSuccs().size() > 1) {
                    if (succBlock.getSuccs().get(1).getSuccs().contains(succBlock)) {
                        i++;
                        continue;
                    }
                }
            }
        }

        for (int i = 0; i < blocks.size(); i++) {
            Block blk = blocks.get(i);
            Unit u = blk.getTail();
            if (u instanceof IfStmt) {
                IfStmt ifStmt = (IfStmt) u;
            }
        }

        Body body = blockGraph.getBody();
        unitGraph = new BriefUnitGraph(body);
        ug = unitGraph;
        wrapBlocks(unitGraph);
        //bhu-a little hack to identify true region
        setTrueRegion(regions);
        if (regions.size() > 1) {
            constructRegions();
        }

    }


    private Block getSuccBlockToMerge(Block b) {
        Unit curBlockTail = b.getTail();
        if (!(curBlockTail instanceof IfStmt))
            return null;
        List<Block> succBlocks = b.getSuccs();

        // If last if-else block, then nothing to merge - Tejas
        if (succBlocks.size() != 2)
            return null;
        // Multiple if-else can be considered as multiple nested single if-else. So, each conditional region
        // preconditions satisfied - Tejas
        Block blk1 = succBlocks.get(0);
        Block blk2 = succBlocks.get(1);

        //These two conditions would never be true according to me, and the return value will always be null - Tejas
        if (blk1.getSuccs().contains(blk2)) {
            // if (blk1.getHead().equals(blk1.getTail())
            // &&
            if (blk1.getTail() instanceof IfStmt && isCandidate(blk1))
                return blk1;
        }
        if (blk2.getSuccs().contains(blk1)) {
            // if (blk2.getHead().equals(blk2.getTail())
            // &&
            if (blk2.getTail() instanceof IfStmt && isCandidate(blk2))
                return blk2;
        }
        return null;
    }

    private boolean isCandidate(Block b) {
        Iterator<Unit> units = b.iterator();
        while (units.hasNext()) {
            Stmt stmt = (Stmt) units.next();
            if (stmt instanceof IfStmt)
                continue;
            if (stmt instanceof AssignStmt) {
                AssignStmt as = (AssignStmt) stmt;


                //Tejas. Condition that Block b should not contain definition of any local on which its condition
				// depends
                boolean proceed = true;
                if (b.getTail() instanceof JIfStmt) {
                    Unit uTail = b.getTail();
                    List<ValueBox> lTail = uTail.getUseBoxes();
                    Iterator iter = b.iterator();
                    while (iter.hasNext()) {
                        Unit u = (Unit) iter.next();
                        for (ValueBox vbt : lTail) {
                            for (ValueBox vbi : u.getDefBoxes()) {
                                if (vbi.getValue().toString().equals(vbt.getValue().toString())) {
                                    proceed = false;
                                    break;
                                }
                            }
                            if (proceed == false)
                                break;
                        }
                        if (proceed == false)
                            break;
                    }
                }


                if (!as.getLeftOp().toString().startsWith("$") || !proceed)
                    return false;
            } else
                return false;

        }
        return true;
    }

    private void constructRegions() {
        boolean moreIterations = true;

        while (moreIterations) {
            moreIterations = false;
            ARegion merged = null;

            for (ARegion r : regions) {
                if (!r.canMerge()) {
                    continue;
                }
                merged = r.merge();
                moreIterations = true;
                break;
            }
            if (moreIterations) {
                regions.add(merged);
                regions.removeAll(merged.getSubRegions());
            }
         }
    }

    //adds all blocks except exception catch blocks to 'regions', and 'basicRegions' - Tejas
    private void wrapBlocks(UnitGraph unitGraph) {
        Map<Block, ARegion> basicRegions = new HashMap<Block, ARegion>();
        for (Block b : blockGraph) {
            //Modified By bhu
//            if(isFunctionDefinitionStmt(b)){
//                FunctionRegion fr = new FunctionRegion(b);
//
//                regions.add(fr);
//            }
//            else if (!isCatchBlock(b)) {
            //modified by bhu Ends, uncomment next line
                if (!isCatchBlock(b)) {
                List<Block> preds = b.getPreds();
                List<Block> nonPreds = new ArrayList<Block>();
                boolean addBlock = true;
                int count = 0;
                for (Block blk : preds) {
                    if (!basicRegions.containsKey(blk)) {
                        //loop body - Tejas
                        if (!(b.getSuccs().size() == 1 && b.getPreds().size() == 1 && b.getSuccs().get(0).equals(b
								.getPreds().get(0)))) {
                            //when will this condition satisfy in addition to the above one? - Tejas
                            if (blk.getIndexInMethod() < b.getIndexInMethod()) {
                                count++;
                                nonPreds.add(blk);
                            }
                        }
                    }
                }
                // blocks other than loop body and first block- Tejas
                if (count == preds.size() && b.getIndexInMethod() != 0)
                    addBlock = false;
                // loop body. anything else? - Tejas
                if (count < preds.size()) {
                    List<Block> newPreds = new ArrayList<Block>();
                    for (Block blk : preds) {
                        if (!(nonPreds.contains(blk)))
                            newPreds.add(blk);

                    }
                    b.setPreds(newPreds);
                }
                // handling a block which has only one stmt - goto and 1 pred
                // and 1 succ : ignore such a block

                // Loop in catch block
                if (b.getSuccs().size() == 1 && b.getPreds().size() == 1 && b.getPreds().get(0).equals(b.getSuccs().get(0))) {
                    count = 0;
                    Block blkLoopHead = b.getPreds().get(0);
                    List<Block> preds2 = blkLoopHead.getPreds();
                    List<Block> nonPreds2 = new ArrayList<Block>();
                    for (Block blk : preds2) {
                        if (!basicRegions.containsKey(blk)) {
                            if (blk.getIndexInMethod() < blkLoopHead.getIndexInMethod()) {
                                count++;
                            }
                        }
                    }
                    if (count == preds2.size() && blkLoopHead.getIndexInMethod() != 0)
                        addBlock = false;
                }

                if (addBlock) {
                    if (b.getHead() instanceof IdentityStmt) {
                        IdentityStmt i = (IdentityStmt) b.getHead();

                        if (i.getRightOp().toString().equals("@caughtexception"))
                            continue;
                    }
                    Region r = new Region(b);

                    regions.add(r);
                    basicRegions.put(b, r);
                }
            }
        }
        for (ARegion r : regions) {
            r.init(basicRegions);
        }
    }

    private boolean isCatchBlock(Block blk) {
        if (blk.getHead() instanceof IdentityStmt) {
            IdentityStmt i = (IdentityStmt) blk.getHead();
            if (i.getRightOp().toString().equals("@caughtexception"))
                return true;
        }
        return false;
    }

    private void setTrueRegion(List<ARegion> regions){
        for (ARegion r : regions) {
            if(r.getSuccRegions().size()==2){
                r.getSuccRegions().get(0).setTrueRegion(true);
            }
        }
    }
    @Override
    public List<ARegion> getHeads() {
        return regions;
    }

    @Override
    public List<ARegion> getTails() {
        return null;
    }

    @Override
    public List<ARegion> getPredsOf(ARegion s) {
        return Arrays.asList(new ARegion[]{s.getParent()});
    }

    @Override
    public List<ARegion> getSuccsOf(ARegion s) {
        return s.getSubRegions();
    }

    @Override
    public int size() {
        return regions.size();
    }

    @Override
    public Iterator<ARegion> iterator() {
        return regions.iterator();
    }

    public String print() {
        return regions.get(0).print();
    }

    public  boolean isFunctionDefinitionStmt(Block b){
        Unit unit=b.getHead();
        if(unit instanceof FunctionDefStmt){
            return true;
        }
        return false;
    }
}

package rewrite.pd2;
/* bhu created on 21/5/20  */


import DataFileAnalysis.model.MetaData;
import soot.Unit;

public class InputFileDataTypeMapper {
    int lineno;
    Unit unit;
    MetaData md;

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public MetaData getMd() {
        return md;
    }

    public void setMd(MetaData md) {
        this.md = md;
    }
}

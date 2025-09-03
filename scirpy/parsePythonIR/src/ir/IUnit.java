package ir;

import java.io.Serializable;
import java.util.List;

public interface IUnit extends Serializable {
    public boolean isLeaf();

    //List of child units in the order of list
    public List<IUnit> getChildIUnits();

    //Parent unit of this Iunit
    public IUnit getParentIUnit();



}

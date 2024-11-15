package cia.northboat.pojo;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class QuadTree {
    private Location p;
    private Element[] x;
    private Element s_x, t_x;
    private QuadTree[] subtree;

    public QuadTree(){
        subtree = new QuadTree[4];
    }

    public QuadTree(Location o){
        this.p = o;
        subtree = new QuadTree[4];
    }

    public QuadTree(Location p, Element[] x, Element s_x, Element t_x){
        this.p = p;
        this.x = x;
        this.s_x = s_x;
        this.t_x = t_x;
        subtree = new QuadTree[4];
    }


    public void setSubtree(QuadTree t, int i){
        this.subtree[i] = t;
    }
}

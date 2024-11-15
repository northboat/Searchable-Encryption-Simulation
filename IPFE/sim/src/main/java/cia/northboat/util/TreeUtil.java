package cia.northboat.util;

import cia.northboat.pojo.Cy;
import cia.northboat.pojo.Location;
import cia.northboat.pojo.QuadTree;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

public class TreeUtil {
    // 映射为 z 阶码（前缀码）
    public static String toZCode(int x1, int y1, int x2, int y2){
        if(x1 <= x2){
            if(y1 >= y2){
                return "00";
            } else {
                return "01";
            }
        } else if(y1 >= y2){
            return "10";
        }
        return "11";
    }

    public static String toZCode(Location p1, Location p2){
        return toZCode(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }


    public static QuadTree enc(String pre, Location p, Location o){
        String z = toZCode(p, o);
        Element[] x = IPFEUtil.hashStr2ZrArr(pre + z);
        return IPFEUtil.keyGen(x, p);
    }

    public static QuadTree enc(String str, Location p){
        Element[] x = IPFEUtil.hashStr2ZrArr(str);
        return IPFEUtil.keyGen(x, p);
    }

    public static boolean insert(String pre, Location p, QuadTree root){
        Location o = root.getP();
        String z = toZCode(p, o); // 增加的前缀
        int i = switch (z) { // 找下标
            case "00" -> 0;
            case "01" -> 1;
            case "10" -> 2;
            case "11" -> 3;
            default -> -1;
        };
        if(i == -1){
            return false;
        }
        QuadTree n = enc(pre + z, p); // 当前前缀总和
        QuadTree cur = root.getSubtree()[i];
        if(cur == null){
            root.setSubtree(n, i);
            return true;
        }
        return insert(pre+z, p, root.getSubtree()[i]);
    }


    public static QuadTree build(Location[] P){
        Location o = new Location(0, 0);
        QuadTree root = new QuadTree(o);
        for(Location p: P){
            insert("", p, root);
        }
        return root;
    }


    public static QuadTree search(QuadTree root, QuadTree p){

        return null;
    }

}

package cia.northboat.sim;

import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

public class pMatch {
    private static int n;
    private static Pairing bp;
    private static Field G, GT, Zr;
    private static Element g, g1, g2, x1, x2, f1, t, t_i, EK, D_i, E_i;

    public static Element f(Element x){
        return x1.add(f1.mul(x)).getImmutable();
    }

    public static void init(Pairing bp, Field G, Field GT, Field Zr, int n,
                            Element g, Element x1, Element x2, Element f1, Element t, Element t_i){
        pMatch.bp = bp;
        pMatch.G = G;
        pMatch.GT = GT;
        pMatch.Zr = Zr;
        pMatch.n = n;
        pMatch.g = g;
        pMatch.x1 = x1;
        pMatch.g1 = g.powZn(x1).getImmutable();
        pMatch.x2 = x2;
        pMatch.g2 = g.powZn(x2).getImmutable();
        pMatch.f1 = f1;
        pMatch.t = t;
        pMatch.t_i = t_i;

        pMatch.EK = g.powZn(f(t).div(x1)).getImmutable();

        Element t1 = t.negate().div(t_i.sub(t)).getImmutable();
        Element t2 = t_i.negate().div(t.sub(t_i)).getImmutable();

        pMatch.D_i = g2.powZn(f(t_i).mul(t1)).getImmutable();
        pMatch.E_i = g2.powZn(x1.mul(t2)).getImmutable();
    }


    public static Element C1, C2, C3, C4;
    public static void enc(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element r1 = Zr.newRandomElement().getImmutable(), r2 = Zr.newRandomElement().getImmutable();
        C1 = g2.powZn(r2).mul(HashUtil.hashZrArr2G(g, w).powZn(r1)).getImmutable();
        C2 = g1.powZn(r1).getImmutable();
        C3 = EK.powZn(r2).getImmutable();
        C4 = g.powZn(r2).getImmutable();
//        System.out.println(C1);
//        System.out.println(C2);
//        System.out.println(C3);
//        System.out.println(C4);
//
//        System.out.println();
    }


    public static Element T1, T2, T3, T4;
    public static void trap(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element s = Zr.newRandomElement().getImmutable();
        T1 = g1.powZn(s);
        T2 = HashUtil.hashZrArr2G(g, w).powZn(s).getImmutable();
        T3 = E_i.powZn(s).getImmutable();
        T4 = D_i.powZn(s).getImmutable();
//        System.out.println(T1);
//        System.out.println(T2);
//        System.out.println(T3);
//        System.out.println(T4);
//        System.out.println();
    }


    public static boolean match(){
        Element left = bp.pairing(C1, T1).getImmutable();
        Element p1 = bp.pairing(C2, T2).getImmutable();
        Element p2 = bp.pairing(C3, T3).getImmutable();
        Element p3 = bp.pairing(C4, T4).getImmutable();
        Element right = p1.mul(p2).mul(p3).getImmutable();
        System.out.println("pMatch left: " + left);
//        System.out.println(p1);
//        System.out.println(p2);
//        System.out.println(p3);
        System.out.println("pMatch right: " + right);
        return left.isEqual(right);
    }

}

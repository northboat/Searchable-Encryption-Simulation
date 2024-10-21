package cia.northboat.sim;

import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

public class SA_PAUKS {
    private static int n;
    private static Pairing bp;
    private static Field G, GT, Zr;
    private static Element g, h;


    private static Element sk_s, sk_r, sk_ss, sk_rs, sk_cs;
    public static Element pk_s, pk_r, pk_ss, pk_rs, pk_cs;
    public static void init(Pairing bp, Field G, Field GT, Field Zr, Element g, int n,
                            Element s, Element r, Element ss, Element rs, Element cs){
        SA_PAUKS.bp = bp;
        SA_PAUKS.G = G;
        SA_PAUKS.GT = GT;
        SA_PAUKS.Zr = Zr;
        SA_PAUKS.g = g;
        SA_PAUKS.n = n;

        SA_PAUKS.sk_s = s;
        SA_PAUKS.pk_s = g.powZn(s).getImmutable();

        SA_PAUKS.sk_r = r;
        SA_PAUKS.pk_r = g.powZn(r).getImmutable();

        SA_PAUKS.sk_ss = ss;
        SA_PAUKS.pk_ss = g.powZn(ss).getImmutable();

        SA_PAUKS.sk_rs = rs;
        SA_PAUKS.pk_rs = g.powZn(rs).getImmutable();

        SA_PAUKS.sk_cs = cs;
        SA_PAUKS.pk_cs = g.powZn(cs).getImmutable();
    }


    public static Element c1, c2, c3, c_1, c_2, c_3, c_4, c_5;
    public static void enc(String str){
        Element x1 = Zr.newRandomElement().getImmutable(), x2 = Zr.newRandomElement().getImmutable();
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
//        for(Element e: w){
//            System.out.println(e);
//        }
//        System.out.println();

        c1 = HashUtil.hashZrArr2GWithTwoFact(pk_ss, pk_rs, w).powZn(sk_s).mul(g.powZn(x1.add(x2))).getImmutable();

//        System.out.println(c1);

        c2 = pk_rs.powZn(x1).getImmutable();
        c3 = pk_cs.powZn(x2).getImmutable();

        Element x3 = Zr.newRandomElement().getImmutable();
        c_1 = c1.powZn(sk_ss).mul(g.powZn(x3.mul(sk_ss))).getImmutable();
        c_2 = c2.powZn(sk_ss).getImmutable();
        c_3 = c3.powZn(sk_ss).mul(pk_cs.powZn(x3.mul(sk_ss))).getImmutable();
        c_4 = pk_s.powZn(sk_ss).getImmutable();
        c_5 = pk_s;

    }


    public static Element t1, t2, t3, t_1, t_2, t_3, t_4, t_5;
    public static void trap(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
//        for(Element e: w){
//            System.out.println(e);
//        }
//        System.out.println();

        Element y1 = Zr.newRandomElement().getImmutable(), y2 = Zr.newRandomElement().getImmutable();
        t1 = HashUtil.hashZrArr2GWithTwoFact(pk_ss, pk_rs, w).powZn(sk_r).mul(g.powZn(y1.add(y2))).getImmutable();

//        System.out.println(t1);

        t2 = pk_ss.powZn(y1).getImmutable();
        t3 = pk_cs.powZn(y2).getImmutable();

        Element y3 = Zr.newRandomElement().getImmutable();
        t_1 = t1.powZn(sk_rs).mul(g.powZn(y3.mul(sk_rs))).getImmutable();
        t_2 = t2.powZn(sk_rs).getImmutable();
        t_3 = t3.powZn(sk_rs).mul(pk_cs.powZn(y3.mul(sk_rs))).getImmutable();
        t_4 = pk_r.powZn(sk_rs).getImmutable();
        t_5 = pk_r;
    }

    public static boolean test(){
        Element part1 = bp.pairing(c_1.div(c_3.powZn(sk_cs.invert())), t_4).getImmutable();
        Element part2 = bp.pairing(c_2, t_5).getImmutable();
        Element part3 = bp.pairing(t_1.div(t_3.powZn(sk_cs.invert())), c_4).getImmutable();
        Element part4 = bp.pairing(t_2, c_5).getImmutable();

        Element left = part1.div(part2).getImmutable();
        Element right = part3.div(part4).getImmutable();

        System.out.println("SA-PAUKS left: " + left);
        System.out.println("SA-PAUKS right: " + right);

        return left.isEqual(right);
    }
}

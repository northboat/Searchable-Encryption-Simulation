package cia.northboat.sim;

import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

public class CR_IMA {
    private static int n;
    private static Pairing bp;
    private static Field G, GT, Zr;
    private static Element g, msk, s, sk_svr, id_a, id_b, sk_b;
    public static Element mpk, pk_svr, pk_b;

    private static Element[] sk_a;
    public static Element[] pk_a;
    public static void init(Pairing bp, Field G, Field GT, Field Zr, int n,
                            Element g, Element x, Element t,
                            Element s, Element id_a, Element id_b){
        CR_IMA.bp = bp;
        CR_IMA.G = G;
        CR_IMA.GT = GT;
        CR_IMA.Zr = Zr;
        CR_IMA.n = n;
        CR_IMA.g = g;
        CR_IMA.s = s;
        CR_IMA.msk = x;
        CR_IMA.mpk = g.powZn(x).getImmutable();
        CR_IMA.sk_svr = t;
        CR_IMA.pk_svr = g.powZn(t);

        CR_IMA.id_a = id_a;
        sk_a = new Element[2];
        sk_a[0] = s;
        sk_a[1] = HashUtil.hashZr2G(g, id_a).powZn(x).getImmutable();
        pk_a = new Element[2];
        pk_a[0] = g.powZn(s).getImmutable();
        pk_a[1] = HashUtil.hashZr2G(g, id_a).getImmutable();
//        System.out.println("nmsl");

        CR_IMA.id_b = id_b;
        sk_b = HashUtil.hashZr2G(g, id_b).powZn(x).getImmutable();
        pk_b = HashUtil.hashZr2G(g, id_b).getImmutable();
//        System.out.println("wdnmd");
    }

    public static Element Ci;
    public static void enc(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element k = bp.pairing(sk_a[1], pk_b).getImmutable();
        Element h1 = HashUtil.hashGT2GWithZrArr(Zr, k, w).getImmutable();
        Element h2 = HashUtil.hashG2ZrWithZr(Zr, pk_a[0], h1).getImmutable();
        Ci = pk_svr.powZn(s.mul(h2)).getImmutable();
    }


    public static Element Ti;
    public static void trap(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        // 这个地方？？？
        Element k = bp.pairing(pk_a[1], sk_b).getImmutable();
        // 太奇怪了太奇怪了太奇怪了
        Ti = HashUtil.hashGT2GWithZrArr(Zr, k, w).getImmutable();
    }

    public static boolean match(){
        Element left = Ci;
        Element right = pk_a[0].powZn(sk_svr.mul(HashUtil.hashG2ZrWithZr(Zr, pk_a[0], Ti))).getImmutable();
        System.out.println("CR-IMA left: " + left);
        System.out.println("CR-IMA right: " + right);
        return left.isEqual(right);
    }
}

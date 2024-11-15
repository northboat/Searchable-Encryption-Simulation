package cia.northboat.sim;

import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

public class dIBAEKS {
    private static int n;
    private static Pairing bp;
    private static Field G, GT, Zr;
    private static Element g, h;


    private static Element sk_svr, id_r, id_s, a;
    public static Element pk_svr;
    public static void init(Pairing bp, Field G, Field GT, Field Zr, int n,
                            Element g, Element h, Element t){
        dIBAEKS.bp = bp;
        dIBAEKS.G = G;
        dIBAEKS.GT = GT;
        dIBAEKS.Zr = Zr;
        dIBAEKS.n = n;
        dIBAEKS.g = g;
        dIBAEKS.h = h;

        dIBAEKS.sk_svr = t;
        dIBAEKS.pk_svr = g.powZn(t).getImmutable();

        id_r = Zr.newRandomElement().getImmutable();
        id_s = Zr.newRandomElement().getImmutable();
        a = Zr.newRandomElement().getImmutable();
    }

    public static Element C1, C2, C3;
    public static void enc(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element sk_id = HashUtil.hashZr2G(g, id_s).powZn(a).getImmutable();
        Element k = bp.pairing(sk_id, HashUtil.hashZr2G(g, id_r));

        Element s = Zr.newRandomElement().getImmutable();
        C1 = bp.pairing(HashUtil.hashGT2GWithZrArr(G, k, w), pk_svr.powZn(s)).getImmutable();
        C2 = g.powZn(s).getImmutable();
        C3 = h.powZn(s).getImmutable();
    }

    public static Element T1, T2;
    public static void trap(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element sk_id = HashUtil.hashZr2G(g, id_r).powZn(a).getImmutable();
        Element k = bp.pairing(HashUtil.hashZr2G(g, id_s), sk_id);

        Element r = Zr.newRandomElement().getImmutable();
        T1 = HashUtil.hashGT2GWithZrArr(G, k, w).mul(h.powZn(r)).getImmutable();
        T2 = g.powZn(r).getImmutable();
    }

    public static boolean test(){
        Element p = bp.pairing(T2.powZn(sk_svr), C3).getImmutable();
        Element left = C1.mul(p).getImmutable();
        Element right = bp.pairing(T1.powZn(sk_svr), C2);
        System.out.println("dIBAEKS left: " + left);
        System.out.println("dIBAEKS right: " + right);
        return left.isEqual(right);
    }
}

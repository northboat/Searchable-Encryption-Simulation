package cia.northboat.sim;

import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

public class PAUKS {

    private static int n;
    private static Pairing bp;
    private static Field G, GT, Zr;
    private static Element g;


    // 系统的密钥
    private static Element sk_r1, sk_r2, sk_r3, sk_r4; // x1, x2, x3, x5
    public static Element pk_r1, pk_r2, pk_r3, pk_r4; // g^x1, g^x2, g^x3

    // 用户的密钥
    private static Element sk_s; // y
    public static Element pk_s; // g^y

    public static void init(Pairing bp, Field G, Field GT, Field Zr, Element g, int n,
                            Element x1, Element x2, Element x3, Element x4, Element y){
        PAUKS.bp = bp;
        PAUKS.G = G;
        PAUKS.GT = GT;
        PAUKS.Zr = Zr;
        PAUKS.g = g;
        PAUKS.n = n;

        PAUKS.sk_r1 = x1;
        PAUKS.sk_r2 = x2;
        PAUKS.sk_r3 = x3;
        PAUKS.sk_r4 = x4;

        PAUKS.pk_r1 = g.powZn(x1).getImmutable();
        PAUKS.pk_r2 = g.powZn(x2).getImmutable();
        PAUKS.pk_r3 = g.powZn(x3).getImmutable();

        PAUKS.sk_s = y;
        PAUKS.pk_s = g.powZn(y).getImmutable();
    }


    public static Element C1, C2, C3, C4, C5;
    public static void enc(String str){
        Element r1 = Zr.newRandomElement().getImmutable(), r2 = Zr.newRandomElement().getImmutable();
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);

//        for(Element e: w){
//            System.out.println(e);
//        }
//        System.out.println();

        C1 = pk_r2.powZn(HashUtil.hashZrArr2Zr(Zr, pk_r1.powZn(sk_s), w)).mul(pk_r3).powZn(r1).getImmutable();

//        System.out.println("wdnmd: " + HashUtil.hashZrArr2Zr(Zr, pk_r1.powZn(sk_s), w));

        C2 = g.powZn(r1).getImmutable();
        C3 = pk_r2.powZn(HashUtil.hashG2Zr(Zr, pk_r1.powZn(sk_s))).mul(pk_r3).powZn(r2).mul(g.powZn(HashUtil.hashG2Zr(Zr, pk_r1.powZn(sk_s)).mul(r1))).getImmutable();
        C4 = HashUtil.hashZrArr2G(g, w).powZn(r2).getImmutable();
        C5 = HashUtil.hash4G(C1, C2, C3, C4).powZn(r1).getImmutable();

//        System.out.println(C1);
//        System.out.println(C2);
//        System.out.println(C3);
//        System.out.println(C4);
    }

    public static Element T1, T2;
    public static void trap(String str){
        Element r3 = Zr.newRandomElement().getImmutable();
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
//        for(Element e: w){
//            System.out.println(e);
//        }
        T1 = g.powZn(r3.div(sk_r2.mul(HashUtil.hashZrArr2Zr(Zr, pk_s.powZn(sk_r1), w)).add(sk_r3))).getImmutable();

//        System.out.println("nmsl: " + HashUtil.hashZrArr2Zr(Zr, pk_s.powZn(sk_r1), w));

        T2 = g.powZn(r3).getImmutable();
    }

    public static boolean test(){
        System.out.println("C1: " + C1);
        System.out.println("C2: " + C2);
        System.out.println("T1: " + T1);
        System.out.println("T2: " + T2 + "\n");
        Element left = bp.pairing(C1, T1);
        Element right = bp.pairing(C2, T2);
        System.out.println("PAUKS verify test left: " + left);
        System.out.println("PAUKS verify test right: " + right);
        return left.isEqual(right);
    }


    private static Element uk_s1, uk_s2;
    public static void update(){
        uk_s1 = HashUtil.hashG2Zr(Zr, pk_s.powZn(sk_r1)).getImmutable();
        uk_s2 = sk_r4.div(sk_r2.mul(HashUtil.hashG2Zr(Zr, pk_s.powZn(sk_r1))).add(sk_r3)).getImmutable();
    }

    public static Element C6;
    public static Element[] C;
    public static void updEnc(){
        C6 = C3.div(C2.powZn(uk_s1)).powZn(uk_s2).getImmutable();

        Element left = bp.pairing(HashUtil.hash4G(C1, C2, C3, C4), C2);
        Element right = bp.pairing(C5, g);

        if(left.isEqual(right)){
            C = new Element[5];
            C[0] = C1; C[1] = C2; C[2] = C3; C[3] = C4; C[4] = C5;
        } else {
            C = new Element[6];
            C[0] = C1; C[1] = C2; C[2] = C3; C[3] = C4; C[4] = C5; C[5] = C6;
        }

    }



    public static Element T_1, T_2;
    public static void constTrap(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element r = Zr.newRandomElement().getImmutable();
        T_1 = g.powZn(sk_r4.mul(r)).getImmutable();
        T_2 = HashUtil.hashZrArr2G(g, w).powZn(r).getImmutable();

//        System.out.println(C6);
//        System.out.println(T_2);

//        System.out.println(T1);
//        System.out.println(T2);

//        for(Element e: w){
//            System.out.println(e);
//        }
    }

    public static boolean updTest(){
        Element left = bp.pairing(C4, T_1);
        Element right = bp.pairing(C6, T_2);
        System.out.println("PAUKS update test left: " + left);
        System.out.println("PAUKS update test right: " + right);
//        System.out.println(GT.newZeroElement());
        return left.isEqual(right);
    }
}

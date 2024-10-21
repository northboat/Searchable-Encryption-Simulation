package cia.northboat.sim;

import cia.northboat.util.BitUtil;
import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.math.BigInteger;
import java.util.*;

public class DuMSE {
    private static int n;
    private static Pairing bp;
    private static Field G1, G2, GT, Zr;
    private static Element g1, g2, q;


    private static Element sk_o, sk_i, sk_fs, sk_ss;

    public static Element pk_o, pk_i, pk_fs;
    public static Element[] pk_ss;

    public static Map<String, Element> record;

    public static void init(Pairing bp, Field G1, Field G2, Field GT, Field Zr, int n, Element g1,
                            Element g2, Element q, Element x1, Element x2, Element x3, Element x4,
                            Element id, Element sk_id, Element AI_o, Element A, Element B, Element C){
        DuMSE.bp = bp;
        DuMSE.G1 = G1;
        DuMSE.G2 = G2;
        DuMSE.GT = GT;
        DuMSE.Zr = Zr;
        DuMSE.n = n;
        DuMSE.g1 = g1;
        DuMSE.g2 = g2;
        DuMSE.q = q;

        DuMSE.sk_o = x1;
        DuMSE.pk_o = g2.powZn(sk_o.invert()).getImmutable();

        DuMSE.sk_i = x2;
        DuMSE.pk_i = g2.powZn(sk_i.invert()).getImmutable();

        DuMSE.sk_fs = x3;
        DuMSE.pk_fs = g1.powZn(sk_fs).getImmutable();

        DuMSE.sk_ss = x4;
        DuMSE.pk_ss = new Element[2];
        pk_ss[0] = g1.powZn(sk_ss).getImmutable();
        pk_ss[1] = g2.powZn(sk_ss).getImmutable();


        // 在 enc 中用到的用户数据
        record = new HashMap<>();
        DuMSE.id = id;
        DuMSE.sk_id = sk_id;

        // 在 trap 中用到的用户数据
        DuMSE.AI_o = AI_o;
        T_i = new Element[3];
        T_i[0] = A;
        T_i[1] = B;
        T_i[2] = C;
    }


    public static Element id;
    private static Element sk_id;
    public static Element C1, C2, C3;
    public static void enc(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element r = Zr.newRandomElement().getImmutable(), L = Zr.newRandomElement().getImmutable();
        Element p = Zr.newRandomElement().getImmutable(), pr = Zr.newRandomElement().getImmutable();

        C2 = g1.powZn(r).getImmutable();
        Element h = BitUtil.connect(Zr, id, L, sk_id);

        // 这里涉及到位运算：异或和位连接
        if(!record.containsKey(str)){
            record.put(str, L);
            Element p1 = bp.pairing(HashUtil.hashZrArr2G(g1, w).powZn(p), pk_ss[1]).powZn(sk_o.invert()).getImmutable();
            C1 = HashUtil.hashGT2G(Zr, p1).getImmutable();

            Element p2 = bp.pairing(g1.powZn(pr), pk_ss[1]).powZn(sk_o.invert()).getImmutable();
            Element p3 = HashUtil.hashGT2G(Zr, p2).getImmutable();
            C3 = BitUtil.xor(Zr, p3, h);

        } else {
            C1 = record.get(str);
            Element p1 = bp.pairing(g1.powZn(pr), pk_ss[1]).powZn(sk_o.invert()).getImmutable();
            Element p2 = HashUtil.hashGT2G(Zr, p1);
            BigInteger i1 = p2.toBigInteger();
            BigInteger i2 = h.toBigInteger();
            BigInteger res = i1.xor(i2);
            C3 = Zr.newElement(res.mod(Zr.getOrder())).getImmutable();
        }
    }

    public static Element AI_o;
    public static Element[] T_i;
    public static Element T1, T2, T3, T_1, T_2;
    public static void trap(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element r1 = Zr.newRandomElement().getImmutable(), r2 = Zr.newRandomElement().getImmutable();
        T1 = pk_ss[0].powZn(r1).getImmutable();
        T2 = pk_fs.powZn(r2).getImmutable();
        T3 = HashUtil.hashZrArr2G(g1, w).powZn(sk_i).mul(g1.powZn(r1.add(r2))).getImmutable();

        T_1 = T_i[0];
        T_2 = bp.pairing(T_i[2].div(T_i[1].powZn(sk_fs.invert())), AI_o).getImmutable();
    }


    public static boolean test(){
        Element p1 = bp.pairing(T_1, AI_o).getImmutable();
        Element L = HashUtil.hashGT2G(Zr, T_2.powZn(sk_ss).div(p1)).getImmutable();

        Element U1 = bp.pairing(C2, AI_o).powZn(sk_ss).getImmutable();
        Element U2 = C3;
        Element p2 = HashUtil.hashGT2G(Zr, U1.powZn(sk_i)).getImmutable();
        BigInteger i1 = p2.toBigInteger();
        BigInteger i2 = U2.toBigInteger();
        BigInteger res = i1.xor(i2);
        Element Pt = Zr.newElement(res.mod(Zr.getOrder()));

        System.out.println(L);
        System.out.println(Pt);
        return L.isEqual(Pt);
    }

}

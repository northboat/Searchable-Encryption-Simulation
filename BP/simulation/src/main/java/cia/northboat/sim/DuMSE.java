package cia.northboat.sim;

import cia.northboat.util.BitUtil;
import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class DuMSE {
    private static int n;
    private static Pairing bp;
    private static Field G1, G2, GT, Zr;
    private static Element g1, g2;
    private static int q;


    private static Element sk_o, sk_i, sk_fs, sk_ss;

    public static Element pk_o, pk_i, pk_fs;
    public static Element[] pk_ss;

    public static Map<String, Element> record;

    public static void init(Pairing bp, Field G1, Field G2, Field GT, Field Zr, int n,
                            Element g1, Element g2, Element x1, Element x2, Element x3, Element x4,
                            Element sk_id, Element AI_o){
        DuMSE.bp = bp;
        DuMSE.G1 = G1;
        DuMSE.G2 = G2;
        DuMSE.GT = GT;
        DuMSE.Zr = Zr;
        DuMSE.n = n;
        DuMSE.g1 = g1;
        DuMSE.g2 = g2;
        DuMSE.q = 1024;

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
        id = Zr.newElement(123456).getImmutable();
        DuMSE.sk_id = Zr.newElement(123456789).getImmutable();

        // 在 trap 中用到的用户数据
        DuMSE.AI_o = AI_o;
    }


    public static Element id;
    private static Element sk_id;
    public static Element C1, C2, C3;
    public static void enc(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element r = Zr.newRandomElement().getImmutable();

        // log(q) 位的随机数
        Element L = BitUtil.random(Zr, (int)Math.log(q));

        // 不知道哪来的参数
        Element p = Zr.newRandomElement().getImmutable(), pr = Zr.newRandomElement().getImmutable();

        C2 = g1.powZn(r).getImmutable();

        System.out.println("L: " + L);

        // 连接，这里的连接如果超出了 Zr 群的上限，将会除余，可能会影响后续的分割，即还原不了
        Element h = BitUtil.connect(Zr, id, L, sk_id);

        Element s = BitUtil.split(Zr, h, id, sk_id);

        // 这里涉及到一个异或操作，我直接把他处理为 BigInteger 的 xor 操作，应该没问题
        // 并且在哈希的时候限定了哈希值的长度，这个处理很有可能有问题
        if(!record.containsKey(str)){
            record.put(str, L);
            Element p1 = bp.pairing(HashUtil.hashZrArr2G(g1, w).powZn(p), pk_ss[1]).powZn(sk_o.invert()).getImmutable();

            // 没问题
            C1 = HashUtil.hashGT2ZrWithQ(Zr, p1, (int)Math.log(q)).getImmutable();

            Element p2 = bp.pairing(g1.powZn(pr), pk_ss[1]).powZn(sk_o.invert()).getImmutable();
            // 这里有问题捏，只要在某一区间就行 [-6, 24]，太神奇了，Math.log(q) 也行
            Element p3 = HashUtil.hashGT2ZrWithQ(Zr, p2, (int)Math.log(id.toBigInteger().bitLength() + q + sk_id.toBigInteger().bitLength()));

            C3 = BitUtil.xor(Zr, p3, h);

        } else {
            C1 = record.get(str);
            Element p1 = bp.pairing(g1.powZn(pr), pk_ss[1]).powZn(sk_o.invert()).getImmutable();
            Element p2 = HashUtil.hashGT2ZrWithQ(Zr, p1, (int)Math.log(id.toBigInteger().bitLength() + q + sk_id.toBigInteger().bitLength()));
            C3 = BitUtil.xor(Zr, p2, h);
            record.put(str, L);
        }
    }

    public static Element AI_o;
    public static Element T1, T2, T3, T_1, T_2;
    // 陷门计算应该没问题，不涉及一些敏感操作
    public static void trap(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
        Element r1 = Zr.newRandomElement().getImmutable(), r2 = Zr.newRandomElement().getImmutable();
        T1 = pk_ss[0].powZn(r1).getImmutable();
        T2 = pk_fs.powZn(r2).getImmutable();
        T3 = HashUtil.hashZrArr2G(g1, w).powZn(sk_i).mul(g1.powZn(r1.add(r2))).getImmutable();

        T_1 = T1.getImmutable();
        T_2 = bp.pairing(T3.div(T2.powZn(sk_fs.invert())), AI_o).getImmutable();
    }


    public static boolean search(){
        Element p1 = bp.pairing(T_1, AI_o).getImmutable();
        Element p2 = T_2.powZn(sk_ss).getImmutable();

        // 又用到了这个哈希
        Element L1 = HashUtil.hashGT2ZrWithQ(Zr, p2.div(p1), (int)Math.log(q)).getImmutable();

        Element U1 = bp.pairing(C2, AI_o).powZn(sk_ss).getImmutable();
        Element U2 = C3;

        Element p3 = HashUtil.hashGT2ZrWithQ(Zr, U1.powZn(sk_i), (int)Math.log(q)).getImmutable();

        // 异或
        Element Msg = BitUtil.xor(Zr, p3, U2);
        // 分割
        Element Pt = BitUtil.split(Zr, Msg, id, sk_id);

        System.out.println("L': " + L1);
//        System.out.println("Pt: " + Pt);
        return false;
    }

}

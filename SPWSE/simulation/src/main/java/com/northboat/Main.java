package com.northboat;

import com.northboat.sim.SPWSE_1;
import com.northboat.sim.SPWSE_2;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class Main {

    private static final Pairing bp = PairingFactory.getPairing("a160.properties");
    // 加密单词长度，为 2n
    private static final int n = 20;
    // 主公钥
    public static Field G1, G2, GT, Zr;
    public static Element g1, g2;


    // 主私钥
    private static Element v;
    private static Element[] S, T;

    // 算法二要添加的公钥和私钥
    public static Element pkc, pka;
    private static Element a1, b1, b2, xt, xti;


    // 一组用于加密的随机数
    private static Element r, m, s;
    private static Element[] R;

    // 初始化生成元
    static{
        System.out.println("系统参数初始化\n=====================");
        G1 = bp.getG1();
        G2 = bp.getG2();
        GT = bp.getGT();
        Zr = bp.getZr();
        g1 = G1.newRandomElement().getImmutable();
        g2 = G2.newRandomElement().getImmutable();

        v = Zr.newRandomElement().getImmutable();

        S = new Element[2*n];
        T = new Element[2*n];

        for(int i = 0; i < 2*n; i++){
            S[i] = Zr.newRandomElement().getImmutable();
            T[i] = Zr.newRandomElement().getImmutable();
        }

        System.out.println("n: " + n + "\ng1: " + g1 + "\ng2: " + g2 + "\nv: " + v);
        System.out.println("S[i]:\t\t\t\t\t\t\t\t\t\t\t\t\tT[i]:");
        for(int i = 0; i < 2*n; i++){
            System.out.println(S[i] + "\t\t" + T[i]);
        }



        pkc = G1.newRandomElement().getImmutable();
        pka = G1.newRandomElement().getImmutable();
        a1 = Zr.newRandomElement().getImmutable();
        b1 = Zr.newRandomElement().getImmutable();
        b2 = Zr.newRandomElement().getImmutable();
        xt = Zr.newRandomElement().getImmutable();
        xti = Zr.newRandomElement().getImmutable();

        // 一组用于加密的随机数和随机数组
        // 在算法一/二中作为加密的参数
        r = Zr.newRandomElement().getImmutable();
        m = Zr.newRandomElement().getImmutable();
        s = Zr.newRandomElement().getImmutable();
        R = new Element[n];
        for(int i = 0; i < n; i++){
            // 取随机数填充 R
            R[i] = Zr.newRandomElement().getImmutable();
        }
        System.out.println("随机数 r: " + r + "\n随机数 m: " + m);
        System.out.print("随机数组 R: ( ");
        for(Element e: R){
            System.out.print(e + " ");
        }
        System.out.println(")");

        System.out.println("=====================\n");
    }


    public static void main(String[] args) {

        int k = 10; // 循环次数
        String w = "wouldwouldwouldwould"; // 关键词
        String t = "wouldwo**dwouldwould"; // 搜索字

        SPWSE_1.setup(bp, n, G1, G2, GT, Zr, g1, g2, v, S, T, r, R, m);
        SPWSE_2.setup(bp, n, G1, GT, Zr, g1, pkc, pka, a1, b1, b2, xt, v, r, xti, S, T, R, m, s);
        test1(w, t, k);
        test2(w, t, k);
    }

    public static void test1(String w, String t, int m){

        long startTime1 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            SPWSE_1.encode(w);
        }
        long endTime1 = System.currentTimeMillis();

        long startTime2 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            SPWSE_1.genKey(t);
        }
        long endTime2 = System.currentTimeMillis();


        boolean flag = false;
        long startTime3 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            flag = SPWSE_1.pairing();
        }
        long endTime3 = System.currentTimeMillis();


        System.out.println("算法Ⅰ对 " + w + " 和 " + t + " 的测试\n==========================");
        System.out.println("验证结果: " + flag);
        System.out.println("加密 " + w + " 时长: " + (double)(endTime1 - startTime1)/m + "ms");

        System.out.println("计算 " + t + " 陷门时长: " + (double)(endTime2 - startTime2)/m + "ms");
        System.out.println("匹配时长: " + (double)(endTime3 - startTime3)/m + "ms\n==========================\n\n");
    }


    public static void test2(String w, String t, int m){

        long startTime1 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            SPWSE_2.encode(w);
        }
        long endTime1 = System.currentTimeMillis();



        long startTime2 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            SPWSE_2.genKey(t);
        }
        long endTime2 = System.currentTimeMillis();


        boolean flag = false;
        long startTime3 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            flag = SPWSE_2.pairing();
        }
        long endTime3 = System.currentTimeMillis();

        System.out.println("算法Ⅱ对 " + w + " 和 " + t + " 的测试\n==========================");
        System.out.println("验证结果: " + flag);
        System.out.println("加密 " + w + " 时长: " + (double)(endTime1 - startTime1)/m + "ms");

        System.out.println("计算 " + t + " 陷门时长: " + (double)(endTime2 - startTime2)/m + "ms");
        System.out.println("匹配时长: " + (double)(endTime3 - startTime3)/m + "ms\n==========================\n\n");
    }

}

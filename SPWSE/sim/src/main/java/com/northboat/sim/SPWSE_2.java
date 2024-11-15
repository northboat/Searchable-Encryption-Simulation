package com.northboat.sim;

import com.northboat.util.StrUtil;
import it.unisa.dia.gas.jpbc.*;

public class SPWSE_2 {

    private static Pairing bp;
    // 加密单词长度，为 2n
    private static int n;



    // 系统公钥
    public static Field G1, GT, Zr;
    public static Element h, g, g1, pkc, pka, EK;
    // 系统私钥
    private static Element b1, b2, a1, xt, v;



    // 用户公钥
    public static Element Ni, Fi;
    public static Element[] H;
    // 用户私钥
    private static Element[] S, V;
    private static Element Ei, Di, Mi, xti;

    // 用于加密的随机数组
    private static Element[] R;


    public static Element f(Element x){
        return a1.mul(x).add(b1).getImmutable();
    }

    public static void setup(Pairing bp, int n, Field G1, Field GT, Field Zr, // 系统公钥
                             Element g, Element pkc, Element pka,
                             Element a1, Element b1, Element b2, Element xt, Element v, // 系统私钥
                             Element r, Element xti, Element[] S, Element[] V,
                             Element[] R, Element m, Element s){ // 参与计算的随机数

        // 系统私钥
        SPWSE_2.a1 = a1;
        SPWSE_2.b1 = b1;
        SPWSE_2.b2 = b2;
        SPWSE_2.xt = xt;
        SPWSE_2.v = v;

        // 系统公钥
        SPWSE_2.bp = bp;
        SPWSE_2.n = n;
        SPWSE_2.G1 = G1;
        SPWSE_2.GT = GT;
        SPWSE_2.Zr = Zr;
        SPWSE_2.g = g;
        SPWSE_2.g1 = g.powZn(b1).getImmutable();
        SPWSE_2.EK = g.powZn(f(xt).div(b1)).getImmutable();
        // 这里从g1改成了g
        SPWSE_2.h = g.powZn(v).getImmutable();
        SPWSE_2.pkc = pkc;
        SPWSE_2.pka = pka;


        // 初始化用户私钥
        SPWSE_2.Mi = r;
        SPWSE_2.xti = xti;
        SPWSE_2.Di = g.powZn(b2.mul(f(xti)).mul(xt.negate().div(xti.sub(xt)))).getImmutable();
        SPWSE_2.Ei = g1.powZn(b2.mul(xti.negate().div(xt.sub(xti)))).getImmutable();
        SPWSE_2.S = S;
        SPWSE_2.V = V;

        // 用户公钥
        SPWSE_2.Ni = g.powZn(r).getImmutable();
        SPWSE_2.Fi = g.powZn(r.mul(b2)).getImmutable();

        H = new Element[2*n];
        for(int i = 0; i < 2*n; i++){
            H[i] = g.powZn(S[i]).mul(h.powZn(V[i])).getImmutable();
        }

        SPWSE_2.R = R;
        SPWSE_2.m = m;
        SPWSE_2.s = s;

//        Element f1 = f(xti).mul(xt.negate().div(xti.sub(xt)));
//        Element f2 = f(xt).mul(xti.negate().div(xt.sub(xti)));
//        System.out.println(f1.add(f2));
//        System.out.println(b1);
    }

    // 密文
    public static Element C1, C2, C3;
    public static Element[] E;
    public static void encode(String word){
        Element[] W = StrUtil.mapping(Zr, word, n);
        Element[] X = new Element[2*n];
        for(int i = 0; i < n; i++){
            X[2*i] = Mi.mul(R[i]).mul(W[i]).getImmutable();
            X[2*i+1] = Mi.negate().mul(R[i]).getImmutable();
        }
        C1 = Fi;
        C2 = EK.powZn(Mi).getImmutable();
        C3 = Ni;
        E = new Element[2*n];
        // 这里从g1改成了g
        for(int i = 0; i < 2*n; i++){
            E[i] = g.powZn(Mi.mul(X[i])).getImmutable();
        }
    }


    // 参与计算的随机数
    public static Element m, s;
    // 陷门
    public static Element T1, T2, T3, T4, T5;
    public static Element[] K, P;
    public static void genKey(String word){
        Element[] W = StrUtil.mapping(Zr, word, n);
        // 构造 2n 长向量 Y
        Element[] Y = new Element[2*n];
        for(int i = 0; i < n; i++){
            if(i < word.length() && word.charAt(i) != '*'){
                Y[2*i] = Zr.newOneElement().getImmutable();
                Y[2*i+1] = W[i];
            } else {
                Y[2*i] = Zr.newZeroElement().getImmutable();
                Y[2*i+1] = Zr.newZeroElement().getImmutable();
            }
        }

//        System.out.print("陷门加密的中间态 Y: ( ");
//        for(Element e: Y){
//            System.out.print(e + " ");
//        }
//        System.out.println(")");

        T1 = g1.powZn(s).getImmutable();
        T2 = Ei.powZn(s).getImmutable();
//        System.out.println("T2: " + T2);
//        System.out.println(g.powZn(s.mul(b2).mul(xti.negate().div(xt.sub(xti)))));
        T3 = Di.powZn(s).getImmutable();
//        System.out.println("T3: " + T3);
//        System.out.println(g.powZn(s.mul(b2).mul(f(xti).mul(xt.negate().div(xti.sub(xt))))));


        K = new Element[2*n];
        P = new Element[2*n];
        Element s1 = Zr.newZeroElement(), s2 = Zr.newZeroElement(); // 用于计算和
        for(int i = 0; i < 2*n; i++){
            s1.add(S[i].mul(Y[i]));
            s2.add(V[i].mul(Y[i]));
            K[i] = g.powZn(m.mul(Y[i])).getImmutable();
            P[i] = H[i].powZn(s).getImmutable();
        }
//        System.out.println("s1: " + s1 + "\ns2: " + s2);
        T4 = g.powZn(s.mul(m.mul(s1))).getImmutable();
        // 这里之前写错了sub，已改
        T5 = g.powZn(s.mul(m.mul(s2))).getImmutable();
    }

    public static boolean pairing(){
        Element acc = GT.newOneElement();
        for(int i = 0; i < 2*n; i++){
//            System.out.println("E[" + i + "] = " + E[i]);
//            System.out.println("P[" + i + "] = " + P[i]);
            acc.mul(bp.pairing(E[i].mul(P[i]), K[i]));
//            System.out.println("acc step " + i + ": " + acc);
        }
        Element d = bp.pairing(g, T4).mul(bp.pairing(h, T5)).getImmutable();


        Element part1 = bp.pairing(C1, T1).getImmutable();
        Element part2 = acc.div(d).getImmutable();
        Element part3 = bp.pairing(C2, T2).getImmutable();
        Element part4 = bp.pairing(C3, T3).getImmutable();

//        System.out.println("part1: " + part1);
//        System.out.println("part3*4: " + part3.mul(part4));

//        System.out.println("part2: " + part2);

//        System.out.println("part1: " + part1);
//        System.out.println("part2: " + part2);
//        System.out.println("part3: " + part3);
//        System.out.println("part4: " + part4);
//
//        System.out.println("C1 pairing check: " + bp.pairing(C1, T1));  // part1
//        System.out.println("C2 pairing check: " + bp.pairing(C2, T2));  // part3
//        System.out.println("acc pairing check: " + acc);  // part2
//        System.out.println("T4 pairing check: " + bp.pairing(g, T4));   // T4
//        System.out.println("T5 pairing check: " + bp.pairing(h, T5));   // T5

        Element left = part1.mul(part2).getImmutable();
        Element right = part3.mul(part4).getImmutable();

        System.out.println("left: " + left + "\nright: " + right + "\n\n");

        return left.isEqual(right);
    }

}

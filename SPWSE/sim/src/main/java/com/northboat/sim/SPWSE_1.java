package com.northboat.sim;

import com.northboat.util.StrUtil;
import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import java.security.SecureRandom;


public class SPWSE_1 {

    private static Pairing bp;

    // 加密单词长度，为 2n
    private static int n;
    public static void setN(int n){
        SPWSE_1.n = n;
    }

    // 主公钥
    // 循环群 G1、G2、GT，整数群 Zr
    public static Field G1, G2, GT, Zr;
    public static Element h, g1, g2;
    public static Element[] H;


    // 主私钥
    private static Element v;
    private static Element[] S, T;


    // 初始化主公钥、主私钥
    public static void setup(Pairing bp, int n, Field G1, Field G2, Field GT, Field Zr, Element g1, Element g2, // 公钥
                             Element v, Element[] S, Element[] T, // 私钥
                             Element r, Element[] R, Element m){ // 一些参与计算的随机数
        SPWSE_1.bp = bp;
        SPWSE_1.n = n;
        // 初始化生成元
        SPWSE_1.G1 = G1;
        SPWSE_1.G2 = G2;
        SPWSE_1.GT = GT;
        SPWSE_1.Zr = Zr;
        SPWSE_1.g1 = g1;
        SPWSE_1.g2 = g2;

        SPWSE_1.v = v;
        SPWSE_1.h = SPWSE_1.g1.powZn(SPWSE_1.v).getImmutable();

        SPWSE_1.S = S;
        SPWSE_1.T = T;

        H = new Element[2*n];
        for(int i = 0; i < 2*n; i++){
            H[i] = g1.powZn(S[i]).mul(h.powZn(T[i])).getImmutable();
        }

        SPWSE_1.r = r;
        SPWSE_1.R = R;
        SPWSE_1.m = m;
    }




    // 用于加密的随机数和随机数组
    public static Element r;
    public static Element[] R;


    // 加密后的密文
    public static Element C1, C2;
    public static Element[] E;
    public static void encode(String word){

        System.out.println("加密关键词 " + word + "\n=====================");

        // 将字符串各个字符映射到整数群 W，长度为 n
        Element[] W = StrUtil.mapping(Zr, word, n);

        System.out.print("关键词的 ASCII 码映射 W: ( ");
        for(Element e: W){
            System.out.print(e + " ");
        }
        System.out.println(")");



        Element[] X = new Element[2*n];
        // 通过 W 构造向量 X
        for(int i = 0; i < n; i++){
            // 实际上文档里给的是 x[2i-1] = r*ri*wi，但我从0开始存，所以偶数用这个
            // 即用 x[0] 表示 x1
            X[2*i] = r.mul(R[i]).mul(W[i]).getImmutable();
            // 而奇数用 -r*ri
            X[2*i+1] = r.negate().mul(R[i]).getImmutable();
        }

        System.out.print("关键词的加密的中间态 X: ( ");
        for(Element e: X){
            System.out.print(e + " ");
        }
        System.out.println(")");



        // 计算关键词 word 的密文
        C1 = g1.powZn(r).getImmutable();
        C2 = h.powZn(r).getImmutable();
        // 将向量 X 扩展为二维的密文 E
        E = new Element[2*n];
        for(int i = 0; i < 2*n; i++){
            E[i] = g1.powZn(X[i]).mul(H[i].powZn(r)).getImmutable();
        }

        System.out.println("关键词密文 C1: " + C1 + "\n关键词密文 C2: " + C2);
        System.out.println("关键词密文 E: ");
        for(Element e: E){
            System.out.println(e);
        }
        System.out.println("=====================\n");

    }


    // 参与计算的随机数
    public static Element m;
    // 计算要查找的关键词的陷门（加密信息）
    public static Element T1, T2;
    public static Element[] K;
    public static void genKey(String word){
        System.out.println("计算陷门 " + word + "\n=====================");

        // 将关键词字符串映射为整数群向量，这里的处理和上面一样，用 0 填充多余的位置
        Element[] W = StrUtil.mapping(Zr, word, n);

        System.out.print("陷门的 ASCII 码映射 W: ( ");
        for(Element e: W){
            System.out.print(e + " ");
        }
        System.out.println(")");

        // 通过 W 构造关键词对应的向量 Y
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

        System.out.print("陷门加密的中间态 Y: ( ");
        for(Element e: Y){
            System.out.print(e + " ");
        }
        System.out.println(")");


        // 将 Y 扩展为二维矩阵 K
        Element s1 = Zr.newZeroElement(), s2 = Zr.newZeroElement();
        K = new Element[2*n];
        for(int i = 0; i < 2*n; i++){
            s1.add(S[i].mul(Y[i]));
            s2.add(T[i].mul(Y[i]));
//            System.out.println(S[i].mul(Y[i]));
//            System.out.println("s1: " + s1);
//            System.out.println("s2: " + s2 + "\n");
            K[i] = g2.powZn(m.mul(Y[i])).getImmutable();
        }
        T1 = g2.powZn(m.mul(s1)).getImmutable();
        T2 = g2.powZn(m.mul(s2)).getImmutable();


        System.out.println("陷门 T1: " + T1 + "\n陷门 T2: " + T2 + "\n陷门 K:");
        for(Element e: K){
            System.out.println(e);
        }
        System.out.println("=====================\n");
    }


    public static boolean pairing(){
        System.out.println("开始匹配\n=====================");
        Element acc = GT.newOneElement();
        for(int i = 0; i < 2*n; i++){
            acc.mul(bp.pairing(E[i], K[i]));
        }
//        Element m = acc.getImmutable();
        Element d = bp.pairing(C1, T1).mul(bp.pairing(C2, T2)).getImmutable();

        Element ans = acc.div(d).getImmutable();
        System.out.println("等式左侧: " + ans);
        System.out.println("等式右侧: " + GT.newOneElement());
        System.out.println("=====================\n\n");
        return ans.isEqual(GT.newOneElement());
    }


    public static void main(String[] args) {
        genParams();
    }

    public static void genParams(){
        // 1. 创建固定种子的 SecureRandom 实例
        SecureRandom fixedRandom = new SecureRandom();
        fixedRandom.setSeed(12345L);  // 固定随机种子
        // 初始化 type a 类型曲线
        PairingParametersGenerator pg = new TypeACurveGenerator(160, 512);
        // 生成参数
        PairingParameters params = pg.generate();
        // 打印参数
        System.out.println(params.toString());
    }

    // 简单的双线性验证
    public static void testPairing(){

        Pairing bp = PairingFactory.getPairing("a8.properties");

        // 二、选择群上的元素
        Field G1 = bp.getG1();
        Field G2 = bp.getG2();
        Field Zr = bp.getZr();
        Element u = G1.newRandomElement().getImmutable();
        Element v = G2.newRandomElement().getImmutable();
        Element a = Zr.newRandomElement().getImmutable();
        Element b = Zr.newRandomElement().getImmutable();
        System.out.println("G1 上元素 u: " + u);
        System.out.println("Zr 上元素 a: " + a);


        // 三、计算等式左半部分
        Element ua = u.powZn(a);
        Element vb = v.powZn(b);
        Element left = bp.pairing(ua,vb);

        // 四、计算等式右半部分
        Element euv = bp.pairing(u,v).getImmutable();
        Element ab = a.mul(b);
        Element right = euv.powZn(ab);

        if (left.isEqual(right)) {
            System.out.println("Yes");
        } else {
            System.out.println("No");
        }

    }
}
package cia.northboat;

import cia.northboat.sim.PAUKS;
import cia.northboat.sim.SA_PAUKS;
import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class Main {
    private static final Pairing bp = PairingFactory.getPairing("a.properties");
    // 加密单词长度，为 2n
    private static final int n = 4;
    // 主公钥
    public static Field G1, G2, GT, Zr;
    public static Element g1, g2;


    public static Element x1, x2, x3, x4, y;

    // 初始化生成元
    static{
        G1 = bp.getG1();
        G2 = bp.getG2();
        GT = bp.getGT();
        Zr = bp.getZr();
        g1 = G1.newRandomElement().getImmutable();
        g2 = G2.newRandomElement().getImmutable();

        x1 = Zr.newRandomElement().getImmutable();
        x2 = Zr.newRandomElement().getImmutable();
        x3 = Zr.newRandomElement().getImmutable();
        x4 = Zr.newRandomElement().getImmutable();
        y = Zr.newRandomElement().getImmutable();
    }
    public static void main(String[] args) {
        String str = "word";
//        testPAUKS(str);
        testSAPAUKS(str);
    }

    public static void testPAUKS(String str){
        PAUKS.init(bp, G1, GT, Zr, g1, n, x1, x2, x3, x4, y);
        PAUKS.enc(str);
        PAUKS.trap(str);
        System.out.println(PAUKS.test());

        PAUKS.update();
        PAUKS.updEnc();
        PAUKS.constTrap(str);
        System.out.println(PAUKS.updTest());
    }

    public static void testSAPAUKS(String str){
        SA_PAUKS.init(bp, G1, GT, Zr, g1, n, x1, x2, x3, x4, y);
        SA_PAUKS.enc(str);
        SA_PAUKS.trap(str);
        System.out.println(SA_PAUKS.test());
    }


    public static void testTime(String w, String t, int m){

        long startTime1 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            // 操作1
        }
        long endTime1 = System.currentTimeMillis();

        long startTime2 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            // 操作2
        }
        long endTime2 = System.currentTimeMillis();


        boolean flag = false;
        long startTime3 = System.currentTimeMillis();
        for(int i = 0; i < m; i++){
            // 操作3
        }
        long endTime3 = System.currentTimeMillis();


        System.out.println("算法Ⅰ对 " + w + " 和 " + t + " 的测试\n==========================");
        System.out.println("验证结果: " + flag);
        System.out.println("加密 " + w + " 时长: " + (double)(endTime1 - startTime1)/m + "ms");

        System.out.println("计算 " + t + " 陷门时长: " + (double)(endTime2 - startTime2)/m + "ms");
        System.out.println("匹配时长: " + (double)(endTime3 - startTime3)/m + "ms\n==========================\n\n");
    }
}
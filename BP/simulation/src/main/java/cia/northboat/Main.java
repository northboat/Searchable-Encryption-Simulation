package cia.northboat;

import cia.northboat.sim.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Pairing bp = PairingFactory.getPairing("a.properties");
    // 加密单词长度，为 2n
    private static final int n = 7;
    // 主公钥
    public static Field G1, G2, GT, Zr;
    public static Element g1, g2, h;


    public static Element x1, x2, x3, x4, y;

    // DuMSE
    public static Element id, sk_id, AI_o, A, B, C;

    // Tu2CKS
    public static Element a, alpha, beta, lambda, k1, k2, id_p, id_kgc, id_u1, id_u2;

    // 初始化生成元
    static{
        G1 = bp.getG1();
        G2 = bp.getG2();
        GT = bp.getGT();
        Zr = bp.getZr();
        g1 = G1.newRandomElement().getImmutable();
        g2 = G2.newRandomElement().getImmutable();
        h = G1.newRandomElement().getImmutable();

        x1 = Zr.newRandomElement().getImmutable();
        x2 = Zr.newRandomElement().getImmutable();
        x3 = Zr.newRandomElement().getImmutable();
        x4 = Zr.newRandomElement().getImmutable();
        y = Zr.newRandomElement().getImmutable();

        // DuMSE
        AI_o = G1.newRandomElement().getImmutable();
        A = G1.newRandomElement().getImmutable();
        B = G1.newRandomElement().getImmutable();
        C = G1.newRandomElement().getImmutable();

        // Tu2CKS
        a = Zr.newRandomElement().getImmutable();
        alpha = Zr.newRandomElement().getImmutable();
        beta = Zr.newRandomElement().getImmutable();
        lambda = Zr.newRandomElement().getImmutable();
        k1 = Zr.newRandomElement().getImmutable();
        k2 = Zr.newRandomElement().getImmutable();
        id_p = Zr.newRandomElement().getImmutable();
        id_kgc = Zr.newRandomElement().getImmutable();
        id_u1 = Zr.newRandomElement().getImmutable();
        id_u2 = Zr.newRandomElement().getImmutable();
    }

    public static void main(String[] args) {
        String str = "word";
//        testPAUKS(str); // √
//        testSAPAUKS(str); // √
//        testdIBAEKS(str); // √
//        testDuMSE(str); // ?
//        testpMatch(str); // √
//        testCRIMA(str); // √
        Tu2CKS.setup(bp, G1, GT, Zr, g1, n, x1, y, x2, x3, x4, a, alpha, beta, lambda, k1, k2, id_p, id_kgc);
        Tu2CKS.keyGen(id_u1, id_u2);
        List<String> W = Arrays.asList("word", "chinese", "english");
        Tu2CKS.enc(W);
        List<String> Q = Arrays.asList("word", "chinese");
        Tu2CKS.usr1Trap(Q);
        Tu2CKS.usr2Trap("english");
        Tu2CKS.trap();
        System.out.println(Tu2CKS.match());
    }

    public static void testCRIMA(String str){
        CR_IMA.init(bp, G1, GT, Zr, n, g1, x1, x2, x3, x4, y);
        CR_IMA.enc(str);
        CR_IMA.trap(str);
        System.out.println(CR_IMA.match());
    }

    public static void testpMatch(String str){
        pMatch.init(bp, G1, GT, Zr, n, g1, x1, x2, x3, x4, y);
        pMatch.enc(str);
        pMatch.trap(str);
        System.out.println(pMatch.match());
    }

    public static void testDuMSE(String str){
        DuMSE.init(bp, G1, G2, GT, Zr, n, g1, g2, x1, x2, x3, x4, y, AI_o);
        DuMSE.enc(str);
        DuMSE.trap(str);
        System.out.println(DuMSE.search());
    }

    public static void testdIBAEKS(String str){
        dIBAEKS.init(bp, G1, GT, Zr, n, g1, h, y);
        dIBAEKS.enc(str);
        dIBAEKS.trap(str);
        System.out.println(dIBAEKS.test());
    }

    public static void testSAPAUKS(String str){
        SA_PAUKS.init(bp, G1, GT, Zr, g1, n, x1, x2, x3, x4, y);
        SA_PAUKS.enc(str);
        SA_PAUKS.trap(str);
        System.out.println(SA_PAUKS.test());
    }

    public static void testPAUKS(String str){
        PAUKS.init(bp, G1, GT, Zr, g1, n, x1, x2, x3, x4, y);
        PAUKS.enc(str);
        PAUKS.trap("str");
        System.out.println(PAUKS.test());

        PAUKS.update();
        PAUKS.updEnc();
        PAUKS.constTrap(str);
        System.out.println(PAUKS.updTest());
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
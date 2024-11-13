package cia.northboat;

import cia.northboat.sim.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Pairing bp = PairingFactory.getPairing("a.properties");
    // 加密单词长度，为 2n
    private static final int n = 8;
    // 主公钥
    public static Field G1, G2, GT, Zr;
    public static Element g1, g2, h;


    public static Element x1, x2, x3, x4, y;

    // DuMSE
    public static Element AI_o;

    // Tu2CKS
    public static Element a, alpha, beta, lambda, id_p, id_kgc;
    public static Element[] id;

    // 初始化生成元
    public static void init(){
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

        // Tu2CKS / TuCR
        a = Zr.newRandomElement().getImmutable();
        alpha = Zr.newRandomElement().getImmutable();
        beta = Zr.newRandomElement().getImmutable();
        lambda = Zr.newRandomElement().getImmutable();
        id_p = Zr.newElement(1234).getImmutable();
        id_kgc = Zr.newElement(789).getImmutable();

        id = new Element[3];
        id[0] = Zr.newElement(123).getImmutable();
        id[1] = Zr.newElement(456).getImmutable();
        id[2] = Zr.newElement(789).getImmutable();


        time = new long[7][3];
    }

    static long[][] time;
    public static void printTime(int n){
        System.out.println("\n====== 耗时 ======");
        for(long[] t: time){
            for(long i: t){
                System.out.print(i/n + "\t\t");
            }
            System.out.println();
        }
    }



    public static void main(String[] args) {
        init();

        List<String> W = new ArrayList<>();
        W.add("word");
//        W.add("chinese");
//        W.add("english"); W.add("english");
//        W.add("security"); W.add("school");
//        W.add("cyber"); W.add("engine");

        for(String w: W){
//            testCRIMA(w); // √
//            testTuCR(w); // √
            testTu2CKS(); // √
//            testPAUKS(w); // √
//            testSAPAUKS(w); // √
//            testDIBAEKS(w); // √
//            testDuMSE(w); // ?
//            testPMatch(w); // √
        }

        printTime(W.size());
    }


    public static void testTu2CKS(){
        Tu2CKS.setup(bp, G1, GT, Zr, g1, n, x1, x2, x3, x4, y, a, alpha, beta, lambda, id_p, id_kgc);
        Tu2CKS.keyGen(id);
        List<String> W = Arrays.asList("word", "chinese", "english");

        long s1 = System.currentTimeMillis();
        Tu2CKS.enc(W);
        long e1 = System.currentTimeMillis();

        long s2 = System.currentTimeMillis();
        Tu2CKS.usrTrap(); Tu2CKS.trap();
        long e2 = System.currentTimeMillis();

        long s3 = System.currentTimeMillis();
        System.out.println(Tu2CKS.match());
        long e3 = System.currentTimeMillis();

        time[6][0] += e1-s1;
        time[6][1] += e2-s2;
        time[6][2] += e3-s3;
    }


    public static void testTuCR(String str){
        while(true){
            try{
                testTuCRWithException(str);
            } catch (Exception e){
                init();
                continue;
            }
            return;
        }
    }

    public static void testTuCRWithException(String str) throws Exception{
        TuCR.setup(bp, G1, GT, Zr, n, g1, x1, x2, x3, x4, y);
        TuCR.keyGen(id);

        long s1 = System.currentTimeMillis();
        TuCR.enc(str);
        long e1 = System.currentTimeMillis();

        long s2 = System.currentTimeMillis();
        TuCR.localTrap(str);
        long e2 = System.currentTimeMillis();

        long s3 = System.currentTimeMillis();
        System.out.println(TuCR.localMatch());
        long e3 = System.currentTimeMillis();


        TuCR.usrAuth();
        TuCR.usrAuthorize();
        System.out.println(TuCR.usrIDMatch());

        TuCR.usrAuthorizationFactor();
        TuCR.retrievalAuthGen();

        TuCR.federatedTrap(str);
        System.out.println(TuCR.federatedMatch());


        time[5][0] += e1-s1;
        time[5][1] += e2-s2;
        time[5][2] += e3-s3;
    }


    public static void testCRIMA(String str){
        CR_IMA.init(bp, G1, GT, Zr, n, g1, x1, x2, x3, x4, y);

        long s1 = System.currentTimeMillis();
        CR_IMA.enc(str);
        long e1 = System.currentTimeMillis();

        long s2 = System.currentTimeMillis();
        CR_IMA.trap(str);
        long e2 = System.currentTimeMillis();

        long s3 = System.currentTimeMillis();
        CR_IMA.match();
        long e3 = System.currentTimeMillis();

        time[4][0] += e1-s1;
        time[4][1] += e2-s2;
        time[4][2] += e3-s3;
    }


    public static void testPMatch(String str){
        pMatch.init(bp, G1, GT, Zr, n, g1, x1, x2, x3, x4, y);

        long s1 = System.currentTimeMillis();
        pMatch.enc(str);
        long e1 = System.currentTimeMillis();

        long s2 = System.currentTimeMillis();
        pMatch.trap(str);
        long e2 = System.currentTimeMillis();

        long s3 = System.currentTimeMillis();
        System.out.println(pMatch.match());
        long e3 = System.currentTimeMillis();

        time[3][0] += e1-s1;
        time[3][1] += e2-s2;
        time[3][2] += e3-s3;

    }


    public static void testDuMSE(String str){
        DuMSE.init(bp, G1, G2, GT, Zr, n, g1, g2, x1, x2, x3, x4, y, AI_o);

        long s1 = System.currentTimeMillis();
        DuMSE.enc(str);
        long e1 = System.currentTimeMillis();


        long s2 = System.currentTimeMillis();
        DuMSE.trap(str);
        long e2 = System.currentTimeMillis();


        long s3 = System.currentTimeMillis();
        System.out.println(DuMSE.search());
        long e3 = System.currentTimeMillis();

//        time[3][0] += e1-s1;
//        time[3][1] += e2-s2;
//        time[3][2] += e3-s3;
    }


    public static void testDIBAEKS(String str){
        dIBAEKS.init(bp, G1, GT, Zr, n, g1, h, y);
        long s1 = System.currentTimeMillis();
        dIBAEKS.enc(str);
        long e1 = System.currentTimeMillis();


        long s2 = System.currentTimeMillis();
        dIBAEKS.trap(str);
        long e2 = System.currentTimeMillis();


        long s3 = System.currentTimeMillis();
        System.out.println(dIBAEKS.test());
        long e3 = System.currentTimeMillis();

        time[2][0] += e1-s1;
        time[2][1] += e2-s2;
        time[2][2] += e3-s3;
    }


    public static void testSAPAUKS(String str){
        SA_PAUKS.init(bp, G1, GT, Zr, g1, n, x1, x2, x3, x4, y);

        long s1 = System.currentTimeMillis();
        SA_PAUKS.enc(str);
        long e1 = System.currentTimeMillis();



        long s2 = System.currentTimeMillis();
        SA_PAUKS.trap(str);
        long e2 = System.currentTimeMillis();


        long s3 = System.currentTimeMillis();
        System.out.println(SA_PAUKS.test());
        long e3 = System.currentTimeMillis();


        time[1][0] += e1-s1;
        time[1][1] += e2-s2;
        time[1][2] += e3-s3;
    }



    public static void testPAUKS(String str){
        PAUKS.init(bp, G1, GT, Zr, g1, n, x1, x2, x3, x4, y);

        long s1 = System.currentTimeMillis();
        PAUKS.enc(str);
        long e1 = System.currentTimeMillis();
        long t1 = e1-s1;

        long s2 = System.currentTimeMillis();
        PAUKS.trap(str);
        long e2 = System.currentTimeMillis();
        long t2 = e2-s2;

        long s3 = System.currentTimeMillis();
        System.out.println(PAUKS.test());
        long e3 = System.currentTimeMillis();
        long t3 = e3-s3;


        PAUKS.update();
        long s4 = System.currentTimeMillis();
        PAUKS.updEnc();
        long e4 = System.currentTimeMillis();
        long t4 = e4-s4;


        long s5 = System.currentTimeMillis();
        PAUKS.constTrap(str);
        long e5 = System.currentTimeMillis();
        long t5 = e5-s5;

        long s6 = System.currentTimeMillis();
        System.out.println(PAUKS.updTest());
        long e6 = System.currentTimeMillis();
        long t6 = e6-s6;

        time[0][0] += t1+t4;
        time[0][1] += t2+t5;
        time[0][2] += t3+t6;
    }

}
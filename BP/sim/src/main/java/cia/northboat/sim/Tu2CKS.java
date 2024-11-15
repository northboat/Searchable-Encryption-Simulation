package cia.northboat.sim;

import cia.northboat.util.AESUtil;
import cia.northboat.util.BitUtil;
import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;


import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Tu2CKS {
    // 哈希长度、用户个数、参与计算个数
    private static int n, k;
    private static Pairing bp;
    private static Field G, GT, Zr;
    private static Element g, g1, g2, Y, h;
    private static Element x1, x2, t, a, alpha, beta, lambda, id_p, r2;
    private static Element sk_c, pk_c, sk_kgc, pk_kgc;
    private static Element[] sk_p, pk_p;
    private static Element[] fi;

    private static Element ZERO, ONE, TWO, THREE;

    private static Element H(String str){
        return HashUtil.hashZrArr2Zr(Zr, g, HashUtil.hashStr2ZrArr(Zr, str, n)).getImmutable();
    }

    private static Element H1(Element e){
        return HashUtil.hashZr2G(g, e).getImmutable();
    }

    private static Element H2(Element e){
        return HashUtil.hashG2Zr(Zr, e).getImmutable();
    }

    private static Element f(Element x){
        Element res = t.duplicate();
        // fi 一共 k-1 长，最后一轮 x 的指数将是 k-2+1 = k-1 次，正确的
        for(int i = 0; i < k-1; i++){
            Element e = x.duplicate();
            e.pow(new BigInteger(String.valueOf(i+1)));
            res.add(fi[i].mul(e));
        }
        return res;
    }

    public static void setup(Pairing bp, Field G, Field GT, Field Zr, Element g, int n,
                            Element x, Element x1, Element x2, Element t, Element y,
                            Element a, Element alpha, Element beta, Element lambda,
                            Element id_p, Element id_kgc) {
        System.out.println("============ Setup ============");

        Tu2CKS.bp = bp;
        Tu2CKS.G = G;
        Tu2CKS.GT = GT;
        Tu2CKS.Zr = Zr;
        Tu2CKS.g = g;
        Tu2CKS.n = n;
        Tu2CKS.x1 = x1;
        Tu2CKS.x2 = x2;
        Tu2CKS.t = t;
        Tu2CKS.a = a;
        Tu2CKS.alpha = alpha;
        Tu2CKS.beta = beta;
        Tu2CKS.lambda = lambda;
        Tu2CKS.id_p = id_p;

        Tu2CKS.g1 = g.powZn(x1).getImmutable();
        Tu2CKS.g2 = g.powZn(x2).getImmutable();
        Tu2CKS.Y = bp.pairing(g, g).powZn(beta).getImmutable();

        Tu2CKS.r2 = H2(g.powZn(x1.mul(x2)));

        sk_c = x;
        pk_c = g.powZn(x).getImmutable();

        sk_p = new Element[2];
        sk_p[0] = y.getImmutable();
        sk_p[1] = H1(id_p).powZn(alpha).getImmutable();

        System.out.println("y: " + y);
        System.out.println("sk_p[0]: " + sk_p[0]);


        pk_p = new Element[2];
        pk_p[0] = g.powZn(y).getImmutable();
        pk_p[1] = H1(id_p).getImmutable();

        System.out.println("g^y: " + g.powZn(y));
        System.out.println("pk_p[0]: " + pk_p[0]);

        sk_kgc = H1(id_kgc).powZn(alpha).getImmutable();
        pk_kgc = H1(id_kgc).getImmutable();

        k = 3;
        fi = new Element[k-1];
        fi[0] = Zr.newRandomElement().getImmutable();
        fi[1] = Zr.newRandomElement().getImmutable();

        ZERO = Zr.newZeroElement().getImmutable();
        ONE = Zr.newOneElement().getImmutable();
        TWO = Zr.newElement(2).getImmutable();
        THREE = Zr.newElement(3).getImmutable();


        // AES 密钥
        try {
            k1 = AESUtil.getRandomKey();
            k2 = AESUtil.getRandomKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private static Element[] sk_u1, sk_u2, sk_u3;
    public static Element[] pk_u1, pk_u2, pk_u3;


    private static byte[] k1, k2;
    public static Element aes(Element id){
        try{
            byte[] theta = Zr.newRandomElement().toBytes();
            byte[] zeta = AESUtil.enc(id.toBytes(), k1);
            return Zr.newElementFromBytes(AESUtil.enc(BitUtil.connect(zeta, theta), k2)).getImmutable();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Element delta(Element[] t, int i){
        Element delta = Zr.newOneElement();
        for(int j = 0; j < t.length; j++){
            if(j != i){
                Element e = t[j].negate().div(t[i].sub(t[j]));
                delta.mul(e);
            }
        }
        return delta.getImmutable();
    }

    public static void keyGen(Element[] sk, Element[] pk, Element[] id, Element[] delta,
                              Element[] r, Element[] mu, Element[] t, int i){
        sk[0] = g.powZn(beta.sub(a.mul(r[i])).div(lambda.add(delta[i]))).getImmutable();
        sk[1] = delta[i].getImmutable();
        sk[2] = g.powZn(r[i]).getImmutable();
        sk[3] = H1(id[i]).powZn(alpha).getImmutable();

        sk[4] = mu[i].getImmutable();

        sk[5] = r2.mul(f(t[i]).mul(delta(t, i))).getImmutable();

        pk[0] = H1(id[i]).getImmutable();
        pk[1] = g.powZn(mu[i]).getImmutable();
    }

    public static void keyGen(Element[] id){
        System.out.println("====== Key Gen ======");
        Element[] delta = new Element[k];
        Element[] r = new Element[k];
        Element[] mu = new Element[k];
        Element[] t = new Element[k];
        for(int i = 0; i < k; i++){
            t[i] = Zr.newRandomElement().getImmutable();
            mu[i] = Zr.newRandomElement().getImmutable();
            r[i] = Zr.newRandomElement().getImmutable();
            delta[i] = aes(id[i]);
        }
        sk_u1 = new Element[6]; sk_u2 = new Element[6]; sk_u3 = new Element[6];
        pk_u1 = new Element[2]; pk_u2 = new Element[2]; pk_u3 = new Element[6];

        keyGen(sk_u1, pk_u1, id, delta, r, mu, t, 0);
        keyGen(sk_u2, pk_u2, id, delta, r, mu, t, 1);
        keyGen(sk_u3, pk_u3, id, delta, r, mu, t, 2);

//        for(Element e: sk_u3){
//            System.out.println(e);
//        }
    }


    public static Element C1, C2, r1;
    public static Element[] B;
    public static void enc(List<String> W){

        r1 = Zr.newRandomElement().getImmutable();
        C1 = g2.powZn(t.mul(r2)).getImmutable();
        C2 = bp.pairing(g, g).powZn(r1.negate()).getImmutable();


        B = new Element[4];
        B[0] = g.powZn(r1.mul(Zr.newElement(-5))).getImmutable();
        B[1] = g.powZn(r1.mul(Zr.newElement(11))).getImmutable();
        B[2] = g.powZn(r1.mul(Zr.newElement(-6))).getImmutable();
        B[3] = g.powZn(r1.mul(Zr.newElement(1))).getImmutable();
    }

    public static Element[] T1_u1, T1_u2, T1_u3;
    public static Element T2_u1, T3_u1, T2_u2, T3_u2, T2_u3, T3_u3;

    public static Element[] usrTrap(Element HW, Element[] T1, Element[] sk){
        Element p = pk_p[0].powZn(sk[4]).getImmutable();

        T1[0] = g.powZn(HW.powZn(ZERO)).mul(p).getImmutable();
        T1[1] = g.powZn(HW.powZn(ONE)).mul(p).getImmutable();
        T1[2] = g.powZn(HW.powZn(TWO)).mul(p).getImmutable();
        T1[3] = g.powZn(HW.powZn(THREE)).mul(p).getImmutable();

        Element[] R = new Element[2];
        // 返回 T2
        R[0] = bp.pairing(g1, g2).powZn(sk[5]).mul(bp.pairing(sk[3], pk_p[1])).getImmutable();
        // 返回 T3
        R[1] = bp.pairing(sk[3], pk_kgc).getImmutable();
        return R;
    }

    public static void usrTrap(Element[] HW){
        System.out.println("====== Usr Trap ======");
        System.out.println();


        Element p = pk_p[0].powZn(sk_u1[4]).getImmutable();

        Element HW1 = HW[0];
        Element HW2 = HW[1];

        Element p21 = HW2.powZn(ZERO);
        Element p22 = HW2.powZn(ONE);
        Element p23 = HW2.powZn(TWO);
        Element p24 = HW2.powZn(THREE);


        T1_u1 = new Element[4];
        T1_u1[0] = g.powZn(HW1.powZn(ZERO).add(p21)).mul(p).getImmutable();
        T1_u1[1] = g.powZn(HW1.powZn(ONE).add(p22)).mul(p).getImmutable();
        T1_u1[2] = g.powZn(HW1.powZn(TWO).add(p23)).mul(p).getImmutable();
        T1_u1[3] = g.powZn(HW1.powZn(THREE).add(p24)).mul(p).getImmutable();


        T2_u1 = bp.pairing(g1, g2).powZn(sk_u1[5]).mul(bp.pairing(sk_u1[3], pk_p[1])).getImmutable();
        T3_u1 = bp.pairing(sk_u1[3], pk_kgc).getImmutable();
    }


    public static void usrTrap(){
        Element[] R;

//        // USR1
//        Element[] HW = new Element[2];
//        HW[0] = ONE; HW[1] = TWO;
//        usrTrap(HW);

        // USR1
        Element HW1 = ONE;
        T1_u1 = new Element[4];
        R = usrTrap(HW1, T1_u1, sk_u1);
        T2_u1 = R[0];
        T3_u1 = R[1];

        // USR2
        Element HW2 = TWO;
        T1_u2 = new Element[4];
        R = usrTrap(HW2, T1_u2, sk_u2);
        T2_u2 = R[0];
        T3_u2 = R[1];

        Element HW3 = THREE;
        T1_u3 = new Element[4];
        R = usrTrap(HW3, T1_u3, sk_u3);
        T2_u3 = R[0];
        T3_u3 = R[1];
    }


    public static Element T1, T3;
    public static Element[] T_Q;
    public static void trap(){
        System.out.println("====== Valid Trap ======");
        Element s = Zr.newRandomElement().getImmutable();
        T1 = g1.powZn(s).getImmutable();


        Element p1 = pk_u1[1].powZn(sk_p[0]).getImmutable();
        Element p2 = pk_u2[1].powZn(sk_p[0]).getImmutable();

        Element p3 = pk_u3[1].powZn(sk_p[0]).getImmutable();
//        Element p3 = G.newOneElement().getImmutable();

        Element p4 = pk_c.powZn(sk_p[0]).getImmutable();


        T_Q = new Element[4];
        for(int i = 0; i < 4; i++){
            Element e1 = T1_u1[i].div(p1);
            Element e2 = T1_u2[i].div(p2);
            Element e3 = T1_u3[i].div(p3);
//            Element e3 = G.newOneElement();
            T_Q[i] = e1.mul(e2).mul(e3).mul(p4).getImmutable();
        }


        Element p5 = T2_u1.div(bp.pairing(pk_u1[0], sk_p[1])).getImmutable();
        Element p6 = T2_u2.div(bp.pairing(pk_u2[0], sk_p[1])).getImmutable();

        Element p7 = T2_u3.div(bp.pairing(pk_u3[0], sk_p[1])).getImmutable();
//        Element p7 = GT.newOneElement().getImmutable();
        T3 = (p5.mul(p6).mul(p7)).powZn(s).getImmutable();
    }


    public static boolean match(){
        System.out.println("====== Match ======");
        Element m = Zr.newElement(3).getImmutable();
        Element inv = m.invert().getImmutable();
        Element e = pk_p[0].powZn(sk_c).getImmutable();

        Element p1 = bp.pairing(C1, T1).getImmutable();
        Element p2 = bp.pairing(B[0], T_Q[0].div(e).powZn(inv)).getImmutable();
        Element p3 = bp.pairing(B[1], T_Q[1].div(e).powZn(inv)).getImmutable();
        Element p4 = bp.pairing(B[2], T_Q[2].div(e).powZn(inv)).getImmutable();
        Element p5 = bp.pairing(B[3], T_Q[3].div(e).powZn(inv)).getImmutable();


        Element p6 = C2.mul(p2).mul(p3).mul(p4).mul(p5).getImmutable();

        Element left = p1.mul(p6).getImmutable();
        Element right = T3;


        System.out.println(p6);
        System.out.println(left);
        System.out.println(right);

        return left.isEqual(right);
    }


    public static void test(String[] args) {
        System.out.println("nmsl");
    }

}

package cia.northboat.sim;

import cia.northboat.util.AESUtil;
import cia.northboat.util.BitUtil;
import cia.northboat.util.HashUtil;
import cia.northboat.util.MatrixUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class Tu2CKS {
    private static final int k = 2, n0 = 3, l = 3;

    private static int n;

    private static Pairing bp;

    private static Field G, GT, Zr;

    private static Element g, g1, g2, Y, h;

    private static Element x1, x2, t, a, alpha, beta, lambda, k1, k2, id_p, r2;

    private static Element sk_c, pk_c, sk_kgc, pk_kgc;
    private static Element[] sk_p, pk_p;
    private static Element[] fi;

    private static Element H(byte[] b){
        return Zr.newElementFromHash(b, 0, b.length).getImmutable();
    }

    private static Element H1(Element e){
        return HashUtil.hashZr2G(g, e);
    }

    private static Element H2(Element e){
        return HashUtil.hashG2Zr(Zr, e).getImmutable();
    }

    private static Element f(Element x){
        Element res = t.duplicate();
        // fi 一共 k-1 长，最后一轮 x 的指数将是 k-2+1 = k-1 次，正确的
        for(int i = 0; i < fi.length; i++){
            Element e = x.duplicate();
            e.pow(new BigInteger(String.valueOf(i+1)));
            res.add(fi[i].mul(e));
        }
        return res;
    }

    public static void setup(Pairing bp, Field G, Field GT, Field Zr, Element g, int n,
                            Element x, Element y, Element x1, Element x2, Element t,
                            Element a, Element alpha, Element beta, Element lambda,
                            Element k1, Element k2, Element id_p, Element id_kgc){
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
        Tu2CKS.k1 = k1;
        Tu2CKS.k2 = k2;
        Tu2CKS.id_p = id_p;

        Tu2CKS.g1 = g.powZn(x1).getImmutable();
        Tu2CKS.g2 = g.powZn(x2).getImmutable();
        Tu2CKS.Y = bp.pairing(g, g).powZn(beta).getImmutable();
        Tu2CKS.g = g.powZn(lambda).getImmutable();
        Tu2CKS.r2 = H2(g.powZn(x1.mul(x2)));

        sk_c = x;
        pk_c = g.powZn(x).getImmutable();

        sk_p = new Element[2];
        sk_p[0] = y;
        sk_p[1] = H1(id_p).powZn(alpha).getImmutable();

        pk_p = new Element[2];
        pk_p[0] = g.powZn(y).getImmutable();
        pk_p[1] = H1(id_p).getImmutable();

        sk_kgc = H1(id_kgc).powZn(alpha).getImmutable();
        pk_kgc = H1(id_kgc).getImmutable();

        fi = new Element[k-1];
        fi[0] = Zr.newRandomElement().getImmutable();
    }


    private static Element[] sk_u1, sk_u2;
    public static Element[] pk_u1, pk_u2;
    public static void keyGen(Element id_u1, Element id_u2){
        try{
            byte[] k1 = AESUtil.getRandomKey(), k2 = AESUtil.getRandomKey();
            byte[] theta1 = Zr.newRandomElement().toBytes(), theta2 = Zr.newRandomElement().toBytes();

            byte[] zeta1 = AESUtil.enc(id_u1.toBytes(), k1);
            byte[] zeta2 = AESUtil.enc(id_u2.toBytes(), k1);
            Element delta1 = Zr.newElementFromBytes(AESUtil.enc(BitUtil.connect(zeta1, theta1), k2));
            Element delta2 = Zr.newElementFromBytes(AESUtil.enc(BitUtil.connect(zeta2, theta2), k2));


            System.out.println(delta1);
            System.out.println(delta2);
            System.out.println();

            Element r_u1 = Zr.newRandomElement().getImmutable(), r_u2 = Zr.newRandomElement().getImmutable();
            Element mu_u1 = Zr.newRandomElement().getImmutable(), mu_u2 = Zr.newRandomElement().getImmutable();
            Element t_u1 = Zr.newRandomElement().getImmutable(), t_u2 = Zr.newRandomElement().getImmutable();

            sk_u1 = new Element[6];
            sk_u1[0] = g.powZn(beta.sub(a.mul(r_u1)).div(lambda.add(delta1))).getImmutable();
            sk_u1[1] = delta1;
            sk_u1[2] = g.powZn(r_u1).getImmutable();
            sk_u1[3] = H1(id_u1).powZn(alpha).getImmutable();
            sk_u1[4] = mu_u1;
            sk_u1[5] = r2.mul(f(t_u1).mul(t_u2.negate().div(t_u1.sub(t_u2)))).getImmutable();

            pk_u1 = new Element[2];
            pk_u1[0] = H1(id_u1);
            pk_u1[1] = g.powZn(mu_u1).getImmutable();

            sk_u2 = new Element[6];
            sk_u2[0] = g.powZn(beta.sub(a.mul(r_u2)).div(lambda.add(delta2))).getImmutable();
            sk_u2[1] = delta2;
            sk_u2[2] = g.powZn(r_u2).getImmutable();
            sk_u2[3] = H1(id_u2).powZn(alpha).getImmutable();
            sk_u2[4] = mu_u2;
            sk_u2[5] = r2.mul(f(t_u2).mul(t_u1.negate().div(t_u2.sub(t_u1)))).getImmutable();

            pk_u2 = new Element[2];
            pk_u2[0] = H1(id_u2);
            pk_u2[1] = g.powZn(mu_u2).getImmutable();

        } catch (Exception e){
            e.printStackTrace();
        }

    }


    public static Element C1, C2, r1;
    public static Element[] B;
    public static void enc(List<String> W){
        Element[] HW = new Element[W.size()];

        for(int i = 0; i < W.size(); i++){
            System.out.println(W.get(i));
            Element[] w = HashUtil.hashStr2ZrArr(Zr, W.get(i), n);
            HW[i] = HashUtil.hashZrArr2Zr(Zr, g, w).getImmutable();
            System.out.println(HW[i]);
        }
        System.out.println();

        r1 = Zr.newRandomElement().getImmutable();
        C1 = g2.powZn(t.mul(r2)).getImmutable();
        C2 = bp.pairing(g, g).powZn(r1.negate()).getImmutable();

        // 解方程
        BigInteger[][] matrix = new BigInteger[W.size()][3];
        for(int i = 0; i < W.size(); i++){
            for(int j = 0; j < 3; j++){
                matrix[i][j] = HW[i].powZn(Zr.newElement(3-j)).toBigInteger();
            }
        }
        for(BigInteger[] row: matrix){
            for(BigInteger i: row){
                System.out.print(i + "   ");
            }
            System.out.println();
        }
        System.out.println();

        // 求逆
        matrix = MatrixUtil.inverse(Zr, matrix);
        for(BigInteger[] row: matrix){
            for(BigInteger i: row){
                System.out.print(i + "   ");
            }
            System.out.println();
        }
        System.out.println();

        // 求解
        BigInteger[] vec = MatrixUtil.mul(matrix, MatrixUtil.getOneVector(3));
        Element[] eta = new Element[3];
        B = new Element[3];
        for(int i = 0; i < 3; i++){
            eta[i] = Zr.newElement(vec[i]);
            B[i] = g.powZn(r1.mul(eta[i])).getImmutable();
//            System.out.println(eta[i]);
            System.out.println(B[i]);
        }
    }

    public static Element[] T1_u1, T1_u2;
    public static Element T2_u1, T3_u1, T2_u2, T3_u2;

    public static void usr1Trap(List<String> W){
        Element p = pk_p[0].powZn(sk_u1[4]).getImmutable();
        Element HW1 = HashUtil.hashZrArr2Zr(Zr, g, HashUtil.hashStr2ZrArr(Zr, W.get(0), n));
        Element HW2 = HashUtil.hashZrArr2Zr(Zr, g, HashUtil.hashStr2ZrArr(Zr, W.get(1), n));
        T1_u1 = new Element[3];
        T1_u1[0] = g.powZn(HW1.add(HW2.powZn(Zr.newZeroElement()))).mul(p).getImmutable();
        T1_u1[1] = g.powZn(HW1.twice().add(HW2.twice())).mul(p).getImmutable();
        Element three = Zr.newElement(3).getImmutable();
        T1_u1[2] = g.powZn(HW1.powZn(three).add(HW2.powZn(three))).mul(p).getImmutable();
        T2_u1 = bp.pairing(g1, g2).powZn(sk_u1[5]).mul(bp.pairing(sk_u1[3], pk_p[1])).getImmutable();
        T3_u1 = bp.pairing(sk_u1[3], pk_kgc).getImmutable();
    }

    public static void usr2Trap(String w){
        Element p = pk_p[0].powZn(sk_u2[4]).getImmutable();
        Element HW = HashUtil.hashZrArr2Zr(Zr, g, HashUtil.hashStr2ZrArr(Zr, w, n));
        T1_u2 = new Element[3];
        T1_u2[0] = g.powZn(HW).mul(p).getImmutable();
        T1_u2[1] = g.powZn(HW.twice()).mul(p).getImmutable();
        Element three = Zr.newElement(3).getImmutable();
        T1_u2[2] = g.powZn(HW.powZn(three)).mul(p).getImmutable();
        T2_u2 = bp.pairing(g1, g2).powZn(sk_u2[5]).mul(bp.pairing(sk_u2[3], pk_p[1])).getImmutable();
        T3_u2 = bp.pairing(sk_u2[3], pk_kgc).getImmutable();
    }


    public static Element T1, T3;
    public static Element[] T_Q;
    public static void trap(){
        Element s = Zr.newRandomElement().getImmutable();
        T1 = g1.powZn(s).getImmutable();
        Element p1 = pk_u1[1].powZn(sk_p[0]).getImmutable();
        Element p2 = pk_u2[1].powZn(sk_p[0]).getImmutable();
        Element p3 = pk_c.powZn(sk_p[0]).getImmutable();
        T_Q = new Element[3];
        T_Q[0] = T1_u1[0].div(p1).mul(T1_u2[0].div(p2)).mul(p3).getImmutable();
        T_Q[1] = T1_u1[1].div(p1).mul(T1_u2[1].div(p2)).mul(p3).getImmutable();
        T_Q[2] = T1_u1[2].div(p1).mul(T1_u2[2].div(p2)).mul(p3).getImmutable();
        Element p4 = T2_u1.div(bp.pairing(pk_u1[0], sk_p[1])).getImmutable();
        Element p5 = T2_u2.div(bp.pairing(pk_u2[0], sk_p[1])).getImmutable();
        T3 = p4.mul(p5).getImmutable();
    }


    public static boolean match(){
        Element m = Zr.newElement(3);
        Element p1 = bp.pairing(C1, T1).getImmutable();
        Element p2 = C2;
        Element p3 = bp.pairing(B[0], T_Q[0].div(pk_p[0].powZn(sk_c)).powZn(m.invert())).getImmutable();
        Element p4 = bp.pairing(B[1], T_Q[1].div(pk_p[0].powZn(sk_c)).powZn(m.invert())).getImmutable();
        Element p5 = bp.pairing(B[2], T_Q[2].div(pk_p[0].powZn(sk_c)).powZn(m.invert())).getImmutable();
        Element left = p1.mul(p2).mul(p3).mul(p4).mul(p5).getImmutable();
        Element right = T3;

        System.out.println();
        System.out.println(left);
        System.out.println(right);

        return left.isEqual(right);
    }

//    static{
//        bp = PairingFactory.getPairing("a.properties");
//        G = bp.getG1();
//        GT = bp.getGT();
//        Zr = bp.getZr();
//        g = G.newRandomElement().getImmutable();
//        x1 = Zr.newRandomElement().getImmutable();
//        x2 = Zr.newRandomElement().getImmutable();
//        g1 = g.powZn(x1).getImmutable();
//        g2 = g.powZn(x2).getImmutable();
//        t = Zr.newRandomElement().getImmutable();
//        r2 = Zr.newRandomElement().getImmutable();
//        n = 7;
//    }

    public static void main(String[] args) {
        List<String> W = Arrays.asList("word", "chinese", "english");
        enc(W);
    }

}

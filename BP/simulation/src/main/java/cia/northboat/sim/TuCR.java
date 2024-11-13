package cia.northboat.sim;

import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import java.math.BigInteger;
import java.sql.SQLOutput;

public class TuCR {
    private static int n;
    private static Pairing bp;
    private static Field G, GT, Zr;
    private static Element x1, x2, alpha, v, m, sk_svr, sk_f;
    public static Element g, g1, g2, pk_svr, pk_f;
    private static Element[] fi, p_u;

    public static Element f(Element x){
        Element res = v.duplicate();
        // fi 一共 k-1 长，最后一轮 x 的指数将是 k-2+1 = k-1 次，正确的
        for(int i = 0; i < fi.length; i++){
            Element e = x.duplicate();
            e.pow(new BigInteger(String.valueOf(i+1)));
            res.add(fi[i].mul(e));
        }
        return res.getImmutable();
    }

    public static Element H(String str){
        Element[] w = HashUtil.hashStr2ZrArr(Zr, str, n);
//        for(Element e: w){
//            System.out.println(e);
//        }
        Element res = HashUtil.hashZrArr2G(g, w).getImmutable();
//        System.out.println(res);
        return res;
    }

    public static Element H1(Element r){
        return HashUtil.hashZr2G(g, r).getImmutable();
    }

    public static Element H2(Field Zr, Element gt){
        return HashUtil.hashGT2G(Zr, gt).getImmutable();
    }

    public static void setup(Pairing bp, Field G, Field GT, Field Zr, int n, Element g,
                            Element x1, Element x2, Element alpha, Element l, Element f){
        TuCR.bp = bp;
        TuCR.G = G;
        TuCR.GT = GT;
        TuCR.Zr = Zr;
        TuCR.n = n;
        TuCR.x1 = x1;
        TuCR.x2 = x2;
        TuCR.alpha = alpha;
        TuCR.g = g;

        g1 = g.powZn(x1).getImmutable();
        g2 = g.powZn(x2).getImmutable();

        TuCR.sk_svr = l;
        TuCR.pk_svr = g.powZn(l).getImmutable();
        TuCR.sk_f = f;
        TuCR.pk_f = g.powZn(f).getImmutable();

        fi = new Element[2];
        fi[0] = Zr.newElement(11).getImmutable();
        fi[1] = Zr.newElement(13).getImmutable();

        m = Zr.newElement(3).getImmutable();
        v = Zr.newElement(7).getImmutable();

        p_u = new Element[3];
        p_u[0] = Zr.newElement(2).getImmutable();
        p_u[1] = Zr.newElement(3).getImmutable();
        p_u[2] = Zr.newElement(5).getImmutable();
    }


    private static Element[] S_u1, S_u2, S_u3, t_u;
    public static Element[] P_u1, P_u2, P_u3;
    public static Element delta(Element t_u1, Element t_u2){
        return t_u2.negate().div(t_u1.sub(t_u2)).getImmutable();
    }

    public static void keyGen(Element id, Element[] S, Element[] P, int i,
                              Element t_u1, Element t_u2, Element t_u3){

        Element mu = Zr.newRandomElement().getImmutable();
        S[0] = H1(id).powZn(alpha).getImmutable();
        S[1] = f(t_u1).mul(delta(t_u1, t_u2)).mul(delta(t_u1, t_u3)).getImmutable();
        S[2] = mu.getImmutable();

        P[0] = H1(id).getImmutable();
        P[1] = p_u[i].getImmutable();
        P[2] = g.powZn(mu).getImmutable();
    }

    public static void keyGen(Element[] id){
        S_u1 = new Element[3]; S_u2 = new Element[3]; S_u3 = new Element[3];
        P_u1 = new Element[3]; P_u2 = new Element[3]; P_u3 = new Element[3];
        t_u = new Element[3];
        t_u[0] = Zr.newRandomElement().getImmutable();
        t_u[1] = Zr.newRandomElement().getImmutable();
        t_u[2] = Zr.newRandomElement().getImmutable();

        keyGen(id[0], S_u1, P_u1, 0, t_u[0], t_u[1], t_u[2]);
        keyGen(id[1], S_u2, P_u2, 1, t_u[1], t_u[0], t_u[2]);
        keyGen(id[2], S_u3, P_u3, 2, t_u[2], t_u[0], t_u[1]);
    }


    public static Element[] C;
    public static void enc(String str){

        System.out.println("============ Enc =============");

        Element HW = H(str);

        Element r1 = Zr.newRandomElement().getImmutable();
        Element r2 = Zr.newRandomElement().getImmutable();

        System.out.println(r1);
        System.out.println(HW.powZn(r1));
        System.out.println();

        C = new Element[4];
        C[0] = g2.powZn(r2).mul(HW.powZn(r1)).getImmutable();
        C[1] = g2.powZn(v).mul(HW.powZn(r1)).getImmutable();
        C[2] = g1.powZn(r1).getImmutable();
        C[3] = g2.powZn(r2).getImmutable();

        for(Element c: C){
            System.out.println(c);
        }
        System.out.println();
    }


    public static Element[] T;
    public static void localTrap(String str){
        System.out.println("============ Local Trap =============");
        Element HQ = H(str).getImmutable();

        System.out.println(HQ);
        System.out.println();

        Element s = Zr.newRandomElement().getImmutable();

        System.out.println(s);
        System.out.println(HQ.powZn(s).getImmutable());
        System.out.println();

        T = new Element[3];
        T[0] = g1.powZn(s).getImmutable();
        T[1] = HQ.powZn(s).mul(pk_svr.powZn(S_u1[2])).getImmutable();
        T[2] = bp.pairing(g1, g2).powZn(s).getImmutable();

        for(Element t: T){
            System.out.println(t);
        }
        System.out.println();
    }


    public static boolean localMatch(){
        System.out.println("============ Local Match ============");
        Element left = bp.pairing(C[0], T[0]).getImmutable();

        Element p1 = bp.pairing(C[2], T[1].div(P_u1[2].powZn(sk_svr))).getImmutable();
        Element p2 = bp.pairing(T[0], C[3]).getImmutable();
        Element right = p1.mul(p2).getImmutable();

        System.out.println(p1);
        System.out.println(p2);

        System.out.println(left);
        System.out.println(right);

        return left.isEqual(right);
    }


    public static Element N, C_id;
    public static Element[] N_u, N_inv, C_id_su;
    public static void usrAuth() throws Exception{
        System.out.println("=========== Usr Authenticate ===========");

        N = Zr.newOneElement();
        for(Element p: p_u){
            N.mul(p);
        }
        N = N.getImmutable();

        N_u = new Element[3]; N_inv = new Element[3];

        N_u[0] = N.div(p_u[0]).getImmutable();
        // 求在特定模数下的逆
        N_inv[0] = HashUtil.getInvModP(Zr, N_u[0], p_u[0]);
        N_u[1] = N.div(p_u[1]).getImmutable();
        N_inv[1] = HashUtil.getInvModP(Zr, N_u[1], p_u[1]);
        N_u[2] = N.div(p_u[2]).getImmutable();
        N_inv[2] = HashUtil.getInvModP(Zr, N_u[2], p_u[2]);

        for(int i = 0; i < 3; i++){
            System.out.print(N_u[i] + " ");
            System.out.println(N_inv[i]);
        }

        C_id_su = new Element[3];
        Element e1 = H2(Zr, bp.pairing(S_u3[0], P_u1[0])).getImmutable();
        Element e2 = H2(Zr, bp.pairing(S_u3[0], P_u2[0])).getImmutable();
        Element e3 = H2(Zr, bp.pairing(S_u3[0], P_u3[0])).getImmutable();

        C_id_su[0] = HashUtil.getInvModP(Zr, e1, P_u1[1]).getImmutable();
        C_id_su[1] = HashUtil.getInvModP(Zr, e2, P_u2[1]).getImmutable();
        C_id_su[2] = HashUtil.getInvModP(Zr, e3, P_u3[1]).getImmutable();


        C_id = Zr.newZeroElement();
        for(int i = 0; i < 3; i++){
            Element e = C_id_su[i].mul(N_u[i]).mul(N_inv[i]).getImmutable();
            C_id.add(e);
        }
        C_id = HashUtil.getInvModP(Zr, C_id, N).getImmutable();

        System.out.println(e1 + " % " + P_u1[1] + " = " + C_id_su[0]);
        System.out.println(e2 + " % " + P_u2[1] + " = " + C_id_su[1]);
        System.out.println(e3 + " % " + P_u3[1] + " = " + C_id_su[2]);
        System.out.println(N);
        System.out.println(C_id);
        System.out.println();

    }


    public static Element[] C_id_u;
    public static void usrAuthorize(){
        C_id_u = new Element[3];
        C_id_u[0] = HashUtil.getInvModP(Zr, H2(Zr, bp.pairing(P_u3[0], S_u1[0])), P_u1[1]).getImmutable();
        C_id_u[1] = HashUtil.getInvModP(Zr, H2(Zr, bp.pairing(P_u3[0], S_u2[0])), P_u2[1]).getImmutable();
        C_id_u[2] = HashUtil.getInvModP(Zr, H2(Zr, bp.pairing(P_u3[0], S_u3[0])), P_u3[1]).getImmutable();
    }

    public static boolean usrIDMatch(){
        System.out.println("========= User Identity Match =========");
        Element left1 = C_id_su[0];
        Element right1 = HashUtil.getInvModP(Zr, C_id_u[0], P_u1[1]);
        Element left2 = C_id_su[1];
        Element right2 = HashUtil.getInvModP(Zr, C_id_u[1], P_u2[1]);
        Element left3 = C_id_su[2];
        Element right3 = HashUtil.getInvModP(Zr, C_id_u[2], P_u3[1]);
        System.out.println(left1 + "  " + right1);
        System.out.println(left2 + "  " + right2);
        System.out.println(left3 + "  " + right3);
        return left1.isEqual(right1) && left2.isEqual(right2) && left3.isEqual(right3);
    }


    public static Element[] Af_u = new Element[3];
    public static Element UAf;
    public static void usrAuthorizationFactor(){
        System.out.println("========= User Authorization Factor ==========");
        Element p = bp.pairing(g1, g2).getImmutable();
        Element p1 = bp.pairing(P_u3[0], S_u1[0]).getImmutable();
        Element p2 = bp.pairing(P_u3[0], S_u2[0]).getImmutable();

        Af_u[0] = p.powZn(S_u1[1]).mul(p1).getImmutable();
        Af_u[1] = p.powZn(S_u2[1]).mul(p2).getImmutable();
        UAf = Af_u[0].mul(Af_u[1]).getImmutable();

        System.out.println(Af_u[0]);
        System.out.println(Af_u[1]);
//        System.out.println(Af_u[0].mul(Af_u[1]));
        System.out.println(UAf);
        System.out.println();
    }



    public static Element K, A;
    public static void retrievalAuthGen(){
        System.out.println("======= Retrieval Auth Gen =======");
        Element p1 = bp.pairing(P_u3[0], S_u1[0]).getImmutable();
        Element p2 = bp.pairing(P_u3[0], S_u2[0]).getImmutable();
        K = p1.mul(p2).getImmutable();

        Af_u[2] = bp.pairing(g1, g2).powZn(S_u3[1]).getImmutable();
        A = UAf.div(K).mul(Af_u[2]).getImmutable();

        System.out.println(K);
        System.out.println(Af_u[2]);
        System.out.println(A);
        System.out.println();
    }


    public static Element[] T_h;
    public static void federatedTrap(String q){
        System.out.println("======== Federated Trap ==========");
        Element s = Zr.newRandomElement().getImmutable();
        T_h = new Element[3];
        T_h[0] = g1.powZn(s).getImmutable();
        T_h[1] = H(q).powZn(s).mul(pk_f.powZn(S_u3[2])).getImmutable();
        T_h[2] = A.powZn(s).getImmutable();

        for(Element t: T_h){
            System.out.println(t);
        }
        System.out.println(bp.pairing(g1, g2).powZn(v.mul(s)));
        System.out.println();
    }


    public static boolean federatedMatch(){
        System.out.println("======= Federated Match ========");

        Element left = bp.pairing(C[1], T_h[0]).getImmutable();

        Element p = T_h[1].div(P_u3[2].powZn(sk_f)).getImmutable();
        Element right = bp.pairing(C[2], p).mul(T_h[2]).getImmutable();

        System.out.println(p);
        System.out.println(T_h[2]);
        System.out.println(left);
        System.out.println(right);

        return left.isEqual(right);
    }

}

package cia.northboat.util;

import cia.northboat.pojo.Cy;
import cia.northboat.pojo.Location;
import cia.northboat.pojo.QuadTree;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;

public class IPFEUtil {
    private static Pairing bp;
    private static Field G, Zr;
    private static int l;

    // 公钥
    public static Element g, h;
    private static Element[] s, t, h_i;

    static{
        setup();
    }

    public static void setup(){
        System.out.println("====== Setup ======");

        bp = PairingFactory.getPairing("a.properties");
        G = bp.getG1();
        Zr = bp.getZr();
        l = 50;

        g = G.newRandomElement().getImmutable();
        h = G.newRandomElement().getImmutable();

        s = new Element[l];
        t = new Element[l];
        h_i = new Element[l];
        for(int i = 0; i < l; i++){
            s[i] = Zr.newRandomElement().getImmutable();
            t[i] = Zr.newRandomElement().getImmutable();
            h_i[i] = g.powZn(s[i]).mul(g.powZn(t[i])).getImmutable();
        }

        r = Zr.newRandomElement().getImmutable();
    }


    public static QuadTree keyGen(Element[] x, Location p){
        System.out.println("====== KeyGen ======");
        Element s1 = Zr.newZeroElement();
        Element s2 = Zr.newZeroElement();
        for(int i = 0; i < l; i++){
//            System.out.println(x[i]);
            s1.add(s[i].mul(x[i]));
            s2.add(t[i].mul(x[i]));
        }
        Element s_x = s1.getImmutable();
        Element t_x = s2.getImmutable();

        System.out.println("s_x: " + s_x);
        System.out.println("t_x: " + t_x);


        return new QuadTree(p, x, s_x, t_x);
    }


    private static Element r;
    public static Cy encrypt(Element[] y){
        System.out.println("====== Encrypt ======");
        Element C = g.powZn(r).getImmutable();
        Element D = h.powZn(r).getImmutable();
        Element[] E = new Element[l];
        for(int i = 0; i < l; i++){
            E[i] = g.powZn(y[i]).mul(h_i[i].powZn(r)).getImmutable();
        }

        return new Cy(C, D, E);
    }

    public static Element decrypt(QuadTree t, Cy c){

        System.out.println("====== Decrypt ======");
        Element[] x = t.getX();
        Element s_x = t.getS_x();
        Element t_x = t.getT_x();

        Element e = G.newOneElement();
        for(int i = 0; i < l; i++){
            e.mul(c.getE()[i].powZn(x[i]));
        }

        Element p1 = e.getImmutable();
        Element p2 = c.getC().powZn(s_x).mul(c.getD().powZn(t_x)).getImmutable();

        return p1.div(p2).getImmutable();
    }



    public static void main(String[] args) {
        Location p = new Location(1, 1);
        String str = "101010";
        Element[] x = hashStr2ZrArr(str);
        QuadTree t = keyGen(x, p);

        String q = "101010";
        Element[] y = hashStr2ZrArr(q);
        Cy c = encrypt(y);

        Element res = decrypt(t, c);
        System.out.println(res);
    }


    public static Element[] hashStr2ZrArr(String word){
        Element[] W = new Element[l];
        for(int i = 0; i < l; i++){
            if(i >= word.length()){
                W[i] = Zr.newZeroElement().getImmutable();
                continue;
            }
            // ASCII 码
            int number = word.charAt(i)+95;
            W[i] = Zr.newElement(number).getImmutable();
        }
        return W;
    }
}

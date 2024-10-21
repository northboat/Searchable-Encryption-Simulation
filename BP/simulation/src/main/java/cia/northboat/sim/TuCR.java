package cia.northboat.sim;

import cia.northboat.util.HashUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.math.BigInteger;

public class TuCR {
    private static int n;
    private static Pairing bp;
    private static Field G, GT, Zr;
    private static Element x1, x2, v, a, id_u, sk_svr, sk_f;
    public static Element g, g1, g2, pk_svr, pk_f;

    private static Element[] fi;

    public static Element f(Element x){
        Element h = v;
        // fi 一共 k-1 长，最后一轮 x 的指数将是 k-2+1 = k-1 次，正确的
        for(int i = 0; i < fi.length; i++){
            Element e = x;
            e.pow(new BigInteger(String.valueOf(i+1)));
            h.add(fi[i].mul(e));
        }
        return h;
    }

    public static void init(Pairing bp, Field G, Field GT, Field Zr, int n,
                            Element x1, Element x2, Element v, Element a, Element l, Element f,
                            Element g, Element id_u, Element[] fi){
        TuCR.bp = bp;
        TuCR.G = G;
        TuCR.GT = GT;
        TuCR.Zr = Zr;
        TuCR.n = n;
        TuCR.x1 = x1;
        TuCR.x2 = x2;
        TuCR.v = v;
        TuCR.a = a;

        TuCR.sk_f = f;
        TuCR.pk_f = g.powZn(f).getImmutable();
        TuCR.sk_svr = l;
        TuCR.pk_svr = g.powZn(l).getImmutable();

        g1 = g.powZn(x1).getImmutable();
        g2 = g.powZn(x2).getImmutable();

        TuCR.fi = fi;

        TuCR.id_u = id_u;

        Element t_u = Zr.newRandomElement().getImmutable();
        Element S1 = HashUtil.hashZr2G(g, id_u).powZn(a).getImmutable();
    }
}

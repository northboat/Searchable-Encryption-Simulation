package cia.northboat.util;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

public class HashUtil {

    // 字符串映射，通过 ASCII 码将每个字符映射到 Zr 群上
    // 映射为 Zr 群上的整数数组，但文献上给的 {0,1} 串（可能是布隆过滤器）
    public static Element[] mapping(Field Zr, String word, int n){
        Element[] W = new Element[n];
        for(int i = 0; i < n; i++){
            if(i >= word.length()){
                W[i] = Zr.newZeroElement().getImmutable();
                continue;
            }
            // ASCII 码
            int number = word.charAt(i);
            W[i] = Zr.newElement(number).getImmutable();
        }
        return W;
    }

    // 把 Zr 群上元素 r 通过 G 上生成元 g 映射到 G 上
    public static Element H3(Element g, Element r){
        return g.powZn(r).getImmutable();
    }

    // 四个 G 上的元素通过累乘哈希为一个 G 上的元素
    public static Element H0(Element g1, Element g2, Element g3, Element g4){
        return g1.mul(g2).mul(g3).mul(g4).getImmutable();
    }

    // 将 Zr 群上的数组 w 通过 G 上的生成元 g 映射为 Zr 群上的单个元素
    public static Element H1(Field Zr, Element g, Element[] w){
        Element h = g;
        for(Element e: w){
            h.powZn(e);
        }
        return HashUtil.H2(Zr, h);
    }

    // 将 G 上元素 e 映射到 Zr 群上
    public static Element H2(Field Zr, Element e){
        byte[] hash = e.toBytes();
        return Zr.newElementFromHash(hash, 0, hash.length).getImmutable();
    }

    // 通过生成元 g 把 {0,1}* 映射到 G 上
    public static Element H4(Element g, Element[] w){
        Element h = g;
        for(Element e: w){
            h.powZn(e);
        }
        return h.getImmutable();
    }

    public static Element H(Element pk1, Element pk2, Element[] w){
        Element h = pk1.mul(pk2);
        for(Element e: w){
            h.powZn(e);
        }
        return h;
    }

}

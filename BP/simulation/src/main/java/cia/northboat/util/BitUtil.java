package cia.northboat.util;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.math.BigInteger;

public class BitUtil {
    public static Element xor(Field Zr, Element e1, Element e2){
        BigInteger i1 = e1.toBigInteger();
        BigInteger i2 = e2.toBigInteger();
        BigInteger res = i1.xor(i2);

        return Zr.newElement(res.mod(Zr.getOrder())).getImmutable();
    }

    public static Element connect(Field Zr, Element e1, Element e2, Element e3){
        BigInteger b1 = e1.toBigInteger(), b2 = e2.toBigInteger(), b3 = e3.toBigInteger();
        BigInteger bShifted = b1.shiftLeft(b2.bitLength());
        BigInteger bMerged = bShifted.or(b2);
        BigInteger bMergedShifted = bMerged.shiftLeft(b3.bitLength());
        BigInteger b = bMergedShifted.or(b3);
        return Zr.newElement(b.mod(Zr.getOrder())).getImmutable();
    }
}

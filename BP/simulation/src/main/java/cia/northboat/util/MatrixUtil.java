package cia.northboat.util;

import it.unisa.dia.gas.jpbc.Field;

import java.math.BigInteger;

public class MatrixUtil {

    public static BigInteger[] getOneVector(int n){
        BigInteger[] b = new BigInteger[n];
        for(int i = 0; i < n; i++){
            b[i] = new BigInteger("1");
        }
        return b;
    }

    // 求行列式
    public static BigInteger determinant(BigInteger[][] matrix) {
        int n = matrix.length;
        if (n == 1) {
            return matrix[0][0];
        }
        BigInteger det = BigInteger.ZERO;
        BigInteger sign = BigInteger.ONE;
        for (int i = 0; i < n; i++) {
            BigInteger[][] subMatrix = getSubMatrix(matrix, 0, i);
            det = det.add(sign.multiply(matrix[0][i]).multiply(determinant(subMatrix)));
            sign = sign.negate();
        }
        return det;
    }


    public static BigInteger[][] getSubMatrix(BigInteger[][] matrix, int excludingRow, int excludingCol) {
        int n = matrix.length;
        BigInteger[][] subMatrix = new BigInteger[n - 1][n - 1];
        int r = -1;
        for (int i = 0; i < n; i++) {
            if (i == excludingRow) continue;
            r++;
            int c = -1;
            for (int j = 0; j < n; j++) {
                if (j == excludingCol) continue;
                subMatrix[r][++c] = matrix[i][j];
            }
        }
        return subMatrix;
    }

    // 方法：计算伴随矩阵
    public static BigInteger[][] adjugate(BigInteger[][] matrix) {
        int n = matrix.length;
        BigInteger[][] adj = new BigInteger[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                BigInteger[][] subMatrix = getSubMatrix(matrix, i, j);
                adj[j][i] = ((i + j) % 2 == 0 ? BigInteger.ONE : BigInteger.ONE.negate())
                        .multiply(determinant(subMatrix));
            }
        }
        return adj;
    }


    // 方法：计算逆矩阵
    public static BigInteger[][] inverse(Field Zr, BigInteger[][] matrix) {
        BigInteger det = determinant(matrix);
        det = det.mod(Zr.getOrder());
//        System.out.println(det);
//        System.out.println();
        if (det.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Matrix is not invertible.");
        }
        BigInteger[][] adj = adjugate(matrix);
        BigInteger[][] inv = new BigInteger[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                inv[i][j] = adj[i][j].divide(det).mod(Zr.getOrder());
//                System.out.println(adj[i][j].divide(det));
            }
        }
        return inv;
    }

    public static BigInteger[] mul(BigInteger[][] matrix, BigInteger[] vector){
        int n = matrix.length, m = vector.length;
        BigInteger[] res = new BigInteger[n];
        for(int i = 0; i < n; i++){
            BigInteger row = new BigInteger("0");
            for(int j = 0; j < m; j++){
                row = row.add(matrix[i][j].multiply(vector[j]));
            }
            res[i] = row;
        }
        return res;
    }

    public static void main(String[] args) {

//
//        // 解方程
//        BigInteger[][] matrix = new BigInteger[W.size()][3];
//        for(int i = 0; i < W.size(); i++){
//            for(int j = 0; j < 3; j++){
//                matrix[i][j] = HW[i].powZn(Zr.newElement(3-j)).toBigInteger().mod(Zr.getOrder());
//            }
//        }
//
//        for(BigInteger[] row: matrix){
//            for(BigInteger i: row){
//                System.out.print(i + "   ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//
//        // 求逆
//        matrix = MatrixUtil.inverse(Zr, matrix);
//        for(BigInteger[] row: matrix){
//            for(BigInteger i: row){
//                System.out.print(i + "   ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//
//        BigInteger[][] test = MatrixUtil.inverse(Zr, matrix);
//        for(BigInteger[] row: test){
//            for(BigInteger i: row){
//                System.out.print(i + "   ");
//            }
//            System.out.println();
//        }
//        System.out.println();

        // x^3-6x^2+11x-5 = 1
        // 求解，从 3 到 1
//        BigInteger[] vec = MatrixUtil.mul(matrix, MatrixUtil.getOneVector(3));
    }
}

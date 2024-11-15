package cia.northboat;


import cia.northboat.pojo.Location;
import cia.northboat.pojo.QuadTree;
import cia.northboat.util.TreeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.openjdk.jol.info.ClassLayout;


public class Main {

    static int n = 1;
    static long s1, e1, s2, e2, s3, e3;
    public static void main(String[] args) {

        genPoint();
        QuadTree root = null;
        Object lock1 = new Object();
        Object lock2 = new Object();
        Object lock3 = new Object();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                s1 = System.currentTimeMillis();
                for(int i = 0; i < n; i++){
                    TreeUtil.build(O);
                }
                e1 = System.currentTimeMillis();
                synchronized (lock1) {//获取对象锁
                    lock1.notify();//子线程唤醒
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                s2 = System.currentTimeMillis();
                for(int i = 0; i < n; i++){
                    TreeUtil.build(O);
                }
                e2 = System.currentTimeMillis();
                synchronized (lock2) {//获取对象锁
                    lock2.notify();//子线程唤醒
                }
            }
        });

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                s3 = System.currentTimeMillis();
                for(int i = 0; i < n; i++){
                    TreeUtil.build(O);
                }
                e3 = System.currentTimeMillis();
                synchronized (lock3) {//获取对象锁
                    lock3.notify();//子线程唤醒
                }
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            synchronized (lock1) {//这里也是一样
                lock1.wait();//主线程等待
            }
            synchronized (lock2) {//这里也是一样
                lock2.wait();//主线程等待
            }
            synchronized (lock3) {//这里也是一样
                lock3.wait();//主线程等待
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        bfs(root);
        System.out.println((e1-s1+e2-s2+e3-s3)/n + " ms");
//        System.out.println(ClassLayout.parseInstance(root));
    }


    public static void printTree(QuadTree t){
        if(t != null) {
            System.out.print(t.getP() + "\tX: ");
            System.out.print(Arrays.toString(t.getX()) + "\ts_x: ");
            System.out.print(t.getS_x() + "\tt_x: ");
            System.out.print(t.getT_x());
            System.out.println();
        }
    }

    public static void bfs(QuadTree root){
        List<QuadTree> list = new ArrayList<>();
        if(root != null){
            list.add(root);
        }

        while(!list.isEmpty()){
            QuadTree cur = list.get(0);
            printTree(cur);
            for(int i = 0; i < 4; i++){
                if(cur.getSubtree()[i] != null){
                    list.add(cur.getSubtree()[i]);
                }
            }
            list.remove(0);
        }
    }


    static Location[] O;
    public static void genPoint() {
        double centerX = 0; // 圆心的横坐标
        double centerY = 0; // 圆心的纵坐标
        double radius = 900; // 圆半径
        int numPoints = 100000; // 生成的点数

        Random random = new Random();
        O = new Location[numPoints];
        double[] x = new double[numPoints];
        double[] y = new double[numPoints];

        // 生成圆内的点
        for (int i = numPoints - 1; i >= 0; i--) {
            x[i] = centerX + random.nextDouble() * radius;
            y[i] = centerY + random.nextDouble() * radius;
            O[i] = new Location((int)x[i], (int)y[i]);
        }
        // 输出点坐标
        for (int i = 0; i < numPoints; i++) {
            System.out.println("["+x[i]+","+y[i]+"]");
        }

        for(Location o: O){
            System.out.println(o);
        }
    }

}
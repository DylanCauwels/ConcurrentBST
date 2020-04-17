package lockfree.bst;

import org.junit.Test;
import java.util.Random;

public class Tester {

    @Test
    public void test() {
        BST tree = new BST();

        int[] inserts = new int[100];
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            inserts[i] = r.nextInt(1000);
        }

        for (int i:inserts) {
            tree.insert(i);
        }

        System.out.println(tree);
//        Assert.assertEquals(true, true);
    }

    @Test
    public void concurrentInsert() throws InterruptedException {
        BST tree = new BST();
        Putter put = new Putter(tree);
        Thread[] threads = new Thread[10000];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(put, String.valueOf(i));
            threads[i].start();
        }

        for (Thread t: threads) {
            t.join();
        }

        for (int i = 0; i < 10000; i++) {
            if (!tree.contains(i)) {
                System.out.println("does not contain: " + i);
            }
        }
    }

    class Remover implements Runnable {
        BST tree;

        Remover(BST tree) {
            this.tree = tree;
        }

        @Override
        public void run() {

        }
    }

    class Putter implements Runnable{
        BST tree;

        Putter(BST tree) {
            this.tree = tree;
        }

        @Override
        public void run() {
            tree.insert(Integer.valueOf(Thread.currentThread().getName()));
        }
    }
}

package lockfree.bst;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class Tester {

    @Test
    public void sequentialInsertAll() {
        BST tree = new BST();
        int numNodes = 10000;
        ArrayList<Integer> missedValues = new ArrayList<>();

        for (int i = 0; i < numNodes; i++) {
            tree.insert(i);
        }
        for (int i = 0; i < numNodes; i++) {
            if (!tree.contains(i))
                missedValues.add(i);
        }

        assertTrue(missedValues.isEmpty());
    }

    @Test
    public void concurrentInsertAll() throws InterruptedException {
        BST tree = new BST();
        int numNodes = 10000;
        Putter put = new Putter(tree);
        Thread[] threads = new Thread[numNodes];
        ArrayList<Integer> missedValues = new ArrayList<>();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(put, String.valueOf(i));
            threads[i].start();
        }

        for (Thread t: threads) {
            t.join();
        }

        for (int i = 0; i < numNodes; i++) {
            if (!tree.contains(i)) {
                missedValues.add(i);
                System.out.println("missed value on concurrent insert all: " + i);
            }
        }

        assertTrue(missedValues.isEmpty());
    }

    @Test
    public void sequentialDeleteAll() {
        BST tree = new BST();
        int numNodes = 10000;
        ArrayList<Integer> remainingValues = new ArrayList<>();

        for (int i = 0; i < numNodes; i++) {
            tree.insert(i);
        }
        for (int i = 0; i < numNodes; i++) {
            tree.delete(i);
        }
        for (int i = 0; i < numNodes; i++) {
            if (tree.contains(i))
                remainingValues.add(i);
        }

        assertTrue(remainingValues.isEmpty());
    }

    @Test
    public void concurrentDeleteSome() throws InterruptedException {
        BST tree = new BST();
        int numNodes = 10000;
        Remover rem = new Remover(tree);
        Thread[] threads = new Thread[numNodes];
        ArrayList<Integer> missedValues = new ArrayList<>();

        for (int i = 0; i < numNodes; i++) {
            tree.insert(i);
        }

        for (int i = 0; i < threads.length; i++) {
            if (i % 3 == 0) {
                threads[i] = new Thread(rem, String.valueOf(i));
                threads[i].start();
            } else {
                threads[i] = null;
            }

        }

        for (Thread t: threads) {
            if (t != null) t.join();
        }

        for (int i = 0; i < numNodes; i++) {
            if (i % 3 == 0 && tree.contains(i)) {
                missedValues.add(i);
                System.out.println("missed value on concurrent delete some: " + i);
            }
        }

        assertTrue(missedValues.isEmpty());
    }

    @Test
    public void concurrentDeleteAll() throws InterruptedException {
        BST tree = new BST();
        int numNodes = 10000;
        Remover rem = new Remover(tree);
        Thread[] threads = new Thread[numNodes];
        ArrayList<Integer> missedValues = new ArrayList<>();

        for (int i = 0; i < numNodes; i++) {
            tree.insert(i);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(rem, String.valueOf(i));
            threads[i].start();
        }

        for (Thread t: threads) {
            t.join();
        }

        for (int i = 0; i < numNodes; i++) {
            if (tree.contains(i)) {
                missedValues.add(i);
                System.out.println("missed value on concurrent delete all: " + i);
            }
        }

        assertTrue(missedValues.isEmpty());
    }

    class Remover implements Runnable {
        BST tree;

        Remover(BST tree) {
            this.tree = tree;
        }

        @Override
        public void run() {
            tree.delete(Integer.valueOf(Thread.currentThread().getName()));
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

import lock.bst.BST;
import lock.singlelock.SLBST;
import org.junit.Test;
import util.TreeInterface;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Tester {

    // HELPER CLASSES //

    class ThreadBSTSingleAddRemove implements Runnable {

        private lock.bst.BST hohLock;
        private lockfree.bst.BST lFree;
        private SLBST sLock;

        public ThreadBSTSingleAddRemove(BST hohLock, lockfree.bst.BST lFree, SLBST sLock) {
            this.hohLock = hohLock;
            this.lFree = lFree;
            this.sLock = sLock;
        }

        // implement run method for thread
        public void run() {
            Random r = new Random();
            int toAdd = r.nextInt(200);

            int i = (int)Thread.currentThread().getId();
            if (i % 2 == 0) {
//                System.out.println("adding " + toAdd);
//                if (hohLock.insert(toAdd) && lFree.insert(toAdd) && sLock.insert(toAdd)) System.out.println(i + ":    successfully added " + toAdd + " to all trees");
                hohLock.insert(toAdd);
                lFree.insert(toAdd);
                sLock.insert(toAdd);
            }
            else {
//                System.out.println("deleting " + toAdd);
//                if (hohLock.delete(toAdd) && lFree.delete(toAdd) && sLock.delete(toAdd)) System.out.println(i + ":    successfully deleted " + toAdd + " from all trees");
                hohLock.delete(toAdd);
                lFree.delete(toAdd);
                sLock.delete(toAdd);
            }
        }
    }

    class ThreadBSTSingleAddRemoveContains implements Runnable {
        private lock.bst.BST hohLock;
        private lockfree.bst.BST lFree;
        private SLBST sLock;

        public ThreadBSTSingleAddRemoveContains(BST hohLock, lockfree.bst.BST lFree, SLBST sLock) {
            this.hohLock = hohLock;
            this.lFree = lFree;
            this.sLock = sLock;
        }


        @Override
        public void run() {
            Random r = new Random();
            int key = r.nextInt(500);

            int i = (int)Thread.currentThread().getId();
            if (i % 3 == 0) {
                hohLock.insert(key);
                lFree.insert(key);
                sLock.insert(key);
            }
            else if (i % 3 == 1) {
                hohLock.contains(key);
                lFree.contains(key);
                sLock.contains(key);
            }
            else {
                hohLock.delete(key);
                lFree.delete(key);
                sLock.delete(key);
            }
        }
    }

    class Inserter implements Runnable{
        private TreeInterface tree;
        private int inserts;
        private int bound;

        Inserter(TreeInterface tree, int in, int bound) {
            this.tree = tree;
            inserts = in;
            this.bound = bound;
        }

        @Override
        public void run() {
            Random r = new Random();
            for (int i = 0; i < inserts; i++) {
                tree.insert(r.nextInt(bound));
            }
        }
    }
    class Deleter implements Runnable{
        private TreeInterface tree;

        Deleter(TreeInterface tree) {
            this.tree = tree;
        }

        @Override
        public void run() {
            tree.delete(Integer.valueOf(Thread.currentThread().getName()));
        }
    }
    class ContainChecker implements Runnable{
        private TreeInterface tree;

        ContainChecker(TreeInterface tree) {
            this.tree = tree;
        }

        @Override
        public void run() {
            tree.contains(Integer.valueOf(Thread.currentThread().getName()));
        }
    }

    // HELPER FUNCTIONS //

    public void incTest(ArrayList<Integer> list) {
        for (int i = 1; i < list.size(); i++) {
            Assert.assertTrue(list.get(i-1) < list.get(i));
        }
    }

    public void printArrays(ArrayList<Integer> arrayList) {
        for (Integer i:
                arrayList) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
    }

    // ACTUAL TESTS //

    @Test
    public void runAllCorrectness() throws InterruptedException {
        double iterations = 10;
        double count = 0;
        for (int i = 0; i < iterations; i++) {
            try {
                System.out.println(i+1);
                correctnessInsertDelete();
                correctnessInsertDeleteContains();
            }
            catch (Exception e) {
                count++;
            }
        }
        System.out.println("Success percentage: " + (iterations-count)*100/iterations);
    }

    @Test
    public void correctnessInsertDelete() throws InterruptedException {
        lock.bst.BST hohLock = new lock.bst.BST();
        lockfree.bst.BST lFree =  new lockfree.bst.BST();
        SLBST sLock = new SLBST();
        int numNodes = 500;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; i++) {
            threads[i] = new Thread(new ThreadBSTSingleAddRemove(hohLock, lFree, sLock));
            threads[i].start();
        }

        for (int i = 0; i<numNodes; i++) {
            threads[i].join();
        }

        ArrayList<Integer> hohVals = hohLock.inorderTraversalTester();

        ArrayList<Integer> lfreeVals = new ArrayList<>();
        String vals = lFree.toString();
        String[] s = vals.trim().split("\n");
        ArrayList<String> stringVals = new ArrayList<>(Arrays.asList(s));
        stringVals.forEach(i -> lfreeVals.add(Integer.parseInt(i)));

        ArrayList<Integer> sLockVals = sLock.inorderTraversalTester();

        incTest(hohVals);
        incTest(lfreeVals);
        incTest(sLockVals);
        Assert.assertArrayEquals("Comparing hand over hand locking and single lock ", sLockVals.toArray(), hohVals.toArray());
        Assert.assertArrayEquals("Comparing lockfree and single lock ", sLockVals.toArray(), lfreeVals.toArray());
        System.out.print("Hand over Hand: ");
        printArrays(hohVals);
        System.out.print("Lock Free:      ");
        printArrays(lfreeVals);
        System.out.print("Single Lock:    ");
        printArrays(sLockVals);
    }

    @Test
    public void correctnessInsertDeleteContains() throws InterruptedException {
        lock.bst.BST hohLock = new lock.bst.BST();
        lockfree.bst.BST lFree =  new lockfree.bst.BST();
        SLBST sLock = new SLBST();
        int numNodes = 5000;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; i++) {
            threads[i] = new Thread(new ThreadBSTSingleAddRemoveContains(hohLock, lFree, sLock));
            threads[i].start();
        }

        for (int i = 0; i<numNodes; i++) {
            threads[i].join();
        }

        ArrayList<Integer> hohVals = hohLock.inorderTraversalTester();

        ArrayList<Integer> lfreeVals = new ArrayList<>();
        String vals = lFree.toString();
        String[] s = vals.trim().split("\n");
        ArrayList<String> stringVals = new ArrayList<>(Arrays.asList(s));
        stringVals.forEach(i -> lfreeVals.add(Integer.parseInt(i)));

        ArrayList<Integer> sLockVals = sLock.inorderTraversalTester();

        incTest(hohVals);
        incTest(lfreeVals);
        incTest(sLockVals);
        Assert.assertArrayEquals("Comparing hand over hand locking and single lock ", sLockVals.toArray(), hohVals.toArray());
        Assert.assertArrayEquals("Comparing lockfree and single lock ", sLockVals.toArray(), lfreeVals.toArray());
        System.out.print("Hand over Hand: ");
        printArrays(hohVals);
        System.out.print("Lock Free:      ");
        printArrays(lfreeVals);
        System.out.print("Single Lock:    ");
        printArrays(sLockVals);
    }

    // test to compare insert times across the three implementations
    // will run with these configs:
    //  1 thread, 100,000 inserts per thread
    //  5 threads, 50,000 inserts per thread
    //  100 threads, 1,000 inserts per thread
    //  10,000 threads, 10 inserts per thread
    @Test
    public void timing_insert() throws InterruptedException {
        // set up
        lock.bst.BST lockBST = new lock.bst.BST();
        lockfree.bst.BST lockfreeBST = new lockfree.bst.BST();
        SLBST seqBST = new SLBST();
        // number of concurrent threads
        int numThreads = 1;
        // number of inserts each thread will perform
        int in_per_thread = 5;
        // endpoint of range for keys
        int bound = 25000;
        // create Runnables
        Inserter[] inserters = new Inserter[3];
        inserters[0] = new Inserter(lockBST, in_per_thread, bound);
        inserters[1] = new Inserter(lockfreeBST, in_per_thread, bound);
        inserters[2] = new Inserter(seqBST, in_per_thread, bound);

        // run each tree with the specified settings
        for(int j = 0; j<inserters.length; j++) {
            long start = System.nanoTime();
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(inserters[j], String.valueOf(i));
                threads[i].start();
            }
            for (Thread t : threads) {
                t.join();
            }
            long elapsed = System.nanoTime() - start;
            System.out.println("Time: " + elapsed);
        }
    }

    // test to compare delete times across the three implementations
    // will run with these configs:
    //  500 threads
    //  1,000 threads
    //  10,000 threads
    //  100,000 threads
    //  1 million threads
    @Test
    public void timing_delete() throws InterruptedException {
        // set up
        lock.bst.BST lockBST = new lock.bst.BST();
        lockfree.bst.BST lockfreeBST = new lockfree.bst.BST();
        SLBST seqBST = new SLBST();
        // number of inserts each thread will perform
        int elements = 500;
        // create Runnables
        Deleter[] deleters= new Deleter[3];
        deleters[0] = new Deleter(lockBST);
        deleters[1] = new Deleter(lockfreeBST);
        deleters[2] = new Deleter(seqBST);
        // populate trees
        for (int i = 0; i<elements; i++) {
            lockBST.insert(i);
            lockfreeBST.insert(i);
            seqBST.insert(i);
        }
        // run each tree with the specified settings
        // each deleter deletes only one element, so
        // we create a thread for every element
        for(int j = 0; j<deleters.length; j++) {
            long start = System.nanoTime();
            Thread[] threads = new Thread[elements];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(deleters[j], String.valueOf(i));
                threads[i].start();
            }
            for (Thread t : threads) {
                t.join();
            }
            long elapsed = System.nanoTime() - start;
            System.out.println("Time: " + elapsed);
        }
    }

    // test to compare delete times across the three implementations
    // will run with these configs:
    //  500 threads
    //  1,000 threads
    //  10,000 threads
    //  100,000 threads
    //  1 million threads
    @Test
    public void timing_contains() throws InterruptedException {
        // set up
        lock.bst.BST lockBST = new lock.bst.BST();
        lockfree.bst.BST lockfreeBST = new lockfree.bst.BST();
        SLBST seqBST = new SLBST();
        // number of inserts each thread will perform
        int elements = 10000;
        // create Runnables
        ContainChecker[] checkers = new ContainChecker[3];
        checkers[0] = new ContainChecker(lockBST);
        checkers[1] = new ContainChecker(lockfreeBST);
        checkers[2] = new ContainChecker(seqBST);
        // populate trees
        for (int i = 0; i<elements; i++) {
            lockBST.insert(i);
            lockfreeBST.insert(i);
            seqBST.insert(i);
        }
        // run each tree with the specified settings
        // each checker checks only one element, so
        // we create a thread for every element
        for(int j = 0; j<checkers.length; j++) {
            long start = System.nanoTime();
            Thread[] threads = new Thread[elements];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(checkers[j], String.valueOf(i));
                threads[i].start();
            }
            for (Thread t : threads) {
                t.join();
            }
            long elapsed = System.nanoTime() - start;
            System.out.println("Time: " + elapsed);
        }
    }

    // test to compare mixed operation times across the three implementations
    // inserts are performed simultaneously with deletes
    // will run with these configs:
    //  1 thread, 100,000 inserts per thread
    //  5 threads, 50,000 inserts per thread
    //  100 threads, 1,000 inserts per thread
    //  10,000 threads, 10 inserts per thread
    @Test
    public void timing_mix() throws InterruptedException {
        // set up
        lock.bst.BST lockBST = new lock.bst.BST();
        lockfree.bst.BST lockfreeBST = new lockfree.bst.BST();
        SLBST seqBST = new SLBST();
        // number of concurrent threads
        int numThreads = 1;
        // number of inserts each thread will perform
        int in_per_thread = 5;
        // max key possible for inserters
        int bound = 1000;
        // create inserters
        Inserter[] inserters = new Inserter[3];
        inserters[0] = new Inserter(lockBST, in_per_thread, bound);
        inserters[1] = new Inserter(lockfreeBST, in_per_thread, bound);
        inserters[2] = new Inserter(seqBST, in_per_thread, bound);
        // create deleters
        Deleter[] deleters= new Deleter[3];
        deleters[0] = new Deleter(lockBST);
        deleters[1] = new Deleter(lockfreeBST);
        deleters[2] = new Deleter(seqBST);
        // run each tree with the specified settings
        for(int j = 0; j<inserters.length; j++) {
            long start = System.nanoTime();
            Thread[] threads = new Thread[numThreads];
            Thread[] deleteThreads = new Thread[bound];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(inserters[j], String.valueOf(i));
                threads[i].start();
            }
            for (int i = 0; i < deleteThreads.length; i++){
                deleteThreads[i] = new Thread(deleters[j], String.valueOf(i));
                deleteThreads[i].start();
            }
            for (Thread t : threads) {
                t.join();
            }
            for (Thread t : deleteThreads) {
                t.join();
            }
            long elapsed = System.nanoTime() - start;
            System.out.println("Time: " + elapsed);
        }
    }
}

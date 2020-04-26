import lock.bst.BST;
import lock.singlelock.SLBST;
import lockfree.bst.InsertInfo;
import org.junit.Test;
import util.TreeInterface;

import java.util.Random;

public class Tester {

    class ThreadBSTSingleAddRemove implements Runnable {

        private lock.bst.BST bst;

        ThreadBSTSingleAddRemove(lock.bst.BST bst) {
            this.bst = bst;
        }

        // implement run method for thread
        public void run() {
            Random r = new Random();
            int toAdd = r.nextInt(400);

            int i = (int)Thread.currentThread().getId();
            if (i % 2 == 0) {
                System.out.println("adding " + toAdd);
                if (bst.insert(toAdd)) System.out.println(i + ":    successfully added " + toAdd);
            }
            else {
                System.out.println("deleting " + toAdd);
                if (bst.delete(toAdd)) ;//System.out.println("successfully deleted " + toAdd);
            }
        }
    }

    class Inserter implements Runnable{
        private TreeInterface tree;
        private int inserts;

        Inserter(TreeInterface tree, int in) {
            this.tree = tree;
            inserts = in;
        }

        @Override
        public void run() {
            Random r = new Random();
            for (int i = 0; i < inserts; i++) {
                tree.insert(r.nextInt(25000));
            }
        }
    }

//    @Test
//    public void correctness() {
//        lock.bst.BST hohLock = new lock.bst.BST();
//        lockfree.bst.BST lFree =  new lockfree.bst.BST();
//        SLBST sLock = new SLBST();
//    }

    @Test
    public void timing_insert() throws InterruptedException {
        // set up
        lock.bst.BST lockBST = new lock.bst.BST();
        lockfree.bst.BST lockfreeBST = new lockfree.bst.BST();
        SLBST seqBST = new SLBST();

        // number of concurrent threads
        int numThreads = 5;
        // number of inserts each thread will perform
        int in_per_thread = 5;

        // create Runnables
        Inserter[] inserters = new Inserter[3];
        inserters[0] = new Inserter(lockBST, in_per_thread);
        inserters[1] = new Inserter(lockfreeBST, in_per_thread);
        inserters[2] = new Inserter(seqBST, in_per_thread);

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

//    @Test
//    public void timing_delete() {
//
//    }

}

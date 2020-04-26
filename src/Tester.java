import lock.bst.BST;
import lock.singlelock.SLBST;
import lockfree.bst.Internal;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Tester {

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
                System.out.println("adding " + toAdd);
                if (hohLock.insert(toAdd) && lFree.insert(toAdd) && sLock.insert(toAdd)) System.out.println(i + ":    successfully added " + toAdd + " to all trees");
            }
            else {
                System.out.println("deleting " + toAdd);
                if (hohLock.delete(toAdd) && lFree.delete(toAdd) && sLock.delete(toAdd)) System.out.println(i + ":    successfully deleted " + toAdd + " from all trees");
            }
        }
    }

    @Test
    public void correctness() throws InterruptedException {
        lock.bst.BST hohLock = new lock.bst.BST();
        lockfree.bst.BST lFree =  new lockfree.bst.BST();
        SLBST sLock = new SLBST();
        int numNodes = 500;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; i++) {
            threads[i] = new Thread(new ThreadBSTSingleAddRemove(hohLock, lFree, sLock));
            threads[i].start();
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

}

package lock.singlelock;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Random;

public class Tester {
    class ThreadBSTAdder implements Runnable {

        private SLBST bst;

        ThreadBSTAdder(SLBST bst) {
            this.bst = bst;
        }

        // implement run method for thread
        public void run() {
            Random r = new Random();
            for(int i = 0;i<2;i++){
                try {
                    int random = r.nextInt(500);
                    //System.out.println("trying to insert: " + random);
                    if(bst.insert(random)) {
                        //System.out.println("Successfully added " + random);
                    }
                    else
                        //System.out.println("Duplicate encountered,  "+random+" already exists");
                        Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            int i = (int)Thread.currentThread().getId();
//            if(bst.insert(i))
//                System.out.println("Successfully added "+i);
//            else
//                System.out.println("Duplicate encountered,  "+i+" already exists");
        }
    }

    class ThreadBSTSingleAddRemove implements Runnable {

        private SLBST bst;

        ThreadBSTSingleAddRemove(SLBST bst) {
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
//            if(bst.insert(i))
//                System.out.println("Successfully added "+i);
//            else
//                System.out.println("Duplicate encountered,  "+i+" already exists");
        }
    }

    class ThreadBSTRemover implements Runnable {
        private SLBST bst;

        public ThreadBSTRemover(SLBST bst) {
            this.bst = bst;
        }

        @Override
        public void run() {
            Random r = new Random();
            for(int i = 0;i<1;i++){
                try {
                    int random = r.nextInt(500);
                    //System.out.println("trying to delete: " + random);
                    if (bst.delete(random)){}
                    //System.out.println("Successfully removed " + random);
                    else
                        //System.out.println(random + " doesnt exist in the tree");
                        Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            int i = (int)Thread.currentThread().getId() / 2;
//            if (i % 2 == 0) {
//                bst.insert(i);
//            }
        }
    }

    class ThreadBSTSingleAddRemoveContains implements Runnable {

        private SLBST bst;

        ThreadBSTSingleAddRemoveContains(SLBST bst) {
            this.bst = bst;
        }

        // implement run method for thread
        public void run() {
            Random r = new Random();
            int key = r.nextInt(400);

            int i = (int)Thread.currentThread().getId();
            if (i % 3 == 0) {
                System.out.println(i + ":  adding " + key);
                if (bst.insert(key)) System.out.println(i + ":  successfully added " + key);
            }
            else if (i % 3 == 1) {
                if (bst.contains(key)) System.out.println(i + ":  tree contains " + key);
                else System.out.println(i + ":  tree doesnt contain " + key);
            }
            else {
                System.out.println(i + ":  deleting " + key);
                if (bst.delete(key)) System.out.println(i + ":  successfully deleted " + key);
            }
        }
    }

    // test for one direction monkey
    @Test
    public void testOneDirectionMonkey() throws InterruptedException {

        SLBST bst = new SLBST();
        int numNodes = 500;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; ++i) {
            // each thread tries to insert 5 elements to bst
            threads[i] = new Thread(new ThreadBSTAdder(bst));
            threads[i].start();
            threads[i].join();
        }
        for (int i = 0; i < numNodes; ++i) {
            // each thread tries to insert 5 elements to bst
            threads[i] = new Thread(new ThreadBSTRemover(bst));
            threads[i].start();
            threads[i].join();
        }

        bst.inorderTraversal();
    }

    @Test
    public void test1() throws InterruptedException {
        SLBST bst = new SLBST();
        int numNodes = 1000;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; ++i) {
            // each thread tries to insert 5 elements to bst
            threads[i] = new Thread(new ThreadBSTSingleAddRemove(bst));
            threads[i].start();
            threads[i].join();
        }
//        for (int i = 0; i < numNodes; ++i) {
//            // each thread tries to insert 5 elements to bst
//            threads[i] = new Thread(new ThreadBSTSingleAddRemove(bst, map));
//            threads[i].start();
//            threads[i].join();
//        }

        bst.inorderTraversal();
    }

    @Test
    public void containsTest() throws InterruptedException {
        SLBST bst = new SLBST();
        int numNodes = 1000;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; ++i) {
            threads[i] = new Thread(new ThreadBSTSingleAddRemoveContains(bst));
            threads[i].start();
            threads[i].join();
        }
        ArrayList<Integer> slbstOutput = bst.inorderTraversalTester();

        incTest(slbstOutput);
    }

    public void incTest(ArrayList<Integer> list) {
        for (int i = 1; i < list.size(); i++) {
            Assert.assertTrue(list.get(i-1) < list.get(i));
        }
    }
}

package lock.bst;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Tester {
    // monkey with different directions can be executed by threads
    class ThreadBSTAdder implements Runnable {

        private BST bst;
        private ConcurrentHashMap<Integer, Integer> map;

        ThreadBSTAdder(BST bst, ConcurrentHashMap<Integer, Integer> map) {
            this.bst = bst;
            this.map = map;
        }

        // implement run method for thread
        public void run() {
            Random r = new Random();
            for(int i = 0;i<2;i++){
                try {
                    int random = r.nextInt(500);
                    //System.out.println("trying to insert: " + random);
                    if(bst.insert(random)) {
                        map.put(random, 0);
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

        private BST bst;
        private ConcurrentHashMap<Integer, Integer> map;

        ThreadBSTSingleAddRemove(BST bst, ConcurrentHashMap<Integer, Integer> map) {
            this.bst = bst;
            this.map = map;
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
        private BST bst;
        private ConcurrentHashMap<Integer, Integer> map;

        public ThreadBSTRemover(BST bst, ConcurrentHashMap<Integer, Integer> map) {
            this.bst = bst;
            this.map = map;
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

        private BST bst;

        ThreadBSTSingleAddRemoveContains(BST bst) {
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

        BST bst = new BST();
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        int numNodes = 500;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; ++i) {
            // each thread tries to insert 5 elements to bst
            threads[i] = new Thread(new ThreadBSTAdder(bst, map));
            threads[i].start();
            threads[i].join();
        }
        for (int i = 0; i < numNodes; ++i) {
            // each thread tries to insert 5 elements to bst
            threads[i] = new Thread(new ThreadBSTRemover(bst, map));
            threads[i].start();
            threads[i].join();
        }

        bst.inorderTraversal();
    }

    @Test
    public void test1() throws InterruptedException {
        BST bst = new BST();
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        int numNodes = 1000;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; ++i) {
            // each thread tries to insert 5 elements to bst
            threads[i] = new Thread(new ThreadBSTSingleAddRemove(bst, map));
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
        BST bst = new BST();
        int numNodes = 1000;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; ++i) {
            threads[i] = new Thread(new ThreadBSTSingleAddRemoveContains(bst));
            threads[i].start();
            threads[i].join();
        }
        bst.inorderTraversal();
    }

}

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
            for(int i = 0;i<5;i++){
                try {
                    int random = r.nextInt(500);
                    System.out.println("trying to add: " + random);
                    if(bst.add(random)) {
                        map.put(random, 0);
                        System.out.println("Successfully added " + random);
                    }
                    else
                        System.out.println("Duplicate encountered,  "+random+" already exists");
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            int i = (int)Thread.currentThread().getId();
//            if(bst.add(i))
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
                if (bst.add(toAdd)) System.out.println(i + ":    successfully added " + toAdd);
            }
            else {
                System.out.println("deleting " + toAdd);
                if (bst.delete(toAdd)) ;//System.out.println("successfully deleted " + toAdd);
            }
//            if(bst.add(i))
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
            for(int i = 0;i<2;i++){
                try {
                    int random = r.nextInt(500);
                    System.out.println("trying to delete: " + random);
                    if (bst.delete(random)){}
                        //System.out.println("Successfully removed " + random);
                    else
                        System.out.println(random + " doesnt exist in the tree");
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            int i = (int)Thread.currentThread().getId() / 2;
//            if (i % 2 == 0) {
//                bst.add(i);
//            }
        }
    }

    // test for one direction monkey
    @Test
    public void testOneDirectionMonkey() throws InterruptedException {

        BST bst = new BST();
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        int numNodes = 1000;
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; ++i) {
            // each thread tries to add 5 elements to bst
            threads[i] = new Thread(new ThreadBSTAdder(bst, map));
            threads[i].start();
            threads[i].join();
        }
        for (int i = 0; i < numNodes; ++i) {
            // each thread tries to add 5 elements to bst
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
            // each thread tries to add 5 elements to bst
            threads[i] = new Thread(new ThreadBSTSingleAddRemove(bst, map));
            threads[i].start();
            threads[i].join();
        }
//        for (int i = 0; i < numNodes; ++i) {
//            // each thread tries to add 5 elements to bst
//            threads[i] = new Thread(new ThreadBSTSingleAddRemove(bst, map));
//            threads[i].start();
//            threads[i].join();
//        }

        bst.inorderTraversal();
    }

}

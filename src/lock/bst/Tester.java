package lock;

import org.junit.Test;

import java.util.Random;

public class Tester {
    // monkey with different directions can be executed by threads
    class ThreadBST implements Runnable {

        private BST bst;


        ThreadBST(BST bst) {
            this.bst = bst;
        }

        // implement run method for thread
        public void run() {
            Random r = new Random();
            for(int i = 0;i<5;i++){
                try {
                    int random = r.nextInt(100);
                    System.out.println("trying to add: " + random);
                    if(bst.add(random))
                        System.out.println("Successfully added "+random);
                    else
                        System.out.println("Duplicate encountered,  "+random+" already exists");
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // test for one direction monkey
    @Test
    public void testOneDirectionMonkey() throws InterruptedException {

        BST bst = new BST();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; ++i) {
            // each thread tries to add 5 elements to bst
            threads[i] = new Thread(new ThreadBST(bst));
            threads[i].start();
            threads[i].join();
        }
        bst.inorderTraversal();
    }

}

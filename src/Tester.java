import lock.bst.BST;
import lock.singlelock.SLBST;
import org.junit.Test;

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

    @Test
    public void correctness() {
        lock.bst.BST hohLock = new lock.bst.BST();
        lockfree.bst.BST lFree =  new lockfree.bst.BST();
        SLBST sLock = new SLBST();
    }
}

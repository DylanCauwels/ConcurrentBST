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
}

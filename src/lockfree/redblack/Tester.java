package lockfree.redblack;

import lockfree.bst.BST;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Tester {

    @Test
    public void test() {
        assertEquals(true, true);
    }

    @Test
    public void sequentialInsertAll() {
        RedBlack tree = new RedBlack();
        int numNodes = 10;
        ArrayList<Integer> missedValues = new ArrayList<>();

        for (int i = numNodes; i > 0; i--) {
            tree.insert(i);
        }
        for (int i = numNodes; i > 0; i--) {
            if (!tree.contains(i))
                missedValues.add(i);
        }

        assertProperties(tree);
        assertTrue(missedValues.isEmpty());
    }

    /**
     * PROPERTIES
     * every node is either red or black
     * the root node is black
     * external nodes are black
     * a red nodes children are both black
     * all paths from a node to its leaf descendants contain the same number of black nodes
     *
     * left child is less than parent
     * right child is greater than or equal to parent
     */
    private void assertProperties(RedBlack tree) {
        ArrayList<BFSObject> q = new ArrayList<>();
        q.add(new BFSObject(tree.getRoot(), 0));
        int globalBlacks = 0;

        Node sentinel = tree.getRoot();
        // assert root is left child of nil parent
        assertEquals(sentinel, sentinel.parent.left);
        // assert parent of root is nil
        assertTrue(sentinel.parent.nil);
        // assert root is black
        assertEquals(tree.getRoot().color, Node.Color.BLACK);
        // do BFS on tree to assert properties
        while (!q.isEmpty()) {
            BFSObject vals = q.remove(0);
            // ensure the node has a color
            assertTrue(vals.node.color != null);
            // check if the node is a nil node
            if (vals.node.nil) {
                // compare number of blacks to globalBlacks
                if (globalBlacks == 0) {
                    globalBlacks = vals.numBlacks;
                } else {
                    assertEquals(vals.numBlacks, globalBlacks);
                }
                // assert external node is black
                assertEquals(vals.node.color, Node.Color.BLACK);
            } else {
                // ensure the node is the correct child
                if (vals.node == vals.node.parent.left && !vals.node.parent.nil) {
                    assertTrue(vals.node.key < vals.node.parent.key);
                } else {
                    assertTrue(vals.node.key >= vals.node.parent.key);
                }
                // add next nodes into the tree
                if (!vals.node.left.nil) q.add(new BFSObject(vals.node.left, (vals.node.color == Node.Color.BLACK) ? vals.numBlacks + 1 : vals.numBlacks));
                if (!vals.node.right.nil) q.add(new BFSObject(vals.node.left, (vals.node.color == Node.Color.BLACK) ? vals.numBlacks + 1 : vals.numBlacks));
            }
        }
    }

    class BFSObject {
        public Node node;
        public int numBlacks = 0;

        public BFSObject(Node x, int total) {
            node = x;
            numBlacks = total;
        }
    }
}

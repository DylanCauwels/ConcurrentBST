package lock.bst;

//import lock.*;

public class Main {
    public static void main(String[] args) {
//        BST t = new BST(5);
//        t.add(26);t.add(2);t.add(12);t.add(27);t.add(35);t.add(30);t.add(31);
//        t.add(6);t.add(15);t.add(18);
//        t.inorderTraversal();
//
//        t.delete(12);t.delete(30);
//        t.inorderTraversal();

        BST s = new BST(8);
        s.add(2);s.add(7);s.add(6);s.add(5);s.add(4);
        s.inorderTraversal();

        s.delete(2);
        s.inorderTraversal();
    }
}

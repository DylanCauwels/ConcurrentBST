package lock.redblack;


public class Main {
    public static void main(String[] args) {
//        BST t = new BST(5);
//        t.insert(26);t.insert(2);t.insert(12);t.insert(27);t.insert(35);t.insert(30);t.insert(31);
//        t.insert(6);t.insert(15);t.insert(18);
//        t.inorderTraversal();
//
//        t.delete(5);
//        t.inorderTraversal();
//        t.delete(18);
//        t.inorderTraversal();

        RBT s = new RBT(8);
        s.insert(2);s.insert(7);s.insert(6);s.insert(5);s.insert(4);
        s.inorderTraversal();
        s.delete(6);
        s.inorderTraversal();
        s.delete(8);
//        s.insert(10);
//        int x = 6;
//        System.out.println("Tree contains value " + x + "? " + s.contains(x));
        s.inorderTraversal();
    }
}

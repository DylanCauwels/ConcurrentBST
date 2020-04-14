public class Main {
    public static void main(String[] args) {
        BST t = new BST(5);
        t.add(26);t.add(2);t.add(12);t.add(27);t.add(35);t.add(30);t.add(31);
        t.add(6);t.add(15);t.add(18);
        t.inorderTraversal();

        t.delete(12);t.delete(30);
        t.inorderTraversal();
    }
}

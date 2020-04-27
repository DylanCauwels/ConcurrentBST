package lock.singlelock;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SLBST implements util.TreeInterface {
    Lock treelock;
    Node root;

    class Node{
        //ReentrantLock lock;
        Node left;
        Node right;
        int val;
        Node parent;

        public Node (int _val){
            val = _val;
            //lock = new ReentrantLock();
            left = null;
            right = null;
            parent = null;
        }
    }

    public SLBST(int rootKey) {
        this.root = new Node(rootKey);
        this.treelock = new ReentrantLock();
    }

    public SLBST() {
        this.root = null;
        this.treelock = new ReentrantLock();
    }

    public boolean insert(int key) {
        treelock.lock();
        if(this.root == null){
            this.root = new Node(key);
            treelock.unlock();
            return true;
        }
        else{
            Node n = this.root;
            return insertHelper(key, n);
        }
    }

    private boolean insertHelper(int key, Node parent){
        if(parent.val == key){
            treelock.unlock();
            return false; //can't insert, key already exists
        }
        else if( key< parent.val){
            //empty so can insert node
            if(parent.left == null){
                parent.left = new Node(key);
                parent.left.parent = parent;
                treelock.unlock();
                return true; //success
            }
            else{ //else keep traversing
                Node left = parent.left;
                return insertHelper(key, left);
            }
        }
        else{
            if(parent.right == null){
                parent.right = new Node(key);
                parent.right.parent = parent;
                treelock.unlock();
                return true; //success
            }
            else{
                Node right = parent.right;
                return insertHelper(key, right);
            }
        }
    }

    public boolean delete(int key) {
        treelock.lock();
        Node head = root;
        if (head == null) {
            treelock.unlock();
            return false;
        }
        Node toDelete = null;
        if (head.val == key) {
            toDelete = head;
        }
        else {
            toDelete = find(head, key);
        }

        if (toDelete == null) {
            // all nodes have been unlocked
            //System.out.println("No node of value: " +key);
            treelock.unlock();
            return false; //no node of that value in tree
        }
        // toDelete and toDelete.parent locked at this point
        // now need to find node to replace toDelete

        Node successor = null;
        boolean flag = true;
        int i = getNumChild(toDelete);
        switch (i) {
            case 0:
                successor = null;
                break;
            case 1:
                //parent of successor is the node to be deleted
                successor = (toDelete.left == null) ? toDelete.right:toDelete.left;
                break;
            case 2:
                // successor and parent are locked
                successor = findSuccessor(toDelete.right);
                flag = (successor == toDelete.right);
                break;
            default:
                System.out.println("shouldnt be here");
                break;

        }
        //locks maintained for successor == null -> toDelete, toDelete.parent (if toDelete != root)
        //toDelete is successor leaf, if its root set to null else remove reference from parent
        if(i == 0){
            if (toDelete == head) {
                root = null;
            }
            else {
                if (toDelete.parent.left == toDelete) {
                    toDelete.parent.left = null;
                }
                else {
                    toDelete.parent.right = null;
                }
                assert toDelete.parent.left != toDelete && toDelete.parent.right != toDelete;
            }
            treelock.unlock();
            return true;
        }
        //locks maintained for case 1: toDelete, toDelete.parent, successor
        //successor takes place of toDelete
        else if (i == 1) {
            if (toDelete == head) {
                root = successor;
                successor.parent = null;
            }
            else {
                if (toDelete.parent.left == toDelete) {
                    toDelete.parent.left = successor;
                    successor.parent = toDelete.parent;
                }
                else {
                    toDelete.parent.right = successor;
                    successor.parent = toDelete.parent;
                }
                assert toDelete.parent.left != toDelete && toDelete.parent.right != toDelete;
            }
            treelock.unlock();
            return true;
        }
        //locks maintained for case 2: toDelete, toDelete.parent (if toDelete not root), successor, successor.parent
        //remove reference to successor
        else {
            swap(toDelete,successor);
            if (successor.parent.left == successor) {
                successor.parent.left = successor.right;

            }
            else {
                successor.parent.right = successor.right;
            }
            if (successor.right != null) {
                successor.right.parent = successor.parent;
            }
            treelock.unlock();
            return true;
        }

    }

    private Node findSuccessor(Node parent) {
        Node right = parent;
        while (right.left != null) {
            right = right.left;
        }
        return right;
    }

    private int getNumChild(Node parent) {
        if(parent.right == null && parent.left == null)
            return 0;
        else if(parent.right != null && parent.left != null) {
            return 2;
        }
        return 1;
    }

    //takes 2 nodes and simples swaps their Vals, nothing else
    private void swap(Node a, Node b){
        int temp = a.val;
        a.val = b.val;
        b.val = temp;
    }

    public boolean contains(int key){
        treelock.lock();
        if (root == null) {
            treelock.unlock();
            return false;
        }
        return containsHelper(this.root,key);
    }

    private Node find(Node parent, int val) {
        boolean leftNull = parent.left == null;
        boolean rightNull = parent.right == null;
        if (val < parent.val) {
            if (!leftNull) {
                if (parent.left.val == val) {
                    return parent.left;
                }
                else {
                    return find(parent.left, val);
                }
            }
            else {
                return null;
            }
        }
        else {
            if (!rightNull) {
                if (parent.right.val == val) {
                    return parent.right;
                }
                else {
                    return find(parent.right, val);
                }
            }
            else {
                return null;
            }
        }
    }

    private boolean containsHelper(Node parent, int val){
        //if node found return it
        if (parent.val == val){
            treelock.unlock();
            return true;
        }
        else{
            //if not found either go left or go right, IF Node exists
            if(val<parent.val){
                if(parent.left != null){
                    return containsHelper(parent.left, val);
                }
                else{
                    //node doesn't exist return null
                    treelock.unlock();
                    return false;
                }
            }
            else{
                if(parent.right != null){
                    return containsHelper(parent.right, val);
                }
                else {
                    treelock.unlock();
                    return false;
                }
            }
        }
    }

    //prints inorder traversal
    public void inorderTraversal (){
        System.out.println("\n");
        if(root != null){
            iot(root.left);
            System.out.print(root.val+ " ");
            iot(root.right);
        }
        System.out.println("\n");
    }

    private void iot(Node head){
        if(head != null){
            iot(head.left);
            System.out.print(head.val+ " ");
            iot(head.right);
        }
    }

    public ArrayList<Integer> inorderTraversalTester (){
        ArrayList<Integer> output = new ArrayList<>();
        if(root != null){
            iotTester(root.left, output);
            output.add(root.val);
            iotTester(root.right, output);
        }
        return output;
    }

    private void iotTester(Node head, ArrayList<Integer> output){
        if(head != null){
            iotTester(head.left, output);
            output.add(head.val);
            iotTester(head.right, output);
        }
    }
}

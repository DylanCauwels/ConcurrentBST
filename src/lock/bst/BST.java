package lock.bst;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

// This lock.BST assumes NO DUPLICATE VALUES, if there are need to edit class to
// insert count variable, better for AVL trees, and edit functions
public class BST implements util.TreeInterface {

    private ReentrantLock treelock = new ReentrantLock();
    private Node root;

    /**
     * Node class for tree
     * expects a value for constructor
     * class variables:
     * left, right : references for left, right children
     * lock: lock for concurrency, intially unlocked
     * parent: reference for parent
     */
    class Node{
        ReentrantLock lock;
        Node left;
        Node right;
        int val;
        Node parent;

        Node(int _val){
            val = _val;
            lock = new ReentrantLock();
            left = null;
            right = null;
            parent = null;
        }
    }

    /**
     * null constructor, constructs a BST with no root
     */
    public BST(){
        this.root = null;
    }

    /**
     * Function to insert value into tree
     * @param key : integer value to be inserted
     * @return :true - if insert is successful (no duplicates)
     *          false- if unsuccessful
     */
    public boolean insert(int key){
        treelock.lock();                  //lock tree first to check if tree is null
        if(this.root == null){            //yes? then add root, unlock and return
            this.root = new Node(key);
            treelock.unlock();
            return true;
        }
        else{                             //else, unlock tree and lock root node to search where to add
            Node n = this.root;
            n.lock.lock();
            treelock.unlock();
            return insertHelper(key, n);
        }
    }

    /**
     * Helper function to insert into BST
     * @param key: integer value to be added
     * @param parent: Current node traversing
     * @return : true- insert is successful
     */
    private boolean insertHelper(int key, Node parent){
        if(parent.val == key){
            parent.lock.unlock();
            return false;                               //can't insert, key already exists
        }
        else if( key< parent.val){                      //if key<parent.val, go left
            if(parent.left == null){ //child null, so can insert node
                parent.left = new Node(key);
                parent.left.parent = parent;
                parent.lock.unlock();
                return true; //success
            }
            else{ //else keep traversing
                Node left = parent.left;
                left.lock.lock();             //hand-over-hand locking from parent to child node for traversal
                parent.lock.unlock();
                return insertHelper(key, left);
            }
        }
        else{                                           //else, go right
            if(parent.right == null){
                parent.right = new Node(key);
                parent.right.parent = parent;
                parent.lock.unlock();
                return true; //success
            }
            else{
                Node right = parent.right;
                right.lock.lock();
                parent.lock.unlock();
                return insertHelper(key, right);
            }
        }
    }

    /**
     * Delete function to remove node from BST
     * @param key : integer value to be added to BST
     * @return : true, if success (node exists and is removed)
     */
    public boolean delete(int key) {
        treelock.lock();
        Node head = root;
        if (head == null) {
            treelock.unlock();
            return false;
        }
        head.lock.lock();
        treelock.unlock();
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
                successor.lock.lock();
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
                toDelete.lock.unlock();
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
                toDelete.parent.lock.unlock();
                toDelete.lock.unlock();
            }
            return true;
        }
        //locks maintained for case 1: toDelete, toDelete.parent, successor
        //successor takes place of toDelete
        else if (i == 1) {
            if (toDelete == head) {
                root = successor;
                successor.parent = null;
                toDelete.lock.unlock();
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
                toDelete.parent.lock.unlock();
                toDelete.lock.unlock();
                 //TODO check why illegal monitor exception here
            }
            successor.lock.unlock();
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
                successor.right.lock.lock();
                successor.right.parent = successor.parent;
                successor.right.lock.unlock();
            }
            if (toDelete != head) {
                toDelete.parent.lock.unlock();

            }
            toDelete.lock.unlock();
            successor.lock.unlock();
            if (!flag) { //successor.parent is same as toDelete
                successor.parent.lock.unlock();
            }
            return true;
        }

    }

    /**
     * Function to find either inorder Sucessor or predecessor
     * @param parent : Node which you want to find successor or predecessor for
     * @return Node: returns Node reference of successor
     */
    private Node findSuccessor(Node parent) {
        Node right = parent;
        right.lock.lock();
        while (right.left != null) {
            //if left not null lock to access
            right.left.lock.lock();
            right = right.left;
            //traverse and unlock parent
            if (right.left != null) {
                right.parent.lock.unlock();
            }
        }
        return right;
    }

    /**
     * function to count number of children for a node
     * @param parent : node that you want to find number of children for
     * @return int value of children
     */
    private int getNumChild(Node parent) {
        if(parent.right == null && parent.left == null)
            return 0;
        else if(parent.right != null && parent.left != null) {
            return 2;
        }
        return 1;
    }

    //takes 2 nodes and simples swaps their Vals, nothing else

    /**
     * Function that takes 2 nodes and simply swaps their values, nothing else
     * @param a : node 1
     * @param b : node 2
     */
    private void swap(Node a, Node b){
        int temp = a.val;
        a.val = b.val;
        b.val = temp;
    }

    /**
     * Same as insert()
     * @param key
     * @return
     */
    public boolean contains(int key){
        treelock.lock();
        if (root == null) {
            treelock.unlock();
            return false;
        }
        this.root.lock.lock();
        treelock.unlock();
        return containsHelper(this.root,key);
    }

    /**
     * function to find if a Node with desired value exists in tree
     * @param parent : current node in traversal
     * @param val : desired value to find
     * @return Node: returns Node reference if it exists, or null if not
     */
    private Node find(Node parent, int val) {
        //parent already locked and value checked
        boolean leftNull = parent.left == null;
        boolean rightNull = parent.right == null;
        if (val < parent.val) {
            if (!leftNull) {
                if (parent.left.val == val) {
                    parent.left.lock.lock();
                    return parent.left;
                }
                else {
                    parent.left.lock.lock();
                    parent.lock.unlock();
                    return find(parent.left, val);
                }
            }
            else {
                parent.lock.unlock();
                return null;
            }
        }
        else {
            if (!rightNull) {
                if (parent.right.val == val) {
                    parent.right.lock.lock();
                    return parent.right;
                }
                else {
                    parent.right.lock.lock();
                    parent.lock.unlock();
                    return find(parent.right, val);
                }
            }
            else {
                parent.lock.unlock();
                return null;
            }
        }
    }

    /**
     * Same as insert helper()
     * @param parent
     * @param val
     * @return
     */
    private boolean containsHelper(Node parent, int val){
        //if node found return it
        if (parent.val == val){
            parent.lock.unlock();
            return true;
        }
        else{
            //if not found either go left or go right, IF Node exists
            if(val<parent.val){
                if(parent.left != null){
                    parent.left.lock.lock();
                    parent.lock.unlock();
                    return containsHelper(parent.left, val);
                }
                else{
                    //node doesn't exist return null
                    parent.lock.unlock();
                    return false;
                }
            }
            else{
                if(parent.right != null){
                    parent.right.lock.lock();
                    parent.lock.unlock();
                    return containsHelper(parent.right, val);
                }
                else {
                    parent.lock.unlock();
                    return false;
                }
            }
        }
    }

    /**
     * function to print in-order traversal of tree
     */
    public void inorderTraversal (){
        System.out.println("\n");
        if(root != null){
            iot(root.left);
            System.out.print(root.val+ " ");
            iot(root.right);
        }
        System.out.println("\n");
    }

    /**
     * Helper function for inorderTraversal()
     * @param head
     */
    private void iot(Node head){
        if(head != null){
            iot(head.left);
            System.out.print(head.val+ " ");
            iot(head.right);
        }
    }

    /**
     * Same as inOrderTraversal()
     * @return
     */
    public ArrayList<Integer> inorderTraversalTester (){
        ArrayList<Integer> output = new ArrayList<>();
        if(root != null){
            iotTester(root.left, output);
            output.add(root.val);
            iotTester(root.right, output);
        }
        return output;
    }

    /**
     * Same as iot()
     * @return
     */
    private void iotTester(Node head, ArrayList<Integer> output){
        if(head != null){
            iotTester(head.left, output);
            output.add(head.val);
            iotTester(head.right, output);
        }
    }
}

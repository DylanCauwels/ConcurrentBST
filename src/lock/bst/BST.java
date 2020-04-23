package lock.bst;

import java.util.concurrent.locks.ReentrantLock;

// This lock.BST assumes NO DUPLICATE VALUES, if there are need to edit class to
// insert count variable, better for AVL trees, and edit functions
public class BST {

    ReentrantLock treelock = new ReentrantLock();
    Node root;

    class Node{
        ReentrantLock lock;
        Node left;
        Node right;
        int val;
        Node parent;

        public Node (int _val){
            val = _val;
            lock = new ReentrantLock();
            left = null;
            right = null;
            parent = null;
        }
    }

    public BST(int rootval){
        this.root = new Node(rootval);
    }

    public BST(){
        this.root = null;
    }

    public boolean insert(int key){
        treelock.lock();
        if(this.root == null){
            this.root = new Node(key);
            treelock.unlock();
            return true;
        }
        else{
            Node n = this.root;
            n.lock.lock();
            treelock.unlock();
            return insertHelper(key, n);
        }
    }

    private boolean insertHelper(int key, Node parent){
        if(parent.val == key){
            parent.lock.unlock();
            return false; //can't insert, key already exists
        }
        else if( key< parent.val){
            //empty so can insert node
            if(parent.left == null){
                parent.left = new Node(key);
                parent.left.parent = parent;
                parent.lock.unlock();
                return true; //success
            }
            else{ //else keep traversing
                Node left = parent.left;
                left.lock.lock();
                parent.lock.unlock();
                return insertHelper(key, left);
            }
        }
        else{
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
        this.root.lock.lock();
        return containsHelper(this.root,key);
    }

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


}

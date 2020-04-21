package lock.bst;

import java.util.concurrent.locks.ReentrantLock;

// This lock.BST assumes NO DUPLICATE VALUES, if there are need to edit class to
// add count variable, better for AVL trees, and edit functions
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
        Node n = null;
    }

    public boolean add(int val){
        treelock.lock();
        if(this.root == null){
            this.root = new Node(val);
            treelock.unlock();
            return true;
        }
        else{
            Node n = this.root;
            n.lock.lock();
            treelock.unlock();
            return addhelper(val, n);
        }
    }

    private boolean addhelper(int val, Node parent){
        if(parent.val == val){
            parent.lock.unlock();
            return false; //can't add, val already exists
        }
        else if( val< parent.val){
            //empty so can add node
            if(parent.left == null){
                parent.left = new Node(val);
                parent.left.parent = parent;
                parent.lock.unlock();
                return true; //success
            }
            else{ //else keep traversing
                Node left = parent.left;
                left.lock.lock();
                parent.lock.unlock();
                return addhelper(val, left);
            }
        }
        else{
            if(parent.right == null){
                parent.right = new Node(val);
                parent.right.parent = parent;
                parent.lock.unlock();
                return true; //success
            }
            else{
                Node right = parent.right;
                right.lock.lock();
                parent.lock.unlock();
                return addhelper(val, right);
            }
        }
    }

    public boolean delete(int val){
        // find node, r and its parent are locked when returned
        Node r = find(val);
        if(r== null){
            System.out.println("No node of value: " +val);
            return false; //no node of that value in tree
        }

        //find node to replace with, r still locked


        Node a = null;
        boolean flag = true;
        int i = getNumChild(r);
        System.out.println("\n\ncase: " + i);
        switch (i) {
            case 0:
                a = null;
                break;
            case 1:
                //parent of a is the node to be deleted
                a = (r.left == null) ? r.right:r.left;
                a.lock.lock();
                break;
            case 2:
                // a and parent are locked
                a = fSucc(r.right);
                flag = (a == r.right);
                break;
            default:
                System.out.println("shouldnt be here");
                break;

        }

        //if no successor ('root' is a leaf) just delete
        if(a == null){
            if (r == root) {
                r.lock.unlock();
                root = null;
            }
            else {
                if (r.parent.left == r) {
                    r.parent.left = null;
                }
                else {
                    r.parent.right = null;
                }
                r.parent.lock.unlock();
            }

            return true;
        }

        System.out.println("\n"+ a.val +" " +r.val);

        if (i == 1) {
            if (r == root) {
                root = a;
            }
            else {
                if (r.parent.left == r) {
                    r.parent.left = a;
                    a.parent = r.parent;
                }
                else {
                    r.parent.right = a;
                    a.parent = r.parent;
                }
                r.parent.lock.unlock();
            }
            a.lock.unlock();
            return true;
        }
        else {
            swap(r,a);
            if (a.parent.left == a) {
                a.parent.left = a.left;
            }
            else {
                a.parent.right = a.right;
            }
            a.parent.lock.unlock();
            r.lock.unlock();
            return true;
        }


        // if successor swap values and
//        swap(r,a);
//        System.out.println("a: "+a.val +" r: " +r.val);
//        inorderTraversal();
//
//        //now need to delete whats in node a, guaranteed to be leaf or single child
//        if (i == 2) {
//
//        }
//        deleteleaf(a);
//        r.lock.unlock();
//        if (!flag) {
//            a.parent.lock.unlock();
//        }
//
//        return true; //success
    }

    //delete's a leaf node or a node with only 1 child
    //assume always either leaf or right node with child
    private void deleteleaf(Node a){
        a.parent.lock.lock(); //should always have parent, minus root
        Node par = a.parent;
        //check if right child
        if(par.right.val == a.val){
            //check if it has children
            //no children? set parent left to null
            if(a.right ==null & a.left ==null){
                par.right = null;
                par.lock.unlock();
                return;
            }
            //if right child, has children no it'll be a left subtree
            par.right = a.left;
            par.lock.unlock();
            return;
        }
        //else its left child
        else{
            if(a.left ==null & a.right ==null){
                par.left = null;
                par.lock.unlock();
                return;
            }
            //if right child, has children no it'll be a left subtree
            par.left = a.right;
            par.lock.unlock();
            return;
        }
    }

    private Node fSucc(Node parent) {
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

    //Find method to get inorder predecessor or successor
    //Root is locked when called
    private Node findSuccessor(Node parent){
        if(parent.right == null && parent.left == null)
            return null;
        //2 child successor
        else if(parent.right != null && parent.left != null){
            Node right = parent.right;
            //lock right node
            right.lock.lock();
            while(right.left != null){
                //if left not null lock to access
                right.left.lock.lock();
                right = right.left;
                //traverse and unlock parent
                right.parent.lock.unlock();
            }
            //right is still locked
            return right;
        }
        // 1 child successor
        else{
            if(parent.left != null){
                parent.left.lock.lock();
                return parent.left;
            }
            else if(parent.right != null){
                parent.right.lock.lock();
                return parent.right;
            }
        }
        System.out.println("It should never get here");
        return null;
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
    private void swap (Node a, Node b){
        int temp = a.val;
        a.val = b.val;
        b.val = temp;
    }


    public Node find(int val){
        this.root.lock.lock();
        return findHelper(root,val);
    }

    private Node findHelper(Node parent, int val){
        //if node found return it
        if (parent.val == val){
            if (parent.parent != null) {
                parent.parent.lock.lock(); //TODO: Might have to change and find another way to relock the parent
            }
            return parent;
        }
        else{
            //if not found either go left or go right, IF Node exists
            if(val<parent.val){
                if(parent.left != null){
                    parent.left.lock.lock();
                    parent.lock.unlock();
                    return findHelper(parent.left, val);
                }
                else{
                    //node doesn't exist return null
                    parent.lock.unlock();
                    return null;
                }
            }
            else{
                if(parent.right != null){
                    parent.right.lock.lock();
                    parent.lock.unlock();
                    return findHelper(parent.right, val);
                }
                else {
                    parent.lock.unlock();
                    return null;
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
    }

    private void iot(Node head){
        if(head != null){
            iot(head.left);
            System.out.print(head.val+ " ");
            iot(head.right);
        }
    }


}

import javax.swing.*;
import java.util.concurrent.locks.ReentrantLock;

// This BST assumes NO DUPLICATE VALUES, if there are need to edit class to
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
        Node n = new Node(rootval);
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
        // find node
        Node r = find(val);
        if(r== null){
            System.out.println("No node of value: " +val);
            return false; //no node of that value in tree
        }
        //find node to replace with
        Node a = findSuccessor(r);

        //if no successor ('root' is a leaf) just delete
        if(a == null){
            deleteleaf(r);
        }

        // if successor swap values and
        swap(r,a);

        // delete the leaf node
        deleteleaf(a);
        return true; //success
    }

    //delete's a leaf node
    private void deleteleaf(Node a){

        Node par = a.parent;

        if(par.right.val==a.val){
            if(a.right != null){
                par.right = a.left;
            }
            else{
                par.right = null;  //delete node
            }
        }
        else{
            if(a.left != null){
                par.left = a.left;
            }
            else{
                par.left = null; //delete node
            }
        }
    }

    //Find method to get inorder predecessor or successor
    private Node findSuccessor(Node root){
        if(root.right != null){
            if(root.right.left != null)
                return root.right.left;
            else
                return root.right;
        }
        else if(root.left != null){
            if(root.left.right != null)
                return root.left.right;
            else
                return root.left;
        }
        else{
            return null; // no successor
        }
    }

    //takes 2 nodes and simples swaps their Vals, nothing else
    private void swap (Node a, Node b){
        int temp = a.val;
        a.val = b.val;
        b.val = temp;
    }


    public Node find(int val){
        return findHelper(root,val);
    }

    private Node findHelper(Node root, int val){
        if (root.val == val){
            return root;
        }
        else{
            if(val<root.val){
                if(root.left != null)
                    return findHelper(root.left, val);
                else
                    return null;
            }
            else{
                if(root.right != null)
                    return findHelper(root.right, val);
                else
                    return null;
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

    //    private void deletehelper(int val, Node root){
//        // find node
//        find(val);
//        //find node to replace with
//        Node a = findSuccessor(root);
//        //if no successor ('root' is a leaf) just delete
//        if(a == null){
//            deleteleaf(root);
//        }
//        // if successor swap values and
//        swap(root,a);
//        // delete the leaf node
//        deleteleaf(a);
//    }

}

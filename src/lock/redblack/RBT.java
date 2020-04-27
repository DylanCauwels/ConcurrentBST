package lock.redblack;

import lock.bst.BST;

import java.util.concurrent.locks.ReentrantLock;

public class RBT extends BST {
    ReentrantLock treelock = new ReentrantLock();
    Node root;
    Node nil = new Node();

    public enum Color {
        red,
        black
    }


    class Node{
        ReentrantLock lock;
        Node left;
        Node right;
        int val;
        Node parent;
        Boolean child; //True == left child, False == right child;
        Color color; // true = red, false = black

        public Node (int _val){
            val = _val;
            lock = new ReentrantLock();
            left = null;
            right = null;
            parent = nil;
            color= Color.red;
            child = null;
        }

        public Node() {
            lock = new ReentrantLock();
            left = null;
            right = null;
            parent = null;
            color= Color.red;
            child = null;
        }
    }


    public RBT(int rootval){
        this.root = new Node(rootval);
    }

    public RBT(){
        this.root = null;
    }

    @Override
    public boolean insert(int key) {
        treelock.lock();
        if(root == null){
            root = new Node(key);
            root.color = Color.black; //make root black, no need to recolor
            treelock.unlock();
            return true; // insert one red node
        }
        //add new Node
        Node n = this.root;
        n.lock.lock();
        treelock.unlock();
        Node nu = new Node(key);
        //at end of insertHelper: nu, nu.parent, nu.gp, and nu.uncle need to be locked
        //so that recolor can work
        if(insertHelper(nu, n)){
            recolor(nu);
        }
        return false;
    }

    //Old signature had
    private boolean insertHelper(Node key, Node parent){
        if(parent.val == key.val){
            parent.lock.unlock();
            return false; //can't insert, key already exists
        }
        else if( key.val< parent.val){
            //empty so can insert node
            if(parent.left == null){
                parent.left = key;
                key.child = true; //left child
                parent.left.parent = parent; //TODO : isnt this the same as key.parent?
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
                key.child = false; //right child
                parent.right = key;
                parent.right.parent = parent; //TODO : isnt this the same as key.parent?
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

    private boolean recolor(Node child){
        Node c = child;
        Node parent;
        Node gp;
        Node unc;
        while(c!=this.root && c.parent.color == Color.red){ //true == red
            parent = c.parent;
            gp = parent.parent;

            if(parent == gp.left){ //parent is left child of grandparent
                unc = gp.right;
                if(unc != null &&  unc.color == Color.red){ //uncle is also red case 1
                    parent.color = Color.black; //then color uncle, parent black
                    unc.color = Color.black;
                    gp.color = Color.red; //color grandparent red
                    c = gp;
                }
                else if (c == parent.right){
                    c = parent;
                    leftRotate(c);
                }
                parent.color = Color.black;
                gp.color = Color.red;
                rightRotate(gp);
            }
            else{                  //parent is right child of grandparent
                unc = gp.left;
                if(unc != null &&  unc.color == Color.red){ //uncle is also red case 1
                    parent.color = Color.black; //then color uncle, parent black
                    unc.color = Color.black;
                    gp.color = Color.red; //color grandparent red
                    c = gp;
                }
                else if (c == parent.left){
                    c = parent;
                    rightRotate(c);
                }
                parent.color = Color.black;
                gp.color = Color.red;
                leftRotate(gp);
            }
        }
        root.color = Color.black;
        return true;
    }

    private void leftRotate(Node n) {
        Node y = n.right;
        n.right = y.left;
        if(y.left != null){ //if y had a left child
            y.left.parent = n; //it is now right child of
        }
        y.parent= n.parent;
        if(n.parent == null){ //if n was root
            this.root = y; //make y root now
        }
        else if(n == n.parent.left){ //if n was left child
            n.parent.left = y;
        }
        else{
            n.parent.right= y;
        }
        y.left = n;
        n.parent = y;
    }

    private void rightRotate(Node n) {
        Node y = n.left;
        n.left = y.right;
        if(y.right != null){ //if y had a left child
            y.right.parent = n; //it is now right child of
        }
        y.parent= n.parent;
        if(n.parent == null){ //if n was root
            this.root = y; //make y root now
        }
        else if(n == n.parent.right){ //if n was left child
            n.parent.right = y;
        }
        else{
            n.parent.left= y;
        }
        y.right = n;
        n.parent = y;
    }

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

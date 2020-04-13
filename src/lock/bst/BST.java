// This BST assumes NO DUPLICATE VALUES, if there are need to edit class to
// add count variable, better for AVL trees, and edit functions
public class BST {

    Node root;

    class Node{
        Node left;
        Node right;
        int val;
        Node parent;

        public Node (int _val){
            val = _val;
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

    public void add(int val){
        if(this.root == null){
            this.root = new Node(val);
        }
        else{
            addhelper(val, this.root);
        }
    }

    private boolean addhelper(int val, Node n){
        if(n.val == val){
            return false; //can't add, val already exists
        }
        else if( val< n.val){
            if(n.left == null){
                n.left = new Node(val);
                n.left.parent = n;
                return true; //success
            }
            else{
                return addhelper(val, n.left);
            }
        }
        else{
            if(n.right == null){
                n.right = new Node(val);
                n.right.parent = n;
                return true; //success
            }
            else{
                return addhelper(val, n.right);
            }
        }
    }

    public void delete(int val){
        if(root.val == val && root.left == null && root.right ==  null){
            root = null;
        }
        else{
            deletehelper(val, root);
        }
    }

    private void deletehelper(int val, Node root){
        // found node
        if (root.val == val){
            //find node to replace with
            Node a = findSuccessor(root);
            //if no successor ('root' is a leaf) just delete
            if(a == null){
                deleteleaf(root);
            }
            // if successor swap values and
            swap(root,a);
            // delete the leaf node
            deleteleaf(a);
        }
    }

    //delete's a leaf node
    private void deleteleaf(Node a){
        Node par = a.parent;
        if(a.val<par.val){
            par.left = null;  //delete node
        }
        else{
            par.right = null; //delete node
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


    public Boolean find(int val){
        return findHelper(root,val);
    }

    private Boolean findHelper(Node root, int val){
        if (root.val == val){
            return true;
        }
        else{
            if(val<root.val){
                if(root.left != null)
                    return findHelper(root.left, val);
                else
                    return false;
            }
            else{
                if(root.right != null)
                    return findHelper(root.right, val);
                else
                    return false;
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

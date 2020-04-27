# 361C Term Project

The implementation of our 361C Term Project prompt: concurrent lockfree and fine-grained lock based binary search trees.

---

## Getting Started
1. download the java IDE of your choice (our project files are from IntelliJ)
2. create a new project (or import ours)
3. include our src/ folder as a project source module
4. add a JUnit run configuration for one of the following options or run the classes directly

#### Options
* test the fine-grained lockbased bst and compare it to a single-lock lockbased bst with  **_src/lock/Tester_**
* test the lockfree bst with **_src/lockfree/bst/Tester_**
* compare the single-lock, fine-grained, and lockfree bst's with **_src/Tester_**

---
## Binary Search Trees

### Description

#### Purpose
Allow insertion, deletion, and searching in a constrained _O(h)_ time where _h_ is the height of the tree being operated on.

#### Properties
* the left subtree of a node only contains nodes with keys less than the nodes key
* the right subtree of a node only contains nodes with keys greater than or equal to the nodes key
* the left and right subtrees are also binary search trees

### Lockbased <span style="font-size:small;">Fine-Grained</span>
![Class Diagram](./references/LockbasedBST.png)

### Lockfree

![Class Diagram](./references/LockfreeBST.png)

---

## Red-Black Trees

#### Purpose
Implements self-balancing into the binary search tree, ensuring optimal worst case operation times of _O(logn)_ instead of the _O(n)_ of traditional binary search trees.

#### Properties
* every node is either red or black
* the root node is black
* the external nodes are black
* a red nodes children are both black
* all paths from a node to its leaf descendants contain the same number of black nodes
* _also retains all the properties of a binary search tree_

### Lockbased <span style="font-size:small;">Fine-Grained</span>

_still in progress_

### Lockfree

_still in progress_

---

### Running the tests

* each tree implementation contains a unit test in the corresponding package that can be run to test correctness
* the larger testing class in the root directory can be used to compare performance between tree implementations (redblack vs bst && lockfree vs lockbased)

### Built With

* [Java SE 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) - language used
* [IntelliJ](https://www.jetbrains.com/idea/) - IDE


### Authors

* **Dylan Cauwels** -- *lockfree team*
* **Carson Schubert** -- *lockfree team*
* **Sandeep Guggari** -- *lockbased team*
* **Shafaat Ahsen** -- *lockbased team*

<!-- See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project. -->

### Acknowledgments
* [**Ellen, Faith & Fatourou, Panagiota & Ruppert, Eric & Breugel, Franck**. (2010). Non-blocking Binary Search Trees. Proceedings of the Annual ACM Symposium on Principles of Distributed Computing. 131-140. 10.1145/1835698.1835736.](https://www.researchgate.net/publication/221344000_Non-blocking_Binary_Search_Trees)
* **Dr. Vijay Garg** -- _Professor_

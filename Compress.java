import java.io.*;
import java.util.*;

/*
 * May 16, 2023
 * Part 1
 * This program intends to compress a source file into a target file using the Huffman coding method.
 */

public class Compress {
    // using priority queue to implement a min heap

    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("Use java Compress sourceFile.txt compressedFile.txt to run");
            System.exit(1);
        }

        File sourceFile = new File(args[0]);
        File compressedFile = new File(args[1]);

        compressFile(sourceFile, compressedFile);
    }

    private static void compressFile(File sourceFile, File compressedFile) throws IOException {
        Scanner scanner = new Scanner(sourceFile);
        StringBuilder stringBuilder = new StringBuilder();
       
        while (scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
            stringBuilder.append("\n");
        }
        scanner.close();

        String text = stringBuilder.toString();
        
        int[] charCounts = new int[256];
        for (int i = 0; i < text.length(); i++) {
            charCounts[(int) text.charAt(i)]++;
        }

        Tree tree = createTree(charCounts);
        String[] codes = getCode(tree.root);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(compressedFile));
        objectOutputStream.writeObject(tree);
        objectOutputStream.close();

        StringBuilder codesBuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            String code = codes[c];
            codesBuilder.append(code);
        }

        BitOutputStream bitOutputStream = new BitOutputStream(compressedFile, true);
        bitOutputStream.writeBit(codesBuilder.toString());
        bitOutputStream.close();
    }    

    public static String[] getCode(Tree.Node root) {
        if (root == null) {
            return null;
        }
        String[] codes = new String[2 * 128];
        assignCode(root, codes);
        return codes;
    }

    private static void assignCode(Tree.Node root, String[] codes) {
        if (root.left != null) {
            root.left.code = root.code + "0";
            assignCode(root.left, codes);

            root.right.code = root.code + "1";
            assignCode(root.right, codes);
        } else {
            codes[(int) root.element] = root.code;
        }
    }

    private static Tree createTree(int[] counts) {

        Heap<Tree> heap = new Heap<>(); 
        for (int i = 0; i < counts.length; i++) {
			if (counts[i] > 0)
				heap.add(new Tree(counts[i], (char) i)); // A leaf node tree
		}

		while (heap.getSize() > 1) {
			Tree t1 = heap.remove(); // Remove the smallest weight tree
			Tree t2 = heap.remove(); // Remove the next smallest weight
			heap.add(new Tree(t1, t2)); // Combine two trees
		}

		return heap.remove(); // The final tree
    }

    public static class Tree implements Comparable<Tree>, Serializable{
        Node root;

        public Tree(Tree t1, Tree t2) {
            root = new Node();
            root.left = t1.root;
            root.right = t2.root;
            root.weight = t1.root.weight + t2.root.weight;
        }

        public Tree(int weight, char element) {
            root = new Node(weight, element);
        }

        public int compareTo(Tree t) {
            if (root.weight < t.root.weight){
                return 1;
            } 
            else if (root.weight == t.root.weight){
                return 0;
            }
            else{
                return -1;
            }
        }

        public class Node implements Serializable {
            char element; 
            int weight; 
            Node left; 
            Node right; 
            String code = ""; 
            
            public Node() {
            }
            
            public Node(int weight, char element) {
                this.weight = weight;
                this.element = element;
            }
        }
    }
    
    static class Heap<E extends Comparable<E>> implements Serializable {

        private ArrayList<E> list = new ArrayList<E>();
        
        //Default Constructor
        public Heap() {}

        public Heap(E[] objects) {
            for (int i = 0; i < objects.length; i++){
                add(objects[i]);
            }
        }
        public void add(E newObject){
            list.add(newObject); 
            int currentIndex = list.size() - 1; 

            while (currentIndex > 0) {
                int parentIndex = (currentIndex - 1) / 2;
                if (list.get(currentIndex).compareTo(list.get(parentIndex)) > 0) {
                    E temp = list.get(currentIndex);
                    list.set(currentIndex, list.get(parentIndex));
                    list.set(parentIndex, temp);
                } else {
                    break; 
                }
                currentIndex = parentIndex;
            }
        }

        public E remove(){
            if (list.size() == 0) {
                return null;
            }

            E removedObject = list.get(0);
            list.set(0, list.get(list.size() - 1));
            list.remove(list.size() - 1);

            int currentIndex = 0;
            while (currentIndex < list.size()){
                int leftChildIndex = 2 * currentIndex + 1;
                int rightChildIndex = 2 * currentIndex + 2;

                if (leftChildIndex >= list.size()){
                    break; 
                }
                int maxIndex = leftChildIndex;
                if(rightChildIndex < list.size()){
                    if (list.get(maxIndex).compareTo(list.get(rightChildIndex)) < 0){
                        maxIndex = rightChildIndex;
                    }
                }
                if(list.get(currentIndex).compareTo(list.get(maxIndex)) < 0){
                    E temp = list.get(maxIndex);
                    list.set(maxIndex, list.get(currentIndex));
                    list.set(currentIndex, temp);
                    currentIndex = maxIndex;
                } 
                else{
                    break; 
                }
            }

            return removedObject;
        }
        public int getSize() {
            return list.size();
        }
    }

    //from assignment 2
    public static class BitOutputStream {
        private FileOutputStream output;
        private byte placeholder = 0; 
        private int numBits = 0; 

        public BitOutputStream(File file, boolean append) throws IOException {
            output = new FileOutputStream(file, append);
        }

        public void writeBit(String bitString) throws IOException {
            for (int i = 0; i < bitString.length(); i++)
                writeBit(bitString.charAt(i));
        }

        public void writeBit(char bit) throws IOException {
            placeholder <<= 1;

            placeholder |= Character.getNumericValue(bit);
            numBits++;

            if (!(numBits == 0 || numBits == 8)) {
                output.write(placeholder);
                placeholder <<= (8 - numBits);
            }

        }
        
        public void close() throws IOException {
            if (numBits>0) {
                placeholder <<= (8 - numBits); 
                output.write(placeholder);
            }
            output.close();
        }
    }
}
        
            


import java.io.*;
import java.util.ArrayList;
public class compressFile {
    public static void main(String[] args) throws IOException{
        if(args.length != 2){
            System.out.println("Please use the Command Line with the following command: java Compress sourceFile.txt compressedFile.txt to run");
            System.exit(1);
        }

        File sourceFile = new File(args[0]);
        if(!sourceFile.exists()){
            System.out.println("File error");
            System.exit(2);
        }
        
        DataInputStream sourceFileStream = new DataInputStream(new BufferedInputStream(new FileInputStream(sourceFile)));
        int size = sourceFileStream.available();
        byte[] temp = new byte[size];
        sourceFileStream.read(temp);
        sourceFileStream.close();
        String text = new String(temp);

        int[] counts = getCharacterFrequency(text);
        Tree tree = getHuffmanTree(counts);
        String[] codes = getCode(tree.root);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(codes[text.charAt(i)]);
        }

        ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(args[1]));
        output.writeObject(codes);
        output.writeInt(result.length());
        output.close();

        BitOutputStream outputStream = new BitOutputStream(new File(args[1]));
        outputStream.writeBit(result.toString());
        outputStream.close();

    }
    //From Assignment 2
    public static class BitOutputStream {
        private FileOutputStream output;
        private int placeholder = 0; 
        private int numBits = 0; 

        public BitOutputStream(File file) throws IOException {
            output = new FileOutputStream(file);
        }

        public void writeBit(String bitString) throws IOException {
            for (int i = 0; i < bitString.length(); i++)
                writeBit(bitString.charAt(i));
        }

        public void writeBit(char bit) throws IOException {
            if (bit == '0') {
                placeholder <<= 1; 
                numBits ++;
            } else if (bit == '1') {
                placeholder = (placeholder << 1) | 1;
                numBits ++;
            } else {
                throw new IllegalArgumentException("Bit must be 0 or 1.");
            } 
            if(numBits == 8) {
                output.write(placeholder);
                placeholder = 0;
                numBits = 0;
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
    
    public static String[] getCode(Tree.Node root){
        if(root == null){
            return null;
        }
        String[] codes = new String[2*128];
        assignCode(root,codes);
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
    
    //Gets a Huffman tree from codes
    public static Tree getHuffmanTree(int[] counts) {
        //Creates heap to hold tree
        Heap<Tree> heap = new Heap<Tree>(); 
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                heap.add(new Tree(counts[i], (char) i)); 
            }
        }

        while (heap.getSize() > 1) {
            Tree t1 = heap.remove(); 
            Tree t2 = heap.remove(); 
            heap.add(new Tree(t1, t2)); 
        }

        return heap.remove(); 
    }

    public static int[] getCharacterFrequency(String text) {
        int[] counts = new int[256];

        for (int i = 0; i < text.length(); i++) {
            counts[(int) text.charAt(i)]++;
        }
        return counts;
    }
    
    public static class Tree implements Comparable<Tree> {
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

        public class Node {
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
    
    static class Heap<E extends Comparable<E>> {

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
}
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class Decompress {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length != 2) {
            System.out.println("Please use the Command Line with the following command: java Decompress.java compressedFile.txt OutputFile.txt to run");
            System.exit(1);
        }

        File sourceFile = new File(args[0]);
        FileOutputStream outputFile = new FileOutputStream(args[1]);

        if (!sourceFile.exists()) {
            System.out.println("Could not find compressed file");
            System.exit(2);
        }
        
        FileInputStream source = new FileInputStream(args[1]);
        char[] result = decompresor(source);

        source.close();
        outputFile.close();
    }

    private static char[] decompresor(
        FileInputStream sourceFile
        ) throws IOException, ClassNotFoundException {

        ObjectInputStream objectInput = new ObjectInputStream(sourceFile);
        Tree tree = (Tree) objectInput.readObject();

        BitInputStream bitInput = new BitInputStream(sourceFile);
        char[] result = tree.decode(bitInput);

        bitInput.close();
        System.out.println(result);

        


        return null;
        
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

        public char[] decode(BitInputStream input) throws IOException {
            StringBuilder result = new StringBuilder();
            Node current = root;

            while (input.available() > 0) {
                boolean bit = input.readBit();
                if (bit) {
                    current = current.left;
                } else {
                    current = current.right;
                }

                if (current.left == null && current.right == null) {
                    result.append(current.element);
                    current = root;
                }
            }

            return result.toString().toCharArray();

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

    static class BitInputStream implements AutoCloseable{
        private FileInputStream input;
        private int bitPosition = 0;
        private byte currentByte;

        public BitInputStream(FileInputStream input) {
            this.input = input;
        }

        public boolean readBit() throws IOException {
            int offset = bitPosition++ % 8;

            if (offset == 0) {
                currentByte = (byte) input.read();
            }

            int mask = 1 << (7 - offset);
            return (currentByte & mask) != 0;
        }

        public int available() throws IOException {
            return (8 * input.available()) + (7 - (bitPosition % 8));
        }

        public void close() throws IOException {
            input.close();
        }
    }

}

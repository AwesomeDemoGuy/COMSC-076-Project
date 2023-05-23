import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class Decompress {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length != 2) {
            System.out.println("Please use the Command Line with the following command: java Decompress.java compressedFile.txt OutputFile.txt to run");
            System.exit(1);
        }

        File checkFile = new File(args[0]);

        if (!checkFile.exists()) {
            System.out.println("Could not find compressed file");
            System.exit(2);
        }

        FileInputStream sourceFile = new FileInputStream(args[0]);
        FileOutputStream outputFile = new FileOutputStream(args[1]);
        
        char[] result = decompresor(sourceFile, outputFile, args[0]);

        sourceFile.close();
        outputFile.close();
    }

    private static char[] decompresor(
        FileInputStream sourceFile, FileOutputStream outputFile, String sourceFilePath
        ) throws IOException, ClassNotFoundException {
        
        ObjectInputStream objectInput = new ObjectInputStream(new FileInputStream(sourceFilePath));

        Tree hashTree = (Tree) objectInput.readObject();

        // Find the size of the hashTree
        int sizeOfData = sizeOf(hashTree);
        objectInput.close();


        sourceFile.skip(sizeOfData);

        int toRead = sourceFile.available();

        List<Byte> codes = new ArrayList<Byte>();
        while (toRead > 0) {
            byte byteToAdd = (byte) sourceFile.read();
            codes.add(byteToAdd);
            toRead--;
        }
        
        // The binary source file is no longer needed so we can close it
        sourceFile.close();

        StringBuilder text = new StringBuilder();
        for (byte b: codes) {
            String byte1 = String.format("%8s", Integer.toBinaryString(b & 0xFF).replace(' ', '0'));
            text.append(byte1);
        }
        PrintWriter pw = new PrintWriter(outputFile);

        Tree.Node node = hashTree.root;
        char[] bitsArray = text.toString().toCharArray();

        for (int i = 0; i < bitsArray.length; i++) {
            char bit = bitsArray[i];
            int bitInt = Integer.parseInt(Character.toString(bit));
            node = (bitInt == 0) ? node.left : node.right;

            if (node.left == null && node.right == null) {
                pw.write(node.element);
                node = hashTree.root;
            }
        }

        pw.close();

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

    public static int sizeOf(Tree hashTree) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);

        objectOutput.writeObject(hashTree);
        objectOutput.flush();
        objectOutput.close();

        return byteOutput.toByteArray().length;
    }

}

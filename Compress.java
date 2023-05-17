import java.io.*;
import java.util.*;

/*
 * May 16, 2023
 * Part 1
 * This program intends to compress a source file into a target file using the Huffman coding method.
 */

public class Compress {
    // using priority queue to implement a min heap
    private static PriorityQueue<Node> queue;
    private static HashMap<Character, String> charToCode;
    private static HashMap<String, Character> codeToChar;

    public static void main(String[] args) throws IOException {
        String sourceFile = args[0];
        String compressedFile = args[1];

        compressFile(sourceFile, compressedFile);
    }

    private static void compressFile(String sourceFile, String compressedFile) throws IOException {
        HashMap<Character, Integer> charCounts = new HashMap<>();
        File file = new File(sourceFile);
        Scanner scanner = new Scanner(file);
    
        //counting character frequencies in source
        while (scanner.hasNext()) {
            char[] chars = scanner.nextLine().toCharArray();
            for (char c : chars) {
                charCounts.put(c, charCounts.getOrDefault(c, 0) + 1);
            }
        }
        scanner.close();
    
        //creating Huffman tree
        createTree(charCounts);
    
        //initializing charToCode and codeToChar
        charToCode = new HashMap<>();
        codeToChar = new HashMap<>();
    
        //creating dictionaries to lookup char or code
        createDictionaries(queue.peek(), "");
    
        //writing compressed file
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(compressedFile));
        objectOutputStream.writeObject(charToCode);
        BitOutputStream bitOutputStream = new BitOutputStream(new File(compressedFile));
        scanner = new Scanner(file);
        while (scanner.hasNext()) {
            char[] chars = scanner.nextLine().toCharArray();
            for (char c : chars) {
                bitOutputStream.writeBit(charToCode.get(c));
            }
        }
        scanner.close();
        bitOutputStream.close();
        objectOutputStream.close();
    }    

    private static void createTree(HashMap<Character, Integer> charCounts) {
        queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.freq));
        for (Map.Entry<Character, Integer> entry : charCounts.entrySet()) {
            queue.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (queue.size() > 1) {
            Node x = queue.poll();
            Node y = queue.poll();
            Node sum = new Node('-', x.freq + y.freq);
            sum.left = x;
            sum.right = y;
            queue.add(sum);
        }
    }

    private static void createDictionaries(Node node, String s) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            charToCode.put(node.c, s);
            codeToChar.put(s, node.c);
        }
        // traverse left
        createDictionaries(node.left, s + '0');
        // traverse right
        createDictionaries(node.right, s + '1');
    }

    static class Node {
        public char c;
        public int freq;
        public Node left = null, right = null;

        Node(char c, int freq) {
            this.c = c;
            this.freq = freq;
        }
    }

    //from assignment 2
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
}
        
            

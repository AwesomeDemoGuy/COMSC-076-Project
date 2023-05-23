import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;


public class Decompress {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
            if (args.length != 2) {
                System.out.println("Please use the Command Line with the following command: java Decompress.java compressedFile.txt OutputFile.txt to run");
                System.exit(1);
            }

            // Initialize the files
            File sourceFile = new File(args[0]);
            File outputFile = new File(args[1]);

            // If the source file does not exist,
            // notify the user and exit
            if (!sourceFile.exists()) {
                System.out.println("Could not find compressed file");
                System.exit(2);
            }
            
            // If the output file does not exist, create it
            if(!outputFile.exists()){
                outputFile.createNewFile();
            }

            // Initialize the input streams
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            BitInputStream bitInputStream = new BitInputStream(fileInputStream);

            // Read the codes from the file into a string
            String[] codes = (String[]) objectInputStream.readObject();

            StringBuilder bitString = new StringBuilder();
            StringBuilder result = new StringBuilder();

            int resultLength = objectInputStream.readInt();
            while (resultLength --> 0) {
                bitString.append(bitInputStream.readBit() ? '1' : '0');

                for (int i = 0; i < codes.length; i++) {
                    if (codes[i] != null && codes[i].equals(bitString.toString())) {
                        bitString = new StringBuilder();
                        result.append((char) i);
                    }
                }
            }

            FileWriter fileWriter = new FileWriter(outputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(result.toString());

            // Close the streams
            bufferedWriter.close();
            objectInputStream.close();
            bitInputStream.close();
    }

    // Allows User to Read Individual Bits, Or Bits In Binary String.
    static class BitInputStream {
        private FileInputStream input;
        private byte currentByte = 0;
        private int position = 0;

        /**
         * Creates a new BitInput stream from the given FileInputStream
         * @param input A file InputStream
         */
        public BitInputStream(FileInputStream input) {
            this.input = input;
        }
        
        /**
         * Creates a new BitInput stream from the given File
         * @param file
         */
        public BitInputStream(File file) throws IOException {
            this(new FileInputStream(file));
        }

        /**
         * Reads an individual bit from the stream.
         * @return A boolean representing true for 1 and false for 0.
         */
        public boolean readBit() throws IOException {
            // Calculate the offset within the byte and read a new byte if
            // needed.
            int offset = position++ % 8;
            if (offset == 0)
                currentByte = (byte) input.read();

            // Calculate the bit and return it in a character-form.
            int mask = 1 << (7 - offset);
            return (currentByte & mask) != 0;
        }

        
        /**
         * Closes the BitInputStream's file handles.
         */
        public void close() throws IOException {
            input.close();
        }
    }

}

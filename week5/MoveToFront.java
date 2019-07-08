import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

public class MoveToFront {

    private static final int LENGTH = 256;
    private static final char[] R = new char[LENGTH];

    public static void main(String[] args) {
        if (args[0].equals("-")) {
            encode();
        }
        if (args[0].equals("+")) {
            decode();
        }
    }

    public static void encode() {
        for (int i = 0; i < LENGTH; i++) {
            R[i] = (char) i;
        }
        while (!BinaryStdIn.isEmpty()) {
            char c = BinaryStdIn.readChar();
            int pos = 0;
            for (int i = 0; i < LENGTH; i++) {
                if (R[i] == c) {
                    pos = i;
                    BinaryStdOut.write(i, 8);
                    break;
                }
            }
            moveToFront(pos);
        }
        BinaryStdIn.close();
        BinaryStdOut.close();
    }

    private static void moveToFront(int pos) {
        for (int i = 0; i < pos; i++) {
            char temp = R[i];
            R[i] = R[pos];
            R[pos] = temp;
        }
    }

    public static void decode() {
        for (int i = 0; i < LENGTH; i++) {
            R[i] = (char) i;
        }
        while (!BinaryStdIn.isEmpty()) {
            char c = BinaryStdIn.readChar();
            int pos = (int) c;
            BinaryStdOut.write(R[pos], 8);
            moveToFront(pos);
        }
        BinaryStdIn.close();
        BinaryStdOut.close();
    }
}
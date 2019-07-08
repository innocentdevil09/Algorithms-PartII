import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

public class BurrowsWheeler {

    private static final int R = 256;

    public static void main(String[] args) {
        if (args[0].equals("-")) {
            transform();
        }
        if (args[0].equals("+")) {
            inverseTransform();
        }
    }

    public static void transform() {
        String s = BinaryStdIn.readString();
        CircularSuffixArray suffixArray = new CircularSuffixArray(s);
        for (int i = 0; i < s.length(); i++) {
            if (suffixArray.index(i) == 0) {
                BinaryStdOut.write(i);
                break;
            }
        }
        for (int i = 0; i < s.length(); i++) {
            int index = suffixArray.index(i) - 1;
            if (index < 0) {
                index += s.length();
            }
            BinaryStdOut.write(s.charAt(index));
        }

        BinaryStdIn.close();
        BinaryStdOut.close();
    }

    public static void inverseTransform() {
        int n = BinaryStdIn.readInt();
        String s = BinaryStdIn.readString();

        int[] count = new int[R + 1];
        int[] next = new int[s.length()];

        for (int i = 0; i < s.length(); i++) {
            count[s.charAt(i) + 1]++;
        }
        for (int i = 0; i < R; i++) {
            count[i + 1] += count[i];
        }
        for (int i = 0; i < s.length(); i++) {
            next[count[s.charAt(i)]++] = i;
        }

        int first = n;
        for (int i = 0; i < next.length; i++) {
            BinaryStdOut.write(s.charAt(next[first]));
            first = next[first];
        }

        BinaryStdIn.close();
        BinaryStdOut.close();
    }
}
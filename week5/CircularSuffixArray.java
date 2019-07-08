public class CircularSuffixArray {

    private final int length;
    private final int[] index;

    public CircularSuffixArray(String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        this.length = s.length();

        index = new int[s.length()];
        for (int i = 0; i < length; i++) {
            index[i] = i;
        }
        sort(index, s);
    }

    private void sort(int[] array, String input) {
        sort(0, array.length - 1, 0, input);
    }

    private void sort(int lo, int hi, int d, String input) {
        if (hi <= lo) { return; }
        int lt = lo, gt = hi;
        int v = charAt(index[lo], d, input);
        int i = lo + 1;
        while (i <= gt) {
            int t = charAt(index[i], d, input);
            if (t < v) {
                exch(lt++, i++);
            } else if (t > v) {
                exch(i, gt--);
            } else {
                i++;
            }
        }
        sort(lo, lt - 1, d, input);
        if (v >= 0) {
            sort(lt, gt, d + 1, input);
        }
        sort(gt + 1, hi, d, input);
    }

    private int charAt(int i, int d, String input) {
        return input.charAt((i + d) % length);
    }

    private void exch(int i, int j) {
        int temp = index[i];
        index[i] = index[j];
        index[j] = temp;
    }

    public int index(int i) {
        if (i < 0 || i > length - 1) {
            throw new IllegalArgumentException();
        }
        return index[i];
    }

    public int length() {
        return this.length;
    }
}
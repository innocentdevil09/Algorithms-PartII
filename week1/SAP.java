package week1;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.util.LinkedList;
import java.util.Queue;

public final class SAP {

    private final Digraph G;

    /**
     * Public constructor to assign Digraph to get shortest common ancestor
     *
     * @param G
     */
    public SAP(Digraph G) {
        if (G == null) { throw new IllegalArgumentException(); }
        this.G = new Digraph(G);
    }

    /**
     * Method to get the shortest length between the two given vertices
     *
     * @param v
     * @param w
     */
    public int length(int v, int w) {
        if (v < 0 || w < 0 || v >= G.V() || w >= G.V()) { throw new IllegalArgumentException(); }
        if (v == w) { return 0; }

        return findAncestralPath(v, w)[0];
    }

    /**
     * Method to find the shortest path between two given vertices
     *
     * @param v
     * @param w
     */
    private int[] findAncestralPath(int v, int w) {
        boolean[] vMarked = new boolean[G.V()];
        boolean[] wMarked = new boolean[G.V()];
        int[] vDistTo = new int[G.V()];
        int[] wDistTo = new int[G.V()];

        bfs(v, vMarked, vDistTo);
        bfs(w, wMarked, wDistTo);

        int commonAncestor = -1, minDistance = -1;
        for (int i = 0; i < G.V(); i++) {
            if (vMarked[i] && wMarked[i]) {
                int distance = vDistTo[i] + wDistTo[i];
                if (minDistance == -1) { minDistance = distance; }
                if (distance <= minDistance) {
                    commonAncestor = i;
                    minDistance = distance;
                }
            }
        }

        return new int[]{minDistance, commonAncestor};
    }

    /**
     * Polymorph method for list of vertices
     *
     * @param v
     * @param w
     */
    private int[] findAncestralPath(Iterable<Integer> v, Iterable<Integer> w) {
        boolean[] vMarked = new boolean[G.V()];
        boolean[] wMarked = new boolean[G.V()];
        int[] vDistTo = new int[G.V()];
        int[] wDistTo = new int[G.V()];

        bfs(v, vMarked, vDistTo);
        bfs(w, wMarked, wDistTo);

        int commonAncestor = -1, minDistance = -1;
        for (int i = 0; i < G.V(); i++) {
            if (vMarked[i] && wMarked[i]) {
                int distance = vDistTo[i] + wDistTo[i];
                if (minDistance == -1) { minDistance = distance; }
                if (distance <= minDistance) {
                    commonAncestor = i;
                    minDistance = distance;
                }
            }
        }

        return new int[]{minDistance, commonAncestor};
    }

    /**
     * Utility method to traverse the graph to get the shortest path
     *
     * @param v
     * @param vMarked
     * @param vDistTo
     */
    private void bfs(int v, boolean[] vMarked, int[] vDistTo) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(v);
        vMarked[v] = true;
        vDistTo[v] = 0;

        while (!queue.isEmpty()) {
            int elem = queue.poll();
            for (int adj : G.adj(elem)) {
                if (!vMarked[adj]) {
                    queue.add(adj);
                    vDistTo[adj] = vDistTo[elem] + 1;
                    vMarked[adj] = true;
                }
            }
        }
    }

    /**
     * Polymorph method for iterables
     *
     * @param vIterable
     * @param vMarked
     * @param vDistTo
     */
    private void bfs(Iterable<Integer> vIterable, boolean[] vMarked, int[] vDistTo) {
        Queue<Integer> queue = new LinkedList<>();
        for (int v : vIterable) {
            if (v < 0 || v >= G.V()) { throw new IllegalArgumentException(); }
            queue.add(v);
            vMarked[v] = true;
            vDistTo[v] = 0;
        }

        while (!queue.isEmpty()) {
            int elem = queue.poll();
            for (int w : G.adj(elem)) {
                if (!vMarked[w]) {
                    queue.add(w);
                    vMarked[w] = true;
                    vDistTo[w] = vDistTo[elem] + 1;
                }
            }
        }
    }

    /**
     * Public api to get common ancestor via the shortest path
     *
     * @param v
     * @param w
     */
    public int ancestor(int v, int w) {
        if (v < 0 || w < 0 || v >= G.V() || w >= G.V()) { throw new IllegalArgumentException(); }
        return findAncestralPath(v, w)[1];
    }

    /**
     * Polymorph method to get the shortest length between iterables
     *
     * @param v
     * @param w
     */
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) { throw new IllegalArgumentException(); }
        v.forEach(elem -> { if (elem == null) { throw new IllegalArgumentException(); } });
        w.forEach(elem -> { if (elem == null) { throw new IllegalArgumentException(); } });

        return findAncestralPath(v, w)[0];
    }

    /**
     * Polymorph method to get common ancestor with shortest path for iterables
     *
     * @param v
     * @param w
     */
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) { throw new IllegalArgumentException(); }
        v.forEach(elem -> { if (elem == null) { throw new IllegalArgumentException(); } });
        w.forEach(elem -> { if (elem == null) { throw new IllegalArgumentException(); } });

        return findAncestralPath(v, w)[1];
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int[] vals = sap.findAncestralPath(v, w);
            int length = vals[0];
            int ancestor = vals[1];
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
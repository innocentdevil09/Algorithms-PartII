import edu.princeton.cs.algs4.Digraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to solve a given Boggle Board to form the maximum possible words out of it
 */
public class BoggleSolver {

    /* Inner static class to form a trie data structure to store dictionary words */
    private final BoggleTrie trie;

    /**
     * Constructor to add dictionary words into trie
     *
     * @param dictionary
     */
    public BoggleSolver(String[] dictionary) {

        trie = new BoggleTrie();
        for (int i = 0; i < dictionary.length; i++) {
            String word = dictionary[i].trim().toUpperCase();
            trie.put(word, i + 1);
        }
    }

    /**
     * Method to get score for a given length of word
     *
     * @param wordLength
     */
    private int getScore(int wordLength) {
        switch (wordLength) {
            case 0:
            case 1:
            case 2:
                return 0;
            case 3:
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
                return 3;
            case 7:
                return 5;
            default:
                return 11;
        }
    }

    /**
     * Method to get score for a word
     *
     * @param word
     */
    public int scoreOf(String word) {
        word = word.trim().toUpperCase();
        if (!trie.contains(word)) { return 0; }
        return getScore(word.length());
    }

    /**
     * Method to return all valid words for a given BoggleBoard
     * Algo:
     * 1.Create a graph for the given board. Each box of the board is connected to its adjacent boxes in a directed
     * edge manner
     * 2. Picking each box at a time, we run a dfs over the graph to form all possible words and save them in a list
     * 3. Return the set of unique valid words from the list
     * <p>
     * NOTE: Instead of creating a graph, you could collect string from the board in a recursive manner using nested
     * for loops inside the getAllWords() method. This will decrease the time taken taken to create a graph and
     * increase your score in the assignment
     * I picked the graph construction route for easier debugging
     *
     * @param board
     */
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        Digraph digraph = createGraph(board);
        List<String> allWords = getAllWords(board, digraph);
        Set<String> validWords = new HashSet<>();

        for (String word : allWords) {
            if (word.length() > 2 && scoreOf(word) > 0) {
                validWords.add(word);
            }
        }
        return validWords;
    }

    /**
     * Method to create a Directed Graph for a given board, where each box is connected to its adjacent boxes
     *
     * @param board
     */
    private Digraph createGraph(BoggleBoard board) {
        Digraph digraph = new Digraph(board.rows() * board.cols());
        Map<String, Integer> gridMap = gridMap(board);
        for (int i = 0; i < board.rows(); i++) {
            for (int j = 0; j < board.cols(); j++) {
                int v = gridMap.get("(" + i + "," + j + ")");
                if (i > 0) { digraph.addEdge(v, gridMap.get("(" + (i - 1) + "," + j + ")")); }
                if (i > 0 && j > 0) { digraph.addEdge(v, gridMap.get("(" + (i - 1) + "," + (j - 1) + ")")); }
                if (i > 0 && j < board.cols() - 1) {
                    digraph.addEdge(v, gridMap.get("(" + (i - 1) + "," + (j + 1) + ")"));
                }
                if (j > 0) { digraph.addEdge(v, gridMap.get("(" + i + "," + (j - 1) + ")")); }
                if (i < board.rows() - 1 && j > 0) {
                    digraph.addEdge(v, gridMap.get("(" + (i + 1) + "," + (j - 1) + ")"));
                }
                if (i < board.rows() - 1) { digraph.addEdge(v, gridMap.get("(" + (i + 1) + "," + j + ")")); }
                if (i < board.rows() - 1 && j < board.cols() - 1) {
                    digraph.addEdge(v, gridMap.get("(" + (i + 1) + "," + (j + 1) + ")"));
                }
                if (j < board.cols() - 1) { digraph.addEdge(v, gridMap.get("(" + i + "," + (j + 1) + ")")); }
            }
        }
        return digraph;
    }

    /**
     * Method to perform a dfs over the given graph constructed out of the board to form all possible words and save
     * them in a list
     *
     * @param board
     * @param digraph
     */
    private List<String> getAllWords(BoggleBoard board, Digraph digraph) {
        List<String> allWords = new ArrayList<>();
        Map<Integer, Character> charMap = charMap(board);

        for (int i = 0; i < (board.rows() * board.cols()); i++) {
            Map<Integer, String> wordsMap = new HashMap<>();
            boolean[] marked = new boolean[digraph.V()];
            dfs(digraph, i, wordsMap, allWords, charMap, marked, "");
        }
        return allWords;
    }

    /**
     * Method to traverse the graph in a dfs manner and store all possible words in a list
     *
     * @param digraph
     * @param v
     * @param wordsMap
     * @param allWords
     * @param charMap
     * @param marked
     * @param str
     */
    private void dfs(Digraph digraph, int v, Map<Integer, String> wordsMap, List<String> allWords,
            Map<Integer, Character> charMap, boolean[] marked, String str) {
        marked[v] = true;
        String value = str + charToString(charMap.get(v));
        wordsMap.put(v, value);
        if (!trie.hasPrefix(value)) {
            wordsMap.remove(v);
            return;
        }
        allWords.add(value);
        for (int w : digraph.adj(v)) {
            if (!marked[w]) {
                dfs(digraph, w, wordsMap, allWords, charMap, marked, value);
                marked[w] = false;
            }
        }
    }

    /**
     * Method to form a map of the board grid and its associated integer.
     * Constructed for ease of use
     *
     * @param board
     */
    private Map<String, Integer> gridMap(BoggleBoard board) {
        Map<String, Integer> gridMap = new HashMap<>();
        int number = 0;
        for (int i = 0; i < board.rows(); i++) {
            for (int j = 0; j < board.cols(); j++) {
                gridMap.put("(" + i + "," + j + ")", number++);
            }
        }
        return gridMap;
    }

    /**
     * Method to construct a character map for the given board grid and position of each character on the board
     *
     * @param board
     */
    private Map<Integer, Character> charMap(BoggleBoard board) {
        Map<Integer, Character> charMap = new HashMap<>();
        int number = 0;
        for (int i = 0; i < board.rows(); i++) {
            for (int j = 0; j < board.cols(); j++) {
                charMap.put(number++, board.getLetter(i, j));
            }
        }
        return charMap;
    }

    /**
     * Method to return string for a given character
     *
     * @param c
     */
    private String charToString(char c) {
        if (c == 'Q') { return "QU"; }
        return String.valueOf(c);
    }

    private static class BoggleTrie {

        private static final int R = 26;
        private Node root;

        private class Node {
            int val;
            Node[] next = new Node[R];
        }

        public void put(String key, int value) {
            root = put(root, key, value, 0);
        }

        private Node put(Node x, String key, int val, int d) {
            if (x == null) {
                x = new Node();
            }
            if (d == key.length()) {
                x.val = val;
                return x;
            }
            int c = key.charAt(d) - 'A';
            x.next[c] = put(x.next[c], key, val, d + 1);
            return x;
        }

        public boolean contains(String key) {
            return get(key) != -1;
        }

        public int get(String key) {
            if (key == null) { return -1; }
            Node x = get(root, key, 0);
            if (x == null) { return -1; }
            return x.val != 0 ? x.val : -1;
        }

        private Node get(Node x, String key, int d) {
            if (x == null) { return null; }
            if (d == key.length()) {
                return x;
            }
            int c = key.charAt(d) - 'A';
            return get(x.next[c], key, d + 1);
        }

        public boolean hasPrefix(String prefix) {
            Node x = get(root, prefix, 0);
            return x != null;
        }
    }
}
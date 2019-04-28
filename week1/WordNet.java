package week1;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WordNet {

    private final Map<String, List<Integer>> synsetNounMap;
    private final Map<Integer, String> synsetMap;
    private final SAP sap;

    /**
     * This is the crux of this class.
     * Public constructor of WordNet which takes two inputs - synsets containing synsetIds corresponding to their
     * nouns, and their hypernym relationships
     * Each input is read line-by-line and their values are stored into the data variables of the class for easy
     * retrievals.
     * Synsets are stored in two variables - synsetNounMap and synsetMap. The names are confusing and should be updated.
     * synsetNounMap contains all the list of synsetIds for a given particular noun. the synsets input file has for
     * some cases, multiple nouns given for one synsetId (separated by space). At the same time, a noun can also have
     * multiple synsetIds. Its a many-to-many relationship.
     * synsetMap is a simpler data structure containing all the nouns for a given synsetId
     * <p>
     * The hypernyms input is graph of synsetIds, giving the relevant information of hypernyms of each synsetId. The
     * data is constructed in a form of directed graph
     *
     * @param synsets
     * @param hypernyms
     */
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException("Input parameters cannot be null!");
        }
        synsetNounMap = new HashMap<>();
        synsetMap = new HashMap<>();
        In synsetIn = new In(synsets);
        String[] synsetLines = synsetIn.readAllLines();

        for (String synsetLine : synsetLines) {
            String[] arr = synsetLine.split(",");
            if (arr.length < 2) { continue; }
            int synsetId = Integer.parseInt(arr[0].trim());
            if (synsetId >= synsetLines.length) { continue; }
            String[] nouns = arr[1].split(" ");
            for (String noun : nouns) {
                List<Integer> list = synsetNounMap.get(noun);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(synsetId);
                synsetNounMap.put(noun.trim(), list);
            }
            synsetMap.put(synsetId, arr[1].trim());
        }
        synsetIn.close();

        In hypernymIn = new In(hypernyms);
        String[] allLines = hypernymIn.readAllLines();
        int maxVertex = -1;
        for (String line : allLines) {
            String[] arr = line.split(",");
            for (String str : arr) {
                int v = Integer.parseInt(str.trim());
                if (v > maxVertex) {
                    maxVertex = v;
                }
            }
        }
        if (allLines.length < maxVertex) { throw new IllegalArgumentException("Number of edges less than maxVertex"); }
        Digraph hypernymGraph = new Digraph(maxVertex + 1);
        for (String line : allLines) {
            String[] arr = line.split(",");
            if (arr.length < 2) { continue; }
            int v = Integer.parseInt(arr[0].trim());
            if (v > maxVertex) { continue; }
            for (int i = 1; i < arr.length; i++) {
                int w = Integer.parseInt(arr[i].trim());
                if (w > maxVertex) { continue; }
                hypernymGraph.addEdge(v, w);
            }
        }
        hypernymIn.close();
        DirectedCycle finder = new DirectedCycle(hypernymGraph);
        if (finder.hasCycle()) { throw new IllegalArgumentException(); }
        sap = new SAP(hypernymGraph);
    }

    public Iterable<String> nouns() {
        return synsetNounMap.keySet();
    }

    public boolean isNoun(String word) {
        if (word == null) { throw new IllegalArgumentException(); }
        return synsetNounMap.containsKey(word);
    }

    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new IllegalArgumentException("Input parameters to find distance cannot be null!");
        }
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException("Input parameters must be valid nouns to calculate distance!");
        }
        return sap.length(synsetNounMap.get(nounA), synsetNounMap.get(nounB));
    }

    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new IllegalArgumentException("Input parameters to find distance cannot be null!");
        }
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException("Input parameters must be valid nouns to calculate distance!");
        }
        int ancestor = sap.ancestor(synsetNounMap.get(nounA), synsetNounMap.get(nounB));
        return synsetMap.get(ancestor);
    }

    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        StdOut.print(wordnet.distance("a", "o"));
    }
}
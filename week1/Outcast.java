package week1;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public final class Outcast {

    private final WordNet wordnet;

    public Outcast(WordNet wordnet) {
        this.wordnet = wordnet;
    }

    public String outcast(String[] nouns) {
        String noun = null;
        int maxDistance = -1;

        for (String nounA : nouns) {
            int distance = 0;
            for (String nounB : nouns) {
                int dist = wordnet.distance(nounA, nounB);
                distance += (dist > 0 ? dist : 0);
            }
            if (distance > maxDistance) {
                maxDistance = distance;
                noun = nounA;
            }
        }
        return noun;
    }

    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}
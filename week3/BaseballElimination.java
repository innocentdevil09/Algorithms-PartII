import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to determine which team falls out of the league using Maximum Flow - Min Cut Theorem (Ford-Fulkerson Algo)
 */
public class BaseballElimination {

    /* Data variable to indicate total no. of teams */
    private final int noOfTeams;
    /* Data variable to store all info related to each team like wins, losses, remaining matches, and its index */
    private final Map<String, List<Integer>> teamInfo;
    /* Data variable to indicate no. of matches left between team i and j */
    private final int[][] fixtureGrid;
    /* Data variable to store teams eliminated from the league */
    private final Map<String, List<String>> eliminated;

    public BaseballElimination(String fileName) {
        if (fileName == null) { throw new IllegalArgumentException(); }
        In in = new In(fileName);
        teamInfo = new HashMap<>();
        eliminated = new HashMap<>();
        noOfTeams = Integer.parseInt(in.readLine());
        fixtureGrid = new int[noOfTeams][noOfTeams];

        int index = 0;
        while (in.hasNextLine()) {
            String line = in.readLine();
            initialize(line.trim(), index);
            index++;
        }
        checkTrivialElimination();
        checkNonTrivialElimination();
    }

    /**
     * Method to initialize data structures, read input file and store the information in the data structures
     *
     * @param line
     * @param index
     */
    private void initialize(String line, int index) {
        String[] info = line.split("\\s+");

        List<Integer> list = new ArrayList<>();
        list.add(Integer.parseInt(info[1]));
        list.add(Integer.parseInt(info[2]));
        list.add(Integer.parseInt(info[3]));
        list.add(index);

        teamInfo.put(info[0], list);
        for (int i = 0; i < noOfTeams; i++) {
            fixtureGrid[i][index] = Integer.parseInt(info[i + list.size()]);
        }
    }

    /**
     * Algo:
     * If the wins + remaining matches total tally of a team is less than the maximum number of matches already won by
     * another team -- current team is eliminated from the league
     */
    private void checkTrivialElimination() {
        int maxWins = -1;
        String teamWithMaxWins = "";
        for (String team : teams()) {
            if (wins(team) > maxWins) {
                maxWins = wins(team);
                teamWithMaxWins = team;
            }
        }

        for (String team : teams()) {
            if (wins(team) + remaining(team) < maxWins) {
                List<String> list = new ArrayList<>();
                list.add(teamWithMaxWins);
                eliminated.put(team, list);
            }
        }
    }

    /**
     * Method to check if a team is eliminated from the league based on Max Flow - Min Cut theorem
     * http://coursera.cs.princeton.edu/algs4/assignments/baseball.html
     */
    private void checkNonTrivialElimination() {
        Map<Integer, String> teamIndex = new HashMap<>();
        for (String team : teams()) {
            teamIndex.put(teamInfo.get(team).get(3), team);
        }
        for (String team : teams()) {
            if (eliminated.containsKey(team)) { continue; }
            Set<Integer> excludedTeams = new HashSet<>();
            for (String team1 : eliminated.keySet()) {
                excludedTeams.add(teamInfo.get(team1).get(3));
            }
            excludedTeams.add(teamInfo.get(team).get(3));
            int total = wins(team) + remaining(team);
            Map<Integer, Integer> edgesTo = edgesToMap(excludedTeams);
            FlowNetwork network = createNetwork(excludedTeams, teamIndex, total, edgesTo);
            FordFulkerson fordFulkerson = new FordFulkerson(network, 0, network.V() - 1);

            boolean elimination = false;
            for (FlowEdge e : network.adj(0)) {
                if (e.residualCapacityTo(e.to()) > 0) {
                    elimination = true;
                }
            }
            if (!elimination) { continue; }

            List<String> subset = new ArrayList<>();
            for (int i = 0; i < noOfTeams; i++) {
                if (excludedTeams.contains(i)) { continue; }
                if (fordFulkerson.inCut(edgesTo.get(i))) {
                    subset.add(teamIndex.get(i));
                }
            }
            eliminated.put(team, subset);
        }
    }

    /**
     * Method to create a FlowNetwork for a given list of excluded teams
     * FlowNetwork contains a source vertex, connecting to all the fixtures between different teams and a sink vertex
     * which is connected to all the teams represented as vertex
     * <p>
     * Refer this link: http://coursera.cs.princeton.edu/algs4/assignments/baseball.html to gain clarity
     *
     * @param excludedTeams
     * @param teamIndex
     * @param total
     * @param edgesTo
     */
    private FlowNetwork createNetwork(Set<Integer> excludedTeams, Map<Integer, String> teamIndex, int total,
            Map<Integer, Integer> edgesTo) {
        int source = 0;
        int fixtureVertices = (noOfTeams - excludedTeams.size()) * (noOfTeams - excludedTeams.size() - 1) / 2;
        int teamVertices = noOfTeams - excludedTeams.size();
        int sink = fixtureVertices + teamVertices + 1;

        FlowNetwork network = new FlowNetwork(sink + 1);
        int sourceEdges = 1;
        for (int i = 0; i < noOfTeams; i++) {
            for (int j = i + 1; j < noOfTeams; j++) {
                if (excludedTeams.contains(i) || excludedTeams.contains(j)) { continue; }
                FlowEdge sourceEdge = new FlowEdge(source, sourceEdges++, fixtureGrid[i][j]);
                FlowEdge iTeamEdge = new FlowEdge(sourceEdge.to(), edgesTo.get(i), Double.POSITIVE_INFINITY);
                FlowEdge jTeamEdge = new FlowEdge(sourceEdge.to(), edgesTo.get(j), Double.POSITIVE_INFINITY);
                network.addEdge(sourceEdge);
                network.addEdge(iTeamEdge);
                network.addEdge(jTeamEdge);
            }
        }

        for (int i = 0; i < noOfTeams; i++) {
            if (excludedTeams.contains(i)) { continue; }
            FlowEdge sinkEdge = new FlowEdge(edgesTo.get(i), sink, total - wins(teamIndex.get(i)));
            network.addEdge(sinkEdge);
        }

        return network;
    }

    /**
     * Method to return a map of each team versus its representation as the vertex (connecting to sink) in the
     * FlowNetwork
     *
     * @param excludedTeams
     */
    private Map<Integer, Integer> edgesToMap(Set<Integer> excludedTeams) {
        Map<Integer, Integer> edgesTo = new HashMap<>();

        int fixtureVertices = (noOfTeams - excludedTeams.size()) * (noOfTeams - excludedTeams.size() - 1) / 2;
        int counter = 1;
        for (int i = 0; i < noOfTeams; i++) {
            if (excludedTeams.contains(i)) { continue; }
            edgesTo.put(i, fixtureVertices + counter++);
        }
        return edgesTo;
    }

    /**
     * Returns a list of all teams
     */
    public Iterable<String> teams() {
        return teamInfo.keySet();
    }

    /**
     * Returns the wins by a team
     *
     * @param team
     */
    public int wins(String team) {
        validate(team);
        List<Integer> list = teamInfo.get(team);
        return list.get(0);
    }

    /**
     * Returns the remaining matches of a team
     *
     * @param team
     */
    public int remaining(String team) {
        validate(team);
        List<Integer> list = teamInfo.get(team);
        return list.get(2);
    }

    /**
     * Method to validate any input team given as parameter
     *
     * @param team
     */
    private void validate(String team) {
        if (team == null) { throw new IllegalArgumentException(); }
        if (!teamInfo.containsKey(team)) { throw new IllegalArgumentException(); }
    }

    /**
     * Return total no. of teams
     */
    public int numberOfTeams() {
        return noOfTeams;
    }

    /**
     * Returns the losses registered by a team
     *
     * @param team
     */
    public int losses(String team) {
        validate(team);
        List<Integer> list = teamInfo.get(team);
        return list.get(1);
    }

    /**
     * Returns the matches left between the two teams
     *
     * @param team1
     * @param team2
     */
    public int against(String team1, String team2) {
        validate(team1);
        validate(team2);
        int i = teamInfo.get(team1).get(3);
        int j = teamInfo.get(team2).get(3);
        return fixtureGrid[i][j];
    }

    /**
     * To check if a team is eliminated from the league
     *
     * @param team
     */
    public boolean isEliminated(String team) {
        validate(team);
        return eliminated.containsKey(team);
    }

    /**
     * Method to determine the cause of elimination of a team
     *
     * @param team
     */
    public Iterable<String> certificateOfElimination(String team) {
        validate(team);
        return eliminated.get(team);
    }
}
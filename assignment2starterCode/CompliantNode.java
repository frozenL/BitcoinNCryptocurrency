import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;
    private HashSet<Transaction> transactions;
    private boolean[] followees;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees.clone();
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.transactions = new HashSet<Transaction>(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        return this.transactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        for(Candidate can: candidates) {
            transactions.add(can.tx);
        }
    }
}

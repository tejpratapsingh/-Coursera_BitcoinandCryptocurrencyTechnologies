import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    public double _p_graph, _p_malicious, _p_txDistribution;
    public int _numRounds, _oldRoundNumber = 0, _currRoundNumber;
    public boolean[] _followees, _blacklists;
    public Set<Transaction> _pendingTransactions, _consensusTransactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        _p_graph = p_graph;
        _p_malicious = p_malicious;
        _p_txDistribution = p_txDistribution;
        _numRounds = numRounds;
        _pendingTransactions = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        _followees = followees;
        _blacklists = new boolean[followees.length];
        Arrays.fill(_blacklists, Boolean.FALSE);
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        _pendingTransactions = pendingTransactions;
        _consensusTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        Set<Transaction> retVal = new HashSet<Transaction>();
        if(_currRoundNumber == _numRounds)
        {
            retVal = _consensusTransactions;
        }else if(_currRoundNumber < _numRounds){
            retVal.addAll(_pendingTransactions);
            _oldRoundNumber = _currRoundNumber;
        }

        return retVal;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        this._currRoundNumber++;
        //Number of rounds complete
        if(_currRoundNumber >= _numRounds -1)
            return;

        if(_oldRoundNumber > 0 && _currRoundNumber > _oldRoundNumber)
        {
            _pendingTransactions.clear();
        }

        //Blacklist: No transactions coming out of a node.
        Set<Integer> senders = candidates.stream().map(c -> c.sender).collect(toSet());
        for (int i = 0; i < _followees.length; i++)
        {
            if(_followees[i] && !senders.contains(i)){
                _blacklists[i] = true;
            }
        }

        //Add correct transactions
        for (Candidate can: candidates) {
            if(!_consensusTransactions.contains(can.tx) && !_blacklists[can.sender]){
                _consensusTransactions.add(can.tx);
                _pendingTransactions.add(can.tx);
            }
        }
    }
}

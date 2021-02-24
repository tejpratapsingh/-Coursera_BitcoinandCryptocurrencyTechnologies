import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    private UTXOPool _utxoPool;
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        _utxoPool = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        double sumIVals = 0;
        double sumOVals = 0;
        ArrayList<UTXO> usedUtxo = new ArrayList<>();

        for (int count=0;count<tx.numInputs();count++) {
            Transaction.Input input = tx.getInput(count);
            int outputInd = input.outputIndex;
            byte[] prevTansactionHash = input.prevTxHash;
            byte[] sig = input.signature;

            UTXO utxo = new UTXO(prevTansactionHash, outputInd);

            //check rule (1): all outputs claimed by tx are in current UTXO pool
            if (!_utxoPool.contains(utxo)) {
                return false;
            }
            //check rule (2): the signatures on each input of tx are valid
            Transaction.Output output = _utxoPool.getTxOutput(utxo);
            byte[] message = tx.getRawDataToSign(count);
            if (!Crypto.verifySignature(output.address,message,sig)) {
                return false;
            }
            //check rule (3): no UTXO is claimed multiple times by tx
            if (usedUtxo.contains(utxo)) {
                return false;
            }
            usedUtxo.add(utxo);
            sumIVals += output.value;
        }
        //check rule (4): all of tx output values are non-negative
        for (int i=0;i<tx.numOutputs();i++) {
            Transaction.Output output = tx.getOutput(i);
            if (output.value < 0) {
                return false;
            }
            sumOVals += output.value;
        }
        //check rule (5): the sum of tx input values is greater than or equal to the sum of its output values
        if (sumIVals < sumOVals) {
            return false;
        }
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> validTransactions = new ArrayList<>();
        for (Transaction t : possibleTxs) {
            if (isValidTx(t)) {
                validTransactions.add(t);

                //removing
                for (Transaction.Input input : t.getInputs()) {
                    int outputInd = input.outputIndex;
                    byte[] prevTransactionHash = input.prevTxHash;
                    UTXO utxo = new UTXO(prevTransactionHash, outputInd);
                    _utxoPool.removeUTXO(utxo);
                }
                //adding
                byte[] hash = t.getHash();
                for (int i=0;i<t.numOutputs();i++) {
                    UTXO utxo = new UTXO(hash, i);
                    _utxoPool.addUTXO(utxo, t.getOutput(i));
                }
            }
        }
        Transaction[] validTransactionsArr = new Transaction[validTransactions.size()];
        validTransactionsArr = validTransactions.toArray(validTransactionsArr);
        return validTransactionsArr;
    }

}

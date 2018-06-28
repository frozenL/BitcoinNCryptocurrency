import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool pool;
    public TxHandler(UTXOPool utxoPool) {
        pool = new UTXOPool(utxoPool);
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
        double sumInput = 0;
        double sumOutput = 0;

        HashMap<UTXO, Boolean> alreadyClaimed = new HashMap<UTXO, Boolean>();
        ArrayList<Transaction.Input> txInputs = tx.getInputs();
        int inputSize = tx.getInputs().size();
        for(int i = 0; i < inputSize; i ++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            // 1
            if(!pool.contains(utxo)) {
                return false;
            }
            // 2
            Transaction.Output o = pool.getTxOutput(utxo);
            if(!Crypto.verifySignature(o.address, tx.getRawDataToSign(i), in.signature)) {
                return false;
            }
            // 3
            if(alreadyClaimed.containsKey(utxo)) {
                return false;
            }
            alreadyClaimed.put(utxo, true);
            sumInput += o.value;
        }
        // 4
        ArrayList<Transaction.Output> txOutputs = tx.getOutputs();
        for(Transaction.Output o: txOutputs) {
            if(o.value < 0) {
                return false;
            }
            sumOutput += o.value;
        }
        // 5
        if(sumInput < sumOutput) {
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
        // IMPLEMENT THIS
        Set<Transaction> validTx = new HashSet<Transaction>();
        for (Transaction tx: possibleTxs) {
            if(! isValidTx(tx)) {
                continue;
            }
            validTx.add(tx);
            // remove inputs
            for(Transaction.Input in: tx.getInputs()) {
                pool.removeUTXO(new UTXO(in.prevTxHash, in.outputIndex));;
            }
            // add outputs
            for(int i = 0; i < tx.numOutputs(); i ++) {
                Transaction.Output out = tx.getOutput(i);
                UTXO curUTXO = new UTXO(tx.getHash(), i);
                pool.addUTXO(curUTXO, out);
            }
        }
        Transaction[] ret = new Transaction[validTx.size()];
        return validTx.toArray(ret);
    }

}

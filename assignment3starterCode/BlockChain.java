import java.util.ArrayList;
import java.util.HashMap;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private TransactionPool txPool = new TransactionPool();
    private BlockExtended maxHeightBlockExtended;
    private HashMap<ByteArrayWrapper, BlockExtended> blocks;

    private class BlockExtended {
        public Block block;
        public long createdTime;
        public int height;
        public UTXOPool utxoPool;

        public BlockExtended(Block block, int height) {
            this.block = block;
            createdTime = System.currentTimeMillis();
            this.height = height;
        }
    }

    /**
     * create an empty block chain with just a genesis block. Assume
     * {@code genesisBlock} is a valid block
     */
    public BlockChain(Block genesisBlock) {
        BlockExtended newBlock = new BlockExtended(genesisBlock, 1);
        UTXOPool utxoPool = new UTXOPool();
        for(int i = 0; i < genesisBlock.getCoinbase().getOutputs().size(); i ++) {
            UTXO utxo = new UTXO(genesisBlock.getCoinbase().getHash(), i);
            utxoPool.addUTXO(utxo, genesisBlock.getCoinbase().getOutput(i));
        }
        newBlock.utxoPool = utxoPool;
        maxHeightBlockExtended = newBlock;
        blocks = new HashMap<ByteArrayWrapper, BlockExtended>();
        blocks.put(new ByteArrayWrapper(genesisBlock.getHash()), newBlock);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return maxHeightBlockExtended.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return maxHeightBlockExtended.utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all
     * transactions should be valid and block should be at
     * {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block
     * height 2) if the block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot
     * create a new block at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // null
        if(block == null) {
            return false;
        }
        // genesis block
        if (block.getPrevBlockHash() == null) {
            return false;
        }

        // prevHash
        if(!blocks.containsKey(new ByteArrayWrapper(block.getPrevBlockHash()))) {
            return false;
        }

        // select a branch
        int maxHeight = maxHeightBlockExtended.height;
        // check if all txs are valid
        BlockExtended newBlock = new BlockExtended(block, maxHeight + 1);
        UTXOPool utxoPool = getMaxHeightUTXOPool();
        TxHandler newHandler = new TxHandler(utxoPool);
        Transaction[] retTxs = newHandler.handleTxs(block.getTransactions().toArray(new Transaction[block.getTransactions().size()]));
        if(retTxs.length != block.getTransactions().size()) {
            return false;
        }
        newBlock.utxoPool = newHandler.getUTXOPool();
        // remove from transaction pool
        for(Transaction tx: block.getTransactions()) {
            txPool.removeTransaction(tx.getHash());
        }
        maxHeightBlockExtended = newBlock;
        blocks.put(new ByteArrayWrapper(block.getHash()), newBlock);
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        txPool.addTransaction(tx);
    }
}

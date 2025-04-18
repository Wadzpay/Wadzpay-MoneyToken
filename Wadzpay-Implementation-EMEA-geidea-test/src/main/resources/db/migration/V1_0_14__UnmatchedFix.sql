ALTER TABLE unmatched_transaction
    ADD CONSTRAINT uc_unmatchedtransaction_blockchaintxid UNIQUE (blockchain_tx_id);

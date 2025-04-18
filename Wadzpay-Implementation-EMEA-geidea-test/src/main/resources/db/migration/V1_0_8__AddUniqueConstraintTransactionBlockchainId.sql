ALTER TABLE transaction
    ADD CONSTRAINT uc_transaction_blockchaintxid UNIQUE (blockchain_tx_id);

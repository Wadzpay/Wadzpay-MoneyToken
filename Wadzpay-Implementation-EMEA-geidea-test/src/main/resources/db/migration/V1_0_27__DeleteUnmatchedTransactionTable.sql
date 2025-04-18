ALTER TABLE unmatched_transaction
    DROP CONSTRAINT fk_unmatched_transaction_on_matched_with;

ALTER TABLE unmatched_transaction
    DROP CONSTRAINT uc_unmatchedtransaction_blockchaintxid;

DROP TABLE unmatched_transaction CASCADE;

alter table transaction
    drop constraint if exists uc_transaction_blockchaintxid;

/*
Because send external transaction and pos order transaction have same hash.
*/

ALTER TABLE block_confirmation_log
    ADD COLUMN if not exists confirmation_source VARCHAR(100);

ALTER TABLE block_confirmation_log
    ADD COLUMN if not exists confirmation_status VARCHAR(100);

/*
Because send external transaction and pos order transaction have same hash.
*/

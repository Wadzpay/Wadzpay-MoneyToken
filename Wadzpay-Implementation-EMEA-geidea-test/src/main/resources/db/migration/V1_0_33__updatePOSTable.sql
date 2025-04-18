ALTER TABLE transaction_pos
    ADD transaction_id bigint
constraint fk5t5otsosiwxigikhd0oeikahdsjh
references transaction,
    ADD comments VARCHAR
;

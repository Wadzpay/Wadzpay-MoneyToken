CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

ALTER TABLE transaction
    ADD uuid UUID NOT NULL DEFAULT uuid_generate_v4();

ALTER TABLE transaction
    ADD CONSTRAINT uc_transaction_uuid UNIQUE (uuid);

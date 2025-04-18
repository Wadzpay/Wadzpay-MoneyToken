create table if not exists issuance_banks_sub_bank_entry
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    issuance_banks_id BIGINT NOT NULL,
    parent_bank_id   BIGINT NOT NULL,
    is_accessible  BOOLEAN DEFAULT true,
    created_at timestamp NULL,
    updated_at timestamp NULL,
    is_active BOOLEAN DEFAULT true,
    CONSTRAINT pk_issuance_banks_sub_bank PRIMARY KEY (id),
    CONSTRAINT fk_issuance_banks_id FOREIGN KEY (issuance_banks_id) REFERENCES issuance_banks (id),
    CONSTRAINT fk_parent_bank_id FOREIGN KEY (parent_bank_id) REFERENCES issuance_banks (id)

    );

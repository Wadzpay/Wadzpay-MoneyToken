create table if not exists department
(
    department_id int NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483645 CACHE 1 ),
    department_name     character varying(255),
    created_at    timestamp NULL,
    created_by    bigint,
    updated_at    timestamp NULL,
    updated_by    bigint,
    status        boolean,
    constraint department_pkey primary key (department_id),
    CONSTRAINT fk_department_created_by FOREIGN KEY (created_by) REFERENCES user_account (id),
    CONSTRAINT fk_department_updated_by FOREIGN KEY (updated_by) REFERENCES user_account (id)
    );

create table if not exists department_transaction
(
    department_transaction_id bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2094967295 CACHE 1 ),
    department_id int ,
    department_name character varying(255),
    created_updated_at timestamp NULL,
    created_updated_by  bigint,
    status  boolean,
    constraint department_transaction_pkey primary key (department_transaction_id),
    CONSTRAINT department_id FOREIGN KEY (department_id) REFERENCES department (department_id),
    CONSTRAINT fk_department_transaction_created_updated_by FOREIGN KEY (created_updated_by) REFERENCES user_account (id)
    );

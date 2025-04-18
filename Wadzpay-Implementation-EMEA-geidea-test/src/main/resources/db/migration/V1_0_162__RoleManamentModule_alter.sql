ALTER TABLE modules ADD Column if not exists parent_name character varying(120);
ALTER TABLE modules_transaction ADD Column if not exists parent_name character varying(120);


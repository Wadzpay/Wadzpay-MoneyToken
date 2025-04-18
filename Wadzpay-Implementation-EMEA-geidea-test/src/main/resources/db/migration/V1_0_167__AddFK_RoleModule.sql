DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_role_module'
    ) THEN
ALTER TABLE role_modules
    ADD CONSTRAINT fk_role_module FOREIGN KEY (role_id) REFERENCES role(role_id);
END IF;
END $$;

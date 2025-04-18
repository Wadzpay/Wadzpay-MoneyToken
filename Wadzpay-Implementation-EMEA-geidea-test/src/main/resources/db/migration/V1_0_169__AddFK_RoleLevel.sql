DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_roles_level_constraint'
    ) THEN
ALTER TABLE role
    ADD CONSTRAINT fk_roles_level_constraint FOREIGN KEY (level_id) REFERENCES levels(level_id);
END IF;
END $$;

ALTER TABLE role DROP CONSTRAINT IF Exists unique_roles_columns_constraint;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE tablename = 'role'
        AND indexname = 'unique_roles_columns_index'
    ) THEN
        EXECUTE '
            CREATE UNIQUE INDEX unique_roles_columns_index
            ON role (role_name, level_id, aggregator_id)
            WHERE status IS NOT FALSE
        ';
END IF;
END $$;

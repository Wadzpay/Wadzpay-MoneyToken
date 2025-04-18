DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'contact' AND column_name = 'nickname' AND is_nullable = 'NO') THEN
ALTER TABLE contact ALTER COLUMN nickname DROP NOT NULL;
END IF;
END $$;

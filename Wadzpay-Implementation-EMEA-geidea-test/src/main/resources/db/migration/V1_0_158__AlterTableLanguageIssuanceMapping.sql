
-- Alter language_issuance_mapping master;
ALTER TABLE public.language_issuance_mapping
    ADD COLUMN if not exists is_default BOOLEAN DEFAULT false;


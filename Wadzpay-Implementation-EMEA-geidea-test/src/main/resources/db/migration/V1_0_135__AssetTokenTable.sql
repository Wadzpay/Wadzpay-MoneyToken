ALTER TABLE asset_creation_request
    ADD COLUMN IF NOT EXISTS asset_type VARCHAR NULL,
    ADD COLUMN IF NOT EXISTS asset_category VARCHAR NULL,
    ADD COLUMN IF NOT EXISTS asset_unit_quantity numeric (40, 20),
    ADD COLUMN IF NOT EXISTS option1 VARCHAR NULL
;

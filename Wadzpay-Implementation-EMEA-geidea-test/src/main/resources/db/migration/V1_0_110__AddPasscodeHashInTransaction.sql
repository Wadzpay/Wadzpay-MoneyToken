ALTER TABLE transaction
    ADD COLUMN IF NOT EXISTS passcode_hash VARCHAR(500)

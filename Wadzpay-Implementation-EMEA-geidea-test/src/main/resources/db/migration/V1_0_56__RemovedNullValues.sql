UPDATE transaction set refund_status = 'NULL' where refund_status IS NULL;
UPDATE transaction set refund_type = 'NA' where refund_type IS NULL;

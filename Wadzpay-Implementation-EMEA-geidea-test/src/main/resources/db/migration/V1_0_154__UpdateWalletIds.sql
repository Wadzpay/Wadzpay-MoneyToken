CREATE OR REPLACE FUNCTION generate_institution_number(institutionId INT)
    RETURNS VARCHAR(6) AS $$
BEGIN
    -- Format the number to be 6 digits with leading zeros
RETURN LPAD(institutionId::text, 6, '0');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_user_number(user_id INT)
    RETURNS VARCHAR(9) AS $$
BEGIN
    -- Format the number to be 8 digits with leading zeros
RETURN LPAD(user_id::text, 9, '0');
END;
$$ LANGUAGE plpgsql;


UPDATE issuance_banks_user_entry
SET wallet_id = concat(generate_institution_number(CAST(issuance_banks_id as int)),'-',generate_user_number(CAST(user_account_id as int)))
WHERE EXISTS (
    SELECT 1
    FROM issuance_banks_user_entry
);

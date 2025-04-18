ALTER TABLE institution
    ADD COLUMN institution_logo VARCHAR(1500);

ALTER TABLE merchant_group
    ADD COLUMN merchant_group_logo VARCHAR(1500);

ALTER TABLE merchant_acquirer
    ADD COLUMN merchant_acquirer_logo VARCHAR(1500);

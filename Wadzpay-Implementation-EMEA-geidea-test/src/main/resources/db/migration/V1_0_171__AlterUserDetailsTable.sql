
-- Alter user_details table;
ALTER TABLE public.user_details
    ADD COLUMN if not exists role_from_user_id bigint null;

-- Alter user_details_transaction table;
ALTER TABLE public.user_details_transaction
    ADD COLUMN if not exists role_from_user_id bigint null;


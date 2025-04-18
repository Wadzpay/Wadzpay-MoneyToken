Alter table public.outlet add column is_parent_blocked BOOLEAN DEFAULT FALSE;
Alter table public.sub_merchant_acquirer add column is_parent_blocked BOOLEAN DEFAULT FALSE;
Alter table public.merchant_acquirer add column is_parent_blocked BOOLEAN DEFAULT FALSE;
Alter table public.merchant_group add column is_parent_blocked BOOLEAN DEFAULT FALSE;
Alter table public.institution add column is_parent_blocked BOOLEAN DEFAULT FALSE;

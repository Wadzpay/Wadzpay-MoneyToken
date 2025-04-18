update public.refund_token set is_expired = true, count = 1 where valid_for <= now();
update public.refund_token set is_expired = false, count = 1 where valid_for > now();

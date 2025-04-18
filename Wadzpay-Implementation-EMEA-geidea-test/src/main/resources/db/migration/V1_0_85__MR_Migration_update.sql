alter table public.transaction_refund_details add column refundable_amount_fiat numeric(40,20) DEFAULT 0.0;

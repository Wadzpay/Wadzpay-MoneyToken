update public.transaction_refund_details trd
set refund_type = 'FULL'
    from transaction tr
where trd.refund_type is null
  and trd.transaction_id = tr.id
  and trd.refund_amount_fiat = tr.total_fiat_received;

update public.transaction_refund_details trd
set refund_type = 'PARTIAL'
    from transaction tr
where trd.refund_type is null
  and trd.transaction_id = tr.id
  and trd.refund_amount_fiat != tr.total_fiat_received;

update public.transaction_refund_details trd
set refundable_amount_fiat = tr.total_fiat_received - tr.total_refunded_amount_fiat
    from public.transaction tr
where trd.transaction_id = tr.id;

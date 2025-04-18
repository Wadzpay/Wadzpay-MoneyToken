alter table transaction
    alter column fee drop default;

alter table transaction
    alter column amount type numeric(40, 20) using cast(amount as numeric(40, 20));

alter table transaction
    alter column fee type numeric(40, 20) using cast(fee as numeric(40, 20));

alter table transaction
    alter column fee set default 0.0;

alter table transaction
    alter column fiat_amount type numeric(40, 20) using cast(fiat_amount as numeric(40, 20));

alter table wadzpay_order
    alter column amount type numeric(40, 20) using cast(amount as numeric(40, 20));

alter table wadzpay_order
    alter column fiat_amount type numeric(40, 20) using cast(fiat_amount as numeric(40, 20));

alter table unmatched_transaction
    alter column amount type numeric(40, 20) using cast(amount as numeric(40, 20));



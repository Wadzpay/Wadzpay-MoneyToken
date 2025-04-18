alter table transaction_pos
    alter column amountcrypto type numeric(40, 20) using cast(amountcrypto as numeric(40, 20)),
    alter column amountfiat type numeric(40, 20) using cast(amountfiat as numeric(40, 20)),
    alter column feewadzpay type numeric(40, 20) using cast(feewadzpay as numeric(40, 20)),
    alter column feeexternal type numeric(40, 20) using cast(feeexternal as numeric(40, 20))    ;

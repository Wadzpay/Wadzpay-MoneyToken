alter table wadzpay_subaccount
    alter column asset type varchar using (
        case asset
            when 0 then 'BTC'
            when 1 then 'ETH'
            when 2 then 'USDT'
        end
    );

alter table wadzpay_order
    alter column currency type varchar using (
        case currency
            when 0 then 'BTC'
            when 1 then 'ETH'
            when 2 then 'USDT'
        end
    ),
    alter column fiat_currency type varchar using (
        case fiat_currency
            when 0 then 'USD'
            when 1 then 'EUR'
            when 2 then 'INR'
            else null
        end
    );

alter table transaction
    alter column asset type varchar using (
        case asset
            when 0 then 'BTC'
            when 1 then 'ETH'
            when 2 then 'USDT'
        end
    ),
    alter column status type varchar using (
        case status
            when 0 then 'SUCCESSFUL'
            when 1 then 'FAILED'
            when 2 then 'IN_PROGRESS'
        end
    ),
    alter column type type varchar using (
        case type
            when 0 then 'ON_RAMP'
            when 1 then 'OFF_RAMP'
            when 2 then 'MERCHANT'
            when 3 then 'PEER_TO_PEER'
            when 4 then 'OTHER'
        end
    );

alter table ramp_order_entity
    alter column ramp_tx_type type varchar using (
        case ramp_tx_type
            when 0 then 'ON_RAMP'
            when 1 then 'OFF_RAMP'
        end
    ),
    alter column status type varchar using (
        case status
            when 0 then 'PROCESSING'
            when 1 then 'WAITING_FOR_PAYMENT'
            when 2 then 'COMPLETED'
            else null
        end
    ),
    alter column ramp_payment_method type varchar using (
        case ramp_payment_method
            when 0 then 'CARD'
            when 1 then 'SEPA'
            when 2 then 'GBP_BANK'
            when 3 then 'NEFT_BANK'
        end
    );

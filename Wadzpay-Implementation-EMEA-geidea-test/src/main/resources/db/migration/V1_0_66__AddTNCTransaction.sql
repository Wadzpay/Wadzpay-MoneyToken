ALTER TABLE merchant
    ADD IF NOT EXISTS tnc VARCHAR(5000) default '';

update merchant set tnc='{"heading":"Terms and Conditions","para1":"Exchange or refund can be made within SIX MONTHS from the date of purchase.","para2":"For any exchange or refund please produce the PURCHASE RECEIPT or SALES DOCKET at the CUSTOMER SERVICE DESK","para3":"Only products with complete accessories and with original packing can be returned or exchanged.","para4":"As per our return policy, all goods can be returned within six months, however, they should be in an unused condition, in its original packaging and with the purchase receipt.Items returned are subject for verification and its physical condition.","footer":"Terms and Conditions"}' where id in (92, 47,191);

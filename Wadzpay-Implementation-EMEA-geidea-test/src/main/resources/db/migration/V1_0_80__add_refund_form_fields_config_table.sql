create table refund_form_fields_config(
                                          form_name varchar(220) PRIMARY KEY,
                                          txn_reference  boolean,
                                          customer_name boolean,
                                          mobile boolean,
                                          email boolean,
                                          digital_amt boolean,
                                          digital_name boolean,
                                          refund_amount_in_fiat boolean,
                                          refund_amount_in_crypto boolean,
                                          reason boolean,
                                          src_wallet_addr boolean,
                                          wallet_addr boolean,
                                          confirm_wallet_addr boolean
);



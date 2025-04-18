ALTER TABLE  public.aggregator drop CONSTRAINT  if exists uk_aggregator_preference_id CASCADE;
ALTER TABLE IF EXISTS public.aggregator add  CONSTRAINT unique_columns_aggregator UNIQUE (aggregator_preference_id, aggregator_name);

ALTER TABLE  public.institution drop CONSTRAINT  if exists unique_columns_constraint1 CASCADE;
ALTER TABLE IF EXISTS public.institution add  CONSTRAINT unique_columns_institution UNIQUE (aggregator_preference_id, institution_id,insitution_name);

ALTER TABLE  public.merchant_group drop CONSTRAINT  if exists unique_columns_merchant_group CASCADE;
ALTER TABLE IF EXISTS public.merchant_group add  CONSTRAINT unique_columns_merchant_group UNIQUE (aggregator_preference_id, insitution_preference_id, merchant_group_preference_id,merchant_group_name);

ALTER TABLE  public.merchant_acquirer drop CONSTRAINT  if exists unique_columns_merchant_acquirer CASCADE;
ALTER TABLE IF EXISTS public.merchant_acquirer add  CONSTRAINT unique_columns_merchant_acquirer UNIQUE (aggregator_preference_id, insitution_preference_id, merchant_group_preference_id, merchant_acquirer_id,merchant_acquirer_name);

ALTER TABLE  public.sub_merchant_acquirer drop CONSTRAINT  if exists unique_columns_sub_merchant_acquirer CASCADE;
ALTER TABLE IF EXISTS public.sub_merchant_acquirer add  CONSTRAINT unique_columns_sub_merchant_acquirer UNIQUE (aggregator_preference_id, insitution_preference_id, merchant_group_preference_id, merchant_acquirer_preference_id, sub_merchant_acquirer_id,sub_merchant_acquirer_name);

ALTER TABLE  public.outlet drop CONSTRAINT  if exists unique_columns_outlet CASCADE;
ALTER TABLE  public.outlet drop CONSTRAINT  if exists unique_column_outlet_name CASCADE;
ALTER TABLE IF EXISTS public.outlet add  CONSTRAINT unique_columns_outlet UNIQUE (aggregator_preference_id, insitution_preference_id, merchant_group_preference_id, merchant_acquirer_preference_id, sub_merchant_preference_id,outlet_id,outlet_name);

ALTER TABLE  public.pos drop CONSTRAINT  if exists unique_ip CASCADE;
ALTER TABLE  public.pos drop CONSTRAINT  if exists unique_mac CASCADE;
ALTER TABLE  public.pos drop CONSTRAINT  if exists unique_serail CASCADE;

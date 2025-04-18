ALTER TABLE  public.institution drop CONSTRAINT  if exists unique_columns_institution CASCADE;
ALTER TABLE IF EXISTS public.institution add  CONSTRAINT unique_columns_institution UNIQUE (aggregator_preference_id, institution_id);

ALTER TABLE  public.merchant_group drop CONSTRAINT if exists unique_columns_merchant_group CASCADE;
ALTER TABLE IF EXISTS public.merchant_group add  CONSTRAINT unique_columns_merchant_group UNIQUE (aggregator_preference_id, insitution_preference_id, merchant_group_preference_id);

ALTER TABLE  public.merchant_acquirer drop CONSTRAINT  if exists unique_columns_merchant_acquirer CASCADE;
ALTER TABLE IF EXISTS public.merchant_acquirer add  CONSTRAINT unique_columns_merchant_acquirer UNIQUE (aggregator_preference_id, insitution_preference_id, merchant_group_preference_id, merchant_acquirer_id);

ALTER TABLE  public.sub_merchant_acquirer drop CONSTRAINT  if exists unique_columns_sub_merchant_acquirer CASCADE;
ALTER TABLE IF EXISTS public.sub_merchant_acquirer add  CONSTRAINT unique_columns_sub_merchant_acquirer UNIQUE (aggregator_preference_id, insitution_preference_id, merchant_group_preference_id, merchant_acquirer_preference_id, sub_merchant_acquirer_id);


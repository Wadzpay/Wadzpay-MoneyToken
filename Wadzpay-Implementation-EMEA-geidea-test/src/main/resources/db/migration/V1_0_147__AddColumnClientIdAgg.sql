ALTER TABLE IF EXISTS public.aggregator add  column if not exists client_aggregator_preference_id  character varying(25) ;
ALTER TABLE IF EXISTS public.merchant_acquirer add  column if not exists client_merchant_acquirer_id character varying(25) ;
ALTER TABLE IF EXISTS public.sub_merchant_acquirer add  column if not exists client_sub_merchant_acquirer_id character varying(25);
ALTER TABLE IF EXISTS public.outlet add  column if not exists client_outlet_id character varying(25);

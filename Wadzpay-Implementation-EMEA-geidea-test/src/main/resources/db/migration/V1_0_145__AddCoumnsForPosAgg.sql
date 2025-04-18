ALTER TABLE IF EXISTS public.pos add column IF NOT EXISTS aggregator_preference_id character varying(255) ;
ALTER TABLE IF EXISTS public.pos add column IF NOT EXISTS insitution_preference_id character varying(255) ;
ALTER TABLE IF EXISTS public.pos add column IF NOT EXISTS merchant_group_preference_id character varying(255) ;
ALTER TABLE IF EXISTS public.pos add column IF NOT EXISTS merchant_acquirer_preference_id character varying(255) ;
ALTER TABLE IF EXISTS public.pos add column IF NOT EXISTS sub_merchant_preference_id character varying(255);


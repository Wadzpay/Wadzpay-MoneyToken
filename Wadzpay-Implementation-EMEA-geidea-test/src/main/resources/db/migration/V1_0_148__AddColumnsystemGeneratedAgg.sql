ALTER TABLE IF EXISTS public.institution add  column if not exists system_generated  BOOLEAN DEFAULT FALSE;
ALTER TABLE IF EXISTS public.merchant_acquirer add  column if not exists system_generated BOOLEAN DEFAULT FALSE;
ALTER TABLE IF EXISTS public.sub_merchant_acquirer add  column if not exists system_generated BOOLEAN DEFAULT FALSE;
ALTER TABLE IF EXISTS public.merchant_group add  column if not exists system_generated BOOLEAN DEFAULT FALSE;
ALTER TABLE IF EXISTS public.merchant_acquirer add  column if not exists generated_from  character varying(25) ;
ALTER TABLE IF EXISTS public.sub_merchant_acquirer add  column if not exists generated_from  character varying(25);
ALTER TABLE IF EXISTS public.merchant_group add  column if not exists generated_from  character varying(25);
ALTER TABLE IF EXISTS public.merchant_group add  column if not exists client_merchant_group_id character varying(25);

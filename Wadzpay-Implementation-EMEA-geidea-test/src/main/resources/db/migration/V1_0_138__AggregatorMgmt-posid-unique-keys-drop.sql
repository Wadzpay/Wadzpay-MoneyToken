ALTER TABLE  public.pos drop CONSTRAINT  if exists unique_column_pos_id CASCADE;
DROP INDEX IF EXISTS public.unique_column_pos_id CASCADE ;
Alter table public.pos add column pos_key varchar(255) ;



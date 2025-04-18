
ALTER TABLE public.role
    ALTER COLUMN created_by   DROP NOT NULL ;
ALTER TABLE public.role
    ALTER COLUMN updated_by   DROP NOT NULL ;


ALTER TABLE public.role_modules
    ALTER COLUMN created_by   DROP NOT NULL ;


ALTER TABLE public.role_modules
    ALTER COLUMN updated_by   DROP NOT NULL ;

ALTER TABLE public.role_modules_transaction
    ALTER COLUMN created_updated_by   DROP NOT NULL ;


ALTER TABLE public.role_transaction
    ALTER COLUMN created_updated_by   DROP NOT NULL ;



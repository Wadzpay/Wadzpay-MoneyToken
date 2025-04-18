ALTER TABLE public.role
drop constraint  fk_roles_created_by;
ALTER TABLE public.role
drop constraint  fk_roles_updated_by;

ALTER TABLE public.role_modules
drop constraint  fk_role_modules_created_by;
ALTER TABLE public.role_modules
drop constraint  fk_role_modules_updated_by;
ALTER TABLE public.role_transaction
drop constraint  fk_role_transaction_created_updated_by;

ALTER TABLE public.role_modules_transaction
drop constraint  fk_role_modules_transaction_created_updated_by;

ALTER TABLE public.role
ALTER COLUMN created_by TYPE character varying(255) ;
ALTER TABLE public.role
    ALTER COLUMN created_by SET NOT NULL ;

ALTER TABLE public.role
ALTER COLUMN  updated_by TYPE character varying(255) ;
ALTER TABLE public.role
    ALTER COLUMN updated_by SET NOT NULL ;

ALTER TABLE public.role
add constraint  fk_roles_created_by FOREIGN KEY (created_by) REFERENCES user_details(user_preference_id);
ALTER TABLE public.role
    add constraint  fk_roles_updated_by FOREIGN KEY (updated_by) REFERENCES user_details(user_preference_id);


ALTER TABLE public.role_modules
ALTER COLUMN created_by TYPE character varying(255) ;
ALTER TABLE public.role_modules
    ALTER COLUMN created_by SET NOT NULL ;

ALTER TABLE public.role_modules
ALTER COLUMN  updated_by TYPE character varying(255) ;
ALTER TABLE public.role_modules
    ALTER COLUMN updated_by SET NOT NULL ;
ALTER TABLE public.role_modules_transaction
ALTER COLUMN created_updated_by TYPE character varying(255) ;
ALTER TABLE public.role_modules_transaction
    ALTER COLUMN created_updated_by SET NOT NULL ;

ALTER TABLE public.role_transaction
ALTER COLUMN  created_updated_by TYPE character varying(255) ;
ALTER TABLE public.role_transaction
    ALTER COLUMN created_updated_by SET NOT NULL ;


ALTER TABLE public.role_modules
    add constraint  fk_role_modules_created_by FOREIGN KEY (created_by) REFERENCES user_details(user_preference_id);
ALTER TABLE public.role_modules
    add constraint  fk_role_modules_updated_by FOREIGN KEY (updated_by) REFERENCES user_details(user_preference_id);

ALTER TABLE public.role_modules_transaction
    add constraint  fk_role_modules_transaction_created_updated_by FOREIGN KEY (created_updated_by) REFERENCES user_details(user_preference_id);
ALTER TABLE public.role_transaction
    add constraint  fk_role_transaction_created_updated_by FOREIGN KEY (created_updated_by) REFERENCES user_details(user_preference_id);



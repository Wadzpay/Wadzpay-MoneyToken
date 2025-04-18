export enum RouteType {
  HOME = "/",
  ACCEPT_INVITE = "/accept-invite",
  ACCEPT_INVITATIONS = "/accept-invitations",
  CREATE_ACCOUNT = "/onboarding/create-password",
  VERIFY_PHONE_CODE = "/onboarding/verify-phone-code",
  VERIFY_EMAIL_CODE = "/onboarding/verify-email-code",
  ACCOUNT_DETAILS = "/onboarding/account-details",
  ISSUANCE_DETAILS = "/onboarding/merchant-details",
  ONBOARDING_SUCCESS = "/onboarding/success",
  SIGN_IN = "/sign-in",
  RESET_PASSWORD = "/reset-password",
  RESET_PASSWORD_SUCCESS = "/reset-password/success",
  SUBMIT_RESET_PASSWORD = "/forgot-password/verify",
  ADMIN = "/settings",
  ADMIN_USERS = "/settings/users",
  ADMIN_USERS_ROLES = "/settings/users/roles",
  ADMIN_USERS_INVITE = "/settings/users/invite",
  ADMIN_API_KEYS = "/settings/api-keys",
  ADMIN_API_KEYS_CREATE = "/settings/api-keys/generate",
  SETTINGS = "/settings",
  CHANGE_PASSWORD = "/changepassword",
  CHANGE_PASSWORD_SUCCESS = "/changepassword/success",
  FIAT_CURRENT_LIMIT = "/settings/fiat-limits",
  CUSTOMER_MANUAL_VERIFICATION_FORM = "/customermanualverificationform",
  WALLETSLIST = "/wallets",
  INSTITUTION_MANAGEMENT = "/admin/institution-management",
  INSTITUTION_REGISTER = "/admin/institution-register",
  ROLE_MANAGEMENT = "/admin/role-management",
  ROLE_CREATE = "/admin/role-create",
  USER_MANAGEMENT = "/admin/user-management",
  REFUND_FORM_TOKEN = "/:id",
  WALLET_PARAMETER = "/configurations/wallet-parameter",
  TRANSACTION_LIMITS = "/configurations/transaction-limits",
  CONVERSION_RATES = "/configurations/conversion-rates",
  CONVERSION_RATES_ADJUSTMENT = "/configurations/conversion-rates-adjustment",
  LANGUAGES = "/configurations/languages",
  ADD_LANGUAGE = "/configurations/languages/add",
  EDIT_LANGUAGE = "/configurations/languages/edit",
  INSTITUTION_LANGUAGES = "/configurations/institution/languages",
  LOGO = "/configurations/institution/logo"
}

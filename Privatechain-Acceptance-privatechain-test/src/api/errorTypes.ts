export enum ErrorType {
  BAD_REQUEST = "BAD_REQUEST",
  FORBIDDEN = "FORBIDDEN",
  INCORRECT_CODE = "INCORRECT_CODE",
  INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR",
  INVALID_INPUT_FORMAT = "INVALID_INPUT_FORMAT",
  INVALID_PHONE_NUMBER = "INVALID_PHONE_NUMBER",
  PHONE_NUMBER_ALREADY_EXISTS = "PHONE_NUMBER_ALREADY_EXISTS",
  PHONE_NUMBER_DOES_NOT_EXISTS = "PHONE_NUMBER_ALREADY_VERIFIED",
  UNAUTHORIZED = "UNAUTHORIZED",
  UNKNOWN_TWILIO_ERROR = "UNKNOWN_TWILIO_ERROR",
  USER_NOT_FOUND = "USER_NOT_FOUND",
  NOT_FOUND = "NOT_FOUND",
  UNVERIFIED_PHONE_NUMBER = "UNVERIFIED_PHONE_NUMBER",
  EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS",
  FRAUDULENT_USER = "FRAUDULENT_USER",
  UNKNOWN_SEON_ERROR = "UNKNOWN_SEON_ERROR",
  EMAIL_DOES_NOT_EXISTS = "EMAIL_DOES_NOT_EXISTS",
  TRANSACTION_NOT_FOUND = "TRANSACTION_NOT_FOUND",
  SUBACCOUNT_NOT_FOUND = "SUBACCOUNT_NOT_FOUND",
  INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS",
  USERS_ALREADY_IN_DATABASE = "USERS_ALREADY_IN_DATABASE",
  MERCHANT_ALREADY_EXISTS = "MERCHANT_ALREADY_EXISTS",
  INVALID_AMOUNT_NEGATIVE_OR_ZERO = "INVALID_AMOUNT_NEGATIVE_OR_ZERO",
  MERCHANT_NOT_FOUND = "MERCHANT_NOT_FOUND",
  INVALID_EMAIL = "INVALID_EMAIL",
  INVALID_ROLE = "INVALID_ROLE",
  INVITATION_NOT_FOUND = "INVITATION_NOT_FOUND",
  TOKEN_EXPIRED = "TOKEN_EXPIRED",
  INVALID_WALLET_ADDRESS = "INVALID_WALLET_ADDRESS",
  UNKNOWN_WALLET_ERROR = "UNKNOWN_WALLET_ERROR",
  EXTERNAL_SEND_INSIDE_THE_SYSTEM = "EXTERNAL_SEND_INSIDE_THE_SYSTEM",
  WALLET_NOT_AVAILABLE = "WALLET_NOT_AVAILABLE",
  TRANSACTION_NOT_MATCHING_REFUND_USER_NAME = "TRANSACTION_NOT_MATCHING_REFUND_USER_NAME",
  REFUND_ALREADY_EXISTS_ERROR = "REFUND_ALREADY_EXISTS_ERROR",
  TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT = "TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT",
  AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE = "AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE"
}

import { useTranslation } from "react-i18next"

import { EndpointKey } from "./constants"
import { ErrorType } from "./errorTypes"

const useGeneralErrorMessages: () => { [key in ErrorType]?: string } = () => {
  const { t } = useTranslation()
  return {
    [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    [ErrorType.UNAUTHORIZED]: t("ERROR_MESSAGE.UNAUTHORIZED"),
    [ErrorType.FORBIDDEN]: t("ERROR_MESSAGE.FORBIDDEN"),
    [ErrorType.NOT_FOUND]: t("ERROR_MESSAGE.NOT_FOUND"),
    [ErrorType.INTERNAL_SERVER_ERROR]: t("ERROR_MESSAGE.INTERNAL_SERVER_ERROR"),
    [ErrorType.INVALID_INPUT_FORMAT]: t("ERROR_MESSAGE.INVALID_INPUT_FORMAT"),
    [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND"),
    [ErrorType.USER_BANK_ACCOUNT_NOT_FOUND]: t("ERROR_MESSAGE.USER_BANK_ACCOUNT_NOT_FOUND"),
    [ErrorType.CANNOT_ALLOW_LESS_THEN_MIN_FIAT]: t("ERROR_MESSAGE.CANNOT_ALLOW_LESS_THEN_MIN_FIAT"),
    [ErrorType.CANNOT_ALLOW_MORE_THEN_MAX_FIAT]: t("ERROR_MESSAGE.CANNOT_ALLOW_MORE_THEN_MAX_FIAT"),
    [ErrorType.BLOCKCHAIN_SERVICE_NOT_FOUND]: t("ERROR_MESSAGE.BLOCKCHAIN_SERVICE_NOT_FOUND"),
    [ErrorType.WALLET_DISABLED_RECIPIENT]: t("ERROR_MESSAGE.WALLET_DISABLED_RECIPENT"),
    [ErrorType.WALLET_DISABLED]: t("ERROR_MESSAGE.WALLET_DISABLED"),
    [ErrorType.BLOCKCHAIN_INSUFFICIENT_AMOUNT]:t("ERROR_MESSAGE.BLOCKCHAIN_INSUFFICIENT_AMOUNT"),
    [ErrorType.RECIPIENT_WALLET_NOT_ACTIVATED]:t("ERROR_MESSAGE.RECIPIENT_WALLET_NOT_ACTIVATED"),
    [ErrorType.OTHER_BANK_TRANSFER_NOT_ALLOWED]:t("ERROR_MESSAGE.OTHER_BANK_TRANSFER_NOT_ALLOWED")
  }
}

export const useErrorMessages: () => {
  [key in EndpointKey]: {
    [key in ErrorType]?: string
  }
} = () => {
  const { t } = useTranslation()
  const generalErrorMessages = useGeneralErrorMessages()
  return {
    signIn: {
      ...generalErrorMessages
    },
    userDetailsAndEmailOTP: {
      ...generalErrorMessages,
      [ErrorType.UNVERIFIED_PHONE_NUMBER]: t(
        "ERROR_MESSAGE.UNVERIFIED_PHONE_NUMBER"
      ),
      [ErrorType.EMAIL_ALREADY_EXISTS]: t("ERROR_MESSAGE.EMAIL_ALREADY_EXISTS"),
      [ErrorType.FRAUDULENT_USER]: t("ERROR_MESSAGE.FRAUDULENT_USER"),
      [ErrorType.UNKNOWN_SEON_ERROR]: t("ERROR_MESSAGE.UNKNOWN_SEON_ERROR")
    },
    verifyEmailOTPAndCreateUser: {
      ...generalErrorMessages,
      [ErrorType.UNVERIFIED_PHONE_NUMBER]: t(
        "ERROR_MESSAGE.UNVERIFIED_PHONE_NUMBER"
      ),
      [ErrorType.EMAIL_ALREADY_EXISTS]: t("ERROR_MESSAGE.EMAIL_ALREADY_EXISTS"),
      [ErrorType.INCORRECT_CODE]: t("ERROR_MESSAGE.INCORRECT_CODE"),
      [ErrorType.VERIFICATION_NOT_FOUND]: t("ERROR_MESSAGE.INCORRECT_CODE"),
      [ErrorType.EMAIL_DOES_NOT_EXISTS]: t(
        "ERROR_MESSAGE.EMAIL_DOES_NOT_EXISTS"
      )
    },
    sendPhoneOTP: {
      ...generalErrorMessages,
      [ErrorType.PHONE_NUMBER_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_ALREADY_EXISTS"
      ),
      [ErrorType.UNKNOWN_TWILIO_ERROR]: t("ERROR_MESSAGE.UNKNOWN_TWILIO_ERROR"),
      [ErrorType.INVALID_PHONE_NUMBER]: t("ERROR_MESSAGE.INVALID_PHONE_NUMBER")
    },
    verifyPhoneOTP: {
      ...generalErrorMessages,
      [ErrorType.INCORRECT_CODE]: t("ERROR_MESSAGE.INCORRECT_CODE"),
      [ErrorType.VERIFICATION_NOT_FOUND]: t("ERROR_MESSAGE.INCORRECT_CODE"),
      [ErrorType.PHONE_NUMBER_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_ALREADY_EXISTS"
      ),
      [ErrorType.PHONE_NUMBER_DOES_NOT_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_DOES_NOT_EXISTS"
      ),
      [ErrorType.UNKNOWN_TWILIO_ERROR]: t("ERROR_MESSAGE.UNKNOWN_TWILIO_ERROR")
    },
    getUser: {
      ...generalErrorMessages
    },
    deleteUser: {
      ...generalErrorMessages
    },
    userBalances: {
      ...generalErrorMessages
    },
    userTransaction: {
      ...generalErrorMessages,
      [ErrorType.TRANSACTION_NOT_FOUND]: t(
        "ERROR_MESSAGE.TRANSACTION_NOT_FOUND"
      )
    },
    userTransactions: {
      ...generalErrorMessages
    },
    addUserTransaction: { 
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND"),
      [ErrorType.SUBACCOUNT_NOT_FOUND]: t("ERROR_MESSAGE.SUBACCOUNT_NOT_FOUND"),
      [ErrorType.INSUFFICIENT_FUNDS]: t("ERROR_MESSAGE.INSUFFICIENT_FUNDS"),
      [ErrorType.WALLET_DISABLED]: t("ERROR_MESSAGE.WALLET_DISABLED")
    }, 
    addFakeTransaction: {
      ...generalErrorMessages
    },
    addFakeUserData: {
      ...generalErrorMessages,
      [ErrorType.USERS_ALREADY_IN_DATABASE]: t(
        "ERROR_MESSAGE.USERS_ALREADY_IN_DATABASE"
      )
    },
    addExpoToken: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EXPO_TOKEN]: t("ERROR_MESSAGE.INVALID_EXPO_TOKEN")
    },
    deleteExpoToken: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EXPO_TOKEN]: t("ERROR_MESSAGE.INVALID_EXPO_TOKEN")
    },
    onRampConfig: {
      ...generalErrorMessages
    },
    userContacts: {
      ...generalErrorMessages
    },
    addUserContact: {
      ...generalErrorMessages,
      [ErrorType.CONTACT_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.CONTACT_ALREADY_EXISTS"
      ),
      [ErrorType.OWN_ACCOUNT_CONTACT]: t("ERROR_MESSAGE.OWN_ACCOUNT_CONTACT"),
      [ErrorType.NICKNAME_ALREADY_USED]: t(
        "ERROR_MESSAGE.NICKNAME_ALREADY_USED"
      )
    },
    updateUserContact: {
      ...generalErrorMessages,
      [ErrorType.NICKNAME_ALREADY_USED]: t(
        "ERROR_MESSAGE.NICKNAME_ALREADY_USED"
      ),
      [ErrorType.CONTACT_NOT_FOUND]: t("ERROR_MESSAGE.CONTACT_NOT_FOUND")
    },
    sendDigitalCurrencyToExternalWallet: {
      ...generalErrorMessages,
      [ErrorType.UNKNOWN_WALLET_ERROR]: t("ERROR_MESSAGE.UNKNOWN_WALLET_ERROR"),
      [ErrorType.WALLET_NOT_AVAILABLE]: t("ERROR_MESSAGE.WALLET_NOT_AVAILABLE"),
      [ErrorType.EXTERNAL_SEND_INSIDE_THE_SYSTEM]: t(
        "ERROR_MESSAGE.EXTERNAL_SEND_INSIDE_THE_SYSTEM"
      ),
      [ErrorType.INVALID_AMOUNT_NEGATIVE_OR_ZERO]: t(
        "ERROR_MESSAGE.INVALID_AMOUNT_NEGATIVE_OR_ZERO"
      ),
      [ErrorType.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES]: t(
        "ERROR_MESSAGE.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES"
      ),
      [ErrorType.SUBACCOUNT_NOT_FOUND]: t("ERROR_MESSAGE.SUBACCOUNT_NOT_FOUND"),
      [ErrorType.WALLET_DISABLED]: t("ERROR_MESSAGE.WALLET_DISABLED"),
      [ErrorType.TRANSACTION_IS_ALREADY_PAID]: t("ERROR_MESSAGE.TRANSACTION_IS_ALREADY_PAID")
    },
    addUserBankAccount: {
      ...generalErrorMessages,
      [ErrorType.INVALID_BANK_ACCOUNT]: t("ERROR_MESSAGE.INVALID_BANK_ACCOUNT"),
      [ErrorType.BANK_ACCOUNT_ALREDY_ADDED]: t("ERROR_MESSAGE.BANK_ACCOUNT_ALREDY_ADDED"),
      [ErrorType.DUPLICATE_BANK_ACCOUNT]: t("ERROR_MESSAGE.DUPLICATE_BANK_ACCOUNT")
    },
    sellDigitalCurrency:{
      ...generalErrorMessages,
      [ErrorType.INSUFFICIENT_FUNDS]: t("ERROR_MESSAGE.INSUFFICIENT_FUNDS")
    },
    buyDigitalCurrency:{
      ...generalErrorMessages,
      [ErrorType.INSUFFICIENT_FUNDS]: t("ERROR_MESSAGE.INSUFFICIENT_FUNDS")
    },
    withdrawFiatInfo:{
      ...generalErrorMessages,
      [ErrorType.INSUFFICIENT_FUNDS]: t("ERROR_MESSAGE.INSUFFICIENT_FUNDS")
    },
    depositFiatInfo:{
      ...generalErrorMessages,
      [ErrorType.INSUFFICIENT_FUNDS]: t("ERROR_MESSAGE.INSUFFICIENT_FUNDS")
    },
    loadToken: {
      ...generalErrorMessages,
      [ErrorType.USER_BANK_ACCOUNT_NOT_FOUND]: t("ERROR_MESSAGE.USER_BANK_ACCOUNT_NOT_FOUND"),
      [ErrorType.INSUFFICIENT_FUNDS] : t("ERROR_MESSAGE.INSUFFICIENT_FUNDS"),
      [ErrorType.UNSUPPORTED_TOKEN] : t("ERROR_MESSAGE.UNSUPPORTED_TOKEN") , 
      [ErrorType.USER_NOT_FOUND]:t("ERROR_MESSAGE.USER_NOT_FOUND") ,
      [ErrorType.ISSUANCE_BANK_NOT_FOUND] : t("ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND") ,
      [ErrorType.ISSUANCE_WALLET_USER_DISABLE]:t("ERROR_MESSAGE.ISSUANCE_WALLET_USER_DISABLE"),
      [ErrorType.WALLET_DISABLED]: t("ERROR_MESSAGE.WALLET_DISABLED")
    },
    refundToken: {
      ...generalErrorMessages,
      [ErrorType.USER_BANK_ACCOUNT_NOT_FOUND]: t("ERROR_MESSAGE.USER_BANK_ACCOUNT_NOT_FOUND"),
      [ErrorType.INSUFFICIENT_FUNDS] : t("ERROR_MESSAGE.INSUFFICIENT_FUNDS"),
      [ErrorType.UNSUPPORTED_TOKEN] : t("ERROR_MESSAGE.UNSUPPORTED_TOKEN") , 
      [ErrorType.USER_NOT_FOUND]:t("ERROR_MESSAGE.USER_NOT_FOUND") ,
      [ErrorType.ISSUANCE_BANK_NOT_FOUND] : t("ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND") ,
      [ErrorType.ISSUANCE_WALLET_USER_DISABLE]:t("ERROR_MESSAGE.ISSUANCE_WALLET_USER_DISABLE"),
      [ErrorType.WALLET_DISABLED]: t("ERROR_MESSAGE.WALLET_DISABLED")
    },
    checkValidEmail :{
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]:t("ERROR_MESSAGE.USER_NOT_FOUND") ,
    },
    sendOTP: {
      ...generalErrorMessages,
      [ErrorType.PHONE_NUMBER_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_ALREADY_EXISTS"
      ),
      [ErrorType.UNKNOWN_TWILIO_ERROR]: t("ERROR_MESSAGE.UNKNOWN_TWILIO_ERROR"),
      [ErrorType.INVALID_PHONE_NUMBER]: t("ERROR_MESSAGE.INVALID_PHONE_NUMBER")
    },
    verifyOTP: {
      ...generalErrorMessages,
      [ErrorType.INCORRECT_CODE]: t("ERROR_MESSAGE.INCORRECT_CODE"),
      [ErrorType.VERIFICATION_NOT_FOUND]: t("ERROR_MESSAGE.INCORRECT_CODE"),
      [ErrorType.PHONE_NUMBER_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_ALREADY_EXISTS"
      ),
      [ErrorType.PHONE_NUMBER_DOES_NOT_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_DOES_NOT_EXISTS"
      ),
      [ErrorType.UNKNOWN_TWILIO_ERROR]: t("ERROR_MESSAGE.UNKNOWN_TWILIO_ERROR")
    },
    savePasscode: {
      ...generalErrorMessages,
      [ErrorType.SALT_KEY_NOT_FOUND]: t("ERROR_MESSAGE.SALT_KEY_NOT_FOUND"),
      [ErrorType.WALLET_DISABLED]: t("ERROR_MESSAGE.WALLET_DISABLED")
    },
    getDecryptedQR :{
      ...generalErrorMessages, 
      "Illegal base64 character 3a":t("Invalid QR Code"),
      [ErrorType.INVALID_QR]:t("Invalid QR Code")
     }
  }
}

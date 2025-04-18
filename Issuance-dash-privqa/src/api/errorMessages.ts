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
    [ErrorType.TOKEN_EXPIRED]: t("TOKEN_EXPIRED")
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
    getUser: {
      ...generalErrorMessages
    },
    sendPhoneOTP: {
      ...generalErrorMessages,
      [ErrorType.PHONE_NUMBER_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_ALREADY_EXISTS"
      ),
      [ErrorType.INVALID_PHONE_NUMBER]: t("ERROR_MESSAGE.INVALID_PHONE_NUMBER")
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
    merchant: {
      ...generalErrorMessages,
      [ErrorType.MERCHANT_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.MERCHANT_ALREADY_EXISTS"
      )
    },
    verifyPhoneOTP: {
      ...generalErrorMessages,
      [ErrorType.INCORRECT_CODE]: t("ERROR_MESSAGE.INCORRECT_CODE"),
      [ErrorType.PHONE_NUMBER_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_ALREADY_EXISTS"
      ),
      [ErrorType.PHONE_NUMBER_DOES_NOT_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_DOES_NOT_EXISTS"
      ),
      [ErrorType.UNKNOWN_TWILIO_ERROR]: t("ERROR_MESSAGE.UNKNOWN_TWILIO_ERROR")
    },
    verifyEmailOTPAndCreateUser: {
      ...generalErrorMessages,
      [ErrorType.UNVERIFIED_PHONE_NUMBER]: t(
        "ERROR_MESSAGE.UNVERIFIED_PHONE_NUMBER"
      ),
      [ErrorType.EMAIL_ALREADY_EXISTS]: t("ERROR_MESSAGE.EMAIL_ALREADY_EXISTS"),
      [ErrorType.INCORRECT_CODE]: t("ERROR_MESSAGE.INCORRECT_CODE"),
      [ErrorType.EMAIL_DOES_NOT_EXISTS]: t(
        "ERROR_MESSAGE.EMAIL_DOES_NOT_EXISTS"
      )
    },
    merchantTransaction: {
      ...generalErrorMessages,
      [ErrorType.TRANSACTION_NOT_FOUND]: t(
        "ERROR_MESSAGE.TRANSACTION_NOT_FOUND"
      )
    },
    merchantTransactions: {
      ...generalErrorMessages
    },
    refundInitiateWebLink: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL")
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
      [ErrorType.SUBACCOUNT_NOT_FOUND]: t("ERROR_MESSAGE.SUBACCOUNT_NOT_FOUND")
    },
    exchangeRates: {
      ...generalErrorMessages
    },
    inviteUser: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.INVALID_ROLE]: t("ERROR_MESSAGE.INVALID_ROLE"),
      [ErrorType.ISSUANCE_BANK_NOT_FOUND]: t(
        "ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND"
      )
    },
    p2pTransaction: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND")
    },
    generateAPIKey: {
      ...generalErrorMessages,
      [ErrorType.ISSUANCE_BANK_NOT_FOUND]: t(
        "ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND"
      )
    },
    issuanceDetails: {
      ...generalErrorMessages,
      // Note, don't translate this, it is used to check if a merchant exists
      [ErrorType.ISSUANCE_BANK_NOT_FOUND]:
        "ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND"
    },
    userList: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.ISSUANCE_BANK_NOT_FOUND]: t(
        "ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND"
      ),
      [ErrorType.INVITATION_NOT_FOUND]: t("ERROR_MESSAGE.INVITATION_NOT_FOUND")
    },
    enableUser: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND")
    },
    disableUser: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND")
    },
    getRefundForm: {
      ...generalErrorMessages
    },
    refundExchangeRates: {
      ...generalErrorMessages,
      [ErrorType.TOKEN_EXPIRED]: t("TOKEN_EXPIRED")
    },
    issuanceBankVerify: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.ISSUANCE_BANK_NOT_FOUND]: t(
        "ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND"
      )
    },
    userVerifyReset: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User.")
    },
    userVerifyResetIssuance: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.ISSUANCE_BANK_NOT_FOUND]: t(
        "ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND"
      )
    },
    walletUserList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    enableDisableWalletUser: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    walletSummary: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    walletBalance: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    walletRefund: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    walletTransactionGraph: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    walletSummaryGraph: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    updateIssuanceDetails: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    getWalletFeeType: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    getTransactionType: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    walletFeeConfig: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    addWalletFeeConfig: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.WALLET_FEE_CONFIG_ALREADY_ADDED]: t(
        "ERROR_MESSAGE.WALLET_FEE_CONFIG_ALREADY_ADDED"
      )
    },
    editWalletFeeConfig: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.WALLET_FEE_CONFIG_ALREADY_ADDED]: t(
        "ERROR_MESSAGE.WALLET_FEE_CONFIG_ALREADY_ADDED"
      )
    },
    addTransactionLimitConfig: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.TRANSACTION_LIMIT_ALREADY_ADDED]: t(
        "ERROR_MESSAGE.TRANSACTION_LIMIT_ALREADY_ADDED"
      ),
      [ErrorType.NO_DATA_SENT]: t("ERROR_MESSAGE.NO_DATA_SENT")
    },
    editTransactionLimitConfig: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.TRANSACTION_LIMIT_ALREADY_ADDED]: t(
        "ERROR_MESSAGE.TRANSACTION_LIMIT_ALREADY_ADDED"
      ),
      [ErrorType.NO_DATA_SENT]: t("ERROR_MESSAGE.NO_DATA_SENT")
    },
    getTransactionLimitConfig: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    fiatExchangeRates: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    conversionRates: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    addConversionRates: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.CONVERSION_RATE_ALREADY_ADDED]: t(
        "ERROR_MESSAGE.CONVERSION_RATE_ALREADY_ADDED"
      )
    },
    editConversionRates: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.CONVERSION_RATE_ALREADY_ADDED]: t(
        "ERROR_MESSAGE.CONVERSION_RATE_ALREADY_ADDED"
      )
    },
    conversionRatesAdjustment: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    addConversionRatesAdjustment: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    editConversionRatesAdjustment: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    getIndustryTypeList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    getFiatCurrencyList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    saveInstitutionDetails: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    getInstitutionDetails: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    getCountryList: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    getAllLanguages: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    addLanguage: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.LANGUAGE_ALREADY_ADDED]: t(
        "ERROR_MESSAGE.LANGUAGE_ALREADY_ADDED"
      ),
      [ErrorType.LANGUAGE_MAPPED_WITH_INSTITUTIONS]: t(
        "ERROR_MESSAGE.LANGUAGE_MAPPED_WITH_INSTITUTIONS"
      )
    },
    makeLanguageDefault: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    getMappedLanguages: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    },
    mappedLanguageInstitution: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST")
    }
  }
}

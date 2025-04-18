import { useTranslation } from "react-i18next";

import { EndpointKey } from "./constants";
import { ErrorType } from "./errorTypes";

const useGeneralErrorMessages: () => { [key in ErrorType]?: string } = () => {
  const { t } = useTranslation();
  return {
    [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    [ErrorType.UNAUTHORIZED]: t("ERROR_MESSAGE.UNAUTHORIZED"),
    [ErrorType.FORBIDDEN]: t("ERROR_MESSAGE.FORBIDDEN"),
    [ErrorType.NOT_FOUND]: t("ERROR_MESSAGE.NOT_FOUND"),
    [ErrorType.INTERNAL_SERVER_ERROR]: t("ERROR_MESSAGE.INTERNAL_SERVER_ERROR"),
    [ErrorType.INVALID_INPUT_FORMAT]: t("ERROR_MESSAGE.INVALID_INPUT_FORMAT"),
    [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND"),
    [ErrorType.TOKEN_EXPIRED]: t("TOKEN_EXPIRED"),
    [ErrorType.INVALID_WALLET_ADDRESS]: t("INVALID_WALLET_ADDRESS"),
    [ErrorType.UNKNOWN_WALLET_ERROR]: t("UNKNOWN_WALLET_ERROR"),
    [ErrorType.REFUND_ALREADY_EXISTS_ERROR]: t("REFUND_ALREADY_EXISTS_ERROR"),
    [ErrorType.TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT]:
      t("Amount should be greater than minimum limit in cash mode"),
    [ErrorType.AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE]: t(
      "After deducting fee final amount is zero or negative"
    ),
  };
};

export const useErrorMessages: () => {
  [key in EndpointKey]: {
    [key in ErrorType]?: string;
  };
} = () => {
  const { t } = useTranslation();
  const generalErrorMessages = useGeneralErrorMessages();
  return {
    getUser: {
      ...generalErrorMessages,
    },
    sendPhoneOTP: {
      ...generalErrorMessages,
      [ErrorType.PHONE_NUMBER_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.PHONE_NUMBER_ALREADY_EXISTS"
      ),
      [ErrorType.INVALID_PHONE_NUMBER]: t("ERROR_MESSAGE.INVALID_PHONE_NUMBER"),
    },
    userDetailsAndEmailOTP: {
      ...generalErrorMessages,
      [ErrorType.UNVERIFIED_PHONE_NUMBER]: t(
        "ERROR_MESSAGE.UNVERIFIED_PHONE_NUMBER"
      ),
      [ErrorType.EMAIL_ALREADY_EXISTS]: t("ERROR_MESSAGE.EMAIL_ALREADY_EXISTS"),
      [ErrorType.FRAUDULENT_USER]: t("ERROR_MESSAGE.FRAUDULENT_USER"),
      [ErrorType.UNKNOWN_SEON_ERROR]: t("ERROR_MESSAGE.UNKNOWN_SEON_ERROR"),
    },
    merchant: {
      ...generalErrorMessages,
      [ErrorType.MERCHANT_ALREADY_EXISTS]: t(
        "ERROR_MESSAGE.MERCHANT_ALREADY_EXISTS"
      ),
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
      [ErrorType.UNKNOWN_TWILIO_ERROR]: t("ERROR_MESSAGE.UNKNOWN_TWILIO_ERROR"),
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
      ),
    },
    userBalances: {
      ...generalErrorMessages,
    },
    merchantTransaction: {
      ...generalErrorMessages,
      [ErrorType.TRANSACTION_NOT_FOUND]: t(
        "ERROR_MESSAGE.TRANSACTION_NOT_FOUND"
      ),
    },
    merchantTransactions: {
      ...generalErrorMessages,
    },
    refundTransactions: {
      ...generalErrorMessages,
    },
    refundAcceptRejectTransactions: {
      ...generalErrorMessages,
      [ErrorType.INVALID_WALLET_ADDRESS]: t(
        "ERROR_MESSAGE.INVALID_WALLET_ADDRESS"
      ),
      [ErrorType.UNKNOWN_WALLET_ERROR]: t("ERROR_MESSAGE.UNKNOWN_WALLET_ERROR"),
      [ErrorType.INSUFFICIENT_FUNDS]: t("ERROR_MESSAGE.INSUFFICIENT_FUNDS"),
      [ErrorType.EXTERNAL_SEND_INSIDE_THE_SYSTEM]: t(
        "ERROR_MESSAGE.EXTERNAL_SEND_INSIDE_THE_SYSTEM"
      ),
      [ErrorType.WALLET_NOT_AVAILABLE]: t("ERROR_MESSAGE.WALLET_NOT_AVAILABLE"),
    },
    refundInitiateWebLink: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.INVALID_AMOUNT_NEGATIVE_OR_ZERO]: t(
        "ERROR_MESSAGE.INVALID_AMOUNT_NEGATIVE_OR_ZERO"
      ),
    },
    refundSubmitFormWithAuth: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    userTransaction: {
      ...generalErrorMessages,
      [ErrorType.TRANSACTION_NOT_FOUND]: t(
        "ERROR_MESSAGE.TRANSACTION_NOT_FOUND"
      ),
    },
    userTransactions: {
      ...generalErrorMessages,
    },
    addUserTransaction: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND"),
      [ErrorType.SUBACCOUNT_NOT_FOUND]: t("ERROR_MESSAGE.SUBACCOUNT_NOT_FOUND"),
      [ErrorType.INSUFFICIENT_FUNDS]: t("ERROR_MESSAGE.INSUFFICIENT_FUNDS"),
    },
    exchangeRates: {
      ...generalErrorMessages,
    },
    inviteUser: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.INVALID_ROLE]: t("ERROR_MESSAGE.INVALID_ROLE"),
      [ErrorType.MERCHANT_NOT_FOUND]: t("ERROR_MESSAGE.MERCHANT_NOT_FOUND"),
    },
    p2pTransaction: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND"),
      [ErrorType.INSUFFICIENT_FUNDS]: t("ERROR_MESSAGE.INSUFFICIENT_FUNDS"),
      [ErrorType.INVALID_AMOUNT_NEGATIVE_OR_ZERO]: t(
        "ERROR_MESSAGE.INVALID_AMOUNT_NEGATIVE_OR_ZERO"
      ),
    },
    generateAPIKey: {
      ...generalErrorMessages,
      [ErrorType.MERCHANT_NOT_FOUND]: t("ERROR_MESSAGE.MERCHANT_NOT_FOUND"),
    },
    merchantDetails: {
      ...generalErrorMessages,
      // Note, don't translate this, it is used to check if a merchant exists
      [ErrorType.MERCHANT_NOT_FOUND]: "ERROR_MESSAGE.MERCHANT_NOT_FOUND",
    },
    userList: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.MERCHANT_NOT_FOUND]: t("ERROR_MESSAGE.MERCHANT_NOT_FOUND"),
      [ErrorType.INVITATION_NOT_FOUND]: t("ERROR_MESSAGE.INVITATION_NOT_FOUND"),
    },
    enableUser: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND"),
    },
    disableUser: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
      [ErrorType.USER_NOT_FOUND]: t("ERROR_MESSAGE.USER_NOT_FOUND"),
    },
    getRefundForm: {
      ...generalErrorMessages,
    },
    submitRefundForm: {
      ...generalErrorMessages,
      [ErrorType.TRANSACTION_NOT_MATCHING_REFUND_USER_NAME]: t(
        "TRANSACTION_NOT_MATCHING_REFUND_USER_NAME"
      ),
      [ErrorType.REFUND_ALREADY_EXISTS_ERROR]: t("REFUND_ALREADY_EXISTS_ERROR"),
      [ErrorType.TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT]:
        t(
          "TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT"
        ),
      [ErrorType.AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE]: t(
        "AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE"
      ),
    },
    refundExchangeRates: {
      ...generalErrorMessages,
      [ErrorType.TOKEN_EXPIRED]: t("TOKEN_EXPIRED"),
    },
    recentPayment: {
      ...generalErrorMessages,
      [ErrorType.INVALID_EMAIL]: t("ERROR_MESSAGE.INVALID_EMAIL"),
    },
    atmWithDraw: {
      ...generalErrorMessages,
      [ErrorType.INSUFFICIENT_FUNDS]: t("Insufficient Funds."),
      [ErrorType.INVALID_AMOUNT_NEGATIVE_OR_ZERO]: t("Amount should be > 0."),
    },
    userVerify: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
    },
    userVerifyReset: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
    },
    initiateWebLinkRefund: {
      ...generalErrorMessages,
      [ErrorType.TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT]:
        t(
          "TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT"
        ),
    },
    approve: {
      ...generalErrorMessages,
      [ErrorType.AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE]: t(
        "AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE"
      ),
    },
    refundFormFields: {
      ...generalErrorMessages,
    },

    addConversionRates: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getIndustryTypeList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getAggregatorTree: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getAggregatorById: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getInstitutionById: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getMerchantGroupById: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getMerchantById: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getSubMerchantById: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getOutletById: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },

    getFiatCurrencyList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    saveAggregatorDetails: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getAggregatorDetails: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateAggregatorDetails: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    deleteAggregator: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createInstitution: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateInstitutionDetails: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    institutionDelete: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createMerchantGroup: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateMerchantGroup: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    merchantGroupDelete: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getMerchantGroupList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getInstitutionList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createMerchantAcquirer: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateMerchantAcquirer: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getMerchantAcquirerList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    merchantDelete: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createSubMerchantAcquirer: {
      ...generalErrorMessages,
    },
    getSubMerchantAcquirerList: {
      ...generalErrorMessages,
    },
    updateSubMerchantAcquirer: {
      ...generalErrorMessages,
    },
    subMerchantDelete: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createOutlet: {
      ...generalErrorMessages,
    },
    outletList: {
      ...generalErrorMessages,
    },
    updateOutlet: {
      ...generalErrorMessages,
    },
    outletDelete: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updatePos: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    deletePos: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    posListDetails: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    editPosError: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    posList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    levelList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createLevel: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateLevel: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    moduleList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createModule: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateModule: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    uploadAggregator: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    uploadInstitution: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    saveAggregatorDraft: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateAggregatorDraft: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },

    updateInstitutionDraft: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },

    saveInstitution: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    saveMerchantGroup: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateMerchantGroupDraft: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    saveMerchantAcquirer: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateMerchantAcquirerDraft: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateSubMerchantAcquirerDraft: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    saveSubMerchantAcquirer: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    saveOutlet: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateOutletDraft: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createPos: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createRole: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    roleList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateRole: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    createUser: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    userManagementList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    updateUser: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    deActivateUser: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getDepartments: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getRoles: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getRolesByUsers: {
      ...generalErrorMessages,
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    moduleListTree: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    userRoleList: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    getUserByUUID: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    resetPassword: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    login: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
    resetPasswordLinkByEmail: {
      ...generalErrorMessages,
      [ErrorType.USER_NOT_FOUND]: t("Inavlid User."),
      [ErrorType.BAD_REQUEST]: t("ERROR_MESSAGE.BAD_REQUEST"),
    },
  };
};

import { TransactionFilters } from "./models"
import { TokenToAmount, FiatTokenToAmount } from "~/constants/types"
export enum EndpointKey {
  SEND_PHONE_OTP = "sendPhoneOTP",
  VERIFY_PHONE_OTP = "verifyPhoneOTP",
  SEND_PHONE_OTP_COMMON = "sendOTP",
  VERIFY_PHONE_OTP_COMMON = "verifyOTP",
  USER_DETAILS_SEND_EMAIL_OTP = "userDetailsAndEmailOTP",
  VERIFY_EMAIL_OTP_CREATE_USER = "verifyEmailOTPAndCreateUser",
  GET_USER = "getUser",
  DELETE_USER = "deleteUser",
  GET_USER_BY_EMAIL_OR_MOBILE = "getUserByEmailOrMobile",
  DELETE_CONTACT_OF_USER = "deleteContactOfUser",
  USER_BALANCES = "userBalances",
  USER_CONTACTS = "userContacts",
  ADD_USER_CONTACT = "addUserContact",
  UPDATE_USER_CONTACT = "updateUserContact",
  USER_TRANSACTION = "userTransaction",
  USER_TRANSACTIONS = "userTransactions",
  ADD_TRANSACTION = "addUserTransaction",
  ADD_EXPO_TOKEN = "addExpoToken",
  DELETE_EXPO_TOKEN = "deleteExpoToken",
  ADD_FAKE_TRANSACTION = "addFakeTransaction",
  ADD_FAKE_USER_DATA = "addFakeUserData",
  WITHDRAW_FIAT = "withdrawFiatInfo",
  DEPOSIT_FIAT = "depositFiatInfo",
  ON_RAMP_CONFIG = "onRampConfig",
  EXCHANGE_RATES = "exchangeRates",
  P2P_FEE = "p2pFee",
  SEND_DIGITAL_CRTPTO_TO_EXTERNAL_DATA = "sendDigitalCurrencyToExternalWallet",
  SEND_PUSH_NOTIFICATION_PAYMENT = "sendPushNotificationPayment",
  ADDRESSES = "addresses",
  GET_PUSH_NOTIFICATION_DATA = "getPushNotificationData",
  UPDATE_PAYMENT_REQUEST = "updatePaymentRequest",
  SAVE_PAYMENT_INFO = "savePaymentInfo",
  USER_KYC = "kyc",
  CHECK_VALID_EMAIL = "checkValidEmail",
 SELL_DIGITAL_CURRENCY="sellDigitalCurrency",
  BUY_DIGITAL_CURRENCY="buyDigitalCurrency",
  FIAT_BALANCES = "getFiatBalance",
  ADD_USER_BANK_ACCOUNT = "addUserBankAccount",
  TOPUP = "loadToken",
  REFUND = "refundToken",
  FIAT_EXCHANGE_RATE = "fiatExchangeRates",
  ENCRYPT_STRING = "getEncryptedQR",
  DECRYPT_STRING = "getDecryptedQR",
  GET_TRANSACTION_VALIDATION="getTransactionValidation",
  GET_PROFILE_DETAILS = "getProfileDetails",
  SAVE_PASSCODE = "savePasscode",
  GET_PASSCODE_TITLE = "getPasscodeTitle"
}

export const useApi: () => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  [key in EndpointKey]: (...args: any[]) => string
} = () => {
  return {
    sendPhoneOTP: () => "user/registration/phone/",
    verifyPhoneOTP: () => "user/registration/phone/verify/",
    userDetailsAndEmailOTP: () => "user/registration/details/",
    verifyEmailOTPAndCreateUser: () => "user/registration/verify-and-create/",
    getUser: (query: string) => `user?${query}`,
    deleteUser: () => "user/",
    userBalances: () => "user/balances",
    userContacts: (query: string) => `user/contacts?${query}`,
    addUserContact: () => "user/contact",
    updateUserContact: () => "user/contact",
    userTransaction: (id: string) => `user/transactions/${id}`,
    userTransactions: (query: string) => `user/transactions?${query}`,
    addUserTransaction: () => "user/account/p2p_transaction",
    addExpoToken: () => "user/expoToken",
    deleteExpoToken: () => "user/expoToken",
    addFakeTransaction: () => "addFakeTransaction",
    addFakeUserData: () => "addFakeUserData",
    withdrawFiat: () => "withdrawFiat",
    depositFiat: () => "depositFiat",
    loadToken: () => "issuance/user/loadToken",
    refundToken: () => "issuance/user/refundToken",
    onRampConfig: () => "v1/config",
    exchangeRates: (fiat: string) => `v1/config/exchangeRates?from=${fiat}`,
    fiatExchangeRates: (fiat: string) => `issuanceWallet/fiatExchangeRates?from=${fiat}`,
    p2pFee: () => "v1/config/fee",
    sendDigitalCurrencyToExternalWallet: () =>
      "user/account/sendDigitalCurrencyToExternalWallet",
    sendPushNotificationPayment: () => `user/sendPushNotificationPayment`,
    addresses: () => "user/account/addresses",
    getPushNotificationData: (query: string) => `user/getPushNotificationData`,
    savePaymentInfo: (query: string) => `user/savePaymentInfo?email=${query}`,
    depositFiatInfo: (query: string) => `depositFiat?countryCode=${query}`,
    withdrawFiatInfo: (query: string) => `withdrawFiat?countryCode=${query}`,
    kyc: () => "user/kyc",
    updatePaymentRequest: () => "user/updatePaymentRequest",
    sellDigitalCurrency:() => "sellDigitalCurrency",
   // buyDigitalCurrency:() => "buyDigitalCurrency",
    buyDigitalCurrency:() => "buyDigitalCurrency",
   // buyDigitalCurrency:() => "buyDigitalCurrency?fiatAmount=500&fiatAsset=AED&digitalAsset=ETH",
    getFiatBalance: () => "getFiatBalance",
    addUserBankAccount:  (query: string) => `addUserBankAccount?countryCode=${query}`,
    getUserByEmailOrMobile: (query: string) => `user/getUserByEmailOrMobile?${query}`,
    checkValidEmail: (query: string) => `api/v1/checkEmailExists?email=${query}`,
    deleteContactOfUser: () => "user/contact",
    getEncryptedQR:() => "user/getEncryptedQR",
    getDecryptedQR:() => "user/getDecryptedQR",
    getTransactionValidation:(query: string)=>`issuanceWallet/getTransactionValidation?type=${query}`,
    getProfileDetails: () => "user/getProfileDetails",
    sendOTP: () => "user/common/sendOTP/",
    verifyOTP: () => "user/common/verifyOTP/",
    savePasscode: () => "user/savePasscode/",
    getPasscodeTitle: () => "user/getPasscodeTitle"
  } as const
}

export const INITIAL_TRANSACTION_FILTERS: TransactionFilters = {
  dateFrom: undefined,
  dateTo: undefined,
  direction: "",
  status: [],
  type: [],
  // asset: [] // done for geidea only
}
export const RESET_TRANSACTION_FILTERS: TransactionFilters = {
  dateFrom: new Date(),
  dateTo: new Date(),
  direction: "",
  status: [],
  type: [],
  asset: []
}


// TODO make this come from the backend?
export const AssetFractionDigits: TokenToAmount = {
  BTC: 8,
  ETH: 8,
  USDT: 6,
  WTK: 6,
  SART:2
}

export const FiatAssetFractionDigits: FiatTokenToAmount = {
  USD: 2,
  EUR: 2,
  INR: 2,
  IDR: 1,
  SGD: 2,
  GBP: 2,
  PKR: 1,
  PHP: 1,
  AED: 2,
  MYR: 2,
  SART:2
}
export enum TransactionTypeControl {
  INITIAL_LOADING = "TTC_001",
  SUBSEQUENT_LOADING = "TTC_002",
  PURCHASE = "TTC_003",
  MERCHANT_OFFLINE = "TTC_004",
  CUSTOMER_OFFLINE = "TTC_005",
  UNSPENT_DIGITAL_CURRENCY_REFUND = "TTC_006",
  P2P_TRANSFER = "TTC_007"
}

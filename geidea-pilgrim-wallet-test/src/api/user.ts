/* eslint-disable @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any */
import { useContext, useState } from "react"
import { EXCHANGE_RATES_REFRESH_TIME } from "~/constants"

import {
  AddFakeTransactionData,
  SaveUserContactData,
  BalancesData,
  ContactsData,
  ExpoTokenData,
  Transaction,
  TransactionsData,
  UserData,
  UserDetailsData,
  VerifyEmailOTPData,
  AddTransactionData,
  ExternalTransferData,
  addressesData,
  SendNotificationForRecievingPaymentData,
  notificationListData,
  NotificationListData,
  userKyc,
  SellDigitalCurrencyData,
  FiatBalanceData,
  WithdrawOrDepositFiat,
  BuyDigitalCurrencyData,
  UserBankAccData,
  TopupOrRefundSart,
  GetDecryptedData, 
  GetEncryptedData,
  fiatExchangeDataSart,
  CheckValidEmailData, 
  UserByEmailOrMobileData, 
  DeleteContactOfUser, 
  transactionValidtionConfigData, userProfileDetail, savePasscodeHashData, passcodeDetail
} from "./models"
import { EndpointKey, useApi } from "./constants"

import { useGet, useSet } from "./index"

import {
  requestResetPasswordCodeAsync,
  submitResetPasswordCodeAsync,
  signInAsync,
  signOutAsync,
  changePasswordAsync
} from "~/auth/AuthManager"
import { NotificationsContext } from "~/context"

const useApiCallStates = () => {
  const [isLoading, setIsLoading] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)
  const [error, setError] = useState<Error | null>(null)
  const reset = () => {
    setIsSuccess(false)
    setIsLoading(false)
    setError(null)
  }
  return {
    isLoading,
    setIsLoading,
    isSuccess,
    setIsSuccess,
    error,
    setError,
    reset
  }
}

export const useGenericApiCall = (
  apiToCall: (...params: any) => Promise<any>
) => {
  const {
    isLoading,
    setIsLoading,
    isSuccess,
    setIsSuccess,
    error,
    setError,
    reset
  } = useApiCallStates()

  return {
    mutate: async (...params: any) => {
      try {
        setIsLoading(true)
        const response = await apiToCall(...params)
        setIsSuccess(true)
        return response
      } catch (e) {
        setError(e)
      } finally {
        setIsLoading(false)
      }
    },
    isLoading,
    isSuccess,
    error,
    reset
  }
}
export const useCheckValidEmail = (query: string,hitAPI?: boolean) =>
  useGet<string>(
    [EndpointKey.CHECK_VALID_EMAIL,query],
    useApi().checkValidEmail(query),
    { enabled: hitAPI },
    EndpointKey.CHECK_VALID_EMAIL
)
export const useSignIn = () => {
  return useGenericApiCall(signInAsync)
}

export const useUserDetailsAndEmailOTP = () =>
  useSet<UserDetailsData>(
    EndpointKey.USER_DETAILS_SEND_EMAIL_OTP,
    useApi().userDetailsAndEmailOTP(),
    "POST",
    {},
    EndpointKey.USER_DETAILS_SEND_EMAIL_OTP
  )

export const useVerifyEmailOTPAndCreateUser = () =>
  useSet<VerifyEmailOTPData>(
    EndpointKey.VERIFY_EMAIL_OTP_CREATE_USER,
    useApi().verifyEmailOTPAndCreateUser(),
    "POST",
    {},
    EndpointKey.VERIFY_EMAIL_OTP_CREATE_USER
  )

export const useSignOut = () => {
  const { isLoading, setIsLoading, isSuccess, setIsSuccess, error, setError } =
    useApiCallStates()
  // const { expoPushToken, removeExpoPushToken } =
  //   useContext(NotificationsContext)
  const { mutate: deleteExpoToken } = useDeleteExpoToken()

  return {
    mutate: async () => {
      try {
        setIsLoading(true)
        await signOutAsync()
        // if (expoPushToken) {
        //   await deleteExpoToken({ expoToken: expoPushToken })
        // }
        // removeExpoPushToken()
        setIsSuccess(true)
      } catch (e) {
        setError(e)
      } finally {
        setIsLoading(false)
      }
    },
    isLoading,
    isSuccess,
    error
  }
}

export const useChangePassword = () => {
  return useGenericApiCall(changePasswordAsync)
}

export const useRequestResetPasswordCode = () => {
  return useGenericApiCall(requestResetPasswordCodeAsync)
}

export const useSubmitResetPasswordCode = () => {
  return useGenericApiCall(submitResetPasswordCodeAsync)
}

export const useUser = (query: string, hitAPI?: boolean) =>
  useGet<UserData>(
    [EndpointKey.GET_USER, query],
    useApi().getUser(query || ""),
    { enabled: hitAPI },
    EndpointKey.USER_KYC
  )
export const useDeleteUser = () =>
  useSet<string>(
    EndpointKey.DELETE_USER,
    useApi().deleteUser(),
    "DELETE",
    {},
    EndpointKey.DELETE_USER
  )

  export const useSellDigitalCurrency = () =>
  useSet<SellDigitalCurrencyData>(
    EndpointKey.SELL_DIGITAL_CURRENCY,
    useApi().sellDigitalCurrency(),
    "POST",
    {},
    []
  )

  export const useBuyDigitalCurrency = () =>
  useSet<BuyDigitalCurrencyData>(
    EndpointKey.BUY_DIGITAL_CURRENCY,
    useApi().buyDigitalCurrency(),
    "POST",
    {},
    []
  )
export const useUserBalances = () =>
  useGet<BalancesData>(EndpointKey.USER_BALANCES, useApi().userBalances(), {}
)

  export const useGetFiatBalance = () =>
  useGet<FiatBalanceData>(EndpointKey.FIAT_BALANCES,useApi().getFiatBalance(),{
  })
export const useAddresses = () =>
  useGet<addressesData>(EndpointKey.ADDRESSES, useApi().addresses(), {})

export const useUserKYC = (hitKycApi?: boolean) =>
  // console.log("useUserKYC query" , query !== "" && query != undefined  )
  useGet<userKyc>(EndpointKey.USER_KYC, useApi().kyc(), { enabled: hitKycApi })

export const useUserContacts = (query?: string) =>
  useGet<ContactsData>(
    [EndpointKey.USER_CONTACTS, query],
    useApi().userContacts(query || ""),
    { keepPreviousData: true },
    "cognitoUsername"
  )

export const useGetPushNotificationData = (query?: string) =>
  useGet<NotificationListData>(
    [EndpointKey.GET_PUSH_NOTIFICATION_DATA, query],
    useApi().getPushNotificationData(),
    {},
    ""
  )

export const useAddUserContact = () =>
  useSet<SaveUserContactData>(
    EndpointKey.ADD_USER_CONTACT,
    useApi().addUserContact(),
    "POST",
    {},
    [EndpointKey.ADD_USER_CONTACT, EndpointKey.USER_CONTACTS]
  )

export const useUpdateUserContact = () =>
  useSet<SaveUserContactData>(
    EndpointKey.UPDATE_USER_CONTACT,
    useApi().updateUserContact(),
    "PATCH",
    {},
    [EndpointKey.UPDATE_USER_CONTACT, EndpointKey.USER_CONTACTS]
  )

export const useUserTransaction = (id: string) =>
  useGet<Transaction>(
    [EndpointKey.USER_TRANSACTION, id],
    useApi().userTransaction(id),
    { keepPreviousData: true }
  )

export const useUserTransactions = (query?: string) =>
  useGet<TransactionsData>(
    [EndpointKey.USER_TRANSACTIONS, query],
    useApi().userTransactions(query || ""),
    { keepPreviousData: true },
    "uuid"
  )

export const useAddTransaction = () =>
  useSet<AddTransactionData>(
    EndpointKey.ADD_TRANSACTION,
    useApi().addUserTransaction(),
    "POST",
    {},
    [
      EndpointKey.ADD_TRANSACTION,
      EndpointKey.USER_BALANCES,
      EndpointKey.USER_TRANSACTIONS,
     // EndpointKey.GET_PROFILE_DETAILS
    ]
  )

export const useSendDigitalCurrencyToExternalWallet = () =>
  useSet<ExternalTransferData>(
    EndpointKey.SEND_DIGITAL_CRTPTO_TO_EXTERNAL_DATA,
    useApi().sendDigitalCurrencyToExternalWallet(),
    "POST",
    {},
    [
      EndpointKey.SEND_DIGITAL_CRTPTO_TO_EXTERNAL_DATA,
      EndpointKey.USER_BALANCES,
      EndpointKey.USER_TRANSACTIONS,
     // EndpointKey.GET_PROFILE_DETAILS
    ]
  )

export const useSendPushNotificationPayment = () =>
  useSet<SendNotificationForRecievingPaymentData>(
    EndpointKey.SEND_PUSH_NOTIFICATION_PAYMENT,
    useApi().sendPushNotificationPayment(),
    "POST",
    {},
    []
  )


export const useSavePaymentInfo = (requesterEmail: string) =>
  useSet<SendNotificationForRecievingPaymentData>(
    EndpointKey.SAVE_PAYMENT_INFO,
    useApi().savePaymentInfo(requesterEmail),
    "POST",
    {},
    []
  )

export const useUpdatePaymentRequest = () =>
  useSet<SendNotificationForRecievingPaymentData>(
    EndpointKey.UPDATE_PAYMENT_REQUEST,
    useApi().updatePaymentRequest(),
    "POST",
    {},
    []
  )

export const useAddExpoToken = () =>
  useSet<ExpoTokenData>(
    EndpointKey.ADD_EXPO_TOKEN,
    useApi().addExpoToken(),
    "POST",
    {},
    EndpointKey.ADD_EXPO_TOKEN
  )

export const useDeleteExpoToken = () =>
  useSet<ExpoTokenData>(
    EndpointKey.DELETE_EXPO_TOKEN,
    useApi().deleteExpoToken(),
    "DELETE",
    {},
    EndpointKey.DELETE_EXPO_TOKEN
  )

export const useAddFakeTransaction = () =>
  useSet<AddFakeTransactionData>(
    EndpointKey.ADD_FAKE_TRANSACTION,
    useApi().addFakeTransaction(),
    "POST",
    {},
    [
      EndpointKey.ADD_FAKE_TRANSACTION,
      EndpointKey.USER_BALANCES,
      EndpointKey.USER_TRANSACTIONS
    ]
  )

export const useAddFakeUserData = () =>
  useSet<string>(
    EndpointKey.ADD_FAKE_USER_DATA,
    useApi().addFakeUserData(),
    "POST",
    {},
    EndpointKey.ADD_FAKE_USER_DATA
  )

// added by swati
export const useWithdrawFiat = (query?: string) =>
  useSet<WithdrawOrDepositFiat>(
    [EndpointKey.WITHDRAW_FIAT, query],
    useApi().withdrawFiatInfo(query),
    "POST",
    {},
    EndpointKey.WITHDRAW_FIAT
  )

// added by swati
export const useDepositFiat = (query?: string) =>
  useSet<WithdrawOrDepositFiat>(
    [EndpointKey.DEPOSIT_FIAT, query],
    useApi().depositFiatInfo(query),
    "POST",
    {},
    EndpointKey.DEPOSIT_FIAT
  )


// added by swati
export const useGetFiatExchangeRate = (fiat: string) =>
useGet<fiatExchangeDataSart>(
  [EndpointKey.FIAT_EXCHANGE_RATE, fiat],
  useApi().fiatExchangeRates(fiat),
  {      
    staleTime: EXCHANGE_RATES_REFRESH_TIME
  }
  )

export const useTopupSart = () =>
useSet<TopupOrRefundSart>(
  EndpointKey.TOPUP,
  useApi().loadToken(),
  "POST",
  {},
  [ 
     EndpointKey.TOPUP,
     EndpointKey.USER_BALANCES,
     EndpointKey.USER_TRANSACTIONS,
     //EndpointKey.GET_PROFILE_DETAILS
  ]
  )
  

  export const useRefundSart = () =>
  useSet<TopupOrRefundSart>(
    EndpointKey.REFUND,
    useApi().refundToken(),
    "POST",
    {},
    [ 
      EndpointKey.REFUND,
      EndpointKey.USER_BALANCES,
      EndpointKey.USER_TRANSACTIONS
    ]
  )

  export const useAddUserBankAccount = (query?: string) =>
  useSet<UserBankAccData>(
    [EndpointKey.ADD_USER_BANK_ACCOUNT, query],
    useApi().addUserBankAccount(query),
    "POST",
    {},
    []
  )

  export const useUserByEmailOrMobile = (query: string) =>
useGet<UserByEmailOrMobileData>(
  [EndpointKey.GET_USER_BY_EMAIL_OR_MOBILE, query],
  useApi().getUserByEmailOrMobile(query || ""),
  {},
  EndpointKey.GET_USER_BY_EMAIL_OR_MOBILE
)


export const useDeleteContactOfUser = () =>
useSet<DeleteContactOfUser>(
  EndpointKey.DELETE_CONTACT_OF_USER,
  useApi().deleteContactOfUser(),
  "DELETE",
  {},
 [EndpointKey.USER_CONTACTS]
)


// added by swati
export const useGetDecryptedData = () =>
  useSet<GetDecryptedData>(
    EndpointKey.DECRYPT_STRING,
    useApi().getDecryptedQR(),
    "POST",
    {},
    []
  )


  // added by swati
export const useGetEncryptedData = () =>
useSet<GetEncryptedData>(
  EndpointKey.ENCRYPT_STRING,
  useApi().getEncryptedQR(),
  "POST",
  {},
  []
)

export const useTransactionValidationConfig = (query: string , hitAPI?: boolean) =>
  useGet<transactionValidtionConfigData>(
    EndpointKey.GET_TRANSACTION_VALIDATION,
    useApi().getTransactionValidation(query || ""),
    { enabled: hitAPI }
  )

export const useGetUserProfileDetails = () =>
  useGet<userProfileDetail>(EndpointKey.GET_PROFILE_DETAILS, useApi().getProfileDetails(), {}
)

export const useGetPasscodeTitle = () =>
  useGet<passcodeDetail>(EndpointKey.GET_PASSCODE_TITLE, useApi().getPasscodeTitle(), {}
)


export const useSavePasscodeHash = () =>
useSet<savePasscodeHashData>(
  EndpointKey.SAVE_PASSCODE,
  useApi().savePasscode(),
  "POST",
  {},
  [EndpointKey.GET_PROFILE_DETAILS]
  )

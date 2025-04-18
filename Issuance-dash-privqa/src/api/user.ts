/* eslint-disable @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any */
import { useState } from "react"
import env from "src/env"

import {
  signInAsync,
  requestResetPasswordCodeAsync,
  submitResetPasswordCodeAsync,
  getIdTokenAsync
} from "../auth/AuthManager"
import {
  IssuanceData,
  IssuanceDetailsData,
  UserDetailsData,
  VerifyEmailOTPData,
  UserData,
  WalletsUserResData,
  EnableDisableWalletUser,
  UpdateIssuanceDetails,
  GetWalletFeeType,
  GetTransactionType,
  AddWalletFeeConfig,
  EditWalletFeeConfig,
  GetTransactionLimitConfig,
  AddTransactionLimitConfig,
  EditTransactionLimitConfig,
  GetConversionRates,
  AddConversionRates,
  EditConversionRates,
  GetConversionRatesAdjustment,
  AddConversionRatesAdjustment,
  EditConversionRatesAdjustment,
  GetIndustryTypeList,
  GetFiatCurrencyList,
  SaveInstitutionDetails,
  GetCountryList,
  GetAllLanguages,
  addLanguage,
  saveLanguage,
  updateLanguage,
  mappedLanguageInstitutionType,
  makeLanguageDefaultType
} from "./models"
import { EndpointKey, useApi } from "./constants"

import { useGet, useSet } from "./index"

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
      } catch (e: any | null) {
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

export const useMerchant = () =>
  useSet<IssuanceData>(
    EndpointKey.MERCHANT,
    useApi().merchant(),
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

export const useRequestResetPasswordCode = () => {
  return useGenericApiCall(requestResetPasswordCodeAsync)
}

export const useSubmitResetPasswordCode = () => {
  return useGenericApiCall(submitResetPasswordCodeAsync)
}
export const useIssuanceDetails = () =>
  useGet<IssuanceDetailsData>(
    EndpointKey.ISSUANCE_DETAILS,
    useApi().issuanceDetails(),
    {}
  )

export const useUpdateIssuanceDetails = () =>
  useSet<UpdateIssuanceDetails>(
    EndpointKey.UPDATE_ISSUANCE_DETAILS,
    useApi().updateIssuanceDetails(),
    "POST",
    {},
    EndpointKey.UPDATE_ISSUANCE_DETAILS
  )

export const useUserVerify = (query: string) =>
  useGet<UserData>(
    [EndpointKey.USER_VERIFY, query],
    useApi().issuanceBankVerify(query),
    { enabled: query != "" }
  )

export const useUserVerifyReset = (query: string) =>
  useGet<UserData>(
    [EndpointKey.USER_VERIFY_RESET, query],
    useApi().userVerifyReset(query),
    { enabled: query != "" }
  )

export const useUserVerifyResetIssuance = (query: string) =>
  useGet<UserData>(
    [EndpointKey.USER_VERIFY_RESET_ISSUANCE, query],
    useApi().userVerifyResetIssuance(query),
    { enabled: query != "" }
  )

export const useWalletUserList = () =>
  useSet<WalletsUserResData>(
    EndpointKey.WALLET_USER_LIST,
    useApi().walletUserList(),
    "POST",
    {},
    EndpointKey.WALLET_USER_LIST
  )

export const useEnableDisableWalletUser = () =>
  useSet<EnableDisableWalletUser>(
    EndpointKey.ENABLE_DISABLE_WALLET_USER,
    useApi().enableDisableWalletUser(),
    "POST",
    {},
    EndpointKey.ENABLE_DISABLE_WALLET_USER
  )

export const useWalletSummary = () =>
  useGet([EndpointKey.WALLET_SUMMARY], useApi().walletSummary())

export const useWalletBalance = () =>
  useGet([EndpointKey.WALLET_BALANCE], useApi().walletBalance())

export const useWalletRefund = () =>
  useGet([EndpointKey.WALLET_REFUND], useApi().walletRefund())

export const useWalletTransactionGraph = () =>
  useGet(
    [EndpointKey.WALLET_TRANSACTION_GRAPH],
    useApi().walletTransactionGraph()
  )

export const useWalletSummaryGraph = () =>
  useGet([EndpointKey.WALLET_SUMMARY_GRAPH], useApi().walletSummaryGraph())

export const useGetWalletFeeType = (query?: string) =>
  useGet<GetWalletFeeType>(
    [EndpointKey.GET_WALLET_FEE_TYPE, query],
    useApi().getWalletFeeType(),
    { keepPreviousData: true }
  )

export const useGetTransactionType = (query?: string) =>
  useGet<GetTransactionType>(
    [EndpointKey.GET_TRANSATION_TYPE, query],
    useApi().getTransactionType(),
    { keepPreviousData: true }
  )

export const useWalletFeeConfig = () =>
  useGet<GetWalletFeeType>(
    [EndpointKey.WALLET_FEE_CONFIG],
    useApi().walletFeeConfig(),
    { keepPreviousData: true }
  )

export const useAddWalletFeeConfig = () =>
  useSet<AddWalletFeeConfig>(
    EndpointKey.ADD_WALLET_FEE_CONFIG,
    useApi().addWalletFeeConfig(),
    "POST",
    {},
    EndpointKey.ADD_WALLET_FEE_CONFIG
  )

export const useEditWalletFeeConfig = () =>
  useSet<EditWalletFeeConfig>(
    EndpointKey.EDIT_WALLET_FEE_CONFIG,
    useApi().addWalletFeeConfig(),
    "POST",
    {},
    EndpointKey.EDIT_WALLET_FEE_CONFIG
  )

export const useAddTransactionLimitConfig = () =>
  useSet<AddTransactionLimitConfig>(
    EndpointKey.ADD_TRANSACTION_LIMIT_CONFIG,
    useApi().addTransactionLimitConfig(),
    "POST",
    {},
    EndpointKey.ADD_TRANSACTION_LIMIT_CONFIG
  )

export const useEditTransactionLimitConfig = () =>
  useSet<EditTransactionLimitConfig>(
    EndpointKey.EDIT_TRANSACTION_LIMIT_CONFIG,
    useApi().addTransactionLimitConfig(),
    "POST",
    {},
    EndpointKey.EDIT_TRANSACTION_LIMIT_CONFIG
  )

export const useGetTransactionLimitConfig = () =>
  useGet<GetTransactionLimitConfig>(
    [EndpointKey.GET_TRANSACTION_LIMIT_CONFIG],
    useApi().getTransactionLimitConfig(),
    { keepPreviousData: true }
  )

export const useFiatExchangeRates = (query: string) =>
  useGet<any>(
    [EndpointKey.FIAT_EXCHANGE_RATES, query],
    useApi().fiatExchangeRates(query || ""),
    { enabled: query != "" }
  )

export const useConversionRates = () =>
  useGet<GetConversionRates>(
    [EndpointKey.CONVERSION_RATES],
    useApi().conversionRates(),
    { keepPreviousData: true }
  )

export const useAddConversionRates = () =>
  useSet<AddConversionRates>(
    EndpointKey.ADD_CONVERSION_RATES,
    useApi().addConversionRates(),
    "POST",
    {},
    EndpointKey.ADD_CONVERSION_RATES
  )

export const useEditConversionRates = () =>
  useSet<EditConversionRates>(
    EndpointKey.EDIT_CONVERSION_RATES,
    useApi().editConversionRates(),
    "POST",
    {},
    EndpointKey.EDIT_CONVERSION_RATES
  )

export const useConversionRatesAdjustment = () =>
  useSet<GetConversionRatesAdjustment>(
    EndpointKey.CONVERSION_RATES_ADJUSTMENT,
    useApi().conversionRatesAdjustment(),
    "POST",
    {},
    EndpointKey.CONVERSION_RATES_ADJUSTMENT
  )

export const useAddConversionRatesAdjustment = () =>
  useSet<AddConversionRatesAdjustment>(
    EndpointKey.ADD_CONVERSION_RATES_ADJUSTMENT,
    useApi().addConversionRatesAdjustment(),
    "POST",
    {},
    EndpointKey.ADD_CONVERSION_RATES_ADJUSTMENT
  )

export const useEditConversionRatesAdjustment = () =>
  useSet<EditConversionRatesAdjustment>(
    EndpointKey.EDIT_CONVERSION_RATES_ADJUSTMENT,
    useApi().editConversionRatesAdjustment(),
    "POST",
    {},
    EndpointKey.EDIT_CONVERSION_RATES_ADJUSTMENT
  )

export const useGetIndustryTypeList = () =>
  useGet<GetIndustryTypeList>(
    [EndpointKey.GET_INDUSTRY_TYPE_LIST],
    useApi().getIndustryTypeList(),
    { keepPreviousData: true }
  )

export const useGetFiatCurrencyList = () =>
  useGet<GetFiatCurrencyList>(
    [EndpointKey.GET_FIAT_CURRENCY_LIST],
    useApi().getFiatCurrencyList(),
    { keepPreviousData: true }
  )

export const useSaveInstitutionDetails = () =>
  useSet<SaveInstitutionDetails>(
    EndpointKey.ADD_CONVERSION_RATES,
    useApi().saveInstitutionDetails(),
    "POST",
    {},
    EndpointKey.SAVE_INSTITUTION_DETAILS
  )

export const useGetInstitutionDetails = () =>
  useGet<GetFiatCurrencyList>(
    [EndpointKey.GET_INSTITUTION_DETAILS],
    useApi().getInstitutionDetails(),
    { keepPreviousData: true }
  )

export const useGetCountryList = () =>
  useGet<GetCountryList>(
    [EndpointKey.GET_COUNTRY_LIST],
    useApi().getCountryList(),
    { keepPreviousData: true }
  )

export const useGetAllLanguages = () =>
  useGet<GetAllLanguages>(
    [EndpointKey.GET_ALL_LANGUAGES],
    useApi().getAllLanguages(),
    { keepPreviousData: true }
  )

export const useAddLanguage = () =>
  useSet<addLanguage>(
    EndpointKey.ADD_LANGUAGE,
    useApi().addLanguage(),
    "POST",
    {},
    EndpointKey.ADD_LANGUAGE
  )

export const useUpdateLanguage = () =>
  useSet<updateLanguage>(
    EndpointKey.ADD_LANGUAGE,
    useApi().addLanguage(),
    "POST",
    {},
    EndpointKey.ADD_LANGUAGE
  )

export const useSaveLanguage = () =>
  useSet<saveLanguage>(
    EndpointKey.ADD_LANGUAGE,
    useApi().addLanguage(),
    "POST",
    {},
    EndpointKey.ADD_LANGUAGE
  )

export const useMappedLanguageInstitution = () =>
  useSet<mappedLanguageInstitutionType>(
    EndpointKey.MAPPED_LANGUAGE_INSTITUTION,
    useApi().mappedLanguageInstitution(),
    "POST",
    {},
    EndpointKey.MAPPED_LANGUAGE_INSTITUTION
  )

export const useMakeLanguageDefault = () =>
  useSet<makeLanguageDefaultType>(
    EndpointKey.MAKE_LANGUAGE_DEFAULT,
    useApi().makeLanguageDefault(),
    "POST",
    {},
    EndpointKey.MAKE_LANGUAGE_DEFAULT
  )

export const verifyToken = async (token: string) => {
  return fetch(env.PUBLIC_API_URL + "user/account/verifyToken", {
    method: "POST",
    headers: {
      Accept: "*",
      Host: env.PUBLIC_API_URL,
      "Access-Control-Allow-Origin": "*",
      authorization: `Bearer ${await getIdTokenAsync()}`
    }
  })
}
export const saveToken = async (token: string) => {
  return fetch(env.PUBLIC_API_URL + "user/account/saveToken", {
    method: "POST",
    headers: {
      Accept: "*",
      Host: env.PUBLIC_API_URL,
      "Access-Control-Allow-Origin": "*",
      authorization: `Bearer ${await getIdTokenAsync()}`
    }
  })
}

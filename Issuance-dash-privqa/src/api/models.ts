import {
  Asset,
  FiatAsset,
  TransactionStatus,
  TransactionType,
  TransactionDirection
} from "src/constants/types"
import { TokenToAmount } from "src/constants/types"

export type SignInData = {
  email: string
  password: string
}

export type SendPhoneOTPData = {
  phoneNumber: string
}

export type UserDetailsData = {
  email: string
  phoneNumber: string
  password: string
}

export type IssuanceData = {
  id: number
  bankName: string
  countryCode: string
  timeZone: string
  defaultCurrency: string
  phoneNumber: string
  email: string
  cognitoUsername: string
  updatedAt: Date
  createdAt: Date
  isActive: boolean
}

export type VerifyPhoneOTPData = {
  phoneNumber: string
  code: string
}

export type VerifyEmailOTPData = {
  email: string
  phoneNumber: string
  code: string
  password: string
  isMerchantAdmin: boolean
}

export type WalletsUserResData = {
  page: number
  sortBy: string
  sortDirection: string
  limit: number
}

export type WalletsUserListdata = {
  totalCount: number
  totalEnabled: number
  totalDisabled: number
  walletList: [WalletsUserList]
  pagination: {
    current_page: number
    total_records: number
    total_pages: number
    links: string
  }
}

export type WalletsUserList = {
  status: string
  firstName: string
  lastName: string
  walletId: string
  phoneNumber: string
  email: string
  cognitoUsername: string
  validFromDate: Date
  validToDate: Date
  isActive: boolean
  tokenBalance: number
  tokenAsset: string
}

export type EnableDisableWalletUser = {
  email: string
  isEnabled: boolean
}

export type UserData = {
  email: string
  phoneNumber: string
  cognitoUsername: string
  merchant: IssuanceData
}

export type InviteUserData = {
  email: string
  role: string
}

export type GenerateAPIKey = {
  username?: string
  password?: string
  basicKey?: string
}

export type IssuanceDetailsData = {
  id: number
  bankLogo: string
  bankName: string
  countryCode: string
  timeZone: string
  defaultCurrency: string
  fiatCurrency: string
  destinationFiatCurrency: string
  phoneNumber: string
  email: string
  cognitoUsername: string
  updatedAt: Date
  createdAt: Date
  isActive: boolean
  p2pTransfer: boolean | null
}

export type EnableDisableUserData = {
  email: string
}

export type MerchantUserData = {
  userAccount: UserData
  isActive: boolean
}

export type UpdateIssuanceDetails = {
  bankName: string
  countryCode: string
  defaultCurrency: string
  phoneNumber: string
  defaultTimeZone: string
}

export type AddConversionRates = {
  currencyFrom: string
  currencyTo: string
  baseRate: number
  validFrom: string | Date
  isActive: boolean
}

export type EditConversionRates = {
  id: number
  currencyFrom: string
  currencyTo: string
  baseRate: number
  validFrom: string | Date
  isActive: boolean
}

export type ConversionRate = {
  status: string
  firstName: string
  lastName: string
  walletId: string
  phoneNumber: string
  email: string
  cognitoUsername: string
  validFromDate: Date
  validToDate: Date
  isActive: boolean
  tokenBalance: number
  tokenAsset: string
}

export type ConversionRatesList = [
  {
    id: number
    currencyFrom: string
    currencyTo: string
    baserRate: number
    validFrom: Date
    validTo: Date
    isActive: boolean
    currentActive: boolean
  }
]

export type GetWalletFeeType = [
  {
    createdAt: Date
    feeType: string
    id: number
    isActive: boolean
    walletFeeId: string
  }
]

export type GetTransactionType = [
  {
    id: number
    transactionTypeId: string
    transactionType: string
    createdAt: Date
    isActive: boolean
  }
]

export type AddWalletFeeConfig = {
  isActive: boolean
  fiatCurrency: string
  walletConfigType: string
  frequency: string | null
  value: string
  minimum: number | null
  maximum: number | null
}

export type EditWalletFeeConfig = {
  id: number
  isActive: boolean
  fiatCurrency: string
  walletConfigType: string
  frequency: string | null
  value: string
  minimum: number | null
  maximum: number | null
}

export type GetWalletFeeConfig = [
  {
    createdAt: Date
    currency: string
    frequency: string
    id: number
    isActive: boolean
    maxValue: number
    minValue: number
    modifiedDate: Date
    userCategory: Date
    value: string
    walletFeeId: string
    walletFeeType: string
  }
]

export type AddTransactionLimitConfig = {
  isActive: boolean
  fiatCurrency: string
  transactionType: string
  frequency: string | null
  transactionCount: number | null
  minimum: any | null
  maximum: any | null
}

export type EditTransactionLimitConfig = {
  id: number
  isActive: boolean
  fiatCurrency: string
  transactionType: string
  frequency: string | null
  transactionCount: number | null
  minimum: number | null
  maximum: number | null
}

export type GetTransactionLimitConfig = [
  {
    id: number
    currency: string
    transactionTypeId: string
    transactionType: string
    frequency: string
    frequencyStr: string
    transactionCount: number
    minValue: number
    maxValue: number
    userCategory: string
    createdAt: Date
    modifiedDate: Date
    isActive: boolean
  }
]

export type GetConversionRates = [
  {
    id: number
    currencyFrom: string
    currencyTo: string
    baserRate: number
    validFrom: Date
    validTo: null
    createdAt: Date
    isActive: boolean
    currentActive: boolean
  }
]

export type GetConversionRatesAdjustment =
  | [
      {
        createdAt: Date
        currencyFrom: string
        currencyTo: string
        id: number
        isActive: boolean
        percentage: number
        type: string
        validFrom: Date
        validTo: Date | null
        currentActive: boolean
      }
    ]
  | any

export type AddConversionRatesAdjustment = {
  currencyFrom: string
  currencyTo: string
  percentage: number
  validFrom: string | Date
  markType: string
  isActive: boolean
}

export type EditConversionRatesAdjustment = {
  id: number
  currencyFrom: string
  currencyTo: string
  percentage: number
  validFrom: string | Date
  markType: string
  isActive: boolean
}

export type ConversionRatesAdjustmentItems = {
  key: string
  currencyFrom: string
  currencyTo: string
  percentage: number
  validFrom: string | Date
  createdAt: Date
  isActive: boolean
  currentActive: boolean
}

export type GetIndustryTypeList = {
  key: string
}

export type GetFiatCurrencyList = {
  key: string
}

export type SaveInstitutionDetails = {
  key: string
}

export type GetCountryList = [
  {
    countryId: number
    countryCode: string
    countryName: string
    countryImageUrl: number
  }
]

export type GetAllLanguages = [
  {
    id: number
    languageName: string
    languageDisplayName: string
    countryName: string
    countryImageUrl: string
    resourceFileUrl: string
    isActive: boolean
  }
]

export type addLanguage = {
  languageName: string
  languageDisplayName: string
  countryId: number | null
  resourceFileUrl: string
  isActive: boolean
}

export type updateLanguage = {
  id: number
  languageName: string
  languageDisplayName: string
  countryId: number | null
  resourceFileUrl: string
  isActive: boolean
}

export type saveLanguage = {
  id: number
  isActive: boolean
  languageName: string
}

export type mappedLanguageInstitutionType = [
  {
    id: number
    isActive: boolean
    languageId: number
  }
]

export type makeLanguageDefaultType = {
  isDefault: boolean
  languageId: number
}

import { AnyObjectSchema } from "yup"
import {
  Asset,
  FiatAsset,
  TokenToAmount,
  TransactionDirection,
  TransactionStatus,
  TransactionType
} from "~/constants/types"

export type SignInData = {
  email: string
  password: string
}

export type ResetPasswordData = {
  email: string
}

export type UserData = {
  email: string
  phoneNumber: string
  cognitoUsername: string
  kycVerified?: string
  userBankAccount:[] 
}

export type UserDetailsData = {
  email: string
  phoneNumber: string
  password: string
}

export type VerifyEmailOTPData = {
  email: string
  phoneNumber: string
  code: string
  password: string
}

export type SendPhoneOTPData = {
  phoneNumber: string
}

export type VerifyPhoneOTPData = {
  phoneNumber: string
  code?: string
  phoneNumberCode?: string
}

export type SellDigitalCurrencyData = {
  digitalAmount: any
  fiatAsset: string
  digitalAsset: string
}

export type BuyDigitalCurrencyData = {
  fiatAmount: any
  fiatAsset: string
  digitalAsset: string
}
export type BalancesData = TokenToAmount

export type FiatBalance = {
  fiatasset: FiatAsset
  balance: number
  updatedAt: Date
}

export type FiatBalanceData = FiatBalance[]

export type Addresses = {
  asset: Asset
  address: string
}
export type addressesData = Addresses[]

export type userKyc = {
  timestamp: string
  transactionReference: string
  redirectUrl: string
  successUrl: string
}
export type Transaction = {
  id: string // uuid
  reference: string
  description: string
  senderName: string
  receiverName: string
  asset: Asset
  fiatAmount: number
  fiatAsset: FiatAsset
  status: TransactionStatus
  transactionType: TransactionType
  amount: number
  totalAmount: number
  feePercentage: number
  feeAmount: number
  createdAt: Date
  paymentReceivedDate:Date
  direction: TransactionDirection
  uuid: string,
  blockchainTxId: string,
  refundTransactionId: string,
  totalDigitalCurrencyReceived: any
  feeConfigData: any
  totalFeeApplied: number,
  totalFeeAppliedAsset: string,
  totalRequestedAmount: number,
  totalRequestedAmountAsset: string,
}

export type WithdrawOrDepositFiat = {
  fiatAmount: any,
  fiatType: string,
  bankAccountNumber: string
}

export type TopupOrRefundSart = {
  tokenAsset: any,
  amount: number,
  bankAccountNumber: string,
  isFromWallet:boolean,
  fiatAsset:string,
  fiatAmount:number | any
  feeConfigData: any,
  totalFeeApplied:number
}

export type fiatExchangeDataSart = {
  SAR:number
}

export type UserBankAccData = {
  bankAccountNumber: string,
  confirmBankAccountNumber: string
}

export type TransactionsData = Transaction[]

export type AddFakeTransactionData = {
  amount: number
  asset: Asset
}

export type ExternalTransferData = {
  amount: any
  asset: Asset | string
  receiverAddress: string
  description: string
  uuid?: string
  status?: string
}


export type DeleteContactOfUser = {
  nickname: string
}


export type UserByEmailOrMobileData = {
  nickname:string
  isSelected:boolean
  email: string
  phoneNumber: string
  cognitoUsername: string
  kycVerified?: string
}


export type SendNotificationForRecievingPaymentData = {
  requesterId?: any
  requesterName?: string
  requesterEmail?: string
  requesterPhone?: string
  receiverName?: string
  receiverEmail?: string
  receiverPhone?: string
  digitalCurrency?: string
  amount?: any
  fee?: string
  walletAddress?: string
  time?: any
  timeNotification?: string
  title?: string
  body?: string
  id?: string
  uuid?: string
  status?: string
  transactionId?: string
  requestStatusId?: string
}
export type transactionConfirmationData = {
  screenType?: string,
  transactionType?: string
  amountRequested ?: number | any
  requestPayload ?: topupRequestPayload
  totalAmount ?: number | any
  totalFee ?: number
  tokenAsset ?: string,
  transactionConfigData ?: any
  ttc ?: any
  feeTitle ?: string
  isFinalAmountNegative?:boolean
  netAmountCalulated ?: number | any
  uniqueList ?: any
  totalAmountText : string
}

export type AddTransactionData = {
  receiverEmail: string
  amount: number
  asset: Asset
  uuid?: string
  status?: string
}

export type ExpoTokenData = {
  expoToken: string
}

export type OnRampConfigData = {
  onRampApiKey: string
  gateways: string
  digitalCurrencyList: [
    {
      code: string
      inwardAddress: string
      outwardAddress: string
    }
  ]
  fiats: [
    {
      code: string
      sign: string
      fullName: string
    }
  ]
}

export type TransactionFilters = {
  dateFrom?: Date
  dateTo?: Date
  amountFrom?: string
  amountTo?: string
  direction?: TransactionDirection | ""
  type?: TransactionType[]
  status?: TransactionStatus[]
  asset?: Asset[]
}

export type TransactionsParams = {
  page?: string
  search?: string
} & TransactionFilters

export type Contact = {
  id: string // cognitoUsername
  nickname: string
  phoneNumber: string
  email: string
  cognitoUsername: string
}

export type ContactsData = Contact[]
export type NotificationListData = SendNotificationForRecievingPaymentData[]

export type SaveUserContactData = {
  nickname: string
  cognitoUsername: string
}
export type CheckValidEmailData = {
  email: string
}

export type GetDecryptedData = {
  data: string
}

export type GetEncryptedData = {
  data: string
}

export type transactionValidtionConfigParameter = {
  count: number,
  availableCount: number,
  minimumBalance: number,
  maximumBalance: number,
  remainingMaximumBalance: number
  }

  export type feeConfigParameter = {
    feeFrequency?: string,
    feeMaximumAmount?: number,
    feeMinimumAmount?: number,
    feeName?: string,
    feeType?: string,
    feeAmount?: number,
    feeId?: string,
    feeCalculatedAmount?:number,
    enteredAmount?:number,
    currencyType?: string

    }
  
  export type transactionValidtionConfigData = {
    initialLoading: boolean,
    feeConfig: feeConfigParameter[]
    one_TIME : transactionValidtionConfigParameter,
    per_TRANSACTION : transactionValidtionConfigParameter
    daily : transactionValidtionConfigParameter
    weekly : transactionValidtionConfigParameter
    monthly : transactionValidtionConfigParameter
    quarterly : transactionValidtionConfigParameter
    half_YEARLY : transactionValidtionConfigParameter
    yearly : transactionValidtionConfigParameter
  }
  export type userProfileDetail = {
    isPasscodeSet: boolean
    isActivationFeeCharged : boolean
    isBalanceRunningLow : boolean
    lowMaintainWalletBalance: string
  }

  export type passcodeDetail = {
    passcodeScreen : passcodeScreenTitle
  }

  type passcodeScreenTitle = {
    createPasscode : passcodetitle,
    confirmPasscode : passcodetitle

  }
 type passcodetitle = {
    title : string
    subtitle : string
  }

  export type savePasscodeHashData = {
    passcodeHash: string,
    message?: string
  }
  export type topupRequestPayload = {
      tokenAsset?: string,
      amount?: number,
      bankAccountNumber?: string,
      isFromWallet?: boolean,
      fiatAsset?: string,
      fiatAmount?: number | any,
      feeConfigData?: feeConfigParameter[] | AnyObjectSchema,
      totalFeeApplied?:number
  }

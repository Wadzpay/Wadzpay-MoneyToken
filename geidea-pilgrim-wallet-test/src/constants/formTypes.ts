import { Control } from "react-hook-form"

import { Asset, SendFundType } from "./types"

import { TransactionFilters } from "~/api/models"

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type FieldControl = Control<any>
export type FieldName =
  | keyof CreateAccountForm
  | keyof SignInForm
  | keyof VerifyCodeForm
  | keyof AccountDetailsForm
  | keyof PersonalDetailsForm
  | keyof AddFakeTransactionForm
  | keyof TransactionFiltersForm
  | keyof SaveContactForm
  | keyof ChangePasswordForm
  | keyof WalletAddressForm
  | keyof SelectSendWalletForm
  | keyof RefundForm
  | keyof VerifyPhoneOTPCodeForm

export type SignInForm = {
  email: string
  password: string
}

export type BuyForm = {
  amount: string
}

export type SellForm = {
  amount: string
}

export type WithdrawDepositForm = {
  fiatAmount: string
}

export type ChangePasswordForm = {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export type ResetPasswordForm = {
  email: string
  newPassword: string
  confirmPassword: string
}

export type CreateAccountForm = {
  country: string
  phoneNumber: string
  isNewsletter: boolean
  isAgreement: boolean
}
export type RefundForm = {
  isAccepted: boolean
}

export type VerifyCodeForm = {
  code: string
}

export type VerifyPhoneOTPCodeForm = {
  phoneNumberCode: string
}


export type AccountDetailsForm = {
  email: string
  newPassword: string
  confirmPassword: string
}

export type PersonalDetailsForm = {
  firstName: string
  lastName: string
  dateOfBirth: Date
  profession: string
  sourceOfFund: string
}

export type AddFakeTransactionForm = {
  amount: string
  asset: Asset
}

export type SelectSendWalletForm = {
  sendFundType: SendFundType
  isDisclaimerChecked: boolean
}

export type TransactionFiltersForm = TransactionFilters

export type SaveContactForm = {
  nickname: string
  userId: string
  search: string
  query: string
}

export type WalletAddressForm = {
  walletAddress: string
}

export type GenrateQrCodeForm = {
  asset: Asset
}

import { Control } from "react-hook-form"

import { Asset, FiatAsset } from "./types"
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type FieldControl = Control<any>

export type FieldName =
  | keyof CreateAccountForm
  | keyof VerifyCodeForm
  | keyof SignInForm
  | keyof AccountDetailsForm

export type CreateAccountForm = {
  country: string
  phoneNumber: string
}

export type SignInForm = {
  email: string
  password: string
}

export type VerifyCodeForm = {
  code: string
  newPassword: string
  confirmPassword: string
}

export type AccountDetailsForm = {
  email: string
  newPassword: string
  confirmPassword: string
}

export type MerchantDetailsForm = {
  name: string
  countryOfRegistration: string
  registrationCode: string
  primaryContactFullName: string
  primaryContactEmail: string
  companyType: string
  industryType: string
  primaryContactPhoneNumber: string
  merchantId: string
  defaultRefundableFiatValue: number
  tnc: string
}

export type ResetPasswordForm = {
  email: string
  // newPassword: string
  // confirmPassword: string
}

export type InviteUserForm = {
  email: string
}

export type p2pTransactionForm = {
  amount: string
  asset: Asset
  sendBy: string
  receiverUsername: string
  receiverEmail: string
  receiverPhone: string
  description: string
}

export type requestTransactionForm = {
  email: string
  phoneNo: string
  fiatType: string
  fiatAmount: number
  digitalCurrency: string
  digitalAmount: number
}
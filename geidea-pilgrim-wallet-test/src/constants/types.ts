import {
  QueryObserverIdleResult,
  QueryObserverLoadingErrorResult,
  QueryObserverLoadingResult,
  QueryObserverRefetchErrorResult,
  QueryObserverSuccessResult
} from "react-query"

import { IconName } from "~/icons"

export type CryptoFullName = "Bitcoin" | "Ethereum" | "Tether" | "WadzPay Token"
export type Asset = "BTC" | "ETH" | "USDT" | "WTK" | "XSGD" | "AED" | "SART"

export type SendFundType = "WadzpayWallet" | "ExternalWallet"

// fiat currencies and signs are part of the config from BE we need to use that
export type FiatSign = "$" | "€" | "₹" | "Rp" | "S$" | "£" | "₨" | "₱" | "د.إ" | "RM" | "SAR"
export type FiatAsset =
  | "USD"
  | "EUR"
  | "INR"
  | "IDR"
  | "SGD"
  | "GBP"
  | "PKR"
  | "PHP"
  | "AED"
  | "MYR"
  | "SAR" 
  | "SART"

export const FiatSignMap: { [key in FiatAsset]: FiatSign } = {
  USD: "$",
  EUR: "€",
  INR: "₹",
  IDR: "Rp",
  SGD: "S$",
  GBP: "£",
  PKR: "₨",
  PHP: "₱",
  AED: "د.إ",
  MYR: "RM",
  SAR: "SAR",
}

export const AssetIconNamesMap: { [key in Asset]: IconName } = {
  BTC: "BTC",
  ETH: "ETH",
  USDT: "USDT",
  WTK: "WTK",
  SART: "SART"
}

export type TokenToAmount = { [key in Asset]: number }
export type FiatTokenToAmount = { [key in FiatAsset]: number }

export type TransactionStatus = "SUCCESSFUL" | "FAILED" | "IN_PROGRESS" 
// | "OVERPAID" | "UNDERPAID"
export type TransactionDirection = "INCOMING" | "OUTGOING" | "UNKNOWN"
export type TransactionType =
  "DEPOSIT"
  | "WITHDRAW"
  | "PEER_TO_PEER"
  | "EXTERNAL_SEND"
  | "EXTERNAL_RECEIVE"
  | "POS"
  | "REFUND"
  | "SERVICE_FEE"
  | "OTHER"

export type QueryResult<T> =
  | QueryObserverIdleResult<T[], Error>
  | QueryObserverLoadingErrorResult<T[], Error>
  | QueryObserverLoadingResult<T[], Error>
  | QueryObserverRefetchErrorResult<T[], Error>
  | QueryObserverSuccessResult<T[], Error>

export type Nullable<T> = T | null;
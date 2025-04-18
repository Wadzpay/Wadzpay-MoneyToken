import { useContext } from "react"
import { UserContext } from "~/context"
import { BlockedCountries } from "./blockedNumber"
import { useTranslationItems } from "./translationItems"
import {
  Asset,
  TransactionDirection,
  TransactionStatus,
  TransactionType
} from "./types"

export const useAssetSelectItems: (useShortNames?: boolean) => {
  label: string
  value: Asset
}[] = (useShortNames = false) => {
  const { asset } = useTranslationItems()
  return [
   { label: useShortNames ? "WTK" : asset.WTK, value: "WTK" },
   { label: useShortNames ? "BTC" : asset.BTC, value: "BTC" },
   { label: useShortNames ? "ETH" : asset.ETH, value: "ETH" },
   { label: useShortNames ? "USDT" : asset.USDT, value: "USDT" },
   { label: useShortNames ? "SART" : asset.SART, value: "SART" }
  ]
}

export const useTransactionDirectionSelectItems: () => {
  label: string
  value: TransactionDirection
}[] = () => {
  const { direction } = useTranslationItems()
  return [
    { label: direction.INCOMING, value: "INCOMING" },
    { label: direction.OUTGOING, value: "OUTGOING" }
  ]
}

export const useTransactionTypeSelectItems: () => { label: string; value: TransactionType}[] = () => {

  const { type } = useTranslationItems()
  const { user } = useContext(UserContext)

  if (user?.attributes?.phone_number?.startsWith(BlockedCountries.SINGAPORE)) {
    return [
      { label: type.PEER_TO_PEER, value: "PEER_TO_PEER" },
      { label: type.OTHER, value: "OTHER" }
    ]
  } else {
    return [
      { label: type.DEPOSIT, value: "DEPOSIT" },
      { label: type.WITHDRAW, value: "WITHDRAW" },
      { label: type.PEER_TO_PEER, value: "PEER_TO_PEER" },
      { label: type.POS, value: "POS" },    
        { label: type.REFUND, value: "REFUND" },
    ]
  }
}

export const useTransactionStatusSelectItems: () => {
  label: string
  value: TransactionStatus
}[] = () => {
  const { status } = useTranslationItems()
  return [
    { label: status.SUCCESSFUL, value: "SUCCESSFUL" },
    { label: status.IN_PROGRESS, value: "IN_PROGRESS" },
    { label: status.FAILED, value: "FAILED" },
    // { label: status.OVERPAID, value: "OVERPAID" },
    // { label: status.UNDERPAID, value: "UNDERPAID" }
  ]
}

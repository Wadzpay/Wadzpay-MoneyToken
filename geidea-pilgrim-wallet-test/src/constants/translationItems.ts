import { useTranslation } from "react-i18next"

import {
  Asset,
  TransactionDirection,
  TransactionStatus,
  TransactionType,
  SendFundType
} from "./types"

export const useTranslationItems: () => {
  direction: { [key in TransactionDirection]: string }
  type: { [key in TransactionType]: string }
  status: { [key in TransactionStatus]: string }
  asset: { [key in Asset]: string }
  assetShort: { [key in Asset]: string }
  sendFundType: { [key in SendFundType]: string }
} = () => {
  const { t } = useTranslation()
  return {
    direction: {
      INCOMING: t("Inbound"),
      OUTGOING: t("Outbound"),
      UNKNOWN: t("Unknown")
    },
    type: {
      DEPOSIT: t("Topup"),
      WITHDRAW: t("Redeem Unspent"),
      PEER_TO_PEER: t("Transfer"),
      EXTERNAL_SEND: t("Payment"),
      POS:t("Payment"),
      REFUND:t("Merchant Refund"),
    },
    status: {
      SUCCESSFUL: t("Successful"),
      IN_PROGRESS: t("Pending"),
      FAILED: t("Failed"),
      OVERPAID: t("Overpaid"),
      UNDERPAID: t("Underpaid")
    },
    asset: {
      WTK: t("WadzPay Token (WTK)"),
      BTC: t("Bitcoin (BTC)"),
      ETH: t("Ethereum (ETH)"),
      USDT: t("Tether (USDT)")
    },
    assetShort: {
      WTK: t("WadzPay Token"),
      BTC: t("Bitcoin"),
      ETH: t("Ethereum"),
      USDT: t("Tether")
    },
    sendFundType: {
      WadzpayWallet: t("WadzPay Wallet"),
      ExternalWallet: t("External Wallet")
    }
  }
}

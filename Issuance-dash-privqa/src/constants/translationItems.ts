import { useTranslation } from "react-i18next"

import {
  Asset,
  TransactionDirection,
  TransactionStatus,
  TransactionType,
  ExternalPOS
} from "./types"

export const useTranslationItems: () => { type: { OTHER: string; PEER_TO_PEER: string; ON_RAMP: string; OFF_RAMP: string; MERCHANT: string }; asset: { BTC: string; ETH: string; USDT: string; WTK: string }; externalPos: { EXT_POS_SEQUENCE_NO: string; EXT_POS_TRANSACTION_ID: string; EXT_POS_ID: string }; assetShort: { BTC: string; ETH: string; USDT: string; WTK: string }; direction: { INCOMING: string; UNKNOWN: string; OUTGOING: string }; status: { IN_PROGRESS: string; FAILED: string; SUCCESSFUL: string; UNDERPAID: string; OVERPAID: string } } = () => {
  const { t } = useTranslation()
  return {
    direction: {
      INCOMING: t("Inbound"),
      OUTGOING: t("Outbound"),
      UNKNOWN: t("Unknown")
    },
    type: {
      MERCHANT: t("Merchant"),
      ON_RAMP: t("On-Ramp"),
      OFF_RAMP: t("Off-Ramp"),
      PEER_TO_PEER: t("Peer To Peer"),
      OTHER: t("Other")
    },
    status: {
      SUCCESSFUL: t("Successful"),
      IN_PROGRESS: t("Pending"),
      FAILED: t("Failed"),
      OVERPAID: t("OVERPAID"),
      UNDERPAID: t("UNDERPAID")
    },
    asset: {
      BTC: t("Bitcoin (BTC)"),
      ETH: t("Ethereum (ETH)"),
      USDT: t("Tether (USDT)"),
      WTK: t("WadzPay (WTK)")
    },
    assetShort: {
      BTC: t("Bitcoin"),
      ETH: t("Ethereum"),
      USDT: t("Tether"),
      WTK: t("WadzPay")
    },
    externalPos: {
      EXT_POS_ID: t("extPosId"),
      EXT_POS_SEQUENCE_NO: t("extPosSequenceNo"),
      EXT_POS_TRANSACTION_ID: t("extPosTransactionId")
    }
  }
}

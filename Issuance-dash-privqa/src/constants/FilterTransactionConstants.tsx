import { Value } from "sass"

export const Asset = [
  { label: "Bitcoin", value: "BTC" },
  { label: "Ethereum", value: "ETH" },
  { label: "Tether", value: "USDT" },
  //{ label: "WTK", value: "WTK" },
  { label: "USD Coin", value: "USDC" }
]
export const Status = [
  { label: "Successful", value: "SUCCESSFUL" },
  { label: "Failed", value: "FAILED" },
  { label: "In Progress", value: "IN_PROGRESS" },
  { label: "Underpaid", value: "UNDERPAID" },
  { label: "Overpaid", value: "OVERPAID" }
]
export const SearchItems = [
  { label: "Transaction ID", value: "uuid" },
  { label: "POS Transaction ID", value: "extPosTransactionId" }
]
export const refundStatus = [
  { label: "Yet to start", value: "NULL", title: "Yet to start" },
  {
    label: "Weblink Sent",
    value: "REFUND_ACCEPTED, REFUND_APPROVED",
    title: "Weblink Sent"
  },
  { label: "CVF Submitted", value: "REFUND_ACCEPTED", title: "CVF Submitted" },
  { label: "Refunded", value: "REFUNDED", title: "Refund Completed" },
  {
    label: "WL Expired",
    value: "REFUND_EXPIRED",
    title: "Reinitiating Refund for expired weblink"
  },
  {
    label: "Rejected",
    value: "REFUND_CANCELED",
    title: "Reinitiating Refund rejected by Supervisor"
  },
  {
    label: "Failed",
    value: "REFUND_FAILED",
    title: "Reinitiating Refund failed from Blockchain"
  }
]
export const TransactionType = [
  { label: "On-Ramp", value: "ON_RAMP" },
  { label: "Off-Ramp", value: "OFF_RAMP" },
  { label: "Merchant", value: "MERCHANT" },
  { label: "Peer to Peer", value: "PEER_TO_PEER" },
  { label: "Other", value: "OTHER" },
  { label: "External Receive", value: "EXTERNAL_RECEIVE" }
]

export const DebitCredit = [
  { label: "Incoming", value: "INCOMING" },
  { label: "Outgoing", value: "OUTGOING" }
  // { label: "Unknown", value: "UNKNOWN" }
]

export const RefundMode = [
  { label: "Wallet", value: "WALLET" },
  { label: "Cash", value: "CASH" }
  // { label: "Unknown", value: "UNKNOWN" }
]

export const SearchItemsRefund = [
  { label: "Transaction ID", value: "uuid" },
  { label: "POS Transaction ID", value: "extPosTransactionId" }
]

export const refundType = [
  { label: "Partial", value: "PARTIAL" },
  { label: "Full", value: "FULL" }
]

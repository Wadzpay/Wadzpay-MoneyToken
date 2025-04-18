import { Value } from "sass";

export const Asset = [{ label: "SAR*", value: "SART" }];
export const Status = [
  { label: "Successful", value: "SUCCESSFUL" },
  { label: "Failed", value: "FAILED" },
  { label: "In Progress", value: "IN_PROGRESS" },
  { label: "Underpaid", value: "UNDERPAID" },
  { label: "Overpaid", value: "OVERPAID" },
];
export const SearchItems = [
  { label: "Transaction ID", value: "uuid" },
  { label: "Acquirer Transaction ID", value: "extPosTransactionId" },
];
export const refundStatus = [
  {
    label: "Yet to start",
    value: "NULL, REFUND_CANCELED, REFUND_FAILED, REFUND_EXPIRED",
    title: "Yet to start",
  },
  {
    label: "In Progress",
    value: "REFUND_INITIATED, REFUND_HOLD, REFUND_ACCEPTED",
    title: "In Progress",
  },
  { label: "Completed", value: "REFUNDED", title: "Completed" },
  { label: "Rejected", value: "REFUND_CANCELED", title: "Rejected" },
];
export const TransactionType = [
  { label: "On-Ramp", value: "ON_RAMP" },
  { label: "Off-Ramp", value: "OFF_RAMP" },
  { label: "Merchant", value: "MERCHANT" },
  { label: "Peer to Peer", value: "PEER_TO_PEER" },
  { label: "Other", value: "OTHER" },
  { label: "External Receive", value: "EXTERNAL_RECEIVE" },
];

export const DebitCredit = [
  { label: "Incoming", value: "INCOMING" },
  { label: "Outgoing", value: "OUTGOING" },
  // { label: "Unknown", value: "UNKNOWN" }
];

export const RefundMode = [
  { label: "Wallet", value: "WALLET" }
  // { label: "Unknown", value: "UNKNOWN" }
];

export const SearchItemsRefund = [
  { label: "Transaction ID", value: "uuid" },
  { label: "Acquirer Transaction ID", value: "extPosTransactionId" },
];

export const refundType = [
  { label: "Partial", value: "PARTIAL" },
  { label: "Full", value: "FULL" },
];

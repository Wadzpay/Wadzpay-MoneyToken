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
  | "WTK"
  | "USDC"
  | "SAR";

export type TransactionStatus =
  | "SUCCESSFUL"
  | "FAILED"
  | "IN_PROGRESS"
  | "UNDERPAID"
  | "OVERPAID";

export type ExternalPOS =
  | "EXT_POS_ID"
  | "EXT_POS_SEQUENCE_NO"
  | "EXT_POS_TRANSACTION_ID";

export type TransactionDirection = "INCOMING" | "OUTGOING" | "UNKNOWN";
export type TransactionType =
  | "ON_RAMP"
  | "OFF_RAMP"
  | "MERCHANT"
  | "PEER_TO_PEER"
  | "OTHER";

export type CryptoFullName =
  | "Bitcoin"
  | "Ethereum"
  | "Tether"
  | "WTK"
  | "USD Coin"
  | "SART";
export type Asset = "BTC" | "ETH" | "USDT" | "WTK" | "USDC" | "SART";
export type TokenToAmount = { [key in Asset]: number };
export type FiatTokenToAmount = { [key in FiatAsset]: number };

// fiat currencies and signs are part of the config from BE we need to use that
export type FiatSign =
  | "$"
  | "€"
  | "₹"
  | "Rp"
  | "S$"
  | "£"
  | "₨"
  | "₱"
  | "AED"
  | "WTK"
  | "USDC"
  | "SAR";

export const FiatSignMap: { [key in FiatAsset]: FiatSign } = {
  USD: "$",
  EUR: "€",
  INR: "₹",
  IDR: "Rp",
  SGD: "S$",
  GBP: "£",
  PKR: "₨",
  PHP: "₱",
  AED: "AED",
  WTK: "WTK",
  USDC: "USDC",
  SAR: "SAR",
};

export const availableCountries = [
  { label: "USA", value: "US", phoneNumberPrefix: "+1" },
  { label: "Australia", value: "AU", phoneNumberPrefix: "+61" },
  { label: "Indonesia", value: "ID", phoneNumberPrefix: "+62" },
  { label: "India", value: "IN", phoneNumberPrefix: "+91" },
  { label: "Czech Republic", value: "CZ", phoneNumberPrefix: "+420" },
  { label: "Hungary", value: "HU", phoneNumberPrefix: "+36" },
  { label: "Philippines", value: "PH", phoneNumberPrefix: "+63" },
  { label: "Singapore", value: "SG", phoneNumberPrefix: "+65" },
  { label: "Slovakia", value: "SK", phoneNumberPrefix: "+421" },
  {
    label: "The Kingdom of Saudi Arabia",
    value: "SA",
    phoneNumberPrefix: "+966",
  },
  { label: "The United Arab Emirates", value: "AE", phoneNumberPrefix: "+971" },
  { label: "Ukraine", value: "UA", phoneNumberPrefix: "+380" },
  { label: "United Kingdom", value: "UK", phoneNumberPrefix: "+44" },
];

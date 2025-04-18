import { useEffect, useState } from "react"
import { AssetFractionDigits, FiatAssetFractionDigits } from "src/api/constants"
import { useGetExchangeRate } from "src/api/onRamp"
import {
  Asset,
  FiatAsset,
  FiatTokenToAmount,
  TokenToAmount
} from "src/constants/types"

type CurrencyDisplay = "symbol" | "narrowSymbol" | "code" | "name"
type Style = "decimal" | "currency" | "percent" | "unit"

type CurrencyAmountFormatter = (
  amount: number,
  opts: {
    assetFractionDigits?: TokenToAmount
    fiatAssetFractionDigits?: FiatTokenToAmount
    maxFractionDigits?: number
    locale?: string
    asset?: Asset
    fiatAsset?: FiatAsset
    currency?: string
    currencyDisplay?: CurrencyDisplay
    style?: Style
  }
) => string

export const useAssetFractionDigits: () => TokenToAmount = () => {
  const [assetFractionDigits, setAssetFractionDigits] =
    useState<TokenToAmount>(AssetFractionDigits)
  const { data: exchangeRatesData } = useGetExchangeRate("USD")

  useEffect(() => {
    if (exchangeRatesData) {
      setAssetFractionDigits(
        Object.keys(assetFractionDigits).reduce(
          (newAssetFractionDigits, key) => {
            return {
              ...newAssetFractionDigits,
              [key]: exchangeRatesData[key as Asset]
                ? Math.ceil(-Math.log10(exchangeRatesData[key as Asset] / 1000))
                : assetFractionDigits[key as Asset]
            }
          },
          {} as TokenToAmount
        )
      )
    }
  }, [exchangeRatesData])

  return assetFractionDigits
}
const formatCurrencyAmount: CurrencyAmountFormatter = (amount, opts) => {
  const {
    maxFractionDigits,
    locale,
    asset,
    fiatAsset,
    assetFractionDigits,
    fiatAssetFractionDigits,
    style,
    currency,
    currencyDisplay
  } = opts ?? {}

  const maximumFractionDigits =
    maxFractionDigits ??
    (asset && assetFractionDigits && !isNaN(assetFractionDigits[asset])
      ? assetFractionDigits[asset]
      : fiatAsset && fiatAssetFractionDigits
      ? fiatAssetFractionDigits[fiatAsset]
      : undefined)

  let localeVar = locale

  if (currency === "IDR") {
    localeVar = "id-ID"
  } else if (currency === "SGD") {
    localeVar = "zh-sg"
  } else if (currency === "PKR") {
    localeVar = "en-PK"
  } else if (currency === "AED") {
    localeVar = "ar-AE"
  }

  const num = Intl.NumberFormat(localeVar, {
    ...{
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    },
    ...(currency && { currency }),
    ...(currencyDisplay && { currencyDisplay }),
    ...(style && { style })
  }).format(amount)

  const rgx = /^[0-9]*\.?[0-9]*$/
  if (rgx.test(num)) {
    return parseFloat(num).toFixed(2)
  }

  const regexNum = /^[0-9.,]*$/
  if (regexNum.test(num) && num.includes(",")) {
    const parsedNum = parseFloat(num.replaceAll(",", ""))
    return parsedNum.toFixed(2)
  }

  return num
}

const useFormatCurrencyAmount: (opts?: {
  locale: string
}) => CurrencyAmountFormatter = (opts = { locale: "en" }) => {
  const { locale } = opts
  const assetFractionDigits = useAssetFractionDigits()

  return (
    amount: number,
    opts: {
      maxFractionDigits?: number
      locale?: string
      asset?: Asset
      fiatAsset?: FiatAsset
      currency?: string
      currencyDisplay?: CurrencyDisplay
      style?: Style
    }
  ) =>
    formatCurrencyAmount(amount, {
      locale,
      assetFractionDigits,
      fiatAssetFractionDigits: FiatAssetFractionDigits,
      ...opts
    })
}

export default useFormatCurrencyAmount

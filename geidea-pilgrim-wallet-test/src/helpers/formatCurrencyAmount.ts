import { useEffect, useState } from "react"

import { AssetFractionDigits, FiatAssetFractionDigits } from "~/api/constants"
import {
  Asset,
  FiatAsset,
  FiatTokenToAmount,
  TokenToAmount
} from "~/constants/types"
import { useGetExchangeRate } from "~/api/onRamp"

import "intl"
// TODO import localizations that are used for formatting numbers
import "intl/locale-data/jsonp/en"

type CurrencyAmountFormatter = (
  amount: number,
  opts: {
    assetFractionDigits?: TokenToAmount
    fiatAssetFractionDigits?: FiatTokenToAmount
    maxFractionDigits?: number
    locale?: string
    asset?: Asset
    fiatAsset?: FiatAsset
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
              [key]: AssetFractionDigits[key as Asset]
              // [key]: Math.ceil(
              //   -Math.log10(exchangeRatesData[key as Asset] / 1000)
              // )
            }
          },
          {} as TokenToAmount
        )
      )
    }
  }, [exchangeRatesData])

  return assetFractionDigits
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
    }
  ) =>


    formatCurrencyAmount(amount, {
      locale,
      assetFractionDigits,
      fiatAssetFractionDigits: FiatAssetFractionDigits,
      ...opts
    })

}

const formatCurrencyAmount: CurrencyAmountFormatter = (amount, opts) => {
  const {
    maxFractionDigits,
    locale,
    asset,
    fiatAsset,
    assetFractionDigits,
    fiatAssetFractionDigits
  } = opts ?? {}

  const maximumFractionDigits =
    maxFractionDigits ??
    (asset && assetFractionDigits
      ? assetFractionDigits[asset]
      : fiatAsset && fiatAssetFractionDigits
      ? fiatAssetFractionDigits[fiatAsset]
      : undefined)

    
// console.log("maximumFractionDigits ", maximumFractionDigits)
  // return Intl.NumberFormat(locale, {
  //   maximumFractionDigits
  // }).format(amount)

  return amount.toFixed(2) // AS mandatory to show 2 decimal places after number 
}

export default useFormatCurrencyAmount

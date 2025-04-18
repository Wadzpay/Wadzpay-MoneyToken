import { EXCHANGE_RATES_REFRESH_TIME } from "src/constants"
import { FiatAsset, TokenToAmount } from "src/constants/types"

import { EndpointKey, useApi } from "./constants"

import { useGet } from "./index"

export const useGetExchangeRate = (fiat: FiatAsset) =>
  useGet<TokenToAmount>(
    [EndpointKey.EXCHANGE_RATES, fiat],
    useApi().exchangeRates(fiat),
    {
      staleTime: EXCHANGE_RATES_REFRESH_TIME
    }
  )

export const useGetExchangeRateForRefund = (fiat: FiatAsset) =>
  useGet<TokenToAmount>(
    [EndpointKey.REFUND_EXCHANGE_RATES, fiat],
    useApi().refundExchangeRates(fiat),
    {
      staleTime: EXCHANGE_RATES_REFRESH_TIME
    }
  )

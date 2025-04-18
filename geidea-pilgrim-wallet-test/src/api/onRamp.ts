/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { OnRampConfigData } from "./models"
import { EndpointKey, useApi } from "./constants"

import { useGet } from "./index"

import { FiatAsset, TokenToAmount } from "~/constants/types"
import { EXCHANGE_RATES_REFRESH_TIME } from "~/constants"

export const useOnRampConfig = () =>
  useGet<OnRampConfigData>(
    EndpointKey.ON_RAMP_CONFIG,
    useApi().onRampConfig(),
    {}
  )

export const useGetExchangeRate = (fiat: FiatAsset) =>
  useGet<TokenToAmount>(
    [EndpointKey.EXCHANGE_RATES, fiat],
    useApi().exchangeRates(fiat),
    {
      staleTime: EXCHANGE_RATES_REFRESH_TIME
    }
  )

export const useGetP2pFee = () =>
  useGet<number>(EndpointKey.P2P_FEE, useApi().p2pFee(), {})

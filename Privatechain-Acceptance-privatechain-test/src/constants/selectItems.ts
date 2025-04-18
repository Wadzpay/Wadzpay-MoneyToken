import { Asset } from "./types"
import { useTranslationItems } from "./translationItems"
export const useAssetSelectItems: (useShortNames?: boolean) => [{ label: string; value: string }, { label: string; value: string }, { label: string; value: string }] = (useShortNames = false) => {
  const { asset } = useTranslationItems()
  return [
    { label: useShortNames ? "BTC" : asset.BTC, value: "BTC" },
    { label: useShortNames ? "ETH" : asset.ETH, value: "ETH" },
    { label: useShortNames ? "USDT" : asset.USDT, value: "USDT" }
  ]
}

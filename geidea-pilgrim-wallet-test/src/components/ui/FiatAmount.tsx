import React from "react"

import Typography from "./Typography"
import { TypographyColorVariant, TypographyVariant } from "./theme"

import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { FiatAsset, FiatSignMap } from "~/constants/types"

type FiatAmountProps = {
  amount: number
  fiatAsset: FiatAsset
  variant?: TypographyVariant
  color?: TypographyColorVariant
}

const FiatAmount: React.FC<FiatAmountProps> = ({
  amount,
  fiatAsset,
  variant,
  color
}: FiatAmountProps) => {
  const formatter = useFormatCurrencyAmount()
  const fiatSign = FiatSignMap[fiatAsset]

  return (
    <Typography
    fontFamily="Rubik-Medium"
      variant={variant ? variant : "body"}
      color={color ? color : "grayLight"}
    >
      {fiatAsset}
      {fiatSign.length > 1 ? " " : ""}
      {formatter(amount, { fiatAsset })}
    </Typography>
  )
}

export default FiatAmount

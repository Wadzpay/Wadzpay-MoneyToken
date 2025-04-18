import React, { useContext } from "react"
import { StyleSheet, TouchableOpacity } from "react-native"

import {
  Container,
  FiatAmount,
  Icon,
  LoadingSpinner,
  theme,
  Typography
} from "~/components/ui"
import { useTranslationItems } from "~/constants/translationItems"
import { Asset, AssetIconNamesMap } from "~/constants/types"
import { UserContext } from "~/context"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"

const getStyles = (isSelected: boolean, isZeroBalance: boolean) =>
  StyleSheet.create({
    row: {
      justifyContent: "space-between",
      paddingVertical: theme.spacing.xs,
      borderRadius: theme.borderRadius.md,
      borderWidth: 0,
      opacity: isZeroBalance ? 0.5 : 1,
      ...(isSelected
        ? {
            borderColor: theme.colors.orange,
            backgroundColor: `${theme.colors.yellow}25`
          }
        : {
            borderColor: theme.colors.white
          })
    },
    rowTitle: {
      flex: 1
    }
  })

type Props = {
  asset: Asset
  amount: number
  fiatAmount: number
  isSelected: boolean
  onPress?: () => void
  isLoading?: boolean
  isRecievedScreen?: boolean
}

const CurrencyItem: React.FC<Props> = ({
  asset,
  amount,
  fiatAmount,
  isSelected,
  onPress,
  isLoading = false,
  isRecievedScreen = false
}: Props) => {
  const formatter = useFormatCurrencyAmount()
  const { fiatAsset } = useContext(UserContext)
  const assetTranslationItems = useTranslationItems().assetShort
  const isZeroBalance = amount === 0 && !isRecievedScreen

  const styles = getStyles(isSelected, isZeroBalance)

  // console.log("isRecievedScreen ", isRecievedScreen)
  const currencyItem = (
    <Container
      direction="row"
      justify="flex-start"
      alignItems="center"
      style={styles.row}
    >
      <Container direction="row" spacing={1}>
        <Icon name={AssetIconNamesMap[asset]} size="xl" />
        <Container justify="center" noItemsStretch style={styles.rowTitle}>
          <Typography color={isSelected ? "orange" : "grayDark"}>
            {asset}
          </Typography>
          <Typography color={isSelected ? "grayDark" : "grayMedium"}>
            {assetTranslationItems[asset]}
          </Typography>
        </Container>
      </Container>

      {isLoading ? (
        <LoadingSpinner color="orange" />
      ) : (
        !isRecievedScreen && (
          <Container justify="space-between" alignItems="center" noItemsStretch>
            <Typography
              color={
                isSelected ? "orange" : isZeroBalance ? "error" : "grayDark"
              }
            >
              {formatter(amount, { asset })} {asset}
            </Typography>

            <FiatAmount
              amount={fiatAmount}
              fiatAsset={fiatAsset}
              variant="label"
              color={isSelected ? "grayDark" : "grayMedium"}
            />
          </Container>
        )
      )}
    </Container>
  )
  return onPress ? (
    <TouchableOpacity
      onPress={onPress}
      disabled={!isRecievedScreen ? isZeroBalance : false}
    >
      {currencyItem}
    </TouchableOpacity>
  ) : (
    currencyItem
  )
}

export default CurrencyItem

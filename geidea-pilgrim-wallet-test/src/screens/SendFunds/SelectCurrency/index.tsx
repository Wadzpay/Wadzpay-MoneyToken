import React, { useContext } from "react"
import { useTranslation } from "react-i18next"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import { LogBox, StyleSheet } from "react-native"

import CurrencyItem from "./CurrencyItem"

import {
  Container,
  ScreenLayoutTab,
  LoadingSpinner,
  ErrorModal,
  theme
} from "~/components/ui"
import { SendFundsStackNavigatorParamList } from "~/components/navigators"
import { useUserBalances } from "~/api/user"
import { Asset } from "~/constants/types"
import { useGetExchangeRate } from "~/api/onRamp"
import { UserContext } from "~/context"

const styles = StyleSheet.create({
  container: {
    marginHorizontal: theme.spacing.lg
  }
})

type Props = NativeStackScreenProps<
  SendFundsStackNavigatorParamList,
  "SelectCurrency"
>

const SelectCurrency: React.FC<Props> = ({ route, navigation }: Props) => {
  const { t } = useTranslation()
  const { fiatAsset } = useContext(UserContext)
  const { selectedAsset, onSelectedAssetChange, isRecievedScreen } =
    route.params
  const {
    data: balanceData,
    isFetching: isFetchingBalance,
    isFetched: isFetchedBalances,
    refetch: refetchBalance,
    error: errorBalance
  } = useUserBalances()
  const {
    data: exchangeRatesData,
    isFetching: isFetchingExhchangeRates,
    refetch: refetchExchangeRates
  } = useGetExchangeRate(fiatAsset)

  // Warning that a function as a screen param can break deep
  // linking and state persistance (doesn't apply to this screen)
  LogBox.ignoreLogs([
    "Non-serializable values were found in the navigation state"
  ])

  const _onSelectedAssetChange = (asset: Asset) => {
    if (isRecievedScreen || (balanceData && balanceData[asset] > 0)) {
      onSelectedAssetChange(asset)
      navigation.goBack()
    }
  }

  const refetch = () => {
    refetchExchangeRates()
    refetchBalance()
  }
  

  return (
    <ScreenLayoutTab
      title={t("Digital Currency")}
      leftIconName="ArrowLeft"
      onLeftIconClick={navigation.goBack}
      refreshing={isFetchingBalance || isFetchingExhchangeRates}
      onRefresh={refetch}
    >
      <ErrorModal error={errorBalance} />
      {isFetchingBalance || isFetchingExhchangeRates ? (
        <Container alignItems="center" noItemsStretch>
          <LoadingSpinner color="orange" />
        </Container>
      ) : (
        balanceData &&
        isFetchedBalances && (
          <Container spacing={1} style={styles.container}>
            {Object.entries(balanceData).map(([token, amount]) => {
              const asset = token as Asset
              const isSelected = selectedAsset === asset
                return (
                  <CurrencyItem
                    key={asset}
                    asset={asset}
                    onPress={() => _onSelectedAssetChange(asset)}
                    amount={amount}
                    fiatAmount={
                      exchangeRatesData ? amount / exchangeRatesData[asset] : 0
                    }
                    isSelected={isSelected}
                    isRecievedScreen={isRecievedScreen}
                  />
                )
              
            })}
          </Container>
        )
      )}
    </ScreenLayoutTab>
  )
}

export default SelectCurrency

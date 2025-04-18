import React, { useContext, useState } from "react"
import { useTranslation } from "react-i18next"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import { LogBox, StyleSheet, TouchableOpacity, View } from "react-native"

import {
  Container,
  ScreenLayoutTab,
  LoadingSpinner,
  ErrorModal,
  theme,
  Typography,
  NumericKeyboard,
  Button,
  FiatAmount
} from "~/components/ui"
import { SendFundsStackNavigatorParamList } from "~/components/navigators"
import { useUserBalances } from "~/api/user"
import { isNumeric } from "~/utils"
import { UserContext } from "~/context"
import { KEY } from "~/components/ui/NumericKeyboard"
import { useGetExchangeRate } from "~/api/onRamp"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"

const DISABLED_KEYS = [
  KEY.ONE,
  KEY.TWO,
  KEY.THREE,
  KEY.FOUR,
  KEY.FIVE,
  KEY.SIX,
  KEY.SEVEN,
  KEY.EIGHT,
  KEY.NINE,
  KEY.ZERO,
  KEY.DECIMAL_POINT
]

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: theme.spacing.lg,
  },
  balanceContainer: {
    height: theme.spacing.xxl
   
  },
  buttonContainer: {
    justifyContent: 'flex-end',
    marginBottom:40,
    marginHorizontal: theme.spacing.md
  }
})

type Props = NativeStackScreenProps<SendFundsStackNavigatorParamList, "EnterAmount">

const EnterAmount: React.FC<Props> = ({ route, navigation }: Props) => {
  const { t } = useTranslation()
  const formatter = useFormatCurrencyAmount()
  const { asset, amount, onAmountChange, isRecievedScreen } = route.params
  const { fiatAsset } = useContext(UserContext)
  const { data: exchangeRatesData, isFetching: isFetchingExchangeRates } =
    useGetExchangeRate(fiatAsset)
  const [cryptoAmount, setCryptoAmount] = useState<string>(amount.toString())
  const {
    data: balancesData,
    isFetching: isFetchingBalances,
    isFetched: isFetchedBalances,
    error
  } = useUserBalances()
  const cryptoAmountNumber = isNumeric(cryptoAmount)
    ? Number(cryptoAmount)
    : NaN
  const isCryptoAmountAvailable =
    balancesData && balancesData[asset] >= cryptoAmountNumber

  // Warning that a function as a screen param can break deep
  // linking and state persistance (doesn't apply to this screen)
  LogBox.ignoreLogs([
    "Non-serializable values were found in the navigation state"
  ])

  const onConfirmAmount = () => {
    if (isNumeric(cryptoAmount)) {
      // console.log("onConfirmAmount ", Number(cryptoAmount))
      onAmountChange(Number(cryptoAmount))
      navigation.goBack()
    }
  }

  return (
    <ScreenLayoutTab
      title={t("Enter Amount")}
      leftIconName="ArrowLeft"
      onLeftIconClick={navigation.goBack}
      useScrollView={false}
    >
      <ErrorModal error={error} />
      <Container justify="space-evenly"  style={styles.container}>
        <Container alignItems="center"   justify="flex-start"noItemsStretch>
          <Container direction="row" alignItems="center" noItemsStretch>
            <Typography
              variant="title"
              fontWeight="bold"
              color={
                !isRecievedScreen && !isCryptoAmountAvailable
                  ? "error"
                  : "grayDark"
              }
            >
              {cryptoAmount}{" "}
            </Typography>
            <Typography
              variant="title"
              color={
                !isRecievedScreen && !isCryptoAmountAvailable
                  ? "error"
                  : "grayDark"
              }
            >
              {asset}
            </Typography>
          </Container>

          <Typography>
            <FiatAmount
              amount={
                exchangeRatesData
                  ? cryptoAmountNumber / exchangeRatesData[asset]
                  : 0
              }
              fiatAsset={fiatAsset}
            />
          </Typography>
        </Container>

        {(!isRecievedScreen && isFetchingBalances) ||
        isFetchingExchangeRates ? (
          <Container alignItems="center" noItemsStretch>
            <LoadingSpinner color="orange" />
          </Container>
        ) : (
          !isRecievedScreen &&
          balancesData &&
          isFetchedBalances && (
            // <TouchableOpacity
            //   onPress={() => setCryptoAmount(balancesData[asset].toString())}
            // >
            <Container>
              <Container
                direction="row"
                justify="center"
                alignItems="center"
                noItemsStretch
                spacing={1}
                style={styles.balanceContainer}
              >
                <Typography
                  color={isCryptoAmountAvailable ? "grayMedium" : "error"}
                >
                  {t("Available")}:
                </Typography>
                <Typography
                  color={isCryptoAmountAvailable ? "orange" : "error"}
                >
                  {formatter(balancesData[asset], { asset })} {asset}
                </Typography>
              </Container>
              {!isCryptoAmountAvailable ? (
                <Typography
                  variant="body"
                  color={isCryptoAmountAvailable ? "grayMedium" : "error"}
                >
                  {t("Payment Amount is more than wallet balance")}
                </Typography>
              ) : null}
            </Container>
            // </TouchableOpacity>
          )
        )}

        <NumericKeyboard
          value={cryptoAmount}
          onChange={(value) => {
            // console.log("value ", value)
            setCryptoAmount(value)
          }}
          isValueAmount
          disabledKeys={
            isRecievedScreen ? [] : isCryptoAmountAvailable ? [] : DISABLED_KEYS
          }
        />
        <View style={styles.buttonContainer}>
          <Button
            text={t("Confirm")}
            onPress={onConfirmAmount}
            disabled={isRecievedScreen ? false : !isCryptoAmountAvailable}
          />
        </View>
      </Container>
    </ScreenLayoutTab>
  )
}

export default EnterAmount

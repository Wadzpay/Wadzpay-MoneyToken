import { NativeStackScreenProps } from "@react-navigation/native-stack"

import React, { useContext, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  TouchableOpacity,
  View,
  StyleSheet,
  Dimensions,
  Alert
} from "react-native"
import { useGetExchangeRate } from "~/api/onRamp"
import { useAddresses, useUserBalances, useUserContacts } from "~/api/user"
import { SendFundsStackNavigatorParamList } from "~/components/navigators"
import { RecieveFundsStackNavigatorParamList } from "~/components/navigators/RecieveFundsStackNavigator"
import {
  Button,
  Container,
  Icon,
  ScreenLayoutTab,
  SelectField,
  theme,
  Typography
} from "~/components/ui"
import { useValidationSchemas, WADZPAY_WALLET } from "~/constants"
import { Asset, AssetIconNamesMap } from "~/constants/types"
import { UserContext } from "~/context"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import ContactItem from "../SendFunds/SelectContact/ContactItem"
import CurrencyItem from "../SendFunds/SelectCurrency/CurrencyItem"
import RadioButtonRN from "radio-buttons-react-native"
import { useSendModeSelectItems } from "../SendFunds"
import { useForm } from "react-hook-form"
import { SelectSendWalletForm } from "~/constants/formTypes"

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.xl

type Props = NativeStackScreenProps<
  RecieveFundsStackNavigatorParamList,
  "ReceiveFunds"
>

const ReceiveFunds: React.FC<Props> = ({ navigation, route }: Props) => {
  const { t } = useTranslation()
  const [requestMode, setRequestMode] = useState("")
  const { asset, cognitoUsername, amount } = route.params ?? {}
  const { fiatAsset } = useContext(UserContext)
  const { depositSchema } = useValidationSchemas()
  const formatter = useFormatCurrencyAmount()
  const sendFunds = useSendModeSelectItems()
  // const [walletMode, setWalletMode] = useState("")

  // let onChangeWalletSelector = (value: string) => {
  //   console.log("value" , value)
  //   setWalletMode(value)
  // }
  // console.log("walletMode" , walletMode)
  // console.log("WADZPAY" , WADZPAY_WALLET)
  const { control } = useForm<SelectSendWalletForm>({
    defaultValues: { sendFundType: "WadzpayWallet" }
  })
  const [transaction, setTransaction] = useState<{
    asset: Asset
    cognitoUsername: string
    amount: number
  }>({
    asset: asset ?? "ETH",
    cognitoUsername: cognitoUsername ?? "",
    amount: amount ?? 0
  })

  const {
    data: contacts,
    isFetching: contactsIsFetching,
    error: contactsError
  } = useUserContacts()

  const {
    data: balances,
    isFetching: balancesIsFetching,
    error: balancesError
  } = useUserBalances()

  const { data: exchangeRates, isFetching: exchangeRatesIsFetching } =
    useGetExchangeRate(fiatAsset)

  //TODO Nandani API error
  // BTC address ----> 2NE3u7ZPrVZRYC5NZfRYSzGmx5Xfj5gGnq2
  const {
    data: addresses,
    isFetching: addressesIsFetching,
    error: addressesError
  } = useAddresses()

  //  console.log("addresses", addresses?.length)
  // console.log("find particular value " , addresses?.find(obj => obj?.asset == "WTK"))
  const walletAddress = addresses?.find(
    (obj) => obj?.asset === transaction.asset
  )?.address
  // const walletAddress = "wefwehrfkwehuriuwehrweiuhr"
  // console.log("walletAddress ", walletAddress)

  const contact = contacts?.find((c) => c.id === transaction.cognitoUsername)

  const getBalancesAmount = () => {
    return balances && balances[transaction.asset]
      ? balances[transaction.asset]
      : 0
  }

  let requestFundconfirmation = () => {
    {
      requestMode === WADZPAY_WALLET
        ? !contact?.email
          ? Alert.alert("Please Enter Request From")
          : (transaction.asset && !transaction.amount) ||
            transaction.amount === 0
          ? Alert.alert("Please Enter Amount")
          : navigation.navigate("RecieveFundsConfirmation", {
              contact: contact,
              asset: transaction.asset,
              amount: transaction.amount
            })
        : transaction.asset && !walletAddress
        ? Alert.alert("Wallet Address is not available")
        : !transaction.amount || transaction.amount === 0
        ? Alert.alert("Please Enter Amount")
        : navigation.navigate("RecieveFundsConfirmation", {
            walletAddress: walletAddress,
            asset: transaction.asset,
            amount: transaction.amount
          })
    }
  }

  const data = [
    {
      label: "Internal"
    }
  ]

  return (
    <ScreenLayoutTab title={t("Receive Payment")}>
      <Container spacing={1} style={styles.container}>
        <Typography fontWeight="bold" textAlign="left">
          
        </Typography>
        {requestMode === WADZPAY_WALLET ? undefined : 
        <RadioButtonRN
          style={styles.radioStyle}
          animationTypes={["pulse"]}
          initial={1}
          data={data}
          selectedBtn={(e: any) => {
            setRequestMode(e.label)
          }}
          box={false}
          circleSize={14}
          activeColor={"#FFC235"}
          deactiveColor={theme.colors.gray.light}
          boxStyle={{ flex: 1, alignItems: "center" }}
        /> }
      </Container>
      {requestMode === WADZPAY_WALLET ? (
        <Container spacing={1} style={styles.container}>
          <Typography fontWeight="bold" textAlign="left">
            Request From
          </Typography>
          <TouchableOpacity
            onPress={() =>
              navigation.navigate("SelectContact", {
                cognitoUsername: transaction.cognitoUsername || "",
                title: "Request From" || "",
                onCognitoUsernameChange: (cognitoUsername: string) =>
                  setTransaction({ ...transaction, cognitoUsername })
              })
            }
          >
            <View style={styles.selectItem}>
              <ContactItem
                nickname={contact ? contact.nickname : ""}
                email={contact ? contact.email : ""}
                isLoading={contactsIsFetching}
              />
            </View>
          </TouchableOpacity>
        </Container>
      ) : null}
      <Container spacing={1} style={styles.container}>
        <Typography fontWeight="bold" textAlign="left">
          {t("Digital Currency")}
        </Typography>
        <TouchableOpacity
          onPress={() =>
            navigation.navigate("SelectCurrency", {
              selectedAsset: transaction.asset || "",
              onSelectedAssetChange: (asset: Asset) =>
                setTransaction({ ...transaction, asset, amount: 0 }),
              isRecievedScreen: true
            })
          }
        >
          <Container
            justify="space-evenly"
            spacing={1}
            style={styles.selectCurrency}
          >
            <CurrencyItem
              asset={transaction.asset}
              amount={getBalancesAmount()}
              fiatAmount={
                exchangeRates
                  ? getBalancesAmount() / exchangeRates[transaction.asset]
                  : 0
              }
              isSelected={false}
              isLoading={balancesIsFetching || exchangeRatesIsFetching}
              isRecievedScreen={true}
            />
          </Container>
        </TouchableOpacity>
      </Container>

      <Container spacing={1} style={styles.container}>
        <Typography textAlign="left" fontWeight="bold">
          {t("Enter Amount")}
        </Typography>
        <TouchableOpacity
          onPress={() =>
            navigation.navigate("EnterAmount", {
              asset: transaction.asset || "",
              amount: transaction.amount || 0,
              onAmountChange: (amount: number) =>
                setTransaction({ ...transaction, amount }),
              isRecievedScreen: true
            })
          }
        >
          <Container
            direction="row"
            alignItems="center"
            noItemsStretch
            spacing={1}
            style={[styles.selectItem, {  flexWrap: "wrap", paddingVertical: theme.spacing.xs }]}
          >
            <Container direction="row" alignItems="center" spacing={1}>
              <Icon name={AssetIconNamesMap[transaction.asset]} size="xl" />
            </Container>

            <Typography textAlign="right">
              {formatter(transaction.amount, {
                asset: transaction.asset
              })}
            </Typography>
          </Container>
        </TouchableOpacity>
      </Container>

      <Container spacing={1} style={styles.container}>
        <View style={styles.buttonContainer}>
          <Button
            text={"Request"}
            onPress={() => {
              requestFundconfirmation()
            }}
          />

        <View style={{ height: 20 }}></View>

          <Button
          style={{marginTop:10}}
            text={"Cancel"}
            variant={"secondary"}
            onPress={() => {
              setTransaction({
                asset: asset ?? "ETH",
                cognitoUsername: cognitoUsername ?? "",
                amount: amount ?? 0
              })
              navigation.navigate("Home")
            }}
          />

        </View>
      </Container>
    </ScreenLayoutTab>
  )
}

const styles = StyleSheet.create({
  selectItem: {
    flexDirection: "row",
    minWidth:
      width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs,
    minHeight: 60,
    borderColor: "#E8E8E8",
    backgroundColor: "#F1F1F1",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: theme.spacing.xs,
    borderRadius: theme.borderRadius.md,
    borderWidth: theme.borderWidth.sm
  },
  selectCurrency: {
    flexDirection: "row",
    borderColor: "#E8E8E8",
    backgroundColor: "#F1F1F1",
    justifyContent: "space-between",
    paddingHorizontal: theme.spacing.xs,
    borderRadius: theme.borderRadius.md,
    borderWidth: theme.borderWidth.sm
  },
  container: {
    flex: 1,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: theme.spacing.lg
  },
  buttonContainer: {
    flex: 1,
    justifyContent: "flex-end",
    marginBottom: 36,
    marginTop: 10,
  },
  radioStyle: {
    flexWrap: "wrap",
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-around",
    borderColor: "#E8E8E8",
    backgroundColor: "#F1F1F1",
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.xs,
    borderRadius: theme.borderRadius.md,
    borderWidth: theme.borderWidth.sm,
    minHeight: 60
  }
})

export default ReceiveFunds

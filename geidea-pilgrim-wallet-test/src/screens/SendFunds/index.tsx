import React, { useContext, useState } from "react"
import { useTranslation } from "react-i18next"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import {
  StyleSheet,
  View,
  Dimensions,
  TouchableOpacity,
  TextInput,
  Alert
} from "react-native"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"

import CurrencyItem from "./SelectCurrency/CurrencyItem"
import ContactItem from "./SelectContact/ContactItem"

import { SendFundsStackNavigatorParamList } from "~/components/navigators"
import {
  Button,
  Checkbox,
  Container,
  ErrorModal,
  FiatAmount,
  Icon,
  ScreenLayoutTab,
  SelectField,
  TextField,
  theme,
  Typography
} from "~/components/ui"
import { Asset, AssetIconNamesMap, SendFundType } from "~/constants/types"
import { useUserBalances, useUserContacts } from "~/api/user"
import { useGetExchangeRate, useGetP2pFee } from "~/api/onRamp"
import { UserContext } from "~/context"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { useTranslationItems } from "~/constants/translationItems"
import { fieldProps, useValidationSchemas, WADZPAY_WALLET } from "~/constants"
import { SelectSendWalletForm } from "~/constants/formTypes"
import { AssetFractionDigits } from "~/api/constants"
import { calculateEstimatedFee } from "~/utils"
import RadioButtonRN from "radio-buttons-react-native"

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.xl

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: theme.spacing.lg
  },
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
  selectFeeStyle: {
    flexDirection: "row",
    minWidth:
      width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs,
    minHeight: 60,
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: theme.spacing.xs
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
  contact: {
    paddingHorizontal: theme.spacing.xs
  },
  amount: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    padding: theme.spacing.md
  },
  amountText: {
    flex: 1
  },
  buttonContainer: {
    marginTop: 30,
  },
  checkboxContainer: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 20
  },
  checkbox: {
    alignSelf: "center"
  },
  label: {
    margin: 8
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

type Props = NativeStackScreenProps<SendFundsStackNavigatorParamList, "SendFunds">
export const useSendModeSelectItems: (useShortNames?: boolean) => {
  label: string
  value: SendFundType
}[] = (useShortNames = false) => {
  const { sendFundType } = useTranslationItems()
  return [
    {
      label: useShortNames ? "WadzPay" : sendFundType.WadzpayWallet,
      value: "WadzPay"
    },
    {
      label: useShortNames ? "External" : sendFundType.ExternalWallet,
      value: "External"
    }
  ]
}

const SendFunds: React.FC<Props> = ({ navigation, route }: Props) => {
  const { t } = useTranslation()
  const { asset, cognitoUsername, amount } = route.params ?? {}
  const { fiatAsset } = useContext(UserContext)
  const { data: p2pFee } = useGetP2pFee()
  const P2P_FEE_PERCENTAGE = p2pFee ? p2pFee : 0
  const formatter = useFormatCurrencyAmount()
  // TODO remake with a useForm instead - validation will be needed
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
    data: balances,
    isFetching: balancesIsFetching,
    error: balancesError
  } = useUserBalances()
  const {
    data: contacts,
    isFetching: contactsIsFetching,
    error: contactsError
  } = useUserContacts()
  const { data: exchangeRates, isFetching: exchangeRatesIsFetching } =
    useGetExchangeRate(fiatAsset)

  // console.log("nandani ER ", exchangeRates, fiatAsset)
  const contact = contacts?.find((c) => c.id === transaction.cognitoUsername)
  const getBalancesAmount = () => {
    return balances && balances[transaction.asset]
      ? balances[transaction.asset]
      : 0
  }
  const { depositSchema } = useValidationSchemas()
  const {
    control,
    setValue,
    watch,
    handleSubmit,
    formState: { errors }
  } = useForm<SelectSendWalletForm>({
    resolver: yupResolver(depositSchema),
    defaultValues: { sendFundType: "WadzpayWallet" }
  })

  const [requestMode, setRequestMode] = useState("")
  const sendFunds = useSendModeSelectItems()
  const [walletMode, setWalletMode] = useState("")
  const [walletAddress, onChangeWalletAddress] = useState("")
  const [isSelected, setSelection] = useState(false)
  const isWadzpayWallet = requestMode && requestMode === WADZPAY_WALLET

  const data = [
    {
      label: "Internal"
    }
  ]
  let onChangeWalletSelector = (value: string) => {
    setWalletMode(value)
  }

  let onChangeWalletAddressListner = (value: string) => {
    onChangeWalletAddress(value)
  }

  let WalletNavigation = () => {
    // console.log("transaction.amount ", transaction.amount)
    const amount = formatter(transaction.amount, {
      asset: transaction.asset
    })
    // console.log("amount ", amount)
    if (isWadzpayWallet) {
      //internal wallet

      !contact
        ? Alert.alert("Please Enter Recipeint", "", [{ text: "Ok" }])
        : transaction.amount <= 0
        ? Alert.alert("Please Enter Amount", "", [{ text: "Ok" }])
        : navigation.navigate("PaymentSummary", {
            contact: contact,
            asset: transaction.asset,
            amount: amount,
            walletMode: requestMode
          })
    } else {
      //external wallet
      walletAddress === ""
        ? Alert.alert("Please Enter Wallet Address", "", [{ text: "Ok" }])
        : transaction.amount <= 0
        ? Alert.alert("Please Enter Amount", "", [{ text: "Ok" }])
        : navigation.navigate("PaymentSummary", {
            walletAddress: walletAddress,
            asset: transaction.asset,
            amount: amount,
            walletMode: requestMode
          })
    }
  }
  return (
    <ScreenLayoutTab title={t("transfer")} useScrollView={true}>
      <ErrorModal error={balancesError} />
      <ErrorModal error={contactsError} />
      <Container justify="space-between" style={styles.container}>
        <Container spacing={1}>
          <Container spacing={1}>
            <Typography fontWeight="bold" textAlign="left">
             
            </Typography>
            {isWadzpayWallet ? undefined : <RadioButtonRN
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
            />}
          </Container> 
          <Container spacing={1} style={{ marginTop: 10 }}>
            <Typography fontWeight="bold" textAlign="left">
              {t("Digital Currency")}
            </Typography>
            <TouchableOpacity
              onPress={() =>
                navigation.navigate("SelectCurrency", {
                  selectedAsset: transaction.asset || "",
                  onSelectedAssetChange: (asset: Asset) =>
                    setTransaction({ ...transaction, asset, amount: 0 })
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
                />
              </Container>
            </TouchableOpacity>
          </Container>

          {isWadzpayWallet ? (
            <Container spacing={1} style={{ marginTop: 5 }}>
              <Typography fontWeight="bold" textAlign="left">
                {t("recipient_address")}
              </Typography>
              <TouchableOpacity
                onPress={() =>
                  navigation.navigate("SelectContact", {
                    cognitoUsername: transaction.cognitoUsername || "",
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
          ) : (
            requestMode !== "" && (
              <Container spacing={1} style={{ marginTop: 5 }}>
                <Typography fontWeight="bold" textAlign="left">
                  Wallet Address
                </Typography>
                <View style={styles.selectItem}>
                  <View style={{ width: "90%", paddingHorizontal: 10 }}>
                    <TextInput
                      underlineColorAndroid="transparent"
                      placeholder="Enter wallet address or scan QR code"
                      onChangeText={(value) => {
                        onChangeWalletAddressListner(value)
                      }}
                      value={walletAddress}
                    />
                  </View>
                  <TouchableOpacity
                    style={{ flex: 1, paddingHorizontal: 10 }}
                    onPress={() =>
                      navigation.navigate("ScanWalletAddressQrCode", {
                        asset: transaction.asset,
                        onScanQRCode: (
                          walletAddress: string,
                          amount,
                          asset
                        ) => {
                          onChangeWalletAddressListner(walletAddress)

                          setTransaction({ ...transaction, asset, amount })
                          // setTransaction({ ...transaction, amount})
                        }
                        
                      })
                    }
                  >
                    <Icon name={"Scanner"} size="sm" />
                  </TouchableOpacity>
                </View>
              </Container>
            )
          )}

          <Container spacing={1} style={{ marginTop: 5 }}>
            <Typography textAlign="left" fontWeight="bold">
              {t("Enter Amount")}
            </Typography>
            <TouchableOpacity
              onPress={() =>
                navigation.navigate("EnterAmount", {
                  asset: transaction.asset || "",
                  amount: transaction.amount || 0,
                  onAmountChange: (amount: number) =>
                    setTransaction({ ...transaction, amount })
                })
              }
            >
              <Container
                direction="row"
                alignItems="center"
                noItemsStretch
                spacing={1}
                style={[
                  styles.selectItem,
                  { paddingVertical: theme.spacing.xs }
                ]}
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
          {!isWadzpayWallet && requestMode !== "" ? (
            <Container
              direction="row"
              alignItems="center"
              noItemsStretch
              spacing={1}
              style={[
                styles.selectFeeStyle,
                { paddingVertical: theme.spacing.xs }
              ]}
            >
              <Typography fontWeight="bold" textAlign="left">
                Estimated Network Fee
              </Typography>
              <Container alignItems="flex-end" noItemsStretch>
                <Typography
                  textAlign="right"
                  fontWeight="bold"
                  color="grayDark"
                >
                  {calculateEstimatedFee(
                    transaction.amount,
                    P2P_FEE_PERCENTAGE,
                    transaction.asset
                  )}{" "}
                  {transaction.asset}
                </Typography>
                {exchangeRates && (
                  <FiatAmount
                    amount={
                      (transaction.amount * P2P_FEE_PERCENTAGE) /
                      exchangeRates[transaction.asset]
                    }
                    fiatAsset={fiatAsset}
                  />
                )}
              </Container>
            </Container>
          ) : null}
        </Container>

        <View style={styles.buttonContainer}>
          <Button
          style={{marginTop:15}}
            text={"Send"}
            onPress={() => {
              WalletNavigation()
            }}
          />
          <View style={{ height: 20 }}></View>
          <Button
          style={{marginTop:10}}
            text={"Cancel"}
            variant={"secondary"}
            onPress={() => {
              onChangeWalletAddress("")
              setTransaction({
                asset: asset ?? "SART",
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

export default SendFunds
function onChangeText(text: string): void {
  throw new Error("Function not implemented.")
}

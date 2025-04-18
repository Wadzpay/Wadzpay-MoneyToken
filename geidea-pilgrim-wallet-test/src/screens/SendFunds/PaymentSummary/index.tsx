import React, { useContext, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import { Dimensions, StyleSheet, View } from "react-native"
import { CommonActions } from "@react-navigation/native"

import { SendFundsStackNavigatorParamList } from "~/components/navigators"
import {
  Button,
  Container,
  ErrorModal,
  FiatAmount,
  Icon,
  ScreenLayoutTab,
  theme,
  Typography
} from "~/components/ui"
import {
  useAddTransaction,
  useSavePaymentInfo,
  useSendDigitalCurrencyToExternalWallet,
  useUpdatePaymentRequest
} from "~/api/user"
import { AssetIconNamesMap } from "~/constants/types"
import { useTranslationItems } from "~/constants/translationItems"
import { UserContext } from "~/context"
import { useGetExchangeRate, useGetP2pFee } from "~/api/onRamp"
import { WADZPAY_WALLET } from "~/constants"
import { calculateEstimatedFee } from "~/utils"

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.xl
const rowWidth =
  width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: containerMargin,
    marginTop: theme.spacing.md,
    marginBottom: theme.spacing.lg
  },
  row: {
    width: rowWidth
  },
  separator: {
    width: rowWidth,
    height: theme.borderWidth.xs,
    backgroundColor: theme.colors.gray.light
  },
  buttonContainer: {
    justifyContent: 'flex-end',
    marginBottom:70,
    marginHorizontal: theme.spacing.md
  }
})

type Props = NativeStackScreenProps<
  SendFundsStackNavigatorParamList,
  "PaymentSummary"
>

const PaymentSummary: React.FC<Props> = ({ route, navigation }: Props) => {
  const { t } = useTranslation()
  const { contact, asset, amount, walletMode, walletAddress, fromScreen, id } =
    route.params
  const { assetShort: assetTranslationItems } = useTranslationItems()
  const { fiatAsset, user } = useContext(UserContext)
  const { data: exchangeRates } = useGetExchangeRate(fiatAsset)
  const { data: p2pFee } = useGetP2pFee()
  const [confirmButtonDisable, setConfirmButtonDisable] = useState(false)

  const {
    mutate: addTransaction,
    isLoading: isAddTransactionDataLoading,
    error: isAddTransactionDataError,
    isSuccess,
    data: addTransactionData
  } = useAddTransaction()

  const {
    mutate: sendDigitalCurrencyExternalWallet,
    isLoading: isSendExternalWalletLoading,
    error: sendExternalWalletError,
    isSuccess: isSendExternalWalletSuccess,
    data: sendExternalWalletData
  } = useSendDigitalCurrencyToExternalWallet()
  const P2P_FEE_PERCENTAGE = p2pFee ? p2pFee : 0
  const {
    mutate: savePaymentInfo,
    isLoading: isSavePaymentInfoLoading,
    error: savePaymentInfoError,
    isSuccess: isSavePaymentInfoSuccess,
    data: savePaymentInfoData
  } = useSavePaymentInfo(user?.attributes.email || "")

  const {
    mutate: updatePaymentRequest,
    isLoading,
    error,
    isSuccess: isSendNotifyRequestStatusSuccess,
    data
  } = useUpdatePaymentRequest()

  const savePaymentInfoForNotification = (
    transactionId: string,
    status: string
  ) => {
    savePaymentInfo({
      requesterId: id,
      requesterName: contact?.cognitoUsername || "",
      requesterEmail: contact?.email || "",
      requesterPhone: contact?.phoneNumber || "",
      receiverName: user?.attributes.email || "",
      receiverEmail: user?.attributes.email || "",
      receiverPhone: user?.attributes?.phone_number,
      digitalCurrency: asset || "",
      amount: amount,
      fee: "",
      walletAddress: walletAddress || "",
      time: new Date(),
      title: "Payment Request",
      body: `You have a payment request from  ${user?.attributes.email}`,
      uuid: user?.username,
      status: status, //Requested,
      transactionId: transactionId
    })
  }
  const sendNotifyRequestStatusForNotification = (
    transactionId: string,
    status: string
  ) => {
    updatePaymentRequest({
      id: id,
      status: status
      // requesterName: user?.attributes.email|| "",
      // requesterEmail: contact?.email || "",
      // requesterPhone: contact?.phoneNumber || "",
      // receiverName: user?.attributes.email || "",
      // receiverEmail: user?.attributes.email || "",
      // receiverPhone: user?.attributes?.phone_number,
      // digitalCurrency: asset  || "",
      // amount: amount,
      // fee: "",
      // walletAddress: walletAddress || "",
      // time: new Date(),
      // title: "Payment Approved",
      // body: `Your payment approved by  ${user?.attributes.email}`,
      // uuid: contact?.cognitoUsername, //Requested,
      // requestStatusId: transactionId
    })
  }

  useEffect(() => {
    if (
      (isSuccess && addTransactionData) ||
      (isSendExternalWalletSuccess && sendExternalWalletData)
    ) {
      let response = addTransactionData || sendExternalWalletData
      if (fromScreen === "notification") {
        // savePaymentInfoForNotification(response?.uuid , response?.status)
        sendNotifyRequestStatusForNotification(response?.uuid, response?.status)
      }
      navigation.dispatch(
        CommonActions.reset({
          index: 0,
          routes: [
            {
              name: "PaymentSuccess",
              params: {
                transactionId: (response as Record<"uuid", string>).uuid
              }
            }
          ]
        })
      )
    }
  }, [
    isSuccess,
    addTransactionData,
    isSendExternalWalletSuccess,
    sendExternalWalletData
  ])

  const onConfirm = () => {
    if (walletAddress) {
      // external wallet
      sendDigitalCurrencyExternalWallet({
        amount,
        asset,
        receiverAddress: walletAddress,
        description: "external wallet"
      })
    } else {
      // Wadzpay wallet
      addTransaction({
        receiverEmail: contact?.email || "",
        asset,
        amount
      })
    }
    setConfirmButtonDisable(true)
  }
  let estimatedFee = calculateEstimatedFee(amount, P2P_FEE_PERCENTAGE, asset)
  let totalFee = parseFloat(estimatedFee) + parseFloat(amount)

  return (
    <ScreenLayoutTab
      title={t("transfer_confirmation")}
      leftIconName="ArrowLeft"
      onLeftIconClick={navigation.goBack}
      useScrollView={false}
    >
      <ErrorModal
        error={contact ? isAddTransactionDataError : sendExternalWalletError}
      />
      <Container justify="space-between" style={styles.container}>
        <Container alignItems="center" noItemsStretch spacing={1}>
          <Icon name={AssetIconNamesMap[asset]} size="xl" />
          <Typography variant="subtitle">
            {assetTranslationItems[asset]}
          </Typography>
        </Container>
        <Container alignItems="center" noItemsStretch spacing={2}>
          <Container
            direction="row"
            justify="space-between"
            alignItems="center"
            noItemsStretch
            style={styles.row}
          >
            <Typography textAlign="left">{t("To")}</Typography>
            <Container alignItems="flex-end" noItemsStretch>
              <Typography
                style={{ width: 150 }}
                numberOfLines={1}
                ellipsizeMode="middle"
                textAlign="right"
                fontWeight="bold"
              >
                {contact?.nickname || walletAddress}
              </Typography>
              {contact?.email ? (
                <Typography
                  variant="label"
                  textAlign="right"
                  color="grayMedium"
                >
                  {contact?.email || ""}
                </Typography>
              ) : null}
            </Container>
          </Container>

          {walletMode === WADZPAY_WALLET ? null : (
            <View style={styles.separator} />
          )}
          {walletMode === WADZPAY_WALLET ? null : (
            <Container
              direction="row"
              justify="space-between"
              style={styles.row}
            >
              <Typography textAlign="left">{t("Amount")}</Typography>
              <Container>
                <Typography fontWeight="bold" textAlign="right">
                  {amount} {asset}
                </Typography>
              </Container>
            </Container>
          )}

          <View style={styles.separator} />

          {walletMode !== WADZPAY_WALLET ? (
            <Container
              direction="row"
              justify="space-between"
              style={styles.row}
            >
              <Typography textAlign="left">Estimated Network Fee</Typography>
              <Container>
                <Typography fontWeight="bold" textAlign="right">
                  {calculateEstimatedFee(amount, P2P_FEE_PERCENTAGE, asset)}{" "}
                  {asset}
                </Typography>
              </Container>
            </Container>
          ) : null}

          {walletMode !== WADZPAY_WALLET ? (
            <View style={styles.separator} />
          ) : null}

          <Container
            direction="row"
            justify="space-between"
            alignItems="center"
            noItemsStretch
            style={styles.row}
          >
            <Typography textAlign="left">{t("Total")}</Typography>
            <Container alignItems="flex-end" noItemsStretch>
              <Typography textAlign="right" fontWeight="bold">
                {walletMode !== WADZPAY_WALLET
                  ? `${totalFee} ${asset}`
                  : `${amount} ${asset}`}
              </Typography>


              {(exchangeRates && asset != "SART") ? (
                <FiatAmount
                  amount={
                    walletMode !== WADZPAY_WALLET
                      ? totalFee / exchangeRates[asset]
                      : amount / exchangeRates[asset]
                  }
                  fiatAsset={fiatAsset}
                />
              ):null}
            </Container>
          </Container>
        </Container>
        <View style={styles.buttonContainer}>
          <Button
            text={t("Confirm")}
            onPress={onConfirm}
            loading={isAddTransactionDataLoading || isSendExternalWalletLoading}
            disabled={confirmButtonDisable}
          />
        </View>
      </Container>
    </ScreenLayoutTab>
  )
}

export default PaymentSummary

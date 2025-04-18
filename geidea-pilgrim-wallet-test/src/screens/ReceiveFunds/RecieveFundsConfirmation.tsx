import { NativeStackScreenProps } from "@react-navigation/native-stack"
import React, { useContext, useRef, useState } from "react"
import { RecieveFundsStackNavigatorParamList } from "~/components/navigators/RecieveFundsStackNavigator"
import {
  Button,
  Container,
  Icon,
  ScreenLayoutTab,
  TextField,
  theme,
  Typography
} from "~/components/ui"
import QRCode from "react-native-qrcode-svg"
import {
  Alert,
  Dimensions,
  StyleSheet,
  ToastAndroid,
  TouchableOpacity,
  View
} from "react-native"
import Clipboard from '@react-native-clipboard/clipboard';
import Share from 'react-native-share';
import { CommonActions, RouteProp } from "@react-navigation/native"
import ViewShot, { captureScreen } from "react-native-view-shot"
import { Asset } from "~/constants/types"
import { useSendPushNotificationPayment } from "~/api/user"
import { UserContext } from "~/context"
import { useTranslation } from "react-i18next"
import { isIOS, showToast,sartTxt } from "~/utils"
import QrCodeCardCarousel from "./QrCodeCardCarousel"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.xl
const rowWidth =
  width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs
type Props = NativeStackScreenProps<
  RecieveFundsStackNavigatorParamList,
  "RecieveFundsConfirmation"
>

const copyToClipboard = (value: string) => {
  // console.log(value)
  Clipboard.setString(value);
  isIOS ? alert(`Copied ${value}`) : showToast("Copied!")
}

let showAssetsName = (asset: Asset) => {
  if (asset === "BTC") {
    return "BitCoin"
  } else if (asset === "ETH") {
    return "Ethereum"
  } else if (asset === "WTK") {
    return "WadzPay Token"
  } else if (asset === "USDT") {
    return "Tether"
  } else if (asset === "SART") {
    return sartTxt
  }
}
const RecieveFundsConfirmation: React.FC<Props> = ({
  route,
  navigation
}: Props) => {
  const { asset, contact, amount, walletAddress } = route.params
  let qrvalue = walletAddress ? walletAddress : contact?.cognitoUsername
  // console.log("walletAddress" , walletAddress)
  // console.log("qrvalue" , qrvalue)
  const [buttonDisable, setButtonDisable] = useState(false)
  const [imageURI, setImageURI] = useState('');
  const viewShotRef = useRef()
  const { user } = useContext(UserContext)
  const formatter = useFormatCurrencyAmount()

  const { t } = useTranslation()

  async function captureViewShot() {
     //  const imageURI = await viewShotRef.current.capture()
     viewShotRef.current.capture().then(uri => {
      setImageURI(uri);
    });
    // console.log(imageURI)
    const options = {
      url: imageURI,
      message: 'recipt',
    };
    await Share.open(options);
    //sendNotificationForRecievePayment()
  }

  const sendNotificationForRecievePayment = () => {
    // console.log("sending notif")
    // let str = user?.attributes.email
    // let nameMatch = str?.match(/^([^@]*)@/) ;
    // let name = nameMatch ? nameMatch[1] : null;

    sendSendPushNotificationPayment({
      senderEmail: contact?.email || "",
      digitalCurrency: asset || "",
      amount: amount,
      time: new Date() || ""
    })

    Alert.alert("", "Payment Request Raised successfully", [
      {
        text: "OK",
        onPress: () => {
          navigation.dispatch(
            CommonActions.reset({
              index: 0,
              routes: [{ name: "Home" }]
            })
          )
        }
      }
    ])
  }

  const {
    mutate: sendSendPushNotificationPayment,
    isLoading: isSendPushNotificationPaymentLoading,
    error: SendPushNotificationPaymentError,
    isSuccess: isSendPushNotificationPaymentSuccess,
    data: sendSendPushNotificationPaymentData
  } = useSendPushNotificationPayment()

  return (
    <ScreenLayoutTab
      title={"Request Confirmation"}
      leftIconName="ArrowLeft"
      onLeftIconClick={navigation.goBack}
      useScrollView={true}
    >
      <ViewShot
        style={{ backgroundColor: theme.colors.white, paddingTop: 30 }}
        ref={viewShotRef}
        options={{ format: "jpg", quality: 0.5 }}
      >
        {contact && (
          <Container
            alignItems="center"
            noItemsStretch
            spacing={0.5}
            style={{ marginBottom: 30, marginTop: 20 }}
          >
            <Icon name={asset} size="xl" />
            <Typography variant="subtitle">{asset}</Typography>
          </Container>
        )}

        {walletAddress && (
          <Container
            justify="space-between"
            noItemsStretch
            alignItems="center"
            spacing={2}
          >
            <QrCodeCardCarousel
              walletAddress={walletAddress}
              amount={amount}
              digitalAsset={asset}
            />
            {/* <QRCode
              value={qrvalue}
              size={250}
              color="black"
              backgroundColor="white"
            /> */}
          </Container>
        )}

        {walletAddress && (
          <Container>
            <Typography
              variant="chip"
              textAlign="center"
              style={{ marginTop: 5 }}
            >
              Send only {showAssetsName(asset)} ({asset}) to this address
            </Typography>
            <Typography
              variant="chip"
              textAlign="center"
              style={{ marginTop: 0 }}
            >
              Sending any other currency may result in permanent loss
            </Typography>
          </Container>
        )}

        <Container
          justify="space-between"
          alignItems="center"
          noItemsStretch
          spacing={2}
          style={styles.container}
        >
          <Typography textAlign="left" variant="label" style={{ marginTop: 5 }}>
            {t("Digital Currency")}
          </Typography>
          <Container
            alignItems="center"
            noItemsStretch
            style={{ flexDirection: "row" }}
          >
            <Icon name={asset} size="lg" />
            <Typography fontWeight="bold" variant="label" textAlign="right">
              {showAssetsName(asset)}
            </Typography>
          </Container>
        </Container>

        {contact && <View style={styles.separator} />}

        {contact && (
          <Container
            justify="space-between"
            alignItems="center"
            noItemsStretch
            spacing={2}
            style={styles.container}
          >
            <Typography
              textAlign="left"
              variant="label"
              style={{ marginTop: 5 }}
            >
              From
            </Typography>
            <Container>
              <Typography
                fontWeight="bold"
                variant="label"
                textAlign="right"
                color="orange"
              >
                {contact?.nickname}
              </Typography>
              <Typography variant="label" textAlign="right" color="grayMedium">
                {contact?.email}
              </Typography>
            </Container>
          </Container>
        )}

        <View style={styles.separator} />

        {amount === 0 ? null : (
          <Container
            justify="space-between"
            alignItems="center"
            noItemsStretch
            spacing={2}
            style={[styles.container, { marginTop: 8, marginBottom: 8 }]}
          >
            <Typography
              textAlign="left"
              variant="label"
              style={{ marginTop: 5 }}
            >
              Amount
            </Typography>
            <Container>
              <Typography fontWeight="bold" variant="label" textAlign="right">
              {formatter(amount, {
                asset: asset
              })} {asset}
              </Typography>
            </Container>
          </Container>
        )}
        {amount === 0 ? null : <View style={styles.separator} />}
      </ViewShot>

      {walletAddress && (
        <Container spacing={1} style={styles.requestLinkContainer}>
          <Typography variant="label" textAlign="left">
            Wallet Address
          </Typography>
          <Container
            direction="row"
            spacing={1}
            style={{
              borderWidth: 2,
              borderRadius: 8,
              borderColor: "#EDEDED",
              paddingHorizontal: 10,
              paddingBottom: 15,
              paddingTop: 15
            }}
          >
            <Typography
              variant="label"
              style={styles.copyQrTextStyle}
              numberOfLines={1}
              ellipsizeMode="middle"
              textAlign="left"
            >
              {qrvalue}
            </Typography>
            <TouchableOpacity onPress={() => copyToClipboard(qrvalue)}>
              <Icon name={"CopyIcon"}></Icon>
            </TouchableOpacity>
          </Container>
        </Container>
      )}

      <Container spacing={1} style={styles.buttonContainer}>
        {/* <Button
            text={"Done"}
            onPress={() => {
              navigation.dispatch(
                CommonActions.reset({
                  index: 0,
                  routes: [{ name: "ReceiveFunds" }]
                })
              )
            }}
          />
          <View style={{ height: 10 }}></View> */}
        <Button
          text={walletAddress ? "Share" : "Done"}
          disabled={buttonDisable}
          onPress={() => {
            walletAddress
              ? captureViewShot()
              : sendNotificationForRecievePayment()
            walletAddress ? null : setButtonDisable(true)
          }}
        />
      </Container>
    </ScreenLayoutTab>
  )
}

const styles = StyleSheet.create({
  qrcontainer: {
    borderWidth: 20,
    borderRadius: 18,
    borderColor: "#FFC235",
    marginHorizontal: 60,
    marginVertical: 8
  },
  container: {
    flexDirection: "row",
    marginHorizontal: 20,
    marginVertical: 4
  },
  requestLinkContainer: {
    marginHorizontal: 20,
    marginVertical: 20
  },
  buttonContainer: {
    marginHorizontal: 30,
    marginVertical: 10
  },
  separator: {
    height: theme.borderWidth.xs,
    backgroundColor: "#EDEDED",
    marginHorizontal: 15,
    marginVertical: 1
  },
  copyQrTextStyle: {
    flex: 1,
    width: 300,
    paddingRight: 10
  }
})
export default RecieveFundsConfirmation

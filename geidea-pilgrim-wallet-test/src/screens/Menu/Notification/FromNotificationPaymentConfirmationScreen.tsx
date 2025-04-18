import { NativeStackScreenProps } from "@react-navigation/native-stack"
import React, { useContext, useEffect, useRef, useState } from "react"
import { RecieveFundsStackNavigatorParamList } from "~/components/navigators/RecieveFundsStackNavigator"
import {
  Button,
  Container,
  FiatAmount,
  Icon,
  ScreenLayoutTab,
  TextField,
  theme,
  Typography
} from "~/components/ui"
import QRCode from "react-native-qrcode-svg"
import {
  Dimensions,
  Image,
  StyleSheet,
  TouchableOpacity,
  View
} from "react-native"
import { CommonActions, RouteProp } from "@react-navigation/native"
import ViewShot, { captureScreen } from "react-native-view-shot"
import { Asset } from "~/constants/types"
import { HomeStackNavigatorParamList } from "~/components/navigators/HomeStackNavigator"
import { NotificationsContext, OnboardingContext, UserContext } from "~/context"
import { useGetExchangeRate } from "~/api/onRamp"
import {
  useSavePaymentInfo,
  useUpdatePaymentRequest,
  useUserContacts
} from "~/api/user"
import { WADZPAY_WALLET } from "~/constants"
import Share from 'react-native-share';

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.xl
const rowWidth =
  width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs
type Props = NativeStackScreenProps<
  HomeStackNavigatorParamList,
  "FromNotificationPaymentConfirmationScreen"
>

// const copyToClipboard = (value: string) => {
//   // console.log(value)
//   Clipboard.setString(value)
//   alert(`Copied ${value}`)
// }

const FromNotificationPaymentConfirmationScreen: React.FC<Props> = ({
  route,
  navigation
}: Props) => {
  // const { notificationData, setNotiCount, notifCount } =
  //   useContext(NotificationsContext)
  const { user } = useContext(UserContext)
 // console.log("&&&&&&& erwerwerwerwere", notificationData)
  const { notificationDataParam } = route?.params
  const [rejectStatus, setRejectStatus] = useState("")
  // console.log("notificationDataParam " , notificationDataParam)
  ///  let qrvalue = notificationData.requesterName
  // const viewShotRef = useRef()

  //   async function captureViewShot() {
  //     const imageURI = await viewShotRef.current.capture();
  //     // console.log(imageURI)
  //     await Sharing.shareAsync(imageURI);
  //   }
  const {
    data: contacts,
    isFetching: contactsIsFetching,
    error: contactsError
  } = useUserContacts()

  // console.log("contacts ", contacts)
  // const requesterEmailId = notificationDataParam
  //   ? notificationDataParam?.requesterEmail
  //   : notificationData.requesterEmail

 //const contact = contacts?.find((c) => c.email === requesterEmailId)

  const {
    mutate: updatePaymentRequest,
    isLoading,
    isSuccess,
    data
  } = useUpdatePaymentRequest()

  // const updatePaymentRequestRejectTransaction = () => {
  //   updatePaymentRequest({
  //     id: notificationDataParam
  //       ? notificationDataParam?.id
  //       : notificationData.id,
  //     status: "FAILED"
  //   })
  //   setRejectStatus("FAILED")
   
  // }

 // console.log("contact ", contact)

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
      return "SARt"
    }
  }
  const { fiatAsset } = useContext(UserContext)
  // const { onboardingValues } = useContext(OnboardingContext)
  // console.log("onboardingValuesonboardingValues",onboardingValues)
  console.log(fiatAsset)

  const { data: exchangeRates, isFetching: exchangeRatesIsFetching } =
    useGetExchangeRate(fiatAsset)

  // const fiatAmount = exchangeRates
  //   ? (notificationDataParam?.amount || notificationData.amount) /
  //     exchangeRates[
  //       notificationDataParam?.digitalCurrency ||
  //         notificationData?.digitalCurrency
  //     ]
  //   : 0

  // const showDateParam = `${new Date(
  //   notificationDataParam?.timeNotification
  // ).toDateString()} ${new Date(
  //   notificationDataParam?.timeNotification
  // ).toLocaleTimeString()}`

  // const showDateNotification = `${new Date(
  //   notificationData.time
  // ).toLocaleDateString()} ${new Date(
  //   notificationData.time
  // ).toLocaleTimeString()}`

  // console.log("requesterEmailId ", requesterEmailId)
  // const isUserSame = user?.attributes.email === requesterEmailId
  // console.log("isUserSame ", isUserSame)
  // const recieverEmailId = notificationDataParam
  //   ? notificationDataParam?.receiverEmail
  //   : notificationData.receiverEmail

  // const status = notificationDataParam
  //   ? notificationDataParam?.status
  //   : notificationData.status

  const showImageStatus = (status: string) => {
    if (status === "SUCCESSFUL" || status === "OVERPAID" || status == "UNDERPAID") {
      return (
        <Container
          alignItems="center"
          noItemsStretch
          spacing={4}
          style={[{ marginTop: 30 }]}
        >
          <Image
            source={require("~images/Welcome/transaction_success.png")}
            style={styles.image}
          />
          <Typography variant="subtitle">Payment Successful</Typography>
        </Container>
      )
    } else if (status === "FAILED" || rejectStatus === "FAILED") {
      return (
        <Container
          alignItems="center"
          noItemsStretch
          spacing={4}
          style={[{ marginTop: 30 }]}
        >
          <Image
            source={require("~images/Welcome/rejecttransaction.png")}
            style={styles.image}
          />
          <Typography variant="subtitle">Payment Rejected</Typography>
        </Container>
      )
    } else {
      return null
    }
  }

  // const setFromFeildText = () => {
  //   if (requesterEmailId === user?.attributes.email) {
  //     if (status === "NEW") {
  //       return `To`
  //     } else if (status === "FAILED" || rejectStatus === "FAILED") {
  //       return `By`
  //     }
  //   }
  //   if (recieverEmailId === user?.attributes.email) {
  //     if (status === "SUCCESSFUL" || status === "OVERPAID" || status == "UNDERPAID") {
  //       return `From`
  //     } else if (status === "FAILED" || rejectStatus === "FAILED") {
  //       return `By`
  //     }
  //   }

  //   return `From`
  // }

  // const setFromFieldValueText = () => {
  //   console.log("recieverEmailId ", recieverEmailId)
  //   console.log("status ", status)
  //   if (requesterEmailId === user?.attributes.email) {
  //     if (status === "NEW") {
  //       return recieverEmailId
  //     } else if (status === "FAILED" || rejectStatus === "FAILED") {
  //       return recieverEmailId
  //     } else if (status === "SUCCESSFUL" || status === "OVERPAID" || status == "UNDERPAID") {
  //       return recieverEmailId
  //     }
  //   }
  //   if (recieverEmailId === user?.attributes.email) {
  //     if (status === "SUCCESSFUL" || status === "OVERPAID" || status == "UNDERPAID") {
  //       return recieverEmailId
  //     } else if (status === "FAILED" || rejectStatus === "FAILED") {
  //       return recieverEmailId
  //     } else if (status === "NEW") {
  //       return requesterEmailId
  //     }
  //   }
  //   return requesterEmailId
  // }
  return (
    <View></View>
    // <ScreenLayoutTab
    //   title={"Transfer Request"}
    //   leftIconName="ArrowLeft"
    //   onLeftIconClick={() =>
    //     navigation.dispatch(
    //       CommonActions.reset({
    //         index: 0,
    //         routes: [{ name: "Home" }]
    //       })
    //     )
    //   }
    //   useScrollView={true}
    // >
    //   {showImageStatus(status || "NEW")}
    //   <Container
    //     justify="space-between"
    //     alignItems="center"
    //     noItemsStretch
    //     spacing={4}
    //     style={[styles.container, { marginTop: 60 }]}
    //   >
    //     <Typography textAlign="left" variant="label" style={{ marginTop: 5 }}>
    //       Digital Currency
    //     </Typography>
    //     <Container
    //       alignItems="center"
    //       noItemsStretch
    //       style={{ flexDirection: "row" }}
    //     >
    //       <Icon
    //         name={
    //           notificationDataParam?.digitalCurrency ||
    //           notificationData?.digitalCurrency
    //         }
    //         size="lg"
    //       />
    //       <Typography fontWeight="bold" variant="label" textAlign="right">
    //         {showAssetsName(
    //           notificationDataParam?.digitalCurrency ||
    //             notificationData.digitalCurrency
    //         )}
    //       </Typography>
    //     </Container>
    //   </Container>

    //   <View style={styles.separator} />

    //   <Container
    //     justify="space-between"
    //     alignItems="center"
    //     noItemsStretch
    //     spacing={4}
    //     style={styles.container}
    //   >
    //     <Typography textAlign="left" variant="label" style={{ marginTop: 5 }}>
    //       {setFromFeildText()}
    //     </Typography>
    //     <Container>
    //       <Typography
    //         fontWeight="bold"
    //         variant="label"
    //         textAlign="right"
    //         color="orange"
    //       >
    //         {setFromFieldValueText()}
    //       </Typography>
    //     </Container>
    //   </Container>

    //   <View style={styles.separator} />

    //   {notificationData.amount !== 0 ? (
    //     <Container
    //       justify="space-between"
    //       alignItems="center"
    //       noItemsStretch
    //       spacing={4}
    //       style={[styles.container, { marginTop: 8, marginBottom: 8 }]}
    //     >
    //       <Typography textAlign="left" variant="label" style={{ marginTop: 5 }}>
    //         Amount
    //       </Typography>
    //       <Container>
    //         <Typography fontWeight="bold" variant="label" textAlign="right">
    //           {notificationDataParam?.amount || notificationData.amount}{" "}
    //           {notificationDataParam?.digitalCurrency ||
    //             notificationData?.digitalCurrency}
    //         </Typography>

    //         <Typography variant="chip" fontWeight="bold" textAlign="right">
    //           {
    //             // {fiatAmount && fiatAsset != "SART" ? (
    //             //   <FiatAmount
    //             //     amount={fiatAmount}
    //             //     fiatAsset={fiatAsset}
    //             //     color={"grayDark"}
    //             //   />
    //             // ) : null}

    //             <FiatAmount
    //               amount={fiatAmount}
    //               fiatAsset={fiatAsset}
    //               variant="chip"
    //               color={"grayDark"}
    //             />
    //           }
    //         </Typography>
    //       </Container>
    //     </Container>
    //   ) : null}

    //   <View style={styles.separator} />

    //   <Container
    //     justify="space-between"
    //     alignItems="center"
    //     noItemsStretch
    //     spacing={1}
    //     style={[styles.container, { marginTop: 8, marginBottom: 8 }]}
    //   >
    //     <Typography textAlign="left" variant="label" style={{ marginTop: 5 }}>
    //       Time
    //     </Typography>
    //     <Container>
    //       <Typography variant="chip" fontWeight="bold" textAlign="right">
    //         {notificationDataParam ? showDateParam : showDateNotification}
    //       </Typography>
    //     </Container>
    //   </Container>
    //   <View style={styles.separator} />

    //   {notificationDataParam?.status === "SUCCESSFUL" ||
    //   rejectStatus === "FAILED" ||
    //   notificationDataParam?.status === "FAILED" ||
    //   notificationDataParam?.status === "OVERPAID" || 
    //   notificationDataParam?.status == "UNDERPAID" ||
    //   isUserSame ? null : (
    //     <Container spacing={1} style={styles.buttonContainer}>
    //       <Button
    //         text={"Transfer"}
    //         onPress={() => {
    //           setNotiCount(notifCount - 1)
    //           navigation.navigate("PaymentSummary", {
    //             fromScreen: "notification",
    //             id: notificationDataParam
    //               ? notificationDataParam?.id
    //               : notificationData.id,
    //             contact: contact
    //               ? contact
    //               : {
    //                   // for user who are not in contact list
    //                   id: "", //
    //                   nickname: "",
    //                   phoneNumber: notificationDataParam
    //                     ? notificationDataParam?.requesterPhone
    //                     : notificationData.requesterPhone,
    //                   email: notificationDataParam
    //                     ? notificationDataParam?.requesterEmail
    //                     : notificationData.requesterEmail,
    //                   cognitoUsername: ""
    //                 },
    //             walletAddress: notificationDataParam
    //               ? notificationDataParam?.walletAddress
    //               : notificationData.walletAddress,
    //             asset: notificationDataParam
    //               ? notificationDataParam?.digitalCurrency
    //               : notificationData.digitalCurrency,
    //             amount: notificationDataParam
    //               ? notificationDataParam?.amount
    //               : notificationData.amount,
    //             walletMode: WADZPAY_WALLET
    //           })
    //         }}
    //       />
    //       <View style={{ height: 10 }}></View>
    //       <Button
    //         text={"Reject"}
    //         variant={"secondary"}
    //         onPress={() => {
    //           setNotiCount(notifCount - 1)
    //           updatePaymentRequestRejectTransaction()
    //         }}
    //       />
    //     </Container>
    //   )}
    // </ScreenLayoutTab>
  )
}

const styles = StyleSheet.create({
  qrcontainer: {
    borderWidth: 20,
    borderRadius: 18,
    borderColor: "#FFC235",
    marginHorizontal: 70,
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
  },
  image: {
    height: 80,
    width: 80,
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.xs
  }
})
export default FromNotificationPaymentConfirmationScreen

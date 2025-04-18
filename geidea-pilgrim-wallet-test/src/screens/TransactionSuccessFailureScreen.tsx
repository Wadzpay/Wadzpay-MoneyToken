// ============================== Imports =================================

import React, { useEffect,useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import ViewShot, { captureScreen } from "react-native-view-shot"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import {
  StyleSheet,
  Image,
  TouchableOpacity,
  Dimensions,
  View,
  BackHandler
} from "react-native"
import { CommonActions, RouteProp } from "@react-navigation/native"
import Clipboard from '@react-native-clipboard/clipboard';
import { formatAMPM } from "~/utils"
import {
  SendFundsStackNavigatorParamList,
  TransactionStackNavigatorParamList
} from "~/components/navigators"
import {
  Button,
  Container,
  FiatAmount,
  Icon,
  ScreenLayoutTab,
  theme,
  Typography
} from "~/components/ui"
import { useUserTransaction } from "~/api/user"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { MenuStackParamList } from "~/components/navigators"
import { Transaction } from "~/api/models"
import { isIOS, showToast } from "~/utils"
import { FiatSignMap } from "~/constants/types"

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.md
const rowWidth =
  width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs

// ============================== Constants =================================

// type Props = {
//   navigation: StackNavigationProp<
//     TransactionStackNavigatorParamList,
//     "Transactions"
//   >
//   route: RouteProp<SendFundsStackNavigatorParamList, "PaymentSuccess">
// }

type Props = NativeStackScreenProps<MenuStackParamList, "TransactionSuccessFailureScreen">

type Props1 = {
  transaction: Transaction
}
// ============================== Class =================================

const TransactionSuccessFailureScreen: React.FC<Props> = ({ navigation, route }: Props) => {
 
  const formatter = useFormatCurrencyAmount()
  const { t } = useTranslation()
  const {  transactionId, transactionType, bankAccountNumber, userEmail, fiatAmount, createdAt, status , description } = route.params
  const [backButtonPressed, setBackButtonPressed] = useState(false)

  
  const viewShotRef = useRef()
  const date = new Date(createdAt)

  const [successFailureScreenOpened, setSuccessFailureScreenOpened] = useState(false);
  const [shareRecipt, setShareRecipt] = useState(false)
  const [imageURI, setImageURI] = useState('');


  // ************************ Button Clicks ************************ 



  useEffect(() => {
    function handleBackButton() {
      onBackButtonClick()
      return true
    }

    const backHandler = BackHandler.addEventListener(
      "hardwareBackPress",
      handleBackButton
    )

    return () => backHandler.remove()
  }, [navigation])

    const onBackButtonClick = (): any => {
      if(backButtonPressed == false) {
        navigation.dispatch(
          CommonActions.reset({
            index: 0,
            routes: [
              {
                name: "Menu"
              }
            ]
          })
        )
      }
    }

    const onShareClick = () => {
      setShareRecipt(true)
      setTimeout(() => {
        captureViewShot()
      }, 80)
    }

    async function captureViewShot() {
      // const imageURI = await viewShotRef.current.capture()
      // setShareRecipt(false)
      // await Sharing.shareAsync(imageURI)

      viewShotRef.current.capture().then(uri => {
        setImageURI(uri);
      });
      const options = {
        url: imageURI,
        message: 'recipt',
      };
      await Share.open(options);
      setShareRecipt(false);

    }

    const copyToClipboard = (value: string) => {
      Clipboard.setString(value)
      isIOS ? alert(`Copied!`) : showToast("Copied!")
    }


  // ************************ Handling Server Responses ************************ 

  useEffect(() => {
    if(!successFailureScreenOpened){

      const txt = "Digital currency balances should be updated in 30 secs to 2 minutes"
      isIOS ? alert(txt) : showToast(txt)
      navigation.removeListener('focus',{})

      setSuccessFailureScreenOpened(true);
   }
    
  },[navigation])


  // ************************ Handling UI ************************ 

  return (
    <ScreenLayoutTab
      title=""
      leftIconName="ArrowLeft"
      onLeftIconClick={onBackButtonClick}
    >
      <Container justify="space-between" style={styles.container}>
        <ViewShot
          style={{ backgroundColor: theme.colors.white, paddingTop: 0 }}
          ref={viewShotRef}
          options={{ format: "jpg", quality: 0.5 }}
        >
          <Container alignItems="center" noItemsStretch spacing={1}>
              <Container
                direction="column"
                noItemsStretch
                alignItems="center"
                justify="space-evenly"
              >
                <Image
                  source={status == "SUCCESSFUL" ?  require("~images/successful_menu.png") : require("~images/transaction_failed.png")}
                  style={styles.image}
                />
                <Typography fontFamily="Helvetica-Bold" color={status == "SUCCESSFUL"  ?"success" : "error" } variant="subtitle" style={{ marginTop: 0, marginBottom:0 }}>
                  {status == "SUCCESSFUL" ? "Transaction Successful" : "Transaction Failed"}
                </Typography>
              </Container>
          </Container>

          <Container
            alignItems="center"
            noItemsStretch
            spacing={1}
            style={{ marginVertical: 50 }}
          >
            {/* For Transaction ID Field*/}            
            <Container>
                {(status == "SUCCESSFUL" && shareRecipt == false) ?  <TouchableOpacity onPress={() => copyToClipboard(transactionId)}>
                <Container
                  direction="row"
                  noItemsStretch
                  alignItems="center"
                  justify="space-between"
                  style={{marginTop:-20}}
                >
                  <Typography color="grayMedium" textAlign="left" fontFamily="HelveticaNeue-Medium">Transaction Id  </Typography>
                  <Typography
                    fontFamily="HelveticaNeue-Medium"
                      style={{ width: 180, marginRight: 0 }}
                      numberOfLines={1}
                      ellipsizeMode="tail"
                    >
                    {transactionId}
                  </Typography>
                  <Icon name="CopyIcon" size="xs" />
                </Container>
                </TouchableOpacity> 
               : undefined }
            </Container>

            {(status == "SUCCESSFUL" && shareRecipt == true) ? <Container
                  direction="row"
                  noItemsStretch
                  alignItems="flex-start"
                  justify="flex-start"
                  style={{marginRight:20}}
                >
                  <Typography  textAlign="left" fontFamily="HelveticaNeue-Medium">Txn Id :  {transactionId} </Typography>
                
                </Container>
                : undefined}

            <Container></Container>

            {/* For Transaction Type Field */}            
            <Container
              direction="row"
              justify="space-between"
              style={[styles.row, styles.topHeight]}
            >
              <Typography color="grayMedium" textAlign="left" fontFamily="HelveticaNeue-Medium">Transaction Type</Typography>
              <Container>
                <Typography textAlign="right" fontFamily="HelveticaNeue-Medium" color="darkBlack">
                  {transactionType}
                </Typography>
              </Container>
            </Container>

            <View style={styles.separator} />


            {/* For From Field */}            
            <Container
              direction="row"
              justify="space-between"
              style={styles.row}
            >
              <Typography color="grayMedium" textAlign="left" fontFamily="HelveticaNeue-Medium">From</Typography>
              <Container>
                <Typography textAlign="right" fontFamily="HelveticaNeue-Medium" color="darkBlack">
                {transactionType == "DEPOSIT" ? "AEXX23XXXXXXXXXXXXX"+bankAccountNumber.slice(-4) : userEmail}
                </Typography>
              </Container>
            </Container>

            <View style={styles.separator} />

            {/* For To Field */}            
            <Container
              direction="row"
              justify="space-between"
              alignItems="center"
              noItemsStretch
              style={styles.row}
            >
              <Typography color="grayMedium" textAlign="left" fontFamily="HelveticaNeue-Medium">To</Typography>
              <Container>
                <Typography textAlign="right" fontFamily="HelveticaNeue-Medium" color="darkBlack">
                {transactionType == "DEPOSIT" ? userEmail : "AEXX23XXXXXXXXXXXXX"+bankAccountNumber.slice(-4) }
                </Typography>
                {/* <Typography variant="label" textAlign="right" color="grayMedium">
               email to be passed as param
              </Typography> */}
              </Container>
            </Container>

            <View style={styles.separator} />

            {/* For Amount Field */}            
            <Container
              direction="row"
              justify="space-between"
              alignItems="center"
              noItemsStretch
              style={styles.row}
            >
              <Typography color="grayMedium" textAlign="left" fontFamily="HelveticaNeue-Medium">Amount</Typography>
              <Container>
                <Typography textAlign="right" fontFamily="HelveticaNeue-Medium" color="darkBlack">
                {"AED"} {fiatAmount}
                </Typography>
              </Container>
            </Container>

            <View style={styles.separator} />

            {/* For Time Field */}            
            <Container
              direction="row"
              justify="space-between"
              style={styles.row}
            >
              <Typography color="grayMedium" textAlign="left" fontFamily="HelveticaNeue-Medium">Time</Typography>
              <Container>
                <Typography textAlign="right" fontFamily="HelveticaNeue-Medium" color="darkBlack">
                  {date ? `${date.toDateString()} ${date.toLocaleTimeString()}`: ""}
                </Typography>
              </Container>
            </Container>

            {status !== "SUCCESSFUL"  ? <View style={styles.separator} /> : undefined }

            {/* For description Field */}     
            {status !== "SUCCESSFUL"  ?
            <Container
              direction="row"
              justify="space-between"
              style={styles.row}
            >
              <Typography color="grayMedium" textAlign="left" fontFamily="HelveticaNeue-Medium">Description</Typography>
              <Container>
                <Typography  style={{width:190}} textAlign="right" color={status == "SUCCESSFUL"  ? "success" : "error" }  fontFamily="HelveticaNeue-Medium">
                  {description}
                </Typography>
              </Container>
            </Container> : undefined }

            <View style={styles.separator} />

            {/* For Status Field */}            
            <Container
              direction="row"
              justify="space-between"
              style={styles.row}
            >
              <Typography color="grayMedium" textAlign="left" fontFamily="HelveticaNeue-Medium">Status</Typography>
              <Container>
                <Typography textAlign="right" color={status == "SUCCESSFUL"  ?"success" : "error" }  fontFamily="HelveticaNeue-Medium">
                  {status}
                </Typography>
              </Container>
            </Container>

          </Container>
        </ViewShot>

        {/* Container for Share Button */}
        <Container alignItems="center" justify="center" noItemsStretch spacing={1}>
        <Typography fontFamily="HelveticaNeue-Light" variant="chip" color="darkBlack" spacing={5}>
            {/* Rates will be refreshed in 30 seconds */}
          </Typography>
          <Button
          style={{
            height:65,
            alignItems: "center",
            justifyContent: "center",
            borderColor: theme.colors.transparent,
            borderRadius: 15,
            borderWidth: 2,
            paddingHorizontal: 100,
          }}
            text= {"Share"}
            fontWeight="bold"
            fontFamily={"Helvetica-Bold"}
            onPress={() => {
              onShareClick()
            }}
          />
        </Container>
      </Container>
    </ScreenLayoutTab>
)}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: theme.spacing.lg,
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.lg
  },
  image: {
    height: 80,
    width: 120,
  },
  buttonContainer: {
    marginHorizontal: theme.spacing.md
  },
  wadz_pay_logo: {
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.xs
  },
  row: {
    width: rowWidth,
  },
  topHeight:{
    marginTop: 10
  },
  separator: {
    marginTop: 4,
    marginBottom: 5,
    width: rowWidth,
    height: theme.borderWidth.xs,
    backgroundColor: theme.colors.gray.light
  }
})

export default TransactionSuccessFailureScreen

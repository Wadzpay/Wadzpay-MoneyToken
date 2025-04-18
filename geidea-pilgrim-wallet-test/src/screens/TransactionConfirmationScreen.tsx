// ============================== Imports =================================

import React, { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
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
import { 
  useDepositFiat, 
  useWithdrawFiat,   
} from "~/api/user"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { showToast } from "~/utils"
import { API } from "aws-amplify"
import { MenuStackParamList } from "~/components/navigators"
import { BuySellReciptScreenParam } from "~/components/navigators/MenuStackNavigator"
import { TransactionSuccessFailureParams } from "~/components/navigators/MenuStackNavigator"


// ============================== Properties & Constants =================================


const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.md
const rowWidth =
  width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs


type Props = NativeStackScreenProps<MenuStackParamList, "TransactionConfirmationScreen">



// ============================== TransactionConfirmationScreen Class =================================


const TransactionConfirmationScreen: React.FC<Props> = ({ navigation, route }: Props) => {
 
  // ************************ Constants ************************ 

  const formatter = useFormatCurrencyAmount()
  const { t } = useTranslation()
  const { bankAccountNumber, userEmail, amount, fiatType , transactionType} = route.params
  const [showModal, setShowModal] = useState(false)
  const [showProgressModal, setShowProgressModal] = useState(false)
  const [showSuccessModal, setShowSuccessModal] = useState(false)
  const [showErrorModal, setShowErrorModal] = useState(false)
  const [confirmClicked, setConfirmClicked] = useState(false)
  
  const [] = useState<Transaction>()

  // ************************ Creating Webservice API Interactions ************************ 

  const {
    mutate: withdrawFiat,
    isLoading: withdrawFiatLoading,
    error: withdrawFiatError,
    isSuccess:withdrawFiatSuccess,
    data:withdrawFiatData
  } = useWithdrawFiat("AE")

  const {
    mutate: depositFiat,
    isLoading: depositFiatLoading,
    error: depositFiatError,
    isSuccess: depositFiatSuccess,
    data: depositFiatData
  } = useDepositFiat("AE")


  // ************************ Handling Server Errors ************************ 

  useEffect(() => {

    if (depositFiatError) {

      navigation.navigate("TransactionSuccessFailureScreen",{ 
        transactionId:  "",
        transactionType: "DEPOSIT",
        bankAccountNumber: bankAccountNumber,
        userEmail: userEmail,
        fiatAmount: amount,
        createdAt: new Date() ,
        description : depositFiatError?.message || "Server Request Failed",
        status: "Failed"})
      
    }
  }, [depositFiatError])


  useEffect(() => {

    if (withdrawFiatError) {

      navigation.navigate("TransactionSuccessFailureScreen",{ 
        transactionId:  "",
        transactionType: "WITHDRAW",
        bankAccountNumber: bankAccountNumber,
        userEmail: userEmail,
        fiatAmount: amount,
        createdAt: "",
         description : withdrawFiatError?.message || "Server Request Failed",
        status: "Failed"})
    }
  }, [withdrawFiatError])


  // ************************ Handling Server Responses ************************ 

  useEffect(() => {
    if (withdrawFiatData) {

      let response : TransactionSuccessFailureParams = withdrawFiatData

        setTimeout(() => {
          setShowProgressModal(false)
          setShowSuccessModal(true)
        }, 1000)
        setTimeout(() => {
          setShowModal(false)
        }, 2000)

        // show next screen
        navigation.navigate("TransactionSuccessFailureScreen",{ 
          transactionId:  response.transactionId,
          transactionType: response.transactionType,
          bankAccountNumber: bankAccountNumber,
          userEmail: userEmail,
          fiatAmount: response.fiatAmount,
          createdAt: response.createdAt,
          status: response.status})
    }
  }, [withdrawFiatData])


  useEffect(() => {
    if (depositFiatData) {

      let response : TransactionSuccessFailureParams = depositFiatData

        setTimeout(() => {
          setShowProgressModal(false)
          setShowSuccessModal(true)
        }, 1000)
        setTimeout(() => {
          setShowModal(false)
        }, 2000)




       // show next screen
        navigation.navigate("TransactionSuccessFailureScreen",{ 
          transactionId:  response.transactionId,
          transactionType: response.transactionType,
          bankAccountNumber: bankAccountNumber,
          userEmail: userEmail,
          fiatAmount: response.fiatAmount,
          createdAt: response.createdAt,
          status: response.status})
    }
  }, [depositFiatData])

  // ************************ Button Clicks ************************ 

  const onBackButtonClick = (): any => {
    navigation.goBack() // brackets were missing added them
  }

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

  const onConfirmClick = () => {
    
    if(confirmClicked == false) {

      setConfirmClicked(true)

      // hit the API
      setShowModal(true)
      setShowProgressModal(true)

      transactionType == "deposit" ?  depositFiat({
          fiatAmount: amount,
          fiatType: fiatType,
          bankAccountNumber:bankAccountNumber,
        }): withdrawFiat({
          fiatAmount: amount,
          fiatType: fiatType,
          bankAccountNumber:bankAccountNumber,
        })
    }
  }

   // ************************ UI Part ************************ 

  return (
    <ScreenLayoutTab
      title={"Transaction Confirmation"}
      leftIconName="ArrowLeft"
       onLeftIconClick={onBackButtonClick}
    >
    
    <Container justify="space-between" style={styles.container}>
   
      <Container
      alignItems="center"
      noItemsStretch
      spacing={1}
      style={{ marginVertical: 20 }}
      >
        {/* Container for From field */}
        <Container
        direction="column"
        justify="space-between"
        style={styles.row}
        >
          <Typography textAlign="left" fontFamily="HelveticaNeue-Medium" style={{ fontSize:14 , color:"#7C7A7A" }}>From</Typography>
          <Typography fontFamily="Helvetica-Bold" textAlign="left" style={{ fontSize:16 }}>{transactionType == "deposit" ? "IBAN" : ""}</Typography>
          <Container>
          <Typography textAlign="left" fontFamily="Helvetica-Bold" style={{ fontSize:16 }}>
          {transactionType == "deposit" ? "AE"+bankAccountNumber : userEmail}
          </Typography>
          </Container>
        </Container>

        <Typography></Typography>
        <View style={styles.separator} />
        <Typography></Typography>

        {/* Container for To field */}
        <Container
        direction="column"
        justify="space-between"
        style={styles.row}
        >
          <Typography textAlign="left" fontFamily="HelveticaNeue-Medium" style={{ fontSize:14 , color:"#7C7A7A"}}>To</Typography>
          <Typography textAlign="left" fontFamily="Helvetica-Bold" style={{ fontSize:16 }}>{transactionType == "deposit" ? "" : "IBAN"}</Typography>
          <Container>
            <Typography textAlign="left" fontFamily="Helvetica-Bold" style={{ fontSize:16 }}>
            {transactionType == "deposit" ? userEmail : "AE"+bankAccountNumber}
            </Typography>
          </Container>
        </Container>

        <Typography></Typography>
        <View style={styles.separator} />
        <Typography></Typography>

        {/* Container for Total Amount field */}
        <Container
        direction="row"
        justify="space-between"
        alignItems="center"
        noItemsStretch
        style={styles.row}
        >
          <Typography textAlign="left" fontFamily="Helvetica-Bold" style={{ fontSize:20 }}>Total Amount</Typography>
          <Container>
            <Typography textAlign="right" fontFamily="Helvetica-Bold" style={{ fontSize:20 }}>
              {"AED "}{amount}
            </Typography>
          </Container>
        </Container>
      </Container>

      {/* Container for Deposit / Withdraw Button */}
      <Container alignItems="center" justify="center" noItemsStretch spacing={15}>
          <Typography fontFamily="HelveticaNeue-Light" variant="chip" color="darkBlack" spacing={25}>
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
              text= {"Confirm"}
              fontWeight="bold"
              fontFamily={"Helvetica-Bold"}
              loading={depositFiatLoading || withdrawFiatLoading}
              disabled = {transactionType == "deposit" ? ((depositFiatLoading == true) ? true : false)
              :
              ((withdrawFiatLoading == true) ? true : false)}
              onPress={() => {
                onConfirmClick()
              }}
            />
      </Container>

    </Container>
  </ScreenLayoutTab>
 )
}

 const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: theme.spacing.lg,
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.lg
  },
  image: {
    height: 80,
    width: 80,
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.xs
  },
  buttonContainer: {
    marginHorizontal: theme.spacing.md
  },
  wadz_pay_logo: {
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.xs
  },
  row: {
    width: rowWidth
  },
  separator: {
    marginTop: 4,
    marginBottom: 5,
    width: rowWidth,
    height: theme.borderWidth.xs,
    backgroundColor: theme.colors.gray.light
  }
})
 export default TransactionConfirmationScreen


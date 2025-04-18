import React, { useContext, useState, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { StackScreenProps } from "@react-navigation/stack"
import {
  StyleSheet,
  View,
  Dimensions,
  TouchableOpacity,
  TextInput,
  Alert,
  Image
} from "react-native"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import { CommonActions, useIsFocused } from "@react-navigation/native"

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
  Typography,
  DismissKeyboard,
  CommonAlertModal,
  LoadingSpinner
} from "~/components/ui"
import { Asset, AssetIconNamesMap, SendFundType } from "~/constants/types"
import {
  useUserBalances,
  useSendDigitalCurrencyToExternalWallet,
  useAddTransaction,
  useTransactionValidationConfig
} from "~/api/user"
import { useGetExchangeRate, useGetP2pFee } from "~/api/onRamp"
import { UserContext } from "~/context"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { useTranslationItems } from "~/constants/translationItems"
import { fieldProps, useValidationSchemas, WADZPAY_WALLET } from "~/constants"
import { SelectSendWalletForm } from "~/constants/formTypes"
import { AssetFractionDigits, TransactionTypeControl } from "~/api/constants"
import { calculateEstimatedFee, ConTwoDecDigit, isTransactionAllowed,sartTxt } from "~/utils"
//import RadioButtonRN from "radio-buttons-react-native"
import { useNavigation } from "@react-navigation/native"

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.xl
//import { sartTxt } from "~/utils"


const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: theme.spacing.lg,
    marginTop: 50
  },
  image: {
    height: 40,
    width: 40
  },
  availableBalnceView: {
    marginVertical: 45,
    backgroundColor: "white", //"#ececec",
    paddingVertical: 15,
    borderColor: "#E8E8E8",
    borderRadius: 10,
    borderWidth: 1
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
    flex: 1,
    justifyContent: "flex-end"
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
  },
  textFieldStyle: {
    height: 35,
    fontSize: 24,
    paddingHorizontal: theme.spacing.xs,
    textAlign: "left",
    color: theme.colors.midDarkToneGray,
    fontFamily: "Rubik-Medium"
  },
  box: {
    marginLeft: 20,
    marginRight: 20,
    marginVertical: 20,
    paddingHorizontal: 10,
    height: 60,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: "#C4C4C4",
    backgroundColor: "#F6F6F6"
  }
})

type Props = {
  walletAddress1: string
  amount1: number | string
  asset1: string | Asset
  merchantName: string
  transactionId: string
  onWalletAddressChange?: (value: string) => void
  transactionTypeControlQuery?: string
  merchantId?: string
  posId?: string
}

const SendExternalComponent: React.FC<Props>
 = ({
  walletAddress1,
  amount1,
  asset1,
  merchantName,
  transactionId,
  transactionTypeControlQuery,
  onWalletAddressChange,
  merchantId = "",
  posId = ""
}: Props) => {
  const { t } = useTranslation()
  const navigation = useNavigation()
  const { fiatAsset, user, sarTokens, instDetails } = useContext(UserContext)
  const isFocused = useIsFocused();
  const { data: p2pFee } = useGetP2pFee()
  const P2P_FEE_PERCENTAGE = p2pFee ? p2pFee : 0
  const formatter = useFormatCurrencyAmount()
  const [confirmButtonDisable, setConfirmButtonDisable] = useState(false)
  const [paymentAmount, setPaymentAmount] = useState(amount1)
  const [showInvalidQrMsg, setShowInvalidQrMsg] = useState(false)
  // const [textTypeDecimal, setIsTextTypeDecimal] = useState(false);
  const [showAlertConfig, setshowAlertConfig] = useState({name: "",message:""})
  const { depositSchema } = useValidationSchemas()
  //const specialChars = /[`!@#$%^&*()_+\-=\[\]{};':"\\|,<>\/?~]/;


  // const hideTabBar = () => {
  //   navigation.setOptions({
  //     tabBarStyle: { display: 'none' },
  //   });
  // };

  useEffect(() => {
    navigation.getParent()?.setOptions({
      tabBarStyle: {
        display: "none"
      }
    });
    return () => navigation.getParent()?.setOptions({
      tabBarStyle: undefined
    });
  }, [navigation]);
  // TODO remake with a useForm instead - validation will be needed
  const [transaction, setTransaction] = useState<{
    asset: Asset | string
    walletAddress: string
    amount: number | string
    transactionId: string
  }>({
    asset: asset1 ?? "",
    walletAddress: walletAddress1 ?? "",
    amount: amount1 ?? undefined,
    transactionId: transactionId
  })

  const { 
    data: transactionConfigData,
    refetch: refetchTransactionConfigData,
    isSuccess: isTransactionConfigSuccess,
    error: transactionConfigError,
  } = useTransactionValidationConfig(transactionTypeControlQuery || "")

  const {
    data: balances,
    isFetching: balancesIsFetching,
    error: balancesError
  } = useUserBalances()

  const { data: exchangeRates, isFetching: exchangeRatesIsFetching } =
    useGetExchangeRate(fiatAsset)

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

  useEffect(() => {
    refetchTransactionConfigData()
  }, [isFocused])

  const getBalancesAmount = () => {
    return balances && balances[transaction.asset]
      ? balances[transaction.asset]
      : 0
  }

  useEffect(() => {
    // check limit for transactions 
    if (transactionConfigData &&  transactionTypeControlQuery == TransactionTypeControl.PURCHASE) {
  // console.log("Transaction config API response ----> ", transactionTypeControlQuery, transactionConfigData)
      // console.log("Transaction config utils method ----> send external", isTransactionAllowed( transactionConfigData , 500))

      const isTransactionPermitted =
      transactionConfigData?.per_TRANSACTION == null
      ? 'allowed'
      : isTransactionAllowed(transactionConfigData, Number(amount1));

     
      setShowError('')

      if (isTransactionPermitted !== 'allowed') {
        setShowError(isTransactionPermitted)
       // setTransaction({...transaction, amount: 0});
      } 
      if (sarTokens < Number(amount1)) {
                  
        setShowError('Insufficient Funds for Transfer');

       // setTransaction({...transaction, amount: 0})
    }

    }
  }, [transactionConfigData, isTransactionConfigSuccess, ])

  useEffect(() => {
    //check if user and merchant name is similar
    
    if (merchantName == user?.attributes.email ){
      setshowAlertConfig({name: "Sacn to pay", message : "Can not transfer to this QR code"})
      }
  }, [])


  useEffect(() => {
    if (
      !merchantName ||
      !asset1 ||
      !walletAddress1 ||
      !amount1
      // ||
      // !transactionId
    ) {
      setShowInvalidQrMsg(true)
    } else {
      setShowInvalidQrMsg(false)
    }
  }, [])

  useEffect(() => {
    if (
      (isSuccess && addTransactionData) ||
      (isSendExternalWalletSuccess && sendExternalWalletData)
    ) {
      let response = addTransactionData || sendExternalWalletData
      // if (fromScreen === "notification") {
      //   // savePaymentInfoForNotification(response?.uuid , response?.status)
      //   sendNotifyRequestStatusForNotification(response?.uuid, response?.status)
      // }
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
  const [showError, setShowError] = useState('');

  const onDismiss = () => {
    setShowInvalidQrMsg(false)
  }
  //// console.log("transactionId ", transactionId, "merchantId " , merchantId, "posId ", posId)
  let temp_transaction_id = transaction.transactionId.replace(/^0+/, "")
  let temp_merchantId = merchantId.replace(/^0+/, "")
 // let temp_posId = posId.replace(/^0+/, "")
 // // console.log("temp_transaction_id ", temp_transaction_id, "temp_tmerchantId " , temp_merchantId, "temp_posId ", temp_posId)
  //// console.log("temp_transaction_id ", temp_transaction_id.length, "temp_tmerchantId " , temp_merchantId.length, "temp_posId ", temp_posId.length)
 
  let getScreenType = () => {
    if(temp_transaction_id.length > 0) {
      // merchant online
      // console.log("merchant online ")
      return "merchantOnline"
    } 
    else if( temp_merchantId.length > 0 && temp_transaction_id.length == 0 ) {
      // merchant offline
      // console.log("merchant offline")
      return "merchantOffline"
    } else { 
      // Peer_2_peer
      // console.log("Peer_2_peer")
      return "peer_2_peer"
    }
  }
  let getTransactionType = () => {
    if(temp_transaction_id.length > 0) {
      // merchant online
      // console.log("merchant online ")
      return "Payment"
    } 
    else if( temp_merchantId.length > 0 && temp_transaction_id.length == 0 ) {
      // merchant offline
      // console.log("merchant offline")
      return "Payment"
    } else { 
      // Peer_2_peer
      // console.log("Peer_2_peer")
      return "Transfer"
    }
  }

  let getQrPaymentPayload = () => {
    //
    if(temp_transaction_id.length > 0) {
      // merchant online
      // console.log("merchant online ")
      return {
         amount: paymentAmount,
         asset: asset1,
         description:"merchant online",
         receiverAddress: transaction.walletAddress,
         uuid: transaction.transactionId
        }
    } 
    else if( temp_merchantId.length > 0 && temp_transaction_id.length == 0 ) {
      // merchant offline
      // console.log("merchant offline")
      return {
        amount: paymentAmount,
        asset: asset1,
        description:"merchant offline",
        receiverAddress: transaction.walletAddress,
        merchantId: merchantId,
        posId: posId
       }
    } else { 
      // Peer_2_peer
      // console.log("Peer_2_peer")
      return {
        amount: paymentAmount,
        asset: asset1,
        description:"Peer 2 peer",
        receiverAddress: transaction.walletAddress,
       }
    }


  }

  let WalletNavigation = () => {
 // console.log("transaction :", transaction)
    setshowAlertConfig({name: "", message : ""})
    let isTransactionPermitted =  transactionConfigData?.per_TRANSACTION == null ? "allowed" : isTransactionAllowed(
      transactionConfigData,
      Number(paymentAmount)
    ) // transaction control and loading
  
    
    if (asset1 && walletAddress1 && amount1) {
      setShowInvalidQrMsg(false)

      // console.log("isTransactionPermitted ", isTransactionPermitted)
      if (isTransactionPermitted !== "allowed") {
        setshowAlertConfig({name: "Scan To pay", message : isTransactionPermitted})
       // Alert.alert(isTransactionPermitted, "", [{ text: "Ok" }]) // transaction control and loading
      } else if ( Number(paymentAmount) == 0) {
        setshowAlertConfig({name: "Scan To pay", message : "Please enter amount"})
      }
       else {
        // via POS machine that  is online merchant
        // static QR code will not contain any transaction id so uuid is set to null
        // merchant static QR code OR User static QR
       
        // console.log("requestCode ", getQrPaymentPayload() )

        navigation.navigate("TransactionConfirmationScreenTopUp" ,
        {
          transactionConfirmationDataParam : {
          requestPayload: getQrPaymentPayload(),
        transactionType : getTransactionType(),
        screenType: getScreenType(),
        amountRequested :`${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(paymentAmount).toFixed(2)}`,
        totalAmount : `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(paymentAmount).toFixed(2)}`,
        totalFee : 0.00,
        tokenAsset :'SART',
        transactionConfigData: transactionConfigData,
        ttc : transactionTypeControlQuery,
        feeTitle: 'Net Tokens Paid',
        netAmountCalulated : `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(paymentAmount).toFixed(2)}`,
        totalAmountText : "Tokens to be paid"
        }
        })
      //sendDigitalCurrencyExternalWallet() 
      }
    } else {
      setShowInvalidQrMsg(true)
    }
  }

  const numberRegex = /^(?:\d*\.\d{1,2}|\d+)$/;
  const specialChars = /[`!@#$%^&*()_+\-=\[\]{};':"\\|,<>\/?~]/;

  let getDisabledState = () => {

    const isTransactionPermitted =
    transactionConfigData?.per_TRANSACTION == null
      ? 'allowed'
      : isTransactionAllowed(transactionConfigData, Number(paymentAmount));

    return (paymentAmount && paymentAmount != 0 &&  sarTokens >= paymentAmount && numberRegex.test(transaction.amount.toString())&& (!specialChars.test(transaction.amount.toString())) && isTransactionPermitted === 'allowed')
      ? false
      : true;
  };

  let showAlert = () => {
    if (paymentAmount && Number(paymentAmount) > sarTokens) {
      setshowAlertConfig({name: "Scan to pay", message : "Insufficient Funds for Transfer"})
    } else if (paymentAmount && Number(paymentAmount) <= 0) {
      setshowAlertConfig({name: "Scan to pay", message : "Please enter valid amount"})

    } else if (
      String(paymentAmount).includes(",") ||
      String(paymentAmount).includes(" ") ||
      String(paymentAmount).includes("-") ||
      String(paymentAmount).includes("+") ||
      String(paymentAmount).includes("*")
    ) {
      setshowAlertConfig({name: "Scan to pay", message : "Please enter valid amount"})
    } else if ( merchantName == user?.attributes.email) {
      setshowAlertConfig({name: "Scan to pay", message : "Can not transfer to this QR code"})
    }
  }

  //// console.log("showInvalidQrMsg ", showInvalidQrMsg)

  return (
    <ScreenLayoutTab showTitleHeader={false} useScrollView={false}>
      <ErrorModal error={balancesError} />
      <ErrorModal error={sendExternalWalletError} />
      {showAlertConfig.message.length > 0 &&  <ErrorModal error={showAlertConfig} />}
      {showInvalidQrMsg && (
        <CommonAlertModal
          subtitle="Something Went wrong"
          message={"QR code you have scanned is invalid!!"}
        />
      )}
      <DismissKeyboard>
        {
          isTransactionConfigSuccess ? 
       
        <Container justify="space-between" style={styles.container}>
          <Container spacing={1}>
            {/* Wallet Address */}
            {/* <Container
              direction="row"
              alignItems="center"
              noItemsStretch
              justify="flex-start"
              style={[styles.box, {marginTop:30}]}>
             
              <TextInput
                  value={transaction.walletAddress}
                  style={styles.textFieldStyle}
                  placeholder={t("Enter The Amount")}
                  keyboardType="numeric"
                  contextMenuHidden={true}
              />
              <TouchableOpacity
                style={{ flex: 1, marginRight:10}}
                onPress={() => {
                    setTransaction({
                        asset:  "ETH",
                        walletAddress: "",
                        amount: 0
                      })
                    // onWalletAddressChange("")
                }
                }
              >
                <Icon name={"Scanner"} size="sm" />
              </TouchableOpacity>
          </Container> */}

            {/* Confirm & Pay Logo */}
            <Container
              alignItems="center"
              justify="center"
              noItemsStretch
              spacing={1}
            >
              <Container
                direction="column"
                noItemsStretch
                alignItems="center"
                justify="space-evenly"
                style={{
                  width: 70,
                  height: 70,
                  borderColor: "#FFA600",
                  borderRadius: 50,
                  borderWidth: 2,
                  padding: 10
                }}
              >
                <Image
                  source={require("~images/confirm.png")}
                  style={styles.image}
                />
              </Container>
            </Container>

            {/* Merchant Name Label */}
            <Container
              alignItems="center"
              justify="center"
              noItemsStretch
              style={{ marginTop: 10 }}
            >
              <Typography
                variant="button"
                fontFamily="Rubik-Medium"
                color="BlackLight"
                textAlign="center"
              >
                {merchantName}
              </Typography>
            </Container>

            {/* Confirm & Pay Label */}
            <Container
              alignItems="center"
              justify="center"
              noItemsStretch
              style={{ marginTop: 50 }}
            >
              <Typography
                variant="body"
                fontFamily="Rubik-Regular"
                color="BlackLight"
                textAlign="center"
              >
                You are paying
              </Typography>
            </Container>

            {/* Amount data */}

            <Container
              direction="row"
              alignItems="center"
              noItemsStretch
              justify="center"
              style={{ marginTop: 10 , marginLeft:90}}
            >
              <Typography
                variant="subtitle"
                color="midDarkToneGray"
                fontFamily="Rubik-Medium"
                textAlign="center"
              >
                {asset1 == "SART" ? instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : "" : asset1}{" "}
              </Typography>
              <TextInput
                value={paymentAmount}
                keyboardType="number-pad"
                placeholder={'0.00'}
               // maxLength={textTypeDecimal ? 11 : 8}
                style={{
               //   backgroundColor:theme.colors.blueLinkColor,
                  color: "#636363",
                  alignSelf:"flex-start",
                  fontFamily: "Rubik-Medium",
                  fontSize: 24,
                  textAlign: "left",
                    height: 75,
                    width:180,
                   // paddingHorizontal: theme.spacing.md,
                }}
                editable={!(temp_transaction_id.length > 0)}
                onChangeText={(newText) => {
                //  console.log("newText.includes ",newText.includes("0"))
                 // console.log("/^0*$/.test(newText) ",/^0*$/.test(newText))
                 // console.log("newText.includes ",newText.includes("0"))
               
                 var good = newText
                 // remove bad characters and multiple points
                 //.replace(/[^\d.]|\.(?=.*\.)/, '')
                 // remove any excess of 8 integer digits
                 .replace(/^(\d{8})\d+/, '$1')
                 // remove any excess of 2 decimal digits
                 .replace(/(\.\d\d).+/, '$1');
   
                 //console.log("*----*  good " , good)
   
                 newText = good
                 const reg = /^-?\d*(\.\d*)?$/;
                 const amountLimit = newText.includes('.') ? 10 : 8;
                const isTransactionPermitted =
                  transactionConfigData?.per_TRANSACTION == null
                  ? 'allowed'
                  : isTransactionAllowed(transactionConfigData, Number(newText));

                 
                  setShowError('')

                  if (isTransactionPermitted !== 'allowed') {
                    setShowError(isTransactionPermitted)
                    setPaymentAmount('');
                   // setTransaction({...transaction, amount: 0});
                  } 
                  if (sarTokens < Number(newText)) {
                  
                    setShowError('Insufficient Funds for Transfer');
          
                   // setTransaction({...transaction, amount: 0})
                }

                if (newText.length > amountLimit) {
                  return false;
                }
                 else if (newText.length <= 0) {
                 setShowError('');
                }

                 if(newText.startsWith('.')) {
                  newText = newText.replace(/^.+/, '0.');
                } 
                
                if (newText === '-' || newText == '0') {
                  setShowError('Please Enter valid amount');
                  setPaymentAmount('');

                } 
                
                if (reg.test(newText) || newText === '' || newText === '-') {
                  if (newText.indexOf('.') >= 0 == false && newText.length > 0) {
                    newText = newText.replace(/^0+/, '');
                  } else if (newText.indexOf('.') >= 0) {
                    newText = newText.replace(/^0+/, '0');
                  }
            
                  setPaymentAmount(
                    newText.indexOf('.') >= 0
                      ? newText.substr(0, newText.indexOf('.')) +
                      newText.substr(newText.indexOf('.'), 3)
                      : newText.replace(/^0+/, ''),
                  );
                  amount1 = newText
                  } 

                //  if (/^0*$/.test(newText)) {
                //       setPaymentAmount("")
                //  } 
                //   else if (
                //  //   newText == '0' ||
                //    // newText.includes("0")||
                //     newText.includes(' ') ||
                //     specialChars.test(newText) ||
                //     newText.includes(',') ||
                //     newText.includes('-') ||
                //     newText.includes('+') ||
                //     newText.includes('*')
                //   ) {
                //    //setPaymentAmount("")
                //   } else {
                //     if(!Math.floor(Number(newText)) && !newText.includes("0.") && newText.includes(".")){
                //       newText = newText.replace(".", "0.")
                //     }
                //    newText = ConTwoDecDigit(newText)
                //    setPaymentAmount(newText)
                //    amount1 = newText
                //   }
                 
               }}
              />
              
            </Container>

            <Typography variant="label"
              fontFamily="Rubik-Regular"
              color="error"
              textAlign="center">
              {showError.length > 0 ? showError : ''}
            </Typography>

            {/* Available Balance of SART */}
            <Container
              direction="row"
              alignItems="center"
              noItemsStretch
              justify="center"
              style={styles.availableBalnceView}
            >
              <Typography
                variant="label"
                color="midDarkToneGray"
                fontFamily="Rubik-Medium"
                textAlign="left"
              >
                {`Available ${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} Tokens:`}
              </Typography>
              <Typography
                style={{ paddingHorizontal: 10 }}
                variant="label"
                color="midDarkToneGray"
                fontFamily="Rubik-Medium"
                textAlign="left"
              >
                  {formatter(sarTokens, {
                asset: 'SART',
              })}
              </Typography>
            </Container>

            {/* Amount */}
            {/* <Container
                  direction="row"
                  alignItems="center"
                  noItemsStretch
                  justify="flex-start"
                  style={[styles.box]}>
                  <Container
                      alignItems="flex-start"
                      noItemsStretch
                      justify="flex-start"
                      direction="row"
                    >
                      <Typography variant="label" fontFamily="HelveticaNeue-Medium" color="BlackLight" textAlign="center" style={{top:4}}>
                      SARt 
                      </Typography>
                      <View style={{ marginHorizontal: 10, borderRadius: 3, height: 25, width: 1.5, backgroundColor: theme.colors.gray.light }}></View>
                  </Container>

                  <TextInput
                      value={"0.000029"}
                      style={styles.textFieldStyle}
                      placeholder={t("Enter The Amount")}
                      keyboardType="numeric"
                      contextMenuHidden={true}
                  />
          </Container> */}
          </Container>

          <Container
            direction="row"
            justify="space-around"
            style={{ marginVertical: 20 }}
          >
            {/* Wallet address Change */}
            <Container style={{ width: 160, paddingRight: 20, height: 100 }}>
              <TouchableOpacity
                style={[
                  {
                    borderColor: theme.colors.black,
                    borderRadius: 6,
                    borderWidth: 1,
                    height: 50,
                    width: "100%",
                    alignItems: "center",
                    justifyContent: "center",
                    // position: 'absolute',
                    top: 0
                  },
                  { backgroundColor: theme.colors.white }
                ]}
                onPress={() => {
                  onWalletAddressChange("")
                  // navigation.navigate("Home")
                }}
              >
                <Typography
                  variant="label"
                  fontWeight="bold"
                  textAlign="center"
                  fontFamily="Rubik-Medium"
                >
                  Cancel
                </Typography>
              </TouchableOpacity>
            </Container>

            {/* Button Container */}
            <Container style={{ width: 160, paddingLeft: 20, height: 100 }}>
            
            <TouchableOpacity
              style={[
                {
                  borderRadius: 6,
                  borderWidth: 1,
                  height: 50,
                  width: '100%',
                  alignItems: 'center',
                  justifyContent: 'center',
                },
                {
                  backgroundColor:
                  getDisabledState()
                      ? theme.colors.white
                      : theme.colors.orange,
                },
                {
                  borderColor:
                  getDisabledState()
                      ? theme.colors.black
                      : theme.colors.orange,
                },
              ]}
              disabled={getDisabledState()}
              onPress={() => {
                if (isAddTransactionDataLoading == true) {
                } else {
                  WalletNavigation();
                }
              }}>
              <Typography
                variant="heading"
                textAlign="center"
                color='darkBlackBold'
                fontFamily="Rubik-Medium">
                {' '}
                {isAddTransactionDataLoading
                  ? t('Please Wait  ...')
                  : `Pay`}
              </Typography>
            </TouchableOpacity>
              {/* <Button
                text="Pay"
                textVariant="label"
                fontFamily="Rubik-Medium"
                loading={isSendExternalWalletLoading}
                style={[
                  {
                    borderRadius: 6,
                    borderWidth: 0,
                    height: 50,
                    width: "100%",
                    alignItems: "center",
                    justifyContent: "center",
                    top: 0
                  }
                  // {
                  //   backgroundColor:
                  //   getDisabledState()
                  //       ? theme.colors.white
                  //       : theme.colors.orange,
                  // },
                  // {
                  //   borderColor:
                  //   getDisabledState()
                  //       ? theme.colors.black
                  //       : theme.colors.orange,
                  // }
                ]}
                disabled={getDisabledState()}
                onPress={() => {
                  ;(paymentAmount && Number(paymentAmount) > sarTokens) ||
                  (paymentAmount && Number(paymentAmount) <= 0) ||
                  String(paymentAmount).includes(",") ||
                  String(paymentAmount).includes(" ") ||
                  String(paymentAmount).includes("-") ||
                  String(paymentAmount).includes("+") ||
                  String(paymentAmount).includes("*") ||
                  merchantName == user?.attributes.email
                    ? showAlert()
                    : WalletNavigation()
                }}
              /> */}
            </Container>
          </Container>

          {/* <View style={styles.buttonContainer}>
            
          <Button
          style={{width:"100%",position: 'absolute',bottom:120}}
            text={"Confirm & Pay"}
            onPress={() => {
              Number(transaction.amount) > sarTokens ?  showAlert() : WalletNavigation()
            }}
            loading={isSendExternalWalletLoading}
          />
          <View style={{ height: 20 }}></View>
          <Button
          style={{width:"100%",position: 'absolute',
          bottom:60}}
            text={"Cancel"}
            variant={"secondary"}
            onPress={() => {
              onWalletAddressChange("")
             // navigation.navigate("Home")
            }}
          />
        </View>
         */}
        </Container> : 
        <View style={{height: theme.fontHeight.subtitle, alignItems:"center"}}>
                          <LoadingSpinner color="black" />
                        </View> }
      </DismissKeyboard>
    </ScreenLayoutTab>
  )
}

export default SendExternalComponent
function onChangeText(text: string): void {
  throw new Error("Function not implemented.")
}

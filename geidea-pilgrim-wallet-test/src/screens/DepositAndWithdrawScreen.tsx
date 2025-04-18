// ============================== Imports =================================

import React, { useContext, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  Alert,
  TouchableOpacity,
  StyleSheet,
  View,
  Dimensions,
  TextInput,
  Platform
} from "react-native"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import { MenuStackParamList } from "~/components/navigators"
import {
  Typography,
  ErrorModal,
  LoadingSpinner,
  Container,
  ScreenLayoutTab,
  theme,
  Icon,
  Modal,
  Checkbox,
  Button,
  AvailableBalance
} from "~/components/ui"
import Withdraw from "~/icons/Withdraw"
import { Image, Tooltip } from "react-native-elements"
import {
  fieldProps,
  useValidationSchemas,
  WithdrawDepositForm
} from "~/constants"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import { 
  useUserBalances, 
  useGetFiatBalance,
} from "~/api/user"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { UserContext } from "~/context"
import { useGetExchangeRate } from "~/api/onRamp"
import IbanVerificationScreen from "./IbanVerificationScreen"
import { FiatSignMap } from "~/constants/types"
import Clipboard from '@react-native-clipboard/clipboard';




export const isIOS = Platform.OS === "ios"

// ============================== Constants =================================

type Props = NativeStackScreenProps<MenuStackParamList, "DepositAndWithdrawScreen">

// ============================== Class =================================

const DepositAndWithdrawScreen: React.FC<Props> = ({ navigation, route }: Props) => {

  // ************************ Class Variables ************************ 

  const { t } = useTranslation()
  const formatter = useFormatCurrencyAmount()

  // ************************ Parameters Received ************************ 

  const { screenType, asset } = route.params ?? {}

  // ************************ Use State Methods ************************ 

  const [amount, setAmount] = useState("")
  const [value, setValue] = useState<{ value: any }>("AED")
  const [valueRight, setValueRight] = useState<{ valueRight: string  }>("ETH")
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [isAlertVisible, setIsAlertVisible] = useState(false)
  const [isErrorVisible, setIsErrorVisible] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [showProgressModal, setShowProgressModal] = useState(false)
  const [showSuccessModal, setShowSuccessModal] = useState(false)
  const [showErrorModal, setShowErrorModal] = useState(false)
  const { user } = useContext(UserContext)
  const [isValidationError, setIsValidationError] = useState(false)
  const [isValidationErrorTextInput, setIsValidationErrorTextInput] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")
  const [errorMessageTextInput, setErrorMessageTextInput] = useState("")
  const [checkboxSelected, setCheckboxSelected] = useState(true)
  const [submitButtonPressed, setSubmitButtonPressed] = useState(false)

  // ************************ API Calling Methods ************************ 

  const {
    data: balancesData,
    isFetching: isFetchingBalances,
    isFetched: isFetchedBalances,
    error
  } = useUserBalances()
  const isCryptoAmountAvailable = balancesData && balancesData[valueRight == "XSGD" ? "BTC" : valueRight] >= amount


  const {
    data: fiatBalancesData,
    isFetching: isFetchingfiatBalancesData,
    isSuccess: isSuccessfiatBalancesData,
    refetch: refetchfiatBalancesData
  } = useGetFiatBalance()

  const { data: exchangeRatesData } = useGetExchangeRate(value)

  const fiatBalanceAed =    fiatBalancesData?.find((item) => item.fiatasset === "AED")
  
  const { height } = Dimensions.get("window")
  const { width } = Dimensions.get("window")
  const containerMargin = theme.spacing.md
  const rowWidth = width - containerMargin * 10 - theme.iconSize.md - theme.spacing.xs
  const width_proportion = "80%"
  const height_proportion = "40%"

  const { withdrawDepositSchema } = useValidationSchemas()

  const {  
    bankAccountNumber,
    countryCode,
    isIbanSkipped, 
  } = useContext(UserContext)


  const isFiatAmountAvailable = fiatBalanceAed && fiatBalanceAed.balance >= amount


  const {
    control,
    handleSubmit,
    formState: { errors },
    getValues
  } = useForm<WithdrawDepositForm>({
    resolver: yupResolver(withdrawDepositSchema),
    defaultValues: {
      fiatAmount: "0"
    }
  })

  // ************************ Button Clicks ************************ 

  const onSubmit = () => {

    if(!amount) {
      setIsValidationError(true) 
      setErrorMessage("Enter Amount")
    }
    else if (amount == ".") {
      setIsValidationError(true) 
      setErrorMessage("Enter amount properly")
    }
    else if (isValidationErrorTextInput == true) {
      setErrorMessage("Special characters are not allowed except decimal")
    }
    else if (amount.toString().includes(".") && amount.split(".").length>2) {
      setIsValidationErrorTextInput(true) 
      setErrorMessageTextInput("Decimals allowed only once")
    }
    else if (amount.toString().includes(".") && amount.split(".")[1].length>2) {
      setIsValidationErrorTextInput(true) 
      setErrorMessageTextInput("Amount allowed till 2 decimal places only")
    }
    else if( !isFiatAmountAvailable && screenType == "withdraw"){
      setIsValidationError(true) 
      setErrorMessage("Insufficient funds to execute withdraw")
    }
    else if( Number(amount) < 10){
      setIsValidationError(true) 
      setErrorMessage("Minimum amount to transact is 10 AED")
    }
    else if(Number(amount) > 5000){
      setIsValidationError(true) 
      setErrorMessage("Maximum Amount to transact is 5000 AED")
    } 
    else if (!control._formValues.isDisclaimerChecked) {
      setCheckboxSelected(false) 
    }
    else {
      setIsValidationError(false) 

      screenType == "deposit" ?  
        navigation.navigate("TransactionConfirmationScreen" , {bankAccountNumber:bankAccountNumber, userEmail:user?.attributes?.email, amount:amount , fiatType:"AED", transactionType:"deposit"})
      : navigation.navigate("TransactionConfirmationScreen" , {bankAccountNumber:bankAccountNumber, userEmail:user?.attributes?.email, amount:amount , fiatType:"AED", transactionType:"withdraw"})
    }

    // !amount
    //   ? Alert.alert("", "Please Enter Amount.")
    //   : !isCryptoAmountAvailable
    //   ? Alert.alert("", txt)
    //   : (Number(amount) < 10) 
    //   ? Alert.alert("", "Minimum amount to transact is 10 AED")
    //   : (Number(amount) > 5000)
    //   ? Alert.alert("", "Maximum Amount to transact is 5000 AED")
    //   : !control._formValues.isDisclaimerChecked
    //   ? Alert.alert("", "Please select disclaimer")
      // : screenType == "deposit" ?  
      //   navigation.navigate("TransactionConfirmationScreen" , {bankAccountNumber:str, userEmail:user?.attributes?.email, amount:amount , fiatType:"AED", transactionType:"deposit"})
      // : navigation.navigate("TransactionConfirmationScreen" , {bankAccountNumber:str, userEmail:user?.attributes?.email, amount:amount , fiatType:"AED", transactionType:"withdraw"})
  }

  
  const onBackButtonClick = () => {
    setIsErrorVisible(false)
    navigation.goBack()
  }

  // ************************ UI Part ************************ 

  return (
    <ScreenLayoutTab
      title={screenType == "deposit" ? t("Deposit") : t("Withdraw")}
      leftIconName="ArrowLeft"
      onLeftIconClick={onBackButtonClick}
    >
      {/* Main Container */}
      {bankAccountNumber.length === 0 ?
       <Container spacing={0} style={styles.ibanContainer}>
        <IbanVerificationScreen  hideSkip={true}>

        </IbanVerificationScreen>
       </Container>
      : <Container noItemsStretch spacing={3} style={styles.mainContainer}>

       <Container style={{marginTop:40}} alignItems="center" noItemsStretch justify="center">
        
       {isFetchingfiatBalancesData ? 
        <Container alignItems="center" noItemsStretch>
          <LoadingSpinner color="orange" />
        </Container> 
        : <AvailableBalance 
        fiatAsset={"AED"}
        fiatBalancesData={fiatBalancesData}
        isAmountAvailable={screenType=="deposit"?true:isFiatAmountAvailable}
        variant={"body"}>
        </AvailableBalance> }
       </Container>

        {/* for Extra space below Available balance */}
        <Container></Container>

        {/* Container for TextField : Amount */}
        <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          justify="flex-start"
          style={[styles.box, styles.shadowProp]}
        >
          <Container
            alignItems="center"
            noItemsStretch
            justify="center"
            style={{
              backgroundColor: "#F9F8F8",
              height: 49,
              width: 80,
              borderBottomLeftRadius: 15,
              borderTopLeftRadius: 15,
              borderColor: theme.colors.transparent,
              borderWidth: 1
            }}
          >
            <Typography variant="label" textAlign="center">
              {FiatSignMap["AED"]}
            </Typography>
          </Container>

         <TextInput
          value={amount}
            style={styles.textFieldStyle}
            placeholder={t("Enter The Amount")}
            keyboardType="numeric"
            contextMenuHidden={true} 
            onFocus={() => Clipboard.setString('')} 
            onSelectionChange={() => Clipboard.setString('')}
            onChangeText={(newText) => {
              if (newText.includes(",") || newText.includes(" ") || newText.includes("-") || newText.includes("+") || newText.includes("*")) {
                setIsValidationErrorTextInput(true)
                setErrorMessageTextInput("Special characters are not allowed except decimal")
              } else {
                setAmount(newText)
                setIsValidationErrorTextInput(false)
                setIsValidationError(false)
              }
            } }
          />
        </Container>

        <Container style={{marginTop:-10}}>
           {/* for placeholder text */}
          <Typography variant="chip" style={{marginTop:0}} fontFamily="HelveticaNeue-Medium" color="grayLight">Please enter amount between 10 to 5000 AED</Typography>

          {isValidationErrorTextInput ? <Typography style={{marginTop:10}} fontFamily="HelveticaNeue-Medium" variant="chip" color="error">{errorMessageTextInput}</Typography> : undefined}
          {isValidationError ? <Typography style={{marginTop:10}} fontFamily="HelveticaNeue-Medium" variant="chip" color="error">{errorMessage}</Typography> : undefined}
        </Container>


        {/* Container for TextField : IBAN */}
        <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          justify="flex-start"
          style={[styles.box, styles.shadowProp]}
        >
          <Container
            alignItems="center"
            noItemsStretch
            justify="center"
            style={{
              backgroundColor: "#F9F8F8",
              height: 48,
              width: 80,
              borderBottomLeftRadius: 15,
              borderTopLeftRadius: 15,
              borderColor: theme.colors.transparent,
              borderWidth: 1
            }}
          >
            <Typography variant="label" textAlign="center">
              {countryCode}
            </Typography>
          </Container>

          <TextInput
            style={styles.textFieldStyle}
            placeholder={bankAccountNumber}
            editable={false}
          />
        </Container>

        {/* Container for Checkbox, IBAN belongs to user check + disclaimer text  */}
        <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          justify="center"
          style={{ marginTop: 30, marginRight: 10 }}
        >
          <Checkbox
            size="xs"
            label={t("")}
            name="isDisclaimerChecked"
            control={control}
          />
          <Typography variant="chip" fontFamily="HelveticaNeue-Medium" style={styles.certifyTextStyle} color={checkboxSelected ? "#171717" : "error"}>
            {t("I certify the above IBAN number belongs to me")}
          </Typography>
        </Container>

        {/* Container for Disclaimer Text & its Tooltip */}
        <Container justify="flex-start">
          <Typography variant="label" style={styles.disclaimerTextStyle} >
          {screenType == "deposit" ? t("* The Deposit will be simulated via WadzPay \nbackend and is not processed by the Bank Account") : t("* The Withdrawal will be simulated via WadzPay\nbackend and is not processed by the Bank Account")}
          </Typography>
        </Container>


      {/* Container for Deposit / Withdraw Button */}
      <Container alignItems="center" justify="center" noItemsStretch spacing={1}>
          <Typography fontFamily="HelveticaNeue-Light" variant="chip" color="darkBlack" spacing={20}>
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
            text= {screenType == "deposit" ? t("Deposit") : t("Withdraw")}
            fontWeight="bold"
            fontFamily={"Helvetica-Bold"}
            //disabled = {submitButtonPressed ? true : false}
            onPress={() => {
              onSubmit()
            }}
          />
      </Container>

    </Container>
}
    </ScreenLayoutTab>
  )
}

const styles = StyleSheet.create({
  mainContainer: {
    alignItems: "center",
    justify: "center",
    backgroundColor: "white",
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.md,
    marginVertical: theme.spacing.md,
  },
  ibanContainer: {
    backgroundColor:"white",
    paddingVertical: theme.spacing.md,
  },
  label: {
    position: "absolute",
    backgroundColor: "white",
    left: 22,
    top: 7,
    zIndex: 999,
    paddingHorizontal: 8,
    fontSize: 14
  },
  accountNumberTextField: {
    backgroundColor: "#000000"
  },
  textFieldStyle: {
    height: 35,
    fontSize: 14,
    paddingHorizontal: theme.spacing.xs,
    textAlign: "left",
    paddingRight: 100
  },
  certifyTextStyle: {
    fontSize: 10,
    marginTop: 1
  },
  disclaimerTextStyle: {
    height: 25,
    fontSize: 10,
    paddingHorizontal: 1,//theme.spacing.xs,
    width: 300,
    marginTop: 0,
    marginLeft: 15,
    fontFamily: "Montserrat-Light",
    color: "#171717"
  },
  box: {
    width: 300,
    height: 50,
    borderRadius: 15,
    borderWidth: 0.3,
    borderColor: theme.colors.gray.light,
    backgroundColor: "white"
  },
  checkboxContainer: {
    width: 10,
    height: 10,
    marginTop: 30
  },
  shadowProp: {
    borderRadius: 15,
    borderWidth: 0.3,
    borderColor: theme.colors.gray.light,
    // shadowColor: theme.colors.gray.dark,
    shadowOffset: {width: -2, height: 4},
    shadowOpacity: 0.2,
    shadowRadius: 1,
    elevation: 20,
  }
})

export default DepositAndWithdrawScreen

import React, { useContext, useEffect, useState } from "react"
import { CommonActions } from "@react-navigation/native"
import { useTranslation } from "react-i18next"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import Clipboard from '@react-native-clipboard/clipboard';

import { OnboardingNavigationProps } from "~/components/navigators/OnboardingStackNavigator"
import {
  Container,
  ScreenLayoutOnboarding,
  SelectField,
  TextField,
  DateField,
  Typography,
  theme,
  Modal,
  ErrorModal
} from "~/components/ui"
import { fieldProps } from "~/constants/fieldProps"
import { PersonalDetailsForm, useValidationSchemas } from "~/constants"
import { OnboardingContext, UserContext } from "~/context"
import { IbanNavigationProps } from "~/components/navigators/IbanStackNavigator"
import { StyleSheet, TextInput } from "react-native"
import { useAddUserBankAccount } from "~/api/user"
import { Image } from "react-native-elements"

type IbanNavigationProps = {
  hideSkip : boolean
}

const IbanVerificationScreen: React.FC<IbanNavigationProps> = ({
  hideSkip
}: IbanNavigationProps) => {

    const { t } = useTranslation()
    const [ibanNumber, setIbanNumber] = useState("")
    const [isValidationErrorTextInput, setIsValidationErrorTextInput] = useState(false)
    const [confirmIbanNumber, setConfirmIbanNumber] = useState("")
    const [amount, setAmount] = useState("")
    const [isValidationError, setIsValidationError] = useState(false)
    const [errorMessage, setErrorMessage] = useState("")
    const [showSuccessModal, setshowSuccessModal] = useState(false)
    
    const { setBankAccNumber, setConCode, setSkipIban } = useContext(UserContext)
    const {
      mutate: addUserBankAccount,
      isLoading: isaddUserBankAccountLoading,
      error: addUserBankAccountError,
      isSuccess: isaddUserBankAccountSuccess,
      data: addUserBankAccountData
    } = useAddUserBankAccount("AE")

  const onNext = () => {
    const ibanSplit = ibanNumber.split("")
    if(ibanSplit.length === 0) {
      setIsValidationError(true)
      setErrorMessage("Enter IBAN Number")
      return
    } 
    else if(ibanSplit[2] !== "2" || ibanSplit[3] !== "3") {
      setIsValidationError(true)
      setErrorMessage("Invalid IBAN Number, Please enter CBD's Iban Number")
      return
    } 
    else if(ibanNumber !== confirmIbanNumber) { 
      setIsValidationError(true)
      setErrorMessage("IBAN Number Mismatch")
      return
    } 
    else {
      addUserBankAccount({
        bankAccountNumber:  ibanNumber,
        confirmBankAccountNumber: confirmIbanNumber
    })
 }
   
}



// useEffect(() => {
//   console.log("add acc sucess respo buy addUserBankAccountError ", addUserBankAccountError)
//   if (addUserBankAccountData) {
    
//   }
 
// }, [addUserBankAccountError])

useEffect(() => {
  if (addUserBankAccountData) {
    setshowSuccessModal(true)
    let response = addUserBankAccountData
   const interval = setTimeout(() => {
  setshowSuccessModal(false)
  setBankAccNumber(addUserBankAccountData.bankAccountNumber)
  setConCode(addUserBankAccountData.countryCode)
  }, 500);
  return () => clearInterval(interval);
  }
 
}, [isaddUserBankAccountSuccess])

  const onSkip = async () => {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    setSkipIban(true)
    return true
  }

  return (
    <ScreenLayoutOnboarding
      title={t("")}
      hasHeader={!hideSkip} // utilized this condition
      onSkip={onSkip}
      rightComponent={hideSkip ? undefined :<Typography  color="orange">{t("Add Later")}</Typography>}
      content={
        <Container spacing={3} style={{top:-29}}>
        <Typography variant="title" textAlign="left" fontFamily="Helvetica-Bold">Your IBAN</Typography>
        <Typography
          variant="body"
          textAlign="left"
          fontFamily="HelveticaNeue-Light"
          >{t("As an additional layer of Security, we require customers to share their IBAN before they start trading, this will be the account you can deposit/withdraw funds to. \n\n\nYou can change this at any time by contacting us")}</Typography>
           <Typography variant="subtitle" textAlign="left" fontFamily="Helvetica-Bold">Your UAE IBAN</Typography>
           <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          justify="flex-start"
          style={styles.box}
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
            <Typography variant="body" textAlign="center">
            AE
            </Typography>
          </Container>

          <TextInput
              value={ibanNumber}
              maxLength={21}
              placeholder="IBAN Number"
              keyboardType="numeric"
              onChangeText={(newText) => {
                if (newText.includes(",") || newText.includes(" ") || newText.includes(".")) {
                  setIsValidationErrorTextInput(true)
                } else {
                  setIbanNumber(newText)
                  setIsValidationError(false)
                  setIsValidationErrorTextInput(false)
                }
              } }
              defaultValue={ibanNumber}
             style={[styles.textFieldStyle,{width: 290, fontFamily: "Montserrat-Light"}]}
            />
        </Container>
        <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          justify="flex-start"
          style={styles.box}
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
            <Typography variant="body" textAlign="center">
            AE
            </Typography>
          </Container>

          <TextInput
              value={confirmIbanNumber}
              maxLength={21}
              placeholder="Confirm IBAN number"
              keyboardType="numeric"
              onFocus={() => Clipboard.setString('')} onSelectionChange={() => Clipboard.setString('')}
              onChangeText={(newText) => {
                if (newText.includes(",") || newText.includes(" ") || newText.includes(".")) {
                  setIsValidationErrorTextInput(true)
                } else {
                  setConfirmIbanNumber(newText)
                  setIsValidationError(false)
                  setIsValidationErrorTextInput(false)
                }
              } }
              defaultValue={confirmIbanNumber}
             style={[styles.textFieldStyle,{width: 290, fontFamily: "Montserrat-Light"}]}
            />
        </Container>
        {isValidationError ? <Typography fontFamily="HelveticaNeue-Medium" variant="chip" color="error">{errorMessage}</Typography> : undefined}
        {isValidationErrorTextInput ? <Typography fontFamily="HelveticaNeue-Medium" variant="chip" color="error">Special Characters are not Allowed</Typography> : undefined}

        <ErrorModal error={addUserBankAccountError} />
{/* {showSuccessModal && 
      <Modal
            variant="center"
            isVisible={true}
            dismissButtonVariant="none"
            contentStyle={{ height: theme.modalMidScreenHeight }}
          >
              <Container
                alignItems="center"
                justify="center"
                noItemsStretch
              >
                <Image
                  source={require("~images/successful_menu.png")}
                  style={{width: 150, height: 170 }}
                />
                <Typography
                  variant="subtitle"
                  fontFamily="Helvetica-Bold"
                >
                  {t("IBAN Added Successfully")}
                </Typography>

              </Container>
          </Modal>
} */}
        </Container>
      }
      nextButtonText={"Add"}
      onNext={onNext}
    />
  )
}

const styles = StyleSheet.create({
    box: {
        width: 300,
        height: 50,
        borderRadius: 15,
        borderWidth: 1,
        borderColor: theme.colors.gray.medium,
        backgroundColor: "white"
      },
      textFieldStyle: {
        height: 35,
        fontSize: 14,
        paddingHorizontal: theme.spacing.xs,
        textAlign: "left",
        paddingRight: 100
      },
})
export default IbanVerificationScreen

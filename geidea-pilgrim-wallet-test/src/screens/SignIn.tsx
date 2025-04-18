import React, { useContext, useEffect, useState } from "react"
import {
  StyleSheet,
  Image,
  TouchableOpacity,
  TextInput,
  Alert,ScrollView
} from "react-native"
import { useTranslation } from "react-i18next"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"

import { RootNavigationProps } from "~/components/navigators"
import {
  Button,
  ScreenLayoutBottomActions,
  TextField,
  Typography,
  Container,
  ErrorModal,
  theme,
  Modal,
  UnderDevelopmentMessage,
  Checkbox
} from "~/components/ui"
import { fieldProps, useValidationSchemas, SignInForm } from "~/constants"
import { useAddExpoToken, useSignIn, useUser, useUserKYC } from "~/api/user"
import env from "~/env"
import { is } from "date-fns/locale"
import { NotificationsContext, UserContext } from "~/context"
//import * as Notifications from "expo-notifications"
import { isIOS, showToast } from "~/utils"
import NetInfo from "@react-native-community/netinfo"

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center"
  },
  imagelogo: {
    marginTop: 0
  },
  forgotpassword: {
    color: theme.colors.iconFocused,
    marginBottom: 0
  }
})

const SignIn: React.FC<RootNavigationProps> = ({
  navigation
}: RootNavigationProps) => {
  const { t } = useTranslation()
  const { signInSchema } = useValidationSchemas()

  const [hitAPI, sethitAPI] = useState(false)
  const [hitKycApi, setHitKycApi] = useState(false)
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [isAlertVisible, setIsAlertVisible] = useState(false)
  const [isErrorVisible, setIsErrorVisible] = useState(false)
  const [isConnected, setIsConnected] = useState(false)
  
  var validRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;

  const {
    control,
    handleSubmit,
    formState: { errors },
    getValues
  } = useForm<SignInForm>({
    resolver: yupResolver(signInSchema),
    defaultValues: {
      email: env.DEFAULT_USER_EMAIL,
      password: env.DEFAULT_USER_PASSWORD
    }
  })
  const {
    user,
    reLoadSignUpPage,
    isLoadingSignupPage,
    isUserKycApproved,
    setUserKycApproved,
    setBankAccNumber,
    setConCode,
    setSkipIban,
    isLoading: isUserLoading
  } = useContext(UserContext)

  //const { expoPushToken, isNotificationEnabled, setExpoPushToken } = useContext(NotificationsContext)
  const { mutate: signIn, isLoading, error, isSuccess } = useSignIn()
  const { mutate: addExpoToken } = useAddExpoToken()
  const {
    data: response,
    isSuccess: userIsSuccess,
    isFetching: userIsFeteching,
    error: userExistError,
    isFetched
  } = useUser(`email=${user?.attributes?.email}`, hitAPI)

  const {
    data: userKyc,
    isSuccess: kycIsSuccess,
    isFetching: kycIsFetching,
    isFetched: kycFetched,
    error: kycError
  } = useUserKYC(hitKycApi)

  useEffect(() => {
    NetInfo.configure({
      reachabilityUrl: 'https://clients3.google.com/generate_204',
      reachabilityTest: async (response) => response.status === 204,
      reachabilityLongTimeout: 60 * 1000, // 60s
      reachabilityShortTimeout: 5 * 1000, // 5s
      reachabilityRequestTimeout: 15 * 1000, // 15s
      reachabilityShouldRun: () => true,
      shouldFetchWiFiSSID: true, // met iOS requirements to get SSID. Will leak memory if set to true without meeting requirements.
      useNativeReachability: false
    });

    const unsubscribe = NetInfo.addEventListener(state => {
      //console.log("Connection type", state.details);
      // alert(t(`NetInfo ${JSON.stringify(state.details)} ${state.isConnected} **`))
     // console.log("Is connected?", state.isConnected);
     setIsConnected(state.isConnected)
    });

    return () => {
      unsubscribe()
    }

  }, [])

  const onLogin = () => {
   let { email, password } = getValues()
    email = email.toLocaleLowerCase()
    reLoadSignUpPage()

   {isConnected ? signIn({ email, password }) :  showToast(t("No Internet connectivity")) }
  }


  useEffect(() => {
    if (userIsSuccess) {
      
      if(response?.userBankAccount.length > 0 ){

        // for CBD
        setBankAccNumber(response?.userBankAccount[0].bankAccountNumber)
        setConCode(response?.userBankAccount[0].countryCode)

        // apple-swati
        // For Pilgrim Wallet
        user.userBankAccount = response.userBankAccount
        
      } else {
        setBankAccNumber("")
        setConCode("")
      }
      if (
        response?.kycVerified === "APPROVED_VERIFIED" ||
        response?.kycVerified === "DENIED_FRAUD" ||
        response?.kycVerified === "IN_PROGRESS"
      ) {
        setHitKycApi(true)
      } else {
        setIsErrorVisible(false)
        setIsModalVisible(true) // this shows the disclaimer pop-up
        user ? setHitKycApi(true) : setHitKycApi(false) // this will call jumio verification url 
      }
    }
  }, [userIsSuccess])

  // const registerForPushNotifications = async () => {
  //   if (expoPushToken === null || Constants.isDevice) {
  //     try {
  //       const { status: existingStatus } =
  //         await Notifications.getPermissionsAsync()
  //       let finalStatus = existingStatus
  //       if (existingStatus !== "granted") {
  //         const { status } = await Notifications.requestPermissionsAsync()
  //         //  console.log("status" , status)
  //         finalStatus = status
  //       }
  //       if (finalStatus !== "granted") {
  //         alert(t("Please enable push notifications from the phone settings"))
  //         return
  //       }

  //       // Set token to context and post it to the BE
  //       const token = (await Notifications.getExpoPushTokenAsync()).data
  //       await addExpoToken({ expoToken: token })
  //       setExpoPushToken(token)

  //       Notifications.setNotificationHandler({
  //         handleNotification: async () => ({
  //           shouldShowAlert: true,
  //           shouldPlaySound: false,
  //           shouldSetBadge: true
  //         })
  //       })
  //       // console.log("herehehehehhejdcsdkcbjsdjcsdc")
  //      // alert("here" , token)
  //     } catch (e) {
  //      // alert(t("There was an error setting up push notifications.") e)
  //     } finally {
  //     }
  //   } else {
  //    alert(t("Must use a physical device for Push Notifications."))
  //   }

  //   if (!isIOS) {
  //     Notifications.setNotificationChannelAsync("default", {
  //       name: "default",
  //       importance: Notifications.AndroidImportance.MAX,
  //       vibrationPattern: [0, 250, 250, 250],
  //       lightColor: "#FF231F7C"
  //     })
  //   }
  // }

  useEffect(() => {
    if (isSuccess) {
      user ? sethitAPI(true) : sethitAPI(false) // this will check if user's kyc is done or not after succesful login
      //registerForPushNotifications()
    }
  }, [isSuccess, user])

  const showKycRedirectionAlertbox = (redirectUrl: string , title: string, successUrl:string) => { 
     redirectUrl &&  Alert.alert("", title, [
      {
        text: t("Ok"),
        onPress: () =>
          navigation.navigate("kycJumioWebviewScreen", {
            redirectUrl: redirectUrl,
            successUrl: successUrl
          })
      },
      
            {
              text: t("Cancel")
            }
          
    ])
  }
  useEffect(() => {
    // this block will handle kyc process on the basis of kyc status 
    if (response) {
      sethitAPI(false)
      if (response?.kycVerified === "APPROVED_VERIFIED") {
        setUserKycApproved(true) // user's kyc is approved
      } else if (response?.kycVerified === "DENIED_FRAUD") {
        Alert.alert("", t("Fradulent Document Detected. Login Denied"), [
          { text: t("Ok") }
        ])
      } else if (response?.kycVerified === "DENIED_UNSUPPORTED_ID_TYPE") {
        showKycRedirectionAlertbox(userKyc?.redirectUrl ,  t("Document not acceptable"), userKyc?.successUrl)
        // isAlertVisible && userKyc?.redirectUrl &&  Alert.alert("", t("Document not acceptable"), [
        //   {
        //     text: t("Ok"),
        //     onPress: () =>
        //       navigation.navigate("kycJumioWebviewScreen", {
        //         redirectUrl: userKyc?.redirectUrl,
        //         successUrl: userKyc?.successUrl
        //       })
        //   }
        // ])
      } else if (response?.kycVerified === "DENIED_UNSUPPORTED_ID_COUNTRY") {
        showKycRedirectionAlertbox(userKyc?.redirectUrl , t(
          "Unsupported Country ID uploaded.\nPlease try with supported Country ID "
        ), userKyc?.successUrl)
        // isAlertVisible &&  userKyc?.redirectUrl &&  Alert.alert(
        //   "",
        //   t(
        //     "Unsupported Country ID uploaded.\nPlease try with supported Country ID "
        //   ),
        //   [
        //     {
        //       text: t("Ok"),
        //       onPress: () =>
        //         navigation.navigate("kycJumioWebviewScreen", {
        //           redirectUrl: userKyc?.redirectUrl,
        //           successUrl: userKyc?.successUrl
        //         })
        //     }
        //   ]
        // )
      } else if (response?.kycVerified === "ERROR_NOT_READABLE_ID") {
        showKycRedirectionAlertbox(userKyc?.redirectUrl , t(
          "Document not readable. Please try again"
        ), userKyc?.successUrl)
       
      } else if (response?.kycVerified === "UNKNOWN") {

        // Swati : for bypassing KYC
        showKycRedirectionAlertbox(userKyc?.redirectUrl , t("Please upload ID Proof for KYC\nPress OK to continue"), userKyc?.successUrl)
       // setUserKycApproved(true)
      } else if (response?.kycVerified === "IN_PROGRESS") {
        Alert.alert(
          "",
          t("KYC is in progress\nPlease try login after sometime"),
          [{ text: t("Ok") }]
        )
      } else if (response?.kycVerified === "NO_ID_UPLOADED") {
        showKycRedirectionAlertbox(userKyc?.redirectUrl , t("Please upload ID Proof for KYC\nPress OK to continue"), userKyc?.successUrl)
       // setUserKycApproved(true)
      }
    }
  }, [  userIsSuccess])

  // const onDismiss = () => {
  //   setIsErrorVisible(false)
  //   setIsModalVisible(false)
  // }
  // const onKycAlertVisible = () => {
  //  // console.log("control " , control._formValues.isDisclaimerChecked)
  //  if(control._formValues.isDisclaimerChecked){
  //   setIsModalVisible(false)
  //  // setIsAlertVisible(true)
  //  } else {
  //   setIsErrorVisible(true)
  //  }
   
  //   console.log("isAlertVisible after " , isAlertVisible)
  // }

  const  handleInput = (input: string) => {

    console.log(control._formValues)
    if(!validRegex.test(control._formValues.email)) {
     // {errors.email?.message : "hey !" || ""}
    }

  }
 
  return (
    <ScreenLayoutBottomActions
      dismissKeyboard
      content={
        <ScrollView automaticallyAdjustKeyboardInsets={false}
        contentContainerStyle={{
          flex: 1
        }}
      >
        <Container spacing={5} style={styles.container}>
          <Container alignItems="center" noItemsStretch>
            <Image
              style={styles.imagelogo}
              source={require("~images/WadzPay-Logo.png")}
            />
          </Container>
          <Container
            spacing={1}
            noItemsStretch
            alignItems="center"
            justify="center"
          >
            <TextField
              label={t("Email Address")}
              name="email"
              control={control}
              error={errors.email}

              // iconName="User"
              placeholder={"Email Address"}
              {...fieldProps.username}
             
            />
            {/* <TextInput
              placeholder={t("Password")}
             /> */}

            <TextField
              label={t("Password")}
              name="password"
              control={control}
              error={errors.password}
              
              // iconName="Lock"
              placeholder={t("Password")}
              maxLength={16}
              isPassword
              {...fieldProps.password}
            />
          </Container>
          {/* {userExistError ? (
            <ErrorModal error={userExistError} />
          ) : (
            <ErrorModal error={error} />
          )}*/}
          <ErrorModal error={error} />
        </Container>
        </ScrollView>
      }
      actions={
        <Container spacing={2}>
          <TouchableOpacity
            onPress={() => navigation.navigate("ResetPassword")}
           // style={{ height: 40, marginTop:0 }}
          >
            <Typography style={styles.forgotpassword}>
              {t("Forgot password?")}
            </Typography>
          </TouchableOpacity>
          <Button
            variant="primary"
            onPress={handleSubmit(onLogin)}
            text={t("Login")}
            loading={isLoading}
          />
          <Button
            variant="secondary"
            onPress={() => {
              navigation.navigate("CreateAccount")
            }}
            text={t("Create Account")}
          />
        </Container>
      }
      // disclaimer={
      //   <Modal
      //   variant="bottom"
      //   isVisible={isModalVisible}
      //   onDismiss={onDismiss}
      //   dismissButtonVariant="cancel"
      //   contentStyle={{ height: theme.modalFullScreenHeight }}
      // >
      //   {/* TODO remove UnderDevelopmentMessage */}
      //     <Container spacing={4}>
      //       <Typography variant="title">{t("Disclaimer")}</Typography>
      //       <Container spacing={2} alignItems="center" noItemsStretch>
      //       <Image source={require("~images/Welcome/welcome.png")} style={{ width:200,height:150}}/>
      //       <Container direction="row" alignItems="center" noItemsStretch justify="center" style={{marginTop:50 , marginRight:30}}>
      //       <Checkbox
      //         label={t("")}
      //         name="isDisclaimerChecked"
      //         control={control}
      //       />
      //         <Typography variant="body">{t("Please Accept Terms & Conditions")}</Typography>
      //       </Container>
      //       <Typography style={{marginBottom:30}} variant="label">{t("(According to CBD Disclaimer)")}</Typography>
            
      //         <Button
      //       variant="primary"
      //       onPress={onKycAlertVisible}
      //       text={t("Agree & Proceed")}
      //       loading={isLoading}
      //     />
      //     {isErrorVisible && <Typography variant="chip" color="error">{t("Please click on Checkbox")}</Typography>}
      //       </Container>
      //     </Container>
      // </Modal>
      // }
    />
  )
}

export default SignIn

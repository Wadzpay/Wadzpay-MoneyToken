import React, { useContext, useEffect, useRef, useState } from "react"
import { Alert, BackHandler, Image, StyleSheet, Text, View , PermissionsAndroid} from "react-native"
import { WebView } from "react-native-webview"
import { RootNavigationProps } from "~/components/navigators"
import {
  Button,
  Container,
  LoadingSpinner,
  Screen,
  Typography
} from "~/components/ui"
import { CommonActions } from "@react-navigation/native"
import { UserContext } from "~/context"
import { Colors } from "react-native/Libraries/NewAppScreen"
import { useUser, useUserKYC } from "~/api/user"
import { useTranslation } from "react-i18next"

const styles = StyleSheet.create({
  container: { flex: 1 }
})

const kycJumioWebviewScreen: React.FC<RootNavigationProps> = ({
  route,
  navigation
}: RootNavigationProps) => {
  const { t } = useTranslation()
  const [hasPermission, setHasPermission] = useState(false)
  const webViewRef = useRef(null)
  const widgetURL = route?.params?.redirectUrl || ""
  const successUrl = route?.params?.successUrl || ""
  //const [widgetURL, setwidgetURL] = useState("")
  const [showSuccessPage, setShowSuccessPage] = useState(false)
  //const [stopAPI, setStopAPI] = useState(false)

  const [intervalMs, setIntervalMs] = useState(4000)

  const { user, reLoadSignUpPage, isLoadingSignupPage, setUserKycApproved } =
    useContext(UserContext)
  //const [emailId, setEmailId] = useState(user?.attributes.email)
  const [hitAPI, sethitAPI] = useState(false)
  const [hitKycApi, setHitKycApi] = useState(false)
  const {
    data: response,
    isSuccess: userIsSuccess,
    isFetching: userIsFeteching,
    isFetched
  } = useUser(`email=${user?.attributes.email}`, hitAPI)

  //   useEffect(() => {
  //   if(kycIsSuccess  ) {
  //     setwidgetURL(userKyc?.redirectUrl)
  //     // setHitKycApi(false)
  //   }
  // }, [kycIsSuccess]);

  useEffect(() => {
    ;(async () => {

      const {granted} = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.CAMERA
      )
  
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        setHasPermission(true)
      } else {
        setHasPermission(true)
      }
    })()
  }, [])
  useEffect(() => {
    const backAction = () => {
      navigation.goBack()
      return true
    }

    const backHandler = BackHandler.addEventListener(
      "hardwareBackPress",
      backAction
    )

    return () => backHandler.remove()
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => {

      if (showSuccessPage) {
        //sethitAPI(true)
        navigation.goBack()
      }
    }, 2000)
    return () => clearTimeout(timer)
  }, [showSuccessPage])

  useEffect(() => {
   
    if (showSuccessPage && userIsSuccess) {
      //setStopAPI(true)
      // Alert.alert(t("kyc kyc screen"), "", [
      //   { text: t("Ok"), onPress: () =>  navigation.goBack() }
      // ])
      if (response) {
        if (response?.kycVerified === "APPROVED_VERIFIED") {
          Alert.alert(t("your KYC is approved "), "", [
            { text: t("Ok"), onPress: () => setUserKycApproved(true) }
          ])
        } else if (response?.kycVerified === "DENIED_FRAUD") {
          reLoadSignUpPage()
          Alert.alert("", t("Fradulent Document Detected. Login Denied"), [
            {
              text: t("Ok"),
              onPress: () =>
                navigation.dispatch(
                  CommonActions.reset({
                    index: 0,
                    routes: [{ name: "Welcome" }]
                  })
                )
            }
          ])
        } else if (response?.kycVerified === "DENIED_UNSUPPORTED_ID_TYPE") {
          reLoadSignUpPage()
          Alert.alert("", t("Document not acceptable"), [
            {
              text: t("Ok"),
              onPress: () =>
                navigation.dispatch(
                  CommonActions.reset({
                    index: 0,
                    routes: [{ name: "SignIn" }]
                  })
                )
            }
          ])
        } else if (response?.kycVerified === "DENIED_UNSUPPORTED_ID_COUNTRY") {
          reLoadSignUpPage()
          Alert.alert(
            "",
            t(
              "Unsupported Country ID uploaded.\nPlease try with supported Country ID "
            ),
            [
              {
                text: t("Ok"),
                onPress: () =>
                  navigation.dispatch(
                    CommonActions.reset({
                      index: 0,
                      routes: [{ name: "SignIn" }]
                    })
                  )
              }
            ]
          )
        } else if (response?.kycVerified === "ERROR_NOT_READABLE_ID") {
          reLoadSignUpPage()
          Alert.alert("", t("Document not readable. Please try again"), [
            {
              text: t("Ok"),
              onPress: () =>
                navigation.dispatch(
                  CommonActions.reset({
                    index: 0,
                    routes: [{ name: "SignIn" }]
                  })
                )
            }
          ])
        } else if (response?.kycVerified === "UNKNOWN") {
          reLoadSignUpPage()
          Alert.alert(
            "",
            t("Please upload ID Proof for KYC\nPress OK to continue"),
            [
              {
                text: t("Ok"),
                onPress: () =>
                  navigation.dispatch(
                    CommonActions.reset({
                      index: 0,
                      routes: [{ name: "SignIn" }]
                    })
                  )
              },
              {
                text: t("Cancel")
              }
            ]
          )
        } else if (response?.kycVerified === "IN_PROGRESS") {
          Alert.alert(
            "",
            t("KYC is in progress\nPlease try login after sometime"),
            [
              {
                text: t("Ok"),
                onPress: () => {
                  reLoadSignUpPage()
                  navigation.dispatch(
                    CommonActions.reset({
                      index: 0,
                      routes: [{ name: "SignIn" }]
                    })
                  )
                }
              }
            ]
          )
        } else if (response?.kycVerified === "NO_ID_UPLOADED") {
          Alert.alert(
            "",
            t("Please upload ID Proof for KYC\nPress OK to continue"),
            [
              {
                text: t("Ok"),
                onPress: () =>
                  navigation.dispatch(
                    CommonActions.reset({
                      index: 0,
                      routes: [{ name: "SignIn" }]
                    })
                  )
              },
              {
                text: t("Cancel")
              }
            ]
          )
        } else {
          Alert.alert(
            t("your kyc is in progress please try login after sometime"),
            "",
            [
              {
                text: t("Ok"),
                onPress: () => {
                  reLoadSignUpPage()
                  navigation.dispatch(
                    CommonActions.reset({
                      index: 0,
                      routes: [{ name: "SignIn" }]
                    })
                  )
                }
              }
            ]
          )
        }
      }
    }
  }, [userIsSuccess, response])

  if (hasPermission === null) {
    return <View />
  }
  if (hasPermission === false) {
    return <Text>No access to camera</Text>
  }
  return (
    <Screen>
      {showSuccessPage && (
        <Container
          alignItems="center"
          justify="center"
          spacing={1}
          noItemsStretch
          style={{ flex: 1, paddingLeft: 10, paddingRight: 10 }}
        >
          <Typography variant="subtitle">
            KYC is done. Please reload the app to login again
          </Typography>
          <LoadingSpinner color="orange" />
        </Container>
      )}
      {widgetURL && widgetURL.length > 0 && !showSuccessPage ? (
        <WebView
          source={{
            uri: widgetURL
          }}
          ref={webViewRef}
          style={styles.container}
          originWhitelist={["*"]}
          allowsInlineMediaPlayback
          javaScriptEnabled
          scalesPageToFit
          mediaPlaybackRequiresUserAction={false}
          javaScriptEnabledAndroid
          useWebkit
          startInLoadingState={true}
          renderLoading={() => (
            <Container
              alignItems="center"
              justify="center"
              spacing={1}
              noItemsStretch
              style={{
                flex: 1,
                position: "absolute",
                height: "100%",
                width: "100%"
              }}
            >
              <LoadingSpinner color="orange" />
            </Container>
          )}
          onLoadProgress={({ nativeEvent }) => {
            //your code goes here

            // if(nativeEvent?.url === "https://wadzpay-test.web.apac-1.jumio.ai/web/v4/app/start") {
            //   setShowSpinner(false)
            // }
            if (nativeEvent?.url.includes(successUrl)) {
              setShowSuccessPage(true)

              //setIntervalMs(10000)
              // reLoadSignUpPage()
            }
          }}
        />
      ) : null}
    </Screen>
  )
}

export default kycJumioWebviewScreen

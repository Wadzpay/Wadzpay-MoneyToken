import React, { useContext, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { Alert, Linking, View } from "react-native"
import { NativeStackScreenProps } from "@react-navigation/native-stack"


import MenuSection, { MenuItemType } from "./MenuSection"
import Deposit from "./Deposit"
import Withdraw from "./Withdraw"

import { Container, ScreenLayoutTab, Typography } from "~/components/ui"
import { MenuStackParamList } from "~/components/navigators"
import { UserContext } from "~/context"
import { BlockedCountries } from "~/constants/blockedNumber"
import { useGetUserProfileDetails, useSignOut } from "~/api/user"
import { useSendPhoneOtpCommon } from "~/api/otp"
import { formatPhoneNumber, isDev, isGeideaDev, isGeideaTest, isGeideaUat, isTesting } from "~/utils"
import { usePublicApiUrl } from "~/api"

type Props = NativeStackScreenProps<MenuStackParamList, "Menu">

const Menu: React.FC<Props> = ({ navigation, route }: Props) => {
  const modalTypeOpen = route?.params?.modalTypeOpen
  const { t } = useTranslation()
  const [isDepositOpen, setIsDepositOpen] = useState(false)
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false)
  const [passcodeText, setPasscodeText ] = useState("")
  const { user } = useContext(UserContext)
  const { mutate: signOut } = useSignOut()
  const userMobilenumber = user?.attributes?.phone_number

  const {
    data: profileData,
    isFetching: isFetchingProfileDetails,
    error: errorProfileDetails
  } = useGetUserProfileDetails()
  const PUBLIC_API_URL = usePublicApiUrl()
  useEffect(() => {
    // Check if passcode is set for the user or not 
    // console.log("profileData", profileData)
     if(profileData?.isPasscodeSet){
       setPasscodeText("Change/Forgot Passcode")
     } else {
       setPasscodeText("Set Passcode")
     }
   }, [profileData])
   const {
    mutate: sendPhoneOTP,
    isLoading,
    error,
    isSuccess
  } = useSendPhoneOtpCommon()

  useEffect(() => {
    if (modalTypeOpen === "deposit") {
      setIsDepositOpen(false)
    } else if (modalTypeOpen === "withdraw") {
      setIsWithdrawOpen(false)
    }
    // Has to be route, because the string "modalTypeOpen" does not register a change
  }, [route])

  useEffect(() => {
    if (isSuccess) {

      navigation.navigate("VerifyPhoneOtp", {
        screenType: "phone",
        target: userMobilenumber?.toString() || ""
      })
    }
  }, [isSuccess])

  const onNext = () => {
 //navigation.navigate("PasscodeScreen")
      sendPhoneOTP({
        phoneNumber: formatPhoneNumber(userMobilenumber?.toString() || "")
      })
    }

  return (
    <ScreenLayoutTab title={'Settings'}>
      {user?.attributes?.phone_number?.startsWith(
        BlockedCountries.SINGAPORE,
      ) === false ? (
        <>
          {/* <Deposit
            isVisible={isDepositOpen}
            onDismiss={() => setIsDepositOpen(false)}
          />
          <Withdraw
            isVisible={isWithdrawOpen}
            onDismiss={() => setIsWithdrawOpen(false)}
          />
          <MenuSection
            title={t("")}
            items={[
              {
                // added by swati
                title: t("Deposit"),
                onPress: () => navigation.navigate("DepositAndWithdrawScreen" , {screenType: "deposit", asset:""}),
                iconName: "Deposit"
              },
              {
                title: t("Buy"),
                onPress: () => navigation.navigate("BuyScreen"),
                iconName: "Buy"
              },
              {
                title: t("Sell"),
                onPress: () => navigation.navigate("SellScreen"),
                iconName: "Sell"
              },
              {
                // added by swati
                title: t("Withdraw"),
                onPress: () => navigation.navigate("DepositAndWithdrawScreen" , {screenType: "withdraw", asset:""}),
                iconName: "Withdraw"
              },
            ]}
          /> */}
          <MenuSection
            title={t('')}
            items={[
              {
                title: t('User Details'),
                onPress: () => navigation.navigate('User'),
                iconName: 'User',
              },
              {
                title: t('Contacts'),
                onPress: () => navigation.navigate('Contacts'),
                iconName: 'Contact',
              },
              {
                title: passcodeText,
                onPress: () => onNext(),
                iconName: 'Passcode',
              },
              {
                title: t('Show My QR'),
                onPress: () => navigation.navigate('SendViaQRCode'),
                iconName: 'ShowMyQr',
              },
              {
                title: t('Logout'),
                onPress: () => signOut(),
                iconName: 'Exit',
              },
              // },
              // {
              //   title: t("Settings"),
              //   onPress: () => navigation.navigate("Settings"),
              //   iconName: "Settings"
              // }
            ]}
          />
          {/* <MenuSection
            title={t()}
            hideSeparator
            items={[
              // {
              //   title: t("About WadzPay"),
              //   onPress: () => Linking.openURL("https://wadzpay.com"),
              //   iconName: "About",
              //   type: MenuItemType.LINK
              // },
              // {
              //   title: t("Where to WadzPay?"),
              //   onPress: () =>
              //     Linking.openURL("https://wadzpay.com/where-to-wadzpay"),
              //   type: MenuItemType.LINK
              // },
              {
                title: t("FAQ"),
                onPress: () => {
                  Alert.alert(
                    "",
                    t(
                      "Accessing a link from WadzPay website will log you out from the app.\n\nDo you want to Continue ?"
                    ),
                    [
                      {
                        text: t("Yes"),
                        onPress: () => {
                          Linking.openURL(
                            "https://support.wadzpay.com/support/home"
                          )
                          signOut()
                        }
                      },
                      { text: t("No") }
                    ]
                  )
                },
                iconName: "Faq"
              },
              {
                title: t("Contact Support"),
                onPress: () => {
                  Alert.alert(
                    "",
                    t(
                      "Accessing a link from WadzPay website will log you out from the app.\n\nDo you want to Continue ?"
                    ),
                    [
                      {
                        text: t("Yes"),
                        onPress: () => {
                          Linking.openURL(
                            "https://support.wadzpay.com/support/tickets/new"
                          )
                          signOut()
                        }
                      },
                      { text: t("No") }
                    ]
                  )
                },
                iconName: "Support"
              }
            ]}
          /> */}
        </>
      ) : (
        <>
          {/* <Deposit
            isVisible={isDepositOpen}
            onDismiss={() => setIsDepositOpen(false)}
          />
          <Withdraw
            isVisible={isWithdrawOpen}
            onDismiss={() => setIsWithdrawOpen(false)}
          /> */}
          <MenuSection
            title={t('')}
            items={[
              {
                title: t('User Details'),
                onPress: () => navigation.navigate('User'),
                iconName: 'User',
              },
              {
                title: t('Contacts'),
                onPress: () => navigation.navigate('Contacts'),
                iconName: 'Contact',
              },
              // {
              //   title: t("Settings"),
              //   onPress: () => navigation.navigate("Settings"),
              //   iconName: "Settings"
              // }
            ]}
          />
          {/* <MenuSection
            title={t("")}
            hideSeparator
            items={[
              // {
              //   title: t("About WadzPay"),
              //   onPress: () => Linking.openURL("https://wadzpay.com"),
              //   iconName: "About",
              //   type: MenuItemType.LINK
              // },
              // {
              //   title: t("Where to WadzPay?"),
              //   onPress: () =>
              //     Linking.openURL("https://wadzpay.com/where-to-wadzpay"),
              //   type: MenuItemType.LINK
              // },
              {
                title: t("FAQ"),
                onPress: () => {
                  Alert.alert(
                    "",
                    t(
                      "Accessing a link from WadzPay website will log you out from the app.\n\nDo you want to Continue ?"
                    ),
                    [
                      {
                        text: t("Yes"),
                        onPress: () => {
                          Linking.openURL(
                            "https://support.wadzpay.com/support/home"
                          )
                          signOut()
                        }
                      },
                      { text: t("No") }
                    ]
                  )
                },
                iconName: "Faq"
              },
              {
                title: t("Contact Support"),
                onPress: () => {
                  Alert.alert(
                    "",
                    t(
                      "Accessing a link from WadzPay website will log you out from the app.\n\nDo you want to Continue ?"
                    ),
                    [
                      {
                        text: t("Yes"),
                        onPress: () => {
                          Linking.openURL(
                            "https://support.wadzpay.com/support/tickets/new"
                          )
                          signOut()
                        }
                      },
                      { text: t("No") }
                    ]
                  )
                },
                iconName: "Support"
              }
            ]}
          /> */}
        </>
      )}
       <View
        style={{
          marginTop: 280,
          marginBottom: 30,
        }}>
        <Container spacing={0} justify={'flex-end'} alignItems={'center'}>
          <Typography variant="chip">Build Version: 1.1.10</Typography>
          <Typography variant="chip">Build Server URL: {PUBLIC_API_URL}</Typography>
        </Container>
      </View>
    </ScreenLayoutTab>
  );
}

export default Menu

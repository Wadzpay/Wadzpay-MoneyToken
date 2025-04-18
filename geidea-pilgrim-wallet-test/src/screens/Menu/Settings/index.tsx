import React, { useContext, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  Alert,
  StyleSheet,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View
} from "react-native"
import PickerSelect from "react-native-picker-select"

import {
  Icon,
  ScreenLayoutTab,
  SelectFieldControlled,
  spacing
} from "~/components/ui"
import { RootNavigationProps } from "~/components/navigators"
import { Typography, theme, Container } from "~/components/ui"
import { NotificationsContext, UserContext } from "~/context"
import { DevelopmentSection } from "~/screens/DevelopmentSettings"
import { isDev, isIOS, isProd, isTesting, isUat } from "~/utils"
import { FiatAsset } from "~/constants/types"
//import * as Notifications from "expo-notifications"
import { useAddExpoToken } from "~/api/user"

const useFiatCurrencySelectItems: () => {
  label: string
  value: FiatAsset
}[] = () => {
  const { t } = useTranslation()
  return [
    { label: t("Euro (EUR)"), value: "EUR" },
    { label: t("Indian Rupee (INR)"), value: "INR" },
    { label: t("Indonesian Rupiah (IDR)"), value: "IDR" },
    { label: t("Pakistani Rupee (PKR)"), value: "PKR" },
    { label: t("Philippine Peso (PHP)"), value: "PHP" },
    { label: t("Pound Sterling (GBP)"), value: "GBP" },
    { label: t("Singapore Dollar (SGD)"), value: "SGD" },
    { label: t("UAE Dirham (AED)"), value: "AED" },
    { label: t("United States Dollar (USD)"), value: "USD" }
  ]
}

const styles = StyleSheet.create({
  container: {
    margin: theme.spacing.md
  },
  row: {
    flexWrap: "wrap"
  },
  devSection: {
    marginTop: theme.spacing.lg
  },
  selectContainer: {
    height: spacing(4.5),
    borderBottomWidth: theme.borderWidth.xs,
    borderBottomColor: theme.colors.gray.medium
  },
  label: {
    marginHorizontal: spacing(0.5)
  }
})

const Settings: React.FC<RootNavigationProps> = ({
  navigation
}: RootNavigationProps) => {
  const { t } = useTranslation()
  const fiatCurrencySelectItems = useFiatCurrencySelectItems()
  const pickerRef = useRef<PickerSelect>(null)
  const { fiatAsset, setFiatAsset } = useContext(UserContext)
  // const { expoPushToken, removeExpoPushToken } =
  //   useContext(NotificationsContext)
//  const { setExpoPushToken } = useContext(NotificationsContext)
  const { mutate: addExpoToken } = useAddExpoToken()
  const [isLoading, setIsLoading] = useState(false)
  const [isTokenExist, setIsTokenExists] = useState(true)
  // const _removeExpoPushToken = async () => {
  //   const result = await removeExpoPushToken()
  //   Alert.alert(
  //     result ? "Notifications disabled." : "Failed to disable notifications."
  //   )
  //   setIsTokenExists(false)
  // }

  // const registerForPushNotifications = async () => {
  //   if (Constants.isDevice) {
  //     setIsLoading(true)
  //     try {
  //       const { status: existingStatus } =
  //         await Notifications.getPermissionsAsync()
  //       let finalStatus = existingStatus
  //       if (existingStatus !== "granted") {
  //         const { status } = await Notifications.requestPermissionsAsync()
  //         finalStatus = status
  //       }
  //       if (finalStatus !== "granted") {
  //         alert(t("Failed to register for push notifications."))
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
  //           shouldSetBadge: false
  //         })
  //       })
  //       // console.log("herehehehehhejdcsdkcbjsdjcsdc")
  //       // alert("here" , token)
  //     } catch (e) {
  //       alert(t("There was an error setting up push notifications."))
  //     } finally {
  //       setIsLoading(false)
  //     }
  //   } else {
  //     alert(t("Must use a physical device for Push Notifications."))
  //   }

  //   if (!isIOS) {
  //     Notifications.setNotificationChannelAsync("default", {
  //       name: "default",
  //       importance: Notifications.AndroidImportance.MAX,
  //       vibrationPattern: [0, 250, 250, 250],
  //       lightColor: "#FF231F7C"
  //     })
  //   }
  //   setIsTokenExists(true)
  // }
  // const setShowNotification = () => {
  //   console.log("token token isTokenExist", isTokenExist)
  //   if (isTokenExist) {
  //     setIsTokenExists(false)
  //     _removeExpoPushToken()
  //   } else {
  //     setIsTokenExists(true)
  //     registerForPushNotifications()
  //   }
  // }

  return (
    <ScreenLayoutTab
      title={t("Settings")}
      leftIconName="ArrowLeft"
      onLeftIconClick={navigation.goBack}
    >
      <Container style={styles.container}>
        <TouchableWithoutFeedback
          onPress={() => pickerRef?.current?.togglePicker(true)}
        >
          <View>
            <Typography variant="label" textAlign="left" color="grayDark">
              {t("Display Currency")}
            </Typography>
            <SelectFieldControlled
            placeHolderLabel = {"Select Currency ..."}
              value={fiatAsset}
              onChange={setFiatAsset as (value: string) => void}
              items={fiatCurrencySelectItems}
            />
          </View>
        </TouchableWithoutFeedback>
        <Container spacing={1} style={{ marginTop: 10 }}>
          <Typography variant="button" textAlign="left">
            Notification
          </Typography>
          <TouchableOpacity>
            <Container
              direction="row"
              alignItems="center"
              noItemsStretch
              style={styles.row}
              spacing={2}
            >
              <Icon
                name={isTokenExist ? "CheckboxChecked" : "CheckboxUnchecked"}
              />
              <Typography>Enable Notification</Typography>
            </Container>
          </TouchableOpacity>
        </Container>
      </Container>

      {isDev && (
        <>
          <Typography
            variant="subtitle"
            color="grayMedium"
            style={styles.devSection}
          >
            Development Only
          </Typography>
          <DevelopmentSection />
        </>
      )}
    </ScreenLayoutTab>
  )
}

export default Settings

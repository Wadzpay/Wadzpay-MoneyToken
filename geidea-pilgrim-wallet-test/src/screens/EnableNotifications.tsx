import React, { useContext, useEffect, useState } from "react"
import { BackHandler, Image, StyleSheet } from "react-native"
import { useTranslation } from "react-i18next"
//import * as Notifications from "expo-notifications"

import {
  Container,
  ScreenLayoutOnboarding,
  theme,
  Typography
} from "~/components/ui"
import { RootNavigationProps } from "~/components/navigators"
import { NotificationsContext, UserContext } from "~/context"
import { isIOS } from "~/utils"
import { useAddExpoToken } from "~/api/user"

const styles = StyleSheet.create({
  image: {
    marginBottom: theme.spacing.xs
  }
})

const EnableNotifications: React.FC<RootNavigationProps> = ({
  navigation
}: RootNavigationProps) => {
  const { t } = useTranslation()
  const [isLoading, setIsLoading] = useState(false)
  const { user, isLoading: isLoadingUser } = useContext(UserContext)
  //const { setExpoPushToken, setIsNotificationEnabled } = useContext(NotificationsContext)
  const { mutate: addExpoToken } = useAddExpoToken()

  const registerForPushNotifications = async () => {
   // setIsNotificationEnabled(true)
    // if (Constants.isDevice) {
    //   setIsLoading(true)
    //   try {
    //     const { status: existingStatus } =
    //       await Notifications.getPermissionsAsync()
    //     let finalStatus = existingStatus
    //     if (existingStatus !== "granted") {
    //       const { status } = await Notifications.requestPermissionsAsync()
    //       finalStatus = status
    //     }
    //     if (finalStatus !== "granted") {
    //       alert(t("Failed to register for push notifications."))
    //       return
    //     }

    //     // Set token to context and post it to the BE
    //     const token = (await Notifications.getExpoPushTokenAsync()).data
    //     await addExpoToken({ expoToken: token })
    //     setExpoPushToken(token)

    //     Notifications.setNotificationHandler({
    //       handleNotification: async () => ({
    //         shouldShowAlert: true,
    //         shouldPlaySound: false,
    //         shouldSetBadge: false
    //       })
    //     })
    //   } catch (e) {
    //     alert(t("There was an error setting up push notifications."))
    //   } finally {
    //     setIsLoading(false)
    //   }
    // } else {
    //   alert(t("Must use a physical device for Push Notifications."))
    // }

    // if (!isIOS) {
    //   Notifications.setNotificationChannelAsync("default", {
    //     name: "default",
    //     importance: Notifications.AndroidImportance.MAX,
    //     vibrationPattern: [0, 250, 250, 250],
    //     lightColor: "#FF231F7C"
    //   })
    // }

    navigation.push("Success")
  }

  const onSkip = async () => {
    setExpoPushToken("")
    setIsNotificationEnabled(false)
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    navigation.push("Success")
    return true
  }

  useEffect(() => {
    function handleBackButton() {
     // setExpoPushToken("")
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      navigation.push("Success")
      return true
    }

    const backHandler = BackHandler.addEventListener(
      "hardwareBackPress",
      handleBackButton
    )

    return () => backHandler.remove()
  }, [navigation])

  return (
    <ScreenLayoutOnboarding
      title=""
      content={
        <Container alignItems="center" noItemsStretch spacing={3}>
          <Image
            source={require("~images/EnableNotifications/Notifications.png")}
            style={styles.image}
          />
          <Typography variant="subtitle">
            {t("Enable notifications.")}
          </Typography>
          <Typography color="grayMedium">
            {t(
              "To get notified about every transaction success, please enable notifications."
            )}
          </Typography>
        </Container>
      }
      onNext={registerForPushNotifications}
      nextButtonText={t("Enable")}
      nextLoading={isLoadingUser || isLoading}
      nextDisabled={isLoadingUser || isLoading}
      onSkip={onSkip}
      rightComponent={<Typography color="orange">{t("Skip")}</Typography>}
    />
  )
}

export default EnableNotifications

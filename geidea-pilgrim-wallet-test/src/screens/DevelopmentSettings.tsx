/* eslint-disable no-console */
import React, { useContext, useEffect } from "react"
import Clipboard from '@react-native-clipboard/clipboard';
import { Alert, StyleSheet, TouchableOpacity } from "react-native"

import { useAddFakeUserData } from "~/api/user"
import { usePublicApiUrl } from "~/api"
import { RootNavigationProps } from "~/components/navigators"
import {
  Typography,
  theme,
  Screen,
  Container,
  Icon,
  ErrorModal,
  LoadingSpinner,
  SelectFieldControlled
} from "~/components/ui"
import {
  EnvironmentContext,
  NotificationsContext,
  UserContext
} from "~/context"
import { getIdTokenAsync } from "~/auth/AuthManager"

// TODO replace with the same values from the BE
// as for the dummy countries for account creation?
const devOnRampCountriesSelectItems = [
  { label: "Indonesia", value: "ID" },
  { label: "India", value: "IN" },
  { label: "Czech Republic", value: "CZ" },
  { label: "Hungary", value: "HU" },
  { label: "Philippines", value: "PH" },
  { label: "Singapore", value: "SG" },
  { label: "Slovakia", value: "SK" },
  { label: "Bangladesh", value: "BG" },
  { label: "Pakistan", value: "PK" },
  { label: "The Kingdom of Saudi Arabia", value: "SA" },
  { label: "The United Arab Emirates", value: "AE" },
  { label: "Ukraine", value: "UA" },
  { label: "United Kingdom", value: "UK" }
]

const styles = StyleSheet.create({
  container: {
    margin: theme.spacing.md
  },
  row: {
    flexWrap: "wrap"
  }
})

export const DevelopmentSection: React.FC = () => {
  const {
    isLocal,
    setIsLocal,
    isFakeOnRamp,
    setIsFakeOnRamp,
    onRampCountry,
    setOnRampCountry
  } = useContext(EnvironmentContext)
  // const { expoPushToken, removeExpoPushToken } =
  //   useContext(NotificationsContext)
  const { user } = useContext(UserContext)
  const {
    mutate: addFakeUserData,
    isSuccess,
    isLoading,
    error
  } = useAddFakeUserData()
  const PUBLIC_API_URL = usePublicApiUrl()

  useEffect(() => {
    if (isSuccess) {
      Alert.alert("User data successfully added.")
    }
  }, [isSuccess])

  const copyToClipboard = (value: string) => {
    Clipboard.setString(value)
    alert("Copied!")
  }

  const _removeExpoPushToken = async () => {
    // const result = await removeExpoPushToken()
    // Alert.alert(
    //   result
    //     ? "Expo token was removed successfully."
    //     : "Failed to remove expo token"
    // )
  }

  return (
    <>
      <ErrorModal error={error} />
      <Container spacing={2} style={styles.container}>
        <Typography variant="button" textAlign="left">
          Environment
        </Typography>
        <TouchableOpacity onPress={() => setIsLocal(!isLocal)}>
          <Container
            direction="row"
            alignItems="center"
            noItemsStretch
            style={styles.row}
            spacing={2}
          >
            <Icon name={isLocal ? "CheckboxChecked" : "CheckboxUnchecked"} />
            <Typography>isLocal</Typography>
          </Container>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => copyToClipboard(PUBLIC_API_URL)}>
          <Typography textAlign="left" color="orange">
            PUBLIC_API_URL: <Typography>{PUBLIC_API_URL}</Typography>
          </Typography>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => copyToClipboard("No token available")}
        >
          <Typography color="orange" textAlign="left">
            Get EXPO_PUSH_TOKEN {"(No token available)"}
          </Typography>
        </TouchableOpacity>
        <TouchableOpacity onPress={_removeExpoPushToken}>
          <Typography color="error" textAlign="left">
            Remove EXPO_PUSH_TOKEN
          </Typography>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={async () =>
            copyToClipboard((await getIdTokenAsync()) || "No token available")
          }
        >
          <Typography color="orange" textAlign="left">
            Get USER_ID_TOKEN {!user && "(No token available)"}
          </Typography>
        </TouchableOpacity>

        <Typography variant="button" textAlign="left">
          On-Ramp
        </Typography>
        <TouchableOpacity onPress={() => setIsFakeOnRamp(!isFakeOnRamp)}>
          <Container
            direction="row"
            alignItems="center"
            noItemsStretch
            style={styles.row}
            spacing={2}
          >
            <Icon
              name={isFakeOnRamp ? "CheckboxChecked" : "CheckboxUnchecked"}
            />
            <Typography>isFakeOnRamp</Typography>
          </Container>
        </TouchableOpacity>
        <SelectFieldControlled
        placeHolderLabel = {"Select Country ..."}
          value={onRampCountry}
          onChange={setOnRampCountry}
          items={devOnRampCountriesSelectItems}
        />

        <Typography variant="button" textAlign="left">
          Data
        </Typography>
        <TouchableOpacity onPress={() => addFakeUserData("")}>
          {isLoading ? (
            <LoadingSpinner />
          ) : (
            <Typography color="orange" textAlign="left">
              Add Fake User Data
            </Typography>
          )}
        </TouchableOpacity>
      </Container>
    </>
  )
}
const DevelopmentSettings: React.FC<RootNavigationProps> = ({
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  navigation
}: RootNavigationProps) => {
  return (
    <Screen dismissKeyboard>
      <DevelopmentSection />
    </Screen>
  )
}

export default DevelopmentSettings

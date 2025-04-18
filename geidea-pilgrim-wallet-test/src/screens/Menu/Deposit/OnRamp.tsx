import React, { useContext } from "react"
import { useTranslation } from "react-i18next"
import { Dimensions, StyleSheet, TouchableOpacity } from "react-native"
import { WebView } from "react-native-webview"

import { Container, LoadingSpinner, theme, Typography } from "~/components/ui"
import { EnvironmentContext, UserContext } from "~/context"
import { isDev, isIOS } from "~/utils"
import { useOnRampConfig } from "~/api/onRamp"
import { OnRampConfigData } from "~/api/models"

const { width, height: windowHeight } = Dimensions.get("window")
const height =
  windowHeight -
  theme.headerHeight -
  theme.appTabBar.height -
  theme.appTabBar.paddingBottom

const styles = StyleSheet.create({
  widget: {
    width,
    ...(isIOS ? { height } : { minHeight: height }),
    marginTop: -theme.spacing.lg
  },
  errorContainer: {
    height,
    marginHorizontal: theme.spacing.lg
  }
})

const getWalletsString: (data?: OnRampConfigData) => string = (data) => {
  if (!data) return ""
  return data?.digitalCurrencyList
    ?.map((crypto) => `${crypto.code}:${crypto.inwardAddress}`)
    .join(",")
}

const getOnlyCryptosString: (data?: OnRampConfigData) => string = (data) => {
  if (!data) return ""
  return data?.digitalCurrencyList?.map((crypto) => crypto.code).join(",")
}

const getOnlyFiatsString: (data?: OnRampConfigData) => string = (data) => {
  if (!data) return ""
  return data.fiats.map((fiat) => fiat.code).join(",")
}

const OnRamp: React.FC = () => {
  const { t } = useTranslation()
  const { user } = useContext(UserContext)
  const { onRampCountry } = useContext(EnvironmentContext)
  const { data, isFetching, error, refetch } = useOnRampConfig()
  const onRamperConfig = {
    apiKey: data?.onRampApiKey || "",
    wallets: getWalletsString(data),
    onlyGateways: data?.gateways || "",
    onlyCryptos: getOnlyCryptosString(data),
    onlyFiat: getOnlyFiatsString(data),
    // Allow changing country only in dev, else detected automatically
    ...(isDev ? { country: onRampCountry } : {}),
    isAddressEditable: "false",
    // The widget does't use "#", only the hexcode
    color: theme.colors.orange.substr(1),
    fontFamily: "Arial",
    supportSell: "false",
    partnerContext: JSON.stringify({ cognitoUsername: user?.username })
  }

  const params = new URLSearchParams(onRamperConfig)
  const widgetURL = `https://widget.onramper.com?${params}`

  // console.log("widgetURL", widgetURL)
  // console.log("params", params)

const HTML = `
<div style="display: flex; justify-content: center; padding: 15px;">
  <iframe
    style="--border-radius: 10px; box-shadow: 0 2px 10px 0 rgba(0,0,0,.20); border-radius: var(--border-radius); margin: auto;max-width: 420px"
    src=${widgetURL}
    height="660px"
    width="482px"
    title="Onramper widget"
    frameborder="0"
    allow="accelerometer; autoplay; camera; gyroscope; payment"
  >
  </iframe>
</div>
`;
  return data && !isFetching ? (
    <WebView
    originWhitelist={["*"]}
          allowsInlineMediaPlayback
          javaScriptEnabled
          scalesPageToFit
          mediaPlaybackRequiresUserAction={false}
          javaScriptEnabledAndroid
          useWebkit
          startInLoadingState={true}
      style={styles.widget}
      useWebKit={true}
      renderLoading={() => (
        <Container
          justify="center"
          alignItems="center"
          noItemsStretch
          style={styles.widget}
        >
          <LoadingSpinner color="orange" />
        </Container>
      )}
      source={{
        uri: widgetURL
      }}
    />
  ) : error ? (
    <Container justify="center" spacing={2} style={styles.errorContainer}>
      <Typography>
        {t("Could not fetch data from the server. Please try again later.")}
      </Typography>
      <TouchableOpacity onPress={() => refetch}>
        <Typography variant="button" color="orange">
          {t("Retry")}
        </Typography>
      </TouchableOpacity>
    </Container>
  ) : (
    <Container
      justify="center"
      alignItems="center"
      noItemsStretch
      style={styles.widget}
    >
      <LoadingSpinner color="orange" />
    </Container>
  )
}

export default OnRamp

import React from "react"
import { useTranslation } from "react-i18next"
import { TFunction } from "i18next"

import { RootNavigationProps } from "~/components/navigators"
import {
  ScreenLayoutBottomActions,
  Carousel,
  Button,
  Container
} from "~/components/ui"
import { isProd } from "~/utils"

const carouselData = (t: TFunction) => [
  {
    id: 1,
    image: require("~images/Welcome/Worldmapblank_1.png"),
    imagePager: require("~images/Welcome/Group_12744.png"),
    header: t("Simpler, Smarter, Supportive Banking")
    // body: t("Welcome to WadzPay - body")
    // },
    // {
    //   id: 2,
    //   image: require("~images/Welcome/Worldmapblank_1.png"),
    //   imagePager: require("~images/Welcome/undrawtransfer_1.png"),
    //   header2: t("Instant Transfers")
    // },
    // {
    //   id: 3,
    //   image: require("~images/Welcome/undrawaroundtheworld_1.png"),
    //   header3: t("Easy Transactions")
    //   // body: t("Easy Transactions - body")
  }
]

const Welcome: React.FC<RootNavigationProps> = ({
  navigation
}: RootNavigationProps) => {
  const { t } = useTranslation()
  return (
    <ScreenLayoutBottomActions
      fullWidthContent
      content={<Carousel data={carouselData(t)} />}
      actions={
        <Container spacing={2}>
          {/* {!isProd && (
            <Button
              variant="primary"
              onPress={() => {
                navigation.navigate("DevelopmentSettings")
              }}
              text={"Dev Settings"}
            />
          )} */}
          <Button
            variant="primary"
            onPress={() => {
              navigation.navigate("SignIn")
            }}
            text={t("Login")}
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
    />
  )
}

export default Welcome

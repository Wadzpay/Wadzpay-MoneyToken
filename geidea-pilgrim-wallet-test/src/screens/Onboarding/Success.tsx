import React, { useContext, useEffect } from "react"
import { Image, StyleSheet } from "react-native"
import { useTranslation } from "react-i18next"
import { CommonActions } from "@react-navigation/native"

import {
  Container,
  ScreenLayoutOnboarding,
  theme,
  Typography
} from "~/components/ui"
import { OnboardingNavigationProps } from "~/components/navigators/OnboardingStackNavigator"
import { useSignIn } from "~/api/user"
import { OnboardingContext } from "~/context"

const styles = StyleSheet.create({
  image: {
    marginTop: theme.spacing.lg,
    marginBottom: theme.spacing.xs
  }
})

const Success: React.FC<OnboardingNavigationProps> = ({
  navigation
}: OnboardingNavigationProps) => {
  const { t } = useTranslation()
  const { onboardingValues } = useContext(OnboardingContext)
  const { mutate: signIn, isLoading, error } = useSignIn()

  useEffect(() => {
    if (error) {
      navigation.dispatch(
        CommonActions.reset({
          index: 0,
          routes: [{ name: "SignIn" }]
        })
      )
    }
  }, [error])

  const onLogin = () => {
    // signIn({
    //   email: onboardingValues.accountDetails.email,
    //   password: onboardingValues.accountDetails.newPassword
    // })

    navigation.dispatch(
      CommonActions.reset({
        index: 0,
        routes: [{ name: "SignIn" }]
      })
    )
  }

  return (
    <ScreenLayoutOnboarding
      title=""
      hasHeader={false}
      content={
        <Container alignItems="center" noItemsStretch spacing={3}>
          <Image
            source={require("~images/Welcome/Welcome_1.png")}
            style={styles.image}
          />
          <Typography variant="subtitle">
            {t("Your account was created.")}
          </Typography>
          <Typography color="grayMedium">{t("Few more details")}</Typography>
        </Container>
      }
      onNext={onLogin}
      nextLoading={isLoading}
      nextDisabled={isLoading}
      error={error}
    />
  )
}

export default Success

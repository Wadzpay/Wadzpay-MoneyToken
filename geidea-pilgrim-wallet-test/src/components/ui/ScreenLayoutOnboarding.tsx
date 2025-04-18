import React from "react"
import { KeyboardAvoidingView, StyleSheet, View } from "react-native"
import { useTranslation } from "react-i18next"

import theme, { spacing } from "./theme"
import Screen from "./Screen"
import Header from "./Header"
import Button from "./Button"
import Container from "./Container"
import ErrorModal from "./ErrorModal"

import { isIOS, StatusBarHeight } from "~/utils"

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: spacing(3),
    marginTop: isIOS ?  spacing(2) : 0,
   // marginTop: theme.spacing.xs
  },
  contentContainer: {
    flex: 5
  },
  actionsContainer: {
    flex: 1,
    marginBottom: theme.spacing.lg
  }
})

type Props = {
  title: string
  content: React.ReactNode
  onNext: () => void
  onBack?: () => void
  rightComponent?: React.ReactNode
  onSkip?: () => void
  hasHeader?: boolean
  useLogo?: boolean
  nextDisabled?: boolean
  nextLoading?: boolean
  error?: Error | null
  avoidKeyboard?: boolean
  nextButtonText?: string
}

const ScreenLayoutOnboarding: React.FC<Props> = ({
  title,
  content,
  onNext,
  onBack,
  rightComponent,
  onSkip,
  hasHeader = true,
  useLogo = false,
  nextDisabled = false,
  nextLoading = false,
  error,
  avoidKeyboard = false,
  nextButtonText
}: Props) => {
  const { t } = useTranslation()

  const mainContainer = (
    <>
      <View style={styles.contentContainer}>{content}</View>
      <Container justify="flex-end" style={styles.actionsContainer}>
        <Button
          variant="primary"
          onPress={onNext}
          text={nextButtonText || t("Next")}
          loading={nextLoading}
          disabled={nextDisabled}
          {...(nextButtonText ? {} : { iconName: "ArrowRight" })}
        />
      </Container>
    </>
  )
  return (
    <Screen dismissKeyboard>
      {hasHeader && (
        <Header
          title={title}
          useLogo={useLogo}
          {...(onBack
            ? { leftIconName: "ArrowLeft", onLeftIconClick: onBack }
            : {})}
          rightComponent={rightComponent}
          onRightIconClick={onSkip}
        />
      )}
      {avoidKeyboard ? (
        <KeyboardAvoidingView
          behavior={isIOS ? "padding" : "height"}
          keyboardVerticalOffset={StatusBarHeight}
          style={styles.container}
        >
          {mainContainer}
        </KeyboardAvoidingView>
      ) : (
        <View style={styles.container}>{mainContainer}</View>
      )}
      <ErrorModal error={error} />
    </Screen>
  )
}

export default ScreenLayoutOnboarding

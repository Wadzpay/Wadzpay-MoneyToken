import React from "react"
import { useTranslation } from "react-i18next"

import Container from "./Container"
import Modal from "./Modal"
import Typography from "./Typography"

type Props = {
  isVisible: boolean
  setIsVisible: (value: boolean) => void
}

export const UnderDevelopmentMessage: React.FC = () => {
  const { t } = useTranslation()
  return (
    <Container spacing={2}>
      <Typography variant="subtitle" color="orange">
        {t("Coming soon!")}
      </Typography>
      <Typography>{t("We are working on this feature.")}</Typography>
    </Container>
  )
}

export const UnderDevelopmentModal: React.FC<Props> = ({
  isVisible,
  setIsVisible
}: Props) => {
  return (
    <Modal
      variant="center"
      isVisible={isVisible}
      onDismiss={() => setIsVisible(false)}
      swipeDirection={["down"]}
      dismissButtonVariant="button"
    >
      <UnderDevelopmentMessage />
    </Modal>
  )
}

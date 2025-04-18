import React from "react"
import { useTranslation } from "react-i18next"

import {
  Container,
  Modal,
  theme,
  Typography,
  UnderDevelopmentMessage
} from "~/components/ui"

type Props = {
  isVisible: boolean
  onDismiss: () => void
}

const Withdraw: React.FC<Props> = ({ isVisible, onDismiss }: Props) => {
  const { t } = useTranslation()
  return (
    <Modal
      variant="bottom"
      isVisible={isVisible}
      onDismiss={onDismiss}
      dismissButtonVariant="cancel"
      contentStyle={{ height: theme.modalFullScreenHeight }}
    >
      {/* TODO remove UnderDevelopmentMessage */}
      <Container spacing={4}>
        <Typography variant="title">{t("Withdraw")}</Typography>
        <UnderDevelopmentMessage />
      </Container>
    </Modal>
  )
}

export default Withdraw

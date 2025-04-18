import React, { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"

import Typography from "./Typography"
import Modal from "./Modal"
import Container from "./Container"

type Props = {
    subtitle?: string
    message?: string
    onDissmiss?: () => void
}

const CommonAlertModal: React.FC<Props> = ({ subtitle, message, onDissmiss}: Props) => {
  const { t } = useTranslation()
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    setIsVisible(!!message)
  }, [message])

  return (
    <Modal
      variant="center"
      isVisible={isVisible}
      onDismiss={() => setIsVisible(false)}
      swipeDirection={["down"]}
      dismissButtonVariant="button"
    >
      <Container spacing={2}>
        <Typography variant="subtitle" color="error">
          {subtitle}
        </Typography>
       
        <Typography variant="button">{message}</Typography>
      </Container>
    </Modal>
  )
}

export default CommonAlertModal

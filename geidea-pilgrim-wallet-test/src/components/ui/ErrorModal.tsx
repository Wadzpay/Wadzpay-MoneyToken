import React, { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"

import Typography from "./Typography"
import Modal from "./Modal"
import Container from "./Container"

type Props = {
  error?: Error | null
}

const ErrorModal: React.FC<Props> = ({ error }: Props) => {
  const { t } = useTranslation()
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    setIsVisible(!!error)
  }, [error])

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
          {t("An error occured.")}
        </Typography>
       
        <Typography variant="button">{error?.message}</Typography>
      </Container>
    </Modal>
  )
}

export default ErrorModal

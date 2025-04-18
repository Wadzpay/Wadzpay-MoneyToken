import React, { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"

import Typography from "./Typography"
import Modal from "./Modal"
import Container from "./Container"
import { TouchableOpacity } from "react-native"
import theme from "./theme"

type Props = {
    subtitle?: string
    message?: string
    onDissmiss?: () => void
}

const CommonWarningModal: React.FC<Props> = ({ subtitle, message, onDissmiss}: Props) => {
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
      <Container
            alignItems="flex-start"
            noItemsStretch
            justify="flex-start"
            spacing={2}>
            <Typography
              variant="label"
              fontFamily="Rubik-Medium"
              fontWeight="bold"
              color="darkBlackBold">
              {t('Balance running low')}
            </Typography>
            <Typography
              variant="label"
              fontFamily="Rubik-Regular"
              fontWeight="regular"
              color="darkBlackBold">
              {message}
            </Typography>
          </Container>
       
          <Container
          direction="row"
          alignItems="center"
          justify="flex-end"
          noItemsStretch
          spacing={2}
          style={{marginTop: 20}}>
          <TouchableOpacity
            style={
              {
                alignItems:"center",
                justifyContent:"center",
                width:100,
                height:40,
                borderRadius:5,
                backgroundColor:theme.colors.white,
                borderColor:theme.colors.darkBlackBold,
                borderWidth:1
              }
            }
            onPress={() => {setIsVisible(false)}}>
            <Typography
              color="darkBlackBold"
              variant="heading"
              textAlign="center"
              fontFamily="Rubik-Medium">
              OK
            </Typography>
          </TouchableOpacity>
          </Container>
      </Container>
    </Modal>

  )
}

export default CommonWarningModal

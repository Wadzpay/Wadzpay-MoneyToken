import React from "react"
import { useTranslation } from "react-i18next"
import { StyleSheet, TouchableOpacity, View } from "react-native"

import {
  Container,
  LoadingSpinner,
  theme,
  Typography,
  UserAvatar
} from "~/components/ui"

const getStyles = (isSelected: boolean) =>
  StyleSheet.create({
    row: {
      width: "100%",
      paddingHorizontal: theme.spacing.xs,
      borderRadius: theme.borderRadius.md,
      borderWidth: 0,
      paddingVertical: 15,
      ...(isSelected
        ? {
            borderColor: theme.colors.orange,
            backgroundColor: `${theme.colors.yellow}25`,
            
          }
        : {
            borderColor: theme.colors.white
          })
    },
    shiftRight:{
      marginLeft:20
    }
  })

type Props = {
  nickname?: string
  email?: string
  isSelected?: boolean
  isLoading?: boolean
  onPress?: () => void
}

const ContactItem: React.FC<Props> = ({
  nickname,
  email,
  isSelected = false,
  isLoading = false,
  onPress
}: Props) => {
  const { t } = useTranslation()
  const styles = getStyles(isSelected)
  const contactItem = (
    <Container
      direction="row"
      alignItems="center"
      noItemsStretch
      spacing={-1}
      style={[styles.row]}
    >
       <UserAvatar name={nickname} />
      {isLoading ? (
        <LoadingSpinner color="orange" />
      ) : (
        <Container style={styles.shiftRight}>
         {nickname ?  <Typography  fontFamily="Rubik-Regular" variant="heading"  color="BlackLight"  textAlign="left">
            {nickname || t("Please Select")}
          </Typography> : undefined}
          {!!email && (
            <Typography variant="label" textAlign="left" color="grayMedium">
              {email}
            </Typography>
          )}

        </Container>
      )}
    </Container>
  )

  return onPress ? (
    <TouchableOpacity onPress={onPress}>{contactItem}</TouchableOpacity>
  ) : (
    contactItem
  )
}

export default ContactItem

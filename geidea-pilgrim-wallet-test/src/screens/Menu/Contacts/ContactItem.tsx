import React from "react"
import { StyleSheet, TouchableOpacity } from "react-native"

import { Contact } from "~/api/models"
import {
  Container,
  Icon,
  spacing,
  theme,
  Typography,
  UserAvatar
} from "~/components/ui"
import { IconName } from "~/icons"

const styles = StyleSheet.create({
  contactRow: {
    marginVertical: spacing(0.75) ,// 6px,
    paddingHorizontal:15,
  },
  avatar: {
    padding: spacing(0.5),
    marginRight: theme.spacing.xs
  }
})

type Props = Omit<Contact, "id"> & {
  iconName?: IconName
  onPress?: () => void
}

const ContactItem: React.FC<Props> = ({
  nickname,
  email,
  phoneNumber,
  iconName,
  onPress
}: Props) => {
  const contactItem = (
    <Container
      direction="row"
      justify="space-between"
      alignItems="center"
      style={styles.contactRow}
      noItemsStretch
    >
      <Container direction="row" alignItems="center" noItemsStretch>
        <Container style={styles.avatar}>
          <UserAvatar name={nickname} />
        </Container>

        <Container noItemsStretch>
          <Typography  fontFamily="Rubik-Regular" variant="label" color="BlackLight"  >{nickname}</Typography>
          <Typography variant="label" textAlign="left" color="darkBlack">
            {email}
          </Typography>
          <Typography variant="label" textAlign="left" color="grayMedium">
            {phoneNumber}
          </Typography>
        </Container>
      </Container>

      {iconName && <Icon name={iconName} />}
    </Container>
  )

  return onPress ? (
    <TouchableOpacity onPress={onPress}>{contactItem}</TouchableOpacity>
  ) : (
    contactItem
  )
}

export default ContactItem

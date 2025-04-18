import React from "react"
import { StyleSheet } from "react-native"

import Typography from "./Typography"
import Container from "./Container"
import Icon from "./Icon"
import theme from "./theme"

const styles = StyleSheet.create({
  userAvatarCirle: {
    width: theme.spacing.xl,
    height: theme.spacing.xl,
    borderRadius: theme.spacing.md,
    borderColor: "#ececec",
    borderWidth: theme.borderWidth.xs,
    backgroundColor: "#ececec"
  }
})

type Props = {
  name?: string
}

const UserAvatar: React.FC<Props> = ({ name }: Props) => {
  return (
    <Container
      justify="center"
      alignItems="center"
      noItemsStretch
      style={styles.userAvatarCirle}
    >
      {name && name.length ? (
        <Typography color="blackishGray" uppercase>
          {name[0]}
        </Typography>
      ) : (
        <Icon name="User" color="focused" size="sm" />
      )}
    </Container>
  )
}

export default UserAvatar

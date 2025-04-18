import React from "react"
import { Linking } from "react-native"

import Typography from "./Typography"

type Props = {
  title: string
  url: string
}

const Link: React.FC<Props> = ({ title, url }: Props) => {
  return (
    <Typography onPress={() => Linking.openURL(url)} color="orange">
      {title}
    </Typography>
  )
}

export default Link

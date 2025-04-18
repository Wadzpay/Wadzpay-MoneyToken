import React from "react"

import { Chip, Container, Typography } from "~/components/ui"
import { ChipColorVariant } from "~/components/ui/theme"

type InfoItemProps = {
  title: string
  text?: string
  component?: JSX.Element
  isChip?: boolean
  chipColor?: ChipColorVariant
}

const InfoItem: React.FC<InfoItemProps> = ({
  title,
  text = "",
  component,
  isChip = false,
  chipColor = "black"
}: InfoItemProps) => (
  <Container noItemsStretch spacing={1}>
    <Typography color="grayMedium">{title}</Typography>
    {isChip ? (
      <Chip variant="body" text={text} color={chipColor} />
    ) : component ? (
      component
    ) : (
      <Typography>{text}</Typography>
    )}
  </Container>
)

export default InfoItem

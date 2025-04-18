import React from "react"
import { Dimensions, StyleSheet, View, TouchableOpacity } from "react-native"

import { Typography, Container, theme, Icon } from "~/components/ui"
import { IconName } from "~/icons"

const { width } = Dimensions.get("window")

const styles = StyleSheet.create({
  container: {
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.md,

  },
  item: {
    marginTop:15,
    width: "100%",

  },
  separatorContainer: {
    display: "flex",
    alignItems: "center"
  },
  separator: {
    backgroundColor: "#C3C3C3",
    width: width,
    height: 0.7,
    marginTop:15
  }
})

export enum MenuItemType {
  NAV = "nav",
  LINK = "link"
}

const menuItemTypeIconMap: { [key in MenuItemType]: IconName } = {
  nav: "ArrowRight",
  link: "Link"
}

export type MenuItem = {
  title: string
  onPress: () => void
  iconName?: IconName
  type?: MenuItemType
}

type Props = {
  title: string
  items: MenuItem[]
  hideSeparator?: boolean
}

const MenuSection: React.FC<Props> = ({
  title,
  items,
  hideSeparator = false
}: Props) => {
  return (
    <Container spacing={2} style={styles.container}>
      {/* <Typography variant="label" textAlign="left" color="BlackLight">
        {title}
      </Typography> */}
      <Container spacing={1}>
        {items.map((item) => (
          <TouchableOpacity
            key={item.title}
            onPress={item.onPress}
            style={styles.item}
          >
            <Container  alignItems="center" justify="space-between">
              <Container
                direction="row"
                alignItems="center"
                noItemsStretch
                spacing={1}
              >
                {item.iconName && (
                  <Icon
                  size="sm"
                    name={item.iconName}
                    color={"iconRegulorColor"}
                  />
                )}
                <Typography>
                  {item.title}
                </Typography>
              </Container>
              <View style={styles.separatorContainer} >
              <View style={styles.separator} />
              </View>
              </Container>
            
               


          </TouchableOpacity>
        ))}
      </Container>
     
     
    </Container>
  )
}

export default MenuSection

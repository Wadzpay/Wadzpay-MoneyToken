import React from "react"
import { useTranslation } from "react-i18next"
import { FlatList, StyleSheet } from "react-native"

import { Container, Icon, theme, Typography } from "~/components/ui"
import { IconColorVariant } from "~/components/ui/theme"
import { IconName } from "~/icons"

type RecentContact = {
  iconName: IconName
  text: string
  color: IconColorVariant
}

// TODO replace with API data
const useData: () => RecentContact[] = () => {
  return [
    { iconName: "User", text: "John", color: "regular" },
    { iconName: "User", text: "Alice", color: "regular" },
    { iconName: "User", text: "Joyce", color: "regular" },
    { iconName: "User", text: "Bob", color: "regular" },
    { iconName: "User", text: "Jane", color: "regular" },
    { iconName: "User", text: "Henry", color: "regular" },
    { iconName: "User", text: "Lydia", color: "regular" }
  ]
}

const styles = StyleSheet.create({
  title: {
    marginHorizontal: theme.spacing.md
  },
  listContent: {
    paddingHorizontal: theme.spacing.xs,
    paddingVertical: theme.spacing.xs
  },
  listItem: {
    marginHorizontal: theme.spacing.xs
  },
  iconContainer: {
    padding: theme.spacing.md,
    marginBottom: theme.spacing.xs,
    borderRadius: theme.borderRadius.lg,
    borderWidth: theme.borderWidth.xs,
    borderColor: theme.colors.gray.light
  }
})

const RecentContactsList: React.FC = () => {
  const { t } = useTranslation()
  const data = useData()
  return (
    <Container justify="flex-start" spacing={1}>
      <Typography
        color="grayMedium"
        textAlign="left"
        fontWeight="bold"
        style={styles.title}
      >
        {t("Recent")}
      </Typography>
      <FlatList
        horizontal
        data={data}
        contentContainerStyle={styles.listContent}
        contentInsetAdjustmentBehavior="never"
        snapToAlignment="center"
        decelerationRate="fast"
        automaticallyAdjustContentInsets={false}
        showsHorizontalScrollIndicator={false}
        showsVerticalScrollIndicator={false}
        scrollEventThrottle={1}
        keyExtractor={(item, index) => `${index}-${item}`}
        renderItem={RecentItem}
      />
    </Container>
  )
}

const RecentItem = ({ item }: { item: RecentContact }) => {
  return (
    <Container style={styles.listItem}>
      <Container style={styles.iconContainer}>
        <Icon name={item.iconName} color={item.color} />
      </Container>
      <Typography color="grayMedium">{item.text}</Typography>
    </Container>
  )
}

export default RecentContactsList

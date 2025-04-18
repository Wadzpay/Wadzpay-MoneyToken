import React, { PropsWithChildren } from "react"
import { StyleSheet, View, ScrollView, RefreshControl } from "react-native"

import theme from "./theme"
import Screen from "./Screen"
import Header from "./Header"

import { IconName } from "~/icons"

const styles = StyleSheet.create({
  scrollContainer: {
    flex: 1,
    height:"100%"
  },
  container: {
    flex: 1,
   
  }
})

type Props = PropsWithChildren<{
  title?: string
  showTitleHeader?:boolean
  useLogo?: boolean
  leftIconName?: IconName
  onLeftIconClick?: () => void
  rightComponent?: React.ReactNode
  onRightIconClick?: () => void
  refreshing?: boolean
  onRefresh?: () => void
  useScrollView?: boolean
  dismissKeyboard?: boolean
  notificationComponent?: React.ReactNode
  onNotificationIconClick?: () => void
  showHeaderBg?:boolean
}>

const ScreenLayoutTab: React.FC<Props> = ({
  title,
  rightComponent,
  leftIconName,
  onLeftIconClick,
  useLogo = false,
  showTitleHeader = true,
  onRightIconClick,
  refreshing = false,
  onRefresh,
  useScrollView = true,
  dismissKeyboard = false,
  children,
  notificationComponent,
  onNotificationIconClick,
  showHeaderBg = true
}: Props) => {
  const _onRefresh = onRefresh
    ? {
        refreshControl: (
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        )
      }
    : {}

  return (
    <Screen dismissKeyboard={dismissKeyboard}>
      {showTitleHeader && <Header
        title={title}
        useLogo={useLogo}
        leftIconName={leftIconName}
        onLeftIconClick={onLeftIconClick}
        rightComponent={rightComponent}
        onRightIconClick={onRightIconClick}
        notificationComponent={notificationComponent}
        onNotificationIconClick={onNotificationIconClick}
        showHeaderBg={showHeaderBg}
      />}
      {useScrollView ? (
        <ScrollView 
        showsVerticalScrollIndicator={false}
         {..._onRefresh} style={styles.scrollContainer}>
          {children}
        </ScrollView>
      ) : (
        <View style={styles.container}>{children}</View>
      )}
    </Screen>
  )
}

export default ScreenLayoutTab

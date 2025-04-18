import React, { useState } from "react"
import {
  FlatList,
  RefreshControl,
  SectionList,
  StyleSheet,
  TouchableOpacity,
  View
} from "react-native"

import Typography from "./Typography"
import theme from "./theme"
import Container from "./Container"
import LoadingSpinner from "./LoadingSpinner"
import Button from "./Button"

export type ListItem = { id: number | string } & Record<string, unknown>


const sectionTitleComponent = (title: string) => (
  <Typography color="darkBlack" textAlign="left" fontFamily="Rubik-Medium" style={styles.sectionTitle}>
    {title}
  </Typography>
)

export type ListLocalization = {
  noItemsAvailable: string
  retry: string
}

type Props = {
  data: ListItem[]
  refreshing?: boolean
  onRefresh?: () => void
  localization: ListLocalization
  listItemComponent: (item: ListItem) => React.ReactElement
  getSections?: (data: ListItem[]) => { data: ListItem[]; title: string }[]
  isSearchOpen?: boolean
  isHomeList?: boolean
}

const List: React.FC<Props> = ({
  data,
  refreshing = false,
  onRefresh,
  listItemComponent,
  localization: { noItemsAvailable, retry },
  getSections,
  isSearchOpen = false,
  isHomeList = true
}: Props) => {
  const [showLoader, setShowLoader] = useState(true)
  const [showButton, setShowButton] = useState(false)
  const [updateBottomMargin, setUpdateBottomMargin] = useState(false)
  const styles = createStyles(isSearchOpen, isHomeList)
  const _onRefresh = onRefresh
    ? {
        refreshControl: (
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
          ></RefreshControl>
        )
      }
    : {}

  const noItemsComponent = () => (
    <Container spacing={1}>
      <Typography>{noItemsAvailable}</Typography>
      {onRefresh && (
        <TouchableOpacity onPress={onRefresh}>
          <Typography variant="button" color="orange">
            {retry}
          </Typography>
        </TouchableOpacity>
      )}
    </Container>
  )



  return (
    <View >
      {getSections != undefined ? (
        <SectionList
          keyboardShouldPersistTaps="handled"
          sections={getSections(data)}
          {..._onRefresh}
          keyExtractor={(item: ListItem) => `${item.id}`}
          renderItem={({ item }) => listItemComponent(item)}
          renderSectionFooter={() => <View style={styles.sectionFooter} />}
          renderSectionHeader={({ section: { title } }) =>
            sectionTitleComponent(title)
          }
          showsHorizontalScrollIndicator={false}
          showsVerticalScrollIndicator={false}
          scrollEventThrottle={1}
          ListEmptyComponent={noItemsComponent}
          stickySectionHeadersEnabled
          ListFooterComponent={
            data.length > 0 && showLoader ? (
              <Container alignItems="center" noItemsStretch>
                <LoadingSpinner color="orange" />
              </Container>
            ) : <View style={ updateBottomMargin ? styles.onEndReachedView : styles.listFooter} />
          }
          style={styles.list}
          onEndReached={() => {
            setShowLoader(false)
            setShowButton(true)
            setUpdateBottomMargin(true)
          }}
          onScroll={() => setShowButton(true)}
        />
      ) : (
        <FlatList
          data={data}
          {..._onRefresh}
          contentInsetAdjustmentBehavior="never"
          snapToAlignment="center"
          decelerationRate="fast"
          automaticallyAdjustContentInsets={false}
          showsHorizontalScrollIndicator={false}
          showsVerticalScrollIndicator={false}
          scrollEventThrottle={1}
          keyExtractor={(item: ListItem) => `${item.id}`}
          renderItem={({ item }) => listItemComponent(item)}
          ListEmptyComponent={noItemsComponent}
          ListFooterComponent={
            data.length > 0 && showLoader ? (
              <Container alignItems="center" noItemsStretch>
                <LoadingSpinner color="orange" />
              </Container>
            ) : <View style={ updateBottomMargin ? styles.onEndReachedView : styles.listFooter} />
          }
          onEndReached={() => setShowLoader(false)}
        />
      )}

      {/* <Button></Button> */}
    </View>
  )
}


const createStyles = (
  isSearchOpen : boolean,
  isHomeList : boolean
) =>  StyleSheet.create({
  sectionTitle: {
    paddingHorizontal: theme.spacing.lg,
    paddingVertical: theme.spacing.xs,
    marginBottom:5,
    backgroundColor: "#fafafa"
  },
  sectionFooter: {
    marginBottom: theme.spacing.md
  },
  list: {
  },
  listFooter: {
    marginBottom: isSearchOpen ? 250 : !isHomeList ? 140 :  40
  },
  onEndReachedView: {
    marginBottom: 250
  }
})

export default List

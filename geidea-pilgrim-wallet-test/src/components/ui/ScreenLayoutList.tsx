import React from "react"

import List, { ListItem, ListLocalization } from "./List"
import ErrorModal from "./ErrorModal"
import Container from "./Container"
import LoadingSpinner from "./LoadingSpinner"
import ScreenLayoutTab from "./ScreenLayoutTab"

import { QueryResult } from "~/constants/types"
import { IconName } from "~/icons"

type ScreenLayoutListLocalization = ListLocalization & {
  title: string
}

type Props = {
  children?: React.ReactNode
  rightComponent?: React.ReactNode
  leftIconName?: IconName
  onLeftIconClick?: () => void
  listQuery: (params: string) => QueryResult<ListItem>
  queryParams?: string
  localization: ScreenLayoutListLocalization
  listItemComponent: (item: ListItem) => React.ReactElement
  getSections?: (data: ListItem[]) => { data: ListItem[]; title: string }[]
  isListReverse?: boolean
  isLoggedInUserInSearch?: boolean
  isSortAlphabetically?: boolean
  isSearchOpen?: boolean
}

const ScreenLayoutList: React.FC<Props> = ({
  rightComponent,
  leftIconName,
  onLeftIconClick,
  listQuery,
  queryParams = "",
  localization,
  listItemComponent,
  getSections,
  children,
  isListReverse = false,
  isLoggedInUserInSearch = false,
  isSortAlphabetically = false,
  isSearchOpen=false
}: Props) => {
  const { data, isFetching, refetch, error } = listQuery(queryParams)
  const { title } = localization

if(isSortAlphabetically){
  // sorting list alphabetically\
  console.log("here!!")
  data && data.sort(function (a, b) {
    if (a.nickname < b.nickname) {
      return -1;
    }
    if (a.nickname > b.nickname) {
      return 1;
    }
    return 0;
  });
  // data && data.sort((a, b) => {
  //     if (a.email < b.email) {
  //       return -1;
  //     }
  //   });
}

  const filterDataList = () => {
    let  filteredList =  data.filter(function(data) {
      return data.transactionType === "PEER_TO_PEER" ;
    });

    return filteredList
  }
  return (
    <ScreenLayoutTab
      title={title}
      rightComponent={rightComponent}
      leftIconName={leftIconName}
      onLeftIconClick={onLeftIconClick}
      useScrollView={false}
      dismissKeyboard
    >
      {children}
      {isFetching ? (
        <Container alignItems="center" noItemsStretch>
          <LoadingSpinner color="orange" />
        </Container>
      ) : (
        <List
        data={isListReverse ? data?.reverse() || [] : data || []}
          refreshing={isFetching}
          onRefresh={refetch}
          getSections={undefined}
          listItemComponent={listItemComponent}
          localization={localization}
          isSearchOpen = {isSearchOpen}
        />
      )}
    </ScreenLayoutTab>
  )
}

export default ScreenLayoutList

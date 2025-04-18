import React, { PropsWithChildren, useState } from "react"
import { useTranslation } from "react-i18next"
import { View, StyleSheet } from "react-native"
import { useNavigation } from "@react-navigation/native"

import ContactItem from "./ContactItem"

import ScreenLayoutList from "~/components/ui/ScreenLayoutList"
import { Contact } from "~/api/models"
import { ListItem } from "~/components/ui/List"
import { getParamsFromObject, groupBy } from "~/helpers"
import { Container, Icon, TextFieldControlled, theme } from "~/components/ui"
import { useUserContacts } from "~/api/user"
import SaveContactModal from "~/components/ui/SaveContactModal"


const getSections = (data: ListItem[]) => {
  const contacts = data as Contact[]

  return Object.entries(
    groupBy(contacts, (contact) => contact.nickname.substr(0, 1).toUpperCase())
  )
    .sort(([title1], [title2]) => (title1 < title2 ? -1 : 1))
    .map(([title, data]) => ({ title, data }))
}

const styles = StyleSheet.create({
  searchTextInput: {
    backgroundColor:theme.colors.white,
    borderColor:theme.colors.iconRegulorColor,
    borderWidth:1,
    borderRadius:10,
    paddingVertical:3,
    paddingHorizontal:3
  },
  searchContainer: {
   paddingVertical:theme.spacing.md,
    paddingHorizontal: theme.spacing.lg,
    backgroundColor:"#fbfbfa",
  }
})

type Props = PropsWithChildren<{
  title: string
  listItemComponent: (item: ListItem) => JSX.Element
  rightComponent?: JSX.Element
}>

export const ContactListScreen: React.FC<Props> = ({
  title,
  listItemComponent,
  rightComponent,
  children
}: Props) => {
  const { t } = useTranslation()
  const navigation = useNavigation()
  const [isSearchOpen, setIsSearchOpen] = useState<boolean>(false)
  const [isPageLoadEnabled, setPageLoadEnabled] = useState(true)


  const [search, setSearch] = useState<string>("")
  const paramsString = getParamsFromObject({
    search: isSearchOpen ? search : ""
  }).toString()

  const ActionsBarComponent = (
    <Container direction="row" alignItems="center" noItemsStretch spacing={1}>
      <Icon
        name="Search"
        onPress={() => setIsSearchOpen(!isSearchOpen)}
        color={isSearchOpen ? "orange" : "iconRegulorColor"}
        isActive={isSearchOpen}
      />
      {rightComponent}
    </Container>
  )



  return (
    <ScreenLayoutList
    onLeftIconClick={()=>{
      if(isPageLoadEnabled === true) {
        setPageLoadEnabled(false)
        navigation.goBack() 
      }
    }}
      leftIconName="ArrowLeft"
      rightComponent={ActionsBarComponent}
      listQuery={useUserContacts}
      queryParams={paramsString}
      localization={{
        retry: t(""),
        noItemsAvailable: t("No contacts available."),
        title
      }}
      listItemComponent={listItemComponent}
      getSections={getSections}
      isSortAlphabetically={true}
    >
      {isSearchOpen && (

        <View style={styles.searchContainer}>
          <View style={styles.searchTextInput}>
            <TextFieldControlled
              value={search}
              onChange={setSearch}
              placeholder={t("Search User by Email or Phone number")}
              iconName="Search"
              autoFocus
              isClearable
            />
          </View>
      </View>
      )}
      {children}
    </ScreenLayoutList>
  )
}

const Contacts: React.FC = () => {
  const { t } = useTranslation()
  const [selectedContact, setSelectedContact] = useState<Contact | undefined>(
    undefined
  )
  const [addContactVisible, setAddContactVisible] = useState<boolean>(false)

  const rightComponent = (
    <Icon
      name="AddUser"
      onPress={() => {
        setSelectedContact(undefined)
        setAddContactVisible(true)
      }}
      color="regular"
    />
  )

  const listItemComponent = (item: ListItem) => (
    <ContactItem
      {...(item as Contact)}
      onPress={() => {
        setSelectedContact(item as Contact)
        setAddContactVisible(true)
      }}
    />
  )

  return (
    <ContactListScreen
      title={t("Contacts")}
      rightComponent={rightComponent}
      listItemComponent={listItemComponent}
    >
      <SaveContactModal
        isVisible={addContactVisible}
        onDismiss={() => setAddContactVisible(false)}
        contact={selectedContact}
      />
    </ContactListScreen>
  )
}

export default Contacts

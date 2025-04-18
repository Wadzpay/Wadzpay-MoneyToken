import React, { useState } from "react"
import { useTranslation } from "react-i18next"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import { LogBox, StyleSheet, View } from "react-native"

import ContactItem from "./ContactItem"

import { SendFundsStackNavigatorParamList } from "~/components/navigators"
import { ContactListScreen } from "~/screens/Menu/Contacts"
import { ListItem } from "~/components/ui/List"
import { Contact } from "~/api/models"
import SaveContactModal from "~/components/ui/SaveContactModal"
import { Container, Icon } from "~/components/ui"

type Props = NativeStackScreenProps<SendFundsStackNavigatorParamList, "SelectContact">

const SelectContact: React.FC<Props> = ({ route, navigation }: Props) => {
  const { t } = useTranslation()
  const {
    cognitoUsername,
    onCognitoUsernameChange,
    title = "Recipient Address"
  } = route.params
  const [addContactVisible, setAddContactVisible] = useState<boolean>(false)

  const rightComponent = (
    <Icon
      name="AddUser"
      onPress={() => {
        setAddContactVisible(true)
      }}
      color="regular"
    />
  )

  // Warning that a function as a screen param can break deep
  // linking and state persistance (doesn't apply to this screen)
  LogBox.ignoreLogs([
    "Non-serializable values were found in the navigation state"
  ])

  const styles = StyleSheet.create({
    contactListItemView:{
      marginLeft:0
    },
    separator:{
      height:1,
      width:"100%",
      marginVertical:1,
      backgroundColor:"#ECECEC"
    }
  })

  return (
    <ContactListScreen
      title={title}
      rightComponent={rightComponent}
      listItemComponent={(item: ListItem) => (
        <Container style={styles.contactListItemView} alignItems="flex-start"  justify="center">
        <ContactItem
          {...(item as Contact)}
          isSelected={item.id === cognitoUsername}
          onPress={() => {
            onCognitoUsernameChange(item.id as string)
            navigation.goBack()
          }}
        />
        <View style={styles.separator} />
        </Container>
      )}
    >
      <SaveContactModal
        isVisible={addContactVisible}
        onDismiss={() => setAddContactVisible(false)}
      />
    </ContactListScreen>
  )
}

export default SelectContact

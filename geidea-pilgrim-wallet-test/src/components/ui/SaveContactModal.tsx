import React, { useContext, useEffect, useState } from "react"
import { Alert, Platform, StyleSheet, View } from "react-native"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import { useTranslation } from "react-i18next"

import {
  Button,
  Container,
  DismissKeyboard,
  Icon,
  Modal,
  spacing,
  TextField,
  theme
} from "~/components/ui"
import { fieldProps, useValidationSchemas } from "~/constants"
import { SaveContactForm } from "~/constants/formTypes"
import { useAddUserContact, useDeleteContactOfUser, useUpdateUserContact, useUser, useUserByEmailOrMobile } from "~/api/user"
import { getParamsFromObject, isConsideredPhoneNumber } from "~/helpers"
import ContactItem from "~/screens/Menu/Contacts/ContactItem"
import Typography from "~/components/ui/Typography"
import ErrorModal from "~/components/ui/ErrorModal"
import { Contact, UserByEmailOrMobileData, UserData } from "~/api/models"
import { User } from "~/auth/AuthManager"
import { UserContext } from "~/context"
import List, { ListItem } from "./List"
import { CapitalizeFirstLetter, isIOS, showToast } from "~/utils"

const useSaveUserContact = (isEditing: boolean) => {
  
  const addUserContactQueryResult = useAddUserContact()
  const updateUserContactQueryResult = useUpdateUserContact()

  return isEditing ? updateUserContactQueryResult : addUserContactQueryResult
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: theme.spacing.md,
    marginTop: theme.spacing.xs,
    paddingBottom: theme.spacing.lg
  }
  ,
  icon: {
    flex:1,
    marginTop: -120,
    marginLeft: 320,
  },
  iconContainer: {
    padding: theme.spacing.xs
  },
  contactRow: {
    marginHorizontal: theme.spacing.md,
    marginVertical: spacing(0.75) // 6px
  },
  avatar: {
    padding: spacing(0.5),
    marginRight: theme.spacing.xs
  },
  separator:{
    height:1,
    width:"100%",
    marginVertical:1,
    backgroundColor:"#ECECEC"
  }
})

type Props = {
  onDismiss: () => void
  isVisible: boolean
  contact?: Contact
}

function getCountryCode(number: string) {
  if (number?.startsWith("+91")) {
    return "+91"
  } else if (number?.startsWith("+62")) {
    return "+62"
  } else if (number?.startsWith("+63")) {
    return "+63"
  } else if (number?.startsWith("+65")) {
    return "+65"
  } else if (number?.startsWith("+421")) {
    return "+421"
  } else if (number?.startsWith("+92")) {
    return "+92"
  } else if (number?.startsWith("+971")) {
    return "+971"
  } else if (number?.startsWith("+44")) {
    return "+44"
  } else {
    return "+65"
  }
}

const SaveContactModal: React.FC<Props> = ({
  onDismiss,
  isVisible,
  contact
}: Props) => {
  const { t } = useTranslation()
  const isEditing = contact != undefined
  const { user } = useContext(UserContext)

  const { saveContactSchema } = useValidationSchemas()
  const {
    control,
    formState: { errors, isValid, isDirty },
    handleSubmit,
    watch,
    getValues,
    setValue: setFormValue,
    reset: resetForm,
    register
  } = useForm<SaveContactForm>({
    resolver: yupResolver(saveContactSchema),
    mode: "onChange"
  })

  register("userId") // Necessary to update validation if the value changes
  useEffect(() => {
    resetForm({
      nickname: contact?.nickname ?? "",
      userId: contact?.cognitoUsername ?? "",
      search: "",
      query: ""
    })
  }, [contact])

  const [search, query, nickname] = watch(["search", "query", "nickname"])
  const hitAPI = contact?.email ? true : false
  const { data: foundUser, isSuccess, isFetching, isFetched }  = useUser(`email=${contact?.email}`, hitAPI)

  const {
    mutate: saveUserContact,
    isLoading: saveContactLoading,
    error: saveContactError,
    isSuccess: saveContactSuccess,
    reset: saveContactReset
  } = useSaveUserContact(isEditing)


  const { 
    data: userByEmailMobileData, 
    isSuccess : isUserByEmailMobileSuccess ,
    refetch:  userByEmailMobileRefetch , 
    isFetching : isUserByEmailMobileFetching , 
    isFetched : isUserByEmailMobileFetched, 
    error:   userByEmailMobileError
  } = useUserByEmailOrMobile(query)

  const userList : UserByEmailOrMobileData | {} = userByEmailMobileData 

  const userData: UserData = ((contact as unknown) as UserData) ?? foundUser

  const { 
    mutate: deleteContact, 
    isLoading: isdeleteContactLoading, 
    error: isDeleteContactError, 
    isSuccess: isDeleteContactSuccess 
  } = useDeleteContactOfUser()

  useEffect(() => {
    setFormValue("userId", isSuccess ? foundUser?.cognitoUsername : "", {
      shouldValidate: true
    })
  }, [isSuccess])

  useEffect(() => {
  }, [userByEmailMobileError])

  useEffect(() => {
    const timeout = setTimeout(() => {
      const queryObj: { email?: string; phoneNumber?: string } = {}
      if (isConsideredPhoneNumber(search) || !isNaN(parseInt(search))) {
        let searchTemp = search
        if (!search.toString().startsWith("+") && user != null) {
          searchTemp =
            getCountryCode(user?.attributes?.phone_number) + search.toString()
        }
        queryObj["phoneNumber"] = searchTemp.toString().toLowerCase()
      } else {
        queryObj["email"] = search?.toString().toLowerCase()
      }
    
      const queryString = getParamsFromObject(queryObj).toString()
    
      setFormValue("query", queryString)

    }, 500)

    return () => clearTimeout(timeout)
  }, [search])
//   const OnChangeText = (text:any) => {

//     const queryObj: { email?: string; phoneNumber?: string } = {}
//       if (isConsideredPhoneNumber(text) || !isNaN(parseInt(text))) {
//         let searchTemp = text
//         if (!text.toString().startsWith("+") && user != null) {
//           searchTemp =
//             getCountryCode(user?.attributes?.phone_number) + text.toString()
//         }
//         queryObj["phoneNumber"] = text
//       } else {
//         queryObj["email"] = text?.toString().toLowerCase()
//       }
    
//       const queryString = getParamsFromObject(queryObj).toString()
    
//       setFormValue("query", queryString)
//   }

  useEffect(() => {

    if (saveContactSuccess) 
      _onDismiss()

  }, [saveContactSuccess])

  const _onDismiss = () => {
    saveContactReset()
    resetForm()
    onDismiss()
  }

  const onSaveContact = () => {
    let { nickname, userId: cognitoUsername } = getValues()
    nickname = CapitalizeFirstLetter(nickname.toLocaleLowerCase())
    saveUserContact({
      nickname,
      cognitoUsername
    })
  }
/*
* TODO :  Nandani 
  Implementations of edit , delete and add contacts 
*/


useEffect(() => {
  if (saveContactSuccess) {
    if (isEditing) {
      // show Toast / alert
      const txt = "Contact updated successfully"
      isIOS ? alert(txt) : showToast(txt)
    } else {
      const txt = "Contact added successfully"
      isIOS ? alert(txt) : showToast(txt)
    }

    const timer = setTimeout(() => {
      _onDismiss()
    }, 2000)
    return () => clearTimeout(timer)
  }
}, [saveContactSuccess])


useEffect(() => {
  if (isDeleteContactSuccess) {

    // show Toast / alert
    const txt = "Contact deleted successfully"
    isIOS ? alert(txt) : showToast(txt)
    
    const timer = setTimeout(() => {
      _onDismiss()
    }, 2000)
    return () => clearTimeout(timer)
  } 
}, [isDeleteContactSuccess,])

useEffect(() => {
  if (isDeleteContactError) {
    // show toast / alert
    const txt = "Contact could not be deleted"
    isIOS ? alert(txt) : showToast(txt)
  }
}, [isDeleteContactError])

// useEffect(() => {
//   if (userByEmailMobileData && Object.keys(userList).length === 1 ) {
//                setFormValue("userId", userList[0]?.cognitoUsername)
//         setFormValue("search", userList[0]?.email)
//   }
// }, [isUserByEmailMobileSuccess])


const onEditContact = () => {
    
  const { nickname, userId: cognitoUsername } = getValues()
  // if(nickname.trim().length > 0) {
     saveUserContact({
       nickname,
       cognitoUsername
     })
  //  } else {
  //    !errors.nickname  && Alert.alert("", "Nickname cannot be Blank ")
  //   }
 }

 const listItemComponent = (item: ListItem) => (
  <Container>
    <ContactItem
    nickname={nickname != "" ? nickname : t("New Contact")}
    {...(item as Contact)}
    onPress={() => {
      setFormValue("userId", item?.cognitoUsername)
      setFormValue("search", item?.email)
    }}
    iconName="AddUser"
  />
  <View style={styles.separator} />
  </Container>

)

const deleteContactConfirmation = () => {
  Alert.alert(
    t("Delete Contact"),
    t("Do you really wish to delete the contact?"),
    [
      {
        text: t("Cancel"),
        style: "cancel"
      },
      {
        text: t("Delete"),
        onPress: () => deleteContact({
          nickname: contact?.nickname ? contact?.nickname : ""
        }),
        style: "destructive"
      }
    ],
    {
      cancelable: true
    }
  )
}

/*
  Implementations of edit , delete and add contacts 
*/
  return (
    <Modal
      title={isEditing ? t("Edit Contact") : t("Add Contact")}
      variant="bottom"
      isVisible={isVisible}
      onDismiss={_onDismiss}
      dismissButtonVariant="cancel"
      contentStyle={{ height: theme.modalFullScreenHeight }}
    >
      <ErrorModal error={saveContactError} />
      <DismissKeyboard>
        <Container justify="space-between" style={styles.container}>
          <Container
            spacing={4}
            noItemsStretch
            alignItems="center"
            justify="space-between"
          >
            {!isEditing && (
              <TextField
                label={t("Search User by Email or Phone number")}
                keyboardType={Platform.OS == 'ios' ? "ascii-capable": "visible-password"}
                name="search"
                control={control}
                maxLength={50}
                autoFocus
                error={errors.search}
                placeholder={"Email or Phone number  "}
                iconName={
                  search
                    ? isConsideredPhoneNumber(search)
                      ? "Phone"
                      : "Email"
                    : "Search"
                }
                isLoading={isFetching}
                {...fieldProps.email}
              />
            )}

            <TextField
              label={t("Nickname")}
              name="nickname"
              control={control}
              error={errors.nickname}
              maxLength={50}
              keyboardType={Platform.OS == 'ios' ? "ascii-capable": "visible-password"}
              placeholder={t("Enter New Nickname   ")}
              {...fieldProps.nickname}
            />

            {/* Not required here for validation
             <Typography color={"orange"} textAlign={"left"}>
              {" "}
              {t("Please enter a nickname")}
            </Typography> */}

            
            <Container direction="row" alignItems="center" justify="space-around"  noItemsStretch>
            {userData ? (
              <Container  style={
                {
                  flexDirection:"row",
                  justifyContent:"space-around"
                }
              }>
                <ContactItem
                  nickname={nickname != "" ? nickname : t("New Contact")}
                  {...userData}
                  iconName={isEditing ? undefined : "AddUser"}
                />
              </Container>
              ) : undefined}

            {(userData && isEditing) && (              
              // add delete icon here
              <Icon
                name="RemoveContact"
                size="md"
                onPress={() => {
                  // show an alert
                  isEditing ? deleteContactConfirmation() : undefined
                }}
                color="iconRegulorColor"
              />
            )}
            </Container>

            {!isEditing && userList ? (
                  <Container 
                    alignItems="center"
                    style={
                      {
                        width:380,
                        height: 250
                      }
                    }>
                        <List
                            data={search.length > 2 ?  userList || [] : []}
                            refreshing={isUserByEmailMobileFetching}
                            onRefresh={userByEmailMobileRefetch}
                            getSections={undefined}
                            keyExtractor={(item, index) => String(index)}
                            listItemComponent={listItemComponent}
                            localization={{
                              retry: t(""),
                              noItemsAvailable: t("No user was found for the given contact information."),
                            }}
                        />
              </Container>
              ) : undefined}
           
          </Container>

          <Button
            text={isEditing ? t("Save Changes") : t("Add Contact")}
            onPress={isEditing ? onEditContact : onSaveContact}
            disabled={isEditing ? false : userList && Object.keys(userList).length > 0 ? false : true} // to disable add contact button if no user is found 
            textVariant={"body"}          
          />
        </Container>
      </DismissKeyboard>
    </Modal>
  )
}

export default SaveContactModal
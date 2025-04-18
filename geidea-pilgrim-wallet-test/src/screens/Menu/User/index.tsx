import React, { useContext, useEffect,useState } from "react"
import { useTranslation } from "react-i18next"
import {
  Alert,
  TouchableOpacity,
  StyleSheet,
  View,
  Dimensions
} from "react-native"
import { NativeStackScreenProps } from "@react-navigation/native-stack"


import { MenuStackParamList } from "~/components/navigators"
import {
  Typography,
  ErrorModal,
  LoadingSpinner,
  Container,
  ScreenLayoutTab,
  theme,
  Icon
} from "~/components/ui"
import { useDeleteUser, useGetUserProfileDetails, useSignOut } from "~/api/user"
import { UserContext } from "~/context"
import { formatPhoneNumber, isProd } from "~/utils"
import { useSendPhoneOTP, useSendPhoneOtpCommon } from "~/api/otp"

const { width } = Dimensions.get("window")

const styles = StyleSheet.create({
  container: {
    marginHorizontal: theme.spacing.md,
    marginTop:30
  },
  separatorContainer: {
    display: "flex",
    alignItems: "center"
  },
  separator: {
    backgroundColor: theme.colors.gray.light,
    width: width - theme.spacing.xl * 2,
    height: 1,
    marginBottom: 13,
    marginTop: 13
  },
  contentBottom: {
    marginTop: 370,
    marginBottom: 30
  },
  signOut: {
    color: "black",
    fontSize: 20,
    fontWeight: "200"
  },
  containerBackdrop: {
    marginVertical:5,
    borderRadius: 15,
    paddingVertical:25,
    backgroundColor: "#f6f6f6",
    paddingHorizontal:20
  }
})

type Props = NativeStackScreenProps<MenuStackParamList, "User">

const User: React.FC<Props> = ({ navigation }: Props) => {
  const { t } = useTranslation()
  const { user } = useContext(UserContext)
  const [passcodeText, setPasscodeText ] = useState("")
  const userMobilenumber = user?.attributes?.phone_number
  const userEmailAddress = user?.attributes?.email
  
  const [isPageLoadEnabled, setPageLoadEnabled] = useState(true)

  const {
    data: profileData,
    isFetching: isFetchingProfileDetails,
    error: errorProfileDetails
  } = useGetUserProfileDetails()

  useEffect(() => {
   // Check if passcode is set for the user or not 
   // console.log("profileData", profileData)
    if(profileData?.isPasscodeSet){
      setPasscodeText("Change/Forgot Passcode")
    } else {
      setPasscodeText("Set Passcode")
    }
  }, [profileData])

  const {
    mutate: sendPhoneOTP,
    isLoading,
    error,
    isSuccess
  } = useSendPhoneOtpCommon()

  useEffect(() => {
    if (isSuccess) {

      navigation.navigate("VerifyPhoneOtp", {
        screenType: "phone",
        target: userMobilenumber?.toString() || ""
      })
    }
  }, [isSuccess])

  // useEffect(() => {
  //   navigation.getParent()?.setOptions({
  //     tabBarStyle: {
  //       display: "none"
  //     }
  //   });
  //   return () => navigation.getParent()?.setOptions({
  //     tabBarStyle: undefined
  //   });
  // }, [navigation]);

  const onNext = () => {
  // navigation.navigate("PasscodeScreen")
    sendPhoneOTP({
      phoneNumber: formatPhoneNumber(userMobilenumber?.toString() || "")
    })
  }

  // const { mutate: deleteUser, isLoading, error, isSuccess } = useDeleteUser()
 // const { mutate: signOut } = useSignOut()

  // useEffect(() => {
  //   if (isSuccess) {
  //     signOut()
  //   }
  // }, [isSuccess])

  // const deleteUserConfirmation = () => {
  //   Alert.alert(
  //     t("Delete Account"),
  //     t("Do you really want to delete your account as you will lose all data"),
  //     [
  //       {
  //         text: t("Cancel"),
  //         style: "cancel"
  //       },
  //       {
  //         text: t("Delete"),
  //         onPress: () => deleteUser(""),
  //         style: "destructive"
  //       }
  //     ],
  //     {
  //       cancelable: true
  //     }
  //   )
  // }

  return (
    <ScreenLayoutTab
      title={t("User Details")}
      leftIconName="ArrowLeft"
      onLeftIconClick={()=>{
        if(isPageLoadEnabled === true) {
          setPageLoadEnabled(false)
          navigation.goBack() 
        }
      }}
      // rightComponent={
      //   <Container
      //     direction="row"
      //     alignItems="center"
      //     spacing={1}
      //     noItemsStretch
      //   >
      //     <Typography style={styles.signOut}>{t("Sign Out")}</Typography>
      //     <Icon name="LogOut"/>
      //   </Container>
      // }
      // onRightIconClick={signOut}
    >
      {/* <ErrorModal error={error} /> */}

      <Container spacing={2} style={styles.container}>
        <Container direction="row" justify="space-between" style={styles.containerBackdrop}>
          <Typography variant="heading" fontFamily="Rubik-Regular">{t("Email")}:</Typography>
          <Typography variant="heading" fontFamily="Rubik-Regular" >{userEmailAddress}</Typography>
        </Container>

        <Container direction="row" justify="space-between"  style={styles.containerBackdrop}>
          <Typography variant="heading" fontFamily="Rubik-Regular" >{t("Phone number")}:</Typography>
          <Typography variant="heading" fontFamily="Rubik-Regular" >
            {userMobilenumber}
          </Typography>
        </Container>

        {/* <View style={styles.contentBottom}>
          <TouchableOpacity
            onPress={() => onNext()}
          >
            <Typography color="iconFocused">{passcodeText}</Typography>
          </TouchableOpacity>
        </View> */}
        
      </Container>
    </ScreenLayoutTab>
  )
}

export default User
export { default as ChangePassword } from "./ChangePassword"

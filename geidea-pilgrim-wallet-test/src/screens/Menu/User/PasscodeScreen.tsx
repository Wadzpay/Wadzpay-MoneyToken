import 'react-native-get-random-values'
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import React, { useState ,useEffect, useRef} from "react"
import { MenuStackParamList } from "~/components/navigators"
import { BackHandler, Dimensions, Image, ImageBackground, SafeAreaView, StatusBar, Text, View } from "react-native";
import { Alert } from "react-native";
import { Container, ErrorModal, Icon, Modal, ScreenLayoutTab, Typography, theme } from "~/components/ui";
import ReactNativePinView from "react-native-pin-view"
import { useGetPasscodeTitle, useSavePasscodeHash } from "~/api/user";
import { AES, enc, } from 'crypto-js';
import AesUtil from '~/AesUtil';
import { Button } from 'react-native-elements';


type Props = NativeStackScreenProps<MenuStackParamList, "PasscodeScreen">

const width = Dimensions.get('window').width;
const height = Dimensions.get('window').height;

const PasscodeScreen: React.FC<Props> = ({ navigation }: Props) => {

    const [showPinLock, setShowPinLock ] = useState(true)
   // const [PINCodeStatus, setPINCodeStatus ] = useState("choose")
   const pinView = useRef(null)
   const [showRemoveButton, setShowRemoveButton] = useState(false)
   const [showError, setShowError] = useState(false)
   const [enteredPin, setEnteredPin] = useState("")
   const [createPasscode, setCreatePasscode] = useState("")
   const [confirmPasscode, setConfirmPasscode] = useState("")
   const [title, setTitle] = useState("Create Passcode")
   const [subTitle, setSubTitle] = useState("")
   const [pinLength, setPinLength] = useState("6")
   const [showCompletedButton, setShowCompletedButton] = useState(false)
   const [showSuccessModal, setShowSuccessModal] = useState(false) 
//    const [ secret, setSecret] = useState("test key")
//    const [ cipher, setCipher] = useState("")
//    const [ decrypted, setDecrypted] = useState("")
   const {
    mutate: savePasscodeHash,
    isLoading: savePasscodeHashcodeLoading,
    error: savaPasscodeHashcodeError,
    isSuccess: savaPasscodeHashcodeSuccess,
    data: savaPasscodeHashcodeData
  } = useSavePasscodeHash()

  const {
    data: passcodeTitle,
    isFetching: isFetchingPasscodeTitle,
    error: errorPasscodeTitle
  } = useGetPasscodeTitle()

  useEffect(()=> {
    console.log("***" , passcodeTitle)
    if(passcodeTitle) {
      setTitle(passcodeTitle?.passcodeScreen?.createPasscode?.title || "Create Passcode")
        setSubTitle(passcodeTitle?.passcodeScreen?.createPasscode?.subtitle || "")
    }
  },[passcodeTitle])

  var CryptoJS = require('crypto-js');

  const encryptData = (plainText: string) => {
    var iv = CryptoJS.lib.WordArray.random(128/8).toString(CryptoJS.enc.Hex);
        var salt = CryptoJS.lib.WordArray.random(128/8).toString(CryptoJS.enc.Hex);

        var aesUtil = new AesUtil(128, 1000);
        var ciphertext = aesUtil.encrypt(salt, iv, "passcode",  plainText);

        // console.log("original text :: ",plainText )
        // console.log("encrpted text -->" , iv + "::" + salt + "::" + ciphertext)
        //  var decryptText = aesUtil.decrypt(salt, iv, "passcode", ciphertext)
        // console.log("decrypted text --> ",decryptText )
        
        return (iv + "::" + salt + "::" + ciphertext);
        


  }

  useEffect(() => {
    if(savaPasscodeHashcodeSuccess) {
        setShowSuccessModal(true)
        setTitle(passcodeTitle?.passcodeScreen?.createPasscode?.title || "Create Passcode")
        setSubTitle(passcodeTitle?.passcodeScreen?.createPasscode?.subtitle || "")
        setEnteredPin("")
        setConfirmPasscode("")
        setCreatePasscode("")
        pinView.current.clearAll()
    }
  }, [savaPasscodeHashcodeSuccess])

   useEffect(() => {
     if (enteredPin.length > 0) {
       setShowRemoveButton(true)
     } else {
       setShowRemoveButton(false)
     }
     if (enteredPin.length == 6) {
       setShowCompletedButton(true)
     } else {
       setShowCompletedButton(false)
     }
   }, [enteredPin])

   useEffect(() => {
    if(title === "Create Passcode" && showCompletedButton === true) {
        setTitle(passcodeTitle?.passcodeScreen?.confirmPasscode?.title || "Confirm Passcode")
        setSubTitle(passcodeTitle?.passcodeScreen?.confirmPasscode?.subtitle || "")
        setCreatePasscode(enteredPin)
        setEnteredPin("")
        pinView.current.clearAll()
        setShowCompletedButton(false)
    } 

    if(title === "Confirm Passcode" && showCompletedButton === true) {
       setConfirmPasscode(enteredPin)
        if(createPasscode == enteredPin) {
            // API HIT TO SET PASSCODE 
            // const cipherText = AES.encrypt(createPasscode, secret);
            // setCipher(cipherText.toString())
            // console.log("cipherText" , cipherText.toString())
             
            savePasscodeHash({
                passcodeHash: encryptData(createPasscode)
              })
            // API HIT TO SET PASSCODE

        }else {
            setShowError(true)
        }
    }
    
  }, [showCompletedButton])
  

  const navigateToUserScreen = () => {
    navigation.navigate("Menu")
  }

  useEffect(() => {
    const backAction = () => {
        navigation.navigate("Menu")
      return true
    }

    const backHandler = BackHandler.addEventListener(
      "hardwareBackPress",
      backAction
    )

    return () => backHandler.remove()
  }, [])
  
    // const finishProcess= async () => {
    //     const hasPin = await hasUserSetPinCode();
    //     if (hasPin) {
    //       Alert.alert("", "You have successfully set/entered your pin.", [
    //         {
    //           title: "Ok",
    //           onPress: () => {
    //             // do nothing
    //           },
    //         },
    //       ]);
    //       setShowPinLock(false);
    //     }
    //   };
      return (
        <ScreenLayoutTab
          title={title}
          leftIconName="ArrowLeft"
          onLeftIconClick={() => {
            navigateToUserScreen();
          }}>
          <SafeAreaView
            style={{
              flex: 1,
              backgroundColor: theme.colors.white,
              justifyContent: 'center',
              alignItems: 'center',
              marginTop: 30,
            }}>
            <Text
              style={{
                paddingTop: 24,
                color: '#1D2D3A',
                fontFamily: 'Rubik-Medium',
                fontSize: theme.fontSize.subtitle,
              }}>
              {title}
            </Text>
            <Text
              style={{
                paddingTop: 4,
                paddingBottom: 48,
                color: '#1D2D3A',
                fontFamily: 'Rubik-Light',
                fontSize: theme.fontSize.chip,
                textAlign:'center'
              }}>
              {subTitle}
            </Text>
            <ReactNativePinView
              inputSize={25}
              ref={pinView}
              pinLength={Number(pinLength)}
              buttonSize={70}
              onValueChange={value => setEnteredPin(value)}
              buttonAreaStyle={{
                marginTop: 40,
              }}
              inputAreaStyle={{
                marginBottom: 90,
              }}
              inputViewEmptyStyle={{
                backgroundColor: 'transparent',
                borderWidth: 1,
                borderColor: theme.colors.black,
              }}
              inputViewFilledStyle={{
                padding: 5,
                backgroundColor: showError
                  ? theme.colors.error
                  : theme.colors.orange,
              }}
              buttonViewStyle={{
                borderWidth: 0,
                borderColor: theme.colors.orange,
              }}
              buttonTextStyle={{
                fontSize: 30,
                fontWeight: 400,
                color: theme.colors.darkBlack,
              }}
              onButtonPress={key => {
                // if (key === "custom_left") {
                //     Alert.alert("Entered Pin: " + enteredPin)
                // }
                if (key === 'custom_right') {
                  pinView.current.clear();
                }
                // if (key === "three") {
                //     Alert.alert("You just click to 3")
                // }
              }}
              customRightButton={
                showRemoveButton ? (
                  <Icon name="Backspace" color="black" size="lg" />
                ) : undefined
              }
              //customRightButton={showCompletedButton ? <Icon name={"ios-unlock"} size={36} color={"#FFF"} /> : undefined}
            />
            {showSuccessModal && (
              <Modal
                variant="center"
                isVisible={showSuccessModal}
                onDismiss={() => {
                  setShowSuccessModal(false);
                  navigateToUserScreen();
                }}
                swipeDirection={['down']}
                dismissButtonVariant="none">
                <Container spacing={1}>
                  <View
                    style={{justifyContent: 'center', alignItems: 'center'}}>
                    <ImageBackground
                      source={require('~images/successful_menu.png')}
                      style={{
                        height: 50,
                        width: 50,
                      }}>
                      <View
                        style={{
                          flex: 1,
                          justifyContent: 'center',
                          alignItems: 'center',
                          margin: 0,
                        }}>
                        <Image
                          source={require('~images/tick_mark.png')}
                          style={{
                            justifyContent: 'center',
                            alignItems: 'center',
                            height: 20,
                        width: 20,
                          }}
                        />
                      </View>
                    </ImageBackground>
                  </View>

                  <Typography
                    variant="body"
                    fontFamily="Rubik-Medium"
                    style={{marginVertical: 5,}}>
                      {savaPasscodeHashcodeData?.message}
                  </Typography>
                  <Button
                  title="Ok"
                  onPress={() => 
                    {setShowSuccessModal(false)
                    navigateToUserScreen()}}
              buttonStyle={{ backgroundColor: theme.colors.orange, alignItems:'center',justifyContent:'center' }}
              containerStyle={{
                height: 40,
                width: 120,
                borderRadius: 6,
                marginHorizontal: 50,
                marginVertical: 10,
                alignSelf:'center'
              }}
              titleStyle={{
                color: 'black',
                marginHorizontal: 20,
              }}
                  
                  />
                </Container>
              </Modal>
            )}
            {showError && (
              <Modal
                variant="center"
                isVisible={showError}
                onDismiss={() => {
                  setTitle(passcodeTitle?.passcodeScreen?.createPasscode?.title || "Create Passcode")
                  setSubTitle(passcodeTitle?.passcodeScreen?.createPasscode?.subtitle || "")
                  setEnteredPin("")
                  pinView.current.clearAll()
                  setShowCompletedButton(false)
                  setShowError(false)
                }}
                swipeDirection={['down']}
                dismissButtonVariant="none">
                <Container spacing={1}>
                  <View
                    style={{justifyContent: 'center', alignItems: 'center'}}>
                    <ImageBackground
                      source={require('~images/transaction_failed.png')}
                      style={{
                        height: 70,
                        width: 70,
                      }}>
                    </ImageBackground>
                  </View>

                  <Typography
                    variant="body"
                    fontFamily="Rubik-Medium"
                    style={{marginVertical: 5,}}>
                      your passcode doesn't match.
                  </Typography>
                  <Button
                  title="Ok"
                  onPress={() => 
                    {
                    setTitle(passcodeTitle?.passcodeScreen?.createPasscode?.title || "Create Passcode")
                    setSubTitle(passcodeTitle?.passcodeScreen?.createPasscode?.subtitle || "")
                    setEnteredPin("")
                    pinView.current.clearAll()
                    setShowCompletedButton(false)
                    setShowError(false)
                    }}
              buttonStyle={{ backgroundColor: theme.colors.orange, alignItems:'center',justifyContent:'center' }}
              containerStyle={{
                height: 40,
                width: 120,
                borderRadius: 6,
                marginHorizontal: 50,
                marginVertical: 10,
                alignSelf:'center'
              }}
              titleStyle={{
                color: 'black',
                marginHorizontal: 20,
              }}
                  
                  />
                </Container>
              </Modal>
            )}

            {savaPasscodeHashcodeError && (
              <ErrorModal error={savaPasscodeHashcodeError} />
            )}
          </SafeAreaView>
        </ScreenLayoutTab>
      );
    // return (
    //     showPinLock && (
    //         <PINCode
    //           status={"choose"}
    //           touchIDDisabled={true}
    //           finishProcess={() => finishProcess()}
    //           passwordLength={6}
    //           titleChoose={"Create Passcode"}
    //           titleConfirm={"Confirm Passcode"}
    //           subtitleChoose={" "}
    //           colorPassword={theme.colors.black}
    //           colorCircleButtons= {theme.colors.orange}
    //           colorPasswordEmpty= {theme.colors.blackishGray}
    //           styleMainContainer= {
    //             {flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor:theme.colors.white }
    //           }
    //           stylePinCodeCircle= {
    //             {width:10, height:10, borderRadius: 5}
    //           }
    //           numbersButtonOverlayColor={theme.colors.gray.light}
    //           stylePinCodeColorTitle={theme.colors.darkBlack}
    //           stylePinCodeTextTitle = {{fontSize: theme.fontSize.subtitle, fontWeight: '650', textAlign: 'center', fontFamily: theme.fontFamilyhelvetica}}
    //           stylePinCodeButtonCircle={{ borderColor: "black", borderWidth: 0.5, backgroundColor:"white"}}
    //           stylePinCodeButtonNumber={theme.colors.orange}
    //           stylePinCodeDeleteButtonIcon={"~images/arrow.png"}
    //           iconButtonDeleteDisabled={true}
    //           stylePinCodeDeleteButtonText={{fontWeight: '500', marginTop: 15, fontSize: 18, color: theme.colors.black}}
    //           stylePinCodeDeleteButtonColorShowUnderlay={theme.colors.orange}
    //           stylePinCodeDeleteButtonColorHideUnderlay={theme.colors.black}
    //           disableLockScreen={true}
    //           buttonDeleteText="Delete"
    //         />
    //       )
    //   )

}

export default PasscodeScreen
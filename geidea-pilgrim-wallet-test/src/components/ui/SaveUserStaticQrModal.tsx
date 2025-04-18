import React, { useContext, useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"

import Typography from "./Typography"
import Modal from "./Modal"
import Container from "./Container"
import { Alert, TouchableOpacity, View, Platform, PermissionsAndroid } from "react-native"
import theme from "./theme"
import Icon from "./Icon"
import QrCodeCardCarousel from "~/screens/ReceiveFunds/QrCodeCardCarousel"
import ViewShot, { captureRef } from "react-native-view-shot"
import { UserContext } from "~/context"

import {CameraRoll} from '@react-native-camera-roll/camera-roll';
import { showToast } from "~/utils"

type Props = {
    subtitle?: string
    message?: string
    onDissmiss?: () => void
    amount?: number;
    walletAddresses?: string;
    hasPermission?: any
}

const SaveUserStaticQrModal: React.FC<Props> = ({ subtitle, message, onDissmiss , amount = 0,
    walletAddresses, hasPermission= true }: Props) => {
  const { t } = useTranslation()
  const [isVisible, setIsVisible] = useState(false)
  const viewShotRef = useRef();
  const {user ,setSaveStaticQrCodeToGallery} = useContext(UserContext);

  useEffect(() => {
    setIsVisible(!!message)
  }, [message])


  async function hasAndroidPermission() {
    const getCheckPermissionPromise = () => {
      if (Platform.Version >= 33) {
        return Promise.all([
          PermissionsAndroid.check(
            PermissionsAndroid.PERMISSIONS.READ_MEDIA_IMAGES,
          ),
          PermissionsAndroid.check(
            PermissionsAndroid.PERMISSIONS.READ_MEDIA_VIDEO,
          ),
        ]).then(
          ([hasReadMediaImagesPermission, hasReadMediaVideoPermission]) =>
            hasReadMediaImagesPermission && hasReadMediaVideoPermission,
        );
      } else {
        return PermissionsAndroid.check(
          PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
        );
      }
    };

    const hasPermission = await getCheckPermissionPromise();
    if (hasPermission) {
      return true;
    }
    const getRequestPermissionPromise = () => {
      if (Platform.Version >= 33) {
        return PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.READ_MEDIA_IMAGES,
          PermissionsAndroid.PERMISSIONS.READ_MEDIA_VIDEO,
        ]).then(
          statuses =>
            statuses[PermissionsAndroid.PERMISSIONS.READ_MEDIA_IMAGES] ===
              PermissionsAndroid.RESULTS.GRANTED &&
            statuses[PermissionsAndroid.PERMISSIONS.READ_MEDIA_VIDEO] ===
              PermissionsAndroid.RESULTS.GRANTED,
        );
      } else {
        return PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
        ).then(status => status === PermissionsAndroid.RESULTS.GRANTED);
      }
    };

    return await getRequestPermissionPromise();
  }

  useEffect(() => {
    hasAndroidPermission();
  }, []);



  async function savePicture() {
  if (Platform.OS === 'android' && !(await hasAndroidPermission())) {
      return;
    }

  

    const uri = await captureRef(viewShotRef, {
      format: 'png',
      quality: 0.8,
    });

    //console.log("uri" , uri)

    const image = CameraRoll.save(uri, {type: 'photo', album: 'ss'});
    if (await image) {
        setSaveStaticQrCodeToGallery(true)
        showToast('Image saved successfully!')
        setIsVisible(false)
    //   Alert.alert(
    //     '',
    //     'Image saved successfully.',
    //     [{text: 'OK', onPress: () => {}}],
    //     {cancelable: false},
    //   );
    }
  }

  return (
    <Modal
      variant="center"
      contentStyle={{
        height: theme.modalFullScreenHeight
      }}
      isVisible={isVisible}
      onDismiss={() => setIsVisible(false)}
      swipeDirection={["down"]}
      dismissButtonVariant="button"
    >
      <Container
          justify="space-between"
          alignItems="center"
          noItemsStretch
          style={{
            flex: 1,
            marginLeft: 20,
            marginRight: 20,
            marginBottom: theme.spacing.xs,
          }}>
          {/*  ************************ QR Code ************************  */}

          <ViewShot
            style={{backgroundColor: theme.colors.white, paddingTop: 30}}
            ref={viewShotRef}
            options={{format: 'jpg', quality: 0.8}}>
            <Container
              direction="column"
              justify="space-between"
              noItemsStretch
              alignItems="center"
              //style={{ width: 300, height: 300, backgroundColor: "white", borderColor:"#EDEDED", borderWidth:1, borderRadius:8, padding:4}}
            >
              {/* Show QR code here inside this container */}

              <QrCodeCardCarousel
                walletAddress={walletAddresses}
                amount={amount}
                digitalAsset={'SART'}
              />

              <Container
                alignItems="center"
                justify="center"
                style={{marginTop:30,
                marginBottom:10}}>
                <Typography
                  style={{marginLeft: 30}}
                  variant="label"
                  fontFamily="Rubik-Medium"
                  textAlign="left">
                  User Name
                </Typography>

                <Container
                  direction="row"
                  alignItems="center"
                  justify="center"
                  style={{
                    marginTop: 10,
                    borderWidth: 2,
                    borderRadius: 8,
                    backgroundColor: '#F6F6F6',
                    borderColor: '#EDEDED',
                    height:55,
                    paddingLeft: 20,
                    paddingRight: 25,
                    marginLeft: 30,
                    marginRight: 30,
                  }}>
                  <Typography
                    variant="heading"
                    style={{
                      width: 300,
                      marginTop: 10,
                      paddingRight: 10,
                    }}
                    textAlign="left">
                    {user?.attributes.email}
                  </Typography>
                  
                </Container>
              </Container>

             
            </Container>
            </ViewShot>
         

          {/*  ************************ Copy and Share buttons style 1 ************************  */}

          {/* Container for Wallet Address and copy to clipboard button */}

          {/* Container for Share Button */}

          <Container
            direction="column"
            justify="space-around"
            style={{marginVertical: 10}}>
            <Container
              style={{
                width: 200,
                height: 100,
              }}>
              <TouchableOpacity
                style={[
                  {
                    borderColor: theme.colors.black,
                    borderRadius: 6,
                    borderWidth: 1,
                    height: 50,
                    width: '100%',
                    alignItems: 'center',
                    justifyContent: 'center',
                    // position: 'absolute',
                    top: 0,
                  },
                  {backgroundColor: theme.colors.white},
                ]}
                onPress={() => {
                  setSaveStaticQrCodeToGallery(true)
                  setIsVisible(false)
                  }}>
                <Typography
                  variant="label"
                  textAlign="center"
                  fontFamily="Rubik-Medium">
                  Save Later
                </Typography>
              </TouchableOpacity>
            </Container>

            <Container
              style={{
                width: 200,
                height: 100,
              }}>
              <TouchableOpacity
                style={[
                  {
                    borderColor: theme.colors.black,
                    flexDirection: 'row',
                    borderRadius: 6,
                    borderWidth: 0,
                    height: 50,
                    width: '100%',
                    alignItems: 'center',
                    justifyContent: 'center',
                  },
                  {backgroundColor: theme.colors.orange},
                ]}
                onPress={() => {
                 savePicture();
                }}>
                <Icon name={'SaveIcon'} color={'iconRegulorColor'}></Icon>
                <Typography
                  style={{marginLeft: 10}}
                  variant="label"
                  textAlign="center"
                  fontFamily="Rubik-Medium">
                  Save To Gallery
                </Typography>
              </TouchableOpacity>
            </Container>
            
          </Container>
        </Container>
    </Modal>

  )
}

export default SaveUserStaticQrModal

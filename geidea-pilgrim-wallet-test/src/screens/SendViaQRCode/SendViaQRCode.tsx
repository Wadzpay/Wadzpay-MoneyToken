// ============================== Imports =================================

import {useTranslation} from 'react-i18next';
import {
  TouchableOpacity,
  StyleSheet,
  View,
  Platform,
  Alert,
  PermissionsAndroid,
} from 'react-native';
import {
  Typography,
  ErrorModal,
  LoadingSpinner,
  Container,
  ScreenLayoutTab,
  theme,
  Icon,
  Modal,
  Checkbox,
  Button,
  AvailableBalance,
} from '~/components/ui';
import React, {useContext, useEffect, useRef, useState} from 'react';
import {showToast} from '~/utils';
import Clipboard from '@react-native-clipboard/clipboard';
import ViewShot, {captureRef, captureScreen} from 'react-native-view-shot';
import Share from 'react-native-share';
import {isIOS} from '~/utils';
import {useAddresses} from '~/api/user';
import QrCodeCardCarousel from '~/screens/ReceiveFunds/QrCodeCardCarousel';
import {Asset} from '~/constants/types';
import {useNavigation} from '@react-navigation/native';
import {CameraRoll} from '@react-native-camera-roll/camera-roll';
import {Text} from 'react-native-svg';
import { UserContext } from '~/context';

// ============================== Constants =================================

type Props = {
  asset?: Asset;
  cognitoUsername?: string;
  amount?: number;
  walletAddress?: string;
  onWalletAddressChange?: (value: string) => void;
  isComponent?: boolean;
};

// ============================== Class =================================

const SendViaQRCode: React.FC<Props> = ({
  asset,
  cognitoUsername,
  amount,
  walletAddress,
  onWalletAddressChange,
  isComponent = false,
}: Props) => {
  const {t} = useTranslation();
  const viewShotRef = useRef();
  const {user, setSaveStaticQrCodeToGallery} = useContext(UserContext);

  // ************************ Parameters Received ************************

  const [shareRecipt, setShareRecipt] = useState(false);
  const navigation = useNavigation();
  const [isPageLoadEnabled, setPageLoadEnabled] = useState(true);
  const [imageURI, setImageURI] = useState('');
  const [isResponseReceived, setIsResponseReceived] = useState(false);
  const [walletAddresses, setWalletAddresses] = useState('');

  const {
    data: addresses,
    isFetching: addressesIsFetching,
    isSuccess: addressesIsSuccess,
    error: addressesError,
  } = useAddresses(); // blockchin address of a logged in user 

  useEffect(() => {
    let response = addresses;
    if (response) {
      let wa = addresses?.find(obj => obj?.asset === 'SART')?.address;
      if (wa) {
        //// console.log("nandani", response)
        setWalletAddresses(wa);
        setIsResponseReceived(true);
        // console.log("walletAddresses : ", walletAddresses)
      }
    }
  }, [addressesIsSuccess]);

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

  const onBackButtonClick = () => {
    navigation.goBack();
  };

  // TODO: Nandani

  const copyToClipboard = (value: string) => {
    Clipboard.setString(value);
    isIOS ? alert('Wallet Address Copied') : showToast(`Wallet Address Copied`);
  };

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
      // Alert.alert(
      //   '',
      //   'Image saved successfully.',
      //   [{text: 'OK', onPress: () => {}}],
      //   {cancelable: false},
      // );
    }
  }

  const onShareClick = () => {
    setShareRecipt(true);
    setTimeout(() => {
      captureViewShot();
    }, 80);
  };

  async function captureViewShot() {
    // const imageURI = await viewShotRef.current.capture()
    await viewShotRef.current.capture().then(uri => {
      // console.log('do something with ', uri);
      //setImageURI(uri);
      const options = {
        url: uri,
        message: ``,
      };
      Share.open(options);
    });

    setShareRecipt(false);
  }

  // TODO: Nandani
  // ************************ UI Part ************************

  return (
    <ScreenLayoutTab
      showTitleHeader={isComponent ? false : true}
      title={'Share QR Code'}
      useScrollView={false}
      leftIconName="ArrowLeft"
      onLeftIconClick={() => {
        if (isPageLoadEnabled === true) {
          setPageLoadEnabled(false);
          navigation.goBack();
        }
      }}
      rightComponent={<Icon name={'SaveIcon'} size="md" color="iconRegulorColor" />}
      onRightIconClick={savePicture}>
      {walletAddresses.length > 0 && isResponseReceived ? (
        <Container
          justify="space-between"
          alignItems="center"
          noItemsStretch
          style={styles.container}>
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

            <Container
                alignItems="center"
                justify="center"
                spacing={1}
                style={styles.requestLinkContainer}>
                <Typography
                  style={{marginLeft: 30}}
                  variant="label"
                  fontFamily="Rubik-Medium"
                  textAlign="left">
                  Wallet Address
                </Typography>

                <Container
                  direction="row"
                  spacing={1}
                  alignItems="center"
                  justify="space-between"
                  style={{
                    marginTop: 10,
                    borderWidth: 2,
                    borderRadius: 8,
                    backgroundColor: '#F6F6F6',
                    borderColor: '#EDEDED',
                    paddingVertical: 16,
                    paddingLeft: 5,
                    paddingRight: 5,
                    marginLeft: 30,
                    marginRight: 30,
                  }}>
                  <Typography
                    variant="label"
                    style={styles.copyQrTextStyle}
                    numberOfLines={1}
                    ellipsizeMode="middle"
                    textAlign="left">
                    {walletAddresses}
                  </Typography>

                  <TouchableOpacity
                    onPress={() => copyToClipboard(walletAddresses)}>
                    <Icon name={'CopyIcon'}></Icon>
                  </TouchableOpacity>
                </Container>
              </Container>
            <View style={{height: 65}}></View>
         

          {/*  ************************ Copy and Share buttons style 1 ************************  */}

          {/* Container for Wallet Address and copy to clipboard button */}

          {/* Container for Share Button */}

          <Container
            direction="row"
            justify="space-around"
            style={{marginVertical: 20}}>
            <Container
              style={{
                width: 180,
                paddingLeft: 20,
                paddingRight: 20,
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
                  isComponent && onWalletAddressChange('');
                  navigation.goBack();
                }}>
                <Typography
                  variant="label"
                  textAlign="center"
                  fontFamily="Rubik-Medium">
                  Cancel
                </Typography>
              </TouchableOpacity>
            </Container>

            <Container
              style={{
                width: 180,
                paddingLeft: 20,
                paddingRight: 20,
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
                 // savePicture();
                  onShareClick()
                }}>
                <Icon name={'ShareNew'} color={'black'}></Icon>
                <Typography
                  style={{marginLeft: 10}}
                  variant="label"
                  textAlign="center"
                  fontFamily="Rubik-Medium">
                  Share
                </Typography>
              </TouchableOpacity>
            </Container>
            {/* <Button
              style={{
                width:100,
                bottom:50,
                borderColor: theme.colors.transparent,
                borderRadius: 15,
                borderWidth: 2,
            
              }}
                text= {"Share"}
                // fontWeight="bold"
                fontFamily={"helvetica"}
                onPress={() => {
                  onShareClick()
                }}
              /> */}
          </Container>
        </Container>
      ) : (
        <Typography>Loading...</Typography>
      )}
    </ScreenLayoutTab>
  );
};

const styles = StyleSheet.create({
  box: {
    width: 50,
    height: 50,
    borderRadius: 15,
    borderWidth: 0.3,
    borderColor: theme.colors.gray.light,
    backgroundColor: 'orange',
  },
  buttonContainer: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'green',
  },
  requestLinkContainer: {
    marginVertical: 10,
    paddingTop: 20,
  },
  copyQrTextStyle: {
    flex: 1,
    width: 300,
    paddingRight: 10,
  },
  container: {
    flex: 1,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: theme.spacing.lg,
  },
});

export default SendViaQRCode;

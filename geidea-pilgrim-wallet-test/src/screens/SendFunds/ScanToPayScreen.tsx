import React, {useContext, useEffect, useState} from 'react';
import SendViaQRCode from '~/screens/SendViaQRCode/SendViaQRCode';
import {
  Text,
  View,
  Button,
  Dimensions,
  StyleSheet,
  TouchableOpacity,
  LogBox,
  Platform,
  Image,
  Alert,
} from 'react-native';
//import { BarCodeScanner, BarCodeScannerResult } from "expo-barcode-scanner"
// import BarcodeMask from 'react-native-barcode-mask';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {SendFundsStackNavigatorParamList} from '~/components/navigators';
import {
  CommonAlertModal,
  Container,
  Modal,
  ScreenLayoutTab,
  theme,
  Typography,
} from '~/components/ui';
//import * as ImagePicker from "expo-image-picker"
import {request, PERMISSIONS} from 'react-native-permissions';
import QrCodeMask from 'react-native-qrcode-mask';
import {useTranslation} from 'react-i18next';
import SendFunds from '.';
import SendExternalComponent from './SendExternalComponent';
import {sub} from 'date-fns';
import {tr} from 'date-fns/locale';
import QRCodeScanner from 'react-native-qrcode-scanner';
import {RNCamera} from 'react-native-camera';
import { useGetDecryptedData } from "~/api/user"
import { TransactionTypeControl } from '~/api/constants';
import { UserContext } from '~/context';
import { CommonActions, useIsFocused } from '@react-navigation/native';

const finderWidth: number = 280;
const finderHeight: number = 230;
const width = Dimensions.get('window').width;
const height = Dimensions.get('window').height;
const viewMinX = (width - finderWidth) / 2;
const viewMinY = (height - finderHeight) / 2;

type Props = NativeStackScreenProps<
  SendFundsStackNavigatorParamList,
  'ScanWalletAddressQrCode'
>;

const ScanToPayScreen: React.FC<Props> = ({route, navigation}: Props) => {
  const {t} = useTranslation();
  const {onScanQRCode, asset} = route.params ?? {};
  const {isActivationFeeCharged} = useContext(UserContext);
  const isFocused = useIsFocused();
  const [showConfirmationBox, setShowConfirmationBox] = useState(false);
  const [hasPermission, setHasPermission] = useState();
  const [scanned, setScanned] = useState(false);
  const [showErrorAlert, setShowErrorAlert] = useState(false);

  const [walletAddress, setWalletAddress] = useState('');
  const [qrAmount, setQrAmount] = useState('');
  const [qrTransactionId, setQrTransactionId] = useState('');
  const [qrDescription, setQrDescription] = useState('');
  const [qrAsset, setQrAsset] = useState('');
  const [qrMerchantName, setQrMerchantName] = useState('');
  const [showQr, setShowQr] = useState(false);
  const [isValidQr, setIsValidQr] = useState(false);
  const [isAPIScheduled, setIsAPIScheduled] = useState(false)
  const [merchantId, setMerchantId] = useState("")
  const [posID, setposID] = useState("")

  const {
    mutate: getDecryptedData,
    isLoading: isGetDecryptedDataLoading,
    error: getDecryptedDataError,
    isSuccess: isGetDecryptedDataSuccess,
    data: getDecryptedSuccessData
  } = useGetDecryptedData()



  const [transactionTypecontrolQuery, setTransactionTypecontrolQuery] = useState(TransactionTypeControl.PURCHASE)
  // // console.log("asset asset asset asset", asset)
  useEffect(() => {
    (async () => {
      if (Platform.OS !== 'web') {
        request(
          Platform.OS === 'ios'
            ? PERMISSIONS.IOS.CAMERA
            : PERMISSIONS.ANDROID.CAMERA,
        ).then(result => {
          if (result !== 'granted') {
            alert('Sorry, we need camera roll permissions to make this work!');
          }
          setHasPermission(result)
          // console.log(result);
        });
      }
    })();
  }, []);

  // useEffect(() => {
  //   ;(async () => {
  //     const { status } = await BarCodeScanner.requestPermissionsAsync()
  //     setHasPermission(status === "granted")
  //   })()
  // }, [])

  // useEffect(() => {
  //   setScanned(false)
  // }, [scanned])

  // Warning that a function as a screen param can break deep
  // linking and state persistance (doesn't apply to this screen)
  LogBox.ignoreLogs([
    'Non-serializable values were found in the navigation state',
  ]);

  useEffect(() => {

    if(!isActivationFeeCharged) {
      setShowConfirmationBox(true)
    }
  }, []);


  useEffect(() => {
    if(!isActivationFeeCharged) {
      setShowConfirmationBox(true)
    } 
  }, [isFocused]);

  useEffect(() => {
    if (isGetDecryptedDataSuccess && getDecryptedSuccessData) 
     {
      let response = getDecryptedSuccessData 
      utilizeDecryptedData(String(response))
    }
  }, [isGetDecryptedDataSuccess,getDecryptedSuccessData
  ])


  useEffect(() => {
    if (getDecryptedDataError) 
     {
      // Alert.alert("Invalid QR Code")  
      setIsAPIScheduled(false)    
     }
  }, [getDecryptedDataError])


  function utilizeDecryptedData(decryptedString:String) {

    let transactionId = ""
    let address = ""
    let asset = ""
    let amount = ""
    let merchantId = ""
    let posID = ""
    let merchantName = ""

    // ******* Data Scanned from online POS MACHINE *******

    // transactionID: valid_value|
    // blockchainAddress: valid_wallet_address|
    // type:SART|
    // transactionAmount:20.0|
    // merchantId: valid_value|
    // posId: valid_value|
    // merchantDisplayName:valid_value

    // ******* Data Scanned from offline Merchant Static QR Code *******

    // transactionID: "00000"|
    // blockchainAddress: valid_wallet_address|
    // type:SART|
    // transactionAmount:0.00|
    // merchantId: valid_value|
    // posId: "00000" |
    // merchantDisplayName:valid_value

    // ******* Data Scanned from Wadzpay User's Static QR Code *******

    // transactionID: "00000"|
    // blockchainAddress: valid_wallet_address|
    // type:SART|
    // transactionAmount:0.00|
    // merchantId: 000000|
    // posId: "00000" |
    // merchantDisplayName:valid_value

    if (decryptedString.includes("|")) {
      // split and generate the array
      let qrData = decryptedString.split("|")
      // console.log("here in : ", qrData)

      if (qrData[0].includes(":")) {
        transactionId = qrData[0].split(":")[1]
      }

      if (qrData[1].includes(":")) {
        address = qrData[1].split(":")[1]
      }

      if (qrData[2].includes(":")) {
        let tempAsset = qrData[2].split(":")[1]

        if (
          tempAsset === "SAR Token" ||
          tempAsset === "SART" ||
          tempAsset === "SARt" ||
          tempAsset === "sart"
        ) {
          asset = "SART"
        } else {
          asset = ""
        }
      }

      if (qrData[3].includes(":")) {
        amount = qrData[3].split(":")[1]
      } else {
        amount = ""
      }

      if (qrData[4].includes(":")) {
        merchantId = qrData[4].split(":")[1]
      } else {
        merchantId = "0"
      }

      if (qrData[5].includes(":")) {
        posID = qrData[5].split(":")[1]
      } else {
        posID = "0"
      }

      if (qrData[6].includes(":")) {
        merchantName = qrData[6].split(":")[1]
      } else {
        merchantName = "Name not found"
      }
    } else {
      // only wallet address in qr code
      // console.log("qrData ", "in else")
      address = decryptedString
    }

   // console.log("merchantId ", merchantId,"transactionId ",transactionId )
    let temp_transaction_id = transactionId.replace(/^0+/, "")
    let temp_merchantId = merchantId.replace(/^0+/, "")
    // console.log("merchantId* ", temp_merchantId,"transactionId* ",temp_transaction_id )
   // console.log("merchantId* length", temp_merchantId.length,"transactionId* length ",temp_transaction_id.length ) 
   // user  merchantId* length 0 transactionId* length  0 
   // offline merchantId* length 1 transactionId* length  0
   // online merchantId* length 1 transactionId* length  36
      if(temp_transaction_id.length > 0) {
     // // console.log("here  PURCHASE")
      setTransactionTypecontrolQuery(TransactionTypeControl.PURCHASE)
    } else if( temp_merchantId.length > 0 && temp_transaction_id.length == 0 ) {
      //// console.log("here  MERCHANT_OFFLINE")
      setTransactionTypecontrolQuery(TransactionTypeControl.MERCHANT_OFFLINE)
    } else {
      setTransactionTypecontrolQuery(TransactionTypeControl.P2P_TRANSFER)
    }

    // console.log(
    //   "qrAddress ",
    //   address,
    //   "qrAmount ",
    //   amount,
    //   "qrAsset ",
    //   asset,
    //   "qrMerchantName ",
    //   merchantName,
    //   "tid",
    //   transactionId,
    //   "posID",
    //   posID,
    //   "merchantId" ,
    //   merchantId
    // )


    if (address.length > 0 ){//&& amount >= "0" && asset == "SART" && merchantName.length>0 && transactionId.length>0) {
      setIsValidQr(true)
      setShowErrorAlert(false)

      // set values
      setQrAmount(amount)
      setQrAsset(asset)
      setQrMerchantName(merchantName)
      setWalletAddress(address)
      setQrTransactionId(transactionId)
      setQrDescription("Pay to " + qrMerchantName)
      setMerchantId(merchantId)
      setposID(posID)
    } else {
      setIsValidQr(false)
      setShowErrorAlert(false)
    }
  }



  const getQrCodeData = (scannedQrCodeData) => {

    // scanned data will be encrypted
    // hit an api t get the details as below
    // "transactionID:8dbb8a55-66b5-406e-8fd5-7cdd509bf47f|blockchainAddress:U6EUU4555GDH2J3UVE6JYLXSTDZLMKZJBJTPO6K6JCARPTELKM76KHH5NA|type:SART|transactionAmount:12|merchantId:valid_value|posId:1|merchantDisplayName:suresh-geidea-dev"

    // console.log("value :: ", isAPIScheduled)
    if (isAPIScheduled == false) {
      // console.log("API was HIT")
      setIsAPIScheduled(true)
      // Hit the API to get decrypted details
      getDecryptedData({
        data: String(scannedQrCodeData)
      })
    }
  }


  const handleBarCodeScanned = ({ type, data }) => {
    // alert(`Bar code with type ${type} and data ${data} has been scanned!`);
    getQrCodeData(data)

    if (isValidQr) {
      setScanned(true)
    } else {
      setScanned(false)
    }

    //navigation.navigate("SendFunds", { })
    // navigation.goBack()
  }

  if (hasPermission === null) {
    return <Text>Requesting for camera permission</Text>;
  }
  if (hasPermission === false) {
    return <Text>No access to camera</Text>;
  }

  // const pickImage = async () => {
  //   let result = await ImagePicker.launchImageLibraryAsync({
  //     mediaTypes: ImagePicker.MediaTypeOptions.Images,
  //     allowsEditing: true,
  //     aspect: [4, 4],
  //     quality: 1
  //   })
  //   if (!result.cancelled) {
  //     if (result && result.uri) {
  //       const results = await BarCodeScanner.scanFromURLAsync(result.uri)
  //       // // console.log("results ", results[0].data) // many information
  //       if (results[0]?.data) {
  //         getQrCodeData(results[0]?.data)
  //         navigation.goBack()
  //       } else {
  //         Alert.alert("Invalid QR Code")
  //       }
  //     }
  //   }
  // }

  // useEffect(() => {
  //   if  (scanned){
  //    setScanned(false)
  //   }
  // }, [scanned])

  const onWalletAddressChange = (value: string) => {
    // console.log('here bro ', value, isValidQr);

    setWalletAddress('');
    setIsValidQr(false);
    setShowQr(false);
    setScanned(false);
    setIsAPIScheduled(false)
  };

  const {height, width} = Dimensions.get('window');
  // console.log('isValidQr ', isValidQr);
  // console.log('%%%%%%%%%%', walletAddress.length , scanned);

  return (
    <ScreenLayoutTab
      title={
        showQr
          ? t('Scan QR')
          : walletAddress.length <= 0
          ? t('Scan & Pay')
          : 'Payment Confirmation'
      }
      useScrollView={false}>
      {showQr ? (
        <SendViaQRCode
          isComponent={true}
          asset={'SART'}
          amount={0.000029}
          walletAddress={'0000'}
          onWalletAddressChange={onWalletAddressChange}
        />
      ) : (
        walletAddress.length <= 0 && (
          <View style={styles.container}>
            <QRCodeScanner
            // cameraStyle={{ height: height, width: width }}
              onRead={e => (scanned ? undefined : handleBarCodeScanned(e))}
              //flashMode={RNCamera.Constants.FlashMode.torch}
              reactivate={true}
              reactivateTimeout={500}
              showMarker={true}
              fadeIn
              bottomContent={
                <TouchableOpacity
                  style={{
                    height: 60,
                    width: 160,
                    alignItems: 'center',
                    justifyContent: 'center',
                    paddingVertical: 8,
                    borderRadius: 2,
                    borderWidth: 2,
                    borderColor: theme.colors.white,
                    backgroundColor: theme.colors.black,
                  }}
                  onPress={() => {
                    // console.log('here');
                    setWalletAddress('');
                    setScanned(false);
                   // setShowQr(true);
                    navigation.navigate('SendViaQRCode', {
                      walletAddress: '0000',
                      asset: 'SAR',
                    });
                  }}>
                  <Container direction="row">
                    <Image
                      style={{height: 20, width: 20, marginHorizontal: 5}}
                      source={require('~images/qr.png')}></Image>
                    <Typography
                      variant="label"
                      textAlign="center"
                      fontFamily="Rubik-Medium"
                      color="white">
                      Show My QR
                    </Typography>
                  </Container>
                </TouchableOpacity>
              }
            />
            {/* <BarCodeScanner
              onBarCodeScanned={scanned ? undefined : handleBarCodeScanned}
              style={[
                {
                  position: "absolute",
                  left: 0,
                  right: 0,
                  top: -10,
                  bottom: 60,
                  backgroundColor: theme.colors.black
                }
              ]}
            >
              <TouchableOpacity
                style={{
                  height: 60,
                  width: 160,
                  alignItems: "center",
                  justifyContent: "center",
                  paddingVertical: 8,
                  borderRadius: 2,
                  borderWidth: 2,
                  borderColor: theme.colors.white,
                  backgroundColor: theme.colors.black,
                  position: "absolute",
                  left: (width - 160) / 2,
                  right: 0,
                  top: height - 220,
                  bottom: 0
                }}
                onPress={() => {
                  // console.log("here")
                  setWalletAddress("")
                  setScanned(false)
                  setShowQr(true)
                  navigation.navigate("SendViaQRCode", {
                    walletAddress: "0000",
                    asset: "SAR"
                  })
                }}
              >
                <Container direction="row">
                  <Image
                    style={{ height: 20, width: 20, marginHorizontal: 5 }}
                    source={require("~images/qr.png")}
                  ></Image>
                  <Typography
                    variant="label"
                    textAlign="center"
                    fontFamily="Rubik-Medium"
                    color="white"
                  >
                    Show My QR
                  </Typography>
                </Container>
              </TouchableOpacity>

              <QrCodeMask
                lineColor="green"
                lineDirection="vertical"
                height={260}
                edgeColor="red"
                topTitle="QR Code Scanner"
                bottomTitle="Put the QR Code into the box"
              />
            </BarCodeScanner> */}
            {
              // swati test
              // getQrCodeData("")
            }
          </View>
        )
      )}

      {isValidQr && (
        <SendExternalComponent
          walletAddress1={walletAddress}
          amount1={qrAmount}
          asset1={qrAsset}
          merchantName={qrMerchantName}
          transactionTypeControlQuery = {transactionTypecontrolQuery}
          transactionId={qrTransactionId}
          onWalletAddressChange={onWalletAddressChange}
          merchantId = {merchantId}
          posId = {posID}
        />
      )}

      {showErrorAlert && (
        <Modal
          variant="center"
          isVisible={showErrorAlert}
          onDismiss={() => {
            setScanned(false);
            setShowErrorAlert(false);
          }}
          swipeDirection={['down']}
          dismissButtonVariant="button">
          <Container spacing={2}>
            <Typography variant="subtitle" color="error">
              Error
            </Typography>

            <Typography variant="button">Please scan valid Qr Code</Typography>
          </Container>
        </Modal>
      )}
       {showConfirmationBox &&  (
        <Modal
          variant="center"
          isVisible={showConfirmationBox}
          onDismiss={() =>  isActivationFeeCharged && setShowConfirmationBox(false)}
         // onDismiss={() => setShowConfirmationBox(false)}
          dismissButtonVariant="none"
          swipeDirection={['down']}>
  
            <Container
              alignItems="flex-start"
              noItemsStretch
              justify="flex-start"
              style={{marginVertical: 16}}
              spacing={2}>
              <Typography
                variant="label"
                fontFamily="Rubik-Medium"
                fontWeight="bold"
                color="darkBlackBold">
                {t('Please Activate your Wallet')}
              </Typography>
            </Container>
         
          <Container
            direction="row"
            alignItems="center"
            justify="flex-end"
            noItemsStretch
            spacing={2}
            style={{marginTop: 20}}>
            <TouchableOpacity
              style={styles.cancelButtonClick}
              onPress={() => {
                setShowConfirmationBox(false)
                  navigation.dispatch(
                    CommonActions.reset({
                      index: 0,
                      routes: [
                        { name: 'Home' }
                      ],
                    })
                  )
                 
              }}>
              <Typography
                color="darkBlackBold"
                variant="heading"
                textAlign="center"
                fontFamily="Rubik-Medium">OK</Typography>
            </TouchableOpacity>
          </Container>
        </Modal>
      )}
    </ScreenLayoutTab>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.black,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  separator: {
    marginVertical: 30,
    height: 1,
    width: '80%',
  },
  cancelButtonClick:{
    alignItems:"center",
    justifyContent:"center",
    width:100,
    height:40,
    borderRadius:5,
    backgroundColor:theme.colors.white,
    borderColor:theme.colors.darkBlackBold,
    borderWidth:1
  }
});

export default ScanToPayScreen;

import React, {useEffect, useState} from 'react';
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
import {ScreenLayoutTab} from '~/components/ui';
import {request, PERMISSIONS} from 'react-native-permissions';
//import QrCodeMask from "react-native-qrcode-mask"
import QRCodeScanner from 'react-native-qrcode-scanner';
import {RNCamera} from 'react-native-camera';

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

const ScanWalletAddressQrCode: React.FC<Props> = ({
  route,
  navigation,
}: Props) => {
  const {onScanQRCode, asset} = route.params ?? {};
  const [hasPermission, setHasPermission] = useState(null);
  const [scanned, setScanned] = useState(false);

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

  // Warning that a function as a screen param can break deep
  // linking and state persistance (doesn't apply to this screen)
  LogBox.ignoreLogs([
    'Non-serializable values were found in the navigation state',
  ]);

  const getQrCodeData = scannedQrCodeData => {
    // console.log('scannedData', scannedQrCodeData);
    let qrAddress = '';
    let qrAmount = '';
    let qrAsset = asset;

    if (scannedQrCodeData.includes('?')) {
      const qrData = scannedQrCodeData.split('?');

      // wallet address
      if (qrData[0].includes(':')) {
        const qrSplitAddress = qrData[0].split(':');
        // console.log('qrSplitAddress ', 'amit', qrSplitAddress);

        //asset
        if (
          qrSplitAddress[0] === 'Bitcoin' ||
          qrSplitAddress[0] === 'BTC' ||
          qrSplitAddress[0] === 'bitcoin' ||
          qrSplitAddress[0] === 'btc'
        ) {
          qrAsset = 'BTC';
        } else if (
          qrSplitAddress[0] === 'Ethereum' ||
          qrSplitAddress[0] === 'ETH' ||
          qrSplitAddress[0] === 'ethereum' ||
          qrSplitAddress[0] === 'eth'
        ) {
          qrAsset = 'ETH';
        } else if (
          qrSplitAddress[0] === 'Tether' ||
          qrSplitAddress[0] === 'USDT' ||
          qrSplitAddress[0] === 'tether' ||
          qrSplitAddress[0] === 'usdt'
        ) {
          qrAsset = 'USDT';
        } else if (
          qrSplitAddress[0] === 'WadzPay Token' ||
          qrSplitAddress[0] === 'WTK' ||
          qrSplitAddress[0] === 'wadzpaytoken' ||
          qrSplitAddress[0] === 'wadzpay token' ||
          qrSplitAddress[0] === 'wtk'
        ) {
          qrAsset = 'WTK';
        } else {
          qrAsset = 'BTC';
        }
        qrAddress = qrSplitAddress[1];
      } else {
        qrAddress = qrData[0];
      }
      //amount
      if (qrData[1].includes(':')) {
        const qrSplitAmount = qrData[1].split(':');
        qrAmount = qrSplitAmount[1];
      } else {
        const qrSplitAmount = qrData[1].split('=');
        qrAmount = qrSplitAmount[1];
      }
    } else if (scannedQrCodeData.includes(':')) {
      // pattern currency:address
      const qrData = scannedQrCodeData.split(':');
      // // console.log("here in : ", qrData)
      //qrAsset
      if (
        qrData[0] === 'Bitcoin' ||
        qrData[0] === 'BTC' ||
        qrData[0] === 'bitcoin' ||
        qrData[0] === 'btc'
      ) {
        qrAsset = 'BTC';
      } else if (
        qrData[0] === 'Ethereum' ||
        qrData[0] === 'ETH' ||
        qrData[0] === 'ethereum' ||
        qrData[0] === 'eth'
      ) {
        qrAsset = 'ETH';
      } else if (
        qrData[0] === 'Tether' ||
        qrData[0] === 'USDT' ||
        qrData[0] === 'tether' ||
        qrData[0] === 'usdt'
      ) {
        qrAsset = 'USDT';
      } else if (
        qrData[0] === 'WadzPay Token' ||
        qrData[0] === 'WTK' ||
        qrData[0] === 'wadzpaytoken' ||
        qrData[0] === 'wadzpay token' ||
        qrSplitAddress[0] === 'wtk'
      ) {
        qrAsset = 'WTK';
      } else {
        qrAsset = 'BTC';
      }
      //qrAddress
      qrAddress = qrData[1];
    } else {
      // only wallet address in qr code
      qrAddress = scannedQrCodeData;
    }

    // console.log(
    //   'qrAddress1111 ',
    //   qrAddress,
    //   'qrAmount ',
    //   qrAmount,
    //   'qrAsset ',
    //   qrAsset,
    // );
    onScanQRCode(qrAddress, qrAmount, qrAsset);
  };

  const handleBarCodeScanned = (data: any) => {
    setScanned(true);
    // alert(`Bar code with type ${type} and data ${data} has been scanned!`);
    getQrCodeData(data);
    navigation.goBack();
  };

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
  return (
    <ScreenLayoutTab
      leftIconName="ArrowLeft"
      onLeftIconClick={navigation.goBack}
      useScrollView={false}>
      <View style={styles.container}>
        {/**/}

        <QRCodeScanner
          onRead={e => (scanned ? undefined : handleBarCodeScanned(e))}
          flashMode={RNCamera.Constants.FlashMode.torch}
          reactivate={true}
          reactivateTimeout={500}
          showMarker={true}
          topContent={
            <View
              style={{
                flex: 1,
                alignItems: 'center',
                justifyContent: 'flex-end',
                marginBottom: 70,
              }}>
              <Button title="Pick an image from Gallery" />
            </View>
          }
        />

        {/* <BarCodeScanner
          onBarCodeScanned={scanned ? undefined : handleBarCodeScanned}
          style={StyleSheet.absoluteFillObject}
        >
          <View
            style={{
              flex: 1,
              alignItems: "center",
              justifyContent: "flex-end",
              marginBottom: 70
            }}
          >
            <Button title="Pick an image from Gallery" onPress={pickImage} />
          </View>
          <QrCodeMask
            lineColor="green"
            lineDirection="vertical"
            height={260}
            edgeColor="red"
            topTitle="Bar Code Scanner"
            bottomTitle="Put the QR Code into the box"
          />
        </BarCodeScanner> */}
        {scanned && (
          <Button
            title={'Tap to Scan Again'}
            onPress={() => setScanned(false)}
          />
        )}
      </View>
    </ScreenLayoutTab>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
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
});

export default ScanWalletAddressQrCode;

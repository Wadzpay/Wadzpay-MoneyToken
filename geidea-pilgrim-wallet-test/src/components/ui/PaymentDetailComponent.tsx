import React, {useContext, useEffect, useRef, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Transaction} from '~/api/models';
import Container from './Container';
import ViewShot from 'react-native-view-shot';
import theme from './theme';
import Share from 'react-native-share';
import {
  Dimensions,
  Image,
  ImageBackground,
  StyleSheet,
  TouchableOpacity,
  View,
} from 'react-native';
import Typography from './Typography';
import Icon from './Icon';
import {
  copyToClipboard,
  getActionName,
  getTotalName,
  getTransactionId,
  getTransactionType,
  getTransactionUrl,
  // instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : "",
  showFromFeildValue,
  showToFeildValue,
} from '~/utils';
import useFormatCurrencyAmount from '~/helpers/formatCurrencyAmount';
import FiatAmount from './FiatAmount';
import {FiatAssetFractionDigits} from '~/api/constants';
import Button from './Button';
import Modal from './Modal';
import WebView from 'react-native-webview';
import LoadingSpinner from './LoadingSpinner';
import { UserContext } from '~/context';
import { TransactionSummaryComponent } from './TransactionSummaryComponent';
import { tr } from 'date-fns/locale';

const {width , height} = Dimensions.get('window');
const containerMargin = theme.spacing.lg;
const rowWidth =
  width - containerMargin * 2 ;

type PaymentDetailComponentProps = {
  transaction?: Transaction;
};

const PaymentDetailComponent: React.FC<PaymentDetailComponentProps> = ({
  transaction,
}: PaymentDetailComponentProps) => {

  const {t} = useTranslation();
  const viewShotRef = useRef();
  const {user, instDetails} = useContext(UserContext);
  const [shareRecipt, setShareRecipt] = useState(false);
  const [url, setUrl] = useState('google.com');
  const [isVisible, setIsVisible] = useState(false);
  const formatter = useFormatCurrencyAmount();
  
  const date = transaction ? transaction.transactionType === "POS" ? new Date(transaction.paymentReceivedDate) : new Date(transaction.createdAt) : new Date();

  
  async function captureViewShot() {
    let options
    await viewShotRef.current?.capture().then((uri: any) => {
       options = {
       // message: `Transaction Id ${transaction?.uuid}`,
        url: uri,
      };
      
    });
    options &&  Share.open(options);
    setShareRecipt(false);
  }

  const styles = StyleSheet.create({
    container: {
      flex: 1,
      width: width,
      alignItems:'center',
      marginVertical: theme.spacing.xs,
    },
    image: {
      height: 70,
      width: 70,
    },
    buttonContainer: {
      marginLeft: 20,
      marginRight: 20,
      marginTop: 20,
      marginBottom:20,

    },
    wadz_pay_logo: {
      marginTop: theme.spacing.xs,
      marginBottom: theme.spacing.xs,
    },
    row: {
      width: rowWidth,
      marginVertical: 1,
    },
    separator: {
      marginTop:4,
      marginBottom: 4,
      width: rowWidth,
      height: theme.borderWidth.xs,
      backgroundColor: "#E0E0E0",
    },
  });

  return (
    <Container direction="row">
      {transaction ? (
        <ViewShot
          style={{backgroundColor: theme.colors.white, paddingTop: 10}}
          ref={viewShotRef}
          options={{format: 'jpg', quality: 0.5}}>
          <Container justify="space-between" style={styles.container}>
            <Container alignItems="center" noItemsStretch spacing={1}>
              <Container
                alignItems="center"
                noItemsStretch
                justify="center"
                spacing={2}
                style={{marginTop:10}}>
                <Icon name= {getTransactionType(transaction, transaction.transactionType) == "Service Fee" ? "ServiceFee" : getTransactionType(transaction, transaction.transactionType) == "Wallet Low Balance Fee"? "WalletBalanceFee" :  "Success" } size="xl" />
                <Typography
                  variant="body"
                  fontFamily="Rubik-Medium"
                  color={getTransactionType(transaction, transaction.transactionType) == "Service Fee" || getTransactionType(transaction, transaction.transactionType) == "Wallet Low Balance Fee" ? "darkBlackBold"  : "success" }>
                   {getTransactionType(transaction, transaction.transactionType)} {getTransactionType(transaction, transaction.transactionType) == "Service Fee" || getTransactionType(transaction, transaction.transactionType) == "Wallet Low Balance Fee" ? '' : ' Successful'}
                </Typography>
              </Container>
            </Container>
            <TransactionSummaryComponent
                transactionBlockHeader = {"TRANSACTION DETAILS"}
                transactionType= {getTransactionType(transaction, transaction.transactionType)}
                //amountRequested={`${transaction.totalRequestedAmountAsset == "SART" ? instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : "" : transaction.totalRequestedAmountAsset} ${Number(transaction.totalRequestedAmount).toFixed(2)}`}
                netAmountCalulated={`${transaction.totalRequestedAmountAsset == "SART" ? instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : "" : transaction.totalRequestedAmountAsset} ${transaction.transactionType == "WITHDRAW" ? Number(transaction.amount).toFixed(2) : Number(transaction.totalRequestedAmount).toFixed(2)}`}
                totalAmountText={getTotalName(transaction, transaction.transactionType) || "Amount Received"}
                feeConfigList={transaction?.feeConfigData || 0.00}
                netFeeKeyName={getActionName(transaction, transaction.transactionType) || ""}
                totalAmount={transaction.transactionType== "WITHDRAW" ?`${transaction?.fiatAsset} ${Number(transaction?.fiatAmount).toFixed(2)}` :`${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(transaction?.totalAmount).toFixed(2)}`}
                amountRequested={getTransactionType(transaction, transaction.transactionType)== "Topup" ? `${transaction.fiatAsset} ${Number(transaction.fiatAmount).toFixed(2)}` 
              : transaction.transactionType == "WITHDRAW" ? `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(transaction.totalRequestedAmount).toFixed(2)}` : `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(transaction.amount).toFixed(2)}`}/>
            <Container
              alignItems="center"
              noItemsStretch
              spacing={1}
              style={{marginTop: 10}}>
              {/* <View style={styles.separator} />

              {!shareRecipt ? (
                <TouchableOpacity
                  onPress={() =>
                    copyToClipboard(
                      transaction.blockchainTxId,
                      'TRANSACTION_HASH',
                    )
                  }>
                  <Container
                    direction="row"
                    justify="space-between"
                    style={[styles.row]}>
                    <Typography
                      fontFamily="Rubik-Regular"
                      variant="label"
                      textAlign="left">
                      Status Id
                    </Typography>
                    <Container
                      direction="row"
                      noItemsStretch
                      alignItems="center"
                      justify="space-evenly">
                      <TouchableOpacity
                        onPress={() => {
                          // open a web browser and a link

                          setIsVisible(true);

                          setUrl(
                            getTransactionUrl(
                              transaction.asset,
                              transaction.blockchainTxId,
                            ),
                          );
                        }}>
                        <Typography
                          fontFamily="Rubik-Medium"
                          variant="label"
                          style={{
                            width: 180,
                            marginRight: 8,
                            color: '#006BE2',
                            textDecorationLine: 'underline',
                          }}
                          numberOfLines={1}
                          ellipsizeMode="tail">
                          {transaction.blockchainTxId || ''}
                        </Typography>
                      </TouchableOpacity>
                      <Icon name="CopyIcon" size="xs" />
                    </Container>
                  </Container>
                </TouchableOpacity>
              ) : (
                <Container alignItems="center" justify="space-between">
                  <Typography
                    fontFamily="Rubik-Regular"
                    variant="label"
                    textAlign="center">
                    Status Id
                  </Typography>
                  <Typography
                    textAlign="left"
                    variant="chip"
                    numberOfLines={8}
                    color='darkBlackBold'
                    fontFamily='Rubik-Medium'
                    style={{marginHorizontal: 8, width: 300}}>
                    {transaction.blockchainTxId || ''}
                  </Typography>
                </Container>
              )} */}
              <View style={styles.separator} />
              {!shareRecipt ? (
                <TouchableOpacity
                  onPress={() =>
                    copyToClipboard(
                      getTransactionId(transaction),
                      'TRANSACTION_ID',
                    )
                  }>
                  <Container
                    direction="row"
                    justify="space-between"
                    style={[styles.row]}>
                    <Typography
                      fontFamily="Rubik-Regular"
                      variant="label"
                      textAlign="left">
                      Transaction Id
                    </Typography>
                    <Container
                      direction="row"
                      noItemsStretch
                      alignItems="center"
                      justify="space-evenly">
                      <Typography
                        fontFamily="Rubik-Regular"
                        color='darkBlackBold'
                        style={{width: 180, marginRight: 8}}
                        numberOfLines={1}
                        ellipsizeMode="tail">
                        {getTransactionId(transaction)}
                      </Typography>
                      <Icon name="CopyIcon" size="xs" />
                    </Container>
                  </Container>
                </TouchableOpacity>
              ) : (
                <Container direction='row' alignItems="flex-start" noItemsStretch justify="flex-start">
                  <Typography
                   style={{width : 180}}
                    fontFamily="Rubik-Regular"
                    variant="label"
                    textAlign="left">
                    Transaction Id
                  </Typography>
                  <Typography
                  textAlign='right'
                  style={{width : 180}}
                  numberOfLines={3}
                    fontFamily='Rubik-Regular'
                    color='darkBlackBold'>
                    {getTransactionId(transaction)}
                  </Typography>
                </Container>
              )}

              {/* <View style={styles.separator} />

              <Container
                direction="row"
                justify="space-between"
                style={[styles.row]}>
                <Typography
                  fontFamily="Rubik-Regular"
                  variant="label"
                  textAlign="left">
                  Transaction Type
                </Typography>
                <Typography
                  fontFamily="Rubik-Medium"
                  color='darkBlackBold'
                  variant="label"
                  textAlign="right">
                  {getTransactionType(transaction, transaction.transactionType)}
                </Typography>
              </Container> */}

              <View style={styles.separator} />

              <Container
                direction="row"
                justify="space-between"
                style={styles.row}>
                <Typography
                  fontFamily="Rubik-Regular"
                  variant="label"
                  textAlign="left">
                  From
                </Typography>
                <Typography
                  fontFamily="Rubik-Regular"
                  color='darkBlackBold'
                  variant="label"
                  textAlign="right">
                  {showFromFeildValue(
                    transaction,
                    user?.attributes?.email || '',
                  )}
                </Typography>
              </Container>

              <View style={styles.separator} />

              <Container
                direction="row"
                justify="space-between"
                alignItems="center"
                noItemsStretch
                style={styles.row}>
                <Typography
                  fontFamily="Rubik-Regular"
                  variant="label"
                  textAlign="left">
                  To
                </Typography>
                <Typography
                  textAlign="right"
                  fontFamily="Rubik-Regular"
                  color='darkBlackBold'
                  variant="label">
                  {showToFeildValue(transaction)}
                </Typography>
              </Container>

            

              <View style={styles.separator} />

              <Container
                direction="row"
                justify="space-between"
                style={styles.row}>
                <Typography
                  fontFamily="Rubik-Regular"
                  variant="label"
                  textAlign="left">
                  Time
                </Typography>
                <Typography
                  fontFamily="Rubik-Regular"
                  variant="label"
                  color='darkBlackBold'
                  textAlign="right">
                  {`${date.toLocaleTimeString('en-GB', {
                      day: 'numeric', month: 'short', year: 'numeric', hour:"2-digit",minute:'2-digit', hour12: true
                    })}`}
                </Typography>
              </Container>
              <View style={styles.separator} />
            </Container>
          </Container>
          {!shareRecipt && <Container spacing={1} style={styles.buttonContainer}>
            <Button
              text={'Share'}
              onPress={() => {
                setShareRecipt(true);
                setTimeout(() => {
                  captureViewShot();
                }, 80);
              }}
              textVariant={'heading'}
            />
          </Container>}
        </ViewShot>
      ) : (
        <Typography>{t('Transaction not found.')}</Typography>
      )}

      {url && (
        <Modal
          variant="bottom"
          isVisible={isVisible}
          onDismiss={() => setIsVisible(false)}
          dismissButtonVariant="cancel"
          swipeDirection={['down']}
          contentStyle={{height: theme.modalFullScreenHeight}}>
          <WebView
            source={{
              uri: url,
            }}
            originWhitelist={['*']}
            allowsInlineMediaPlayback
            javaScriptEnabled
            scalesPageToFit
            mediaPlaybackRequiresUserAction={false}
            javaScriptEnabledAndroid
            useWebkit
            startInLoadingState={true}
            renderLoading={() => (
              <Container
                alignItems="center"
                justify="center"
                spacing={1}
                noItemsStretch
                style={{
                  flex: 1,
                  position: 'absolute',
                  height: '100%',
                  width: '100%',
                }}>
                <LoadingSpinner color="orange" />
              </Container>
            )}
          />
        </Modal>
      )}
    </Container>
  );
};

export default PaymentDetailComponent;

// <View style={styles.separator} />

// <Container
//   direction="row"
//   justify="space-between"
//   alignItems="center"
//   noItemsStretch
//   style={styles.row}>
//   <Typography
//     fontFamily="Rubik-Regular"
//     variant="label"
//     textAlign="left">
//     Amount
//   </Typography>
//   <Container>
//     <Container direction="row" alignItems="center">
//       {/* <Icon name={"SartLogo"} size="md" /> */}
//       <Typography
//         variant="label"
//         fontFamily="Rubik-Medium"
//         color='darkBlackBold'
//         textAlign="left">
//         {instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""}
//       </Typography>
//       <Typography
//         textAlign="right"
//         variant="label"
//         color='darkBlackBold'
//         fontFamily="Rubik-Medium">
//         {formatter(
//           transaction.transactionType == 'POS'
//             ? transaction.totalDigitalCurrencyReceived
//             : transaction.amount,
//           {
//             asset: transaction.asset,
//           },
//         )}
//       </Typography>
//     </Container>

//     {(transaction.fiatAmount && transaction.asset != 'SART') ||
//     (transaction.fiatAmount &&
//       transaction?.asset == 'SART' &&
//       (transaction.transactionType == 'DEPOSIT' ||
//         transaction.transactionType == 'WITHDRAW')) ? (
//       <FiatAmount
//         amount={Number(
//           transaction.fiatAmount.toFixed(
//             FiatAssetFractionDigits[transaction.fiatAsset],
//           ),
//         )}
//         fiatAsset={transaction.fiatAsset}
//         color='darkBlackBold'
//         variant={'label'}
//       />
//     ) : null}
//   </Container>
// </Container>
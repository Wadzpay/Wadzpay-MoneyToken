/* eslint-disable @typescript-eslint/ban-ts-comment */
import React, {useContext, useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {
  StyleSheet,
  TouchableOpacity,
  View,
  Dimensions,
  TextInput,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';

import {ConTwoDecDigit, isIOS, isTransactionAllowed} from '~/utils';

import {
  Container,
  ScreenLayoutTab,
  theme,
  Typography,
  Icon,
  DismissKeyboard,
  ErrorModal,
  Modal,
} from '~/components/ui';
import {
  useAddTransaction,
  useTransactionValidationConfig,
  useUserContacts,
} from '~/api/user';
import {Asset, TokenToAmount} from '~/constants/types';
import {UserContext} from '~/context';
import {RecieveFundsStackNavigatorParamList} from '~/components/navigators/RecieveFundsStackNavigator';

const {width, height} = Dimensions.get('window');
import ContactItem from '~/screens/SendFunds/SelectContact/ContactItem';
import {Alert} from 'react-native';
import useFormatCurrencyAmount from '~/helpers/formatCurrencyAmount';
import {CommonActions, useIsFocused} from '@react-navigation/native';
import {el} from 'date-fns/locale';
import {TransactionTypeControl} from '~/api/constants';
type Props = NativeStackScreenProps<
  RecieveFundsStackNavigatorParamList,
  'InternalTransfer'
>;
// import {sartTxt} from '~/utils';
import Clipboard from '@react-native-clipboard/clipboard';

const InternalTransfer: React.FC<Props> = ({route, navigation}: Props) => {
  const {updateSarTokens, sarTokens, isActivationFeeCharged, instDetails} = useContext(UserContext);
  const [showConfirmationBox, setShowConfirmationBox] = useState(false);
  const isFocused = useIsFocused();
  const {t} = useTranslation();
  const formatter = useFormatCurrencyAmount();

  const [scrollViewWidth, setScrollViewWidth] = React.useState(width);
  const [textFieldAmount, setTextFieldAmount] = useState<any>();
  const [btnDisable, setbtnDisable] = useState(true);
  const [showAlert, setshowAlert] = useState({name: '', message: ''});

  const boxWidth = scrollViewWidth * 0.9;
  const {asset, cognitoUsername, amount} = route.params ?? {};

  const {
    data: transactionConfigData,
    refetch: refetchTransactionConfigData,
    isSuccess: isTransactionConfigSuccess,
    error: transactionConfigError,
  } = useTransactionValidationConfig(TransactionTypeControl.P2P_TRANSFER); // transaction limits and fee config 
  const {
    data: contacts,
    isFetching: contactsIsFetching,
    error: contactsError,
  } = useUserContacts(); // to show list of recipient address

  const {
    mutate: addTransaction,
    isLoading: isAddTransactionDataLoading,
    error: isAddTransactionDataError,
    isSuccess,
    data: addTransactionData,
  } = useAddTransaction(); // to perform P2P transaction 

  const [transaction, setTransaction] = useState<{
    asset: Asset; 
    cognitoUsername: string;
    amount: number;
  }>({
    asset: asset ?? 'SART',
    cognitoUsername: cognitoUsername ?? '',
    amount: amount ?? 0,
  });

  const [showError, setShowError] = useState('');

  // console.log("nandani ER ", exchangeRates, fiatAsset)
  const contact = contacts?.find(c => c.id === transaction.cognitoUsername);
  const styles = StyleSheet.create({
    container: {
      flex: 1,
      flexDirection: 'column',
      marginHorizontal: 25,
    },
    box: {
      height: 60,
      borderRadius: 5,
      borderWidth: 1,
      borderColor: '#c4c4c3',
      backgroundColor: 'white',
    },
    textFieldStyle: {
      height: 75,
      fontSize: 18,
      paddingHorizontal: theme.spacing.md,
      color: '#1D2D3A',
      fontFamily: 'Rubik-Medium',
    },
    availableBalnceView: {
      marginVertical: 25,
      borderRadius: 15,
      backgroundColor: '#ececec',
      paddingVertical: 25,
    },
    blankDivider: {
      marginVertical: 15,
    },
    recipientView: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-around',
    },
    amountLabelView: {
      width: 75,
      height: 58,
      borderRightWidth: 1,
      borderTopLeftRadius: 5,
      borderBottomLeftRadius: 5,
      borderRightColor: '#c4c4c3',
      backgroundColor: '#f6f6f7',
    },
    amountView: {
      flexDirection: 'row',
      width: '89%',
      flex: 1,
      paddingRight: 10,
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

  useEffect(() => {

    if(!isActivationFeeCharged) {
      setShowConfirmationBox(true)
    }
  }, []);
  
  useEffect(() => {
    if(!isActivationFeeCharged) {
      setShowConfirmationBox(true)
    } 
   
    refetchTransactionConfigData();
  }, [isFocused]);
  ///^\d+\.\d{0,2}$/
  const numberRegex = /^(?:\d*\.\d{1,2}|\d+)$/;
  const specialChars = /[`!@#$%^&*()_+\-=\[\]{};':"\\|,<>\/?~]/;
  let getDisabledState = () => {

    const isTransactionPermitted =
    transactionConfigData?.per_TRANSACTION == null
      ? 'allowed'
      : isTransactionAllowed(transactionConfigData, Number(transaction.amount));

    return (transaction.cognitoUsername && transaction.amount && transaction.amount != 0 &&  sarTokens >= transaction.amount && numberRegex.test(transaction.amount.toString())&& (!specialChars.test(transaction.amount.toString())) && isTransactionPermitted === 'allowed') || isAddTransactionDataLoading
      ? false
      : true;
  };

  // useEffect(() => {
  //   // check limit for transactions
  //   if (transactionConfigData) {
  //    console.log("Transaction config API response ----> ", transactionConfigData)
  //   //  console.log("Transaction config utils method ----> send external", isTransactionAllowed(transactionConfigData , 500))
  //   }
  // }, [transactionConfigData, isTransactionConfigSuccess, ])

  let WalletNavigation = () => {
   
    setTransaction({...transaction, asset:'SART'}) // TODO NANDANI :  need to change this logic
   
    const amount = formatter(transaction.amount, {
      asset: transaction.asset,
    });
    console.log("sarTokens ", sarTokens , transaction.amount , amount)
    const isTransactionPermitted =
      transactionConfigData?.per_TRANSACTION == null
        ? 'allowed'
        : isTransactionAllowed(transactionConfigData, Number(amount)); // transaction control and loading

    if (sarTokens < transaction.amount) {
      setShowError('');
    } 
    else if (isTransactionPermitted !== 'allowed') {
      //setShowError(isTransactionPermitted) // transaction control and loading
      setshowAlert({
        name: 'Internal Transfer',
        message: isTransactionPermitted,
      });
      setTextFieldAmount('');
      setTransaction({...transaction, amount: 0});
    } 
    else {
      setShowError('');
      navigation.navigate("TransactionConfirmationScreenTopUp" ,
      {
        transactionConfirmationDataParam : {
        requestPayload: {
          receiverEmail: contact?.email || '',
          asset: transaction.asset,
          amount: Number(transaction.amount)?.toFixed(2),
        },
      transactionType : "Transfer",
      screenType: "addTransaction",
      amountRequested :`${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(transaction.amount)?.toFixed(2)}`,
      totalAmount : `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(transaction.amount)?.toFixed(2)}`,
      totalFee : 0.00,
      tokenAsset :'SART',
      transactionConfigData: transactionConfigData,
      ttc : TransactionTypeControl.P2P_TRANSFER,
      feeTitle: 'Net Transfer Tokens',
      netAmountCalulated : `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${Number(transaction.amount)?.toFixed(2)}`,
      totalAmountText : "Tokens to be transfered"
      }
      })



    }
  };

  useEffect(() => {
    setTextFieldAmount('');
  }, []);

  useEffect(() => {
    // if (addTransactionData) {
    //   let response = addTransactionData;
    //   navigation.navigate('PaymentSuccess', {
    //     transactionId: (response as Record<'uuid', string>).uuid,
    //   });
    // }
    setTransaction({...transaction, cognitoUsername: ''});
    setTextFieldAmount('');
  }, [addTransactionData]);

  const clearClipboard = () =>{
    Clipboard.setString('')
   }

   const handleInput = (value: string) => {

//// 
const reg = /^-?\d*(\.\d*)?$/;
       // check for transaction limits

const isTransactionPermitted =
  transactionConfigData?.per_TRANSACTION == null
    ? 'allowed'
    : isTransactionAllowed(transactionConfigData, Number(value));

    console.log("hey hey !",  isTransactionPermitted)
const amountLimit = value.includes('.') ? 10 : 8;

                    setShowError('')


                    if (isTransactionPermitted !== 'allowed') {
                      setShowError(isTransactionPermitted)
                      setTextFieldAmount('');
                     // setTransaction({...transaction, amount: 0});
                    } 

                    if (sarTokens < Number(value)) {
                  
                      setShowError('Insufficient Funds for Transfer');
                      console.log("value** ", value)
                      setTextFieldAmount( value.indexOf('.') >= 0
                      ? value.substr(0, value.indexOf('.')) +
                          value.substr(value.indexOf('.'), 3)
                      : value.replace(/^0+/, ''),);
                     // setTransaction({...transaction, amount: 0})
                  }

                    if (value.length > amountLimit) {
                      return false;
                    }
                     else if (value.length <= 0) {
                      //setShowError('Please Enter valid amount');
                    }
                
                    if(value.startsWith('.')) {
                      value = value.replace(/^.+/, '0.');
                    } 
                
                    if (value === '-' || value == '0') {
                      setShowError('Please Enter valid amount');
                      setTextFieldAmount('');
                    } 

                 
                    
                    if (reg.test(value) || value == ' ' || value === '-') {
                      if (value.indexOf('.') >= 0 == false && value.length > 0) {
                        value = value.replace(/^0+/, '');
                      } else if (value.indexOf('.') >= 0) {
                        value = value.replace(/^0+/, '0');
                      }
                      //
                      setTextFieldAmount(
                        value.indexOf('.') >= 0
                          ? value.substr(0, value.indexOf('.')) +
                              value.substr(value.indexOf('.'), 3)
                          : value.replace(/^0+/, ''),
                      );
                      setTransaction({
                        ...transaction,
                        amount: parseFloat(value.indexOf('.') >= 0
                        ? value.substr(0, value.indexOf('.')) +
                            value.substr(value.indexOf('.'), 3)
                        : value.replace(/^0+/, ''),),
                      });
                
                    }

             
                    
  };

  return (
    <ScreenLayoutTab
      title={'Transfer'}
      showTitleHeader={true}
      useScrollView={false}>
      <ErrorModal error={isAddTransactionDataError} />
      <DismissKeyboard>
        <Container style={styles.container}>
          <Container
            spacing={2}
            justify="center"
            alignItems="center"
            noItemsStretch
            style={styles.availableBalnceView}>
            <Typography
              variant="heading"
              color="midDarkToneGray"
              fontFamily="Rubik-Regular">
              {'Available Balance'}
            </Typography>
            <Typography
              variant="subtitle"
              color="darkBlackBold"
              fontFamily="Rubik-Medium">
              {/* SARt {sarTokens} */}
              {instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} {" "}
              {/* {sartTxt} */}
              {formatter(sarTokens, {
                asset: 'SART',
              })}
            </Typography>
          </Container>

          <Container spacing={1.5} style={styles.blankDivider}>
            <Typography
              textAlign="left"
              fontFamily="Rubik-Medium"
              variant="heading"
              color="grayDark">
              {t('recipient_address')}
            </Typography>

            <Container
              direction="row"
              alignItems="flex-start"
              noItemsStretch
              justify="space-evenly"
              style={[styles.box, {padding: 10}]}>
              <TouchableOpacity
                onPress={() =>
                  navigation.navigate('SelectContact', {
                    cognitoUsername: transaction.cognitoUsername || '',
                    onCognitoUsernameChange: (cognitoUsername: string) =>
                      setTransaction({...transaction, cognitoUsername}),
                  })
                }>
                <View style={styles.recipientView}>
                  <Container
                    alignItems="flex-start"
                    justify="center"
                    style={{width: '88%'}}>
                    <Typography
                      variant="heading"
                      color="BlackLight"
                      fontFamily="Rubik-Regular"
                      textAlign="left">
                      {contact ? contact.nickname || contact.email || contact.phoneNumber || '' : t('please_select')}
                    </Typography>
                    {/* {!!contact && contact.email && (
                      <Typography variant="label" textAlign="left" color="grayMedium">
                        {contact ? contact.email : ""}
                      </Typography>
                    )} */}
                  </Container>

                  <Icon
                    size="lg"
                    name={'CaretRight'}
                    color="black"
                    style={{left: 10}}
                  />
                </View>
              </TouchableOpacity>
            </Container>
          </Container>

          <Container spacing={1.5} style={styles.blankDivider}>
            <Typography
              textAlign="left"
              fontFamily="Rubik-Medium"
              variant="heading"
              color="grayDark">
              {'Enter Amount'}
            </Typography>

            <Container direction="row" alignItems="center" style={[styles.box]}>
              <Container
                alignItems="center"
                justify="center"
                noItemsStretch
                style={styles.amountLabelView}>
                <Typography
                  variant="heading"
                  color="darkBlackBold"
                  fontFamily="Rubik-Regular"
                  textAlign="left">
                     {instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} {" "}
                  {/* {sartTxt} */}
                </Typography>
              </Container>
              <Container alignItems="center" style={styles.amountView}>
                <TextInput
                  value={textFieldAmount}
                  onTouchEnd={clearClipboard}
                //  maxLength={textTypeDecimal ? 11 : 8}
                  onChangeText={value => {
                    var good = value
                    // remove bad characters and multiple points
                   // .replace(/[^\d.]|\.(?=.*\.)/, '')
                    // remove any excess of 8 integer digits
                    .replace(/^(\d{8})\d+/, '$1')
                    // remove any excess of 2 decimal digits
                    .replace(/(\.\d\d).+/, '$1');
      
                    console.log("*----*  good " , good)
      
                    value = good
                    handleInput(value)
                   // setShowError('')
                  //   if (sarTokens < Number(value)) {
                     
                  //     if (
                  //       value.includes(',') ||
                  //       value.includes(' ') ||
                  //       value.includes('-') ||
                  //       value.includes('+') ||
                  //       value.includes('*')
                  //     ) {
                  //       setShowError('Please Enter valid amount');
                  //     }else {
                  //       setShowError('Insufficient Funds for Transfer');
                  //       console.log("value ", value)
                  //       setTextFieldAmount(ConTwoDecDigit(value));
                  //       setTransaction({...transaction, amount: 0});
                  //     }
                     
                  //   } 
                  //  else if (
                  //     value.includes(',') ||
                  //     value.includes(' ') ||
                  //     value.includes('-') ||
                  //     value.includes('+') ||
                  //     value.includes('*')
                  //   ) {
                  //     setShowError('Please Enter valid amount');
                  //    // setTextFieldAmount("");
                  //   } else if(/^0*$/.test(value)) {
                  //     setShowError('Please Enter valid amount');
                  //     setTextFieldAmount("");
                  //   } else {
                  //    // console.log("here",  Math.floor(Number(value)) , ConTwoDecDigit(value))
                  //     // to add 0 before decimal 
                  //     // if(!Math.floor(Number(value)) && !value.includes("0.") && value.includes(".")){
                  //     //   value = value.replace(".", "0.")
                  //     // }
                  //      // to add 0 before decimal 
                  //     setShowError('');
                  //     setTextFieldAmount(ConTwoDecDigit(value));

                  //     setTransaction({
                  //       ...transaction,
                  //       amount: parseFloat(ConTwoDecDigit(value)),
                  //     });
                  //   }
                  }}
                  style={styles.textFieldStyle}
                  placeholder={'0.00'}
                  textAlign={'right'}
                  keyboardType="number-pad"
                />
              </Container>
            </Container>

            <Typography variant="label"
              fontFamily="Rubik-Regular"
              color="error"
              textAlign="center">
              {showError.length > 0 ? showError : ''}
            </Typography>
          </Container>
          <View
            style={{
              position: 'absolute',
              top: isIOS ? height - 770 : height - 670,
              width: '100%',
              alignItems: 'center',
              paddingHorizontal: 20,
            }}>
            <TouchableOpacity
              style={[
                {
                  borderRadius: 6,
                  borderWidth: 1,
                  height: 50,
                  width: '100%',
                  alignItems: 'center',
                  justifyContent: 'center',
                },
                {
                  backgroundColor:
                  getDisabledState()
                      ? theme.colors.white
                      : theme.colors.orange,
                },
                {
                  borderColor:
                  getDisabledState()
                      ? theme.colors.black
                      : theme.colors.orange,
                },
              ]}
              disabled={getDisabledState()}
              onPress={() => {
                if (isAddTransactionDataLoading == true) {
                } else {
                  WalletNavigation();
                }
              }}>
              <Typography
                variant="heading"
                textAlign="center"
                color='darkBlackBold'
                fontFamily="Rubik-Medium">
                {' '}
                {isAddTransactionDataLoading
                  ? t('Please Wait  ...')
                  : `Transfer`}
              </Typography>
            </TouchableOpacity>
          </View>
        </Container>
        {showAlert.message.length > 0 && <ErrorModal error={showAlert} />}

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
      </DismissKeyboard>
    </ScreenLayoutTab>
  );
};

export default InternalTransfer;

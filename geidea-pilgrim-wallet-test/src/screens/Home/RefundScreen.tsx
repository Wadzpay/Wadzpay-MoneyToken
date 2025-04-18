import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {useTranslation, Trans} from 'react-i18next';
import {Alert} from 'react-native';
import CheckBox from '@react-native-community/checkbox';
import {calculateEstimatedFee, findPercentage, getAccountHolderName, getMaskedAccountNumber, sartTxt} from '~/utils';

import {
  LoadingSpinner,
  Button,
  Container,
  ScreenLayoutTab,
  theme,
  Link,
  Typography,
  Modal,
  Icon,
} from '~/components/ui';
//import Checkbox from 'expo-checkbox';

import React, {useContext, useEffect, useState} from 'react';
import {StyleSheet, TextInput, TouchableOpacity, View} from 'react-native';

import BankDetailsComponent from '~/components/ui/BankDetailComponent';
import {useValidationSchemas, RefundForm} from '~/constants';
import {useForm} from 'react-hook-form';

import {HomeStackNavigatorParamList} from '~/components/navigators/HomeStackNavigator';
import {yupResolver} from '@hookform/resolvers/yup';
import WebView from 'react-native-webview';
import {UserContext} from '~/context';
import {showToast, isIOS} from '~/utils';
import {useRefundSart, useTransactionValidationConfig} from '~/api/user';
import useFormatCurrencyAmount from '~/helpers/formatCurrencyAmount';
import {useTopupSart, useGetFiatExchangeRate} from '~/api/user';
import {number} from 'yup';
import { TransactionTypeControl } from '~/api/constants';
import { tr } from 'date-fns/locale';
import { CommonActions } from '@react-navigation/native';

type Props = NativeStackScreenProps<HomeStackNavigatorParamList>;

const RefundScreen: React.FC<Props> = ({navigation, route}: Props) => {
  const {t} = useTranslation();
  const {refundSchema} = useValidationSchemas();
  const [isWalletSelected, setIsWalletSelected] = useState(false);
  const [isBankAccountSelected, setIsBankAccountSelected] = useState(false);
  const [isChecked, setChecked] = useState(false);
  const [isVisible, setIsVisible] = useState(false);
  const [url, setUrl] = useState("");
  const {sarTokens, updateSarTokens, user , isActivationFeeCharged, instDetails} = useContext(UserContext);
  const [refundClicked, setRefundClicked] = useState(false);
  const formatter = useFormatCurrencyAmount();
  const [value, setValue] = useState('MYR');
  const [isPageLoadEnabled, setPageLoadEnabled] = useState(true);
  const [showConfirmationBox, setShowConfirmationBox] = useState(false);
  const [showFeeBreakUpModal, setShowFeeBreakUpModal] = useState(false);
  const [isFinalAmountNegative, setIsFinalAmountNegative] = useState(false);
  const [totalAmount, setTotalAmount] = useState<any>();

  let alertMsg = !isWalletSelected
              ? t('You want to redeem all the unspent tokens to your bank ')
              : t(
                  'You want to redeem all the unspent tokens to your bank wallet ',
                );

  const {
    data: fiatExchangeRatesData,
    refetch: refetchExchangeRates,
    isFetching: isFetchingExhchangeRates,
    isSuccess: isExchangeRateSuccess,
  } = useGetFiatExchangeRate(value + '&transactionType=REFUND'); // from DC to fiat conversion

  const {
    data: transactionConfigData,
    refetch: refetchTransactionConfigData,
    isSuccess: isTransactionConfigSuccess,
    error: transactionConfigError,
  } = useTransactionValidationConfig(TransactionTypeControl.UNSPENT_DIGITAL_CURRENCY_REFUND); // fee config and transaction limits

  const {
    control,
    handleSubmit,
    formState: {errors},
    getValues,
  } = useForm<RefundForm>({
    resolver: yupResolver(refundSchema),
  });
  const selectBankAccountWallet = (value: boolean) => {
    setIsWalletSelected(value);
    setIsBankAccountSelected(false);
  };
  const selectBankAccount = (value: boolean) => {
    setIsWalletSelected(false);
    setIsBankAccountSelected(value);
  };

  // ************************ Creating Webservice API Interactions ************************
  const {
    mutate: refundToken,
    isLoading: refundSartLoading,
    error: refundSartError,
    isSuccess: refundSartSuccess,
    data: refundSartData,
  } = useRefundSart();

    useEffect(() => {
    // check limit for transactions
    
    if (transactionConfigData) {
     //console.log("Transaction config API response refund----> ", sarTokens > 0 )
     const totalDeduction = sarTokens > 0  && transactionConfigData?.feeConfig ?  calculateDeductions() : 0.00 // Calculate total fees Applied 
    // console.log("ttotalDeduction",totalDeduction, "fiatExchangeRatesData?.SAR ", fiatExchangeRatesData?.SAR)
     let totalAmount  =  ((Number(sarTokens) - Number(totalDeduction)) / fiatExchangeRatesData?.SAR)
     console.log("totalDeduction ", totalDeduction, "amount ", sarTokens, "totalAmount ", totalAmount, " * fiatExchangeRatesData?.SAR)?.toFixed(2)" ,   fiatExchangeRatesData?.SAR)
     console.log("totalAmount " , totalAmount.toFixed(2))

     setTotalAmount(totalAmount.toFixed(2));

     if (totalAmount > 0) {
       setIsFinalAmountNegative(false);
     } else {
       setIsFinalAmountNegative(true);
     }  
    }
  }, [transactionConfigData, isTransactionConfigSuccess,fiatExchangeRatesData ])


  const calculateDeductions = () => {
    if (transactionConfigData?.feeConfig) {
      const totalFee = transactionConfigData?.feeConfig.reduce(
        (total: number, curr: any) => {
          if (curr.feeType == 'Percentage') {
            let feeAmount = findPercentage(curr.feeAmount, Number(sarTokens));
            console.log("feeAmount ", feeAmount)
            if(curr.feeMinimumAmount && feeAmount < curr.feeMinimumAmount || feeAmount == curr.feeMinimumAmount) {
              total = total + curr.feeMinimumAmount;
            } else if(curr.feeMaximumAmount && feeAmount > curr.feeMaximumAmount ||feeAmount == curr.feeMaximumAmount ) {
              total = total + curr.feeMaximumAmount;
            } else {
              total = total + feeAmount;
            }
          } else {
            total = total + curr.feeAmount;
          }
          return total;
        },
        0,
      );
      console.log('totalFee nandani ---->', totalFee , totalFee.toFixed(2));
      return totalFee
    }  else {
      return 0.00
    }
   
  }

  const getAllFeeApplied = () => {
    if (transactionConfigData?.feeConfig) {
      const FeeApplied = transactionConfigData?.feeConfig.map((fee : any)=>{
        if (fee.feeType == 'Percentage') {
          let feeAmount = findPercentage(fee.feeAmount, Number(sarTokens));

          if(fee?.feeMinimumAmount && feeAmount < fee.feeMinimumAmount || feeAmount == fee.feeMinimumAmount) {
            fee[fee.feeAmount] = fee.feeMinimumAmount;
          } else if(fee?.feeMaximumAmount && feeAmount > fee.feeMaximumAmount ||feeAmount == fee.feeMaximumAmount ) {
            fee[fee.feeAmount] = fee.feeMaximumAmount;
          } else {
            fee[fee.feeAmount] = feeAmount;
          }

        } else {
          fee[fee.feeAmount] = fee.feeAmount;
        }

        return {
          ['currencyType'] : fee.currencyType,
          ['feeId']: fee.feeId,
          ['feeName']: fee.feeName,
          ['feeCalculatedAmount']: fee[fee.feeAmount].toFixed(2),
          ['feeType']: fee.feeType,
          ['enteredAmount']: Number(sarTokens),
          ['feeAmount'] : fee.feeAmount
        };
      })
      console.log("FeeApplied ", FeeApplied)
      return FeeApplied
    }
  }

  const onRefundClick = () => {
    console.log("onrefundclick" , transactionConfigData?.feeConfig? (sarTokens - calculateDeductions())?.toFixed(2) : sarTokens )
    let calculatedFee  : number =  transactionConfigData?.feeConfig ?  calculateDeductions()?.toFixed(2) : 0 
   // if (sarTokens?.toFixed(2) > calculatedFee) {
    let fiatClaculated = transactionConfigData?.feeConfig? sarTokens - calculateDeductions()?.toFixed(2) : sarTokens 
      setShowConfirmationBox(false);
      //setRefundClicked(true);
      navigation.navigate("TransactionConfirmationScreenTopUp" ,
            {
              transactionConfirmationDataParam : {
              requestPayload: {
                tokenAsset: 'SART',
                amount: transactionConfigData?.feeConfig? sarTokens - calculateDeductions()?.toFixed(2) : sarTokens ,
                bankAccountNumber: isBankAccountSelected ? user?.userBankAccount[0].bankAccountNumber : '',
                isFromWallet: isBankAccountSelected ? false : true,
                fiatAsset: `${instDetails?.acquiringCurrency ? instDetails?.acquiringCurrency  : ""}`,
                fiatAmount: (Number(fiatClaculated) / fiatExchangeRatesData?.SAR).toFixed(2),
                feeConfigData: getAllFeeApplied() ? getAllFeeApplied() : null ,
                totalFeeApplied:calculateDeductions() ? Number(calculateDeductions()?.toFixed(2)) : 0
              },
            isBankAccountSelected:isBankAccountSelected, 
            transactionType : "Redeem Unspent", // if refund unspent  transaction type changed to refund globally in project 
            screenType: "refundUnspent",
            amountRequested : `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${sarTokens.toFixed(2)}`,
            totalAmount :  `${instDetails?.acquiringCurrency ? instDetails?.acquiringCurrency  : ""} ${Number(totalAmount).toFixed(2)}`,
            totalFee : calculateDeductions(),
            tokenAsset :'SART',
            transactionConfigData: transactionConfigData,
            ttc : TransactionTypeControl.UNSPENT_DIGITAL_CURRENCY_REFUND,
            feeTitle: 'Net Redeem Tokens',
            isFinalAmountNegative: isFinalAmountNegative,
            // Number(amount)?.toFixed(2) - Number(calculateDeductions()?.toFixed(2))
            netAmountCalulated : `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${transactionConfigData?.feeConfig? (sarTokens - calculateDeductions())?.toFixed(2) : sarTokens }`,
            totalAmountText : "Amount to be redeem"
          }
            }
          )
    // } else {
    //   setShowConfirmationBox(false);
    //   setShowFeeBreakUpModal(true)
     
    // }
  }
  // ************************ Handling Server Errors ************************

  useEffect(() => {

    if (refundSartData) {
      let updatedSarToken: number = 0;
      updateSarTokens(updatedSarToken);
      const message = 'Successfully refunded unspent amount '; //to your bank
      isIOS ? Alert.alert(message) : showToast(message);
      setIsBankAccountSelected(false);
      setIsWalletSelected(false);
      setChecked(false);
      setRefundClicked(false);
      navigation.goBack();
    }
  }, [refundSartData]);
  useEffect(() => {

    if(!isActivationFeeCharged) {
      setShowConfirmationBox(true)
    }
  }, []);

  useEffect(() => {
    if (refundSartError) {
      let message = refundSartError.message;
      isIOS ? Alert.alert(message) : showToast(message);
      setIsBankAccountSelected(false);
      setIsWalletSelected(false);
      setChecked(false);
      setRefundClicked(false);
    }
  }, [refundSartError]);

  return (
    <ScreenLayoutTab
      title={t('Redeem Unspent')}
      leftIconName="ArrowLeft"
      useScrollView={true}
      onLeftIconClick={() => {
        if (isPageLoadEnabled === true) {
          setPageLoadEnabled(false);
          navigation.goBack();
        }
      }}>
      <Container justify={'center'} alignItems={'center'}>
        <Container
          style={{marginVertical: 25}}
          noItemsStretch
          justify={'center'}
          alignItems={'center'}>
          <Typography
            variant="body"
            color="midDarkToneGray"
            fontFamily="Rubik-Regular"
            textAlign="center">
            {t('Available Redeem tokens')}
          </Typography>
        </Container>
      </Container>
      <Container direction="column" alignItems="center">
        <Container
          alignItems="center"
          noItemsStretch
          justify="flex-start"
          style={[styles.box]}
          direction="row">
          <Typography
            variant="body"
            color="grayMedium"
            fontFamily="Rubik-Regular"
            textAlign="left">
            {/* {sartTxt} */}
            {instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} {""}
          </Typography>
          <Typography
            style={{
              paddingHorizontal: 10,
            }}
            variant="subtitle"
            color="grayMedium"
            fontFamily="Rubik-Medium"
            textAlign="left">
            {formatter(sarTokens, {
              asset: 'SART',
            })}
          </Typography>
        </Container>
        <Typography
          fontFamily="Rubik-Regular"
          variant="chip"
          color="grayMedium"
          textAlign="center">
          {t('All available tokens will be redeemed in your account')}
        </Typography>
      </Container>

      <Container
        noItemsStretch
        alignItems="center"
        justify="center"
        style={{marginTop: 2}}>
        {sarTokens && Number(sarTokens) > 0 ? (
          <Container direction="row" alignItems="center" justify="center">
            <Typography
              variant="label"
              fontFamily="Rubik-Regular"
              color="BlackLight"
              textAlign="center">
              {`You get ${instDetails?.acquiringCurrency ? instDetails?.acquiringCurrency  : ""} ${(totalAmount)}`}
            </Typography>
            {/* {sarTokens?.toFixed(2) <
              Number(calculateDeductions()?.toFixed(2)) && (
              <TouchableOpacity
                style={{paddingHorizontal: 5}}
                onPress={() => {
                  setShowFeeBreakUpModal(true);
                }}>
                {<Icon name={'Info'} size="xs" color="black" />}
              </TouchableOpacity>
            )} */}
          </Container>
        ) : (
          <Typography
            variant="label"
            fontFamily="Rubik-Regular"
            color="error"
            textAlign="center">
            {`No Balance to be Redeemed`}
          </Typography>
        )}
      </Container>

      <View
        style={{
          width: '100%',
          height: 0.5,
          backgroundColor: theme.colors.gray.light,
          marginVertical: 15,
        }}></View>
      <Typography
        style={{paddingLeft: 20}}
        variant="body"
        color="blackishGray"
        fontFamily="Rubik-Medium"
        textAlign="left">
        {t('Select Redeem to')}
      </Typography>

      <BankDetailsComponent
        savingAccountNumber={'xxxx xxxx xxxx ' + getMaskedAccountNumber(user)}
        bankHolderName={getAccountHolderName(user)}
        type={instDetails?.institutionName}
        selectedBank={value => {
          selectBankAccount(value);
        }}
        isBankSelect={isBankAccountSelected}
      />
      <BankDetailsComponent
       bankHolderName={getAccountHolderName(user) || user?.attributes.email}
        type={instDetails?.institutionName} 
        selectedBank={value => {
          selectBankAccountWallet(value);
        }}
        isBankSelect={isWalletSelected}
      />

      <BankDetailsComponent
        bankHolderName={'Other'}
        type={'Other Payments'}
        isDisabled={true}
        // selectedBank = {(value)=>{selectBankAccountWallet(value)}}
        isBankSelect={false}
      />

      <Container
        direction="row"
        noItemsStretch
        justify="flex-start"
        style={{marginTop: 30, marginLeft: 20}}>
        <TouchableOpacity
          style={{flexDirection: 'row', alignItems: 'center'}}
          onPress={() => {
            setChecked(!isChecked);
          }}>
          <CheckBox
            onValueChange={() => setChecked(!isChecked)}
            value={isChecked}
            tintColors={{true: theme.colors.primary}}
            //color={isChecked ? theme.colors.primary : undefined}
          />
          <Typography
            fontFamily="Rubik-Regular"
            variant="chip"
            color="darkBlackBold">
            {' '}
            I accept the{' '}
            <Typography
              fontFamily="Rubik-Regular"
              variant="chip"
              style={{
                color: theme.colors.primary,
                textDecorationLine: 'underline',
              }}
              suppressHighlighting={true}
              onPress={() => {
                setIsVisible(true);
                setUrl(
                  'https://wadzpay.com/legal/conditions-of-access-to-the-wadzpay-website/',
                );
              }}>
              {'Terms of Services'}
            </Typography>
            {` and `}
            <Typography
              fontFamily="Rubik-Regular"
              variant="chip"
              style={{
                color: theme.colors.primary,
                textDecorationLine: 'underline',
              }}
              suppressHighlighting={true}
              onPress={() => {
                setIsVisible(true);
                setUrl(
                  'https://wadzpay.com/legal/conditions-of-access-to-the-wadzpay-website/',
                );
              }}>
              {'Privacy Policy'}
            </Typography>
          </Typography>
        </TouchableOpacity>
      </Container>
      <Container
        justify="flex-end"
        style={{flex: 1, paddingLeft: 20, paddingRight: 20, height: 100}}>
        <TouchableOpacity
          disabled={
            isChecked && (isWalletSelected || isBankAccountSelected) && sarTokens > 0 &&  (sarTokens?.toFixed(2) > Number(calculateDeductions()?.toFixed(2)))
              ? false
              : true
          }
          style={[
            {
              borderRadius: 6,
              borderWidth:
              isChecked && (isWalletSelected || isBankAccountSelected) && sarTokens > 0  && (sarTokens?.toFixed(2) > Number(calculateDeductions()?.toFixed(2)))

                  ? 0
                  : 1,
              height: 50,
              width: '100%',
              alignItems: 'center',
              justifyContent: 'center',
              position: 'relative',
              // top:60
            },
            {
              backgroundColor:
              isChecked && (isWalletSelected || isBankAccountSelected) && sarTokens > 0 && (sarTokens?.toFixed(2) > Number(calculateDeductions()?.toFixed(2)))

                  ? theme.colors.primary
                  : theme.colors.white,
            },
            {
              borderColor:
              isChecked && (isWalletSelected || isBankAccountSelected) && sarTokens > 0 && (sarTokens?.toFixed(2) > Number(calculateDeductions()?.toFixed(2)))

                  ? theme.colors.primary
                  : theme.colors.black,
            },
          ]}
          onPress={() => {
            setShowConfirmationBox(true);
            // const message = 'There is no amount to refund';

            // sarTokens < 1
            //   ? alert(t(message))
            //   : Alert.alert('Are you sure ?', alertMsg, [
            //       {
            //         text: t('Yes'),
            //         onPress: () => {
            //           //   let updatedSarToken : number = 0
            //           //   console.log("updatedSarToken ", updatedSarToken)
            //           //   updateSarTokens(updatedSarToken)
            //           // const message=  "Successfully refunded unspent amount to your bank"
            //           //   isIOS ? alert(message) : showToast(message)
            //           // navigation.goBack()

            //           onRefundClick();
            //         },
            //       },
            //       {text: t('No')},
            //     ]);
          }}>
          <Container direction="row" noItemsStretch>
            <Typography
              style={[{paddingRight: 10}]}
              variant="label"
              fontWeight="bold"
              textAlign="center"
              fontFamily="Helvetica-Bold">
              Continue
            </Typography>
            {refundSartLoading && <LoadingSpinner color="white" />}
          </Container>
        </TouchableOpacity>
      </Container>

      {showConfirmationBox && (
        <Modal
          variant="center"
          isVisible={showConfirmationBox}
          onDismiss={() =>  isActivationFeeCharged && setShowConfirmationBox(false)}
          dismissButtonVariant="none"
          swipeDirection={['down']}>
          {
          !isActivationFeeCharged ? 
          (
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
          ) : 
          sarTokens < 0 ? (
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
                {t('There is no tokens to be redeemed')}
              </Typography>
            </Container>
          ) :
          (
            <Container
              alignItems="flex-start"
              noItemsStretch
              justify="flex-start"
              spacing={2}>
              <Typography
                variant="label"
                fontFamily="Rubik-Medium"
                fontWeight="bold"
                color="darkBlackBold">
                {t('Are you sure?')}
              </Typography>
              <Typography
                variant="label"
                fontFamily="Rubik-Regular"
                fontWeight="regular"
                color="darkBlackBold">
                {alertMsg}
              </Typography>
            </Container>
          )}
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
                {!isActivationFeeCharged && 
                  navigation.dispatch(
                    CommonActions.reset({
                      index: 0,
                      routes: [
                        { name: 'Home' }
                      ],
                    })
                  )
                }
                 
              }}>
              <Typography
                color="darkBlackBold"
                variant="heading"
                textAlign="center"
                fontFamily="Rubik-Medium">
                {!isActivationFeeCharged || sarTokens < 0  ? `OK` : `No`}
              </Typography>
            </TouchableOpacity>
            {!isActivationFeeCharged  || sarTokens < 0 ? undefined : (
              <TouchableOpacity
                style={styles.confirmButtonClick}
                onPress={() => {
                  onRefundClick();
                }}>
                <Typography
                  color="darkBlackBold"
                  variant="heading"
                  textAlign="center"
                  fontFamily="Rubik-Medium">
                  Yes
                </Typography>
              </TouchableOpacity>
            )}
          </Container>
        </Modal>
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
      {/* {showFeeBreakUpModal && transactionConfigData && (
        <Modal
          variant="bottom"
          isVisible={showFeeBreakUpModal}
          onDismiss={() => setShowFeeBreakUpModal(false)}
          dismissButtonVariant="none"
          modalBgColor={'#F0F0F0'}
          contentStyle={{height: '25%'}}>
          <Container
            style={{marginTop: 40, marginHorizontal: 20}}
            justify={'space-between'}
            alignItems={'center'}>
            <Typography
              variant="label"
              color="darkBlackBold"
              fontFamily="Rubik-Medium"
              textAlign="left">
              {t('FEE SUMMARY')}
            </Typography>
            <Container
              spacing={2}
              alignItems="center"
              justify="center"
              style={{marginTop: 30}}>
              <Container justify="space-between" direction="row">
                <Typography
                  textAlign="left"
                  color="darkBlackBold"
                  fontFamily="Rubik-Medium">
                  Amount
                </Typography>
                <Typography
                  textAlign="left"
                  color="darkBlackBold"
                  fontFamily="Rubik-Medium">{sartTxt} {sarTokens}</Typography>
              </Container>
              {transactionConfigData?.feeConfig &&
                transactionConfigData?.feeConfig?.map((x: any) => {
                  return (
                    <Container
                      justify="space-between"
                      direction="row"
                      style={{marginVertical: 10}}>
                      <Typography
                        textAlign="left"
                        color="darkBlackBold"
                        fontFamily="Rubik-Medium">
                        {x?.feeName}
                      </Typography>
                      <Typography
                        textAlign="left"
                        color="darkBlackBold"
                        fontFamily="Rubik-Medium">
                        {x?.currencyType} {calculateDeductions()}
                      </Typography>
                    </Container>
                  );
                })}
                 <Container justify='space-between' direction='row'>
                      <Typography textAlign='left' color= {isFinalAmountNegative ? "error" : 'error'}  fontFamily='Rubik-Medium'>Net Redeem Amount</Typography>
                      <Typography textAlign='left' color= {isFinalAmountNegative ? "error" : 'error'}  fontFamily='Rubik-Medium'>{sartTxt} {(sarTokens - calculateDeductions() )?.toFixed(2)}</Typography>
                      </Container>
                </Container>
            </Container>
        </Modal>
      )} */}
    </ScreenLayoutTab>
  );
};
const styles = StyleSheet.create({
  box: {
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 10,
    padding: 20,
    height: 60,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: '#C4C4C4',
    backgroundColor: '#F6F6F6',
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
  },
  confirmButtonClick:{
    alignItems:"center",
    justifyContent:"center",
    width:100,
    height:40,
    borderRadius:5,
    backgroundColor:theme.colors.primary,
    borderColor:theme.colors.primary,
    borderWidth:1
  }
});

export default RefundScreen;

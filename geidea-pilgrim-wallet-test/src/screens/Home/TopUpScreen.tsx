import React, {useContext, useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {
  Alert,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import {StackScreenProps} from '@react-navigation/stack';
import {
  Button,
  Container,
  ErrorModal,
  Icon,
  LoadingSpinner,
  Modal,
  ScreenLayoutTab,
  theme,
  Typography,
  UnderDevelopmentMessage,
} from '~/components/ui';
import {HomeStackNavigatorParamList} from '~/components/navigators/HomeStackNavigator';
import Contact from '~/icons/Contact';
import BankDetailsComponent from '~/components/ui/BankDetailComponent';
import {useForm} from 'react-hook-form';
import {yupResolver} from '@hookform/resolvers/yup';
//import Checkbox from "expo-checkbox"
import CheckBox from '@react-native-community/checkbox';
import WebView from 'react-native-webview';
import {UserContext} from '~/context';
import {
  showToast,
  isIOS,
  ConTwoDecDigit,
  isTransactionAllowed,
  validateInitialLoading,
  sartTxt,
  getErrorMessage,
  getMaskedAccountNumber,
  getAccountHolderName,
  findPercentage,
} from '~/utils';
import {
  useTopupSart,
  useGetFiatExchangeRate,
  useTransactionValidationConfig,
} from '~/api/user';
import {TransactionTypeControl} from '~/api/constants';
import Info from '~/icons/Info';

type Props = StackScreenProps<HomeStackNavigatorParamList, 'TopUpScreen'>;

const TopUpScreen: React.FC<Props> = ({navigation, route}: Props) => {
  const {t} = useTranslation();
  const {user, instDetails} = useContext(UserContext);
  const [isChecked, setChecked] = useState(false);
  const [isWalletSelected, setIsWalletSelected] = useState(false);
  const [isBankAccountSelected, setIsBankAccountSelected] = useState(false);
  const [amount, setAmount] = useState('');
  const [isEditable, setIsEditable] = useState(true);
 //const [totalFee, setTotalFee] = useState('');
  const [totalAmount, setTotalAmount] = useState<any>();
  const [totalDeduction, setTotalDeduction] = useState<any>();
  

  const [url, setUrl] = useState("");
  const [isVisible, setIsVisible] = useState(false);
  const [value, setValue] = useState<{value: any}>('MYR');
  const [isValidationErrorTextInput, setIsValidationErrorTextInput] =
    useState(false);
  const [isPageLoadEnabled, setPageLoadEnabled] = useState(true);
  const [topupClicked, setTopupClicked] = useState(false);
  const [isAmountError, setAmountError] = useState(false);
  const [isFinalAmountNegative, setIsFinalAmountNegative] = useState(false);
  const [showFeeBreakUpModal, setShowFeeBreakUpModal] = useState(false);
  // const [isFinalAmountNegative, setIsFinalAmountNegative] = useState(false);
  const [showAlert, setshowAlert] = useState({name: '', message: ''});
  const [hitAPI, sethitAPI] = useState(true);
// const [textTypeDecimal, setIsTextTypeDecimal] = useState(false);
  const [ttc, setTtc] = useState(TransactionTypeControl.SUBSEQUENT_LOADING);

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
    data: fiatExchangeRatesData,
    refetch: refetchExchangeRates,
    isFetching: isFetchingExhchangeRates,
    isSuccess: isExchangeRateSuccess,
  } = useGetFiatExchangeRate(value + '&transactionType=LOAD');

  const {
    data: transactionConfigData,
    refetch: refetchTransactionConfigData,
    isSuccess: isTransactionConfigSuccess,
    error: transactionConfigError,
  } = useTransactionValidationConfig(ttc, hitAPI);

  const {
    mutate: topupSart,
    isLoading: topupSartLoading,
    error: topupSartError,
    isSuccess: topupSartSuccess,
    data: topupSartData,
  } = useTopupSart();

  // ************************ Handling Server Errors ************************
  useEffect(() => {
    refetchTransactionConfigData();
  }, [ttc, hitAPI]);


  

  // ************************ Handling Server Responses ************************
 
  const getUniqueFeeList = (feeConFigList : any) => {
    if (feeConFigList) {
      let totaloadingfee = 0
      let otherFee = 0
      let LoadingFeeObj = {}
      let otherFeeObj = {}
      const FeeAppliedUnique = feeConFigList.map((fee)=>{
        if (fee.feeNameId == 'WF_006') {
          
            if (fee.feeType == 'Percentage') {
              let feeAmount = findPercentage(fee.feeAmount, Number(amount));
             // console.log("feeAmountfeeAmountfeeAmountfeeAmount ", feeAmount)

              if(fee?.feeMinimumAmount && feeAmount < fee.feeMinimumAmount || feeAmount == fee.feeMinimumAmount) {
                fee[fee.feeAmount] = fee.feeMinimumAmount;
              } else if(fee?.feeMaximumAmount && feeAmount > fee.feeMaximumAmount ||feeAmount == fee.feeMaximumAmount ) {
                fee[fee.feeAmount] = fee.feeMaximumAmount;
              } else {
                fee[fee.feeAmount] = feeAmount;
              }

              // if (
              //   feeAmount > fee.feeMinimumAmount && feeAmount < fee.feeMaximumAmount
              // ) {
              //   fee[fee.feeAmount] = fee.feeMinimumAmount;
              // } else if (feeAmount > fee.feeMaximumAmount) {
              //   fee[fee.feeAmount] = fee.feeMaximumAmount;
              // } else {
              //   console.log('this execute');
              //   fee[fee.feeAmount] = feeAmount;
              // }
            } else {
              fee[fee.feeAmount] = fee.feeAmount;
            }
            totaloadingfee = totaloadingfee + fee[fee.feeAmount];
            LoadingFeeObj = {
              ['feeName']: fee.feeName,
              ['feeCalculatedAmount']: totaloadingfee.toFixed(2),
              ['currencyType']: fee.currencyType
            };
            //console.log('nikki', totaloadingfee, LoadingFeeObj);
      } else { 
        if (fee.feeType == 'Percentage') {
          let feeAmount = findPercentage(fee.feeAmount, Number(amount));
          
          if(fee?.feeMinimumAmount && feeAmount < fee?.feeMinimumAmount || feeAmount == fee?.feeMinimumAmount) {
                fee[fee.feeAmount] = fee.feeMinimumAmount;
              } else if(fee?.feeMaximumAmount && feeAmount > fee?.feeMaximumAmount ||feeAmount == fee?.feeMaximumAmount) {
                fee[fee.feeAmount] = fee.feeMaximumAmount;
              } else {
               console.log("hey")
                fee[fee.feeAmount] = feeAmount;
              
              }
              console.log(" fee[fee.feeAmount]  ",  fee[fee.feeAmount] )
          // if (
          //   feeAmount > fee?.feeMinimumAmount &&
          //   feeAmount < fee?.feeMaximumAmount
          // ) {
          //   fee[fee.feeAmount] = fee?.feeMinimumAmount ? fee?.feeMinimumAmount : feeAmount ;
          // } else if (feeAmount > fee?.feeMaximumAmount) {
          //   fee[fee.feeAmount] = fee?.feeMaximumAmount ? fee?.feeMaximumAmount : feeAmount ;
          // } else {
          //   fee[fee.feeAmount] = feeAmount;
          // }
        } else {
          fee[fee.feeAmount] = fee.feeAmount;
        }

        
        otherFee = otherFee + fee[fee.feeAmount];
        console.log('this execute ******** otherFee' , otherFee);
        otherFeeObj = {
              ['feeName']: fee.feeName,
              ['feeCalculatedAmount']: otherFee.toFixed(2),
              ['currencyType']: fee.currencyType
            };
           // console.log('nikki', otherFee, [otherFeeObj ,LoadingFeeObj ] );
      }

      })

      console.log("FeeAppliedUnique ",otherFee > 0 && LoadingFeeObj?.feeCalculatedAmount,  [otherFeeObj ,LoadingFeeObj ])

      if(LoadingFeeObj?.feeCalculatedAmount > 0  &&  otherFee > 0 ) {
        return [otherFeeObj,LoadingFeeObj]
      } else if(otherFee > 0) {
        return [otherFeeObj]
      }  else {
        return [LoadingFeeObj]
      }
       
    }
  }

  
  useEffect(() => {
    // check limit for transactions
    console.log("transactionConfigData--->",transactionConfigData?.feeConfig)

      // // TODO : Nandani 
      // const feeConFigList = transactionConfigData?.feeConfig
      // const uniqueList = getUniqueFeeList(feeConFigList);
      // console.log(uniqueList);
      // // TODO :  Nandani

    if (transactionConfigData?.initialLoading) {
      setTtc(
        transactionConfigData?.initialLoading
          ? TransactionTypeControl.INITIAL_LOADING
          : TransactionTypeControl.SUBSEQUENT_LOADING,
      );
      sethitAPI(true);
      //console.log("Transaction config utils method ttc ----> ", ttc, hitAPI , transactionConfigData?.initialLoading)
    }

    // if(transactionConfigData?.feeConfig) {
    //   const totalDeduction = amount.length > 0 ?  calculateDeductions() : 0.00 // Calculate total fees Applied 
    //   setTotalDeduction(totalDeduction)
    // }
  }, [transactionConfigData, isTransactionConfigSuccess]);

  // useEffect(() => {
  //     // calculate all fee & charges
  //     const totalDeduction = amount.length > 0 ?  calculateDeductions() : 0.00 // Calculate total fees Applied 
  //     let totalAmount  =  (Number(amount) - Number(totalDeduction)) * fiatExchangeRatesData?.SAR
  //     console.log("totalDeduction ", totalDeduction, "amount ", amount, "totalAmount ", totalAmount.toFixed(2), " * fiatExchangeRatesData?.SAR)?.toFixed(2)" ,   fiatExchangeRatesData?.SAR)
      
  //     if (totalAmount > 0) {
  //       setTotalAmount(totalAmount.toFixed(2));
  //       setIsFinalAmountNegative(false);
  //       //setAmountError(false)
  //     } else {
  //       setIsFinalAmountNegative(true);
  //       //setAmountError(true)
  //     }  
  // }, [amount])



  // ************************ Confirm button click ************************

  const calculateDeductions = () => {
    if (transactionConfigData?.feeConfig) {
      const totalFee = transactionConfigData?.feeConfig.reduce(
        (total: number, curr: any) => {
          if (curr.feeType == 'Percentage') {
            let feeAmount = findPercentage(curr.feeAmount, Number(amount));
            console.log("feeAmount ", feeAmount)
            console.log("feeAmountfeeAmountfeeAmountfeeAmount ", feeAmount)
              
              if(curr.feeMinimumAmount && feeAmount < curr.feeMinimumAmount || feeAmount == curr.feeMinimumAmount) {
                total = total + curr.feeMinimumAmount;
              } else if(curr.feeMaximumAmount && feeAmount > curr.feeMaximumAmount ||feeAmount == curr.feeMaximumAmount ) {
                total = total + curr.feeMaximumAmount;
              } else {
                total = total + feeAmount;
              }
            // if (
            //   feeAmount > curr.feeMinimumAmount && feeAmount < curr.feeMaximumAmount
            // ) {
            //   total = total + curr.feeMinimumAmount;
            // } else if (feeAmount > curr.feeMaximumAmount) {
            //   total = total + curr.feeMaximumAmount;
            // } else {
            //   console.log("this execute")
            //   total = total + feeAmount;
            // }
            console.log("total ", total)
          } else {
            total = total + curr.feeAmount;
          }
          return total;
        },
        0,
      );
      console.log('totalFee ---->', totalFee , totalFee.toFixed(2));
      return totalFee 
    }
    return 0
  }

  const getAllFeeApplied = () => {
    if (transactionConfigData?.feeConfig) {
      const FeeApplied = transactionConfigData?.feeConfig.map((fee : any)=>{
        if (fee.feeType == 'Percentage') {
          let feeAmount = findPercentage(fee.feeAmount, Number(amount));

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
          ['enteredAmount']: amount,
          ['feeAmount'] : fee.feeAmount
        };
      })
      console.log("FeeApplied ", FeeApplied)
      return FeeApplied
    }
  }
  function onConfirmClick() {
    setshowAlert({name: '', message: ''});
    let isTransactionPermitted;
    // Check transaction limit and control for load Token
    if (ttc == TransactionTypeControl.SUBSEQUENT_LOADING) {
      isTransactionPermitted =
        transactionConfigData?.per_TRANSACTION == null
          ? 'allowed'
          : isTransactionAllowed(
              transactionConfigData,
              Number(amount),
            ); // transaction control and loading
    } else {
      // initail loading
      isTransactionPermitted = validateInitialLoading(
        transactionConfigData,
        Number(amount),
      );
    }

    // call transaction summary screen 
    if (!(user && user?.userBankAccount)) {
      setshowAlert({name: 'TopUp', message: "Please add bank account"});
    }else if (isTransactionPermitted !== 'allowed') {
      setshowAlert({name: 'TopUp', message: isTransactionPermitted});
    } 
    else {
      let totalFee
      let amountInSar = ( Number(amount) * fiatExchangeRatesData?.SAR).toFixed(2)
        if (!isValidationErrorTextInput) {
          if(transactionConfigData?.feeConfig) {
            totalFee = amount.length > 0 ?  calculateDeductions() : 0.00 // Calculate total fees Applied 
           // setTotalDeduction(totalDeduction)
          }
          const amountToDebit = totalFee ? (Number(amount) + Number(totalFee)).toFixed(2) : amount
            navigation.navigate("TransactionConfirmationScreenTopUp" ,
            {
              transactionConfirmationDataParam : {
              requestPayload: {
              tokenAsset: 'SART',
              fiatAsset: `${instDetails?.acquiringCurrency ? instDetails?.acquiringCurrency  : ""}`,
              amount: amountInSar ,
              bankAccountNumber: isBankAccountSelected ? user?.userBankAccount[0].bankAccountNumber : '',
              isFromWallet: isBankAccountSelected ? false : true,
              fiatAmount: Number(amount)?.toFixed(2),
              feeConfigData: getAllFeeApplied() ? getAllFeeApplied() : null ,
              totalFeeApplied:amount.length > 0 ?totalFee? Number(totalFee?.toFixed(2)) : 0 : 0,
              totalRequestedAmount : amountToDebit

            },
            isBankAccountSelected:isBankAccountSelected, 
            transactionType : "Topup",
            amountRequested :  `${instDetails?.acquiringCurrency ? instDetails?.acquiringCurrency : ""} ${Number(amount).toFixed(2)}`,
            screenType: "loadToken",
            uniqueList : getUniqueFeeList(transactionConfigData?.feeConfig) || {},
            feeTitle: 'Amount to be Debited',
            totalAmount : `${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} ${amountInSar}`,
            totalAmountText : "Tokens to be Received",
            netAmountCalulated : `${instDetails?.acquiringCurrency ? instDetails?.acquiringCurrency: ""} ${(Number(amount) + Number(totalFee))?.toFixed(2)}`
            //totalFee : Number(totalFee?.toFixed(2)),
            //tokenAsset :'SART',
            //transactionConfigData: transactionConfigData,
            //ttc : ttc,
            //isFinalAmountNegative: isFinalAmountNegative,
          }
            }
          )
      }
    }
  }

  const handleInput = (value: string) => {
    setAmountError(false);

    setIsValidationErrorTextInput(false);

    const reg = /^-?\d*(\.\d*)?$/;

    const amountLimit = value.includes('.') ? 18 : 15;

    if (value.length > amountLimit) {
      return false;
    } else if (value.length <= 0) {
      setIsValidationErrorTextInput(true);
    }

     if(value.startsWith('.')) {
      value = value.replace(/^.+/, '0.');
    } 

    if (value === '-' || value == '0') {
      setIsValidationErrorTextInput(true);
      setAmount('');
    } else if (reg.test(value) || value === '' || value === '-') {
      if (value.indexOf('.') >= 0 == false && value.length > 0) {
        value = value.replace(/^0+/, '');
      } else if (value.indexOf('.') >= 0) {
        value = value.replace(/^0+/, '0');
      }

      setAmount(
        value.indexOf('.') >= 0
          ? value.substr(0, value.indexOf('.')) +
              value.substr(value.indexOf('.'), 3)
          : value.replace(/^0+/, ''),
      );
      
      // validations based upon transactions limit

      if (ttc == TransactionTypeControl.SUBSEQUENT_LOADING) { 
      if (
        (transactionConfigData?.per_TRANSACTION?.maximumBalance != null &&
          Number(value) >
            transactionConfigData?.per_TRANSACTION?.maximumBalance) ||
        (transactionConfigData?.per_TRANSACTION?.minimumBalance != null &&
          Number(value) <
            transactionConfigData?.per_TRANSACTION?.minimumBalance)
      ) {
        setIsValidationErrorTextInput(true);
        setAmount(ConTwoDecDigit(value));
        setAmountError(true);
      } 
      else {
        setAmount(ConTwoDecDigit(value));
        setIsValidationErrorTextInput(false);
      }
    } else {
      console.log("!!here")
      if (
        (transactionConfigData?.one_TIME?.maximumBalance != null &&
          Number(value) >
            transactionConfigData?.one_TIME?.maximumBalance) ||
        (transactionConfigData?.one_TIME?.minimumBalance != null &&
          Number(value) <
            transactionConfigData?.one_TIME?.minimumBalance)
      ) {
        setIsValidationErrorTextInput(true);
        setAmount(ConTwoDecDigit(value));
        setAmountError(true);
      } 
      else {
        setAmount(ConTwoDecDigit(value));
        setIsValidationErrorTextInput(false);
      }
    }
      
    }

  };

  // ************************ UI Part ************************

  return (
    <ScreenLayoutTab
      title={t('Topup Pilgrim Wallet')}
      leftIconName="ArrowLeft"
      onLeftIconClick={() => {
        if (isPageLoadEnabled === true) {
          setPageLoadEnabled(false);
          navigation.goBack();
        }
      }}>
      <Container justify={'center'} alignItems={'center'}>
        {/* Enter amount to add label */}
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
            {t('Enter amount to add')}
          </Typography>
        </Container>

        {/* Text box container */}

        <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          justify="flex-start"
          style={[styles.box]}>
          <Container
            alignItems="flex-start"
            noItemsStretch
            justify="flex-start"
            direction="row">
            <Typography
              variant="body"
              color="darkBlackBold"
              fontFamily="Rubik-Regular"
              textAlign="left">
              {/* {t('MYR')} */}
              {instDetails?.acquiringCurrency ? instDetails?.acquiringCurrency  : ""}
            </Typography>
          </Container>
          
          <TextInput 
            value={amount}
            // onChangeText={(value)=>{setAmount(value)}}
            style={styles.textFieldStyle}
            placeholder={'0.00'}
            keyboardType="number-pad"
           // editable={isEditable}
            onChangeText={text => {
             
              
              var good = text
              // remove bad characters and multiple points
             // .replace(/[^\d.]|\.(?=.*\.)/, '')
              // remove any excess of 5 integer digits
              .replace(/^(\d{15})\d+/, '$1')
              // remove any excess of 2 decimal digits
              .replace(/(\.\d\d).+/, '$1');
              text = good
        
              handleInput(text);
            
            }}
          />


          
        </Container>

       {/*{amount.length == 0 && isTransactionConfigSuccess && !transactionConfigData.initialLoading && <Typography
              variant="chip"
              fontFamily="Rubik-Regular"
              color="grayMedium"
              textAlign="center"
              numberOfLines={2}>
              {transactionConfigData?.feeConfig && transactionConfigData?.per_TRANSACTION && `${getErrorMessage(transactionConfigData?.per_TRANSACTION)} including fee`}
            </Typography>}
      
             {amount.length == 0 && ttc == TransactionTypeControl.INITIAL_LOADING  && transactionConfigData?.initialLoading &&  (transactionConfigData?.one_TIME != null) ?
            <Typography
              variant="chip"
              fontFamily="Rubik-Regular"
              color="grayMedium"
              textAlign="center"
              numberOfLines={2}>
              {transactionConfigData?.feeConfig && transactionConfigData?.one_TIME && `${getErrorMessage(transactionConfigData?.one_TIME)} including fee`}
            </Typography> :
            undefined
            } */}

        <Container
          direction="row"
          noItemsStretch
          alignItems="center"
          justify="center"
          style={{marginLeft: 20, marginRight: 20}}>
          {amount && Number(amount) > 0 && !isAmountError && fiatExchangeRatesData ? (
            <Container
              direction="row"
              noItemsStretch
              alignItems="center"
              justify="center">
              <Typography
                variant="label"
                fontFamily="Rubik-Regular"
                color="darkBlackBold"
                textAlign="center">
                {`You get `}
              </Typography>

              {amount && !isAmountError ? (
                <Typography
                  variant="label"
                  fontFamily="Rubik-Regular"
                  color="darkBlackBold"
                  textAlign="center">
                  {/* {sartTxt} */}
                  {instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""}
                </Typography>
              ) : undefined}
              <Typography
                variant="label"
                fontFamily="Rubik-Regular"
                color="darkBlackBold"
                textAlign="center">
               {(Number(amount) * fiatExchangeRatesData?.SAR).toFixed(2)}
              </Typography>
            </Container>
          ) : (
           <Container direction='row' alignItems='center' justify='center'>
            <Typography
              variant="label"
              fontFamily="Rubik-Regular"
              color="error"
              textAlign="center"
              numberOfLines={2}>
                {/* transactionConfigData?.feeConfig && isFinalAmountNegative && amount.length > 0 ? t("Insufficient amount to process this transaction.") :
                 */}
              {isAmountError && amount.length != 0 ? getErrorMessage(ttc == TransactionTypeControl.SUBSEQUENT_LOADING ? transactionConfigData?.per_TRANSACTION : transactionConfigData?.one_TIME) : ``}
            </Typography>
           {/* {transactionConfigData?.feeConfig && isFinalAmountNegative && amount.length > 0  &&  <TouchableOpacity
           style={{paddingHorizontal:5}}
           onPress={()=>{
            setShowFeeBreakUpModal(true)
           }}>
            {<Icon name={"Info"} size='sm' color='black'/>}
            </TouchableOpacity>} */}
            </Container>
          )}
        </Container>
      </Container>

      <View
        style={{
          width: '100%',
          height: 0.5,
          backgroundColor: theme.colors.gray.light,
          marginVertical: 15,
        }}></View>

      <>
        <Typography
          style={{paddingLeft: 20}}
          variant="body"
          color="blackishGray"
          fontFamily="Rubik-Medium"
          textAlign="left">
          {t('Select Topup from')}
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
          bankHolderName={'Other Payments'}
          type={'Other Payments'}
          isDisabled={true}
          // selectedBank = {(value)=>{selectBankAccountWallet(value)}}
          isBankSelect={false}
        />
      </>

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
            //color={isChecked ? theme.colors.orange : undefined}
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
        style={{flex: 1, paddingHorizontal: 20, height: 100}}>
        <TouchableOpacity
          disabled={
            amount &&
            !isAmountError &&
            isChecked &&
            (isWalletSelected || isBankAccountSelected)
              ? false
              : true
          }
          style={[
            {
              borderRadius: 6,
              borderWidth:
                amount &&
                !isAmountError &&
                isChecked &&
                (isWalletSelected || isBankAccountSelected)
                  ? 0
                  : 1,
              height: 55,
              width: '100%',
              alignItems: 'center',
              justifyContent: 'center',
              // position: 'absolute',
              top: 0,
            },
            {
              backgroundColor:
                amount &&
                !isAmountError &&
                isChecked &&
                (isWalletSelected || isBankAccountSelected)
                  ? theme.colors.primary
                  : theme.colors.white,
            },
            {
              borderColor:
                !isAmountError &&
                isChecked &&
                (isWalletSelected || isBankAccountSelected)
                  ? theme.colors.primary
                  : theme.colors.black,
            },
          ]}
          onPress={() => {
            onConfirmClick();
          }}>
          <Container direction="row" noItemsStretch>
            <Typography
              style={[{paddingRight: 10}]}
              variant="heading"
              textAlign="center"
              fontFamily="Rubik-Medium">
              Continue
            </Typography>
            {topupSartLoading && <LoadingSpinner color="white" />}
          </Container>
        </TouchableOpacity>
      </Container>

      {url && (
        <Modal
          variant="bottom"
          isVisible={isVisible}
          onDismiss={() => setIsVisible(false)}
          dismissButtonVariant="cancel"
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
            nestedScrollEnabled
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
         modalBgColor = {"#F0F0F0"}
         contentStyle={{height: "30%"}}>
        
         <Container style={{marginTop:40 , marginHorizontal: 20}} justify={'space-between'}alignItems={'center'}>
         <Typography
                  variant="label"
                  color="darkBlackBold"
                  fontFamily="Rubik-Medium"
                  textAlign="left">
                  {t('FEE SUMMARY')}
                </Typography>
              <Container
              spacing={2}
              alignItems='center'
              justify='center'
                style={{marginTop: 30}}
                >
             
                <Container justify='space-between' direction='row'>
                      <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Medium'>Amount Requested</Typography>
                      <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Medium'>{`MYR ${amount}`}</Typography>
                      </Container>
                {transactionConfigData?.feeConfig && getUniqueFeeList(transactionConfigData?.feeConfig)?.map((x:any) => {
                         return <Container justify='space-between' direction='row'>
                         <Typography textAlign='left' color='darkBlackBold' fontFamily='Rubik-Medium'>{x?.feeName}</Typography>
                         <Typography textAlign='left' color='darkBlackBold'  fontFamily='Rubik-Medium'>{x?.currencyType} {x?.feeCalculatedAmount}</Typography>
                         </Container>
                      })
                      }
                <Container justify='space-between' direction='row'>
                      <Typography textAlign='left' color= {isFinalAmountNegative ? "error" : 'darkBlackBold'}  fontFamily='Rubik-Medium'>Net Topup Amount</Typography>
                      <Typography textAlign='left' color= {isFinalAmountNegative ? "error" : 'darkBlackBold'}  fontFamily='Rubik-Medium'>MYR {Number(amount)?.toFixed(2) - Number(calculateDeductions()?.toFixed(2))}</Typography>
                      </Container>
                </Container>
               
          </Container>
       </Modal>
      )} */}
      {showAlert.message.length > 0 && <ErrorModal error={showAlert} />}
    </ScreenLayoutTab>
  );
};

const styles = StyleSheet.create({
  buttonContainer: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  mainContainer: {
    alignItems: 'center',
    justify: 'center',
    backgroundColor: 'white',
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.md,
    marginVertical: theme.spacing.md,
  },
  ibanContainer: {
    backgroundColor: 'white',
    paddingVertical: theme.spacing.md,
  },
  label: {
    position: 'absolute',
    backgroundColor: 'white',
    left: 22,
    top: 7,
    zIndex: 999,
    paddingHorizontal: 8,
    fontSize: 14,
  },
  accountNumberTextField: {
    backgroundColor: '#000000',
  },
  textFieldStyle: {
    height: 55,
    width: 500,
    fontSize: 24,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: theme.spacing.xs,
    textAlign: 'left',
    color: '#131313',
    fontFamily: 'Rubik-Regular',
  },
  certifyTextStyle: {
    fontSize: 10,
    marginTop: 1,
  },
  disclaimerTextStyle: {
    height: 25,
    fontSize: 10,
    paddingHorizontal: 1, //theme.spacing.xs,
    width: 300,
    marginTop: 0,
    marginLeft: 15,
    fontFamily: 'Montserrat-Light',
    color: '#171717',
  },
  box: {
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 20,
    padding: 10,
    height: 60,
    borderRadius: 8,
    borderWidth: 0.8,
    borderColor: theme.colors.gray.light,
    backgroundColor: 'white',
  },
  checkboxContainer: {
    width: 10,
    height: 10,
    marginTop: 30,
  },
  shadowProp: {
    shadowColor: theme.colors.gray.dark,
    shadowOffset: {width: -2, height: 4},
    shadowOpacity: 0.2,
    shadowRadius: 1,
    elevation: 20,
  },
});

export default TopUpScreen;

import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { topupRequestPayload } from '~/api/models'
import { Button, Container, ErrorModal, LoadingSpinner, ScreenLayoutTab, Typography, theme } from '~/components/ui'
import {StackScreenProps} from '@react-navigation/stack';
import { HomeStackNavigatorParamList } from '~/components/navigators/HomeStackNavigator';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { CommonActions, StackActions, useNavigation } from '@react-navigation/native';
import { StyleSheet, TouchableOpacity, View } from 'react-native';
import { ConsoleLogger } from '@aws-amplify/core';
import { height, isIOS, isTransactionAllowed, sartTxt, validateInitialLoading, width } from '~/utils';
import { ScreenHeight } from 'react-native-elements/dist/helpers';
import { he } from 'date-fns/locale';
import { TransactionSummaryComponent } from '~/components/ui/TransactionSummaryComponent';
import { useAddTransaction, useRefundSart, useSendDigitalCurrencyToExternalWallet, useTopupSart } from '~/api/user';
import { TransactionTypeControl } from '~/api/constants';

type Props = NativeStackScreenProps<
  HomeStackNavigatorParamList,
  "TransactionConfirmationScreenTopUp"
>

  const TransactionConfirmationScreenTopUp : React.FC<Props> = (
    { 
        navigation, 
        route
    }: Props) => {
        const {transactionConfirmationDataParam} = route?.params
        const {
          screenType,
          transactionType,
          amountRequested,
          requestPayload,
          totalAmount,
          totalFee,
          tokenAsset,
          transactionConfigData,
          ttc,
          feeTitle,
          netAmountCalulated,
          isFinalAmountNegative,
          uniqueList,
          totalAmountText
        } = transactionConfirmationDataParam || {};
        // console.log("transactionConfigData ", transactionConfigData  , )
        // console.log( "transaction Type" , ttc , totalAmount)
        console.log( "requestPayload" , requestPayload)

        const {t} = useTranslation();
        //const navigation =  useNavigation()
        const [showAlert, setshowAlert] = useState({name: '', message: ''});

        const styles = StyleSheet.create({
          container: {
            flex: 1,
        },
        titleWrapper: {
    
        },
        inputWrapper: {
    
        },
        contentContainer: {
            flex: 1 // pushes the footer to the end of the screen
        },
        footer: {
            height: 100
        }
        })
   

        const {
          mutate: topupSart,
          isLoading: topupSartLoading,
          error: topupSartError,
          isSuccess: topupSartSuccess,
          data: topupSartData,
        } = useTopupSart();
        const {
          mutate: refundToken,
          isLoading: refundSartLoading,
          error: refundSartError,
          isSuccess: refundSartSuccess,
          data: refundSartData,
        } = useRefundSart();
        const {
          mutate: addTransaction,
          isLoading: isAddTransactionDataLoading,
          error: isAddTransactionDataError,
          isSuccess,
          data: addTransactionData,
        } = useAddTransaction();
        const {
          mutate: sendDigitalCurrencyExternalWallet,
          isLoading: isSendExternalWalletLoading,
          error: sendExternalWalletError,
          isSuccess: isSendExternalWalletSuccess,
          data: sendExternalWalletData
        } = useSendDigitalCurrencyToExternalWallet()

        useEffect(() => {
          if (topupSartError) {
            let message = topupSartError.message;
            setshowAlert({name: 'TopUp', message: message});
          }
        }, [topupSartError]);
        useEffect(() => {
          if (refundSartError) {
            let message = refundSartError.message;
            setshowAlert({name: 'Refund', message: message});
          }
        }, [refundSartError]);
        useEffect(() => {
          if (isAddTransactionDataError) {
            let message = isAddTransactionDataError.message;
            setshowAlert({name: 'Internal Transfer', message: message});
          }
        }, [isAddTransactionDataError]);
        useEffect(() => {
          if (sendExternalWalletError) {
            let message = sendExternalWalletError.message;
            setshowAlert({name: 'Payment', message: message});
          }
        }, [sendExternalWalletError]);

        const navigateToTransactionDetailScreen = (uuid : string) => {
          // navigation.dispatch(
          //   CommonActions.reset({
          //     index: 0,
          //     routes: [
          //       {
          //         name: "TransactionDetail",
          //         params: {
          //           transactionId: uuid
          //         }
          //       }
          //     ]
          //   })
          // )
          // navigation.reset({
          //   index: 0,
          //   routes: [{name: 'TransactionDetail' , params: {transactionId:uuid} }],
          // });

       //navigation.dispatch(resetAction)
          navigation.navigate("TransactionDetail", {
            transactionId:uuid
          })

        }
        useEffect(() => {
          if (topupSartData) {
           // navigate to payment success screen
           console.log("topupSartData response" , topupSartData)
           navigateToTransactionDetailScreen(topupSartData?.uuid)
          
           // navigation.goBack();
          }
        }, [topupSartData]);
        useEffect(() => {
          if (refundSartData) {
           // navigate to payment success screen
           console.log("refundSartData" , refundSartData)
            navigateToTransactionDetailScreen(refundSartData?.uuid)
          }
        }, [refundSartData]);
        useEffect(() => {
          if (addTransactionData) {
           // navigate to payment success screen
           console.log("addTransactionData" , addTransactionData)
            navigateToTransactionDetailScreen(addTransactionData?.uuid)
          }
        }, [addTransactionData]);
        useEffect(() => {
          if (sendExternalWalletData) {
           // navigate to payment success screen
           console.log("sendExternalWalletData" , sendExternalWalletData)
            navigateToTransactionDetailScreen(sendExternalWalletData?.uuid)
          }
        }, [sendExternalWalletData]);

       const handleTopupRequest = () => {
         topupSart(requestPayload);
       };
       const handleRefundUnspentRequest = () => {
          refundToken(requestPayload);
      };
      const handleTransferRequest = () => {
          addTransaction(requestPayload);
      };
      const handleSendDigitalCurrencyExternalWallet = () => {
        sendDigitalCurrencyExternalWallet(requestPayload);
      };
    function routeToRequest(screenType: string | undefined) {
      console.log("screenType ", screenType)
      switch (screenType) {
        case 'loadToken':
            handleTopupRequest()
          break;
        case 'refundUnspent':
           handleRefundUnspentRequest()
          break;
        case 'addTransaction':
          handleTransferRequest()
          break;
        case 'merchantOnline':
        case 'merchantOffline':
        case 'peer_2_peer':
          handleSendDigitalCurrencyExternalWallet() 
          break;
        default:
          break;
      }
    }

        return (
          <>
            <ScreenLayoutTab
              title={t('Transaction Confirmation')}
              leftIconName="ArrowLeft"
              onLeftIconClick={() => {
                navigation.goBack();
              }}>
              <TransactionSummaryComponent
                transactionType={transactionType}
                amountRequested={amountRequested}
                feeConfigList={screenType == 'loadToken' ? uniqueList : requestPayload?.feeConfigData}
                netFeeKeyName={feeTitle || ""}
                totalAmount={totalAmount} 
                totalAmountText={totalAmountText || "Amount Received"}
                netAmountCalulated={netAmountCalulated}/>
              
               {showAlert.message.length > 0 && <ErrorModal error={showAlert} />}
            </ScreenLayoutTab>
            <Container
              justify="flex-end"
              alignItems="center"
              noItemsStretch
              style={{
                flex: 1,
                position: 'absolute',
                bottom: 0,
                alignSelf: 'center',
                paddingHorizontal: 20,
              }}>
              <TouchableOpacity
                disabled={false}
                style={[
                  {
                    borderRadius: 6,
                    borderWidth: 1,
                    height: 55,
                    width: width - (width * 15) / 100,
                    alignItems: 'center',
                    justifyContent: 'center',
                    paddingHorizontal: 20,
                    alignSelf: 'center',
                    marginBottom:20,
                    // position: 'absolute',
                    backgroundColor: theme.colors.primary,
                    borderColor: theme.colors.primary,
                    // top: 0,
                  },
                ]}
                onPress={() => {
                           //TODO : call for loadtoken, refundToken, P2P , merchant online , merchant offline 
                           //console.log("nandani")
                           routeToRequest(screenType)

                }}>
                <Container
                  direction="row"
                  alignItems="center"
                  justify="center"
                  noItemsStretch>
                  <Typography
                    style={[{paddingRight: 10}]}
                    variant="heading"
                    textAlign="center"
                    fontFamily="Rubik-Medium">
                    Confirm
                  </Typography>
                  {topupSartLoading && <LoadingSpinner color="black" />}
                  {refundSartLoading  && <LoadingSpinner color="black" />}
                  {isAddTransactionDataLoading  && <LoadingSpinner color="black" />}
                  {isSendExternalWalletLoading  && <LoadingSpinner color="black" />}
                </Container>
              </TouchableOpacity>
            </Container>
          </>
        );
}

export default TransactionConfirmationScreenTopUp;
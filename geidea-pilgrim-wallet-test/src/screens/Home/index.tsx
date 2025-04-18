/* eslint-disable @typescript-eslint/ban-ts-comment */
import React, {useContext, useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {
  StyleSheet,
  LogBox,
  TouchableOpacity,
  View,
  Image,
  Alert,
} from 'react-native';
import {
  useAddresses,
  useDeleteUser,
  useGetUserProfileDetails,
  useSignOut,
} from '~/api/user';
import RecentTransactions from './RecentTransactions';

import {RootNavigationProps} from '~/components/navigators';
import {
  Container,
  ErrorModal,
  Icon,
  LoadingSpinner,
  ScreenLayoutTab,
  theme,
  Typography,
} from '~/components/ui';
import {
  useGetFiatBalance,
  useGetPushNotificationData,
  useUserBalances,
  useUserTransactions,
} from '~/api/user';
import {Asset, TokenToAmount} from '~/constants/types';
import {UserContext} from '~/context';
import {useGetExchangeRate} from '~/api/onRamp';
import useFormatCurrencyAmount from '~/helpers/formatCurrencyAmount';
import {getCryptoBalance, isIOS, sartTxt, width} from '~/utils';
import LinearGradient from 'react-native-linear-gradient';
import CommonWarningModal from '~/components/ui/CommonWarningModal';
import {useIsFocused} from '@react-navigation/native';
import SaveUserStaticQrModal from '~/components/ui/SaveUserStaticQrModal';

const styles = StyleSheet.create({
  balanceContainer: {
    marginHorizontal: theme.spacing.md,
  },
  scrollContainer: {
    paddingBottom: theme.spacing.lg,
  },
  balanceLoadingContainer: {
    height: theme.fontHeight.subtitle,
  },
  transactionsContainer: {
    marginTop: 15,
    backgroundColor: theme.colors.white,
    // borderTopLeftRadius: theme.borderRadius.xxl,
    // borderTopRightRadius: theme.borderRadius.xxl,
    paddingVertical: theme.spacing.xs,
    // ...theme.shadow.card
  },
  fundContainer: {
    flexDirection: 'row',
    borderColor: '#E8E8E8',
    backgroundColor: 'white',
    justifyContent: 'space-between',
    paddingHorizontal: theme.spacing.xs,
    borderRadius: theme.borderRadius.xxs,
    borderWidth: theme.borderWidth.xs,
    width: 155,
    height: 100,
  },
  container: {
    flex: 1,
    marginVertical: theme.spacing.sm,
    justifyContent: 'center',
    paddingBottom: theme.spacing.md,
  },
  greetcontainer: {
    flex: 2,
    marginLeft: 0,
    marginRight: 0,
    marginBottom: theme.spacing.lg,
    justifyContent: 'center',
    backgroundColor: 'red',
    top: 10,
  },
  image: {
    margin: 8,
    width: 25,
    height: 25,
  },
  imageView: {
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: 10,
  },
  card: {
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: theme.borderRadius.xl,
    marginLeft: 20,
    marginRight: 20,
    ...(!isIOS ? theme.shadow.card : {}),
  },
  cryptoBalanceLoadingContainer: {
    height: theme.fontHeight.subtitle,
  },
});

const Home: React.FC<RootNavigationProps> = ({
  navigation,
}: RootNavigationProps) => {
  const {t} = useTranslation();
  const {
    fiatAsset,
    user,
    instDetails,
    updateSarTokens,
    setActivationFeeCharged,
    saveStaticQrCodeToGallery,
  } = useContext(UserContext);
  const [isLoading, setIsLoading] = useState(false);
  const formatter = useFormatCurrencyAmount();
  const {mutate: deleteUser, error, isSuccess} = useDeleteUser();
  const {mutate: signOut} = useSignOut();
  const isFocused = useIsFocused();
  console.log('availble balalnces', user, instDetails);
  useEffect(() => {
    if (isSuccess) {
      signOut();
    }
  }, [isSuccess]);

  const {
    data: balanceData,
    isFetching: isFetchingBalance,
    refetch: refetchBalance,
    error: errorBalance,
  } = useUserBalances(); // use to show available balance
  const {
    data: transactionData,
    isFetching: isFetchingTransactions,
    refetch: refetchTransactions,
    error: errorTransactions,
  } = useUserTransactions('asset=SART'); // used for showing up recent transaction UI
  const {
    data: exchangeRatesData,
    isFetching: isFetchingExhchangeRates,
    refetch: refetchExchangeRates,
  } = useGetExchangeRate(fiatAsset); // help us to convert the currencies
  // const {
  //   data: notificationData,
  //   isSuccess: isSuccessNotificationData,
  //   isFetching: isFetchingNotificationData,
  //   refetch: refetchNotificationData,
  //   error: errorNotificationData
  // } = useGetPushNotificationData(user?.attributes?.email || "")

  const {
    data: fiatBalancesData,
    isFetching: isFetchingfiatBalancesData,
    isSuccess: isSuccessfiatBalancesData,
    refetch: refetchfiatBalancesData,
  } = useGetFiatBalance(); // use to show fiat balance
  const {
    data: profileData,
    isFetching: isFetchingProfileDetails,
    refetch: refetchGetUserProfileDetails,
    error: errorProfileDetails,
  } = useGetUserProfileDetails(); // user profile details

  const [walletAddresses, setWalletAddresses] = useState('');

  const {
    data: addresses,
    isFetching: addressesIsFetching,
    isSuccess: addressesIsSuccess,
    error: addressesError,
  } = useAddresses(); // blockchain address of a logged in user

  useEffect(() => {
    let response = addresses;
    if (response) {
      let wa = addresses?.find(obj => obj?.asset === 'SART')?.address;
      if (wa) {
        //// console.log("nandani", response)
        setWalletAddresses(wa);
        // console.log("walletAddresses : ", walletAddresses)
      }
    }
  }, [addressesIsSuccess]);

  useEffect(() => {
    // Check if wallet is activated  for the user or not
    console.log('profileData', profileData);
    if (profileData?.isActivationFeeCharged) {
      setActivationFeeCharged(profileData?.isActivationFeeCharged);
    }

    //  if(profileData?.isBalanceRunningLow) {

    //  }
  }, [profileData]);
  useEffect(() => {
    refetchGetUserProfileDetails();
  }, [isFocused]);

  // useEffect(() => {
  //   if (notificationData) {
  //     let notiDataStatusNew: NotificationListData = []
  //     notificationData?.find((n) => {
  //       if (n.isRead === false && n.receiverName === "External Wallet") {
  //         notiDataStatusNew.push(n)
  //       }
  //       if (
  //         n.status &&
  //         n.status === "NEW" &&
  //         n.receiverName !== "External Wallet"
  //       ) {
  //         notiDataStatusNew.push(n)
  //       }
  //     })
  //     // setNotiCount(notiDataStatusNew.length)
  //   }
  // }, [isSuccessNotificationData, isFetchingNotificationData])

  useEffect(() => {
    LogBox.ignoreLogs(['VirtualizedLists should never be nested']);
  }, []);

  const refetch = () => {
    refetchGetUserProfileDetails();
    refetchBalance();
    refetchTransactions();
    refetchExchangeRates();
    // refetchNotificationData()
    refetchfiatBalancesData();
  };

  useEffect(() => {
    updateSarTokens(getCryptoBalance('SART', balanceData)); // temp fix
  }, [balanceData]);

  return (
    <ScreenLayoutTab
      useScrollView={true}
      useLogo
      rightComponent={<Icon name={'Exit'} size="md" color="iconRegulorColor" />}
      onRightIconClick={signOut}
      // ****************** Bell Icon ******************
      notificationComponent={
        <View style={{left: -10}}>
          <Icon name={'Bell'} size="lg" color="focused" />
          {/* {notifCount > 0 ? (
            <Badge
              status="error"
              containerStyle={{ position: "absolute", top: -8, right: -3 }}
              value={notifCount}
            />
          ) : undefined} */}
        </View>
      }
      onNotificationIconClick={() =>
        // @ts-ignore
        navigation.navigate('Notification')
      }
      refreshing={isFetchingBalance}
      onRefresh={refetch}>
      <ErrorModal error={errorBalance ? errorBalance : errorTransactions} />

      {/* <ScrollView
        contentContainerStyle={styles.container}
        showsVerticalScrollIndicator={false}
      > */}
      <Container
        style={{backgroundColor: 'white', paddingVertical: 0}}
        direction="column">
        <Typography
          textAlign="center"
          fontFamily="Rubik-Medium"
          fontWeight="bold"
          variant="body"
          color="darkBlackBold"
          style={{marginVertical: 25}}>
          {t('Welcome to Pilgrim Wallet')}
        </Typography>

        <Container alignItems="center" justify="center" style={{height: 130}}>
          <LinearGradient
            start={{x: 0, y: 0}}
            end={{x: 0, y: 1}}
            colors={['#FFCB23', '#FFA63C']}
            style={styles.card}>
            {exchangeRatesData && balanceData && fiatBalancesData ? (
              <Container
                direction="row"
                justify="center"
                alignItems="center"
                noItemsStretch>
                <Container
                  spacing={1}
                  alignItems="center"
                  justify="center"
                  noItemsStretch>
                  <Container
                    justify="center"
                    alignItems="center"
                    style={{marginHorizontal: 20, marginTop: 20}}
                    spacing={1}>
                    <Typography
                      fontFamily="Rubik-Regular"
                      variant="heading"
                      color={'darkBlackBold'}>
                      {t('Available Balance')}
                    </Typography>
                  </Container>

                  <Container justify="center" spacing={1}>
                    {!isLoading ? (
                      <Typography
                        fontFamily="Rubik-Medium"
                        fontWeight="bold"
                        color={'darkBlackBold'}
                        textAlign="center"
                        variant="subtitle">
                        {instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} 
                        {/* {sartTxt} */}
                        {formatter(getCryptoBalance('SART', balanceData), {
                          asset: 'SART',
                        })}
                      </Typography>
                    ) : (
                      <View style={styles.cryptoBalanceLoadingContainer}>
                        <LoadingSpinner color="white" />
                      </View>
                    )}
                  </Container>

                  {profileData && profileData?.isBalanceRunningLow && (
                    <Container
                      alignItems="center"
                      direction="row"
                      spacing={1}
                      justify="center"
                      noItemsStretch
                      style={{
                        backgroundColor: 'rgba(0,0,0,0.4)',
                        // opacity:0.3,
                        paddingHorizontal: 8,
                        paddingVertical: 6,
                        borderRadius: 6,
                        marginTop: 2,
                        marginBottom: 19,
                      }}>
                      <Icon size="xs" name={'Alert'}></Icon>
                      <Typography
                        color="white"
                        style={{fontSize: 13}}
                        fontFamily="Rubik-Regular">
                        Low Balance. Minimum {instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""} {' '}
                        {/* {sartTxt} */}
                        {profileData?.lowMaintainWalletBalance}
                      </Typography>
                    </Container>
                  )}
                </Container>
              </Container>
            ) : (
              <LoadingSpinner color="white" />
            )}
          </LinearGradient>
        </Container>
        <Typography
          textAlign="center"
          fontFamily="Rubik-Regular"
          variant="chip"
          color="darkBlackBold"
          style={{marginVertical: 10}}>
          {t(`* Represents wallet value as ${instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ " " : ""} equivalent`)}
        </Typography>
      </Container>

      <Container style={{backgroundColor: '#F6F6F6'}}>
        {/* ****************** add Funds and Refund ****************** */}

        <Container direction="row" spacing={1} style={styles.container}>
          <Container
            direction="column"
            justify="space-evenly"
            spacing={1}
            style={styles.fundContainer}>
            <TouchableOpacity
              style={styles.imageView}
              onPress={() => navigation.navigate('TopUpScreen')}>
              <Image
                source={require('~images/topup.png')}
                style={styles.image}
              />

              <Typography
                variant="label"
                fontFamily="Rubik-Regular"
                color="black">
                {t('Topup Wallet')}
              </Typography>
            </TouchableOpacity>
          </Container>

          <Container
            direction="column"
            justify="space-evenly"
            spacing={1}
            style={styles.fundContainer}>
            <TouchableOpacity
              style={styles.imageView}
              onPress={() => navigation.navigate('RefundScreen')}>
              <Image
                source={require('~images/refund.png')}
                style={styles.image}
              />

              <Typography
                variant="label"
                fontFamily="Rubik-Regular"
                color="black">
                {t('Redeem Unspent')}
              </Typography>
            </TouchableOpacity>
          </Container>
        </Container>

        {/* ****************** Recent Transactions ****************** */}

        <View style={styles.transactionsContainer}>
          <RecentTransactions
            data={transactionData}
            isFetching={isFetchingTransactions}
          />
        </View>
      </Container>
      {/* </ScrollView> */}

      {!saveStaticQrCodeToGallery && addressesIsSuccess && walletAddresses && (
        <SaveUserStaticQrModal walletAddresses={walletAddresses} message="hi" />
      )}
      {/* {profileData && profileData?.isBalanceRunningLow && <CommonWarningModal message={t('Please Top up your wallet to avoid Wallet Low Balance Fee!')}/>} */}
    </ScreenLayoutTab>
  );
};

export default Home;

import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {StyleSheet, Dimensions, BackHandler} from 'react-native';
import {CommonActions, RouteProp} from '@react-navigation/native';

import {
  SendFundsStackNavigatorParamList,
  TransactionStackNavigatorParamList,
} from '~/components/navigators';
import {ScreenLayoutTab, theme, Typography} from '~/components/ui';
import {useUserTransaction} from '~/api/user';
import useFormatCurrencyAmount from '~/helpers/formatCurrencyAmount';
import PaymentDetailComponent from '~/components/ui/PaymentDetailComponent';

const {width} = Dimensions.get('window');
const containerMargin = theme.spacing.md;
const rowWidth =
  width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs;

type Props = {
  navigation: NativeStackScreenProps<
    TransactionStackNavigatorParamList,
    'Transactions'
  >;
  route: RouteProp<SendFundsStackNavigatorParamList, 'PaymentSuccess'>;
};

const PaymentSuccess: React.FC<Props> = ({navigation, route}: Props) => {
  const {t} = useTranslation();
  const {transactionId} = route.params;
  const {
    data: transaction,
    isFetching,
    refetch,
  } = useUserTransaction(transactionId);

  const onDone = (): any => {
    navigation.dispatch(
      CommonActions.reset({
        index: 0,
        routes: [
          {
            name: 'Home',
          },
        ],
      }),
    );
  };

  useEffect(() => {
    function handleBackButton() {
      navigation.dispatch(
        CommonActions.reset({
          index: 0,
          routes: [
            {
              name: 'Home',
            },
          ],
        }),
      );
      return true;
    }

    const backHandler = BackHandler.addEventListener(
      'hardwareBackPress',
      handleBackButton,
    );

    return () => backHandler.remove();
  }, [navigation]);

  return (
    <ScreenLayoutTab
      title={'Payment'}
      leftIconName="ArrowLeft"
      onLeftIconClick={onDone}
      refreshing={isFetching}
      onRefresh={refetch}>
      {isFetching ? null : transaction ? (
        <PaymentDetailComponent transaction={transaction} />
      ) : (
        <Typography>{t('Transaction not found.')}</Typography>
      )}
    </ScreenLayoutTab>
  );
};

export default PaymentSuccess;

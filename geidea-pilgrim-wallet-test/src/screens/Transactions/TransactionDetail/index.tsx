import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {BackHandler, Dimensions, StyleSheet} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';

import {ScreenLayoutTab, Typography} from '~/components/ui';
import {TransactionStackNavigatorParamList} from '~/components/navigators';
import {useUserTransaction} from '~/api/user';
import PaymentDetailComponent from '~/components/ui/PaymentDetailComponent';
import { CommonActions } from '@react-navigation/native';

type Props = NativeStackScreenProps<
  TransactionStackNavigatorParamList,
  'TransactionDetail'
>;

const TransactionDetail: React.FC<Props> = ({route, navigation}: Props) => {
  const {t} = useTranslation();
  const [isPageLoadEnabled, setPageLoadEnabled] = useState(true);
  const {transactionId} = route.params;
  const {
    data: transaction,
    isFetching,
    refetch,
  } = useUserTransaction(transactionId);

  const routeName = navigation.getState().routes.find(obj => obj?.name == 'Transactions')?.name || ""
  console.log("routeName ", routeName)

const onDone = (): any => {
  if(routeName === 'Transactions') {
    navigation.goBack()
  } else {
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
  }
};

useEffect(() => {
  function handleBackButton() {
    if(routeName === 'Transactions') {
      navigation.goBack()
    } else {
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
    }
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
      title=""
      showHeaderBg={false}
      leftIconName="ArrowLeft"
      onLeftIconClick={() => {
        if (isPageLoadEnabled === true) {
          setPageLoadEnabled(false);
          onDone()
        }
      }}
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

export default TransactionDetail;

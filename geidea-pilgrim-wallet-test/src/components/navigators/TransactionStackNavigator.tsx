import React from 'react';

import {Transactions, TransactionDetail} from '~/screens';
import {createNativeStackNavigator} from '@react-navigation/native-stack';

export type TransactionStackNavigatorParamList = {
  Transactions: {};
  TransactionDetail: {
    transactionId: string;
  };
};

const Stack = createNativeStackNavigator();

const TransactionStackNavigator: React.FC = () => {
  return (
    <Stack.Navigator
      initialRouteName="Menu"
      screenOptions={{headerShown: false}}>
      <Stack.Screen name="Transactions" component={Transactions} />
      <Stack.Screen name="TransactionDetail" component={TransactionDetail} />
    </Stack.Navigator>
  );
};

export default TransactionStackNavigator;

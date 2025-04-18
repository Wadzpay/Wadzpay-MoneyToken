import React from "react"

import {
  EnterAmount,
  PaymentSuccess,
  PaymentSummary,
  SelectContact,
  SelectCurrency,
  SendFunds,
  ScanWalletAddressQrCode
} from "~/screens"
import { Asset } from "~/constants/types"
import { Contact, transactionConfirmationData } from "~/api/models"
import ScanToPayScreen from "~/screens/SendFunds/ScanToPayScreen"
import SendViaQRCode from "~/screens/SendViaQRCode/SendViaQRCode"
import { createNativeStackNavigator } from "@react-navigation/native-stack"
import TransactionConfirmationScreenTopUp from "~/screens/TransactionConfirmationScreenTopUp"

export type SendFundsStackNavigatorParamList = {
  SendFundSelector: {
    asset?: Asset
    cognitoUsername?: string
    amount?: number
  }
  SendFunds: {
    asset?: Asset
    cognitoUsername?: string
    amount?: number
  }
  SelectContact: {
    cognitoUsername: string
    onCognitoUsernameChange: (cognitoUsername: string) => void
    title: string
  }
  SelectCurrency: {
    selectedAsset: Asset
    onSelectedAssetChange: (asset: Asset) => void
  }
  EnterAmount: {
    asset: Asset
    amount: number
    onAmountChange: (amount: number) => void
  }
  PaymentSummary: {
    contact?: Contact
    asset: Asset
    amount: any
    walletMode: string
    walletAddress?: string
    fromScreen?: string
    id?: string
  }
  PaymentSuccess: {
    transactionId: string
  }
  ScanWalletAddressQrCode: {
    onScanQRCode?: (
      walletAddress: string,
      amount?: number,
      qrAsset?: Asset
    ) => void
    asset: Asset
  },
  TransactionConfirmationScreenTopUp: {
    transactionConfirmationDataParam?: transactionConfirmationData
  },
}

const Stack = createNativeStackNavigator()

const SendFundsStackNavigator: React.FC = () => {
  return (
    <Stack.Navigator
      initialRouteName="ScanToPayScreen"
      screenOptions={{ headerShown: false }}
    >
      {/* <Stack.Screen name="SendFundSelector" component={SendFundSelector} /> */}
      {/* <Stack.Screen name="SendFunds" component={SendFunds} /> */}
      {/* <Stack.Screen name="SelectContact" component={SelectContact} /> */}
      {/* <Stack.Screen name="SelectCurrency" component={SelectCurrency} /> */}
      <Stack.Screen
        name="ScanWalletAddressQrCode"
        component={ScanWalletAddressQrCode}
        options={({route}) => ({ asset: "ETH"})
        }
      />
      <Stack.Screen
    
        name="ScanToPayScreen"
        component={ScanToPayScreen}
        options={({route}) => ({ asset: "ETH"})
        }
      />
      
      <Stack.Screen name="EnterAmount" component={EnterAmount} />
      <Stack.Screen name="PaymentSummary" component={PaymentSummary} />
      <Stack.Screen name="PaymentSuccess" component={PaymentSuccess} />
      <Stack.Screen name="SendViaQRCode" component={SendViaQRCode} />
    </Stack.Navigator>
  )
}

export default SendFundsStackNavigator

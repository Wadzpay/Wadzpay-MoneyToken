import React from "react"
import { createNativeStackNavigator } from "@react-navigation/native-stack"

import {
  EnterAmount,
  SelectContact,
  SelectCurrency,
  ReceiveFunds,
  PaymentSuccess
} from "~/screens"

import SendViaQRCode from "~/screens/SendViaQRCode/SendViaQRCode"
import { Asset } from "~/constants/types"
import RecieveFundsConfirmation from "~/screens/ReceiveFunds/RecieveFundsConfirmation"
import { Contact, transactionConfirmationData } from "~/api/models"
import QrCodeCardCarousel from "~/screens/ReceiveFunds/QrCodeCardCarousel"
import TransactionConfirmationScreenTopUp from "~/screens/TransactionConfirmationScreenTopUp"

export type RecieveFundsStackNavigatorParamList = {
  ReceiveFunds: {
    asset?: Asset
    cognitoUsername?: string
    amount?: number
  }
  SendViaQRCode: {
    asset?: Asset
    cognitoUsername?: string
    amount?: number
    walletAddress?: string
    onWalletAddressChange?: (value: string) => void
  }
  SelectContact: {
    cognitoUsername: string
    onCognitoUsernameChange: (cognitoUsername: string) => void
  }
  SelectCurrency: {
    selectedAsset: Asset
    onSelectedAssetChange: (asset: Asset) => void
    isRecievedScreen?: boolean
  }
  EnterAmount: {
    asset: Asset
    amount: number
    onAmountChange: (amount: number) => void
    isRecievedScreen?: boolean
  }
  RecieveFundsConfirmation: {
    asset?: Asset
    contact?: Contact
    amount?: number
    walletAddress?: string
  }
  InternalTransfer: {
    asset?: Asset
    contact?: Contact
    amount?: number
    walletAddress?: string,
    cognitoUsername?: string
  },
  PaymentSummary: {
    contact?: Contact
    asset: Asset
    amount: any
    walletMode?: string
    walletAddress?: string
    fromScreen?: string
    id?: string
  },
  PaymentSuccess: {
    transactionId: string
  },
  TransactionConfirmationScreenTopUp: {
    transactionConfirmationDataParam?: transactionConfirmationData
  },
}

const Stack = createNativeStackNavigator()

const RecieveFundsStackNavigator: React.FC = () => {
  return (
    <Stack.Navigator
      initialRouteName="InternalTransfer"
      screenOptions={{ headerShown: false }}
    >
      <Stack.Screen name="SelectContact" component={SelectContact} />
       <Stack.Screen name="PaymentSuccess" component={PaymentSuccess} />
    </Stack.Navigator>
  )
}

export default RecieveFundsStackNavigator

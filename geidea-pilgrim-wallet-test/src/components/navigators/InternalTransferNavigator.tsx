import React from "react"
import { createNativeStackNavigator } from "@react-navigation/native-stack"

import {
      SelectContact,
      InternalTransfer
 } from "~/screens"


import { Asset } from "~/constants/types"
import { Contact } from "~/api/models"

export type RecieveFundsStackNavigatorParamList = {
 

 
 
  InternalTransfer: {
    asset?: Asset
    contact?: Contact
    amount?: number
    walletAddress?: string,
    cognitoUsername?: string
  }
}

const Stack = createNativeStackNavigator()

const InternalTransferNavigator: React.FC = () => {
  return (
    <Stack.Navigator
      initialRouteName="InternalTransfer"
      screenOptions={{ headerShown: false }}
    >
      <Stack.Screen name="InternalTransfer" component={InternalTransfer} />
      <Stack.Screen name="SelectContact" component={SelectContact} />
     
    </Stack.Navigator>
  )
}

export default InternalTransferNavigator

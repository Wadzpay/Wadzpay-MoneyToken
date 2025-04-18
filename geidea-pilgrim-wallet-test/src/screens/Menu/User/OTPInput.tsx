import { NativeStackScreenProps } from '@react-navigation/native-stack'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { Text, View } from 'react-native'
import { MenuStackParamList } from '~/components/navigators'
import OtpComponent from './OtpComponent'

type Props = NativeStackScreenProps<MenuStackParamList, "OTPInput">

const OTPInput: React.FC<Props> = ({ navigation, route }: Props) => {
  //const { target } = route.params
  const { t } = useTranslation()

  return (
    <OtpComponent/>
  )
}

export default OTPInput


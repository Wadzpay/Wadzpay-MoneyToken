import React, { useContext, useState } from "react"
import { useTranslation } from "react-i18next"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import { StyleSheet, View, Dimensions, TouchableOpacity } from "react-native"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup/dist/yup"

import { SendFundsStackNavigatorParamList } from "~/components/navigators"
import {
  Button,
  Container,
  ErrorModal,
  Icon,
  ScreenLayoutTab,
  SelectField,
  theme,
  Typography
} from "~/components/ui"
import { SendFundType } from "~/constants/types"
import { useUserBalances, useUserContacts } from "~/api/user"
import { useGetExchangeRate } from "~/api/onRamp"
import { UserContext } from "~/context"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"
import { useTranslationItems } from "~/constants/translationItems"
import {
  AddFakeTransactionForm,
  SelectSendWalletForm
} from "~/constants/formTypes"
import { useValidationSchemas } from "~/constants"

const { width } = Dimensions.get("window")
const containerMargin = theme.spacing.xl

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: containerMargin,
    marginBottom: theme.spacing.lg
  },
  selectItem: {
    minWidth:
      width - containerMargin * 2 - theme.iconSize.md - theme.spacing.xs,
    minHeight: 70,
    borderBottomWidth: theme.borderWidth.xs,
    borderColor: theme.colors.gray.light,
    justifyContent: "center"
  },
  contact: {
    paddingHorizontal: theme.spacing.xs
  },
  amount: {
    flexDirection: "row",
    alignItems: "center",
    padding: theme.spacing.md
  },
  amountText: {
    flex: 1
  },
  buttonContainer: {
    marginHorizontal: theme.spacing.md
  }
})

type Props = NativeStackScreenProps<SendFundsStackNavigatorParamList, "SendFunds">
export const useSendModeSelectItems: (useShortNames?: boolean) => {
  label: string
  value: SendFundType
}[] = (useShortNames = false) => {
  const { sendFundType } = useTranslationItems()
  return [
    {
      label: useShortNames ? "WadzPay Wallet" : sendFundType.WadzpayWallet,
      value: "WadzPay Wallet"
    },
    {
      label: useShortNames ? "External Wallet" : sendFundType.ExternalWallet,
      value: "External Wallet"
    }
  ]
}
const SendFundSelector: React.FC<Props> = ({ navigation }: Props) => {
  const { t } = useTranslation()
  const sendFunds = useSendModeSelectItems()
  const { depositSchema } = useValidationSchemas()

  const goNext = () => {
    if (walletMode == "") {
      alert("Please select a wallet.")
    } else if (walletMode == "WadzPay Wallet") {
      navigation.navigate("SendFunds")
    } else if (walletMode == "External Wallet") {
      navigation.navigate("SendFunds") //TODO
    } else {
      alert("Please select a wallet.")
    }
  }

  const {
    control,
    setValue,
    watch,
    handleSubmit,
    formState: { errors }
  } = useForm<SelectSendWalletForm>({
    resolver: yupResolver(depositSchema),
    defaultValues: { sendFundType: "WadzPayWallet" }
  })

  const [walletMode, setWalletMode] = useState("")

  return (
    <ScreenLayoutTab title={t("New Payment")} useScrollView={false}>
      <Container justify="space-between" style={styles.container}>
        <SelectField
          label={t("Send using")}
          name="asset"
          control={control}
          items={sendFunds}
          placeholder={t("Select Wallet")}
          onChange={setWalletMode}
        />

        <Button text={t("Continue")} onPress={goNext} />
      </Container>
    </ScreenLayoutTab>
  )
}

export default SendFundSelector

import React, { useEffect } from "react"
import { useTranslation } from "react-i18next"
import { useForm } from "react-hook-form"
import { Alert, StyleSheet, View } from "react-native"
import { yupResolver } from "@hookform/resolvers/yup"

import { useAddFakeTransaction } from "~/api/user"
import {
  Button,
  Container,
  ErrorModal,
  NumericKeyboard,
  SelectField,
  theme,
  Typography
} from "~/components/ui"
import { useValidationSchemas } from "~/constants"
import { AddFakeTransactionForm } from "~/constants/formTypes"
import { useAssetSelectItems } from "~/constants/selectItems"

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: theme.spacing.xl,
    marginBottom: theme.spacing.xl
  },
  containerButton: {
    marginHorizontal: theme.spacing.md
  }
})

const FakeOnRamp: React.FC = () => {
  const { t } = useTranslation()
  const currencies = useAssetSelectItems()
  const {
    mutate: addFakeTransaction,
    isLoading,
    isSuccess,
    error
  } = useAddFakeTransaction()
  const { depositSchema } = useValidationSchemas()
  const {
    control,
    setValue,
    watch,
    handleSubmit,
    formState: { errors }
  } = useForm<AddFakeTransactionForm>({
    resolver: yupResolver(depositSchema),
    defaultValues: { amount: "0", asset: "WTK" }
  })
  const [amount, asset] = watch(["amount", "asset"])

  useEffect(() => {
    if (isSuccess) {
      Alert.alert(t("Transaction was successful."))
      setValue("amount", "0")
    }
  }, [isSuccess])

  const onSubmit = (data: AddFakeTransactionForm) => {
    addFakeTransaction({
      amount: Number(data.amount),
      asset: data.asset
    })
  }

  return (
    <>
      <ErrorModal error={error} />
      <Container justify="space-between" spacing={2} style={styles.container}>
        <SelectField
          label={t("Currency")}
          name="asset"
          control={control}
          error={errors.asset}
          items={currencies}
          placeholder={t("Select Currency")}
        />
        <Container direction="row" justify="center" noItemsStretch>
          <Typography variant="title" fontWeight="bold">
            {amount}{" "}
          </Typography>
          <Typography variant="title">{asset}</Typography>
        </Container>
        <NumericKeyboard
          value={amount}
          onChange={(value) => setValue("amount", value)}
          isValueAmount
        />
        <View style={styles.containerButton}>
          <Button
            text="Confirm Deposit"
            onPress={handleSubmit(onSubmit)}
            loading={isLoading}
          />
        </View>
      </Container>
    </>
  )
}

export default FakeOnRamp

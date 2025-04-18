import React, { useContext, useEffect } from "react"
import { yupResolver } from "@hookform/resolvers/yup"
import { useForm } from "react-hook-form"
import { useTranslation } from "react-i18next"
import { StyleSheet, TouchableOpacity, ScrollView } from "react-native"
import { subMonths } from "date-fns"

import { TransactionFilters } from "~/api/models"
import {
  Button,
  Container,
  DateField,
  Modal,
  MultiChoiceField,
  theme,
  Typography
} from "~/components/ui"
import { useValidationSchemas } from "~/constants"
import { TransactionFiltersForm } from "~/constants/formTypes"
import {
  useAssetSelectItems,
  useTransactionDirectionSelectItems,
  useTransactionStatusSelectItems,
  useTransactionTypeSelectItems
} from "~/constants/selectItems"
import { INITIAL_TRANSACTION_FILTERS } from "~/api/constants"
import { UserContext } from "~/context"
import { isIOS, showToast } from "~/utils"

const styles = StyleSheet.create({
  container: {
    marginHorizontal: theme.spacing.md,
    marginTop: theme.spacing.md,
    paddingBottom: theme.spacing.lg
  },
  content: {
    marginHorizontal: theme.spacing.md
  },
  actions: {
    marginHorizontal: theme.spacing.xl,
    marginBottom: theme.spacing.md
  },
  resetButton: {
    padding: theme.spacing.md
  }
})

type Props = {
  isFiltersOpen: boolean
  setIsFiltersOpen: (value: boolean) => void
  txFilters: TransactionFilters
  setTxFilters: (value: TransactionFilters) => void
  isDateFilterRemovedFromChip: boolean
  setIsDateFilterRemovedFromChip: (isDateFilterRemovedFromChip: boolean) => void
}

const FiltersModal: React.FC<Props> = ({
  isFiltersOpen,
  setIsFiltersOpen,
  txFilters,
  setTxFilters,
  isDateFilterRemovedFromChip,
  setIsDateFilterRemovedFromChip
}: Props) => {
  const { t } = useTranslation()
  const transactionDirectionSelectItems = useTransactionDirectionSelectItems()
  const transactionTypeSelectItems = useTransactionTypeSelectItems()
  const transactionStatusSelectItems = useTransactionStatusSelectItems()
  const assetSelectItems = useAssetSelectItems(true)
  const { transactionFiltersSchema } = useValidationSchemas()
  const {
    control,
    setValue,
    handleSubmit,
    formState: { errors },
    reset
  } = useForm<TransactionFiltersForm>({
    resolver: yupResolver(transactionFiltersSchema),
    defaultValues: txFilters
  })

  const hasFilters = Object.values(txFilters).some((val) =>
  Array.isArray(val) ? val.length : !!val
)
  
  // console.log("hasFilters ", hasFilters)
  const onSubmit = (data: TransactionFiltersForm) => {
   

if(data.type?.includes("OTHER")) {
  // Nandani: here check if the type filter has recieve or not if External Send is not in array we should not keep external recieve
  const dataTransactionType = data.type?.filter((item) => item !== "OTHER")
  data.type = dataTransactionType
}
    if(data.type?.includes("POS")) {
      data.type.push("OTHER")
    }

    const dateFromTemp = data.dateFrom
    const dateToTemp = data.dateTo
    dateFromTemp?.setUTCHours(0, 0, 0, 0)
    dateToTemp?.setHours(23)
    dateToTemp?.setMinutes(59)
    data.dateFrom = dateFromTemp
    data.dateTo = dateToTemp
    
    
     
    if( dateFromTemp > dateToTemp) {
      // show a toast
      const txt = t("ERROR_MESSAGE.TRANSACTION_FROM_DATE_LESSER_THAN_TO_DATE")
      isIOS ? alert(txt) : showToast(txt)
     } else {
      
      console.log("data : ",data)
      setTxFilters(data)
      setIsFiltersOpen(false)
      // console.log("checking data", JSON.stringify(data))
     }
  }

  useEffect(() => {
    if (isFiltersOpen) {
      reset({ ...txFilters })
      if(isDateFilterRemovedFromChip) {

      setValue("dateFrom",new Date())
      setValue("dateTo", new Date())
    }
     setIsDateFilterRemovedFromChip(false)
    }
  }, [isFiltersOpen])

  return (
    <Modal
      isVisible={isFiltersOpen}
      onDismiss={() => {
        if(hasFilters) {
          setIsFiltersOpen(false)
        } else { 
          setValue("dateFrom",new Date())
          setValue("dateTo", new Date())
          setTxFilters({
            dateFrom: undefined,
            dateTo: undefined,
            direction: "",
            status: [],
            type: [],
            asset: []
          })
          setIsFiltersOpen(false)
        }
        }
      }
      variant="bottom"
      title={t("Filter Transactions")}
      dismissButtonVariant="cancel"
      contentStyle={{
        height: theme.modalFullScreenHeight
      }}
    >
      <ScrollView contentContainerStyle={styles.container}>
        <Container style={styles.content}>
          <Container direction="row" justify="space-between">
            <DateField
              label={t("Date From")}
              name="dateFrom"
              control={control}
              iconName="Calendar"
              error={errors.dateFrom}
              
            />
            <DateField
              label={t("Date To")}
              name="dateTo"
              control={control}
              iconName="Calendar"
              error={errors.dateTo} // Same error because of schema dependency
            />
          </Container>
          {/* TODO amounts filters after BE part is resolved */}
          {/* <Container direction="row" justify="space-between">
            <TextField
              label={t("Amount From")}
              name="amountFrom"
              control={control}
              iconName="SendFunds"
              {...fieldProps.amount}
              error={errors.amountFrom}
            />
            <TextField
              label={t("Amount To")}
              name="amountTo"
              control={control}
              iconName="SendFunds"
              {...fieldProps.amount}
              error={errors.amountTo}
            />
          </Container> */}
          {/* <MultiChoiceField
            name="direction"
            control={control}
            items={transactionDirectionSelectItems}
            label={t("Direction")}
          />
          */}
          <MultiChoiceField
            name="type"
            control={control}
            items={transactionTypeSelectItems}
            label={t("Type")}
          /> 
          <MultiChoiceField
            name="status"
            control={control}
            items={transactionStatusSelectItems}
            label={t("Status")}
          />
          {/* <MultiChoiceField
            name="asset"
            control={control}
            items={assetSelectItems}
            label={t("Crypto Currency")}
          /> */}
        </Container>
        <Container spacing={1} style={styles.actions}>
          <TouchableOpacity onPress={() =>{ 
                                         
                                           reset({ ...{
                                            dateFrom: new Date(),
                                            dateTo: new Date(),
                                            direction: "",
                                            status: [],
                                            type: [],
                                            asset: []
                                          } })
                                        //   setValue("dateFrom",new Date())
                                        // setValue("dateTo", new Date())

                                            // setTxFilters({
                                            //   dateFrom: undefined,
                                            //   dateTo: undefined,
                                            //   direction: "",
                                            //   status: [],
                                            //   type: [],
                                            //   asset: []
                                            // }) 
                                           
                                          // setIsFiltersOpen(false)
                                          }}>
            <Typography
              variant="button"
              color="orange"
              style={styles.resetButton}
            >
              {t("Reset filters")}
            </Typography>
          </TouchableOpacity>
          <Button text={t("Apply")} onPress={handleSubmit(onSubmit)} />
        </Container>
      </ScrollView>
    </Modal>
  )
}

export default FiltersModal

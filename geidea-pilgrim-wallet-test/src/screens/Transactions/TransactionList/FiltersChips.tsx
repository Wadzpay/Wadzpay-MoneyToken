import React from "react"
import { useTranslation } from "react-i18next"
import { StyleSheet, View } from "react-native"
import { ScrollView } from "react-native-gesture-handler"
import { TransactionFilters } from "~/api/models"
import { Chip, Container } from "~/components/ui"
import {
  useTransactionDirectionChipProps,
  useTransactionTypeChipProps,
  useTransactionStatusChipProps,
  useAssetChipProps
} from "~/components/ui/Chip"
import theme, { ChipColorVariant } from "~/components/ui/theme"
import { IconName } from "~/icons"

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    flexWrap: "wrap"
  },
  chip: {
    marginRight: theme.spacing.xs,
    marginBottom: theme.spacing.xs
  }
})

type FilterNames = "direction" | "type" | "status" | "asset"
type FilterProps = {
  title: string
  color: ChipColorVariant
  iconName?: IconName
}

const useChipProps: () => {
  [key in FilterNames]: {
    [key: string]: FilterProps
  }
} = () => {
  const direction = useTransactionDirectionChipProps()
  const type = useTransactionTypeChipProps()
  const status = useTransactionStatusChipProps()
  const asset = useAssetChipProps()
  return {
    direction,
    type,
    status,
    asset
  }
}

type Props = {
  filters: TransactionFilters
  setFilters: (filters: TransactionFilters) => void
  isDateFilterRemovedFromChip: boolean
  setIsDateFilterRemovedFromChip: (isDateFilterRemovedFromChip: boolean) => void
}

const FiltersChips: React.FC<Props> = ({ filters, setFilters, setIsDateFilterRemovedFromChip }: Props) => {
  const {
    dateFrom,
    dateTo,
    amountFrom,
    amountTo,
    direction,
    type,
    status,
    asset
  } = filters
  const chipProps = useChipProps()
  const { t } = useTranslation()

  const removeFilterByNames: (
    names: (keyof TransactionFilters)[],
    values?: { [key in keyof TransactionFilters]: string }
  ) => void = (names, values) => {

    const filtersToRemove = 
    names.reduce(
      (obj, name) => ({
        ...obj,
        [name]:
          values && Array.isArray(filters[name])
            ? [
                ...(filters[name] as string[]).filter(
                  (val) => {
                   return val === "OTHER" ?  false : val !== values[name] 
                  }
                )
              ]
            : undefined
      }),
      {}
    )
    setFilters({
      ...filters,
      ...filtersToRemove
    })
  }

  return (
    <View style={styles.container}>

      {/* For Date */}
      {(!!dateFrom || !!dateTo) && (
        <Chip
          text={`${new Date(
            dateFrom ? dateFrom : 1
          ).toLocaleDateString()} - ${(dateTo
            ? new Date(dateTo)
            : new Date()
          ).toLocaleDateString()}`}
          onPress={() => {setIsDateFilterRemovedFromChip (true)
            removeFilterByNames(["dateFrom", "dateTo"])}}
          leftIconName="Calendar"
          rightIconName="Close"
          color="orange"
          style={styles.chip}
        />
      )}
      {(!!amountFrom || !!amountTo) && (
        <Chip
          text={`${amountFrom ? `${t("From")} ${amountFrom}` : ""} ${`${
            amountTo ? `${t("To")} ${amountTo}` : ""
          }`}`}
          onPress={() => removeFilterByNames(["amountFrom", "amountTo"])}
          leftIconName="SendFunds"
          rightIconName="Close"
          color="black"
          style={styles.chip}
        />
      )}

{/* {Object.entries({
        direction,
        type,
        status,
        asset
      }).map(
        ([key, value]) =>
          {
            console.log("key, value ", key, value)
          }
      )} */}
      {Object.entries({
        direction,
        type,
        status,
        asset
      }).map(
        ([key, value]) =>
          !!value && (
            <ScrollView horizontal={true} showsHorizontalScrollIndicator={false}>
            <Container key={`${key}-${value}`} direction="row" noItemsStretch>
              {Array.isArray(value) ? (
                (value as []).map((filter: string) => (
                 <Chip
                    key={filter}
                    text={chipProps[key as FilterNames][filter]?.title}
                    onPress={() =>
                      removeFilterByNames([key as FilterNames], {
                        [key]: filter
                      })
                    }
                    color={chipProps[key as FilterNames][filter]?.color}
                    leftIconName={
                      chipProps[key as FilterNames][filter]?.iconName
                    }
                    rightIconName="Close"
                    style={styles.chip}
                  />
                ))
              ) : (
                <Chip
                  text={chipProps[key as FilterNames][value].title}
                  onPress={() => removeFilterByNames([key as FilterNames])}
                  color={chipProps[key as FilterNames][value].color}
                  leftIconName={chipProps[key as FilterNames][value].iconName}
                  rightIconName="Close"
                  style={styles.chip}
                />
              )}
            </Container>
            </ScrollView>
          )
      )}
    </View>
  )
}

export default FiltersChips

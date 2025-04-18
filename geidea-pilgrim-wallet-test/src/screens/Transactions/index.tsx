import React, { useState } from "react"
import { useTranslation } from "react-i18next"
import { StyleSheet, View } from "react-native"
import { TFunction } from "i18next"

import FiltersModal from "./TransactionList/FiltersModal"
import FiltersChips from "./TransactionList/FiltersChips"

import { Container, Icon, TextFieldControlled, theme } from "~/components/ui"
import { useUserTransactions } from "~/api/user"
import { Transaction, TransactionFilters } from "~/api/models"
import { INITIAL_TRANSACTION_FILTERS } from "~/api/constants"
import { getParamsFromObject, groupBy } from "~/helpers"
import ScreenLayoutList from "~/components/ui/ScreenLayoutList"
import TransactionItem from "~/screens/Transactions/TransactionList/TransactionItem"
import { ListItem } from "~/components/ui/List"
import Email from "~/icons/Email"
// import jsPDF from "jspdf"
// import autoTable from "jspdf-autotable"
const styles = StyleSheet.create({
  searchContainer: {
    marginTop: theme.spacing.lg,
    marginBottom: theme.spacing.xs,
    paddingHorizontal: theme.spacing.lg
  },
  filtersContainer: {
    paddingHorizontal: theme.spacing.md,
    paddingBottom: theme.spacing.md
  }
})

const getDataWithSections = (data: ListItem[], t: TFunction) => {
  const transactions = data as Transaction[]
  // console.log('all txn data', data);
  return Object.entries(
    groupBy(transactions, (tx) => new Date(tx.createdAt).toLocaleDateString())
  ).map(([title, data]) => ({
    title: title === new Date().toLocaleDateString() ? t("Today") : title,
    data
  }))
}

const Transactions: React.FC = () => {
  const { t } = useTranslation()
  const [isSearchOpen, setIsSearchOpen] = useState<boolean>(false)
  const [isFiltersOpen, setIsFiltersOpen] = useState<boolean>(false)
  const [isDateFilterRemovedFromChip, setIsDateFilterRemovedFromChip] = useState<boolean>(false)

  const [search, setSearch] = useState<any>("")
  // TODO Pagination on scroll
  // const [page, setPage] = useState<string>("")
  const [txFilters, setTxFilters] = useState<TransactionFilters>(
    INITIAL_TRANSACTION_FILTERS
  )
  const hasFilters = Object.values(txFilters).some((val) =>
    Array.isArray(val) ? val.length : !!val
  )
  const regexEmail = /\S+@\S+\.\S+/
  //  console.log("isNaN(search) && isNaN(parseFloat(search)) && (search.match(regexEmail)) ", isNaN(search) , isNaN(parseFloat(search)) ,  (search.match(regexEmail)))
   // TODO NANDANI :  this is for partial search which we have to implement later on 
   // const paramsString = getParamsFromObject({
    // search: isSearchOpen ? search.toLocaleLowerCase() : "",
  //   uuidSearch: isSearchOpen
  //     ? isNaN(search) &&
  //       !search.match(
  //         /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  //       )
  //       ? search
  //       : ""
  //     : "",
  //   amount: isSearchOpen
  //     ? !isNaN(search) &&
  //       !isNaN(parseFloat(search)) &&
  //       !search.match(regexEmail)
  //       ? search
  //       : ""
  //     : "",
  //   // page,
  //   ...txFilters,
  //   dateFrom: txFilters.dateFrom?.toISOString(),
  //   dateTo: txFilters.dateTo?.toISOString()
   // }).toString()
   // TODO NANDANI :  this is for partial search which we have to implement later on 

   // TODO Nandani : search with complete email/transactionid/amount
  const paramsString = getParamsFromObject({
    // Swati : below codes are commented so that we can support Partial email search along with whole amount an dwhole transaction id

    // search: isSearchOpen ? (search.match(regexEmail) ? search.toLocaleLowerCase() : "") : "",
    search: isSearchOpen ? search.toLocaleLowerCase() : "",
    // uuid: isSearchOpen
    //   ? isNaN(search) &&
    //     !search.match(
    //       /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    //     )
    //     ? search
    //     : ""
    //   : "",
    // amount: isSearchOpen
    //   ? !isNaN(search) &&
    //     !isNaN(parseFloat(search)) &&
    //     !search.match(regexEmail)
    //     ? search
    //     : ""
    //   : "",
    "appSearch":"true",
    ...txFilters,
    "asset":"SART",
    dateFrom: txFilters.dateFrom?.toISOString(),
    dateTo: txFilters.dateTo?.toISOString()
  }).toString()
 // TODO Nandani : search with complete email/transactionid/amount

  const RightComponent = (
    <Container direction="row" alignItems="center" noItemsStretch spacing={1}>
      <Icon
        name="Search"
        onPress={() => setIsSearchOpen(!isSearchOpen)}
        color={isSearchOpen ? "orange" : "regular"}
        isActive={isSearchOpen}
      />
      <Icon
        name="Filter"
        onPress={() => setIsFiltersOpen(true)}
        color={hasFilters ? "orange" : "regular"}
        isActive={hasFilters}
      />
      {/* <Icon
        name="ArrowDown"
        onPress={() => populatePdfData()}
        color={"regular"}
      /> */}
    </Container>
  )

  // const {
  //   data: transactionData,
  //   isFetching: isFetchingTransactions,
  //   refetch: refetchTransactions,
  //   error: errorTransactions
  // } = useUserTransactions("page=0")

  // const populatePdfData = () => {
  //     if (transactionData) {
  //       const currentData = [...transactionData]
  //       const head = [
  //         [
  //           "Sl.No",
  //           "Date",
  //           "Transaction ID",
  //           "Amount",
  //           "Fee Amount",
  //           "Asset",
  //           "Total Amount",
  //           "Fiat Value",
  //           "Status"
  //         ]
  //       ]
  //       const finalData: any = []
  //       currentData.map((item, index) => {
  //         const arr = []
  //         arr.push(index + 1)
  //         if (Object.prototype.hasOwnProperty.call(item, "createdAt")) {
  //           arr.push(new Date(item.createdAt).toDateString())
  //         }
  //         if (Object.prototype.hasOwnProperty.call(item, "uuid")) {
  //           arr.push(item.uuid)
  //         }
  //         if (Object.prototype.hasOwnProperty.call(item, "amount")) {
  //           arr.push(item.amount)
  //         }
  //         if (Object.prototype.hasOwnProperty.call(item, "feeAmount")) {
  //           arr.push(item.feeAmount)
  //         }
  //         if (Object.prototype.hasOwnProperty.call(item, "asset")) {
  //           arr.push(item.asset)
  //         }
  //         if (Object.prototype.hasOwnProperty.call(item, "totalAmount")) {
  //           arr.push(item.totalAmount)
  //         }
  //         if (Object.prototype.hasOwnProperty.call(item, "fiatAmount")) {
  //           arr.push(item.fiatAmount)
  //         }
  //         if (Object.prototype.hasOwnProperty.call(item, "status")) {
  //           arr.push(item.status)
  //         }
  //         finalData.push(arr)
  //       })
  //       const doc = new jsPDF()
  //       autoTable(doc, {
  //         head: head,
  //         body: finalData,
  //         columnStyles: {
  //           0: { cellWidth: 10 },
  //           1: { cellWidth: 28 },
  //           5: { cellWidth: 15 }
  //         }
  //       })

  //       doc.save(`${new Date()}myReports-Transactions.pdf`)
  //     }
  //   }

  let renderTransactionItem = (item: ListItem) => {
    // populatePdfData(item)
    return <TransactionItem item={item as Transaction} />
  }
  return (
    <ScreenLayoutList
      listQuery={useUserTransactions}
      localization={{
        retry: t("Retry"),
        noItemsAvailable: t("No transactions available."),
        title: t("Transactions")
      }}
      listItemComponent={(item: ListItem) => renderTransactionItem(item)}
      getSections={(data: ListItem[]) => getDataWithSections(data, t)}
      rightComponent={RightComponent}
      queryParams={paramsString}
      isSearchOpen={isSearchOpen}
    >
      <FiltersModal
      isDateFilterRemovedFromChip={isDateFilterRemovedFromChip}
      setIsDateFilterRemovedFromChip={setIsDateFilterRemovedFromChip}
        isFiltersOpen={isFiltersOpen}
        setIsFiltersOpen={setIsFiltersOpen}
        txFilters={txFilters}
        setTxFilters={setTxFilters}
      />
      {hasFilters && (
        <View style={styles.filtersContainer}>
          <FiltersChips 
          isDateFilterRemovedFromChip={isDateFilterRemovedFromChip} 
          filters={txFilters} 
          setFilters={setTxFilters} 
          setIsDateFilterRemovedFromChip={setIsDateFilterRemovedFromChip}/>
        </View>
      )}
      {isSearchOpen && (
        <View style={styles.searchContainer}>
          <TextFieldControlled
            value={search}
            onChange={setSearch}
            placeholder={"Search by Email/Amount/Transaction Id"}
            iconName="Search"
            autoFocus
            isClearable
          />
        </View>
      )}
    </ScreenLayoutList>
  )
}

export default Transactions

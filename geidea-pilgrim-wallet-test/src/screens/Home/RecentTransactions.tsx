import React from "react"
import { CommonActions, useNavigation } from "@react-navigation/native"
import { useTranslation } from "react-i18next"
import { View, StyleSheet, TouchableOpacity } from "react-native"

import { Container, LoadingSpinner, theme, Typography } from "~/components/ui"
import { Transaction } from "~/api/models"
import List, { ListItem } from "~/components/ui/List"
import TransactionItem from "~/screens/Transactions/TransactionList/TransactionItem"

const PAGE_LIMIT = 4

const styles = StyleSheet.create({
  container: {
    height: "100%",
    width: "100%",
    flex: 1,
    justifyContent: "flex-end",
    top: 0
  },
  titleContainer: {
    marginTop: -40, //theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
   
  
  },
  transactionListContainer: {
    paddingBottom: -70,
    paddingTop:-50,
    marginBottom:-40
    
  }
})

type Props = {
  data?: Transaction[]
  isFetching: boolean
}

const RecentTransactions: React.FC<Props> = ({ data, isFetching }: Props) => {
  const navigation = useNavigation()
  const { t } = useTranslation()
  const shownData = data ? data.slice(0, PAGE_LIMIT) : []

  return (
    <Container style={styles.container}>
      <Container
        direction="row"
        justify="space-between"
        noItemsStretch
        style={styles.titleContainer}
      >
        <Typography
          fontFamily="Rubik-Medium"
          variant="body"
          textAlign="left"
          fontWeight="bold"
          color="iconRegulorColor"
          style={{ marginLeft: 10 }}
        >
          {t("Recent Transactions")}
        </Typography>
        <TouchableOpacity
          onPress={() =>
            navigation.dispatch(
              CommonActions.reset({
                index: 0,
                routes: [
                  {
                    name: "Transactions"
                  }
                ]
              })
            )
          }
        >
          <Typography
            variant="label"
            color="blueLinkColor"
            fontFamily="Rubik-Regular"
            style={{marginRight: 10}}
          >
            {t("View All")}
          </Typography>
        </TouchableOpacity>
      </Container>
      <View style={styles.transactionListContainer}>
        {isFetching ? (
          <Container alignItems="center" noItemsStretch>
            <LoadingSpinner color="orange" />
          </Container>
        ) : (
          <List
            data={shownData}
            listItemComponent={(item: ListItem) => (
              <TransactionItem item={item as Transaction} showDate={true} />
            )}
            isHomeList={true}
            localization={{
              retry: t("Retry"),
              noItemsAvailable: t("No transactions available.")
            }}
          />
        )}
      </View>
    </Container>
  )
}
export default RecentTransactions

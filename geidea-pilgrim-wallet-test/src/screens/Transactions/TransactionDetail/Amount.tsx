import React from "react"
import { View, StyleSheet } from "react-native"

import { Container, Icon, theme, Typography, FiatAmount } from "~/components/ui"
import { Transaction } from "~/api/models"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"

const styles = StyleSheet.create({
  spaceEqualizer: {
    // compensate for the space taken by the Icon on the other side to center amount
    width: theme.iconSize.lg
  }
})

type Props = {
  transaction: Transaction
}

const Amount: React.FC<Props> = ({ transaction }: Props) => {
  const formatter = useFormatCurrencyAmount()
  return (
    <Container
      direction="row"
      justify="center"
      alignItems="center"
      spacing={1}
      noItemsStretch
    >
      <View style={styles.spaceEqualizer} />
      <>
        <Container
          direction="row"
          justify="center"
          alignItems="center"
          noItemsStretch
        >
          <Typography variant="title" fontWeight="bold">
            {formatter(transaction.amount, {
              asset: transaction.asset
            })}{" "}
          </Typography>
          <Typography variant="title">{transaction.asset}</Typography>
        </Container>

        {transaction.fiatAmount && transaction.asset != "SART" ? (
                    <FiatAmount
                      amount={transaction.fiatAmount}
                      fiatAsset={transaction.fiatAsset}
                      variant="subtitle"
                      />
                  ) : null}


        {/* {!!transaction.fiatAmount && (
          <FiatAmount
            amount={transaction.fiatAmount}
            fiatAsset={transaction.fiatAsset}
            variant="subtitle"
          />
        )} */}
      </>
      {transaction.direction === "OUTGOING" ? (
        <Icon name="ArrowUp" color="error" size="lg" />
      ) : (
        <Icon name="ArrowDown" color="success" size="lg" />
      )}
    </Container>
  )
}

export default Amount

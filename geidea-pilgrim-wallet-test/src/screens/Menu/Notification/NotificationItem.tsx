import React from "react"
import { useNavigation, useRoute } from "@react-navigation/native"
import { StyleSheet, TouchableOpacity } from "react-native"

import {
  Container,
  Icon,
  spacing,
  theme,
  Typography,
  UserAvatar,
  Chip
} from "~/components/ui"
import { useTransactionStatusChipProps } from "~/components/ui/Chip"
import {
  SendNotificationForRecievingPaymentData,
  Transaction
} from "~/api/models"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"

const styles = StyleSheet.create({
  container: {
    height: "100%",
    paddingHorizontal: theme.spacing.md
  },
  transactionRow: {
    marginVertical: spacing(0.75) // 6px
  },
  avatar: {
    padding: spacing(0.5),
    marginRight: theme.spacing.xs
  }
})

type Props = {
  item: SendNotificationForRecievingPaymentData
}

const NotificationItem: React.FC<Props> = ({ item }: Props) => {
  const navigation = useNavigation()
  const route = useRoute()
  const formatter = useFormatCurrencyAmount()
  const tsProps = useTransactionStatusChipProps()
  return (
    <TouchableOpacity
      onPress={() =>
        route.name === "Transactions"
          ? navigation.navigate("TransactionDetail", {
              transactionId: item.id
            })
          : navigation.navigate("Transactions", {
              screen: "TransactionDetail",
              initial: false,
              params: { transactionId: item.id }
            })
      }
    >
      <Container
        direction="row"
        justify="space-between"
        alignItems="center"
        noItemsStretch
        style={styles.transactionRow}
      >
        <Container direction="row" alignItems="center" noItemsStretch>
          <Container style={styles.avatar}>
            <UserAvatar
              name={
                item.direction === "OUTGOING"
                  ? item.receiverName
                  : item.senderName
              }
            />
          </Container>
          <Container noItemsStretch>
            <Typography
              textAlign="left"
              variant="label"
              numberOfLines={1}
              ellipsizeMode={"tail"}
              style={{ width: 150 }}
            >
              {item.direction === "OUTGOING"
                ? item.receiverName
                : item.senderName}
            </Typography>
            <Typography variant="label" color="grayLight">
              {new Date(item.createdAt).toLocaleDateString()}
            </Typography>
          </Container>
        </Container>
        <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          spacing={1}
        >
          <Container alignItems="flex-end" noItemsStretch>
            <Typography>
              {formatter(item.amount, {
                asset: item.asset
              })}{" "}
              {item.asset}
            </Typography>
            {item.status !== "SUCCESSFUL" && (
              <Chip
                text={tsProps[item.status].title}
                color={tsProps[item.status].color}
              />
            )}
          </Container>
          <Icon
            name={
              item.direction === "INCOMING"
                ? "ArrowDown"
                : item.direction === "OUTGOING"
                ? "ArrowUp"
                : "ArrowLeft"
            }
            size="xs"
            color={
              item.direction === "INCOMING"
                ? "success"
                : item.direction === "OUTGOING"
                ? "error"
                : "orange"
            }
          />
        </Container>
      </Container>
    </TouchableOpacity>
  )
}
export default NotificationItem

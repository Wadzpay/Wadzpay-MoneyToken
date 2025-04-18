import React from "react"
import { useTranslation } from "react-i18next"

import InfoItem from "./InfoItem"

import { Container, Typography, UserAvatar } from "~/components/ui"
import { useTransactionStatusChipProps } from "~/components/ui/Chip"
import { Transaction } from "~/api/models"
import useFormatCurrencyAmount from "~/helpers/formatCurrencyAmount"

type Props = {
  transaction: Transaction
}

const DetailInfo: React.FC<Props> = ({ transaction }: Props) => {
  const { t } = useTranslation()
  const formatter = useFormatCurrencyAmount()
  const tsProps = useTransactionStatusChipProps()
  const date = new Date(transaction.createdAt)
  const isP2pTx = transaction.transactionType === "PEER_TO_PEER"
  const isMerchantTx = transaction.transactionType === "MERCHANT"
  return (
    <Container spacing={3}>
      <InfoItem title={"Transaction Id"} text={transaction.uuid} />
      <InfoItem
        title={
          transaction.direction === "OUTGOING" ? t("Beneficiary") : t("Sender")
        }
        component={
          <Container
            direction="row"
            alignItems="center"
            spacing={2}
            noItemsStretch
          >
            <UserAvatar
              name={
                transaction.direction === "OUTGOING"
                  ? transaction.receiverName
                  : transaction.senderName
              }
            />
            <Typography>
              {transaction.direction === "OUTGOING"
                ? transaction.receiverName
                : transaction.senderName}
            </Typography>
          </Container>
        }
      />

      <Container direction="row" justify="space-between" noItemsStretch>
        <Container spacing={3}>
          <InfoItem
            title={t("Date")}
            text={`${date.toLocaleTimeString()} ${date.toLocaleDateString()}`}
          />

          <InfoItem
            title={isP2pTx ? t("Transferred Amount") : t("Amount")}
            text={formatter(transaction.amount, {
              asset: transaction.asset
            })}
          />

          {isP2pTx && (
            <InfoItem
              title={t("Fee")}
              text={`${transaction.feeAmount} (${transaction.feePercentage}%)`}
            />
          )}

          {isP2pTx && (
            <InfoItem
              title={t("Total Amount")}
              text={transaction.totalAmount.toString()}
            />
          )}

          {isMerchantTx && (
            <InfoItem
              title={t("Description")}
              text={transaction.description || "-"}
            />
          )}
        </Container>

        <Container spacing={3}>
          <InfoItem
            title={t("Status")}
            isChip
            text={tsProps[transaction.status].title}
            chipColor={tsProps[transaction.status].color}
          />

          <InfoItem title={t("Currency")} text={transaction.asset} />
        </Container>
      </Container>
    </Container>
  )
}

export default DetailInfo

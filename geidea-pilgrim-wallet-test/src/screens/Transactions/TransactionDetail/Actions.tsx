/* eslint-disable no-console */
import React, { useState } from "react"
import { useTranslation } from "react-i18next"
import { StyleSheet, TouchableOpacity } from "react-native"

import {
  Container,
  Icon,
  theme,
  Typography,
  UnderDevelopmentModal
} from "~/components/ui"
import { TransactionDirection, TransactionType } from "~/constants/types"
import { IconName } from "~/icons"
import { Transaction } from "~/api/models"

type Action = {
  title: string
  iconName: IconName
  onPress: (...args: unknown[]) => void
}

// TODO specific actions for specific transaction types
const useActionsData: (onPress: (...args: unknown[]) => void) => {
  [key in TransactionType]: { [key in TransactionDirection]: Action[] }
} = (onPress) => {
  const { t } = useTranslation()
  const generalOutgoingActions: Action[] = [
    {
      title: t("Duplicate Payment"),
      iconName: "SendFunds",
      onPress
    },
    {
      title: t("Add Contact"),
      iconName: "AddUser",
      onPress
    },
    {
      title: t("Generate Invoice"),
      iconName: "Invoice",
      onPress
    }
  ]
  const generalIncomingActions: Action[] = [
    {
      title: t("New Payment"),
      iconName: "SendFunds",
      onPress
    },
    {
      title: t("Add Contact"),
      iconName: "AddUser",
      onPress
    },
    {
      title: t("Generate Invoice"),
      iconName: "Invoice",
      onPress
    },
    {
      title: t("Request Payment"),
      iconName: "ReceiveFunds",
      onPress
    }
  ]

  return {
    MERCHANT: {
      OUTGOING: generalOutgoingActions,
      INCOMING: generalIncomingActions,
      UNKNOWN: []
    },
    PEER_TO_PEER: {
      OUTGOING: generalOutgoingActions,
      INCOMING: generalIncomingActions,
      UNKNOWN: []
    },
    ON_RAMP: {
      OUTGOING: generalOutgoingActions,
      INCOMING: generalIncomingActions,
      UNKNOWN: []
    },
    OFF_RAMP: {
      OUTGOING: generalOutgoingActions,
      INCOMING: generalIncomingActions,
      UNKNOWN: []
    },
    OTHER: {
      OUTGOING: generalOutgoingActions,
      INCOMING: generalIncomingActions,
      UNKNOWN: []
    }
  }
}

const styles = StyleSheet.create({
  action: {
    padding: theme.spacing.xs,
    maxWidth: 75
  },
  label: {
    flexWrap: "wrap"
  }
})

type Props = {
  transaction: Transaction
}

const Actions: React.FC<Props> = ({ transaction }: Props) => {
  const [isVisible, setIsVisible] = useState<boolean>(false)
  const actionsData = useActionsData(() => setIsVisible(true))
  return (
    <>
      <UnderDevelopmentModal
        isVisible={isVisible}
        setIsVisible={setIsVisible}
      />
      <Container direction="row" justify="center" spacing={1}>
        {actionsData[transaction.transactionType][transaction.direction].map(
          (action) => (
            <TouchableOpacity key={action.title} onPress={action.onPress}>
              <Container
                justify="center"
                alignItems="center"
                spacing={0.5}
                noItemsStretch
                style={styles.action}
              >
                <Icon name={action.iconName} size="lg" />
                <Typography
                  variant="label"
                  color="grayLight"
                  style={styles.label}
                >
                  {action.title}
                </Typography>
              </Container>
            </TouchableOpacity>
          )
        )}
      </Container>
    </>
  )
}

export default Actions

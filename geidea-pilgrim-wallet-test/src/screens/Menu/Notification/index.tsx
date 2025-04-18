import React, { useContext, useRef } from "react"
import { useTranslation } from "react-i18next"
import {
  Alert,
  StyleSheet,
  TouchableWithoutFeedback,
  View,
  TouchableOpacity
} from "react-native"
import PickerSelect from "react-native-picker-select"

import {
  Icon,
  LoadingSpinner,
  ScreenLayoutTab,
  SelectFieldControlled,
  spacing
} from "~/components/ui"
import { RootNavigationProps } from "~/components/navigators"
import { Typography, theme, Container } from "~/components/ui"
import { FiatAsset } from "~/constants/types"
import { CommonActions } from "@react-navigation/native"
import ArrowLeft from "~/icons/ArrowLeft"
import { UserContext } from "~/context"
import { useGetPushNotificationData } from "~/api/user"
import List, { ListItem } from "~/components/ui/List"
import { SendNotificationForRecievingPaymentData } from "~/api/models"
import NotificationItem from "./NotificationItem"
import ScreenLayoutList from "~/components/ui/ScreenLayoutList"
import { formatAMPM, isIOS } from "~/utils"
import { Badge } from "react-native-elements"

const styles = StyleSheet.create({
  container: {
    padding: theme.spacing.lg
  },
  row: {
    flexWrap: "wrap"
  },
  devSection: {
    marginTop: theme.spacing.lg
  },
  selectContainer: {
    height: spacing(4.5),
    borderBottomWidth: theme.borderWidth.xs,
    borderBottomColor: theme.colors.gray.medium
  },
  label: {
    marginHorizontal: spacing(0.5)
  },
  separator: {
    marginTop: 10,
    marginBottom: 10,
    backgroundColor: theme.colors.gray.light,
    height: 1
  }
})

const Notification: React.FC<RootNavigationProps> = ({
  navigation
}: RootNavigationProps) => {
  const { t } = useTranslation()
  const { user } = useContext(UserContext)
  // const {
  //   data: notificationData,
  //   isFetching: isFetchingNotification,
  //   refetch: refetchNotification,
  //   error: errorNotification
  // } = useGetPushNotificationData(user?.attributes.email  || "")

  // // console.log("notificationData ", notificationData)

  const TITLE = "Payment Request Raised"
  const BODY = "You have Raised a Payment Request"

  let showCustomBodyText = (item) => {
    // // console.log("item " , item)

    // request raised
    if (item.requesterEmail === user?.attributes.email) {
      if (item.status === "NEW") {
        return `You have raised payment request to ${item.receiverName}`
      }
    }

    // request approved / rejected
    if (item.receiverEmail === user?.attributes.email) {
      if (item.status === "SUCCESSFUL" || item.status === "OVERPAID" || item.status === "UNDERPAID") {
        return `You approved payment to ${item.requesterName}`
      } else if (item.status === "FAILED") {
        return `You rejected payment to ${item.requesterName}`
      }
    }

    return item.body
  }
  return (
    <ScreenLayoutList
      isListReverse={false}
      onLeftIconClick={navigation.goBack}
      leftIconName="ArrowLeft"
      listQuery={useGetPushNotificationData}
      queryParams={""}
      localization={{
        retry: t(""),
        noItemsAvailable: "No notification Available",
        title: "Notification"
      }}
      listItemComponent={(item: ListItem) => {
        return (
          <Container>
            <TouchableOpacity
              style={styles.container}
              onPress={() =>
                navigation.navigate(
                  "FromNotificationPaymentConfirmationScreen",
                  {
                    notificationDataParam: item
                  }
                )
              }
            >
              {/* const requesterEmailId = notificationDataParam? notificationDataParam?.requesterEmail :  notificationData.requesterEmail
// console.log("requesterEmailId " , requesterEmailId)
const isUserSame = user?.attributes.email  === requesterEmailId */}
              <Container alignItems="center">
                <Container
                  direction="row"
                  noItemsStretch
                  justify="space-evenly"
                  alignItems="center"
                >
                  <Icon name="PaymentRequest" size="md" />
                  <Container
                    justify="space-evenly"
                    alignItems="center"
                    style={{ marginLeft: isIOS ? 18 : 8 }}
                  >
                    <Container spacing={0} direction="row">
                      <Typography
                        variant="label"
                        textAlign="left"
                        color="grayDark"
                      >
                        {item.requesterEmail === user?.attributes.email &&
                        item.status === "NEW"
                          ? TITLE
                          : item.title}
                      </Typography>
                      <Typography
                        style={{ marginLeft: 8 }}
                        variant="chip"
                        textAlign="right"
                        color="grayDark"
                      >
                        {new Date(item.timeNotification).toDateString()}{" "}
                        {formatAMPM(new Date(item.timeNotification))}
                      </Typography>
                    </Container>
                    <Typography
                      style={{ marginRight: 8 }}
                      variant="label"
                      textAlign="left"
                      color="grayDark"
                    >
                      {showCustomBodyText(item)}
                    </Typography>
                  </Container>
                  {item.status === "NEW" ? <Badge status="error" /> : undefined}
                </Container>
              </Container>
            </TouchableOpacity>
            {/* <View style={styles.separator} /> */}
          </Container>
        )
      }}
    />
  )
}

export default Notification

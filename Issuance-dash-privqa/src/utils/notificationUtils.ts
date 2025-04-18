import { notification } from "antd"

export const showNotification = (
  type: "success" | "error",
  message: string,
  description: string
) => {
  notification[type]({
    message,
    description
  })
}

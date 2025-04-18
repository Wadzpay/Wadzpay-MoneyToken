import env, { ENV } from "./env"

export const isDev = env.TYPE === ENV.DEV

export const formatPhoneNumber: (phoneNumber: string) => string = (
  phoneNumber
) => {
  return phoneNumber.replace(/[^+\d]/g, "")
}

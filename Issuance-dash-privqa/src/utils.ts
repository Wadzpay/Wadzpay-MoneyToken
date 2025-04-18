import env, { ENV } from "./env"

export const isDev = env.TYPE === ENV.DEV
export const isTest = env.TYPE === ENV.TESTING
export const isProd = env.TYPE === ENV.PRIVATE_CHAIN_PROD

export const formatPhoneNumber: (phoneNumber: string) => string = (
  phoneNumber
) => {
  return phoneNumber.replace(/[^+\d]/g, "")
}

export const numerAllowOnlyTwoDecimal = (event: any) => {
  const value = event.target.value
  const key = event.key

  // Allow backspace and delete keys
  if (key === "Backspace" || key === "Delete") {
    return
  }

  if (value < 0) {
    return
  }

  // Allow only digits and a single dot, and validate the input
  if (
    !/^\d$/.test(key) && // Allow digits
    key !== "." // Allow a single dot
  ) {
    event.preventDefault()
    return
  }

  if (key === "." && value.includes(".")) {
    event.preventDefault()
    return
  }

  // if (value.includes(".") && value.split(".")[1].length >= 2) {
  //   event.preventDefault()
  //   return
  // }
}

export const numerAllowOnlyFiveDecimal = (event: any) => {
  const value = event.target.value
  const key = event.key

  // Allow backspace and delete keys
  if (key === "Backspace" || key === "Delete") {
    return
  }

  if (value < 0) {
    return
  }

  // Allow only digits and a single dot, and validate the input
  if (
    !/^\d$/.test(key) && // Allow digits
    key !== "." // Allow a single dot
  ) {
    event.preventDefault()
    return
  }

  if (key === "." && value.includes(".")) {
    event.preventDefault()
    return
  }

  // if (value.includes(".") && value.split(".")[1].length >= 5) {
  //   event.preventDefault()
  //   return
  // }
}

export const timeZone = {
  value: "Asia/Kolkata",
  label: "(GMT+5:30) Chennai, Kolkata, Mumbai, New Delhi",
  offset: 5.5,
  abbrev: "IST",
  altName: "India Standard Time"
}

export const hasImageExtension = (str: any) => {
  // Regular expression to match any image extension
  const imageExtensions =
    /\.(jpeg|jpg|gif|png|bmp|webp|svg|tiff|ico|heic|heif|raw|jfif|pjpeg|pjp|avif)$/i

  // Check if the string contains any image extension
  return imageExtensions.test(str)
}

export const isValidHost = (url: string) => {
  const domain = "wadzpay.com"
  const dev = "localhost"

  if (
    url.toLocaleLowerCase().includes(domain) ||
    url.toLocaleLowerCase().includes(dev)
  ) {
    return true
  }
  return false
}

export const allowOnlyNumber = (e: any) => {
  const charCode = e.charCode
  // Allow only digits (0-9) and control keys like Backspace (charCode 8)
  if (charCode !== 8 && (charCode < 48 || charCode > 57)) {
    e.preventDefault()
  }
}

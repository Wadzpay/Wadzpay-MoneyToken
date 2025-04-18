import env from "src/env.template"

import { ENV } from "../env"

const REFUND_AMOUNT_LIMIT = 0
const WADZPAY_LOGO = true
const DISPLAY_ATM_MENU = false
const ATM_BANK_NAME = "ATM"
const WALLET_MIN_LENGTH =
  env.TYPE === ENV.DEV || env.TYPE === ENV.TESTING ? 32 : 42 // FOR DDF 42
const WALLET_MAX_LENGTH =
  env.TYPE === ENV.DEV || env.TYPE === ENV.TESTING ? 48 : 42 // FOR DDF 42
const WALLET_REGEX =
  env.TYPE === ENV.DEV || env.TYPE === ENV.TESTING
    ? /^[0-9a-zA-Z]*$/
    : /^0x[0-9a-fA-F]{40}$/ // FOR DDF /^0x[0-9a-fA-F]{40}$/
const DIGITAL_ASSET = "USDT"

export {
  REFUND_AMOUNT_LIMIT,
  WADZPAY_LOGO,
  DISPLAY_ATM_MENU,
  ATM_BANK_NAME,
  WALLET_MAX_LENGTH,
  WALLET_MIN_LENGTH,
  WALLET_REGEX,
  DIGITAL_ASSET
}

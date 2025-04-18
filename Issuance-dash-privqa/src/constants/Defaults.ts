import env from "src/env.template"

import { ENV } from "../env"

const REFUND_AMOUNT_LIMIT = 5
const ATM_BANK_NAME = "MBSB"
const DIGITAL_ASSET = "SART"

const IS_INTEGER_REGEX =
  env.TYPE === ENV.DEV || env.TYPE === ENV.TESTING
    ? /^\d+(\.\d{0,2})?$/
    : /^\d+(\.\d{0,2})?$/

export { REFUND_AMOUNT_LIMIT, ATM_BANK_NAME, DIGITAL_ASSET, IS_INTEGER_REGEX }

/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { SendPhoneOTPData, VerifyPhoneOTPData } from "./models"
import { EndpointKey, useApi } from "./constants"

import { useSet } from "./index"

export const useSendPhoneOTP = () =>
  useSet<SendPhoneOTPData>(
    EndpointKey.SEND_PHONE_OTP,
    useApi().sendPhoneOTP(),
    "POST",
    {},
    EndpointKey.SEND_PHONE_OTP
  )

export const useVerifyPhoneOTP = () =>
  useSet<VerifyPhoneOTPData>(
    EndpointKey.VERIFY_PHONE_OTP,
    useApi().verifyPhoneOTP(),
    "POST",
    {},
    EndpointKey.VERIFY_PHONE_OTP
  )

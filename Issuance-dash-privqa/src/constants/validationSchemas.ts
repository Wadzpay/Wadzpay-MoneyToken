import * as yup from "yup"
import { useTranslation } from "react-i18next"
import { TFunction } from "i18next"

import { Asset } from "./types"
const phoneRegex = /\+[0-9]{7,15}$/
const emptyPhoneRegex = /^(?:\+[0-9]{7,15})?$/

const amountRegex = /^(?!0\d|$)\d*(\.\d{0,15})?$/

const validPassword = (t: TFunction) =>
  yup
    .string()
    .required(t("Please enter your password"))
    .matches(
      /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#&()–[{}\]:;',?/*~$^+=<>])[A-Za-z\d!@#&()–[{}\]:;',?/*~$^+=<>]{8,128}$/,
      t("Password regex")
    )
    .notOneOf(
      [yup.ref("email"), null],
      t("Password and email must be different")
    )

const confirmPassword = (t: TFunction) =>
  yup
    .string()
    .required(t("Please confirm your password"))
    .oneOf(
      [yup.ref("newPassword"), null],
      t("Passwords do not match. Please re-enter your new password.")
    )

type ValidationSchemaVariant =
  | "signInSchema"
  | "createAccountSchema"
  | "accountDetailsSchema"
  | "merchantSchema"
  | "verifyCodeSchema"
  | "resetPasswordSchema"
  | "submitResetPasswordSchema"
  | "depositSchema"
  | "inviteUserSchema"
  | "p2pSchema"
  | "requestTransactionSchema"

export const useValidationSchemas: () => {
  [key in ValidationSchemaVariant]: yup.AnyObjectSchema
} = () => {
  const { t } = useTranslation()

  return {
    createAccountSchema: yup.object().shape({
      country: yup.string().required(t("Please select a country")),
      phoneNumber: yup
        .string()
        .required(t("Please enter your phone number"))
        .matches(phoneRegex, t("Please enter a valid phone number"))
    }),
    signInSchema: yup.object().shape({
      email: yup
        .string()
        .required(t("Please enter your email"))
        .email(t("Please enter a valid email address")),
      password: yup.string().required(t("Please enter your password"))
    }),
    accountDetailsSchema: yup.object().shape({
      email: yup
        .string()
        .required(t("Please enter your email"))
        .email(t("Please enter a valid email address")),
      newPassword: validPassword(t),
      confirmPassword: confirmPassword(t)
    }),
    verifyCodeSchema: yup.object().shape({
      code: yup
        .string()
        .required(t("Please enter verification code"))
        .matches(/^[0-9]{6}$/, t("The code must be 6 digits")),
      newPassword: validPassword(t),
      confirmPassword: confirmPassword(t)
    }),
    merchantSchema: yup.object().shape({
      name: yup.string().required(t("Please enter the merchant name")),
      countryOfRegistration: yup
        .string()
        .required(t("Please enter the Country of Registration")),
      registrationCode: yup
        .string()
        .required(t("Please enter the Registration Code")),
      primaryContactFullName: yup
        .string()
        .required(t("Please enter the Primary Contact Full Name")),
      primaryContactEmail: yup
        .string()
        .required(t("Please enter the Primary Contact Email"))
        .email(t("Please enter a valid email address")),
      primaryContactPhoneNumber: yup
        .string()
        .required(t("Please enter the Primary Contact Phone Number"))
        .matches(phoneRegex, t("Please enter a valid phone number")),
      companyType: yup.string().required(t("Please enter the Company Type")),
      industryType: yup
        .string()
        .required(t("Please please select an Industry Type")),
      merchantId: yup.string().required(t("Please enter the Merchant Id"))
    }),
    resetPasswordSchema: yup.object().shape({
      email: yup
        .string()
        .required(t("Please enter your email"))
        .email(t("Please enter a valid email address"))
      // newPassword: validPassword(t),
      // confirmPassword: confirmPassword(t)
    }),
    submitResetPasswordSchema: yup.object().shape({
      code: yup
        .string()
        .required(t("Please enter verification code"))
        .matches(/^[0-9]{6}$/, t("The code must be 6 digits")),
      newPassword: validPassword(t),
      confirmPassword: confirmPassword(t)
    }),
    depositSchema: yup.object().shape({
      asset: yup.mixed<Asset>(),
      amount: yup
        .string()
        .test(
          "amount",
          t("Amount must be greater than 0"),
          (value) => Number(value) > 0
        )
    }),
    inviteUserSchema: yup.object().shape({
      email: yup
        .string()
        .required(t("Please enter a valid email address"))
        .email(t("Please enter a valid email address"))
    }),
    requestTransactionSchema: yup.object().shape({
      email: yup
        .string()
        .required(t("Please enter a valid email address"))
        .email(t("Please enter a valid email address")),
      phoneNumber: yup
        .string()
        .required(t("Please enter a valid Phone Number"))
        .email(t("Please enter a valid Phone Number"))
    }),
    p2pSchema: yup.object().shape({
      receiverEmail: yup
        .string()
        .when("sendBy", {
          is: "email",
          then: yup.string().required(t("Please enter a valid email address"))
        })
        .email(t("Invalid email address")),
      receiverPhone: yup
        .string()
        .when(["sendBy"], {
          is: "phone",
          then: yup
            .string()
            .required(t("Please enter a valid email phone number"))
        })
        .matches(emptyPhoneRegex, t("Invalid phone number")),
      receiverUsername: yup.string().when(["sendBy"], {
        is: "username",
        then: yup.string().required(t("Please enter a username"))
      }),
      asset: yup.string().required(t("Please select a token")),
      amount: yup
        .string()
        .matches(amountRegex, t("Amount must be a number"))
        .test(
          "amount",
          t("Amount must be greater than 0"),
          (value) => Number(value) > 0
        ),
      description: yup.string()
    })
  }
}

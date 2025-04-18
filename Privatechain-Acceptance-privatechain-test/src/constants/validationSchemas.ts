import * as yup from "yup"
import { useTranslation } from "react-i18next"
import { TFunction } from "i18next"

import { Asset } from "./types"
export const phoneRegex = /\+[0-9]{7,15}$/
export const mobileRegex=/^(\d{10}|\d{12}|\d{11})$/
export const bankAccountRegEx=/^[a-zA-Z0-9]*$/


const emptyPhoneRegex = /^(?:\+[0-9]{7,15})?$/

const amountRegex = /^(?!0\d|$)\d*(\.\d{0,15})?$/
export const  patternIp = /(?:^(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(?:\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)){3}$)|(?:^(?:(?:[a-fA-F\d]{1,4}:){7}(?:[a-fA-F\d]{1,4}|:)|(?:[a-fA-F\d]{1,4}:){6}(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(?:\\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)){3}|:[a-fA-F\d]{1,4}|:)|(?:[a-fA-F\d]{1,4}:){5}(?::(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(?:\\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)){3}|(?::[a-fA-F\d]{1,4}){1,2}|:)|(?:[a-fA-F\d]{1,4}:){4}(?:(?::[a-fA-F\d]{1,4}){0,1}:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(?:\\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)){3}|(?::[a-fA-F\d]{1,4}){1,3}|:)|(?:[a-fA-F\d]{1,4}:){3}(?:(?::[a-fA-F\d]{1,4}){0,2}:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(?:\\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)){3}|(?::[a-fA-F\d]{1,4}){1,4}|:)|(?:[a-fA-F\d]{1,4}:){2}(?:(?::[a-fA-F\d]{1,4}){0,3}:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(?:\\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)){3}|(?::[a-fA-F\d]{1,4}){1,5}|:)|(?:[a-fA-F\d]{1,4}:){1}(?:(?::[a-fA-F\d]{1,4}){0,4}:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(?:\\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)){3}|(?::[a-fA-F\d]{1,4}){1,6}|:)|(?::(?:(?::[a-fA-F\d]{1,4}){0,5}:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(?:\\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)){3}|(?::[a-fA-F\d]{1,4}){1,7}|:)))(?:%[0-9a-zA-Z]{1,})?$)/gm;      
      
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
    .oneOf([yup.ref("newPassword"), null], t("Passwords must match"))

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

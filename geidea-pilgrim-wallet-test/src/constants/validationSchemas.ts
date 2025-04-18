import * as yup from "yup"
import { useTranslation } from "react-i18next"
import { TFunction } from "i18next"

import {
  Asset,
  TransactionDirection,
  TransactionStatus,
  TransactionType
} from "./types"

import { isConsideredPhoneNumber } from "~/helpers"

const phoneRegex = /\+[0-9]{7,15}$/

const validStringWithoutWhitespaces = (t: TFunction) =>
  yup
    .string()
    .required(t("Please enter a nickname"))
    .trim("Please enter valid nickname")
    

const validPassword = (t: TFunction) =>
  yup
    .string()
    .required(t("Please enter your password"))
    .matches(
      /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#&()–[{}\]:;',?/*~$^+=<>])[A-Za-z\d!@#&()–[{}\]:;',?/*~$^+=<>]{8,128}$/,
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
  | "changePasswordSchema"
  | "resetPasswordSchema"
  | "submitResetPasswordSchema"
  | "createAccountSchema"
  | "verifyCodeSchema"
  | "accountDetailsSchema"
  | "personalDetailsSchema"
  | "depositSchema"
  | "transactionFiltersSchema"
  | "saveContactSchema"
  | "refundSchema"

export const useValidationSchemas: () => {
  [key in ValidationSchemaVariant]: yup.AnyObjectSchema
} = () => {
  const { t } = useTranslation()

  let EMAIL_REGX = /^(([^<>()\[\]\\.,;:\s@"]+(.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@(([[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}])|(([a-zA-Z-0-9]+.)+[a-zA-Z]{2,}))$/

    
  // let emailSchema = yup.string()
  // .matches(EMAIL_REGX, "Invalid email address");

  return {
    signInSchema: yup.object().shape({
      email: yup
        .string()
        .required("Please enter your email")
        .email("Please enter a valid email address")
        .matches(EMAIL_REGX, "Please enter a valid email address"),
      password: yup.string().required("Please enter your password")
    }),
    changePasswordSchema: yup.object().shape({
      currentPassword: yup.string().required(t("Please enter your password")),
      newPassword: validPassword(t),
      confirmPassword: confirmPassword(t)
    }),
    resetPasswordSchema: yup.object().shape({
      email: yup
        .string()
        .required(t("Please enter your email"))
        .email(t("Please enter a valid email address")),
      newPassword: validPassword(t),
      confirmPassword: confirmPassword(t)
    }),
    submitResetPasswordSchema: yup.object().shape({
      code: yup
        .string()
        .required(t("Please enter verification code"))
        .matches(/^[0-9]{6}$/, t("The code must be 6 digits"))
    }),
    createAccountSchema: yup.object().shape({
      country: yup.string().required(t("Please select a country")),
      phoneNumber: yup
        .string()
        .required(t("Please enter your phone number"))
        .matches(/^\+[0-9]{7,12}$/im, t("Please enter a valid phone number")),
      isNewsletter: yup.bool(),
      isAgreement: yup
        .bool()
        .oneOf(
          [true],
          t("Please accept the Terms of Service and Privacy Policy")
        )
    }),
    verifyCodeSchema: yup.object().shape({
      phoneNumberCode: yup
        .string()
        .required(t("Please enter verification code"))
        .matches(/^[0-9]{6}$/, t("The code must be 6 digits"))
    }),
    accountDetailsSchema: yup.object().shape({
      email: yup
        .string()
        .required(t("Please enter your email"))
        .email(t("Please enter a valid email address"))
        .max(64, "Email Address should be of max 64 characters"),
      newPassword: validPassword(t),
      confirmPassword: confirmPassword(t)
    }),
    personalDetailsSchema: yup.object().shape({
      // TODO validation schema once the field details for KYC are clear
      firstName: yup.string(),
      lastName: yup.string(),
      dateOfBirth: yup.string(),
      profession: yup.string(),
      sourceOfFund: yup.string()
    }),
    accountNumberSchema: yup.object().shape({
      // TODO validation schema once the field details for KYC are clear
      ibanNumber: yup.string(),
      confirmIbanNumber: yup.string()
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
    withdrawDepositSchema: yup.object().shape({
      fiatAmount: yup
        .string()
        .required(t("Please enter amount"))
        .email(t("Please enter amount")),
    }),
    buySchema: yup.object().shape({
      amount: yup
      .string()
      .required(t("Please enter amount"))
      .test("amount", t("Check the amount."), (value) =>
      value !== undefined && value !== "" ? Number(value) >= 0 : true
    )
    }),
    transactionFiltersSchema: yup.object().shape({
      dateFrom: yup.date(),
      // .when(
      //   "dateTo",
      //   (dateTo: Date, schema: yup.DateSchema) =>
      //     dateTo && schema.max(dateTo, t("Check the dates."))
      // ),
      dateTo: yup.date(),
      // .min(yup.ref("dateFrom"), "Date To must be later \nthan Date From"),
      amountFrom: yup
        .string()
        .test("amountFrom", t("Check the amount."), (value) =>
          value !== undefined && value !== "" ? Number(value) >= 0 : true
        ),
      amountTo: yup
        .string()
        .test("amountTo", t("Check the amount."), (value) =>
          value !== undefined && value !== "" ? Number(value) > 0 : true
        ),
      direction: yup.mixed<TransactionDirection>(),
      type: yup.array().of(yup.mixed<TransactionType>()),
      status: yup.array().of(yup.mixed<TransactionStatus>()),
      asset: yup.array().of(yup.mixed<Asset>())
    }),
    refundSchema: yup.object().shape({
      isAccepted: yup
        .bool()
        .oneOf(
          [true],
          t("Please accept the Terms of Service and Privacy Policy")
        )
    }),

    saveContactSchema: yup.object().shape({
      userId: yup.string().uuid() || "",
      nickname: validStringWithoutWhitespaces(t), //yup.string().required(t("Please enter a nickname")),
      // Validate search field only when we don't have a valid userId
      search: yup
        .string()
        .required(t("Please enter email or phone number"))
        .when("userId", {
          is: (userId: string) => yup.string().uuid().isValid(userId),
          then: yup.string(),
          otherwise: yup.lazy((value: string) =>
            isConsideredPhoneNumber(value)
              ? yup
                  .string()
                  .matches(
                    phoneRegex,
                    t("Please provide a valid email or phone number")
                  )
              : yup
                  .string()
                  .email(t("Please provide a valid email or phone number"))
          )
        })
    })
  }
}

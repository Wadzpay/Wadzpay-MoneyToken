import { TextFieldProps } from "~/components/ui/TextField"

export const fieldProps: {
  [key in
    | "username"
    | "password"
    | "newPassword"
    | "phoneNumber"
    | "verificationCode"
    | "firstName"
    | "lastName"
    | "nickname"
    | "email"
    | "amount"]: Partial<TextFieldProps>
} = {
  username: {
    autoCapitalize: "none",
    autoCompleteType: "username",
    textContentType: "username",
    keyboardType: "email-address",
    autoCorrect: false
  },
  password: {
    textContentType: "password",
    autoCompleteType: "password",
    secureTextEntry: true
  },
  newPassword: {
    autoCompleteType: "password",
    textContentType: "newPassword",
    secureTextEntry: true
  },
  phoneNumber: {
    keyboardType: "phone-pad",
    textContentType: "telephoneNumber",
    autoCompleteType: "tel",
    returnKeyType: "done"
  },
  verificationCode: {
    keyboardType: "numeric",
    textContentType: "oneTimeCode",
    returnKeyType: "done"
  },
  firstName: {
    autoCapitalize: "words",
    autoCompleteType: "name",
    textContentType: "givenName"
  },
  lastName: {
    autoCapitalize: "words",
    autoCompleteType: "name",
    textContentType: "familyName"
  },
  nickname: {
    autoCapitalize: "words",
    textContentType: "nickname"
  },
  email: {
    autoCompleteType: "email",
    textContentType: "emailAddress",
    keyboardType: "email-address",
    autoCapitalize: "none",
    autoCorrect: false
  },
  amount: {
    returnKeyType: "done",
    keyboardType: "decimal-pad",
    selectTextOnFocus: true
  }
}

import { Auth } from "aws-amplify"
import { SignInData } from "src/api/models"
import { verifyToken } from "src/api/user"
import env from "src/env"

Auth.configure(env.COGNITO_CONFIG)

export type IdToken = string | null
export type User =
  | {
      attributes: {
        email: string
        phone_number: string
        sub: string // same as username (cognito username - uuid)
      }
      pool?:{
        clientId:string
      }
      storage?:any
      username: string // same as sub (cognito username - uuid)
      error?:String
    }
  | null
  | undefined

const signInAsync: (signInData: SignInData) => Promise<void> = async ({
  email,
  password
}) => {
  if(sessionStorage.getItem('currentUser')==null||sessionStorage.getItem('currentUser')?.toLowerCase()==email.toLowerCase() ){
  return await Auth.signIn(email.toLowerCase(), password)}
}

const signOutAsync: () => void = async () => {
  await Auth.signOut()
}

const changePasswordAsync: (body: {
  currentPassword: string
  newPassword: string
}) => Promise<"SUCCESS"> = async ({ currentPassword, newPassword }) => {
  const user = await Auth.currentAuthenticatedUser()
  return Auth.changePassword(user, currentPassword, newPassword)
}

const requestResetPasswordCodeAsync: (body: {
  email: string
}) => Promise<void> = async ({ email }) => {
  await Auth.forgotPassword(email)
}

const submitResetPasswordCodeAsync: (body: {
  email: string
  code: string
  newPassword: string
}) => Promise<string> = ({ email, code, newPassword }) => {
  return Auth.forgotPasswordSubmit(email, code, newPassword)
}

const getCurrentUserAsync: () => Promise<User> = async () => {
  try {
    return await Auth.currentAuthenticatedUser()
  } catch {
    return null
  }
}

const getIdTokenAsync: () => Promise<IdToken> = async () => {
  try {
    return (await Auth.currentSession()).getIdToken().getJwtToken()
  } catch {
    return null
  }
}

export {
  signInAsync,
  signOutAsync,
  changePasswordAsync,
  requestResetPasswordCodeAsync,
  submitResetPasswordCodeAsync,
  getCurrentUserAsync,
  getIdTokenAsync
}

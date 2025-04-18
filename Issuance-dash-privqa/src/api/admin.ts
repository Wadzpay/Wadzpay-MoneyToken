import { EndpointKey, useApi } from "./constants"
import {
  InviteUserData,
  GenerateAPIKey,
  EnableDisableUserData,
  MerchantUserData
} from "./models"

import { useGet, useSet } from "."

export const useInviteUser = () =>
  useSet<InviteUserData>(
    EndpointKey.INVITE_USER,
    useApi().inviteUser(),
    "POST",
    {},
    EndpointKey.INVITE_USER
  )

export const useGenerateAPIKey = () =>
  useSet<GenerateAPIKey>(
    EndpointKey.GENERATE_API_KEY,
    useApi().generateAPIKey(),
    "POST",
    {},
    EndpointKey.GENERATE_API_KEY
  )

export const useUserList = () =>
  useGet<MerchantUserData[]>(
    EndpointKey.USER_LIST,
    useApi().userList(),
    {},
    EndpointKey.USER_LIST
  )

export const useEnableUser = () =>
  useSet<EnableDisableUserData>(
    EndpointKey.ENABLE_USER,
    useApi().enableUser(),
    "POST",
    {},
    EndpointKey.ENABLE_USER
  )

export const useDisableUser = () =>
  useSet<EnableDisableUserData>(
    EndpointKey.DISABLE_USER,
    useApi().disableUser(),
    "POST",
    {},
    EndpointKey.DISABLE_USER
  )

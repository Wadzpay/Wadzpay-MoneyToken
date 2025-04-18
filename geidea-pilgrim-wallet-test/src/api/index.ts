/* eslint-disable no-console */
import { useContext } from "react"
import { useTranslation } from "react-i18next"
import {
  QueryKey,
  useMutation,
  UseMutationOptions,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryOptions,
  UseQueryResult
} from "react-query"
import { Alert } from "react-native"

import { EndpointKey } from "./constants"
import { useErrorMessages } from "./errorMessages"
import { ErrorType } from "./errorTypes"

import env, { LOCAL_ENV_PUBLIC_API_URL } from "~/env"
import { getIdTokenAsync } from "~/auth/AuthManager"
import { EnvironmentContext } from "~/context"
import { isDev } from "~/utils"

type IdFieldRecord = { [idField: string]: string } & Record<string, unknown>
type IdRecord = { id: string } & Record<string, unknown>

const transformObjectFieldToId: (
  idField: string
) => (source: IdFieldRecord) => IdRecord = (idField) => (source) => ({
  ...source,
  id: source[idField]
})

export const usePublicApiUrl: () => string = () => {
  const { isLocal } = useContext(EnvironmentContext)
  // return isDev && isLocal ? LOCAL_ENV_PUBLIC_API_URL : env.PUBLIC_API_URL
  return env.PUBLIC_API_URL
}

export const useHeaders: (headers?: {
  [key: string]: string
}) => () => Promise<{ [key: string]: string }> =
  (headers = {}) =>
  async () => ({
    Authorization: `Bearer ${await getIdTokenAsync()}`,
    ...headers
  })

export function useGet<T>(
  key: QueryKey,
  endpoint: string,
  options: UseQueryOptions<T, Error, T> = {},
  idFieldName?: string
): UseQueryResult<T, Error> {
  const getHeaders = useHeaders()
  const { t } = useTranslation()
  const errorMessages = useErrorMessages()
  const PUBLIC_API_URL = usePublicApiUrl()

  // console.log("options ", options, "endpoint", endpoint)
  return useQuery<T, Error>(
    key,
    async () => {
      const headers = await getHeaders()
      try {
        const response = await fetch(PUBLIC_API_URL + endpoint, {
          method: "GET",
          headers: headers
        })
        if (!response.ok) {
          const payload = await response.json()
          // If the key is a combination of a constant and query string,
          // we need to extract the error key
          if (isDev) {
            console.log("GET")
            console.log(headers)
            console.log(PUBLIC_API_URL + endpoint)
            console.log("Request:: ")
            console.log(key)
            console.log("Response:: ")
            console.log(payload)
          }
          const errorKey = Array.isArray(key) ? key[0] : key
         

          throw new Error(
            errorMessages[errorKey as EndpointKey][payload.message as ErrorType]
          )
        }

        const result = (await response.json()) as T
        console.log("result",PUBLIC_API_URL ,  endpoint ,result)
        if (isDev) {
          console.log("GET")
          console.log(headers)
          console.log(PUBLIC_API_URL + endpoint)
          console.log("Request:: ")
          console.log(key)
          console.log("Response:: ")
          console.log(result)
        }
        const transformedResult = idFieldName
          ? Array.isArray(result)
            ? result.map(transformObjectFieldToId(idFieldName))
            : transformObjectFieldToId(idFieldName)(result as IdFieldRecord)
          : result
        return transformedResult as T
      } catch (e) {
        console.error("Error useGet crashed", e, PUBLIC_API_URL + endpoint)
        console.error("Headers", headers)

        throw new Error(
          e?.message || t("Something went wrong, network request has failed.")
        )
      }
    },
    {
      cacheTime: 0,
      retry: false,
      ...options
    }
  )
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type ResponseType = string | { [key: string]: any }

export function useSet<T>(
  key: QueryKey,
  endpoint: string,
  method: "POST" | "PUT" | "PATCH" | "DELETE",
  options: UseMutationOptions<ResponseType, Error, T> = {},
  invalidationKeys?: string | string[]
): UseMutationResult<ResponseType, Error, T> {
  const getHeaders = useHeaders({ "Content-Type": "application/json" })
  const client = useQueryClient()
  const { t } = useTranslation()
  const errorMessages = useErrorMessages()
  const PUBLIC_API_URL = usePublicApiUrl()

  return useMutation(
    key,
    async (data: T) => {
      const headers = await getHeaders()
      try {
        const response = await fetch(PUBLIC_API_URL + endpoint, {
          method,
          headers: headers,
          body: data ? JSON.stringify(data) : null
        })

        console.log("request url", PUBLIC_API_URL + endpoint, {
          method,
          headers: headers,
          body: data ? JSON.stringify(data) : null
        }, "response" ,response)
        if (!response.ok) {
          const payload = await response.json()
          // isDev 
          if (true) {
            console.log(method)
            console.log(headers)
            console.log(PUBLIC_API_URL + endpoint)
            console.log("Request:: ")
            console.log(key)
            console.log("Response:: ")
            console.log(payload)
          }
          console.log("hereherehereherehere")

          /**
           * commenting this code as error messages are not shown up properly 
           */
          // if(payload.status != 200){
          //   Alert.alert('An Error Occured','Please try again later')
          //   return;
          // }

          /**
           * commenting this code as error messages are not shown up properly 
           */
          
          throw new Error(
            errorMessages[key as EndpointKey][payload.message as ErrorType]
          )
        }

        const text = await response.text()
        if (isDev) {
          console.log(method)
          console.log(headers)
          console.log(PUBLIC_API_URL + endpoint)
          console.log("Request:: ")
          console.log(key)
          console.log("Response:: ")
          console.log(text)
        }
        return text.length && ["{", "["].includes(text[0])
          ? JSON.parse(text)
          : text
      } catch (e) {
        console.log("Error useSet crashed", e, PUBLIC_API_URL + endpoint, data)
        console.log("Headers", headers)

        throw new Error(
          e?.message || t("Something went wrong, network request has failed.")
        )
      }
    },
    {
      onSuccess: () => {
        if (invalidationKeys && Array.isArray(invalidationKeys)) {
          invalidationKeys.forEach((_key) => client.invalidateQueries(_key))
        } else {
          client.invalidateQueries(key)
        }
      },
      ...options
    }
  )
}

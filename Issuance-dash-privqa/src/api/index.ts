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
import env from "src/env"
import { getIdTokenAsync } from "src/auth/AuthManager"
import { isValidHost } from "src/utils"

import { EndpointKey } from "./constants"
import { useErrorMessages } from "./errorMessages"
import { ErrorType } from "./errorTypes"

type IdFieldRecord = { [idField: string]: string } & Record<string, unknown>
type IdRecord = { id: string } & Record<string, unknown>

const transformObjectFieldToId: (
  idField: string
) => (source: IdFieldRecord) => IdRecord = (idField) => (source) => ({
  ...source,
  id: source[idField]
})

export const usePublicApiUrl: () => string = () => {
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
          const errorKey = Array.isArray(key) ? key[0] : key
          throw new Error(
            errorMessages[errorKey as EndpointKey][payload.message as ErrorType]
          )
        }

        const result = (await response.json()) as T
        const transformedResult = idFieldName
          ? Array.isArray(result)
            ? result.map(transformObjectFieldToId(idFieldName))
            : transformObjectFieldToId(idFieldName)(result as IdFieldRecord)
          : result
        return transformedResult as T
      } catch (e) {
        console.error("Error useGet crashed", e, PUBLIC_API_URL + endpoint)
        console.error("Headers", headers)
        const message =
          e instanceof Error && e.message
            ? e.message
            : t("Something went wrong, network request has failed.")
        throw new Error(message)
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
        if (isValidHost(PUBLIC_API_URL)) {
          const response = await fetch(PUBLIC_API_URL + endpoint, {
            method,
            headers: headers,
            body: data ? JSON.stringify(data) : null
          })

          const text = await response.text()

          if (text.length && ["{", "["].includes(text[0])) {
            const data = JSON.parse(text)
            if (
              data.status === 422 &&
              data.message === "LANGUAGE_MAPPED_WITH_INSTITUTIONS"
            ) {
              throw new Error(text)
            }
          }

          if (!response.ok) {
            const payload = await response.json()
            throw new Error(
              errorMessages[key as EndpointKey][payload.message as ErrorType]
            )
          }

          return text.length && ["{", "["].includes(text[0])
            ? JSON.parse(text)
            : text
        } else {
          const message = t("Access denied ")
          throw new Error(message)
        }
      } catch (e) {
        console.error(
          "Error useSet crashed",
          e,
          PUBLIC_API_URL + endpoint,
          data
        )
        console.error("Headers", headers)
        const message =
          e instanceof Error && e.message
            ? e.message
            : t("Something went wrong, network request has failed.")
        throw new Error(message)
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

export const TIMER_MINUTES = 1

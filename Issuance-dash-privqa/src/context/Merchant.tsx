import React, { PropsWithChildren, useEffect, useState } from "react"
import { Navigate, useLocation } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { useDispatch, useSelector } from "react-redux"
import { IssuanceDetailsData } from "src/api/models"
import { ROOTSTATE } from "src/utils/modules"
import { useIssuanceDetails } from "src/api/user"
import { RouteType } from "src/constants/routeTypes"
import SignedInUser from "src/auth/SignedInUser"
import SignedOutUser from "src/auth/SignedOutUser"
import { setSettingsConfig } from "src/utils/settingsSlice"
import env from "src/env"
import { getIdTokenAsync } from "src/auth/AuthManager"
type IssuanceContextType = {
  issuanceDetails?: IssuanceDetailsData
  isFetching: boolean
  institutionDetails?: any
}

export const IssuanceContext = React.createContext<IssuanceContextType>({
  issuanceDetails: undefined,
  isFetching: true
})

type Props = PropsWithChildren<{}>

export const SignedInIssuanceContextProvider: React.FC<Props> = ({
  children
}: Props) => {
  const dispatch = useDispatch()
  const { t } = useTranslation()
  const { pathname } = useLocation()
  const { data, isFetching, error, refetch } = useIssuanceDetails()

  const [IssuanceDetailsData, setIssuanceData] = useState<IssuanceDetailsData>()
  const [instDetails, setInstDetails] = useState({
    institutionName: "",
    institutionFullName: "",
    institutionLogo: "",
    issuingCurrency: "",
    acquiringCurrency: ""
  })
  // get settings configuration
  const settingsConfig = useSelector((store: ROOTSTATE) => store.settingsConfig)
  console.log("dashboard", data)

  const fetchInstDetails = async (name: any) => {
    console.log("fetchInstDetails", name)
    const response = await fetch(
      `${env.PUBLIC_API_URL}issuance/getDetails?institutionName=${name}&lang=English`,
      {
        method: "GET", // Assuming it's a GET request
        headers: {
          Authorization: `Bearer ${await getIdTokenAsync()}`, // Use the token from localStorage
          "Content-Type": "application/json" // Optional, based on your API needs
        }
      }
    )

    if (!response.ok) {
      throw new Error("Failed to fetch merchant details")
    }
    return response.json()
  }
  const getInstDetails = async (name: any) => {
    try {
      const data = await fetchInstDetails(name)
      console.log("Merchant Details:", data)
      setInstDetails(data)
      // You can also update state here if you want to store the result in the component's state
    } catch (error) {
      console.error("Error fetching merchant details:", error)
    }
  }

  useEffect(() => {
    if (data) {
      if (data?.defaultCurrency === "null" || data?.defaultCurrency === null) {
        setIssuanceData({ ...data, defaultCurrency: "SART" })
        getInstDetails(data?.bankName)
        // Dispatch action to update settingsConfiguration
        dispatch(
          setSettingsConfig({
            ...settingsConfig,
            p2pTransfer: data && data?.p2pTransfer === false ? false : true
          })
        )
      } else {
        setIssuanceData(data)
        getInstDetails(data?.bankName)
        // Dispatch action to update settingsConfiguration
        dispatch(
          setSettingsConfig({
            ...settingsConfig,
            p2pTransfer: data && data?.p2pTransfer === false ? false : true
          })
        )
      }
    }
  }, [data])

  useEffect(() => {
    if (!data) {
      refetch()
    }
  }, [pathname])

  const getAction = () => {
    // Display a blank screen while fetching the issuanceData details
    if (isFetching) {
      return <></>
    }

    // Display the error message if not ISSUANCE_BANK_NOT_FOUND
    if (error && error.message !== "ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND") {
      return (
        <div className="alert alert-danger" role="alert">
          <div>{error.message}</div>
          <div>{t("Unable to retrieve issuance details from the server")}</div>
        </div>
      )
    }

    // Send to home page if issuance details exist and requesting the issuance details page
    if (IssuanceDetailsData && pathname === RouteType.ISSUANCE_DETAILS) {
      return <Navigate to={RouteType.HOME} />
    }

    // Send to issuance details page if issuance is not found
    if (
      pathname !== RouteType.ISSUANCE_DETAILS &&
      error &&
      error.message === "ERROR_MESSAGE.ISSUANCE_BANK_NOT_FOUND"
    ) {
      return <Navigate to={RouteType.ISSUANCE_DETAILS} />
    }

    // Otherwise, display the content
    return (
      <IssuanceContext.Provider
        value={{
          issuanceDetails: IssuanceDetailsData,
          institutionDetails: instDetails,
          isFetching
        }}
      >
        {children}
      </IssuanceContext.Provider>
    )
  }

  return getAction()
}

/**
 * This will load the issuance data if the current user is logged in
 * otherwise it just renders the children
 */
export const IssuanceContextProvider: React.FC<Props> = ({
  children
}: Props) => {
  return (
    <>
      <SignedInUser>
        <SignedInIssuanceContextProvider>
          {children}
        </SignedInIssuanceContextProvider>
      </SignedInUser>
      <SignedOutUser>{children}</SignedOutUser>
    </>
  )
}

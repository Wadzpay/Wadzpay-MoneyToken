import React from "react"

import DefaultCurrency from "../defaultCurrency/DefaultCurrency"
import DefaultTimezone from "../defaultTimezone/DefaultTimezone"
import WalletsCountPerPage from "../walletsCountPerPage/WalletsCountPerPage"
import { useUpdateIssuanceDetails } from "../../../api/user"

const DefaultPreferences: React.FC = () => {
  const {
    mutate: updateIssuanceDetails,
    error,
    isSuccess
  } = useUpdateIssuanceDetails()

  const updateIssuanceData = (data: any) => {
    updateIssuanceDetails(data)
  }

  return (
    <>
      <div className="">
        <p>
          <b>Default Preferences</b>
        </p>
      </div>
      <DefaultCurrency
        updateIssuanceDetails={(data?: object) =>
          updateIssuanceData(data || {})
        }
      />
      <DefaultTimezone
        updateIssuanceDetails={(data?: object) =>
          updateIssuanceData(data || {})
        }
      />
      <WalletsCountPerPage />
    </>
  )
}

export default DefaultPreferences

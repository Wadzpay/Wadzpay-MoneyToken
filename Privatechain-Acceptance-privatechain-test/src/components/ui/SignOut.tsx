import { t } from "i18next"
import React, { useContext } from "react"
import dayjs from "dayjs"
import utc from "dayjs/plugin/utc"
import tz from "dayjs/plugin/timezone"
import { signOutAsync } from "src/auth/AuthManager"
import SignedInUser from "src/auth/SignedInUser"
import { UserContext } from "src/context/User"
import { MerchantContext } from "src/context/Merchant"

const SignOut: React.FC = () => {
  // const { user } = useContext(UserContext)
  const { merchantDetails } = useContext(MerchantContext)
  dayjs.extend(utc)
  dayjs.extend(tz)

  const dateFormat = () => {
    const localStorageTime = localStorage.getItem("TimeZone")
    if (localStorageTime) {
      const timezone = JSON.parse(localStorageTime).value || "Asia/Dubai"
      const label = JSON.parse(localStorageTime).label
      const str = label.slice(11)
      return (
        dayjs(Date()).tz(timezone).format("MMMM D, YYYY h:mm A") +
        " " +
        `(${str})`
      )
    } else {
      return (
        dayjs(Date()).tz("Asia/Dubai").format("MMMM D, YYYY h:mm A") +
        " " +
        `(${"Abu Dhabi, Muscat"})`
      )
    }
  }

  return (
    <SignedInUser>
      <div className="d-flex justify-content-end me-1 text-nowrap align-items-center">
        {merchantDetails && (
          <div className="mx-3 d-none d-lg-block" data-testid="merchantName">
            {merchantDetails?.merchant?.name}
          </div>
        )}
        <button
          className="btn btn-dark border-secondary"
          onClick={() => signOutAsync()}
        >
          {t("Sign Out")}
        </button>
      </div>
      <div className="mt-1" style={{ textAlign: "right" }}>
        <span>{dateFormat()}</span>
      </div>
    </SignedInUser>
  )
}

export default SignOut

import React, { useMemo, useContext, useState, useEffect } from "react"
import TimezoneSelect, { allTimezones } from "react-timezone-select"
import type { ITimezone } from "react-timezone-select"
import { IssuanceContext } from "src/context/Merchant"

interface Props {
  updateIssuanceDetails: (data?: object) => void
}

const DefaultTimezone: React.FC<Props> = ({ updateIssuanceDetails }: Props) => {
  const { issuanceDetails } = useContext(IssuanceContext)
  // const [tz, setTz] = useState<ITimezone>(
  //   Intl.DateTimeFormat().resolvedOptions().timeZone
  // )
  const [tz, setTz] = useState<ITimezone>({
    value: "Asia/Kolkata",
    label: "(Chennai, Kolkata, Mumbai, New Delhi)",
    offset: 8,
    abbrev: "IST",
    altName: "India Standard Time"
  })

  useEffect(() => {
    setTimeout(() => {
      setTz({
        value: "Asia/Kolkata",
        label: "(Chennai, Kolkata, Mumbai, New Delhi)",
        offset: 8,
        abbrev: "IST",
        altName: "India Standard Time"
      })
    }, 2000)
  }, [])

  // const displayOptions: any = {
  //   dateStyle: "full",
  //   timeStyle: "medium",
  //   hour12: false,
  //   timeZone: typeof tz === "string" ? tz : tz.value
  // }

  // const [datetime, setDatetime] = useState(
  //   new Intl.DateTimeFormat("en-US", displayOptions).format(new Date())
  // )

  // useMemo(() => {
  //   setDatetime(
  //     new Intl.DateTimeFormat("en-US", displayOptions).format(new Date())
  //   )
  // }, [tz])

  const setTime = (date: any) => {
    setTz(date)
    localStorage.setItem("TimeZone", JSON.stringify(date))
    // Update update Issuance Details
    updateIssuanceDetails({
      defaultTimeZone: date.value,
      defaultCurrency: issuanceDetails?.defaultCurrency
    })
    setTimeout(() => {
      window.location.reload()
    })
  }

  return (
    <div className="mt-4">
      <h6>Default Timezone</h6>
      <div className="col-lg-3 col-sm-12">
        <TimezoneSelect
          value={tz}
          // onChange={setTz}
          onChange={(data) => setTime(data)}
          timezones={{
            ...allTimezones,
            "America/Lima": "Pittsburgh",
            "Europe/Berlin": "Frankfurt"
          }}
          components={{
            IndicatorSeparator: () => null
          }}
          // isDisabled={true}
        />
      </div>
    </div>
  )
}

export default DefaultTimezone

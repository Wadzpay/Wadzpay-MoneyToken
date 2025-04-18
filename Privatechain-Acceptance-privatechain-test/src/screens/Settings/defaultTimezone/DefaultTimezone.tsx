import React, { useMemo, useState, useEffect } from "react"
import TimezoneSelect, { allTimezones } from "react-timezone-select"
import type { ITimezone } from "react-timezone-select"
import { string } from "yup/lib/locale"

const DefaultTimezone: React.FC = () => {
  // const [tz, setTz] = useState<ITimezone>(
  //   Intl.DateTimeFormat().resolvedOptions().timeZone
  // )
  const [tz, setTz] = useState<ITimezone>({
    value: "Asia/Dubai",
    label: "(GMT+4:00) Abu Dhabi, Muscat",
    offset: 4,
    abbrev: "GST",
    altName: "Gulf Standard Time"
  })

  useEffect(() => {
    setTimeout(() => {
      const localStorageTime = localStorage.getItem("TimeZone")
      if (localStorageTime) {
        if (typeof JSON.parse(localStorageTime) === "object") {
          setTz(JSON.parse(localStorageTime))
        } else {
          setTz({
            value: "Asia/Dubai",
            label: "(GMT+4:00) Abu Dhabi, Muscat",
            offset: 4,
            abbrev: "GST",
            altName: "Gulf Standard Time"
          })
        }
      }
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
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  }

  return (
    <div className="mt-4">
      <h6>Default Timezone</h6>
      <div className="col-md-3">
        <TimezoneSelect
          value={tz}
          // onChange={setTz}
          onChange={(data) => setTime(data)}
          timezones={{
            "Asia/Riyadh": "Riyadh"
          }}
          components={{
            IndicatorSeparator: () => null
          }}
        />
      </div>
      {/* <div className="output-wrapper">
        <div>
          Current Date / Time in{" "}
          {typeof tz === "string" ? tz.split("/")[1] : tz.value.split("/")[1]}:{" "}
          <pre>{datetime}</pre>
        </div>
        <div>
          <div>Selected Timezone:</div>
          <pre>{JSON.stringify(tz, null, 2)}</pre>
        </div>
      </div> */}
    </div>
  )
}

export default DefaultTimezone
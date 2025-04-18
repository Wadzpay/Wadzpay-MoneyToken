import React, { useEffect, useState } from "react"
import { t } from "i18next"
import { Dropdown } from "react-bootstrap"
import DatePicker from "react-datepicker"
import "react-datepicker/dist/react-datepicker.css"
import { AiFillCloseCircle } from "react-icons/ai"
import dayjs from "dayjs"

type Props = {
  title: string
  dateFrom: Date | null
  dateTo: Date | null
  setFilter: (dateFrom: Date | null, dateTo: Date | null) => void
}

export const FilterDateDropdown: React.FC<Props> = ({
  title,
  dateFrom: initialDateFrom,
  dateTo: initialDateTo,
  setFilter
}: Props) => {
  const [showDropdown, setShowDropdown] = useState(true)
  const [showDatePicker, setShowDatePicker] = useState(false)
  const [dateRange, setDateRange] = useState<(Date | null)[]>([
    initialDateFrom,
    initialDateTo
  ])
  const [dateFrom, dateTo] = dateRange
  const onChange = (date: [Date | null, Date | null]) => {
    setDateRange(date)
  }

  const getDatePeriod = (str: string) => {
    const startDateFrom = new Date()
    let endDateFrom
    switch (str) {
      case "week":
        endDateFrom = new Date(new Date().getTime() - 7 * 24 * 60 * 60 * 1000)
        break
      case "halfMonth":
        endDateFrom = new Date(new Date().getTime() - 15 * 24 * 60 * 60 * 1000)
        break
      case "oneMonth":
        endDateFrom = new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000)
        break
      default:
        break
    }
    if (startDateFrom && endDateFrom) {
      setFilter(endDateFrom, startDateFrom)
    }
  }

  useEffect(() => {
    if (dateRange[0] && dateRange[1]) {
      setFilter(dateRange[0], dateRange[1])
      setShowDropdown(false)
    }
  }, [dateRange])

  return (
    <>
      {!showDropdown && (
        <div>
          <button
            type="button"
            className="me-2 btn btn-secondary"
            onClick={() => {
              setShowDatePicker(true)
              setShowDropdown(true)
            }}
          >
            <span className="pe-2">
              {dateFrom && dateFrom.toLocaleDateString()}
              {" - "}
              {dateTo && dateTo.toLocaleDateString()}
            </span>
            <AiFillCloseCircle
              onClick={(e: React.MouseEvent<Element>) => {
                e.stopPropagation()
                onChange([null, null])
                setFilter(null, null)
              }}
            />
          </button>
        </div>
      )}
      {showDropdown && (
        <Dropdown
          className="me-2"
          onClick={() => setShowDatePicker(true)}
          onToggle={(isOpen: boolean) => {
            if (!isOpen) {
              setShowDropdown(false)
            }
          }}
        >
          <Dropdown.Toggle variant="outline-secondary transaction-filter-dropdown">
            {t(title)}
          </Dropdown.Toggle>
          <Dropdown.Menu
            show={showDatePicker}
            style={{
              backgroundColor: "white",
              width: "max-content"
            }}
          >
            <div className="custom-Datepicker-Periods">
              <Dropdown.Item
                onClick={(e: React.MouseEvent<Element>) =>
                  getDatePeriod("week")
                }
                className="bg-secondary text-white"
              >
                {t("Last Week")}
              </Dropdown.Item>
              <Dropdown.Item
                onClick={(e: React.MouseEvent<Element>) =>
                  getDatePeriod("halfMonth")
                }
                className="bg-secondary text-white"
              >
                {t("Last 15 Days")}
              </Dropdown.Item>
              <Dropdown.Item
                onClick={(e: React.MouseEvent<Element>) =>
                  getDatePeriod("oneMonth")
                }
                className="bg-secondary text-white"
              >
                {t("Last Month")}
              </Dropdown.Item>
            </div>
            <DatePicker
              selected={dateFrom}
              onChange={onChange}
              startDate={dateFrom}
              endDate={dateTo}
              selectsRange
              inline
              monthsShown={2}
            />
          </Dropdown.Menu>
        </Dropdown>
      )}
    </>
  )
}

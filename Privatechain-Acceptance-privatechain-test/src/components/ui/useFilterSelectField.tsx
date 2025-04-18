import React, { useEffect, useState, useContext, useRef } from "react"
import { useLocation, useSearchParams } from "react-router-dom"
import { useTranslation } from "react-i18next"
import "react-datepicker/dist/react-datepicker.css"
import DatePicker from "react-datepicker"
import {
  Asset,
  Status,
  SearchItems,
  TransactionType,
  DebitCredit
} from "src/constants/FilterTransactionConstants"
import { UserContext } from "src/context/User"
import dayjs from "dayjs"
import { Dropdown } from "react-bootstrap"

import { FilterDropdown } from "./FilterDropdown"
import { FilterSearch } from "./FilterSearch"

interface Props {
  submitFilter: (filters: { [key: string]: string }) => void
  filters: { [key: string]: string }
  csvDownload: () => void
  pdfDownload: () => void
}

export const useFilterSelectField = ({
  submitFilter,
  filters,
  csvDownload,
  pdfDownload
}: Props) => {
  const { t } = useTranslation()
  const { user } = useContext(UserContext)
  const txIdInputRef = useRef<HTMLInputElement>(null)
  const extPosTxIDInputRef = useRef<HTMLInputElement>(null)
  const [dateFrom, setDateFrom] = useState<Date | null>(
    filters.dateFrom ? new Date(filters.dateFrom) : null
  )
  const [dateTo, setDateTo] = useState<Date | null>(
    filters.dateTo ? new Date(filters.dateTo) : null
  )
  const [filterAsset, setFilterAsset] = useState<string | undefined>(
    filters.asset
  )
  const [filterStatus, setFilterStatus] = useState<string | undefined>(
    filters.status
  )
  const [filterTransactionType, setFilterTransactionType] = useState<
    string | undefined
  >(filters.type)
  const [filterDebitCredit, setFilterDebitCredit] = useState<
    string | undefined
  >(filters.direction)
  const [searchTxId, setSearchTxId] = useState<string | undefined>(filters.uuid)
  const [searchPOSTxId, setSearchPOSTxId] = useState<string | undefined>(
    filters.posId
  )

  let searchObj: any

  useEffect(() => {
    if (searchTxId == null || searchTxId == "") {
      serachData()
    }
    txIdInputRef.current?.focus()
  }, [searchTxId])

  useEffect(() => {
    if (searchPOSTxId == null || searchPOSTxId == "") {
      serachData()
    }
    extPosTxIDInputRef.current?.focus()
  }, [searchPOSTxId])

  useEffect(() => {
    if (filterTransactionType == null || filterTransactionType == "") {
      serachData()
    }
  }, [filterTransactionType])

  const serachData = () => {
    submitFilter({
      ...(filterAsset && { asset: filterAsset }),
      ...(filterStatus && { status: filterStatus }),
      ...(searchTxId && { uuidSearch: searchTxId }),
      ...(searchPOSTxId && { posId: searchPOSTxId }),
      ...(dateFrom && {
        logicalDateTo: `${dayjs(dateFrom.toString()).format(
          "YYYY-MM-DD"
        )}T00:00:00.000Z`
      }),
      ...(dateTo && {
        logicalDateFrom: `${dayjs(dateTo.toString()).format(
          "YYYY-MM-DD"
        )}T23:59:59.999Z`
      }),
      ...(filterTransactionType && { type: filterTransactionType }),
      ...(filterDebitCredit && { direction: filterDebitCredit }),
      ...(searchTxId && searchObj)
    })
  }

  const setDates = (dateFrom: Date | null, dateTo: Date | null) => {
    setDateFrom(dateFrom)
    setDateTo(dateTo)
  }

  let allAssets = [...Asset]
  if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
    const arr = []
    arr[0] = Asset[1]
    arr[1] = Asset[2]
    allAssets = arr
  } else {
    allAssets = [...Asset]
  }

  const FilterSelectField: React.FC = () => {
    const [searchParams, setSearchParams] = useSearchParams()
    const location = useLocation()
    useEffect(() => {
      const queryParams = new URLSearchParams(location.search)
      if (queryParams.has(`asset`)) {
        queryParams.delete(`asset`)
        setSearchParams(queryParams)
      }
      if (queryParams.has(`status`)) {
        queryParams.delete(`status`)
        setSearchParams(queryParams)
      }
      if (queryParams.has(`direction`)) {
        queryParams.delete(`direction`)
        setSearchParams(queryParams)
      }
      if (queryParams.has(`sortBy`)) {
        queryParams.delete(`sortBy`)
        setSearchParams(queryParams)
      }
      if (queryParams.has(`sortDirection`)) {
        queryParams.delete(`sortDirection`)
        setSearchParams(queryParams)
      }
    }, [])
    return (
      <div id="filter-data">
        <div className="d-flex flex-row">
          <FilterSearch
            title={"Transaction ID"}
            searchDefaultTxt={t("Transaction ID")}
            value={searchTxId}
            inputRef={txIdInputRef}
            setFilterSearch={(value?: string) => setSearchTxId(value || "")}
          />

          <FilterSearch
              title={"PoS Terminal ID"}
              searchDefaultTxt={t("PoS Terminal ID")}
              value={searchPOSTxId}
              inputRef={extPosTxIDInputRef}
              setFilterSearch={(value?: string) => setSearchPOSTxId(value || "")}
          />
          <div className="search-addon right-addon">
            <span>Actual Date From: </span>
            <div className="search-crypto-input">
              <DatePicker
                selected={dateFrom}
                onChange={(date: Date) => setDateFrom(date)}
                startDate={dateFrom}
                endDate={dateTo}
                monthsShown={1}
                dateFormat="dd/M/yyyy"
                placeholderText="DD/MM/YYYY"
                maxDate={dateTo || new Date()}
              />
            </div>
          </div>
          <div className="search-addon right-addon">
            <span>To: </span>
            <div className="search-crypto-input">
              <DatePicker
                selected={dateTo}
                onChange={(date: Date) => setDateTo(date)}
                startDate={dateFrom}
                endDate={dateTo}
                monthsShown={1}
                dateFormat="dd/M/yyyy"
                placeholderText="DD/MM/YYYY"
                minDate={dateFrom}
                maxDate={new Date()}
              />
            </div>
          </div>
        </div>
        <div className="d-flex flex-row" style={{ paddingTop: "4px" }}>
          <FilterDropdown
            title={t("Token")}
            value={filterAsset}
            items={allAssets}
            setFilter={(value?: string) => setFilterAsset(value)}
          />

          <FilterDropdown
            title={t("Status")}
            value={filterStatus}
            items={Status}
            setFilter={(value?: string) => setFilterStatus(value)}
          />

          <FilterDropdown
            title={t("Transaction Type")}
            value={filterDebitCredit}
            items={DebitCredit}
            setFilter={(value?: string) => setFilterDebitCredit(value)}
          />
          <button
            className="btn wdz-btn-primary"
            onClick={() => serachData()}
            style={{ marginRight: "5px" }}
          >
            Search
          </button>

          <Dropdown>
            <Dropdown.Toggle variant="success" className="wdz-btn-primary">
              Download Report
            </Dropdown.Toggle>

            <Dropdown.Menu>
              <Dropdown.Item onClick={() => csvDownload()}>CSV</Dropdown.Item>
              <Dropdown.Item onClick={() => pdfDownload()}>PDF</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </div>
      </div>
    )
  }
  return FilterSelectField
}

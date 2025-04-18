import React, { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { useLocation, useNavigate, useSearchParams } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import {
  useMerchantTransactions,
  useMerchantTransactionsNoPage
} from "src/api/user"
import dayjs from "dayjs"
import { Dropdown } from "react-bootstrap"
import jsPDF from "jspdf"
import "jspdf-autotable"
import autoTable from "jspdf-autotable"
import { Transaction } from "src/api/models"
import useFormatCurrencyAmount from "src/helpers/formatCurrencyAmount"
import Pagination from "src/helpers/pagination"
import { useSortIcons } from "src/components/ui/useSortIcons"
import { useFilterSelectField } from "src/components/ui/useFilterSelectField"
import OverlayTrigger from "react-bootstrap/OverlayTrigger"
import Tooltip from "react-bootstrap/Tooltip"
import { date, string } from "yup"
import TransactionBalance from "src/components/ui/TransactionsBalance"
import ar from "date-fns/esm/locale/ar/index.js"

type State = {
  page: number
  sortBy: string
  sortDirection: string
  filter: { [key: string]: string }
}

const defaults = {
  page: 1,
  sortBy: "CREATED_AT",
  sortDirection: "DESC"
}
const filterKeys = [
  "asset",
  "status",
  "type",
  "direction",
  "dateFrom",
  "dateTo"
]

const InvoiceTransactions: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { t } = useTranslation()
  const formatter = useFormatCurrencyAmount()
  const [searchParams, setSearchParams] = useSearchParams()

  const filters: { [key: string]: string } = {}
  searchParams.forEach((value, key) => {
    if (filterKeys.includes(key)) {
      filters[key] = value
    }
  })

  const [state, setState] = useState<State>({
    page: parseInt(searchParams.get("page") || `${defaults.page}`, 10),
    sortBy: searchParams.get("sortBy") || defaults.sortBy,
    sortDirection: searchParams.get("sortDirection") || defaults.sortDirection,
    filter: filters
  })

  const urlSearchParams = new URLSearchParams()
  Object.entries(state.filter).forEach(([key, value]) =>
    urlSearchParams.append(key, value)
  )

  const apiParams = {
    ...{
      sortBy: state.sortBy,
      sortDirection: state.sortDirection
    },
    ...state.filter
  }

  const apiQueryParams = new URLSearchParams()
  Object.entries(apiParams).forEach(([key, value]) =>
    apiQueryParams.append(key, value)
  )

  const {
    data: transactionData,
    isFetching: isFetchingTransactions,
    error: errorTransactions
  } = useMerchantTransactions(
    `page=${state.page - 1}&${apiQueryParams.toString()}`
  )

  const { data: transactionDataNext } = useMerchantTransactions(
    `page=${state.page}&${apiQueryParams.toString()}`
  )

  const { data: transactionDataNoPage } = useMerchantTransactionsNoPage(
    `${apiQueryParams.toString()}`
  )

  const [loading, setLoading] = useState(isFetchingTransactions)

  const FilterSection = useFilterSelectField({
    submitFilter: (filters) => {
      // setState({ ...state, ...{ filter: filters } })
      setState({
        ...state,
        ...{ page: 1 },
        ...{ filter: filters }
      })
    },
    filters,
    csvDownload: () => {
      downloadTransactionsCSV()
    },
    pdfDownload: () => {
      downloadTransactionsPDF()
    }
  })

  useEffect(() => {
    setLoading(isFetchingTransactions)
  }, [isFetchingTransactions])

  useEffect(() => {
    setSearchParams({
      ...(state.page !== defaults.page && {
        page: `${state.page}`
      }),
      ...(state.sortBy !== defaults.sortBy && {
        sortBy: state.sortBy
      }),
      ...(state.sortDirection !== defaults.sortDirection && {
        sortDirection: state.sortDirection
      }),
      ...state.filter
    })
  }, [state.page, state.sortBy, state.sortDirection, state.filter])

  const sortData = (value: string) => {
    setLoading(true)
    if (value !== state.sortBy) {
      setState({
        ...state,
        ...{ page: 1, sortBy: value, sortDirection: "ASC" }
      })
    } else if (state.sortDirection === "ASC") {
      setState({ ...state, ...{ page: 1, sortDirection: "DESC" } })
    } else {
      setState({ ...state, ...{ page: 1, sortDirection: "ASC" } })
    }
  }

  const SortIcon = useSortIcons({
    sortData,
    sortedField: state.sortBy,
    sortedDirection: state.sortDirection
  })

  const navigatePage = (transactionId: string) => {
    navigate(`${RouteType.TRANSACTION_DETAIL}/${transactionId}`, {
      state: { from: location.pathname + location.search }
    })
  }

  const downloadTransactionsCSV = () => {
    if (transactionDataNoPage) {
      const data = [...transactionDataNoPage]
      // console.log(data)
      JSONToCSVConvertor(data, "Transactions", true)
    }
  }

  const downloadTransactionsPDF = () => {
    if (transactionDataNoPage) {
      const currentData = [...transactionDataNoPage]
      const head = [
        [
          "Sl.No",
          "Date",
          "Transaction ID",
          "Amount",
          "Fee Amount",
          "Asset",
          "Total Amount",
          "Fiat Value",
          "Status"
        ]
      ]
      const finalData: any = []
      currentData.map((item, index) => {
        const arr = []
        arr.push(index + 1)
        if (Object.prototype.hasOwnProperty.call(item, "createdAt")) {
          arr.push(dayjs(item.createdAt).format("MMMM D, YYYY h:mm A"))
        }
        if (Object.prototype.hasOwnProperty.call(item, "uuid")) {
          arr.push(item.uuid)
        }
        if (Object.prototype.hasOwnProperty.call(item, "amount")) {
          arr.push(item.amount)
        }
        if (Object.prototype.hasOwnProperty.call(item, "feeAmount")) {
          arr.push(item.feeAmount)
        }
        if (Object.prototype.hasOwnProperty.call(item, "asset")) {
          arr.push(item.asset)
        }
        if (Object.prototype.hasOwnProperty.call(item, "totalAmount")) {
          arr.push(item.totalAmount)
        }
        if (Object.prototype.hasOwnProperty.call(item, "fiatAmount")) {
          arr.push(item.fiatAmount)
        }
        if (Object.prototype.hasOwnProperty.call(item, "status")) {
          arr.push(item.status)
        }
        finalData.push(arr)
      })
      const doc = new jsPDF()
      autoTable(doc, {
        head: head,
        body: finalData,
        columnStyles: {
          0: { cellWidth: 10 },
          1: { cellWidth: 28 },
          5: { cellWidth: 15 }
        }
      })

      doc.save("myReports-Transactions.pdf")
    }
  }

  const JSONToCSVConvertor = (
    JSONData: any,
    ReportTitle: string,
    ShowLabel: boolean
  ) => {
    const arrData =
      typeof JSONData !== "object" ? JSON.parse(JSONData) : JSONData
    let CSV = ""
    const headersRequried = [
      "createdAt",
      "uuid",
      "amount",
      "feeAmount",
      "asset",
      "totalAmount",
      "fiatAsset",
      "fiatAmount",
      "status"
    ]
    if (ShowLabel) {
      let row = ""
      for (const index in arrData[0]) {
        switch (index) {
          case "createdAt":
            row += "Date" + ","
            break
          case "uuid":
            row += "Transaction ID" + ","
            break
          case "amount":
            row += "Amount" + ","
            break
          case "feeAmount":
            row += "Fee Amount" + ","
            break
          case "asset":
            row += "Asset" + ","
            break
          case "totalAmount":
            row += "Total Amount" + ","
            break
          case "fiatAsset":
            row += "Fiat Asset" + ","
            break
          case "fiatAmount":
            row += "Fiat Amount" + ","
            break
          case "status":
            row += "Status" + ","
            break
          default:
            break
        }
      }
      row = row.slice(0, -1)
      CSV += row + "\r\n"
    }
    for (let i = 0; i < arrData.length; i++) {
      let row = ""
      for (const index in arrData[i]) {
        if (headersRequried.includes(index)) {
          if (index == "createdAt") {
            row +=
              '"' +
              dayjs(arrData[i][index]).format("MMMM D, YYYY h:mm A") +
              '",'
          } else if (index == "feeAmount" || index == "totalAmount") {
            row += '"' + arrData[i][index].toFixed(5) + '",'
          } else {
            row += '"' + arrData[i][index] + '",'
          }
        }
      }
      row.slice(0, row.length - 1)
      CSV += row + "\r\n"
    }
    if (CSV === "") {
      return
    }
    let fileName = "MyReport_"
    fileName += ReportTitle.replace(/ /g, "_")
    const uri = "data:text/csv;charset=utf-8," + escape(CSV)
    const link = document.createElement("a")
    link.href = uri
    link.style.visibility = "hidden"
    link.download = fileName + ".csv"
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
  const paginate = (page: number) => {
    setLoading(true)
    setState({ ...state, ...{ page } })
  }

  const checkIcon = (iconName: string) => {
    switch (iconName) {
      case "IN_PROGRESS":
        return (
          <OverlayTrigger
            overlay={
              <Tooltip id="tooltip-disabled">{t("In Progress")}</Tooltip>
            }
          >
            <img
              src={"/images/transactions/inprogress.png"}
              alt={"In Progress"}
              width="20px"
              height="20px"
              style={{ marginLeft: "20px" }}
            />
          </OverlayTrigger>
        )
        break
      case "SUCCESSFUL":
        return (
          <OverlayTrigger
            overlay={<Tooltip id="tooltip-disabled">{t("Success")}</Tooltip>}
          >
            <img
              src={"/images/transactions/success.png"}
              alt={"success"}
              width="20px"
              height="20px"
              style={{ marginLeft: "20px" }}
            />
          </OverlayTrigger>
        )
        break
      case "FAILED":
        return (
          <OverlayTrigger
            overlay={<Tooltip id="tooltip-disabled">{t("Failed")}</Tooltip>}
          >
            <img
              src={"/images/transactions/failed.png"}
              alt={"Failed"}
              width="20px"
              height="20px"
              style={{ marginLeft: "20px" }}
            />
          </OverlayTrigger>
        )
        break
      case "OVERPAID":
        return (
          <OverlayTrigger
            overlay={<Tooltip id="tooltip-disabled">{t("Overpaid")}</Tooltip>}
          >
            <img
              src={"/images/transactions/overpaid.png"}
              alt={"Failed"}
              width="20px"
              height="20px"
              style={{ marginLeft: "20px" }}
            />
          </OverlayTrigger>
        )
        break
      case "UNDERPAID":
        return (
          <OverlayTrigger
            overlay={<Tooltip id="tooltip-disabled">{t("Underpaid")}</Tooltip>}
          >
            <img
              src={"/images/transactions/underpaid.png"}
              alt={"Failed"}
              width="20px"
              height="20px"
              style={{ marginLeft: "20px" }}
            />
          </OverlayTrigger>
        )
        break
      default:
        return iconName
        break
    }
  }

  return (
    <>
      {/* <TransactionBalance trxdata={transactionDataNoPage} /> */}
      <div style={{ display: "flex" }}>
        <p
          className="wdz-btn-primary"
          style={{ borderRadius: "19px", padding: "8px", margin: "10px 0" }}
        >
          {t("Invoice History")}
        </p>
        <p
          style={{
            borderRadius: "19px",
            padding: "8px",
            margin: "10px 0 10px 10px",
            width: "100px"
          }}
        >
          {t("Tax")}
        </p>
      </div>
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <FilterSection />
        <Dropdown>
          <Dropdown.Toggle variant="success" className="wdz-btn-primary">
            Download Report
          </Dropdown.Toggle>

          <Dropdown.Menu>
            <Dropdown.Item onClick={() => downloadTransactionsCSV()}>
              CSV
            </Dropdown.Item>
            <Dropdown.Item onClick={() => downloadTransactionsPDF()}>
              PDF
            </Dropdown.Item>
          </Dropdown.Menu>
        </Dropdown>
      </div>
      <div className="table-responsive mt-4" style={{ overflowX: "visible" }}>
        <table className="table table-hover transactions-table">
          <thead>
            <tr>
              <SortIcon
                element="Date"
                sortField="CREATED_AT"
                wordLabel="Date"
              />
              <th scope="col">{t("Transaction ID")}</th>
              <SortIcon
                element="Amount"
                sortField="AMOUNT"
                wordLabel="Amount"
              />
              <th scope="col">{t("Fee Amount")}</th>
              <th scope="col">{t("Total Amount")}</th>
              <th scope="col">{t("Fiat Value")}</th>
              <SortIcon
                element="Status"
                sortField="STATUS"
                wordLabel="Status"
              />
            </tr>
          </thead>
          <tbody>
            {!errorTransactions &&
              transactionData &&
              transactionData.length > 0 &&
              transactionData.map((transaction: Transaction) => [
                <tr
                  key={transaction.id}
                  onClick={() => navigatePage(transaction.id)}
                  style={{ cursor: "pointer" }}
                >
                  <td>
                    {dayjs(transaction.createdAt).format("MMMM D, YYYY h:mm A")}
                  </td>
                  <td>
                    {/* {transaction.direction === "INCOMING"
                      ? transaction.senderName
                      : transaction.receiverName} */}
                    {transaction.uuid}
                  </td>
                  <td>
                    {formatter(transaction.amount, {
                      asset: transaction.asset
                    })}
                  </td>
                  <td>
                    {formatter(transaction.feeAmount, {
                      asset: transaction.asset
                    })}
                  </td>
                  <td
                    className={`text-nowrap ${
                      transaction.direction === "INCOMING"
                        ? "text-success"
                        : "text-danger"
                    }`}
                  >
                    <img
                      src={"/images/" + transaction.asset + ".svg"}
                      alt={transaction.asset}
                      width="25px"
                      height="25px"
                      style={{ marginRight: "10px" }}
                    />
                    {transaction.direction === "OUTGOING" && "-"}
                    {formatter(transaction.totalAmount, {
                      asset: transaction.asset
                    })}
                  </td>
                  <td>
                    {transaction.fiatAsset} {transaction.fiatAmount}
                  </td>
                  <td>{checkIcon(transaction.status)}</td>
                </tr>
              ])}
            <tr className="transaction-count">
              <td colSpan={2}>
                <p style={{ margin: 0 }}>
                  {transactionDataNoPage && transactionDataNoPage.length == 0
                    ? `No data available`
                    : null}
                  {transactionDataNoPage &&
                  transactionDataNoPage.length > 0 &&
                  transactionDataNoPage.length <= 9
                    ? `Showing ${
                        transactionDataNoPage && transactionDataNoPage.length
                      } item${
                        transactionDataNoPage &&
                        transactionDataNoPage.length == 1
                          ? ""
                          : "s"
                      }`
                    : null}
                  {transactionDataNoPage && transactionDataNoPage.length > 9
                    ? `Showing ${
                        state.page == 1 ? 1 : (state.page - 1) * 10 + 1
                      } to ${
                        state.page == 1
                          ? 10
                          : transactionData && transactionData.length > 9
                          ? transactionData.length * (state.page - 1) + 10
                          : transactionData &&
                            transactionData.length + state.page * 10 - 10
                      }${""}
                    ${
                      transactionData && transactionData.length == 1
                        ? "item"
                        : "items"
                    } 
                    from ${
                      transactionDataNoPage == undefined
                        ? 0
                        : transactionDataNoPage && transactionDataNoPage.length
                    }`
                    : null}
                </p>
              </td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
          </tbody>
        </table>
        {transactionData && (
          <Pagination
            paginate={paginate}
            currPage={state.page}
            nextPage={transactionDataNext?.length === 0}
            loading={loading}
            alltransactionlength={0}
          />
        )}
        {errorTransactions && (
          <div
            className="alert alert-danger"
            role="alert"
            data-testid="errorMessage"
          >
            {errorTransactions.message}
          </div>
        )}
      </div>
    </>
  )
}

export default InvoiceTransactions

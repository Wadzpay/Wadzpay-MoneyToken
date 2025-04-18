import React, { useEffect, useState, useContext } from "react"
import { useTranslation } from "react-i18next"
import { useLocation, useNavigate, useSearchParams } from "react-router-dom"
import { Button } from "react-bootstrap"
import { RouteType } from "src/constants/routeTypes"
import {
  useRefundTransactions,
  useRefundTransactionsNoPage
} from "src/api/user"
import dayjs from "dayjs"
import utc from "dayjs/plugin/utc"
import tz from "dayjs/plugin/timezone"
import { Dropdown } from "react-bootstrap"
import jsPDF from "jspdf"
import "jspdf-autotable"
import autoTable from "jspdf-autotable"
import { Transaction } from "src/api/models"
import useFormatCurrencyAmount from "src/helpers/formatCurrencyAmount"
import Pagination from "src/helpers/pagination"
import { useSortIcons } from "src/components/ui/useSortIcons"
import { useRefundFilterSelectField } from "src/components/ui/useRefundFilterSelectField"
import OverlayTrigger from "react-bootstrap/OverlayTrigger"
import Tooltip from "react-bootstrap/Tooltip"
import Card from "src/helpers/Card"
import { UserContext } from "src/context/User"
import ApprovalPopupModal from "src/components/ui/PopUps/ApprovalPopupModal"

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

const RefundReqApproval: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { t } = useTranslation()
  const formatter = useFormatCurrencyAmount()
  const { user } = useContext(UserContext)
  const [searchParams, setSearchParams] = useSearchParams()

  dayjs.extend(utc)
  dayjs.extend(tz)

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

  let apiParams

  if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
    apiParams = {
      ...{
        sortBy: state.sortBy,
        sortDirection: state.sortDirection,
        asset: "USDT, ETH",
        refundMode: "WALLET",
        refundStatus: "REFUND_APPROVED, REFUND_CANCELED, REFUNDED"
      },
      ...state.filter
    }
  } else {
    apiParams = {
      ...{
        sortBy: state.sortBy,
        sortDirection: state.sortDirection,
        refundMode: "WALLET",
        refundStatus: "REFUND_APPROVED, REFUND_CANCELED, REFUNDED"
      },
      ...state.filter
    }
  }

  const apiQueryParams = new URLSearchParams()
  Object.entries(apiParams).forEach(([key, value]) =>
    apiQueryParams.append(key, value)
  )

  const {
    data: transactionData,
    isFetching: isFetchingTransactions,
    error: errorTransactions
  } = useRefundTransactions(
    `page=${state.page - 1}&${apiQueryParams.toString()}`
  )

  useEffect(() => {
    // console.log(transactionData)
  }, [transactionData])

  const { data: transactionDataNext } = useRefundTransactions(
    `page=${state.page}&${apiQueryParams.toString()}`
  )

  const { data: transactionDataNoPage } = useRefundTransactionsNoPage(
    `${apiQueryParams.toString()}`
  )

  const [loading, setLoading] = useState(isFetchingTransactions)

  const FilterSection = useRefundFilterSelectField({
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
          "Transaction ID",
          "Date",
          "Asset",
          "Total Amount",
          "Fiat Amount",
          "Status",
          "Refund Value",
          "Refund Mode"
        ]
      ]
      const finalData: any = []
      currentData.map((item, index) => {
        const arr = []
        arr.push(index + 1)
        if (Object.prototype.hasOwnProperty.call(item, "uuid")) {
          arr.push(item.uuid)
        }
        if (Object.prototype.hasOwnProperty.call(item, "createdAt")) {
          arr.push(dayjs(item.createdAt).format("MMMM D, YYYY h:mm A"))
        }
        if (Object.prototype.hasOwnProperty.call(item, "asset")) {
          arr.push(item.asset)
        }
        if (Object.prototype.hasOwnProperty.call(item, "totalAmount")) {
          arr.push(item.totalAmount)
        }
        if (Object.prototype.hasOwnProperty.call(item, "fiatAmount")) {
          arr.push(`${item.fiatAmount} ${item.fiatAsset}`)
        }
        if (Object.prototype.hasOwnProperty.call(item, "status")) {
          arr.push(item.status)
        }
        if (Object.prototype.hasOwnProperty.call(item, "refundFiatAmount")) {
          arr.push(item.refundFiatAmount)
        }
        if (Object.prototype.hasOwnProperty.call(item, "refundType")) {
          arr.push(item.refundType)
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
      "uuid",
      "createdAt",
      "asset",
      "totalAmount",
      "fiatAmount",
      "status",
      "refundFiatAmount",
      "refundType"
    ]
    if (ShowLabel) {
      let row = ""
      for (const index in arrData[0]) {
        switch (index) {
          case "uuid":
            row += "Transaction ID" + ","
            break
          case "createdAt":
            row += "Date" + ","
            break
          case "asset":
            row += "Asset" + ","
            break
          case "totalAmount":
            row += "Total Amount" + ","
            break
          case "fiatAmount":
            row += "Fiat Amount" + ","
            break
          case "status":
            row += "Status" + ","
            break
          case "refundFiatAmount":
            row += "Refund Value" + ","
            break
          case "refundType":
            row += "Refund Mode" + ","
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
          } else if (
            index == "feeAmount" ||
            index == "totalAmount" ||
            index == "amount"
          ) {
            row += '"' + arrData[i][index].toFixed(8) + '",'
          } else if (index == "fiatAmount") {
            // row += '"' + arrData[i][index] + '"' + '"' + arrData[i]["fiatAsset"] + '",'
            row += `${arrData[i][index]} ${arrData[i]["fiatAsset"]},`
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
    const uri = "data:text/csv;charset=utf-8,%EF%BB%BF" + encodeURI(CSV)
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
            placement="left"
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

  const dateFormat = (time: any) => {
    const localStorageTime = localStorage.getItem("TimeZone")
    if (localStorageTime) {
      const timezone = JSON.parse(localStorageTime).value || "Asia/Kolkata"
      return dayjs(time).tz(timezone).format("DD-MM-YYYY HH:mm:ss")
    } else {
      return dayjs(time).format("DD-MM-YYYY HH:mm:ss")
    }
  }
  const trxIdReturn = (obj: any) => {
    if (obj.description && obj.uuid == "") {
      return null
    }

    if (obj.description && obj.uuid == null) {
      return null
    }

    if (
      obj.description &&
      obj.description.includes("-") &&
      obj.description.length == 36
    ) {
      return obj.description
    }

    return obj.uuid
  }

  const checkRefundBtnType = (type: string) => {
    if (type == null) {
      return <Button variant="btn btn-success btn-sm">{t("Refund")}</Button>
    }
    if (type == "REFUNDED") {
      return (
        <Button
          variant="btn"
          style={{ background: "rgb(240, 240, 240)" }}
          disabled
        >
          {t("Refund")}
        </Button>
      )
    }
    if (
      type == "REFUND_INITIATED" ||
      type == "REFUND_ACCEPTED" ||
      type == "REFUND_APPROVED"
    ) {
      return (
        <Button variant="btn btn-primary btn-sm" disabled>
          {t("Refund")}
        </Button>
      )
    }
    if (
      type == "REFUND_FAILED" ||
      type == "REFUND_CANCELED" ||
      type == "REFUND_EXPIRED"
    ) {
      return (
        <Button variant="btn btn-danger btn-sm" disabled>
          {t("Refund")}
        </Button>
      )
    }
  }

  return (
    <div className="refundPopup">
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          margin: "0 2%"
        }}
        className="mt-1"
      >
        <h3>Refund Approval</h3>
        <button
          className="btn btn-primary wdz-btn-primary wdz-btn-md mt-1"
          onClick={() => navigate(RouteType.REFUND_DISPUTE)}
        >
          {t("Cancel")}
        </button>
      </div>
      <Card>
        {/* <div
          style={{ display: "flex", justifyContent: "space-between" }}
          className="mt-1"
        >
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
        </div> */}
        <div className="table-responsive mt-4">
          <table className="table transactions-table">
            <thead>
              <tr>
                <th scope="col">{t("Transaction ID")}</th>
                <SortIcon
                  element="Date"
                  sortField="CREATED_AT"
                  wordLabel="Date"
                />
                <th scope="col">{t("Asset")}</th>
                <SortIcon
                  element="Amount"
                  sortField="AMOUNT"
                  wordLabel="Received Digital Amount"
                />
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Received Fiat Amount")}
                </th>
                {/* <SortIcon
                  element="Status"
                  sortField="STATUS"
                  wordLabel="Status"
                />*/}
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Refund Value(Fiat)")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Refund Value in USDT")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Refund Mode")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Refund")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Customer Name")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Mobile Number")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Email Address")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Reason for Refund")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Wallet Address")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Approve")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Reject")}
                </th>
              </tr>
            </thead>
            <tbody>
              {!errorTransactions &&
                transactionData &&
                transactionData.length > 0 &&
                transactionData.map((transaction: Transaction) => [
                  <tr key={transaction.id}>
                    <td>{trxIdReturn(transaction)}</td>
                    <td>
                      {transaction.refundDateTime === null
                        ? ""
                        : dateFormat(transaction.refundDateTime)}
                    </td>
                    <td>
                      <img
                        src={"/images/" + transaction.asset + ".svg"}
                        alt={transaction.asset}
                        width="20px"
                        height="20px"
                        style={{ marginRight: "5px" }}
                      />
                      <span className="fiatAssetFontSize">
                        {transaction.asset && transaction.asset}
                      </span>
                    </td>
                    <td>
                      {formatter(transaction.totalDigitalCurrencyReceived, {
                        asset: transaction.asset
                      })}
                    </td>
                    <td>
                      <span style={{ float: "right" }}>
                        {transaction.totalFiatReceived &&
                          transaction.totalFiatReceived.toFixed(2)}
                        <span className="fiatAssetFontSize">
                          {transaction.fiatAsset}
                        </span>
                      </span>
                    </td>
                    {/*<td>{checkIcon(transaction.status)}</td>*/}
                    <td>
                      <span style={{ float: "right" }}>
                        {transaction.refundFiatAmount}
                        <span className="fiatAssetFontSize">
                          {transaction.fiatAsset}
                        </span>
                      </span>
                    </td>
                    <td>
                      <span style={{ float: "right" }}>
                        {transaction.refundAmountDigital}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <span>{transaction.refundMode}</span>
                    </td>
                    <td>{checkRefundBtnType(transaction.refundStatus)}</td>
                    <td style={{ textAlign: "center" }}>
                      <span>
                        {transaction.refundUserName === null
                          ? "-"
                          : transaction.refundUserName}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <span>
                        {transaction.refundUserMobile === null
                          ? "-"
                          : transaction.refundUserMobile}
                      </span>
                    </td>
                    <td style={{ wordBreak: "break-all", textAlign: "center" }}>
                      <span>
                        {transaction.refundUserEmail === null
                          ? "-"
                          : transaction.refundUserEmail}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <span>
                        {transaction.refundReason === null
                          ? "-"
                          : transaction.refundReason}
                      </span>
                    </td>
                    <td style={{ wordBreak: "break-all", textAlign: "center" }}>
                      <span>
                        {transaction.refundWalletAddress === null
                          ? "-"
                          : transaction.refundWalletAddress}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <ApprovalPopupModal
                        btntype="accept"
                        txt="Approve"
                        reason="Only products with complete accessories and with original packing can be returned or exchanged."
                        inputdisabled={true}
                        color="btn btn-success btn-sm"
                        page="approvePage"
                        id={trxIdReturn(transaction)}
                        apicallstatus={() => window.location.reload()}
                        disabled={
                          transaction.refundStatus !== "REFUND_APPROVED"
                        }
                        trx={transaction}
                      />
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <ApprovalPopupModal
                        btntype="reject"
                        txt="Reject"
                        reason=""
                        inputdisabled={false}
                        color={
                          transaction.refundStatus == "REFUNDED"
                            ? "btn btn-btn"
                            : "btn btn-danger btn-sm"
                        }
                        page="approvePage"
                        id={trxIdReturn(transaction)}
                        apicallstatus={() => window.location.reload()}
                        disabled={
                          transaction.refundStatus !== "REFUND_APPROVED"
                        }
                        rejectreason={
                          transaction.refundApprovalComment == null
                            ? transaction.refundAcceptanceComment
                            : transaction.refundApprovalComment
                        }
                        trx={transaction}
                      />
                    </td>
                  </tr>
                ])}
              <tr className="transaction-count">
                <td colSpan={12}>
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
                          : transactionDataNoPage &&
                            transactionDataNoPage.length
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
              alltransactionlength={transactionDataNoPage?.length || 0}
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
      </Card>
    </div>
  )
}

export default RefundReqApproval

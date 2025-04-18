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
import { BsInfoCircleFill } from "react-icons/bs"
import { Transaction } from "src/api/models"
import useFormatCurrencyAmount from "src/helpers/formatCurrencyAmount"
import Pagination from "src/helpers/pagination"
import { useSortIcons } from "src/components/ui/useSortIcons"
import { useRefundFilterSelectField } from "src/components/ui/useRefundFilterSelectField"
import OverlayTrigger from "react-bootstrap/OverlayTrigger"
import Tooltip from "react-bootstrap/Tooltip"
import Card from "src/helpers/Card"
import { UserContext } from "src/context/User"
import AcceptancePopupModal from "src/components/ui/PopUps/AcceptancePopupModal"

import RefundButtonStatus from "../Refund/RefundButtonStatus"

const btnMinWidth = "120px"

type State = {
  page: number
  sortBy: string
  sortDirection: string
  isAcceptancePage: boolean
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

const RefundReqAcceptance: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { t } = useTranslation()
  const formatter = useFormatCurrencyAmount()
  const { user } = useContext(UserContext)
  const [searchParams, setSearchParams] = useSearchParams()
  const [show, setShow] = useState(false)

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
    isAcceptancePage: true,
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
        isAcceptancePage: "true",
        asset: "USDT, ETH",
        refundMode: "WALLET",
        refundStatus:
          "REFUND_ACCEPTED, REFUND_CANCELED, REFUND_APPROVED, REFUNDED, REFUND_HOLD, REFUND_FAILED"
      },
      ...state.filter
    }
  } else {
    apiParams = {
      ...{
        sortBy: state.sortBy,
        sortDirection: state.sortDirection,
        isAcceptancePage: "true",
        refundMode: "WALLET",
        refundStatus:
          "REFUND_ACCEPTED, REFUND_CANCELED, REFUND_APPROVED, REFUNDED, REFUND_HOLD, REFUND_FAILED"
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

  const dateTimeFormat = (time: any) => {
    if (!time) {
      return null
    }
    const localStorageTime = localStorage.getItem("TimeZone")
    if (localStorageTime) {
      const timezone = JSON.parse(localStorageTime).value || "Asia/Dubai"
      return dayjs(time).tz(timezone).format("DD-MM-YYYY HH:mm:ss")
    } else {
      return dayjs(time).tz("Asia/Dubai").format("DD-MM-YYYY HH:mm:ss")
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
      return (
        <Button
          variant="btn-sm btn btn-success maXwidth-200"
          style={{ width: btnMinWidth }}
        >
          {t("Initiate")}
        </Button>
      )
    }
    if (type == "REFUNDED") {
      return (
        <Button
          variant="btn-sm btn btn-secondary refundbtn btn-sm"
          style={{ width: btnMinWidth }}
          disabled
        >
          {t("REFUNDED")}
        </Button>
      )
    }
    if (
      type == "REFUND_ACCEPTED" ||
      type == "REFUND_HOLD" ||
      type == "REFUND_APPROVED"
    ) {
      return (
        <Button
          variant="btn-sm btn btn-primary btn-sm"
          style={{ width: btnMinWidth }}
          disabled
        >
          {t("CVF Submitted")}
        </Button>
      )
    }
    if (type == "REFUND_INITIATED") {
      return (
        <Button
          variant="btn btn-primary btn-sm"
          disabled
          style={{ width: btnMinWidth }}
        >
          {t("Weblink Sent")}
        </Button>
      )
    }
    if (type == "REFUND_EXPIRED") {
      return (
        <Button
          variant="btn btn-primary btn-sm"
          style={{ width: btnMinWidth }}
          disabled
        >
          {"Reinitiate"}
          <br />
          {"WL Expired"}
        </Button>
      )
    }
    if (type == "REFUND_CANCELED") {
      return (
        <Button
          variant="btn-sm btn btn-success btn-sm"
          style={{ width: btnMinWidth }}
          disabled
        >
          {"Reinitiate"}
          <br />
          {"Rejected"}
        </Button>
      )
    }
    if (type == "REFUND_FAILED") {
      return (
        <Button
          variant="btn-sm btn btn-success btn-sm"
          style={{ width: btnMinWidth }}
          disabled
        >
          {"Reinitiate"}
          <br />
          {t("Failed")}
        </Button>
      )
    }
  }

  return (
    <>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          margin: "0 2%"
        }}
        className="mt-1"
      >
        <h3>Refund Acceptance</h3>
        {/* <button
          className="btn btn-primary wdz-btn-primary wdz-btn-md mt-1"
          onClick={() => navigate(RouteType.REFUND_DISPUTE)}
        >
          {t("Cancel")}
        </button> */}
      </div>
      <Card>
        <div
          style={{ display: "flex", justifyContent: "space-between" }}
          className="mt-1"
        >
          <FilterSection />
        </div>
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
                <th scope="col">{t("Refund POS Logical Date")}</th>
                <th scope="col">{t("Refund Actual Date")}</th>
                <th scope="col">{t("Refund Actual Time")}</th>
                <th scope="col">{t("Refund POS Shift")}</th>
                <th scope="col">{t("Refund POS Sequence No.")}</th>
                <th scope="col">{t("Refund POS Serial No.")}</th>
                <th scope="col">{t("Refund POS Transaction ID")}</th>
                <th scope="col">{t("Refund Transaction ID")}</th>
                <SortIcon
                  element="extPosLogicalDate"
                  sortField="EXTPOSTRANSACTIONID"
                  wordLabel="POS Transaction ID"
                />
                <th scope="col">{t("Transaction ID")}</th>
                <th scope="col">{t("No of Times Refund Done")}</th>
                <SortIcon
                  element="Date"
                  sortField="CREATED_AT"
                  wordLabel="Date & Time"
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
                  {t("Refundable Amount in Fiat")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Refund Amount in Fiat")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Balance Fiat Amount")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Refund Type")}
                </th>
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
                  {t("Refund Status")}
                  <span
                    style={{ marginLeft: "8px", cursor: "pointer" }}
                    onClick={() => setShow(true)}
                    title="Button color info"
                  >
                    <BsInfoCircleFill />
                  </span>
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Transaction Type")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Received Payment Date & Time")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Status")}
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
                  {t("Source Wallet Address")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Source Wallet Address Status")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Refund initiated from POS/MD")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Accept Button")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Reject Button")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Hold Button")}
                </th>
              </tr>
            </thead>
            <tbody>
              {!errorTransactions &&
                transactionData &&
                transactionData.length > 0 &&
                transactionData.map((transaction: Transaction) => [
                  <tr key={transaction.id}>
                    <td>
                      {dateTimeFormat(transaction.extPosLogicalDateRefund)}
                    </td>
                    <td>
                      {transaction.extPosActualDateRefund
                        ? dateFormat(transaction.extPosActualDateRefund)
                        : ""}
                    </td>
                    <td>{transaction.extPosActualTimeRefund}</td>
                    <td>{transaction.extPosShiftRefund}</td>
                    <td>{transaction.extPosSequenceNoRefund}</td>
                    <td></td>
                    <td>{transaction.extPosTransactionIdRefund}</td>
                    <td>{transaction.refundTransactionID}</td>
                    <td style={{ wordBreak: "break-all" }}>
                      {transaction.extPosTransactionId}
                    </td>

                    <td>{trxIdReturn(transaction)}</td>
                    <td>{transaction.numberOfRefunds}</td>
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
                    <td>{transaction.refundableAmountFiat}</td>
                    <td>{transaction.refundFiatAmount}</td>
                    <td>
                      {transaction?.refundStatus == "NULL"
                        ? "NA"
                        : (
                            transaction?.refundableAmountFiat -
                            transaction?.refundFiatAmount
                          ).toFixed(2)}
                    </td>
                    <td>{transaction.refundType}</td>
                    {/* <td>{checkIcon(transaction.status)}</td>*/}
                    <td style={{ textAlign: "center" }}>
                      {transaction?.refundStatus === "REFUNDED" ? (
                        <span>
                          {transaction.refundFiatAmount}
                          <span className="fiatAssetFontSize">
                            {transaction.fiatAsset}
                          </span>
                        </span>
                      ) : (
                        "-"
                      )}
                    </td>
                    <td style={{ textAlign: "center" }}>
                      {transaction?.refundStatus === "REFUNDED" ? (
                        <span>{transaction.refundAmountDigital}</span>
                      ) : (
                        "-"
                      )}
                    </td>
                    <td style={{ textAlign: "center" }}>
                      {transaction?.refundStatus === "REFUNDED" ? (
                        <span>{transaction.refundMode}</span>
                      ) : (
                        "-"
                      )}
                    </td>
                    <td>{checkRefundBtnType(transaction.refundStatus)}</td>
                    <td>{transaction.transactionType}</td>
                    <td>
                      {transaction.paymentReceivedDate != undefined
                        ? dateTimeFormat(transaction.paymentReceivedDate)
                        : ""}
                    </td>
                    <td>{transaction.status}</td>
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
                    <td style={{ wordBreak: "break-all", textAlign: "center" }}>
                      <span>
                        {transaction.sourceWalletAddress === null
                          ? "-"
                          : transaction.sourceWalletAddress}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      {transaction.walletAddressMatch == true
                        ? "Matched"
                        : "Not Matched"}
                    </td>
                    <td style={{ textAlign: "center" }}>
                      {transaction.refundOrigin}
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <AcceptancePopupModal
                        btntype="accept"
                        txt="Accept"
                        reason="Only products with complete accessories and with original packing can be returned or exchanged."
                        inputdisabled={true}
                        color={
                          transaction.refundStatus === "REFUND_APPROVED"
                            ? "btn btn-primary btn-sm"
                            : "btn btn-success btn-sm"
                        }
                        id={trxIdReturn(transaction)}
                        apicallstatus={() => window.location.reload()}
                        disabled={
                          transaction.refundStatus !== "REFUND_ACCEPTED" ||
                          "REFUND_HOLD"
                        }
                        trx={transaction}
                      />
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <AcceptancePopupModal
                        btntype="reject"
                        txt="Reject"
                        reason={transaction.refundReason}
                        inputdisabled={false}
                        color={
                          transaction.refundStatus == "REFUNDED" ||
                          transaction.refundStatus == "REFUND_FAILED"
                            ? "btn btn-sm refundbtn refundbtnOpacity null"
                            : "btn btn-danger btn-sm"
                        }
                        id={trxIdReturn(transaction)}
                        apicallstatus={() => window.location.reload()}
                        disabled={
                          transaction.refundStatus !== "REFUND_ACCEPTED"
                        }
                        rejectreason="reject test"
                        trx={transaction}
                      />
                    </td>
                    <td style={{ textAlign: "center" }}>
                      {transaction.walletAddressMatch == true &&
                      transaction.refundOrigin == "POS" &&
                      (transaction.refundStatus == "REFUND_ACCEPTED" ||
                        transaction.refundStatus == "REFUND_HOLD") ? (
                        <AcceptancePopupModal
                          btntype="hold"
                          txt="Hold"
                          reason=""
                          inputdisabled={true}
                          color={
                            transaction.refundStatus ===
                            ("REFUND_ACCEPTED" || "REFUND_HOLD")
                              ? "btn btn-danger btn-sm"
                              : "btn btn-danger btn-sm"
                          }
                          id={trxIdReturn(transaction)}
                          apicallstatus={() => window.location.reload()}
                          trx={transaction}
                        />
                      ) : (
                        "-"
                      )}
                    </td>

                    {/* <td style={{ textAlign: "center", cursor: "pointer" }}>
                      {transaction.posIsGenuineTxn == true ? (
                        <button
                          type="button"
                          className="btn btn-sm btn-danger"
                          disabled={true}
                        >
                          Hold
                        </button>
                      ) : (
                        "-"
                      )}
                    </td> */}
                  </tr>
                ])}
              <tr className="transaction-count">
                <td colSpan={17}>
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
        <button
          className="btn btn-primary wdz-btn-primary wdz-btn-md mt-2"
          onClick={() => navigate(RouteType.REFUND_DISPUTE)}
        >
          {t("Back")}
        </button>
      </Card>

      <RefundButtonStatus
        show={show}
        setShow={(value?: boolean) => setShow(value || false)}
      />
    </>
  )
}

export default RefundReqAcceptance

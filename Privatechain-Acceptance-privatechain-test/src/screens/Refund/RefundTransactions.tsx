import React, { useEffect, useState, useContext } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import { RouteType } from "src/constants/routeTypes";
import {
  useRefundTransactions,
  useRefundTransactionsNoPage,
} from "src/api/user";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import tz from "dayjs/plugin/timezone";
import jsPDF from "jspdf";
import "jspdf-autotable";
import autoTable from "jspdf-autotable";
import { BsInfoCircleFill } from "react-icons/bs";
import { Transaction } from "src/api/models";
import useFormatCurrencyAmount from "src/helpers/formatCurrencyAmount";
import Pagination from "src/helpers/pagination";
import { useSortIcons } from "src/components/ui/useSortIcons";
import { useRefundFilterSelectField } from "src/components/ui/useRefundFilterSelectField";
import Card from "src/helpers/Card";
import { UserContext } from "src/context/User";
import { MerchantContext } from "src/context/Merchant";
import env from "src/env.template";

import RefundButtonStatus from "./RefundButtonStatus";
import { ENV } from "../../env";
import InitiateRefundPopupModal from "../../components/ui/PopUps/InitiateRefundPopupModal";

type State = {
  page: number;
  sortBy: string;
  sortDirection: string;
  filter: { [key: string]: string };
};

const defaults = {
  page: 1,
  sortBy: "CREATED_AT",
  sortDirection: "DESC",
};

const filterKeys = [
  "asset",
  "status",
  "type",
  "direction",
  "dateFrom",
  "dateTo",
  "refundMode",
];

const RefundTransactions: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const formatter = useFormatCurrencyAmount();
  const { user } = useContext(UserContext);
  const { merchantDetails, institutionDetails } = useContext(MerchantContext);
  const [searchParams, setSearchParams] = useSearchParams();
  const [show, setShow] = useState(false);
  const [merchantType, setMerchantType] = useState("");
  console.log("institution", institutionDetails)
  const digitalAsset =
    env.TYPE === ENV.DEV || env.TYPE === ENV.TESTING ? "BTC" : "USDT";

  dayjs.extend(utc);
  dayjs.extend(tz);

  const filters: { [key: string]: string } = {};
  searchParams.forEach((value, key) => {
    if (filterKeys.includes(key)) {
      filters[key] = value;
    }
  });

  const [state, setState] = useState<State>({
    page: parseInt(searchParams.get("page") || `${defaults.page}`, 10),
    sortBy: searchParams.get("sortBy") || defaults.sortBy,
    sortDirection: searchParams.get("sortDirection") || defaults.sortDirection,
    filter: filters,
  });

  const urlSearchParams = new URLSearchParams();
  Object.entries(state.filter).forEach(([key, value]) =>
    urlSearchParams.append(key, value)
  );

  let apiParams;

  if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
    apiParams = {
      ...{
        sortBy: state.sortBy,
        sortDirection: state.sortDirection,
        asset: "USDT, ETH",
      },
      ...state.filter,
    };
  } else {
    apiParams = {
      ...{
        sortBy: state.sortBy,
        sortDirection: state.sortDirection,
      },
      ...state.filter,
    };
  }

  const apiQueryParams = new URLSearchParams();
  Object.entries(apiParams).forEach(([key, value]) =>
    apiQueryParams.append(key, value)
  );

  const {
    data: transactionData,
    isFetching: isFetchingTransactions,
    error: errorTransactions,
  } = useRefundTransactions(
    `page=${state.page - 1}&${apiQueryParams.toString()}`
  );

  useEffect(() => {
    if (merchantDetails?.role) {
      setMerchantType(merchantDetails.role);
    }
  }, [merchantDetails]);

  const { data: transactionDataNext } = useRefundTransactions(
    `page=${state.page}&${apiQueryParams.toString()}`
  );

  const { data: transactionDataNoPage } = useRefundTransactionsNoPage(
    `${apiQueryParams.toString()}`
  );

  const [loading, setLoading] = useState(isFetchingTransactions);

  const FilterSection = useRefundFilterSelectField({
    submitFilter: (filters) => {
      // setState({ ...state, ...{ filter: filters } })
      setState({
        ...state,
        ...{ page: 1 },
        ...{ filter: filters },
      });
    },
    filters,
    csvDownload: () => {
      downloadTransactionsCSV();
    },
    pdfDownload: () => {
      downloadTransactionsPDF();
    },
  });

  useEffect(() => {
    setLoading(isFetchingTransactions);
  }, [isFetchingTransactions]);

  useEffect(() => {
    setSearchParams({
      ...(state.page !== defaults.page && {
        page: `${state.page}`,
      }),
      ...(state.sortBy !== defaults.sortBy && {
        sortBy: state.sortBy,
      }),
      ...(state.sortDirection !== defaults.sortDirection && {
        sortDirection: state.sortDirection,
      }),
      ...state.filter,
    });
  }, [state.page, state.sortBy, state.sortDirection, state.filter]);

  const sortData = (value: string) => {
    setLoading(true);
    if (value !== state.sortBy) {
      setState({
        ...state,
        ...{ page: 1, sortBy: value, sortDirection: "ASC" },
      });
    } else if (state.sortDirection === "ASC") {
      setState({ ...state, ...{ page: 1, sortDirection: "DESC" } });
    } else {
      setState({ ...state, ...{ page: 1, sortDirection: "ASC" } });
    }
  };

  const SortIcon = useSortIcons({
    sortData,
    sortedField: state.sortBy,
    sortedDirection: state.sortDirection,
  });

  const navigatePage = (transactionId: string) => {
    navigate(`${RouteType.TRANSACTION_DETAIL}/${transactionId}`, {
      state: { from: location.pathname + location.search },
    });
  };

  const downloadTransactionsCSV = () => {
    if (transactionDataNoPage) {
      let data = [...transactionDataNoPage];
      data = JSON.parse(
        JSON.stringify(data, [
          "uuid",
          "extPosTransactionId",
          "extPosActualDate",
          "extPosActualTime",
          "createdAt",
          "asset",
          "totalDigitalCurrencyReceived",
          "totalFiatReceived",
          "refundFiatAmount",
          "fiatAsset",
          "refundMode",
          "refundStatus",
        ])
      );
      JSONToCSVConvertor(data, "Transactions", true);
    }
  };

  const downloadTransactionsPDF = () => {
    if (transactionDataNoPage) {
      let timezone: any;
      const localStorageTime = localStorage.getItem("TimeZone");
      if (localStorageTime) {
        timezone = JSON.parse(localStorageTime).value || "Asia/Dubai";
      } else {
        timezone = "Asia/Dubai";
      }
      const currentData = [...transactionDataNoPage];
      const head = [
        [
          "Sl.No",
          "Transaction ID",
          "Acquirer Transaction ID",
          "Date",
          "Asset",
          "Received Digital Amount",
          "Received Fiat Amount",
          "Refund Value",
          "Refund Mode",
        ],
      ];
      const finalData: any = [];
      currentData.map((item, index) => {
        const arr = [];
        arr.push(index + 1);
        if (Object.prototype.hasOwnProperty.call(item, "uuid")) {
          arr.push(item.uuid);
        }
        if (Object.prototype.hasOwnProperty.call(item, "extPosTransactionId")) {
          arr.push(item.extPosTransactionId);
        }
        if (Object.prototype.hasOwnProperty.call(item, "createdAt")) {
          arr.push(dayjs(item.createdAt).format("DD-MM-YYYY HH:MM:ss"));
        }
        if (Object.prototype.hasOwnProperty.call(item, "asset")) {
          arr.push("SAR*");
        }
        if (
          Object.prototype.hasOwnProperty.call(
            item,
            "totalDigitalCurrencyReceived"
          )
        ) {
          arr.push(item.totalDigitalCurrencyReceived);
        }
        if (Object.prototype.hasOwnProperty.call(item, "totalFiatReceived")) {
          arr.push(`${item.fiatAsset} ${item.totalFiatReceived}`);
        }
        if (Object.prototype.hasOwnProperty.call(item, "refundFiatAmount")) {
          arr.push(item.refundStatus == "REFUNDED" ? item.refundFiatAmount : 0);
        }
        if (Object.prototype.hasOwnProperty.call(item, "refundMode")) {
          if (item.refundStatus === "REFUNDED") {
            arr.push(item.refundMode);
          } else {
            arr.push("NA");
          }
        }
        finalData.push(arr);
      });
      const doc = new jsPDF();
      autoTable(doc, {
        head: head,
        body: finalData,
        columnStyles: {
          0: { cellWidth: 10 },
          1: { cellWidth: 20 },
          2: { cellWidth: 20 },
          3: { cellWidth: 20 },
        },
      });

      doc.save("myReports-Transactions.pdf");
    }
  };

  const JSONToCSVConvertor = (
    JSONData: any,
    ReportTitle: string,
    ShowLabel: boolean
  ) => {
    const arrData =
      typeof JSONData !== "object" ? JSON.parse(JSONData) : JSONData;
    let CSV = "";
    let timezone: any;
    const localStorageTime = localStorage.getItem("TimeZone");
    if (localStorageTime) {
      timezone = JSON.parse(localStorageTime).value || "Asia/Dubai";
    } else {
      timezone = "Asia/Dubai";
    }
    const headersRequried = [
      "uuid",
      "extPosTransactionId",
      "extPosLogicalDate",
      "extPosActualDate",
      "extPosActualTime",
      "extPosShift",
      "createdAt",
      "asset",
      "totalDigitalCurrencyReceived",
      "totalFiatReceived",
      "refundFiatAmount",
      "refundMode",
    ];
    if (ShowLabel) {
      let row = "";
      for (const index in arrData[0]) {
        switch (index) {
          case "uuid":
            row += "Transaction ID" + ",";
            break;
          case "extPosTransactionId":
            row += "Acquirer Transaction ID" + ",";
            break;
          case "extPosLogicalDate":
            row += "Acquirer Logical Date" + ",";
            break;
          case "extPosActualDate":
            row += "Actual Date" + ",";
            break;
          case "extPosActualTime":
            row += "Actual Time" + ",";
            break;
          case "extPosShift":
            row += "Acquirer Shift" + ",";
            break;
          case "createdAt":
            row += "Date" + ",";
            break;
          case "asset":
            row += "Asset" + ",";
            break;
          case "totalDigitalCurrencyReceived":
            row += "Received Digital Amount" + ",";
            break;
          case "totalFiatReceived":
            row += "Received Fiat Amount" + ",";
            break;
          case "refundFiatAmount":
            row += "Refund Value" + ",";
            break;
          case "refundMode":
            row += "Refund Mode" + ",";
            break;
          default:
            break;
        }
      }
      row = row.slice(0, -1);
      CSV += row + "\r\n";
    }
    for (let i = 0; i < arrData.length; i++) {
      let row = "";
      for (const index in arrData[i]) {
        if (headersRequried.includes(index)) {
          if (index == "createdAt") {
            row +=
              '"' +
              dayjs(arrData[i][index]).format("DD-MM-YYYY HH:MM:ss") +
              '",';
          } else if (index == "extPosLogicalDate") {
            if (arrData[i][index]) {
              row +=
                '"' +
                dayjs(arrData[i][index])
                  .tz(timezone)
                  .format("DD-MM-YYYY HH:MM:ss") +
                '",';
            } else {
              row += '"",';
            }
          } else if (index == "extPosActualDate") {
            if (arrData[i][index]) {
              row +=
                '"' +
                dayjs(arrData[i][index]).tz(timezone).format("DD-MM-YYYY") +
                '",';
            } else {
              row += '"",';
            }
          } else if (index == "extPosActualTime") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index] + '",';
            } else {
              row += '"",';
            }
          } else if (index == "extPosShift") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index] + '",';
            } else {
              row += '"",';
            }
          } else if (
            index == "feeAmount" ||
            index == "totalDigitalCurrencyReceived" ||
            index == "amount"
          ) {
            row += '"' + arrData[i][index] + '",';
          } else if (index == "totalFiatReceived") {
            row += `${arrData[i]["fiatAsset"]} ${arrData[i][index]} ,`;
          } else if (index == "refundFiatAmount") {
            row += `${
              arrData[i]["refundStatus"] === "REFUNDED"
                ? arrData[i][index]
                : "0"
            },`;
          } else if (index === "refundMode") {
            row += `${
              arrData[i]["refundStatus"] === "REFUNDED"
                ? arrData[i]["refundMode"]
                : "NA"
            },`;
          } else if (index === "asset") {
            row += '"SAR*",';
          } else {
            row += '"' + arrData[i][index] + '",';
          }
        }
      }
      row.slice(0, row.length - 1);
      CSV += row + "\r\n";
    }
    if (CSV === "") {
      return;
    }
    let fileName = "MyReport_";
    fileName += ReportTitle.replace(/ /g, "_");
    const uri = "data:text/csv;charset=utf-8,%EF%BB%BF" + encodeURI(CSV);
    const link = document.createElement("a");
    link.href = uri;
    link.style.visibility = "hidden";
    link.download = fileName + ".csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };
  const paginate = (page: number) => {
    setLoading(true);
    setState({ ...state, ...{ page } });
  };

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
        );
        break;
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
        );
        break;
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
        );
        break;
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
        );
        break;
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
        );
        break;
      default:
        return iconName;
        break;
    }
  };

  const dateFormat = (time: any) => {
    const localStorageTime = localStorage.getItem("TimeZone");
    if (localStorageTime) {
      const timezone = JSON.parse(localStorageTime).value || "Asia/Dubai";
      return dayjs(time).tz(timezone).format("DD-MM-YYYY");
    } else {
      const timezone = "Asia/Dubai";
      return dayjs(time).tz(timezone).format("DD-MM-YYYY");
    }
  };

  const dateTimeFormat = (time: any) => {
    if (!time) {
      return null;
    }
    const localStorageTime = localStorage.getItem("TimeZone");
    if (localStorageTime) {
      const timezone = JSON.parse(localStorageTime).value || "Asia/Dubai";
      return dayjs(time).tz(timezone).format("DD-MM-YYYY HH:mm:ss");
    } else {
      return dayjs(time).tz("Asia/Dubai").format("DD-MM-YYYY HH:mm:ss");
    }
  };

  const trxIdReturn = (obj: any) => {
    if (obj.description && obj.uuid == "") {
      return null;
    }

    if (obj.description && obj.uuid == null) {
      return null;
    }

    if (
      obj.description &&
      obj.description.includes("-") &&
      obj.description.length == 36
    ) {
      return obj.description;
    }

    return obj.uuid;
  };

  const checkRefundBtnType = (trx: any) => {
    const type = trx.refundStatus;
    const id = trxIdReturn(trx);
    if (type == null || type == "NULL") {
      return (
        <InitiateRefundPopupModal
          txt="Initiate"
          color="btn btn-success btn-sm"
          id={id}
          rowtrx={trx}
          disabled={false}
          apicallstatus={() => window.location.reload()}
        />
      );
    }
    if (
      type == "REFUND_ACCEPTED" ||
      type == "REFUND_HOLD" ||
      type == "REFUND_APPROVED"
    ) {
      return (
        <InitiateRefundPopupModal
          txt="CVF Submitted"
          color="btn-sm btn btn-primary btn-sm"
          id={id}
          rowtrx={trx}
          disabled={true}
        />
      );
    }
    if (type == "REFUNDED") {
      return (
        <InitiateRefundPopupModal
          txt="Refunded"
          color="btn btn-sm refundbtn refundbtnOpacity"
          id={id}
          rowtrx={trx}
          disabled={true}
        />
      );
    }
    if (type == "REFUND_INITIATED") {
      return (
        <InitiateRefundPopupModal
          txt="Weblink Sent"
          color="btn btn-primary btn-sm refundbtnOpacity"
          id={id}
          rowtrx={trx}
          disabled={true}
        />
      );
    }
    if (type == "REFUND_FAILED") {
      return (
        <InitiateRefundPopupModal
          txt="Reinitiate Failed"
          color="btn btn-success btn-sm"
          id={id}
          rowtrx={trx}
          apicallstatus={() => window.location.reload()}
        />
      );
    }
    if (type == "REFUND_CANCELED") {
      return (
        <OverlayTrigger
          placement="left"
          background-color="white"
          overlay={
            trx.refundStatus === "REFUND_CANCELED" ? (
              <Tooltip background-color="white" id="tooltip-disabled">
                {trx.refundAcceptanceComment}
              </Tooltip>
            ) : (
              <span></span>
            )
          }
        >
          <div>
            <InitiateRefundPopupModal
              txt="Reinitiate Rejected"
              color="btn btn-success btn-sm"
              id={id}
              rowtrx={trx}
              tooltip="test"
              apicallstatus={() => window.location.reload()}
            />
          </div>
        </OverlayTrigger>
      );
    }
    if (type == "REFUND_EXPIRED") {
      return (
        <InitiateRefundPopupModal
          txt="Reinitiate WL Expired"
          color="btn btn-success btn-sm"
          id={id}
          rowtrx={trx}
          apicallstatus={() => window.location.reload()}
        />
      );
    }
  };

  const renderAcceptRejectButtons = () => {
    if (
      merchantType == "MERCHANT_MERCHANT" ||
      merchantType == "MERCHANT_ADMIN" ||
      merchantType == "MERCHANT_READER"
    ) {
      return (
        <>
          <Button
            className="btn wdz-btn-primary"
            variant="warning"
            onClick={() => navigate(RouteType.REFUND_REQUEST_ACCEPTANCE)}
          >
            {t("Refund Acceptance")}
          </Button>
          {/* <Button
            className="btn wdz-btn-primary"
            variant="warning"
            style={{ marginLeft: "10px" }}
            onClick={() => navigate(RouteType.REFUND_REQUEST_APPROVAL)}
          >
            {t("Refund Approval")}
          </Button> */}
        </>
      );
    }
    if (merchantType == "MERCHANT_SUPERVISOR") {
      return (
        <Button
          className="btn wdz-btn-primary"
          variant="warning"
          onClick={() => navigate(RouteType.REFUND_REQUEST_ACCEPTANCE)}
        >
          {t("Refund Acceptance")}
        </Button>
      );
    }
    return null;
  };

  return (
    <>
      <h3>Refund</h3>
      <Card>
        <div
          style={{ display: "flex", justifyContent: "space-between" }}
          className="mt-1"
        >
          <FilterSection />
        </div>
        <div className="table-responsive mt-4" style={{ overflowX: "auto" }}>
          <table className="table transactions-table">
            <thead>
              <tr>
                <th scope="col">{t("Refund Acquirer Actual Date")}</th>
                <th scope="col">{t("Refund Acquirer Actual Time")}</th>
                <th scope="col">{t("Refund Transaction ID")}</th>
                <th scope="col">{t("Merchant ID / PoS Terminal ID")}</th>
                {/*<SortIcon
                  element="extPosLogicalDate"
                  sortField="CREATED_AT"
                  wordLabel="Acquirer Transaction ID"
                />*/}
                <th scope="col">{t("Transaction ID")}</th>
                <th scope="col">{t("No of Times Refund Done")}</th>
                {/* <th scope="col">{t("NoOf TimesRefund Done")}</th> */}
                <SortIcon
                  element="Date"
                  sortField="CREATED_AT"
                  wordLabel="Date & Time"
                />
                <SortIcon element="Asset" sortField="ASSET" wordLabel="Asset" />
                <SortIcon
                  element="Amount"
                  sortField="AMOUNT"
                  wordLabel="Received Digital Amount"
                />
                <th scope="col" style={{ textAlign: "center" }} colSpan={2}>
                  <SortIcon
                    element="totalFiatReceived"
                    sortField="TOTALFIATRECEIVED"
                    wordLabel="Received Fiat Amount"
                  />
                </th>
                <th scope="col" style={{ textAlign: "center" }} colSpan={2}>{t("Refundable Amount in Fiat")}</th>
                <th scope="col" style={{ textAlign: "center" }} colSpan={2}>{t("Refund Amount in Fiat")}</th>
                <th scope="col" style={{ textAlign: "center" }} colSpan={2}>{t("Balance Fiat Amount in Fiat")}</th>
                <th scope="col">{t("Transaction Type")}</th>
                <th scope="col">{t("Received Payment Date & Time")}</th>
                <th scope="col">{t("Status")}</th>
                <th scope="col">{t("Refund Type")}</th>
                {/*<SortIcon
                  element="Status"
                  sortField="STATUS"
                  wordLabel="Status"
                />*/}
                <SortIcon
                  element="refundAmountFiat"
                  sortField="REFUNDAMOUNTFIAT"
                  wordLabel={t("Refund Value(Fiat)")}
                />
                <SortIcon
                  element="refundAmountDigital"
                  sortField="REFUNDAMOUNTDIGITAL"
                  wordLabel={t(`Refund Value in ${institutionDetails?.merchantCurrency}*`)}
                />
                <th scope="col" style={{ textAlign: "right" }}>
                  {t("Refund Mode")}
                </th>
                <th scope="col" style={{ textAlign: "center" }}>
                  {t("Refund initiated from POS/MD")}
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
              </tr>
            </thead>
            <tbody>
              {!errorTransactions &&
                transactionData &&
                transactionData.length > 0 &&
                transactionData.map((transaction: Transaction) => [
                  <tr
                    key={transaction.id}
                    // onClick={() => navigatePage(transaction.id)}
                    // style={{ cursor: "pointer" }}
                  >
                    <td>
                      {transaction.extPosActualDateRefund
                        ? dateFormat(transaction.extPosActualDateRefund)
                        : ""}
                    </td>
                    <td>{transaction.extPosActualTimeRefund}</td>
                    <td>
                      {transaction?.refundStatus === "REFUND_EXPIRED" ||
                      transaction?.refundStatus === "REFUND_CANCELED" ||
                      transaction?.refundStatus === "REFUND_FAILED"
                        ? ""
                        : transaction.refundTransactionID}
                    </td>
                    <td style={{ wordBreak: "break-all" }}>
                      {merchantDetails?.merchant?.merchantId} / {transaction.posId}
                    </td>
                    <td style={{ wordBreak: "break-all" }}>
                      {/* {transaction.direction === "INCOMING"
                        ? transaction.senderName
                        : transaction.receiverName} */}
                      {trxIdReturn(transaction)}
                    </td>
                    <td>{transaction.numberOfRefunds}</td>
                    <td>
                      {transaction.createdAt != undefined
                        ? dateTimeFormat(transaction.createdAt)
                        : ""}
                    </td>
                    <td>
                      {/* <img
                        src={"/images/" + transaction.asset + ".svg"}
                        alt={transaction.asset}
                        width="20px"
                        height="20px"
                        style={{ marginRight: "5px" }}
                      /> */}
                      <span className="fiatAssetFontSize">
                        {transaction.asset && transaction.asset == "SART"
                          ? "SAR*"
                          : transaction.asset}
                      </span>
                    </td>
                    
                    <td>
                      {formatter(transaction.totalDigitalCurrencyReceived, {
                        asset: transaction.asset,
                      })}
                    </td>
                    <td style={{ paddingRight: "0" }}>
                      <span className="fiatAssetFontSize">
                        {transaction.fiatAsset}
                      </span>
                    </td>
                    <td style={{ paddingLeft: "0" }}>
                      <span style={{ float: "left" }}>
                        {transaction.totalFiatReceived &&
                          transaction.totalFiatReceived.toFixed(2)}
                      </span>
                    </td>
                    <td style={{ paddingRight: "0" }}>
                      <span className="fiatAssetFontSize">
                        {transaction.fiatAsset}
                      </span>
                    </td>
                    <td style={{ paddingLeft: "0" }}>
                    <span style={{ float: "left" }}>
                      {transaction.refundableAmountFiat &&
                        transaction.refundableAmountFiat.toFixed(2)}
                        </span>
                    </td>
                    <td style={{ paddingRight: "4px" }}>
                      <span className="fiatAssetFontSize">
                        {transaction.fiatAsset}
                      </span>
                    </td>
                    <td style={{ paddingLeft: "0" }}>
                    <span style={{ float: "left" }}>
                      {transaction.refundFiatAmount &&
                        transaction.refundFiatAmount.toFixed(2)}
                        </span>
                    </td>
                    <td style={{ paddingRight: "0" }}>
                      <span className="fiatAssetFontSize">
                        {transaction.fiatAsset}
                      </span>
                    </td>
                    <td style={{paddingLeft: "6px"}}>
                    <span style={{ float: "left" }}>
                      {transaction?.refundStatus == "NULL"
                        ? "NA"
                        : (
                            transaction?.refundableAmountFiat -
                            transaction?.refundFiatAmount
                          ).toFixed(2)}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      {transaction.transactionType}
                    </td>
                    <td>
                      {transaction.paymentReceivedDate != undefined
                        ? dateTimeFormat(transaction.paymentReceivedDate)
                        : ""}
                    </td>
                    <td>{transaction.status}</td>
                    <td>
                      {transaction.refundType ? transaction.refundType : "NA"}
                    </td>
                    <td>
                      <span style={{ padding: "5px" }}>
                        {transaction.refundFiatAmount &&
                        transaction?.refundStatus === "REFUNDED"
                          ? transaction.refundFiatAmount.toFixed(2)
                          : 0}
                      </span>
                      <span style={{ float: "left" }}>
                        {transaction.fiatAsset}
                      </span>
                    </td>
                    <td>
                      <span style={{ float: "right" }}>
                        {transaction.refundAmountDigital &&
                        transaction?.refundStatus === "REFUNDED"
                          ? transaction.refundAmountDigital == 0
                            ? 0
                            : transaction.refundAmountDigital.toFixed(2)
                          : 0}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <span>
                        {transaction.refundMode &&
                        transaction?.refundStatus === "REFUNDED"
                          ? transaction.refundMode
                          : "NA"}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      {transaction.refundOrigin}
                    </td>
                    <td style={{ textAlign: "center" }}>
                      {checkRefundBtnType(transaction)}
                    </td>
                  </tr>,
                ])}
              <tr className="transaction-count">
                <td colSpan={21}>
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
      </Card>

      <RefundButtonStatus
        show={show}
        setShow={(value?: boolean) => setShow(value || false)}
      />
    </>
  );
};

export default RefundTransactions;

import React, { useEffect, useState, useContext } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { RouteType } from "src/constants/routeTypes";
import {
  useMerchantTransactions,
  useMerchantTransactionsNoPage,
} from "src/api/user";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import tz from "dayjs/plugin/timezone";
import { Dropdown } from "react-bootstrap";
import jsPDF from "jspdf";
import "jspdf-autotable";
import autoTable from "jspdf-autotable";
import { Transaction } from "src/api/models";
import useFormatCurrencyAmount from "src/helpers/formatCurrencyAmount";
import Pagination from "src/helpers/pagination";
import { useSortIcons } from "src/components/ui/useSortIcons";
import { useFilterSelectField } from "src/components/ui/useFilterSelectField";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Tooltip from "react-bootstrap/Tooltip";
import { date, string } from "yup";
import TransactionBalance from "src/components/ui/TransactionsBalance";
import ar from "date-fns/esm/locale/ar/index.js";
import Card from "src/helpers/Card";
import { UserContext } from "src/context/User";
import { MerchantContext } from "src/context/Merchant";

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
];

const RecentTransactions: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const formatter = useFormatCurrencyAmount();
  const { user } = useContext(UserContext);
  const [searchParams, setSearchParams] = useSearchParams();
  const { merchantDetails, institutionDetails } = useContext(MerchantContext)

  dayjs.extend(utc);
  dayjs.extend(tz);

  const filters: { [key: string]: string } = {};
  searchParams.forEach((value, key) => {
    if (filterKeys.includes(key)) {
      filters[key] = value;
    }
  });

  const [isShown, setIsShown] = useState(false);

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
  } = useMerchantTransactions(
    `page=${state.page - 1}&${apiQueryParams.toString()}`
  );

  const { data: transactionDataNext } = useMerchantTransactions(
    `page=${state.page}&${apiQueryParams.toString()}`
  );

  const { data: transactionDataNoPage } = useMerchantTransactionsNoPage(
    `${apiQueryParams.toString()}`
  );

  const [loading, setLoading] = useState(isFetchingTransactions);

  const FilterSection = useFilterSelectField({
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
          "extPosLogicalDate",
          "extPosActualDate",
          "extPosActualTime",
          "extPosShift",
          "uuid",
          "extPosTransactionId",
          "fiatAmount",
          "fiatAsset",
          "amount",
          "requestedDigitalAmount",
          "totalDigitalCurrencyReceived",
          "asset",
          "totalFiatReceived",
          "paymentReceivedDate",
          "status",
        ])
      );
      JSONToCSVConvertor(data, "Transactions", true);
    }
  };

  const downloadTransactionsPDF = () => {
    const localStorageTime = localStorage.getItem("TimeZone");
    let timezone: any;
    if (localStorageTime) {
      timezone = JSON.parse(localStorageTime).value || "Asia/Dubai";
    } else {
      timezone = "Asia/Dubai";
    }
    if (transactionDataNoPage) {
      const currentData = [...transactionDataNoPage];
      const head = [
        [
          "Sl.No",
          "Transaction ID",
          "Acquirer Transaction ID",
          "Order Fiat Amount",
          "Order Digital Amount",
          "Requested Digital Amount",
          "Received Digital Amount",
          "Asset",
          "Received Fiat Amount",
          "Received Payment Date",
          "Status",
        ],
      ];
      const finalData: any = [];
      currentData?.map((item, index) => {
        const arr = [];
        arr.push(index + 1);
        if (Object.prototype.hasOwnProperty.call(item, "uuid")) {
          arr.push(item.uuid);
        }

        if (Object.prototype.hasOwnProperty.call(item, "posId")) {
          console.log("posId ========================")
          if (item.posId) {
            arr.push(
              `${item.posId ? item.posId : ""}`
            );
          } else {
            arr.push("");
          }
        }
        if (Object.prototype.hasOwnProperty.call(item, "extPosTransactionId")) {
          if (item.extPosTransactionId) {
            arr.push(
              `${item.extPosTransactionId ? item.extPosTransactionId : ""}`
            );
          } else {
            arr.push("");
          }
        }
        {
          /* if (Object.prototype.hasOwnProperty.call(item, "extPosId")) {
          if (item.extPosId) {
            arr.push(`${item.extPosId ? item.extPosId : ""}`)
          } else {
            arr.push("")
          }
        }
        if (Object.prototype.hasOwnProperty.call(item, "extPosSequenceNo")) {
          if (item.extPosSequenceNo) {
            arr.push(item.extPosSequenceNo)
          } else {
            arr.push("")
          }
        } */
        }
        if (Object.prototype.hasOwnProperty.call(item, "fiatAmount")) {
          if (item.fiatAmount) {
            arr.push(
              `${item.fiatAsset ? item.fiatAsset : ""}${" "}${
                item.fiatAmount ? item.fiatAmount : ""
              }`
            );
          } else {
            arr.push("");
          }
        }
        if (Object.prototype.hasOwnProperty.call(item, "amount")) {
          if (item.amount) {
            arr.push(`${item.amount ? item.amount : ""}`);
          } else {
            arr.push("");
          }
        }
        if (
          Object.prototype.hasOwnProperty.call(item, "requestedDigitalAmount")
        ) {
          if (item.requestedDigitalAmount) {
            arr.push(
              `${
                item.requestedDigitalAmount ? item.requestedDigitalAmount : ""
              }`
            );
          } else {
            arr.push("");
          }
        }
        if (
          Object.prototype.hasOwnProperty.call(
            item,
            "totalDigitalCurrencyReceived"
          )
        ) {
          if (item.totalDigitalCurrencyReceived) {
            arr.push(
              `${
                item.totalDigitalCurrencyReceived
                  ? item.totalDigitalCurrencyReceived
                  : ""
              }`
            );
          } else {
            arr.push("");
          }
        }
        if (Object.prototype.hasOwnProperty.call(item, "asset")) {
          arr.push("SAR*");
        }
        if (Object.prototype.hasOwnProperty.call(item, "totalFiatReceived")) {
          if (item.totalFiatReceived) {
            arr.push(
              ` ${item.fiatAsset ? item.fiatAsset : ""}${" "}${
                item.totalFiatReceived ? item.totalFiatReceived : ""
              }`
            );
          } else {
            arr.push("");
          }
        }
        if (Object.prototype.hasOwnProperty.call(item, "paymentReceivedDate")) {
          if (item.paymentReceivedDate) {
            arr.push(
              `${
                item.paymentReceivedDate
                  ? dayjs(item.paymentReceivedDate)
                      .tz(timezone)
                      .format("DD-MM-YYYY HH:mm:ss")
                  : ""
              }`
            );
          } else {
            arr.push("");
          }
        }
        if (Object.prototype.hasOwnProperty.call(item, "status")) {
          if (item.status) {
            arr.push(item.status);
          } else {
            arr.push("");
          }
        }
        finalData.push(arr);
      });
      const doc = new jsPDF("landscape");
      autoTable(doc, {
        tableLineColor: [189, 195, 199],
        tableLineWidth: 0.5,
        theme: "grid",
        headStyles: { fillColor: [255, 183, 48] },
        head: head,
        body: finalData,
        columnStyles: {
          0: { cellWidth: 10 },
          2: { cellWidth: 22 },
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
    const headersRequried = [
      "extPosLogicalDate",
      "extPosActualDate",
      "extPosActualTime",
      "extPosShift",
      "uuid",
      "extPosTransactionId",
      "fiatAmount",
      "amount",
      "requestedDigitalAmount",
      "totalDigitalCurrencyReceived",
      "asset",
      "totalFiatReceived",
      "paymentReceivedDate",
      "status",
    ];
    const localStorageTime = localStorage.getItem("TimeZone");
    let timezone;
    if (localStorageTime) {
      timezone = JSON.parse(localStorageTime).value || "Asia/Dubai";
    } else {
      timezone = "Asia/Dubai";
    }
    const arrData =
      typeof JSONData !== "object"
        ? JSON.parse(JSON.stringify(JSONData, headersRequried))
        : JSONData;
    let CSV = "";
    if (ShowLabel) {
      let row = "";
      for (const index in arrData[0]) {
        switch (index) {
          case "extPosLogicalDate":
            row += "POS Logical Date" + ",";
            break;
          case "extPosActualDate":
            row += "Actual Date" + ",";
            break;
          case "extPosActualTime":
            row += "Actual Time" + ",";
            break;
          case "extPosShift":
            row += "POS Shift" + ",";
            break;
          case "uuid":
            row += "Transaction ID" + ",";
            break;
          case "extPosTransactionId":
            row += "POS Transaction ID" + ",";
            break;
          case "fiatAmount":
            row += "Order Fiat Amount" + ",";
            break;
          case "amount":
            row += "Order Digital Amount" + ",";
            break;
          case "requestedDigitalAmount":
            row += "Requested Digital Amount" + ",";
            break;
          case "totalDigitalCurrencyReceived":
            row += "Received Digital Amount" + ",";
            break;
          case "asset":
            row += "Asset" + ",";
            break;
          case "totalFiatReceived":
            row += "Received Fiat Amount" + ",";
            break;
          case "paymentReceivedDate":
            row += "Received Payment Date" + ",";
            break;
          case "status":
            row += "Status" + ",";
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
          if (index == "extPosLogicalDate") {
            if (arrData[i][index]) {
              row +=
                '"' +
                dayjs(arrData[i][index])
                  .tz(timezone)
                  .format("DD-MM-YYYY HH:mm:ss") +
                '",';
            } else {
              row += '"",';
            }
          }
          if (index == "extPosActualDate") {
            if (arrData[i][index]) {
              row +=
                '"' +
                dayjs(arrData[i][index]).tz(timezone).format("DD-MM-YYYY") +
                '",';
            } else {
              row += '"",';
            }
          }
          if (index == "extPosActualTime") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index] + '",';
            } else {
              row += '"",';
            }
          }
          if (index == "extPosShift") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index] + '",';
            } else {
              row += '"",';
            }
          }
          if (index == "uuid") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index] + '",';
            } else {
              row += '"",';
            }
          }
          if (index == "extPosTransactionId") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index] + '",';
            } else {
              row += '"",';
            }
          }
          if (index == "fiatAmount") {
            if (arrData[i][index]) {
              row +=
                '"' +
                `${arrData[i]["fiatAsset"]} ` +
                arrData[i][index].toFixed(2) +
                '",';
            } else {
              row += '"",';
            }
          }
          if (index == "amount") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index].toFixed(8) + '",';
            } else {
              row += '"",';
            }
          }
          if (index == "requestedDigitalAmount") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index].toFixed(8) + '",';
            } else {
              row += '"",';
            }
          }
          if (index == "totalDigitalCurrencyReceived") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index].toFixed(8) + '",';
            } else {
              row += '"",';
            }
          }
          if (index == "asset") {
            if (arrData[i][index]) {
              row += '"SAR*",';
            } else {
              row += '"",';
            }
          }
          if (index == "totalFiatReceived") {
            if (arrData[i][index]) {
              row +=
                '"' +
                `${arrData[i]["fiatAsset"]} ` +
                arrData[i][index].toFixed(2) +
                '",';
            } else {
              row += '"",';
            }
          }
          if (index == "paymentReceivedDate") {
            if (arrData[i][index]) {
              row +=
                '"' +
                dayjs(arrData[i][index])
                  .tz(timezone)
                  .format("DD-MM-YYYY HH:mm:ss") +
                '",';
            } else {
              row += '"",';
            }
          }
          if (index == "status") {
            if (arrData[i][index]) {
              row += '"' + arrData[i][index] + '",';
            } else {
              row += '"",';
            }
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
            placement="left"
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
        );
        break;
      case "FAILED":
        return (
          <OverlayTrigger
            placement="left"
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
            placement="left"
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
            placement="left"
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
    if (!time) {
      return null;
    }
    return dayjs(time).format("DD-MM-YYYY");
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

  const timeFormat = (time: any) => {
    if (!time) {
      return null;
    }
    return time.replaceAll(":", "-");
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

  return (
    <>
      <TransactionBalance trxdata={transactionDataNoPage} />
      <Card>
        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <FilterSection />
        </div>
        <div className="table-responsive mt-4" style={{ overflowX: "auto" }}>
          <table className="table table-hover sales-transactions-table">
            <thead>
              <tr>
                <th scope="col">{t("Issuer Name")}</th>
                <th scope="col">{t("Actual Date")}</th>
                <th scope="col">{t("Actual Time")}</th>
                <th scope="col">{t("Transaction ID")}</th>
                <th style={{ textAlign: "center" }}>
                  {t("Merchant ID / PoS Terminal ID")}
                </th>
                <th colSpan={2} scope="col" style={{ textAlign: "center" }}>
                  {t("Order Fiat Amount")}
                </th>
                <th colSpan={2} scope="col" style={{ textAlign: "center" }}>
                  <SortIcon
                    element="Amount"
                    sortField="AMOUNT"
                    wordLabel="Order Digital Amount"
                  />
                </th>
                {/* <th scope="col" style={{ textAlign: "right" }}>
                  {t("Fee Amount")}
                </th> */}
                <th colSpan={2} scope="col" style={{ textAlign: "center" }}>
                  <SortIcon
                    element="Amount"
                    sortField="AMOUNT"
                    wordLabel="Requested Digital Amount"
                  />
                </th>
                <th scope="col" colSpan={2} style={{ textAlign: "center" }}>
                  {t("Received Digital Amount")}
                </th>
                <th colSpan={2} scope="col" style={{ textAlign: "center" }}>
                  {t("Received Fiat Amount")}
                </th>
                <th style={{ textAlign: "center" }}>
                  {t("Received Payment Date & Time")}
                </th>
                <th style={{ textAlign: "center" }}>{t("Transaction Mode")}</th>
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
                    <td>{transaction.issuerName}</td>
                    <td>{dateFormat(transaction.extPosActualDate)}</td>
                    <td>{transaction.extPosActualTime}</td>
                    <td style={{ wordBreak: "break-all" }}>
                      {/* {transaction.direction === "INCOMING"
                        ? transaction.senderName
                        : transaction.receiverName} */}
                      {trxIdReturn(transaction)}
                    </td>
                    <td style={{ wordBreak: "break-all" }}>
                    {merchantDetails?.merchant?.merchantId} / {transaction.posId}
                    </td>
                    <td style={{ paddingRight: "0" }}>
                      <span className="fiatAssetFontSize">
                        {transaction.fiatAsset}
                      </span>
                    </td>
                    <td style={{ paddingLeft: "0" }}>
                      <span style={{ float: "left" }}>
                        {transaction.fiatAmount &&
                          transaction.fiatAmount.toFixed(2)}
                      </span>
                    </td>
                    <td style={{ paddingRight: "0" }}>
                      <span className="fiatAssetFontSize">{institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+"* " : ""}</span>
                    </td>
                    <td style={{ paddingLeft: "0" }}>
                      <span style={{ float: "left" }}>
                        {formatter(transaction.amount, {
                          asset: transaction.asset,
                        })}
                      </span>
                    </td>
                    {/* <td>
                      {formatter(transaction.feeAmount, {
                        asset: transaction.asset
                      })}
                    </td> */}
                    <td style={{ paddingRight: "0" }}>
                      <span className="fiatAssetFontSize">{institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+"* " : ""}</span>
                    </td>
                    <td style={{ paddingLeft: "0" }}>
                      <span style={{ float: "left" }}>
                        {transaction.requestedDigitalAmount &&
                          transaction.requestedDigitalAmount.toFixed(2)}
                      </span>
                    </td>
                    <td style={{ paddingRight: "0" }}>{institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+"* " : ""}</td>
                    <td
                      style={{ paddingLeft: "0" }}
                      className={`text-nowrap ${
                        transaction.direction === "INCOMING"
                          ? "text-success"
                          : "text-danger"
                      }`}
                    >
                      <span style={{ float: "left" }}>
                        {transaction.direction === "OUTGOING" && "-"}
                        {formatter(transaction.totalDigitalCurrencyReceived, {
                          asset: transaction.asset,
                        })}
                      </span>
                    </td>
                    <td style={{ paddingRight: "0" }}>
                      <span
                        style={{ float: "right" }}
                        className="fiatAssetFontSize"
                      >
                        {transaction.totalFiatReceived && transaction.fiatAsset}
                      </span>
                    </td>
                    <td style={{ paddingLeft: "5" }}>
                      <span style={{ float: "left" }}>
                        {transaction.totalFiatReceived &&
                          transaction.totalFiatReceived.toFixed(2)}
                      </span>
                    </td>

                    <td>{dateTimeFormat(transaction.paymentReceivedDate)}</td>
                    <td>
                      {transaction.txnMode == "MERCHANT_OFFLINE"
                        ? "Merchant Offline"
                        : transaction.txnMode == "MERCHANT_ONLINE"
                        ? "Customer and Merchant Online"
                        : transaction.txnMode == "CUSTOMER_MERCHANT_ONLINE"
                        ? "Customer and Merchant Online"
                        : transaction.txnMode == "CUSTOMER_OFFLINE"
                        ? "Customer Offline"
                        : ""}
                    </td>
                    <td>{checkIcon(transaction.status)}</td>
                  </tr>,
                ])}
              <tr className="transaction-count">
                <td colSpan={9}>
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
    </>
  );
};

export default RecentTransactions;
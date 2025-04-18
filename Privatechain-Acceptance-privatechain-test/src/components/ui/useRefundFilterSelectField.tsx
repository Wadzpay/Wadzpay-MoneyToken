import React, { useEffect, useState, useContext, useRef } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import "react-datepicker/dist/react-datepicker.css";
import DatePicker from "react-datepicker";
import {
  Asset,
  Status,
  refundStatus,
  TransactionType,
  // DebitCredit,
  RefundMode,
  refundType,
  SearchItemsRefund,
} from "src/constants/FilterTransactionConstants";
import { UserContext } from "src/context/User";
import dayjs from "dayjs";
import { Dropdown, Button } from "react-bootstrap";

import { FilterDropdown } from "./FilterDropdown";
import { FilterSearch } from "./FilterSearch";

interface Props {
  submitFilter: (filters: { [key: string]: string }) => void;
  filters: { [key: string]: string };
  csvDownload: () => void;
  pdfDownload: () => void;
}

export const useRefundFilterSelectField = ({
  submitFilter,
  filters,
  csvDownload,
  pdfDownload,
}: Props) => {
  const { t } = useTranslation();
  const { user } = useContext(UserContext);

  const [dateFrom, setDateFrom] = useState<Date | null>(
    filters.dateFrom ? new Date(filters.dateFrom) : null
  );
  const [dateTo, setDateTo] = useState<Date | null>(
    filters.dateTo ? new Date(filters.dateTo) : null
  );
  const [filterAsset, setFilterAsset] = useState<string | undefined>(
    filters.asset
  );
  const txIdInputRef = useRef<HTMLInputElement>(null);
  const extPosTxIDInputRef = useRef<HTMLInputElement>(null);
  const refundTxIdInputRef = useRef<HTMLInputElement>(null);
  const extRefundPosTxIDInputRef = useRef<HTMLInputElement>(null);
  /* const [filterStatus, setFilterStatus] = useState<string | undefined>(
    filters.status
  )*/
  const [filterRefundStatus, setFilterRefundStatus] = useState<
    string | undefined
  >(filters.refundStatus);
  const [filterTransactionType, setFilterTransactionType] = useState<
    string | undefined
  >(filters.type);
  // const [filterDebitCredit, setFilterDebitCredit] = useState<
  //   string | undefined
  // >(filters.direction)
  const [searchTxId, setSearchTxId] = useState<string | undefined>(
    filters.uuid
  );
  const [searchPOSTxId, setSearchPOSTxId] = useState<string | undefined>(
    filters.posId
  );
  const [searchRefundTxId, setSearchRefundTxId] = useState<string | undefined>(
    filters.refundTransactionID
  );
  const [searchRefundPOSTxId, setSearchRefundPOSTxId] = useState<
    string | undefined
  >(filters.refundPosTransactionID);
  const [filterRefundMode, setFilterRefundMode] = useState<string | undefined>(
    filters.refundMode
  );
  const [searchObj, setSearchObj] = useState<string | undefined>();
  const [filterRefundType, setFilterRefundType] = useState<string | undefined>(
    filters.refundType
  );

  let searchObjFilter: any;

  useEffect(() => {
    if (searchTxId == null || searchTxId == "") {
      if (searchPOSTxId == null || searchPOSTxId == "") {
        serachData();
      }
    }
    // if (extPosTxIDInputRef.current) {
    //   extPosTxIDInputRef.current.value = ""
    // }
    txIdInputRef.current?.focus();
  }, [searchTxId]);

  useEffect(() => {
    if (searchPOSTxId == null || searchPOSTxId == "") {
      if (searchTxId == null || searchTxId == "") {
        serachData();
      }
    }
    // if (txIdInputRef.current) {
    //   txIdInputRef.current.value = ""
    // }
    extPosTxIDInputRef.current?.focus();
  }, [searchPOSTxId]);

  useEffect(() => {
    if (searchRefundTxId == null || searchRefundTxId == "") {
      if (searchRefundPOSTxId == null || searchRefundPOSTxId == "") {
        serachData();
      }
    }
    refundTxIdInputRef.current?.focus();
  }, [searchRefundTxId]);

  useEffect(() => {
    if (searchRefundPOSTxId == null || searchRefundPOSTxId == "") {
      if (searchRefundTxId == null || searchRefundTxId == "") {
        serachData();
      }
    }
    extRefundPosTxIDInputRef.current?.focus();
  }, [searchRefundPOSTxId]);

  useEffect(() => {
    if (filterTransactionType == null || filterTransactionType == "") {
      serachData();
    }
  }, [filterTransactionType]);

  useEffect(() => {
    serachData();
  }, [filterRefundMode, filterRefundStatus, filterAsset, filterRefundType]);

  useEffect(() => {
    if (filterRefundType == null || filterRefundType == "") {
      serachData();
    }
  }, [filterRefundType]);

  const serachData = () => {
    const isSearchTxId =
      txIdInputRef.current?.value == "" || txIdInputRef.current?.value == ""
        ? false
        : true;
    const isSearchPOSTxId =
      extPosTxIDInputRef.current?.value == "" ||
      extPosTxIDInputRef.current?.value == ""
        ? false
        : true;
    const isSearchRefundTxnId =
      refundTxIdInputRef.current?.value == "" ||
      refundTxIdInputRef.current?.value == ""
        ? false
        : true;
    const isSearchRefundPosTxnId =
      extRefundPosTxIDInputRef.current?.value == "" ||
      extRefundPosTxIDInputRef.current?.value == ""
        ? false
        : true;
    submitFilter({
      ...(filterAsset && { asset: filterAsset }),
      // ...(filterStatus && { status: filterStatus }),
      ...(filterRefundStatus && { refundStatus: filterRefundStatus }),
      ...(isSearchTxId && searchTxId && { uuidSearch: searchTxId }),
      ...(isSearchPOSTxId &&
        searchPOSTxId && { posId: searchPOSTxId }),
      ...(isSearchRefundTxnId &&
        searchRefundTxId && { refundTransactionID: searchRefundTxId }),
      ...(isSearchRefundPosTxnId &&
        searchRefundPOSTxId && { refundPosTransactionID: searchRefundPOSTxId }),
      ...(dateFrom && {
        dateFrom: `${dayjs(dateFrom.toString()).format(
          "YYYY-MM-DD"
        )}T00:00:00.000Z`,
      }),
      ...(dateTo && {
        dateTo: `${dayjs(dateTo.toString()).format(
          "YYYY-MM-DD"
        )}T23:59:59.999Z`,
      }),
      ...(filterTransactionType && { type: filterTransactionType }),
      // ...(filterDebitCredit && { direction: filterDebitCredit }),
      ...(searchTxId && searchObjFilter),
      ...(filterRefundMode && { refundMode: filterRefundMode }),
      ...(filterRefundType && { refundType: filterRefundType }),
    });
  };

  const setDates = (dateFrom: Date | null, dateTo: Date | null) => {
    setDateFrom(dateFrom);
    setDateTo(dateTo);
  };

  const searchSelected = (searchTerm: string) => {
    if (searchTerm.length == 0) {
      setSearchTxId(undefined);
      setSearchObj(searchTerm);
    } else {
      setSearchObj(searchTerm);
    }
  };

  const searchText = (searchTerm: string) => {
    if (searchTerm === "") {
      setSearchTxId(undefined);
      return;
    }

    if (searchTerm.length > 0) {
      if (searchObj === "uuid") {
        if (searchTerm !== undefined) {
          setSearchTxId(searchTerm);
          return;
        }
      } else {
        setSearchTxId(searchTerm);
      }
    }
  };

  let allAssets = [...Asset];
  if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
    const arr = [];
    arr[0] = Asset[1];
    arr[1] = Asset[2];
    allAssets = arr;
  } else {
    allAssets = [...Asset];
  }
  const FilterSelectField: React.FC = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const location = useLocation();
    useEffect(() => {
      const queryParams = new URLSearchParams(location.search);
      if (queryParams.has(`asset`)) {
        queryParams.delete(`asset`);
        setSearchParams(queryParams);
      }
      if (queryParams.has(`refundMode`)) {
        queryParams.delete(`refundMode`);
        setSearchParams(queryParams);
      }
      if (queryParams.has(`refundType`)) {
        queryParams.delete(`refundType`);
        setSearchParams(queryParams);
      }
      if (queryParams.has(`sortBy`)) {
        queryParams.delete(`sortBy`);
        setSearchParams(queryParams);
      }
      if (queryParams.has(`sortDirection`)) {
        queryParams.delete(`sortDirection`);
        setSearchParams(queryParams);
      }
    }, []);
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
          <FilterSearch
            title={"Refund Transaction ID: "}
            searchDefaultTxt={t("Refund Transaction ID")}
            value={searchRefundTxId}
            inputRef={refundTxIdInputRef}
            setFilterSearch={(value?: string) =>
              setSearchRefundTxId(value || "")
            }
          />
          <FilterSearch
            title={"Refund Acquirer ID"}
            searchDefaultTxt={t("Refund Acquirer ID")}
            value={searchRefundPOSTxId}
            inputRef={extRefundPosTxIDInputRef}
            setFilterSearch={(value?: string) =>
              setSearchRefundPOSTxId(value || "")
            }
          />
          <div className="search-addon right-addon">
            <span>Actual Date : From </span>
          </div>
          <div>
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
        </div>
        <div className="d-flex flex-row mt-2">
          <div className="right-addon ms-1 me-1 mt-2">
            <span> To </span>
          </div>
          <div className="right-addon me-2">
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
          <FilterDropdown
            title={t("Token")}
            value={filterAsset}
            items={allAssets}
            setFilter={(value?: string) => setFilterAsset(value)}
          />
          <FilterDropdown
            title={t("Refund Mode")}
            value={filterRefundMode}
            items={RefundMode}
            setFilter={(value?: string) => setFilterRefundMode(value)}
          />
          {/* <FilterDropdown
            title={t("Status")}
            value={filterStatus}
            items={Status}
            setFilter={(value?: string) => setFilterStatus(value)}
          />*/}
          {/* <FilterDropdown
            title={t("Transaction Type")}
            value={filterDebitCredit}
            items={DebitCredit}
            setFilter={(value?: string) => setFilterDebitCredit(value)}
          /> */}
          <FilterDropdown
            title={t("Refund Status")}
            value={filterRefundStatus}
            items={refundStatus}
            setFilter={(value?: string) => setFilterRefundStatus(value)}
          />
          <FilterDropdown
            title={t("Refund Type")}
            value={filterRefundType}
            items={refundType}
            setFilter={(value?: string) => setFilterRefundType(value)}
          />
          <button
            className="btn wdz-btn-primary"
            onClick={() => serachData()}
            style={{ marginRight: "5px" }}
          >
            Search
          </button>
          <Dropdown>
            <Dropdown.Toggle variant="outline-warning">
              Download Report
            </Dropdown.Toggle>

            <Dropdown.Menu>
              <Dropdown.Item onClick={() => csvDownload()}>CSV</Dropdown.Item>
              <Dropdown.Item onClick={() => pdfDownload()}>PDF</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </div>
      </div>
    );
  };
  return FilterSelectField;
};

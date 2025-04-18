import React, { useContext, useEffect, useState } from "react"
import {
  Input,
  Select,
  Form,
  DatePicker,
  DatePickerProps,
  Table,
  Switch,
  Pagination,
  Button,
  notification,
  Popconfirm,
  Empty
} from "antd"
import { ThreeDots } from "react-loader-spinner"
import moment from "moment"
import dayjs from "dayjs"
import utc from "dayjs/plugin/utc"
import tz from "dayjs/plugin/timezone"
import "../WalletsList/index.scss"
import { IssuanceContext } from "src/context/Merchant"
import { WalletsUserListdata } from "src/api/models"
import { useWalletUserList, useEnableDisableWalletUser } from "src/api/user"
import { numerAllowOnlyTwoDecimal } from "src/utils"

import PageHeading from "../../components/ui/PageHeading"

function WalletsList(): JSX.Element {
  const { issuanceDetails, institutionDetails } = useContext(IssuanceContext)
  const pageSize = 10
  const [form] = Form.useForm()
  const [isApply, setIsApply] = useState<boolean>(false)
  const [reloadData, setReloadData] = useState<boolean>(false)
  const [reloadDataDate, setReloadDataDate] = useState<boolean>(false)
  const [activeTab, setActiveTab] = useState<string | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [WalletUser, setWalletsList] = useState<WalletsUserListdata | any>()
  const [currentPage, setCurrentPage] = useState<number>()
  const [dateFrom, setDateFrom] = useState<Date | null>(null)
  const [dateTo, setDateTo] = useState<Date | null>(null)
  const [filterBy, setFilterBy] = useState<string>("")
  const [searchBy, setSearchBy] = useState<string>("")
  const [walletBalanceBy, setWalletBalanceBy] = useState<string | null>(null)
  const [placeholderWalletBalanceBy, setPlaceholderWalletBalanceBy] =
    useState<string>("")
  const [balance, setBalance] = useState<any>("")
  const [minAmount, setMinAmount] = useState<any>(0)
  const [maxAmount, setMaxAmount] = useState<any>(0)
  const [showFilter, setShowFilter] = useState<boolean>(false)
  const [isApplyButtonDisabled, setIsApplyButtonDisabled] =
    useState<boolean>(false)

  dayjs.extend(utc)
  dayjs.extend(tz)

  // Table columns
  const columns = [
    {
      title: "ID",
      dataIndex: "walletId",
      render: (walletId: string) => walletId
    },
    {
      title: "First Name",
      dataIndex: "firstName",
      render: (firstName: string) =>
        firstName === "" || firstName === null ? (
          <div className="text-center" style={{ marginRight: "25%" }}>
            -
          </div>
        ) : (
          firstName
        )
    },
    {
      title: "Last Name",
      dataIndex: "lastName",
      render: (lastName: string) =>
        lastName === "" || lastName === null ? (
          <div className="text-center" style={{ marginRight: "25%" }}>
            -
          </div>
        ) : (
          lastName
        )
    },
    {
      title: "User ID",
      dataIndex: "email"
    },
    {
      title: "Mobile Number",
      dataIndex: "phoneNumber",
      render: (phoneNumber: string) =>
        phoneNumber === "" || phoneNumber === null ? (
          <div className="text-center" style={{ marginRight: "25%" }}>
            -
          </div>
        ) : (
          phoneNumber
        )
    },
    {
      title: (data: any) => (
        <>{`Balance Amount (${
          institutionDetails?.issuingCurrency
            ? institutionDetails?.issuingCurrency + "*"
            : ""
        })`}</>
      ),
      render: (data: any) => <>{`${data.tokenBalance}`}</>
    },
    {
      title: "Status",
      render: (data: any) => (
        <>
          <Popconfirm
            title={
              data.status == "ENABLE"
                ? "Are you sure to disable this wallet?"
                : "Are you sure to enable this wallet?"
            }
            onConfirm={() => confirm(data.status, data.email)}
            okText="Yes"
            cancelText="No"
          >
            <Switch
              style={{
                backgroundColor: data.status == "ENABLE" ? "#26A6E0" : "#B6B6B6"
              }}
              checked={data.status == "ENABLE" ? true : false}
            />
          </Popconfirm>
        </>
      )
    },
    {
      title: "Activated On",
      dataIndex: "createdAt",
      render: (createdAt: string) => <>{dateTimeFormat(createdAt)}</>
    },
    {
      title: "",
      dataIndex: "action"
    }
  ]

  // API Call enableDisableWalletUser
  const {
    mutate: enableDisableWalletUser,
    error: enableDisableUserError,
    isSuccess: enableDisableUserIsSuccess
  } = useEnableDisableWalletUser()

  // get wallets user list API
  const { mutate: getWalletList, data, error } = useWalletUserList()

  useEffect(() => {
    console.log("walletlit", institutionDetails)
    if (data) {
      setWalletsList(data)
      setLoading(false)
    }
  }, [data])

  useEffect(() => {
    if (error) {
      notification["error"]({
        message: "An error occurred",
        description: error.message
      })
      setLoading(false)
    }
  }, [error])

  useEffect(() => {
    if (reloadData) {
      getWalletUserList()
      setReloadData(false)
    }
  }, [reloadData])

  useEffect(() => {
    getWalletUserList()
  }, [activeTab, currentPage])

  useEffect(() => {
    if (filterBy !== "") {
      getWalletUserList()
    }
  }, [filterBy])

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      getWalletUserList()
    }, 500) // Adjust the delay as needed

    return () => clearTimeout(delayDebounceFn)
  }, [searchBy])

  useEffect(() => {
    if (dateFrom != null && dateTo != null) {
      setReloadDataDate(true)
    }

    if (dateFrom == null && dateTo == null) {
      setReloadDataDate(false)
    }

    if (reloadDataDate) {
      if (dateFrom != null && dateTo != null) {
        getWalletUserList()
      } else {
        getWalletUserList()
        setReloadDataDate(false)
      }
    }
  }, [dateFrom, dateTo, reloadDataDate])

  const handleClickTab = (activeTab: string | null) => {
    setActiveTab(activeTab)
    setCurrentPage(1)
  }

  const handleChange = (e: any, name: string) => {
    let show_filter = true
    if (name === "pagination") {
      setCurrentPage(e)
    }
    if (name === "filterBy") {
      setFilterBy(e)
    }
    if (name === "searchBy") {
      setSearchBy(e.target.value)
    }
    if (name === "walletBalanceBy") {
      setWalletBalanceBy(e)

      if (e == "null") {
        setReloadData(true)
        show_filter = false
      }

      setShowFilter(show_filter)

      setIsApply(false)
      setBalance("0")
      setMinAmount("0")
      setMaxAmount("0")
      // Reset the fields
      form.resetFields(["minimum", "maximum"])
    }

    if (name === "balance" || name === "minAmount" || name === "maxAmount") {
      const reg = /^-?\d*(\.\d*)?$/

      if (name === "balance") {
        const value = e.target.value
        if (
          value === "-" ||
          value.startsWith(".") ||
          value == "00" ||
          value.slice(0, 2) == "00"
        ) {
          setBalance("")
        } else if (reg.test(value) || value === "" || value === "-") {
          setBalance(
            value.indexOf(".") >= 0
              ? value.substr(0, value.indexOf(".")) +
                  value.substr(value.indexOf("."), 3)
              : value
          )
        }
      }

      if (name === "minAmount") {
        const value = e.target.value
        if (
          value === "-" ||
          value.startsWith(".") ||
          value == "00" ||
          value.slice(0, 2) == "00"
        ) {
          setMinAmount("")
        } else if (reg.test(value) || value === "" || value === "-") {
          setMinAmount(
            value.indexOf(".") >= 0
              ? value.substr(0, value.indexOf(".")) +
                  value.substr(value.indexOf("."), 3)
              : value
          )
        }
      }

      if (name === "maxAmount") {
        const value = e.target.value
        if (
          value === "-" ||
          value.startsWith(".") ||
          value == "00" ||
          value.slice(0, 2) == "00"
        ) {
          setMaxAmount("")
        } else if (reg.test(value) || value === "" || value === "-") {
          setMaxAmount(
            value.indexOf(".") >= 0
              ? value.substr(0, value.indexOf(".")) +
                  value.substr(value.indexOf("."), 3)
              : value
          )
        }
      }

      setIsApply(false)
    }
  }

  const handleFromDate: DatePickerProps["onChange"] = (
    date: any,
    dateString: any
  ) => {
    dateString = dateString != "" ? moment(dateString).format("YYYY-MM-DD") : ""
    setDateFrom(dateString != "" ? dateString : null)
  }

  const handleToDate: DatePickerProps["onChange"] = (
    date: any,
    dateString: any
  ) => {
    dateString = dateString != "" ? moment(dateString).format("YYYY-MM-DD") : ""
    setDateTo(dateString != "" ? dateString : null)
  }

  const getWalletUserList = () => {
    const requestParams: any = {
      page: currentPage || 1,
      // sortBy: "STATUS",
      // sortDirection: "DESC",
      limit: pageSize,
      type: activeTab != null ? [activeTab] : []
    }

    if (filterBy != "" && searchBy != "") {
      requestParams[filterBy] = searchBy
    } else {
      delete requestParams[filterBy]
    }

    if (dateFrom != null && dateTo != null) {
      requestParams["createdFrom"] = dateFrom
      requestParams["createdTo"] = dateTo
    } else {
      delete requestParams["createdFrom"]
      delete requestParams["createdTo"]
    }

    requestParams["amountFrom"] = minAmount == "0" ? "" : minAmount
    requestParams["amountTo"] = maxAmount == "0" ? "" : maxAmount

    // show loader
    setLoading(true)
    // API CALL
    getWalletList(requestParams)
  }

  const onFinish = (values: any) => {
    // Set apply status if any of the relevant fields are defined
    if (balance !== undefined || values.minAmount || values.maxAmount) {
      setIsApply(true)
    }

    // Reset minAmount and maxAmount if walletBalanceBy is null
    if (walletBalanceBy === null) {
      setMinAmount("")
      setMaxAmount("")
    } else {
      // Handle different walletBalanceBy scenarios
      if (walletBalanceBy === "between") {
        setMinAmount(minAmount)
        setMaxAmount(maxAmount)
        setPlaceholderWalletBalanceBy("Between")
        setWalletBalanceBy(null)
      }
      if (walletBalanceBy === "greaterThan") {
        setMinAmount(parseFloat(balance) + 0.01)
        setMaxAmount("")
        setPlaceholderWalletBalanceBy("Greater than")
        setWalletBalanceBy(null)
      }
      if (walletBalanceBy === "greaterThanEqualTo") {
        setMinAmount(balance)
        setMaxAmount("")
        setPlaceholderWalletBalanceBy("Greater than or equal to")
        setWalletBalanceBy(null)
      }
      if (walletBalanceBy === "lessThan") {
        setMinAmount("")
        setMaxAmount(parseFloat(balance) - 0.01)
        setPlaceholderWalletBalanceBy("Less than")
        setWalletBalanceBy(null)
      }
      if (walletBalanceBy === "lessThanEqualTo") {
        setMinAmount("")
        setMaxAmount(balance)
        setPlaceholderWalletBalanceBy("Less than or equal to")
        setWalletBalanceBy(null)
      }

      if (
        walletBalanceBy === "" ||
        (minAmount != "" && maxAmount != "") ||
        balance != ""
      ) {
        setReloadData(true)
      }

      // setShowFilter(false)
      // switch (walletBalanceBy) {
      //   case "between":
      //     setMinAmount(values.minAmount)
      //     setMaxAmount(values.maxAmount)
      //     setPlaceholderWalletBalanceBy("Between")
      //     setWalletBalanceBy(null)
      //     break
      //   case "greaterThan":
      //     setMinAmount(parseFloat(balance) + 0.01)
      //     setMaxAmount("")
      //     setPlaceholderWalletBalanceBy("Greater than")
      //     setWalletBalanceBy(null)
      //     break
      //   case "greaterThanEqualTo":
      //     setMinAmount(balance)
      //     setMaxAmount("")
      //     setPlaceholderWalletBalanceBy("Greater than or equal to")
      //     setWalletBalanceBy(null)
      //     break
      //   case "lessThan":
      //     setMinAmount("")
      //     setMaxAmount(parseFloat(balance) - 0.01)
      //     setPlaceholderWalletBalanceBy("Less than")
      //     setWalletBalanceBy(null)
      //     break
      //   case "lessThanEqualTo":
      //     setMinAmount("")
      //     setMaxAmount(balance)
      //     setPlaceholderWalletBalanceBy("Less than or equal to")
      //     setWalletBalanceBy(null)
      //     break
      //   default:
      //     break
      // }
    }

    // Reload data if necessary conditions are met
    if (
      walletBalanceBy === "" ||
      (values.minAmount !== "" && values.maxAmount !== "") ||
      balance !== ""
    ) {
      setReloadData(true)
    }

    setShowFilter(false)
  }

  const onReset = () => {
    form.resetFields()

    setMinAmount("0")
    setMaxAmount("0")
    setIsApply(false)
    setWalletBalanceBy(null)
    // Reload Data
    setReloadData(true)
  }

  const confirm = (status: string, email: string) => {
    changeStatus(status, email)
    notification["success"]({
      message: "Notification",
      description:
        status === "ENABLE"
          ? "Wallet disabled successfully"
          : "Wallet enabled successfully"
    })
  }

  const changeStatus = (status: string, email: string) => {
    enableDisableWalletUser({
      email,
      isEnabled: status === "ENABLE" ? false : true
    })

    // update wallet user array data
    WalletUser.totalEnabled =
      status === "ENABLE"
        ? WalletUser?.totalEnabled - 1
        : WalletUser?.totalEnabled + 1
    WalletUser.totalDisabled =
      status === "ENABLE"
        ? WalletUser?.totalDisabled + 1
        : WalletUser?.totalDisabled - 1

    WalletUser?.walletList.map((obj: any) => {
      if (obj.email === email) {
        obj.status = status === "ENABLE" ? "DISABLED" : "ENABLE"
      }
      return obj
    })
  }

  const dateTimeFormat = (time: any) => {
    if (!time) {
      return null
    }

    return dayjs(time).tz("Asia/Kuala_Lumpur").format("D MMM YYYY, hh:mma")
  }

  const disabledFromDate = (current: any) => {
    return dateTo === null
      ? current && current.isAfter(moment())
      : current && current.isAfter(moment(dateTo).add(1, "days"))
  }

  const disabledToDate = (current: any) => {
    return dateFrom === null
      ? current && current.isAfter(moment())
      : (current && current.isBefore(moment(dateFrom))) ||
          (current && current.isAfter(moment()))
  }

  // Custom validation function for the minimum field
  const validateMinimum = (_: any, value: any) => {
    if (value) {
      const maximumValue = form.getFieldValue("maximum")
      if (parseFloat(value) >= parseFloat(maximumValue)) {
        return Promise.reject(
          "Minimum value must be less than the maximum value"
        )
      }
    }
    return Promise.resolve()
  }

  // Custom validation function for the maximum field
  const validateMaximum = (_: any, value: any) => {
    if (value) {
      const minimumValue = form.getFieldValue("minimum")
      if (parseFloat(value) <= parseFloat(minimumValue)) {
        return Promise.reject(
          "Maximum value must be greater than the minimum value"
        )
      }
    }
    return Promise.resolve()
  }

  // Function to generate keys based on the name property
  const generateRowKey = (record: any) => record.walletId

  const isApplyButtonActive =
    parseFloat(minAmount) === 0 || parseFloat(maxAmount) === 0
      ? false
      : parseFloat(minAmount) >= parseFloat(maxAmount)
      ? true
      : false

  return (
    <div>
      <PageHeading title="Wallets List" />
      <hr className="middle-line" />
      <div className="p-2 ms-1">
        <div className="row bg-white boxShadow rounded WalletFilters">
          <div className="col-xl-1 col-lg-6 col-sm-2 p-1">
            <div
              className={
                activeTab == null ? "card-body wdz-font-color" : "card-body"
              }
              style={{ cursor: "pointer" }}
              onClick={() => handleClickTab(null)}
            >
              <div className="row text-center">
                <div className="col mr-2 tabs" style={{ marginTop: "33px" }}>
                  <div className="text-xs mb-1 h5">All</div>
                  <div className="mb-0">{WalletUser?.totalCount || 0}</div>
                </div>
              </div>
            </div>
            {activeTab == null ? (
              <div
                style={{
                  borderRadius: "0px 0px 0px 5px",
                  marginLeft: "-4px"
                }}
                className="active-tab"
              ></div>
            ) : null}
          </div>

          <div className="col-xl-1 col-lg-6 col-sm-2 col-sm-2 p-1">
            <div
              className={
                activeTab == "ENABLE" ? "card-body wdz-font-color" : "card-body"
              }
              style={{ cursor: "pointer" }}
              onClick={() => handleClickTab("ENABLE")}
            >
              <div className="row text-center">
                <div className="col mr-2 tabs" style={{ marginTop: "33px" }}>
                  <div className="mb-1 h5">Enabled</div>
                  <div className="mb-0">{WalletUser?.totalEnabled || 0}</div>
                </div>
              </div>
            </div>
            {activeTab == "ENABLE" ? <div className="active-tab"></div> : null}
          </div>

          <div className="col-xl-1 col-lg-6 col-sm-2 p-1">
            <div
              className={
                activeTab == "DISABLED"
                  ? "card-body wdz-font-color"
                  : "card-body"
              }
              style={{ cursor: "pointer" }}
              onClick={() => handleClickTab("DISABLED")}
            >
              <div className="row text-center">
                <div className="col mr-2 tabs" style={{ marginTop: "33px" }}>
                  <div className="text-xs mb-1 h5">Disabled</div>
                  <div className="mb-0">{WalletUser?.totalDisabled || 0}</div>
                </div>
              </div>
            </div>
            {activeTab == "DISABLED" ? (
              <div className="active-tab"></div>
            ) : null}
          </div>

          <div className="col-xl-3 col-lg-6 col-sm-6 mt-3 searchByselection">
            <Form.Item label="Search by selection">
              <Input.Group compact>
                <Select
                  placeholder="Select By"
                  onChange={(e) => handleChange(e, "filterBy")}
                  options={[
                    { value: "walletId", label: "Wallet ID" },
                    { value: "firstName", label: "First Name" },
                    { value: "lastName", label: "Last Name" },
                    { value: "email", label: "User ID" },
                    { value: "mobileNumber", label: "Mobile Number" }
                  ]}
                  style={{ width: "50%" }}
                  suffixIcon={<img src={"/images/down-arrow.svg"} />}
                />
                {searchBy?.length == 0 ? (
                  <span className="ant-picker-suffix">
                    <span
                      role="img"
                      aria-label="calendar"
                      className="anticon anticon-calendar"
                    >
                      <img
                        className="searchIcon"
                        src={"/images/wallets/search-icon.svg"}
                      />
                    </span>
                  </span>
                ) : null}
                <Input
                  onChange={(e) => handleChange(e, "searchBy")}
                  style={{ width: "50%" }}
                  onKeyPress={(event) => {
                    if (
                      !/[0-9+()-.]/.test(event.key) &&
                      filterBy === "mobileNumber"
                    ) {
                      event.preventDefault()
                    }
                  }}
                  allowClear
                />
              </Input.Group>
            </Form.Item>
          </div>
          <div className="col-xl-2 col-lg-6 col-sm-3 mt-3 activationDateFrom">
            <Form.Item label="Activation Date From">
              <Input.Group compact>
                <DatePicker
                  className="datepicker-custom"
                  id="fromFromDate"
                  style={{ width: "100%" }}
                  placeholder="DD MM YYYY"
                  disabledDate={disabledFromDate}
                  onChange={handleFromDate}
                  inputReadOnly={true}
                  format={"D MMM YYYY"}
                />
              </Input.Group>
            </Form.Item>
          </div>
          <div className="col-xl-2 col-lg-6 col-sm-3 mt-3 activationDateTo">
            <Form.Item label="Activation Date To">
              <Input.Group compact>
                <DatePicker
                  className="datepicker-custom"
                  id="fromToDate"
                  style={{ width: "100%" }}
                  placeholder="DD MM YYYY"
                  disabledDate={disabledToDate}
                  onChange={handleToDate}
                  inputReadOnly={true}
                  format={"D MMM YYYY"}
                />
              </Input.Group>
            </Form.Item>
          </div>
          <div className="col-xl-2 col-lg-6 col-sm-3 mt-3 balanceAmount">
            <Form.Item label="Balance Amount" style={{ width: "98.3%" }}>
              <Select
                placeholder={
                  placeholderWalletBalanceBy === ""
                    ? "Please select"
                    : placeholderWalletBalanceBy
                }
                defaultValue={walletBalanceBy}
                value={walletBalanceBy}
                onChange={(e) => handleChange(e, "walletBalanceBy")}
                style={{ width: "100%" }}
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
              >
                <Select.Option value={"null"}>
                  <img src={"/images/wallets/balance-all.svg"} /> All
                </Select.Option>
                <Select.Option value="greaterThan">
                  <img
                    className="greater-than"
                    src={"/images/wallets/greater-than.svg"}
                  />{" "}
                  Greater than
                </Select.Option>
                <Select.Option value="greaterThanEqualTo">
                  <img
                    className="equal-to"
                    src={"/images/wallets/equal-to.svg"}
                  />
                  <img
                    className="greater-less-than"
                    src={"/images/wallets/greater-than.svg"}
                  />{" "}
                  Greater than or equal to
                </Select.Option>
                <Select.Option value="lessThan">
                  <img
                    className="less-than"
                    src={"/images/wallets/less-than.svg"}
                  />{" "}
                  Less than
                </Select.Option>
                <Select.Option value="lessThanEqualTo">
                  <img
                    className="equal-to"
                    src={"/images/wallets/equal-to.svg"}
                  />
                  <img
                    className="greater-less-than"
                    src={"/images/wallets/less-than.svg"}
                  />{" "}
                  Less than or equal to
                </Select.Option>
                <Select.Option value="between">
                  <img src={"/images/wallets/balance-between.svg"} /> Between
                </Select.Option>
              </Select>
            </Form.Item>
          </div>
          {showFilter ? (
            <Form
              form={form}
              onFinish={onFinish}
              className={
                walletBalanceBy === "between"
                  ? "balanceFilterFrom"
                  : " balanceFilterFrom balanceFilterFromSmall"
              }
            >
              {/* <Input.Group compact>
                <Form.Item
                  name="minimum"
                  rules={[
                    {
                      required: true,
                      message: "Please enter a minimum value"
                    },
                    {
                      // validator: validateMinimum // Apply custom validation
                    }
                  ]}
                >
                  <Input type="number" min={0} placeholder="Min" />
                </Form.Item>

                <Form.Item
                  name="maximum"
                  rules={[
                    {
                      required: true,
                      message: "Please enter a maximum value"
                    },
                    {
                      // validator: validateMaximum // Apply custom validation
                    }
                  ]}
                >
                  <Input type="number" min={0} placeholder="Max" />
                </Form.Item>
              </Input.Group> */}
              <Form.Item>
                {walletBalanceBy === "between" ? (
                  <Input.Group compact style={{ display: "flex" }}>
                    <Form.Item
                      name="minimum"
                      // rules={[
                      //   {
                      //     type: "number",
                      //     max: maxAmount
                      //   }
                      // ]}
                    >
                      <Input
                        placeholder="Min"
                        value={minAmount}
                        onChange={(e) => handleChange(e, "minAmount")}
                        style={{
                          width: "70%",
                          borderTopRightRadius: 0,
                          borderBottomRightRadius: 0,
                          border: isApplyButtonActive
                            ? "1px solid rgb(255 0 0)"
                            : ""
                        }}
                        className="minAmount"
                        onKeyPress={numerAllowOnlyTwoDecimal}
                        title={
                          isApplyButtonActive
                            ? "Max should be greater than min."
                            : ""
                        }
                      />
                    </Form.Item>
                    <Form.Item
                      name="maximum"
                      // rules={[
                      //   {
                      //     type: "number",
                      //     min: minAmount
                      //   }
                      // ]}
                    >
                      <Input
                        placeholder="Max"
                        value={maxAmount}
                        onChange={(e) => handleChange(e, "maxAmount")}
                        style={{
                          width: "70%",
                          marginLeft: "-30%",
                          border: isApplyButtonActive
                            ? "1px solid rgb(255 0 0)"
                            : ""
                        }}
                        title={
                          isApplyButtonActive
                            ? "Max should be greater than min."
                            : ""
                        }
                        className="maxAmount"
                        onKeyPress={numerAllowOnlyTwoDecimal}
                      />
                    </Form.Item>
                  </Input.Group>
                ) : (
                  <Input
                    placeholder="Amount"
                    value={balance}
                    onChange={(e) => handleChange(e, "balance")}
                    style={{ width: "55%" }}
                    className="balance"
                    onKeyPress={numerAllowOnlyTwoDecimal}
                  />
                )}
                {isApply ? (
                  <Button
                    htmlType="submit"
                    className="ms-1 wdz-grey-bg-color button"
                    onClick={onReset}
                  >
                    Clear
                  </Button>
                ) : (
                  <Button
                    htmlType="submit"
                    className="ms-1 wdz-main-bg-color button"
                    disabled={isApplyButtonActive}
                    title={
                      isApplyButtonActive
                        ? "Max should be greater than min."
                        : ""
                    }
                    style={{ color: isApplyButtonActive ? "#000" : "#fff" }}
                  >
                    Apply
                  </Button>
                )}
              </Form.Item>
            </Form>
          ) : null}
        </div>
      </div>
      <div className="row bg-white mt-1">
        <div className="table-responsive">
          <Table
            columns={columns}
            dataSource={WalletUser?.walletList}
            size="middle"
            className="language walletList"
            loading={{
              indicator: (
                <div className="loader" style={{ width: "40px" }}>
                  <ThreeDots
                    color="#26a6e0"
                    ariaLabel="three-dots-loading"
                    visible={true}
                  />
                </div>
              ),
              spinning: loading
            }}
            pagination={false}
            locale={{
              emptyText: (
                <Empty
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                  description="No wallets found"
                />
              )
            }}
            rowKey={generateRowKey}
            rowClassName={"default-row"} // Apply custom class to row
          />
          {WalletUser?.pagination.total_records > 0 ? (
            <Pagination
              showSizeChanger={false}
              pageSize={pageSize}
              current={WalletUser?.pagination.current_page}
              total={WalletUser?.pagination.total_records}
              onChange={(e) => handleChange(e, "pagination")}
              prevIcon={
                <>
                  <img src={"/images/wallets/arrow-previous.svg"} />
                  <img src={"/images/wallets/arrow-previous.svg"} />
                </>
              }
              nextIcon={
                <>
                  <img src={"/images/wallets/arrow-next.svg"} />
                  <img src={"/images/wallets/arrow-next.svg"} />
                </>
              }
              className={"pagination-custom"}
            />
          ) : null}
        </div>
      </div>
    </div>
  )
}

export default WalletsList

import React, { useState, useEffect, useRef, useContext, useMemo } from "react"
import {
  InputNumber,
  Input,
  Select,
  Form,
  Table,
  Button,
  notification,
  Popconfirm,
  Typography,
  Dropdown,
  MenuProps,
  Tooltip
} from "antd"
const { Option } = Select
import { PoweroffOutlined } from "@ant-design/icons"
import { ThreeDots } from "react-loader-spinner"
import { useSelector } from "react-redux"
import { upperCase, trim } from "lodash"
import { create, all } from "mathjs"
const config = {}
const math = create(all, config)
import dayjs from "dayjs"
import {
  useGetTransactionType,
  useAddTransactionLimitConfig,
  useEditTransactionLimitConfig,
  useGetTransactionLimitConfig
} from "src/api/user"
import { IS_INTEGER_REGEX } from "src/constants/Defaults"
import { Frequency } from "src/api/constants"
import { GetTransactionLimitConfig } from "src/api/models"
import { numerAllowOnlyTwoDecimal } from "src/utils"
import { IssuanceContext } from "src/context/Merchant"
import { ROOTSTATE } from "src/utils/modules"

import "../Configurations/Configurations.scss"
import PageHeading from "../../components/ui/PageHeading"

interface Item {
  key: string
  transaction_type: string
  frequency: string | null
  transactionCount: number
  minimum: any | null
  maximum: any | null
  isActive: boolean
}

const originData: any = []
interface EditableCellProps extends React.HTMLAttributes<HTMLElement> {
  editing: boolean
  dataIndex: string
  title: any
  inputType: "number" | "text" | "select"
  record: Item
  index: number
  children: React.ReactNode
}

const ConversionRates: React.FC = (props) => {
  const { issuanceDetails } = useContext(IssuanceContext)
  const defaultFiatCurrency = issuanceDetails?.fiatCurrency
  const destinationFiatCurrency = issuanceDetails?.defaultCurrency
  const [loading, setLoading] = useState<boolean>(true)
  const [currencyType, setCurrencyType] = useState<string>("")
  const [transactionType, setTransactionType] = useState<string>("")
  const [frequency, setFrequency] = useState<string | null>("")
  const [transactionCount, setCount] = useState<number | null>(null)
  const [minimum, setMinimum] = useState<any | null>(null)
  const [maximum, setMaximum] = useState<any | null>(null)
  const [isButtonDisabled, setIsButtonDisabled] = useState<boolean>(false)
  const [form] = Form.useForm()
  const [addform] = Form.useForm()
  const [cdata, setData] = useState<GetTransactionLimitConfig | any>(originData)
  const [filterdData, setFilterdData] = useState<
    GetTransactionLimitConfig | any
  >([])
  const [search, setSearch] = useState<boolean>(false)
  const [editingKey, setEditingKey] = useState("")
  const [deactivateRow, setDeactivateRow] = useState<any>()
  const [isFrequency, setIsFrequency] = useState<boolean>(false)
  const [isCountDisabled, setIsCountDisabled] = useState<boolean>(false)
  const [isMnimumDisabled, setIsMnimumDisabled] = useState<boolean>(false)
  const [buttonLoading, setButtonLoading] = useState<boolean>(false)
  const [isDeactive, setIsDeactive] = useState<boolean>(false)
  const [transactionTypeList, setTransactionTypeList] = useState<any>([])
  const currencyTypeData = [
    {
      label: `Destination Currency(${
        destinationFiatCurrency === "SART"
          ? /*destinationFiatCurrency.replace("T", "*")*/ "xQAR"
          : destinationFiatCurrency
      })`,
      value: `${destinationFiatCurrency}`
    },
    {
      label: `Home Currency(${defaultFiatCurrency})`,
      value: `${defaultFiatCurrency}`
    }
  ]

  // get Transaction Type Config list API
  const {
    data: getTransactionTypeData,
    isFetching: isFetchingTransactionType,
    error: TransactionTypeError
  } = useGetTransactionType()

  // get Transaction Limit Config list API
  const { data, isFetching, error, refetch } = useGetTransactionLimitConfig()

  // API Call Add Transaction Limit Config
  const {
    mutate: saveTransactionLimitConfig,
    error: saveTransactionTypeConfigError,
    isSuccess
  } = useAddTransactionLimitConfig()

  // API Call Add Transaction Limit edit
  const {
    mutate: editTransactionLimit,
    error: editTransactionLimitError,
    isSuccess: editTransactionLimitSuccess
  } = useEditTransactionLimitConfig()

  const handleChange = (e: any, name: string) => {
    if (name === "currencyType") {
      setCurrencyType(e)
    }
    if (name === "transactionType") {
      if (e === "TTC_008") {
        addform.resetFields(["frequency"])
        setFrequency(null)
      }
      setTransactionType(e)
    }
    if (name === "frequency") {
      setFrequency(e)
      addform.setFieldsValue({ frequency: e })
    }
    if (name === "transactionCount") {
      setCount(e === null ? null : e)
    }
    if (name === "minimum") {
      setMinimum(e.target.value === null ? 0 : e.target.value)
    }
    if (name === "maximum") {
      setMaximum(e.target.value === null ? 0 : e.target.value)
    }
    if (name === "search") {
      setSearch(e.target.value === "" ? false : true)

      const data = cdata.map((element: any, key: number) => {
        return {
          // key: element.key,
          transaction_type: element.transaction_type,
          frequency: element.frequency,
          transactionCount: element.transactionCount,
          minimum: element.minimum,
          maximum: element.maximum
        }
      })

      const filterData = data.filter((res: any) => {
        return JSON.stringify(res)
          .toLocaleLowerCase()
          .match(
            e.target.value.toLocaleLowerCase() === "na"
              ? null
              : e.target.value.toLocaleLowerCase()
          )
      })

      setFilterdData(filterData)
    }
  }

  const onFinish = (values: any) => {
    setButtonLoading(true)

    if (
      minimum != null &&
      maximum != null &&
      parseFloat(maximum) <= parseFloat(minimum)
    ) {
      notification["error"]({
        message: "An error occurred",
        description: `Maximum should be greater than minimum.`
      })

      setButtonLoading(false)
      return false
    }

    saveTransactionLimitConfig({
      fiatCurrency:
        currencyType === "" ? `${defaultFiatCurrency}` : currencyType,
      transactionType,
      frequency: frequency == "" ? null : frequency,
      transactionCount,
      minimum: minimum,
      maximum: maximum,
      isActive: true
    })

    if (isCountDisabled && minimum === null && maximum === null) {
      sessionStorage.setItem(
        "updateMessage",
        "Please enter at least one parameter, such minimum or maximum."
      )
    }
    if (isMnimumDisabled && transactionCount === null && maximum === null) {
      sessionStorage.setItem(
        "updateMessage",
        "Please enter at least one parameter, such as count or maximum."
      )
    }

    // Clear/Reset from
    clearFrom()
  }

  const onFinishFailed = (errorInfo: any) => {
    console.log("Failed:", errorInfo)
  }

  const clearFrom = () => {
    addform.resetFields()
    addform.setFieldsValue({ transactionCount: null })
    setTransactionType("")
    setMinimum(null)
    setMaximum(null)
    setCount(null)
    setIsFrequency(false)
    setIsCountDisabled(false)
    setIsMnimumDisabled(false)
  }

  const EditableCell: React.FC<EditableCellProps> = ({
    editing,
    dataIndex,
    title,
    inputType,
    record,
    index,
    children,
    ...restProps
  }) => {
    let inputNode
    let rules: any
    if (dataIndex === "transaction_type") {
      inputNode = (
        <Select
          value={record.transaction_type}
          size="large"
          placeholder="Currency Type"
          options={transactionTypeList}
          style={{ width: "100%" }}
          disabled={true}
          suffixIcon={null}
        />
      )

      rules = [
        {
          required: false,
          message: ``
        }
      ]
    }
    if (dataIndex === "frequency") {
      inputNode =
        ([
          "",
          "TTC_001",
          "TTC_006",
          "Initial Loading",
          "Unspent Digital Currency Refund"
        ].includes(record.transaction_type) &&
          ["ONE_TIME", "One Time"].includes(
            record.frequency == null ? "" : record.frequency
          )) ||
        [
          "TTC_008",
          "Wallet Balance",
          "Wallet Balance Fee",
          "Wallet Low Balance Fee"
        ].includes(record.transaction_type) ? (
          <Input
            placeholder="Select Frequency"
            size="large"
            value={record.frequency == null ? "" : record.frequency}
            disabled
          /> // mohit
        ) : (
          <Select
            value={record.frequency}
            size="large"
            placeholder="Select Frequency"
            options={Frequency}
            style={{ width: "100%" }}
            suffixIcon={<img src={"/images/down-arrow.svg"} />}
          >
            {Frequency.map((element: any, key: number) => {
              return (
                <Option
                  disabled={
                    isFrequency && element.value !== "ONE_TIME" ? true : false
                  }
                  key={key}
                  value={element.value}
                >
                  {element.label}
                </Option>
              )
            })}
          </Select>
        )

      // rules = [
      //   {
      //     required: true,
      //     message: ``
      //   }
      // ]
    }
    if (dataIndex === "transactionCount") {
      inputNode = (
        <InputNumber
          value={record.transactionCount}
          size="large"
          style={{
            width: "100%"
          }}
          min={1}
          // placeholder={
          //   [
          //     "TTC_001",
          //     "TTC_006",
          //     "Initial Loading",
          //     "Unspent Digital Currency Refund"
          //   ].includes(record.transaction_type) &&
          //   [
          //     "ONE_TIME",
          //     "One Time",
          //     "PER_TRANSACTION",
          //     "Per Transaction"
          //   ].includes(record.frequency == null ? "" : record.frequency)
          //     ? "NA"
          //     : "Enter Count"
          // }
          placeholder={
            [
              "",
              "ONE_TIME",
              "One Time",
              "PER_TRANSACTION",
              "Per Transaction"
            ].includes(record.frequency == null ? "" : record.frequency)
              ? "NA"
              : "Enter Count"
          }
          onKeyPress={(e: any) => {
            const inputValue = e.target.value + e.key // Get the input value with the newly pressed key
            // Check if the input is numeric and greater than 0
            if (!inputValue.match(/^\d+$/) || parseInt(inputValue) <= 0) {
              e.preventDefault() // Prevent the keypress if the condition is not met
            }
          }}
          disabled={
            [
              "",
              "ONE_TIME",
              "One Time",
              "PER_TRANSACTION",
              "Per Transaction"
            ].includes(record.frequency == null ? "" : record.frequency) ||
            [
              "TTC_008",
              "Wallet Balance",
              "Wallet Balance Fee",
              "Wallet Low Balance Fee"
            ].includes(record.transaction_type)
              ? true
              : false
          }
        />
      )

      rules = rules =
        [
          "",
          "TTC_001",
          "TTC_006",
          "Initial Loading",
          "Unspent Digital Currency Refund"
        ].includes(record.transaction_type) &&
        ["ONE_TIME", "One Time"].includes(
          record.frequency == null ? "" : record.frequency
        )
          ? []
          : [
              // { required: true, message: "" },
              {
                validator: async (_: any, value: any) => {
                  if (value !== undefined && value !== null) {
                    value = value.toString()
                    if (value.substr(value.indexOf("."), 4).length === 4) {
                      return Promise.reject(
                        <Tooltip
                          title={`Invalid number (Allow only numbers with 2 decimal places)`}
                          placement="bottom"
                        >
                          <img src={"/images/error.svg"} />
                        </Tooltip>
                      )
                    }
                  }
                }
              }
            ]
    }
    if (dataIndex === "minimum") {
      inputNode = (
        <Input
          value={record.minimum}
          size="large"
          style={{ width: "100%" }}
          min={0}
          onKeyPress={numerAllowOnlyTwoDecimal}
          maxLength={18}
          placeholder={
            ![
              "",
              "ONE_TIME",
              "One Time",
              "PER_TRANSACTION",
              "Per Transaction"
            ].includes(record.frequency == null ? "" : record.frequency)
              ? "NA"
              : "Minimum"
          }
          disabled={
            ![
              "",
              "ONE_TIME",
              "One Time",
              "PER_TRANSACTION",
              "Per Transaction"
            ].includes(record.frequency == null ? "" : record.frequency)
              ? true
              : false
          }
        />
      )

      rules = [
        {
          pattern: /^\d{0,18}(\.\d{0,2})?$/,
          message: (
            <Tooltip
              title={`Length cannot be greater than 18 digits.`}
              placement="bottom"
            >
              <img src={"/images/error.svg"} />
            </Tooltip>
          )
        },
        {
          validator: async (_: any, value: any) => {
            if (value !== undefined && value !== null) {
              value = value.toString()
              if (value.substr(value.indexOf("."), 4).length === 4) {
                return Promise.reject(
                  <Tooltip
                    title={`Invalid number (Allow only numbers with 2 decimal places)`}
                    placement="bottom"
                  >
                    <img src={"/images/error.svg"} />
                  </Tooltip>
                )
              }
            }
          }
        }
      ]
    }
    if (dataIndex === "maximum") {
      inputNode = (
        <Input
          value={record.maximum}
          size="large"
          style={{ width: "100%" }}
          placeholder="Maximum"
          min={0}
          onKeyPress={numerAllowOnlyTwoDecimal}
          maxLength={18}
        />
      )

      rules = [
        {
          pattern: /^\d{0,18}(\.\d{0,2})?$/,
          message: (
            <Tooltip
              title={`Length cannot be greater than 18 digits.`}
              placement="bottom"
            >
              <img src={"/images/error.svg"} />
            </Tooltip>
          )
        },
        {
          validator: async (_: any, value: any) => {
            if (value !== undefined && value !== null) {
              value = value.toString()
              if (value.substr(value.indexOf("."), 4).length === 4) {
                return Promise.reject(
                  <Tooltip
                    title={`Invalid number (Allow only numbers with 2 decimal places)`}
                    placement="bottom"
                  >
                    <img src={"/images/error.svg"} />
                  </Tooltip>
                )
              }
            }
          }
        }
      ]
    }

    return (
      <td {...restProps}>
        {editing ? (
          <>
            <div style={{ width: "222% !important" }}></div>
            <Form.Item name={dataIndex} style={{ margin: 0 }} rules={rules}>
              {inputNode}
            </Form.Item>
          </>
        ) : (
          children
        )}
      </td>
    )
  }

  const deactivateTransactionLimits = () => {
    if (deactivateRow != null) {
      // Update the transaction limit
      editTransactionLimit({
        id: deactivateRow?.key,
        transactionType: deactivateRow?.transaction_type_value,
        fiatCurrency:
          currencyType === "" ? `${defaultFiatCurrency}` : currencyType,
        frequency:
          deactivateRow?.frequency_value == ""
            ? null
            : deactivateRow?.frequency_value,
        transactionCount: deactivateRow.transactionCount,
        minimum: deactivateRow?.minimum,
        maximum: deactivateRow?.maximum,
        isActive: false
      })

      setIsDeactive(true)
    }
  }

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
    if (getTransactionTypeData) {
      const newData = getTransactionTypeData.map((element) => {
        const Obj: any = element
        Obj.label = element.transactionType
        Obj.value = element.transactionTypeId
        return Obj
      })

      setTransactionTypeList(newData)
    }
    if (TransactionTypeError) {
      notification["error"]({
        message: "Notification",
        description: TransactionTypeError.message
      })
    }
  }, [getTransactionTypeData, isFetchingTransactionType, TransactionTypeError])

  useEffect(() => {
    if (data) {
      const newDatas = data.map((element: any, key: number) => {
        return {
          key: element.id,
          transaction_type: element.transactionType,
          transaction_type_value: element.transactionTypeId,
          frequency: element.frequencyStr,
          frequency_value: element.frequency,
          transactionCount: element.transactionCount,
          minimum: element.minValue > 0 ? element.minValue : element.minValue,
          maximum: element.maxValue > 0 ? element.maxValue : element.maxValue,
          isActive: element.isActive
        }
      })

      setData(newDatas)
      setLoading(false)
    }
  }, [data])

  useEffect(() => {
    // transaction type
    let countDisabled = false
    let minimumDisabled = false

    if (transactionType === "TTC_001" || transactionType === "TTC_006") {
      setIsFrequency(true)
      addform.setFieldsValue({ frequency: "ONE_TIME" })
      setFrequency("ONE_TIME")
      countDisabled = true
    } else {
      setIsFrequency(false)
      countDisabled = false

      // addform.resetFields(["frequency"])
    }
    // frequency
    if (
      ((transactionType === "TTC_001" ||
        transactionType === "TTC_002" ||
        transactionType === "TTC_003" ||
        transactionType === "TTC_004" ||
        transactionType === "TTC_005" ||
        transactionType === "TTC_006" ||
        transactionType === "TTC_007") &&
        frequency === "PER_TRANSACTION") ||
      frequency === "ONE_TIME"
    ) {
      countDisabled = true
      addform.setFieldsValue({ transactionCount: null })
      setCount(null)
    } else {
      countDisabled = false
    }

    if (
      (transactionType === "TTC_001" ||
        transactionType === "TTC_002" ||
        transactionType === "TTC_003" ||
        transactionType === "TTC_004" ||
        transactionType === "TTC_005" ||
        transactionType === "TTC_006" ||
        transactionType === "TTC_007") &&
      (frequency === "DAILY" ||
        frequency === "WEEKLY" ||
        frequency === "MONTHLY" ||
        frequency === "QUARTERLY" ||
        frequency === "HALF_YEARLY" ||
        frequency === "YEARLY")
    ) {
      minimumDisabled = true
      addform.setFieldsValue({ minimum: null })
      setMaximum(null)
    } else {
      minimumDisabled = false
    }

    if (transactionType === "TTC_008") {
      countDisabled = true
    }

    setIsCountDisabled(countDisabled)
    setIsMnimumDisabled(minimumDisabled)
  }, [transactionType, frequency, transactionCount, minimum, maximum])

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Transaction limit saved successfully!"
      })
      // refetch transaction limits list
      refetch()
    }

    if (saveTransactionTypeConfigError) {
      notification["error"]({
        message: saveTransactionTypeConfigError.message || "An error occurred",
        description:
          sessionStorage.getItem("updateMessage") === ""
            ? saveTransactionTypeConfigError.message
            : sessionStorage.getItem("updateMessage")
      })

      sessionStorage.setItem("updateMessage", "")
    }

    // hide loader
    setButtonLoading(false)
  }, [isSuccess, saveTransactionTypeConfigError])

  useEffect(() => {
    if (editTransactionLimitSuccess) {
      notification["success"]({
        message: "Notification",
        description: isDeactive
          ? "Transaction limit deactivated successfully."
          : "Transaction limit saved successfully."
      })
      // refetch transaction limits list
      setIsDeactive(false)
      refetch()
    }
    if (editTransactionLimitError) {
      notification["error"]({
        message: "An error occurred",
        description: editTransactionLimitError.message
      })
    }
  }, [editTransactionLimitSuccess, editTransactionLimitError])

  // isEditing
  const isEditing = (record: Item) => record.key === editingKey

  const edit = (record: Partial<Item> & { key: React.Key }) => {
    form.setFieldsValue({ name: "", age: "", address: "", ...record })
    setEditingKey(record.key)
  }

  const cancel = () => {
    setEditingKey("")
  }

  const save = async (key: React.Key) => {
    try {
      const row = (await form.validateFields()) as Item
      if (
        row.minimum != null &&
        row.maximum != null &&
        parseFloat(row.maximum) <= parseFloat(row.minimum)
      ) {
        notification["error"]({
          message: "An error occurred",
          description: `Maximum should be greater than minimum.`
        })

        setButtonLoading(false)
        return false
      }

      const newData = [...cdata]
      const index = newData.findIndex((item) => key === item.key)
      if (index > -1) {
        const item = newData[index]
        // save transaction limits
        const transactionType = row.transaction_type.includes("")
          ? transactionTypeList.filter(
              (element: any) => element.transactionType == row.transaction_type
            )
          : row.transaction_type

        // update transaction limits

        editTransactionLimit({
          id: item.key,
          transactionType:
            typeof transactionType === "object"
              ? transactionType[0].transactionTypeId
              : transactionType,
          fiatCurrency:
            currencyType === "" ? `${defaultFiatCurrency}` : currencyType,
          frequency:
            row.frequency === ""
              ? null
              : upperCase(row.frequency == null ? "" : row.frequency).replace(
                  " ",
                  "_"
                ),
          transactionCount: row.transactionCount,
          minimum: row.minimum,
          maximum: row.maximum,
          isActive: true
        })

        setEditingKey("")
      } else {
        newData.push(row)
        setData(newData)
        setEditingKey("")
      }
    } catch (errInfo) {
      console.log("Validate Failed:", errInfo)
    }
  }

  const columns = [
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Transaction Type{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "transaction_type" &&
                  item?.sortOrder === "ascend"
                    ? "/images/ascending.svg"
                    : "/images/descending.svg"
                }
                style={{ marginLeft: "5px" }}
              />
            </Tooltip>
          </div>
        </>
      ),
      dataIndex: "transaction_type",
      width: "25%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) =>
        a.transaction_type.localeCompare(b.transaction_type),
      showSorterTooltip: false,
      render: (key: string) => (
        <>
          <div className="tableRows more-screen-size-600"></div>
          {key}
        </>
      )
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Frequency{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "frequency" &&
                  item?.sortOrder === "ascend"
                    ? "/images/ascending.svg"
                    : "/images/descending.svg"
                }
                style={{ marginLeft: "5px" }}
              />
            </Tooltip>
          </div>
        </>
      ),
      dataIndex: "frequency",
      width: "15%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.frequency.localeCompare(b.frequency),
      showSorterTooltip: false,
      render: (key: number, record: Item) =>
        record.frequency === null || record?.frequency === ""
          ? "NA"
          : record?.frequency
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Count{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "transactionCount" &&
                  item?.sortOrder === "ascend"
                    ? "/images/ascending.svg"
                    : "/images/descending.svg"
                }
                style={{ marginLeft: "5px" }}
              />
            </Tooltip>
          </div>
        </>
      ),
      dataIndex: "transactionCount",
      width: "15%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.transactionCount - b.transactionCount,
      showSorterTooltip: false,
      render: (key: number, record: Item) =>
        record.transactionCount === null ? "NA" : record.transactionCount
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Minimum{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "minimum" &&
                  item?.sortOrder === "ascend"
                    ? "/images/ascending.svg"
                    : "/images/descending.svg"
                }
                style={{ marginLeft: "5px" }}
              />
            </Tooltip>
          </div>
        </>
      ),
      dataIndex: "minimum",
      width: "15%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.minimum - b.minimum,
      showSorterTooltip: false,
      render: (key: number, record: Item) =>
        record.minimum === null ? "NA" : record.minimum
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Maximum{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "maximum" &&
                  item?.sortOrder === "ascend"
                    ? "/images/ascending.svg"
                    : "/images/descending.svg"
                }
                style={{ marginLeft: "5px" }}
              />
            </Tooltip>
          </div>
        </>
      ),
      dataIndex: "maximum",
      width: "15%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.maximum - b.maximum,
      showSorterTooltip: false,
      render: (key: number, record: Item) =>
        record.maximum === null ? "NA" : record.maximum
    },
    {
      dataIndex: "operation",
      right: 0,
      width: "15%",
      render: (_: any, record: Item) => {
        const editable = isEditing(record)
        return editable ? (
          <span style={{ float: "right" }}>
            <Typography.Link
              className="action-apply"
              style={{ padding: "0px 6px 5px 6px" }}
              onClick={cancel}
            >
              <Tooltip title="" placement="bottomRight">
                <img title="Cancel" src={"/images/cancel.svg"} />
              </Tooltip>
            </Typography.Link>
            &nbsp;&nbsp;
            <Popconfirm
              title="Sure to save?"
              onConfirm={() => save(record.key)}
            >
              <Typography.Link
                className="action-apply"
                style={{ padding: "0px 0px 4px 0px", marginRight: "25px" }}
              >
                <Tooltip title="" placement="bottomRight">
                  <img title="Apply" src={"/images/apply.svg"} />
                </Tooltip>
              </Typography.Link>
            </Popconfirm>
          </span>
        ) : (
          <>
            <Popconfirm
              title="Sure to modify?"
              onConfirm={() => edit(record)}
              autoAdjustOverflow={true}
            >
              <Typography.Link disabled={editingKey !== ""}>
                <Tooltip
                  title={editingKey == "" ? "" : ""}
                  placement="bottomRight"
                >
                  <img
                    title="Modify"
                    className={
                      editingKey !== "" ? "grey-configurations-icon" : ""
                    }
                    src={"/images/edit.svg"}
                  />
                </Tooltip>
              </Typography.Link>
            </Popconfirm>
            &nbsp;
            <Dropdown
              disabled={editingKey !== ""}
              menu={{ items }}
              trigger={["click"]}
            >
              <Typography.Link
                className="action-deactivate"
                onClick={() => setDeactivateRow(record)}
              >
                <img src={"/images/configurations/action-icon.svg"} />
              </Typography.Link>
            </Dropdown>
          </>
        )
      }
    }
  ]

  const items: MenuProps["items"] = [
    {
      label: (
        <Popconfirm
          title="Sure to deactive?"
          onConfirm={() => deactivateTransactionLimits()}
        >
          <span>Deactivate</span>
        </Popconfirm>
      ),
      key: "0"
    }
  ]

  const mergedColumns = columns.map((col: any) => {
    if (!col.editable) {
      return col
    }
    return {
      ...col,
      onCell: (record: Item) => ({
        record,
        inputType: col.dataIndex === "age" ? "number" : "text",
        dataIndex: col.dataIndex,
        title: "",
        editing: isEditing(record)
      })
    }
  })

  const dateTimeFormat = (time: any) => {
    if (!time) {
      return null
    }
    return dayjs(time).tz("Asia/Kolkata").format("D MMM YYYY, hh:mma")
  }

  // Custom formatter to remove leading zeros
  const customFormatter = (value: any) => {
    // Remove leading zeros and format the value
    return value.replace(/^0+(?=\d)/, "")
  }

  // Custom parser to handle the formatted value
  const customParser = (value: any) => {
    // Parse the value and return it as a number
    return parseFloat(value) || 0
  }

  function validateInput(input: number | any) {
    // Define the regular expression pattern
    const pattern = /^(?:\d{1,18}(?:\.\d{1,2})?)?$/

    // Test the input against the pattern
    return pattern.test(input)
  }

  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  return (
    <div className="configurations">
      <PageHeading title="Transaction Limits" />
      <div className="p-2 ms-1">
        <Form
          name="basic"
          labelCol={{ span: 8 }}
          wrapperCol={{ span: 24 }}
          initialValues={{ remember: true }}
          onFinish={onFinish}
          onFinishFailed={onFinishFailed}
          autoComplete="off"
          form={addform}
        >
          <div className="row bg-white boxShadow rounded WalletFilters">
            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                name="currencyType"
                // rules={[{ required: true, message: "" }]}
              >
                <Select
                  size="large"
                  placeholder="Currency Type"
                  options={currencyTypeData}
                  onChange={(e) => handleChange(e, "currencyType")}
                  value={currencyType === "" ? null : currencyType}
                  suffixIcon={
                    <img
                      style={{ zIndex: 99 }}
                      src={"/images/down-arrow.svg"}
                    />
                  }
                  disabled={true}
                />
              </Form.Item>
            </div>
            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                name="transactionType"
                rules={[{ required: true, message: "" }]}
              >
                <Select
                  size="large"
                  placeholder="Transaction Type"
                  options={transactionTypeList}
                  onChange={(e) => handleChange(e, "transactionType")}
                  value={transactionType === "" ? null : transactionType}
                  suffixIcon={
                    <img
                      style={{ zIndex: 99 }}
                      src={"/images/down-arrow.svg"}
                    />
                  }
                />
              </Form.Item>
            </div>

            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                name="frequency"
                rules={[
                  {
                    required: transactionType === "TTC_008" ? false : true,
                    message: ""
                  }
                ]}
              >
                <Select
                  size="large"
                  placeholder="Select Frequency"
                  onChange={(e) => handleChange(e, "frequency")}
                  value={frequency}
                  suffixIcon={
                    <img
                      style={{ zIndex: 99 }}
                      src={"/images/down-arrow.svg"}
                    />
                  }
                  disabled={transactionType === "TTC_008" ? true : false}
                >
                  {Frequency.map((element: any, key: number) => {
                    return (
                      <Option
                        disabled={
                          isFrequency && element.value !== "ONE_TIME"
                            ? true
                            : false
                        }
                        key={key}
                        value={element.value}
                      >
                        {element.label}
                      </Option>
                    )
                  })}
                </Select>
              </Form.Item>
            </div>
            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                className="transactionLimits-count"
                name="transactionCount"
                initialValue={transactionCount}
                rules={[
                  // { required: true, message: "" },
                  {
                    type: "number",
                    min: 0,
                    message: (
                      <Tooltip
                        title={`Count should be greater than 0`}
                        placement="bottom"
                      >
                        <img src={"/images/error.svg"} />
                      </Tooltip>
                    )
                  },
                  {
                    pattern: /^\d{0,6}(\.\d{0,2})?$/,
                    message: (
                      <Tooltip
                        title={`Length cannot be greater than 6 digits.`}
                        placement="bottom"
                      >
                        <img src={"/images/error.svg"} />
                      </Tooltip>
                    )
                  },
                  {
                    validator: async (_, value) => {
                      if (value !== undefined && value !== null) {
                        value = value.toString()
                        if (value.substr(value.indexOf("."), 4).length === 4) {
                          return Promise.reject(
                            <Tooltip
                              title={`Invalid number (Allow only numbers with 2 decimal places)`}
                              placement="bottom"
                            >
                              <img src={"/images/error.svg"} />
                            </Tooltip>
                          )
                        }
                      }
                    }
                  }
                ]}
              >
                <InputNumber
                  className="input-disabled"
                  size="large"
                  style={{ width: "100%" }}
                  placeholder="Enter Count"
                  onChange={(e) => handleChange(e, "transactionCount")}
                  value={transactionCount}
                  min={1}
                  onKeyPress={(e: any) => {
                    const inputValue = e.target.value + e.key // Get the input value with the newly pressed key
                    // Check if the input is numeric and greater than 0
                    if (
                      !inputValue.match(/^\d+$/) ||
                      parseInt(inputValue) <= 0
                    ) {
                      e.preventDefault() // Prevent the keypress if the condition is not met
                    }
                  }}
                  disabled={isCountDisabled}
                  maxLength={6}
                />
              </Form.Item>
            </div>
            <div className="col-xl-1 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                className="transactionLimits-minimum"
                name="minimum"
                rules={[
                  {
                    required: transactionType === "TTC_008" ? true : false,
                    message: ""
                  },
                  // {
                  //   type: "number",
                  //   min: 0,
                  //   message: (
                  //     <Tooltip
                  //       title={`Minimum should be greater than 0`}
                  //       placement="bottom"
                  //     >
                  //       <img src={"/images/error.svg"} />
                  //     </Tooltip>
                  //   )
                  // },
                  {
                    pattern: /^\d{0,18}(\.\d{0,2})?$/,
                    message: (
                      <Tooltip
                        title={`Length cannot be greater than 18 digits.`}
                        placement="bottom"
                      >
                        <img src={"/images/error.svg"} />
                      </Tooltip>
                    )
                  },
                  // {
                  //   type: "number",
                  //   max:
                  //     maximum === 0 || maximum === null
                  //       ? undefined
                  //       : maximum - 1,
                  //   message:
                  //     maximum === 0 || maximum === null ? (
                  //       <Tooltip
                  //         title={`Length cannot be greater than 18 digits.`}
                  //         placement="bottom"
                  //       >
                  //         <img src={"/images/error.svg"} />
                  //       </Tooltip>
                  //     ) : (
                  //       <Tooltip
                  //         title={`Minimum should be less than maximum.`}
                  //         placement="bottom"
                  //       >
                  //         <img src={"/images/error.svg"} />
                  //       </Tooltip>
                  //     )
                  // },
                  {
                    validator: async (_, value) => {
                      if (value !== undefined && value !== null) {
                        value = value.toString()
                        if (value.substr(value.indexOf("."), 4).length === 4) {
                          return Promise.reject(
                            <Tooltip
                              title={`Invalid number (Allow only numbers with 2 decimal places)`}
                              placement="bottom"
                            >
                              <img src={"/images/error.svg"} />
                            </Tooltip>
                          )
                        }
                      }
                    }
                  }
                ]}
              >
                <Input
                  size="large"
                  placeholder="Minimum"
                  onChange={(e) => handleChange(e, "minimum")}
                  style={{ width: "130%" }}
                  min={0}
                  onKeyPress={validateInput}
                  disabled={isMnimumDisabled}
                  // formatter={customFormatter}
                  // parser={customParser}
                  maxLength={18}
                />
              </Form.Item>
            </div>
            <div className="col-xl-1 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                className="transactionLimits-maximum"
                name="maximum"
                rules={[
                  // { required: minimum !== null ? true : false, message: "" },
                  {
                    pattern: /^\d{0,18}(\.\d{0,2})?$/,
                    message: (
                      <Tooltip
                        title={`Length cannot be greater than 18 digits.`}
                        placement="bottom"
                      >
                        <img src={"/images/error.svg"} />
                      </Tooltip>
                    )
                  },
                  // {
                  //   type: "number",
                  //   min: minimum === 0 || minimum === null ? 0 : minimum + 1,
                  //   message:
                  //     minimum === 0 || minimum === null ? (
                  //       <Tooltip
                  //         title={`Length cannot be greater than 18 digits.`}
                  //         placement="bottom"
                  //       >
                  //         <img src={"/images/error.svg"} />
                  //       </Tooltip>
                  //     ) : (
                  //       <Tooltip
                  //         title={`Maximum should be greater than minimum.`}
                  //         placement="bottom"
                  //       >
                  //         <img src={"/images/error.svg"} />
                  //       </Tooltip>
                  //     )
                  // },
                  {
                    validator: async (_, value) => {
                      if (value !== undefined && value !== null) {
                        value = value.toString()
                        if (value.substr(value.indexOf("."), 4).length === 4) {
                          return Promise.reject(
                            <Tooltip
                              title={`Invalid number (Allow only numbers with 2 decimal places)`}
                              placement="bottom"
                            >
                              <img src={"/images/error.svg"} />
                            </Tooltip>
                          )
                        }
                      }
                    }
                  }
                ]}
              >
                <Input
                  size="large"
                  placeholder="Maximum"
                  onChange={(e) => handleChange(e, "maximum")}
                  style={{ width: "130%", marginLeft: "30%" }}
                  min={0}
                  onKeyPress={numerAllowOnlyTwoDecimal}
                  maxLength={18}
                />
              </Form.Item>
            </div>
            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item>
                <Button
                  htmlType="submit"
                  style={{
                    background: buttonColor,
                    color: "#000",
                    border: "none",
                    float: "right",
                    right: 15
                  }}
                  className="button"
                  size="large"
                  disabled={buttonLoading}
                  icon={buttonLoading ? <PoweroffOutlined /> : null}
                  loading={buttonLoading}
                >
                  Add
                </Button>
              </Form.Item>
            </div>
          </div>
        </Form>
        <div className="row bg-white rounded mt-2">
          <div className="col-xl-2 col-lg-6 col-sm-12 mt-2 mb-2">
            <Input
              type="search"
              size="large"
              placeholder="Search"
              onChange={(e) => handleChange(e, "search")}
              style={{ width: "100%", marginLeft: "-10px" }}
              suffix={
                !search ? (
                  <img src={"/images/wallets/search-icon.svg"} />
                ) : (
                  <span style={{ display: "none" }} />
                )
              }
            />
          </div>
        </div>
      </div>
      <div className="row bg-white mt-0">
        <div className="table-responsive">
          <Form form={form} component={false}>
            <Table
              className="table-custom transaction-limits language"
              components={{
                body: {
                  cell: EditableCell
                }
              }}
              dataSource={!search ? cdata : filterdData}
              columns={mergedColumns}
              rowClassName="editable-row default-row"
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
              locale={{ emptyText: "No transaction limits found!" }}
            />
          </Form>
        </div>
      </div>
    </div>
  )
}

export default ConversionRates

import React, { useState, useEffect, useRef, useContext, useMemo } from "react"
import {
  Input,
  InputNumber,
  Select,
  Form,
  Table,
  Button,
  Popconfirm,
  Typography,
  Dropdown,
  MenuProps,
  notification,
  Tooltip
} from "antd"
import { PoweroffOutlined } from "@ant-design/icons"
import { useSelector } from "react-redux"
import { ThreeDots } from "react-loader-spinner"
const { Option } = Select
import { upperCase } from "lodash"
import {
  useGetWalletFeeType,
  useWalletFeeConfig,
  useAddWalletFeeConfig,
  useEditWalletFeeConfig
} from "src/api/user"
import { Frequency } from "src/api/constants"
import { GetWalletFeeConfig } from "src/api/models"
import { numerAllowOnlyTwoDecimal } from "src/utils"
import { IssuanceContext } from "src/context/Merchant"
import { ROOTSTATE } from "src/utils/modules"

import "../Configurations/Configurations.scss"
import PageHeading from "../../components/ui/PageHeading"
interface Item {
  key: string
  parameter: string
  frequency: string | null
  value: string
  valueType: string
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

const ConversionRates: React.FC = ({ ...restProps }) => {
  const [form] = Form.useForm()
  const [addform] = Form.useForm()
  const { issuanceDetails } = useContext(IssuanceContext)
  const defaultFiatCurrency = issuanceDetails?.fiatCurrency
  const destinationFiatCurrency = issuanceDetails?.defaultCurrency
  const [loading, setLoading] = useState<boolean>(true)
  const [currencyType, setCurrencyType] = useState<string>("")
  const [walletConfigType, setWalletConfigType] = useState<string>("")
  const [frequency, setFrequency] = useState<string | null>("")
  const [value, setValue] = useState<number>(0)
  const [valueType, setValueType] = useState<string>("notSelected")
  const [minimum, setMinimum] = useState<any | null>(null)
  const [maximum, setMaximum] = useState<any | null>(null)
  const [cdata, setData] = useState<GetWalletFeeConfig | any>(originData)
  const [filterdData, setFilterdData] = useState<GetWalletFeeConfig | any>([])
  const [search, setSearch] = useState<boolean>(false)
  const [editingKey, setEditingKey] = useState("")
  const [deactivateRow, setDeactivateRow] = useState<any>()
  const [walletFeeTypeList, setWalletFeeTypeList] = useState<any>([])
  const [isFrequency, setIsFrequency] = useState<boolean>(false)
  const [buttonLoading, setButtonLoading] = useState<boolean>(false)
  const [isDeactive, setIsDeactive] = useState<boolean>(false)
  const [editvalueType, setEditValueType] = useState<string | undefined>("")
  const currencyTypeData = [
    {
      label: `Destination Currency(${
        destinationFiatCurrency === "SART"
          ? /* destinationFiatCurrency.replace("T", "*") */ "xQAR"
          : destinationFiatCurrency
      })`,
      value: `${destinationFiatCurrency}`
    },
    {
      label: `Home Currency(${defaultFiatCurrency})`,
      value: `${defaultFiatCurrency}`
    }
  ]

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
    if (dataIndex === "parameter") {
      inputNode = (
        <Select
          value={record.parameter}
          size="large"
          placeholder="Add Parameter"
          options={walletFeeTypeList}
          style={{ width: "100%", zIndex: 1 }}
          disabled={true}
          suffixIcon={null}
        />
      )

      rules = [
        {
          required: false,
          message: ""
        }
      ]
    }
    if (dataIndex === "frequency") {
      const frequency = record.frequency == null ? "" : record.frequency

      inputNode =
        [
          "WF_001",
          "WF_004",
          "WF_006",
          "Activation Fee",
          "Redeem Unspent Fee",
          "Initial Loading Fee",
          "Initial Topup Fee"
        ].includes(record.parameter) &&
        ["ONE_TIME", "One Time", ""].includes(frequency) ? (
          <Input
            style={{ width: "100%", zIndex: 1 }}
            size="large"
            value={frequency}
            placeholder="NA"
            disabled
          /> // mohit
        ) : (
          <Select
            value={record.frequency}
            size="large"
            placeholder="Select Frequency"
            options={Frequency}
            style={{ width: "100%", zIndex: 1 }}
            suffixIcon={<img src={"/images/down-arrow.svg"} />}
            disabled={
              record.frequency === null || record.frequency === ""
                ? true
                : false
            }
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

      rules = [
        {
          required: false,
          message: ``
        }
      ]
    }
    if (dataIndex === "value") {
      inputNode = (
        <InputNumber
          className="valueClass"
          size="large"
          style={{
            width: "100%"
          }}
          placeholder="Enter Value"
          value={record.value}
          onKeyPress={numerAllowOnlyTwoDecimal}
          maxLength={18}
        />
      )

      rules = [
        {
          required: true,
          message: ``
        },
        {
          validator: async (_: any, value: any) => {
            if (value !== undefined && value !== null) {
              value = value.toString()
              if (parseInt(value) < 0) {
                return Promise.reject(
                  <Tooltip
                    title={`Value should be greater than 0`}
                    placement="bottom"
                  >
                    <img src={"/images/error.svg"} />
                  </Tooltip>
                )
              }
              if (parseInt(value) > 999.99) {
                return Promise.reject(
                  <Tooltip
                    title={`Length cannot be greater than 3 digits.`}
                    placement="bottom"
                  >
                    <img src={"/images/error.svg"} />
                  </Tooltip>
                )
              }
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
    if (dataIndex === "valueType") {
      inputNode = (
        <Select
          className="valueType"
          size="large"
          placeholder="Select"
          style={{
            zIndex: 1
          }}
          value={record.valueType}
          suffixIcon={<img src={"/images/down-arrow.svg"} />}
          options={[
            {
              value: "%",
              label: "Percentage (%)",
              disabled: ["WF_001", "Activation Fee"].includes(record.parameter)
            },
            {
              value: "",
              label: "Fixed"
            }
          ]}
          onChange={(e: any) => {
            setEditValueType(e === "fixed" ? "" : e)
          }}
        />
      )
    }
    if (dataIndex === "minimum") {
      inputNode = (
        <Input
          value={record.minimum}
          size="large"
          style={{ width: "100%", zIndex: 1 }}
          placeholder="Minimum"
          min={0}
          onKeyPress={numerAllowOnlyTwoDecimal}
          maxLength={18}
          disabled={editvalueType == "" ? true : false}
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
          style={{ width: "100%", zIndex: 1 }}
          placeholder="Maximum"
          min={0}
          onKeyPress={numerAllowOnlyTwoDecimal}
          maxLength={18}
          disabled={editvalueType == "" ? true : false}
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

  // get Wallet Fee Config list API
  const { data, isFetching, error, refetch } = useWalletFeeConfig()

  // API Call Add Wallet Fee Config
  const {
    mutate: addWalletFeeConfig,
    error: addWalletFeeConfigError,
    isSuccess
  } = useAddWalletFeeConfig()

  // API Call Update Wallet Parameter
  const {
    mutate: editWalletFeeConfig,
    error: editWalletParameterError,
    isSuccess: editWalletParameterSuccess
  } = useEditWalletFeeConfig()

  const handleChange = (e: any, name: string) => {
    if (name === "currencyType") {
      setCurrencyType(e)
    }
    if (name === "walletConfigType") {
      setWalletConfigType(e)
      addform.setFieldsValue({ valueType: "" })
      addform.resetFields(["fixedPercentage", "valueType"])
    }
    if (name === "frequency") {
      setFrequency(e)
      addform.setFieldsValue({ frequency: e })
    }
    if (name === "value") {
      setValue(e === null ? 0 : e)
    }
    if (name === "valueType") {
      setValueType(e === "fixed" ? "" : e)

      if (e === "fixed") {
        addform.resetFields(["minimum", "maximum"])
      }
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
          parameter: element.parameter,
          frequency: element.frequency,
          value:
            element.valueType !== ""
              ? `${element.value}${element.valueType}`
              : element.value,
          valueType: "",
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

    addWalletFeeConfig({
      fiatCurrency:
        currencyType == "" ? `${defaultFiatCurrency}` : currencyType,
      walletConfigType,
      frequency:
        walletConfigType === "WF_004" || walletConfigType === "WF_005"
          ? null
          : frequency,
      value: value + valueType,
      minimum,
      maximum,
      isActive: true
    })

    // Clear/Reset from
    clearFrom()
  }

  const clearFrom = () => {
    addform.resetFields()
    setWalletConfigType("")
    addform.setFieldsValue({ count: 0 })
    addform.setFieldsValue({ value: 0 })
    setValue(0)
    setMinimum(null)
    setMaximum(null)
    setIsFrequency(false)
  }

  const onFinishFailed = (errorInfo: any) => {
    // setValidation(true)
    console.log("Failed:", errorInfo)
  }

  const deactivateWalletParameters = () => {
    if (deactivateRow != null) {
      editWalletFeeConfig({
        id: deactivateRow?.key,
        fiatCurrency:
          currencyType == "" ? `${defaultFiatCurrency}` : currencyType,
        walletConfigType: deactivateRow?.parameter_value,
        frequency: deactivateRow?.frequency_value,
        value: deactivateRow?.value + deactivateRow?.valueType,
        minimum: deactivateRow?.minimum,
        maximum: deactivateRow?.maximum,
        isActive: false
      })

      setIsDeactive(true)
    }
  }

  // get Wallet Fee Config list API
  const {
    data: getWalletFeeTypeData,
    isFetching: isFetchingwalletFeeType,
    error: walletFeeTypeError
  } = useGetWalletFeeType()

  useEffect(() => {
    if (getWalletFeeTypeData) {
      const newData = getWalletFeeTypeData.map((element) => {
        const exists = cdata.find(
          (objs: any) => objs.parameter_value == element.walletFeeId
        )

        const Obj: any = element
        Obj.label = element.feeType
        Obj.value = element.walletFeeId
        Obj.isActive = typeof exists !== "object" ? true : false
        return Obj
      })

      setWalletFeeTypeList(newData)
    }
  }, [cdata, getWalletFeeTypeData])

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
    if (data) {
      const newDatas = data.map((element: any, key: number) => {
        return {
          key: element.id,
          parameter: element.walletFeeType,
          parameter_value: element.walletFeeId,
          frequency:
            element.frequencyStr === null || element.frequencyStr === ""
              ? ""
              : element.frequencyStr,
          frequency_value: element.frequency,
          value: element.value !== null ? element.value.replace("%", "") : "",
          valueType:
            element.value !== null
              ? element.value.slice(-1) === "%"
                ? "%"
                : ""
              : "",
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
    if (walletConfigType === "WF_001" || walletConfigType === "WF_006") {
      setIsFrequency(true)
      addform.setFieldsValue({ frequency: "ONE_TIME" })
      setFrequency("ONE_TIME")
    } else if (walletConfigType === "WF_004" || walletConfigType === "WF_005") {
      setFrequency("")
    } else {
      setIsFrequency(false)
      // addform.resetFields(["frequency"])
    }
  }, [walletConfigType, frequency, value, minimum, maximum])

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Wallet parameter saved successfully!"
      })
      // refetch Wallet parameter list
      refetch()
    }
    if (addWalletFeeConfigError) {
      notification["error"]({
        message: "Notification",
        description: addWalletFeeConfigError.message
      })
    }
    // hide loader
    setButtonLoading(false)
  }, [isSuccess, addWalletFeeConfigError])

  useEffect(() => {
    if (editWalletParameterSuccess) {
      notification["success"]({
        message: "Notification",
        description: isDeactive
          ? "Wallet parameter deactivated successfully"
          : "Wallet parameter saved successfully!"
      })

      // refetch Wallet parameter list
      setIsDeactive(false)
      refetch()
    }
    if (editWalletParameterError) {
      notification["error"]({
        message: "Notification",
        description: editWalletParameterError.message
      })
    }
  }, [editWalletParameterSuccess, editWalletParameterError])

  // Mohit
  useEffect(() => {
    if (editvalueType === "") {
      form.resetFields(["minimum", "maximum"])
    }
  }, [editvalueType])

  // isEditing
  const isEditing = (record: Item) => record.key === editingKey

  const edit = (record: Partial<Item> & { key: React.Key }) => {
    form.setFieldsValue({ ...record })
    setEditingKey(record.key)
    setEditValueType(record?.valueType)
  }

  const cancel = () => {
    setEditingKey("")
    setEditValueType("")
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

        // save Wallet Parameter
        const parameter = row.parameter.includes(" ")
          ? walletFeeTypeList.filter(
              (element: any) =>
                element.feeType == row.parameter ||
                element.feeType == row.parameter + " Fee"
            )
          : row.parameter

        editWalletFeeConfig({
          id: item.key,
          fiatCurrency:
            currencyType == "" ? `${defaultFiatCurrency}` : currencyType,
          walletConfigType:
            typeof parameter === "object"
              ? parameter[0]?.walletFeeId
              : parameter,
          frequency:
            row.frequency == ""
              ? null
              : row.frequency != null
              ? upperCase(row.frequency).replace(" ", "_")
              : row.frequency,
          value: row.value + row.valueType,
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

  const columns: any = [
    {
      key: "parameter",
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Parameter{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "parameter" &&
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
      dataIndex: "parameter",
      width: "25%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.parameter.localeCompare(b.parameter),
      showSorterTooltip: false,
      render: (key: string) => (
        <>
          <div className="tableRows more-screen-size-600"></div>
          {key}
        </>
      )
    },
    {
      key: "frequency",
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
      width: "20%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      showSorterTooltip: false,
      sorter: (a: any, b: any) => a.frequency.localeCompare(b.frequency),
      render: (key: number, record: Item) =>
        record.frequency === null || record.frequency === ""
          ? "NA"
          : record.frequency
    },
    {
      key: "value",
      title: (item: any) => (
        <>
          <div>
            Value(Fixed/Percentage){" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "value" &&
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
      dataIndex: "value",
      width: "20%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.value - b.value,
      showSorterTooltip: false,
      render: (_: any, record: Item) => (
        <>{`${record.value}${record.valueType}`}</>
      )
    },
    {
      dataIndex: "valueType",
      width: "0%",
      editable: true,
      render: (key: number) => <></>
    },
    {
      key: "minimum",
      title: (item: any) => (
        <>
          <div>
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
      width: "12%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.minimum - b.minimum,
      showSorterTooltip: false,
      render: (key: number, record: Item) =>
        record.minimum === null ? "NA" : record.minimum
    },
    {
      key: "maximum",
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
      width: "11%",
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
      width: "12%",
      render: (_: any, record: Item) => {
        const editable = isEditing(record)
        return editable ? (
          <span style={{ display: "flex" }}>
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
            <span style={{ float: "right", display: "flex" }}>
              <Popconfirm
                title="Sure to modify?"
                onConfirm={() => edit(record)}
                autoAdjustOverflow={true}
              >
                <Typography.Link disabled={editingKey !== ""}>
                  <Tooltip
                    title={editingKey === "" ? "" : ""}
                    placement="bottomRight"
                  >
                    <img
                      title="Modify Parameter"
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
                menu={{ items }}
                trigger={["click"]}
                disabled={editingKey !== ""}
              >
                <Typography.Link
                  className="action-deactivate"
                  onClick={() => setDeactivateRow(record)}
                >
                  <img src={"/images/configurations/action-icon.svg"} />
                </Typography.Link>
              </Dropdown>
            </span>
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
          onConfirm={() => deactivateWalletParameters()}
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
        inputType:
          col.dataIndex === "minimum" || col.dataIndex === "maximum"
            ? "number"
            : "text",
        dataIndex: col.dataIndex,
        title: "",
        editing: isEditing(record)
      })
    }
  })

  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  return (
    <div className="configurations">
      <PageHeading title="Wallet Parameter" />
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
            <div
              id="navigation-container-mobile"
              style={{ display: "none", marginTop: "20px" }}
            ></div>
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
                name="parameter"
                rules={[{ required: true, message: "" }]}
              >
                <Select
                  size="large"
                  placeholder="Add Parameter"
                  onChange={(e) => handleChange(e, "walletConfigType")}
                  value={walletConfigType === "" ? null : walletConfigType}
                  suffixIcon={
                    <img
                      style={{ zIndex: 99 }}
                      src={"/images/down-arrow.svg"}
                    />
                  }
                  // style={{ width: "99%" }}
                >
                  {walletFeeTypeList.map((element: any, key: number) => {
                    return (
                      <Option key={key} value={element.walletFeeId}>
                        {element.feeType}
                      </Option>
                    )
                  })}
                </Select>
              </Form.Item>
            </div>
            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                name="frequency"
                rules={[
                  {
                    required:
                      walletConfigType === "WF_004" ||
                      walletConfigType === "WF_005"
                        ? false
                        : true,
                    message: ""
                  }
                ]}
              >
                <Select
                  size="large"
                  placeholder="Select Frequency"
                  onChange={(e) => handleChange(e, "frequency")}
                  value={frequency === "" ? null : frequency}
                  suffixIcon={
                    <img
                      style={{ zIndex: 99 }}
                      src={"/images/down-arrow.svg"}
                    />
                  }
                  disabled={
                    walletConfigType === "WF_004" ||
                    walletConfigType === "WF_005"
                      ? true
                      : false
                  }
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
            <div className="col-xl-3 col-lg-6 col-sm-3 mt-3">
              <Form.Item className="valueFixed">
                <Input.Group compact>
                  <Form.Item
                    name={["fixedPercentage", "value"]}
                    noStyle
                    rules={[
                      { required: true, message: "" },
                      {
                        type: "number",
                        min: 0,
                        message: (
                          <Tooltip
                            title={`Value should be greater than 0`}
                            placement="bottom"
                          >
                            <img src={"/images/error.svg"} />
                          </Tooltip>
                        )
                      },
                      {
                        pattern:
                          valueType === "notSelected"
                            ? /^\d{0,18}(\.\d{0,2})?$/
                            : valueType === ""
                            ? /^(?:\d{1,3}(?:\.\d{1,2})?)?$/
                            : /^(?:\d{1,2}(?:\.\d{1,2})?)?$/,
                        message: (
                          <Tooltip
                            title={
                              valueType === ""
                                ? `Length cannot be greater than 3 digits.`
                                : `Cannot be greater than 99.99%.`
                            }
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
                            if (
                              value.substr(value.indexOf("."), 4).length === 4
                            ) {
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
                      size="large"
                      style={{ width: "50%" }}
                      placeholder="Enter Value"
                      value={value}
                      onChange={(e) => handleChange(e, "value")}
                      min={0}
                      onKeyPress={numerAllowOnlyTwoDecimal}
                      maxLength={valueType === "" ? 18 : 6}
                    />
                  </Form.Item>
                  <Form.Item
                    name={["fixedPercentage", "valueType"]}
                    noStyle
                    rules={[{ required: true, message: "" }]}
                  >
                    <Select
                      size="large"
                      placeholder={"Select"}
                      style={{ width: "50%" }}
                      suffixIcon={<img src={"/images/down-arrow.svg"} />}
                      options={[
                        {
                          value: "%",
                          label: "Percentage (%)",
                          disabled: walletConfigType === "WF_001" ? true : false
                        },
                        {
                          value: "fixed",
                          label: "Fixed"
                        }
                      ]}
                      onChange={(e) => handleChange(e, "valueType")}
                      value={valueType === "" ? null : valueType}
                    />
                  </Form.Item>
                </Input.Group>
              </Form.Item>
            </div>
            <div className="col-xl-1 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                name="minimum"
                rules={[
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
                    validator: async (_, value) => {
                      if (value !== undefined && value !== null) {
                        value = value.toString()
                        if (value.substr(value.indexOf("."), 4).length === 4) {
                          return Promise.reject(
                            <Tooltip
                              title="Invalid number (Allow only numbers with 2 decimal places)"
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
                  style={{ width: "107%" }}
                  min={0}
                  onKeyPress={numerAllowOnlyTwoDecimal}
                  maxLength={18}
                  disabled={valueType === "" ? true : false}
                />
              </Form.Item>
            </div>
            <div className="col-xl-1 col-lg-6 col-sm-3 mt-3">
              <Form.Item
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
                  {
                    validator: async (_, value) => {
                      if (value !== undefined && value !== null) {
                        value = value.toString()
                        if (value.substr(value.indexOf("."), 4).length === 4) {
                          return Promise.reject(
                            <Tooltip
                              title="Invalid number (Allow only numbers with 2 decimal places)"
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
                  style={{ width: "107%", marginLeft: "7%" }}
                  min={0}
                  onKeyPress={numerAllowOnlyTwoDecimal}
                  maxLength={18}
                  disabled={valueType === "" ? true : false}
                />
              </Form.Item>
            </div>
            <div className="col-xl-1 col-lg-6 col-sm-3 mt-3">
              <Form.Item>
                <Button
                  htmlType="submit"
                  style={{
                    background: buttonColor,
                    color: "#000",
                    width: "100%",
                    border: "none",
                    fontWeight: 300
                  }}
                  className="button"
                  size="large"
                  disabled={buttonLoading}
                  icon={buttonLoading ? <PoweroffOutlined /> : null}
                  loading={buttonLoading}
                  title="Add Parameter"
                >
                  Add
                </Button>
              </Form.Item>
            </div>
          </div>
        </Form>

        <div className="row bg-white rounded mt-2">
          <div className="col-xl-2 col-lg-6 col-sm-6 mt-2 mb-2">
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
              locale={{ emptyText: "No wallet parameter found!" }}
              className={
                editingKey !== ""
                  ? "table-custom wallet-parameter custom-wallet-parameter language"
                  : "table-custom custom-wallet-parameter language"
              }
            />
          </Form>
        </div>
      </div>
    </div>
  )
}

export default ConversionRates

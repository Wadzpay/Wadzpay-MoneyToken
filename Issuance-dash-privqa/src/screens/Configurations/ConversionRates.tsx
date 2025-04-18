import React, { useState, useEffect, useRef, useContext } from "react"
import { useSelector } from "react-redux"
import {
  Input,
  InputNumber,
  Select,
  Form,
  Table,
  Button,
  notification,
  Popconfirm,
  Typography,
  Dropdown,
  Space,
  Badge,
  MenuProps,
  DatePicker,
  DatePickerProps,
  Tooltip,
  TimePicker
} from "antd"
import type { RangePickerProps } from "antd/es/date-picker"
import { ThreeDots } from "react-loader-spinner"
import { upperCase, trim } from "lodash"
import moment, { Moment } from "moment"
import dayjs from "dayjs"
import {
  useAddConversionRates,
  useEditConversionRates,
  useConversionRates,
  useFiatExchangeRates
} from "src/api/user"
import { ConversionRatesList } from "src/api/models"
import { ROOTSTATE } from "src/utils/modules"
import { numerAllowOnlyFiveDecimal } from "src/utils"
import { IssuanceContext } from "src/context/Merchant"

import "../Configurations/Configurations.scss"
import PageHeading from "../../components/ui/PageHeading"

interface Item {
  key: string
  currencyFrom: string
  currencyTo: string
  baseRate: number
  validFrom: string | Date
  createdAt: Date
  isActive: boolean
  currentActive: boolean
}

const originData: any = []
interface EditableCellProps extends React.HTMLAttributes<HTMLElement> {
  editing: boolean
  dataIndex: string
  title: any
  inputType: "number" | "text"
  record: Item
  index: number
  children: React.ReactNode
}

const ConversionRates: React.FC = (props) => {
  const { issuanceDetails, institutionDetails } = useContext(IssuanceContext)
  const datePickerRef = useRef<any>(null)
  const timePickerRef = useRef<any>()
  const [loading, setLoading] = useState<boolean>(true)
  const [currencyFrom, setCurrencyFrom] = useState<string>("")
  const [currencyTo, setCurrencyTo] = useState<string>("")
  const [baseRate, setBaseRate] = useState<number | null>(null)
  const [validFrom, setValidFrom] = useState<string | Date>("")
  const [selectedDate, setSelectedDate] = useState<string | Date>("")
  const [selectedTime, setSelectedTime] = useState<string | Date | null>("")
  const [selectedTimeString, setSelectedTimeString] = useState<string | Date>(
    ""
  )
  const [validFromNew, setValidFromNew] = useState(moment())
  const [checkDate, setCheckDate] = useState<boolean>(true)
  const [selectedDateEdit, setSelectedDateEdit] = useState<string | Date>("")
  const [selectedTimeEdit, setSelectedTimeEdit] = useState<
    string | Date | null
  >("")
  const [selectedTimeStringEdit, setSelectedTimeStringEdit] = useState<
    string | Date
  >("")
  const [validFromEdit, setValidFromEdit] = useState<string | Date>("")
  const [isButtonDisabled, setIsButtonDisabled] = useState<boolean>(true)
  const [form] = Form.useForm()
  const [addform] = Form.useForm()
  const [cdata, setData] = useState<ConversionRatesList | any>(originData)
  const [filterdData, setFilterdData] = useState<ConversionRatesList | any>([])
  const [search, setSearch] = useState<boolean>(false)
  const [editingKey, setEditingKey] = useState("")
  const [deactivateRow, setDeactivateRow] = useState<any>()
  const [isDeactive, setIsDeactive] = useState<boolean>(false)

  // Selector
  const defaultFiatCurrency = issuanceDetails?.fiatCurrency
    ? issuanceDetails?.fiatCurrency
    : "MYR"
  const destinationFiatCurrency = issuanceDetails?.defaultCurrency
  // const defaultCurrency = useSelector(
  //   (store: ROOTSTATE) => store?.institutionCurrencies?.defaultCurrency
  // )
  // const destinationCurrency = useSelector(
  //   (store: ROOTSTATE) => store?.institutionCurrencies?.destinationCurrency
  // )

  // API Call Add Conversion Rates
  const {
    mutate: addConversionRates,
    error: addConversionRatesError,
    isSuccess: isSuccess
  } = useAddConversionRates()

  // API Call Edit Transaction Limit
  const {
    mutate: editConversionRate,
    error: editConversionRateError,
    isSuccess: editConversionRateSuccess
  } = useEditConversionRates()

  // Get fiat exchange rates
  const {
    data: fiatExchangeRates,
    isSuccess: userIsSuccess,
    error: errorFiatExchangeRates,
    refetch: refetchFiatExchangeRates
  } = useFiatExchangeRates(
    `from=${
      currencyFrom === "" && currencyTo === ""
        ? defaultFiatCurrency
        : currencyFrom
    }`
  )

  // get Conversion Rates list API
  const { data, isFetching, error, refetch } = useConversionRates()

  const handleChange = (e: any, name: string) => {
    if (name === "currencyFrom") {
      setCurrencyFrom(e)
    }
    if (name === "currencyTo") {
      setCurrencyTo(e)
    }
    if (name === "baseRate") {
      setBaseRate(e === null ? null : e)
    }
    if (name === "search") {
      setSearch(e.target.value === "" ? false : true)

      const data = cdata.map((element: any, key: number) => {
        return {
          // key: element.key,
          currencyFrom: element.currencyFrom,
          currencyTo: element.currencyTo,
          baseRate: element.baseRate,
          validFrom: element.validFrom,
          createdAt: element.createdAt,
          isActive: element.isActive,
          currentActive: element.currentActive
        }
      })

      const filterData = data.filter((res: any) => {
        return JSON.stringify(res)
          .toLocaleLowerCase()
          .match(e.target.value.toLocaleLowerCase())
      })

      setFilterdData(filterData)
    }
  }

  const validFromDate: DatePickerProps["onChange"] = (
    date: any,
    dateString: any
  ) => {
    dateString =
      dateString === "" ? null : moment(dateString).format("YYYY-MM-DD")

    if (new Date(dateString) > new Date()) {
      setSelectedTime(dateString)
      setSelectedTimeString("00:00:00")
    } else {
      const currentTime = moment(dateString).format("HH:mm:ss")
      setSelectedTimeString(currentTime)
      setSelectedTime(new Date())
    }

    setSelectedDate(dateString != "" ? dateString : null)
  }

  const validFromTimeFun: DatePickerProps["onChange"] = (
    time: any,
    dateString: any
  ) => {
    setSelectedTime(time)
    setSelectedTimeString(dateString)
  }

  const validFromDateEdit: DatePickerProps["onChange"] = (
    date: any,
    dateString: any
  ) => {
    setValidFromNew(moment(dateString))

    if (new Date(dateString) > new Date()) {
      setSelectedTimeEdit(dateString)
      setSelectedTimeStringEdit("00:00:00")
    } else {
      const currentTimeFormatted = moment().format("HH:mm:ss")

      setSelectedTimeEdit(new Date())
      setSelectedTimeStringEdit(`${currentTimeFormatted}`)
    }

    setSelectedDateEdit(dateString)
    setCheckDate(false)
  }

  const validFromTimeEdit: DatePickerProps["onChange"] = (
    time: any,
    dateString: any
  ) => {
    setSelectedTimeEdit(time)
    setSelectedTimeStringEdit(dateString)
  }

  const dateTimeFormat = (time: any) => {
    if (!time) {
      return null
    }

    return dayjs(time).tz("Asia/Kolkata").format("D MMM YYYY, HH:mm:ss")
  }

  const disabledFromDate: RangePickerProps["disabledDate"] = (current) => {
    return current && current < dayjs().endOf("day").subtract(1, "days")
  }

  const disabledHours = (defaultDate: any = null) => {
    const selectedDateTime =
      defaultDate !== null && checkDate ? defaultDate : validFromNew

    if (selectedDateTime.isSame(moment(), "day")) {
      const disabledHours = []
      for (let i = 0; i < 24; i++) {
        if (i < moment().hours()) {
          disabledHours.push(i)
        }
      }
      return disabledHours
    }

    return []
  }

  const disabledMinutes = (selectedHour: number, defaultDate: any) => {
    const selectedDateTime =
      defaultDate !== null && defaultDate !== -1 && checkDate
        ? defaultDate
        : validFromNew

    const currentHour = new Date().getHours()

    if (
      selectedDateTime.isSame(moment(), "day") &&
      selectedHour == currentHour
    ) {
      // Disable past minutes for the current hour
      const minutes = []
      for (let i = 0; i < moment().minute(); i++) {
        if (minutes.length === 0) {
          minutes.push(i)
        }
        minutes.push(i + 1)
      }

      return minutes
    }

    // Disable all minutes for previous hours
    return []
  }

  const onFinish = (values: any) => {
    addConversionRates({
      currencyFrom,
      currencyTo,
      baseRate: values.baseRate,
      isActive: true,
      validFrom:
        validFrom != ""
          ? validFrom
          : moment(currentDate()).format("YYYY-MM-DD[T]HH:mm:ssZ")
    })

    // // Clear/Reset from
    clearFrom()
  }

  const currentDate = () => {
    return dayjs(moment().format()).format("D MMM YYYY, HH:mm:ss")
  }

  const clearFrom = () => {
    addform.resetFields()
    addform.setFieldsValue({ baseRate: null })
    setSelectedDate("")
    setSelectedTime("")
    setValidFrom("")
  }

  const deactivateConverstionRates = () => {
    if (deactivateRow != null) {
      editConversionRate({
        id: deactivateRow.key,
        currencyFrom: deactivateRow.currencyFrom,
        currencyTo: deactivateRow.currencyTo,
        baseRate: deactivateRow.baseRate,
        validFrom:
          deactivateRow.validFrom != ""
            ? deactivateRow.validFrom
            : moment(currentDate()).format("YYYY-MM-DD[T]HH:mm:ssZ"),
        isActive: false
      })

      setIsDeactive(true)
    }
  }

  useEffect(() => {
    if (data) {
      const originData = data.map((element: any, key: number) => {
        const {
          id,
          currencyFrom,
          currencyTo,
          baserRate,
          validFrom,
          createdAt,
          isActive,
          currentActive
        } = element
        return {
          key: id,
          currencyFrom: currencyFrom,
          currencyTo: currencyTo,
          baseRate: baserRate,
          validFrom: validFrom,
          createdAt: createdAt,
          isActive: isActive,
          currentActive: currentActive
        }
      })

      setData(originData)
      setLoading(false)
    }
    if (error) {
      notification["error"]({
        message: "An error occurred",
        description: error.message
      })
    }
  }, [data, error])

  useEffect(() => {
    if (currencyFrom !== null && currencyTo !== null && baseRate !== 0) {
      setIsButtonDisabled(false)
    } else {
      setIsButtonDisabled(false)
    }
  }, [currencyFrom, currencyTo, baseRate, validFrom])

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: `Conversion rate saved successfully.`
      })
      // refetch conversion rates list
      refetch()
    }
    if (addConversionRatesError) {
      notification["error"]({
        message: "Notification",
        description: addConversionRatesError.message
      })
    }
  }, [isSuccess, addConversionRatesError])

  useEffect(() => {
    if (editConversionRateSuccess) {
      notification["success"]({
        message: "Notification",
        description: isDeactive
          ? "Conversion rate deactivated successfully"
          : `Conversion rate saved successfully.`
      })
      // refetch Convertion rate list
      setIsDeactive(false)
      refetch()
    }
    if (editConversionRateError) {
      notification["error"]({
        message: "Notification",
        description: editConversionRateError.message
      })
    }
  }, [editConversionRateSuccess, editConversionRateError])

  useEffect(() => {
    if (currencyFrom !== "" && currencyTo !== "") {
      if (fiatExchangeRates) {
        setBaseRate(fiatExchangeRates.SAR)
        addform.setFieldsValue({ baseRate: fiatExchangeRates.SAR })
      }
      if (errorFiatExchangeRates) {
        notification["error"]({
          message: "Notification",
          description: errorFiatExchangeRates.message
        })
      }
    }
  }, [currencyFrom, currencyTo])

  useEffect(() => {
    if (selectedDate != "" || selectedTimeString != "") {
      const combinedDateTime = moment(
        selectedDate + " " + selectedTimeString
      ).format("YYYY-MM-DD[T]HH:mm:ssZ")

      setValidFrom(combinedDateTime)

      if (new Date(`${selectedDate} ${selectedTimeString}`) < new Date()) {
        setSelectedTime(new Date())
      }
    }
  }, [selectedDate, selectedTimeString])

  useEffect(() => {
    if (selectedDateEdit != "" || selectedTimeStringEdit != "") {
      const combinedDateTime = moment(
        selectedDateEdit + " " + selectedTimeStringEdit
      ).format("YYYY-MM-DD[T]HH:mm:ssZ")

      setValidFromEdit(combinedDateTime)

      if (
        new Date(`${selectedDateEdit} ${selectedTimeStringEdit}`) < new Date()
      ) {
        setSelectedTimeEdit(new Date())
      }
      console.log("Combined DateTime:", combinedDateTime)
    }
  }, [selectedDateEdit, selectedTimeStringEdit])

  // isEditing
  const isEditing = (record: Item) => record.key === editingKey

  const edit = (record: Partial<Item> & { key: React.Key }) => {
    form.setFieldsValue({ ...record })
    setEditingKey(record.key)
    setValidFromEdit("")
    setSelectedDateEdit(moment(record.validFrom).format("YYYY-MM-DD"))
    setSelectedTimeStringEdit(moment(record.validFrom).format("HH:mm:ss"))
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
    if (dataIndex === "currencyFrom") {
      inputNode = (
        <Select
          value={record.currencyFrom}
          size="large"
          placeholder="Currency From"
          suffixIcon={<img src={"/images/down-arrow.svg"} />}
          options={[
            {
              value: `${defaultFiatCurrency}`,
              label: `${defaultFiatCurrency}`
            }
          ]}
          style={{ width: "100%" }}
        />
      )

      rules = [
        {
          required: false
        }
      ]
    }
    if (dataIndex === "currencyTo") {
      inputNode = (
        <Select
          value={record.currencyTo}
          size="large"
          placeholder="Currency To"
          suffixIcon={<img src={"/images/down-arrow.svg"} />}
          options={[
            {
              value: `${destinationFiatCurrency}`,
              label:
                destinationFiatCurrency === "SART"
                  ? /* destinationFiatCurrency.replace("T", "*")*/ "xQAR"
                  : `${destinationFiatCurrency}`
            }
          ]}
          style={{ width: "100%" }}
        />
      )

      rules = [
        {
          required: false,
          message: ""
        }
      ]
    }
    if (dataIndex === "baseRate") {
      inputNode = (
        <InputNumber
          min={0.01}
          placeholder="Base Rate"
          size="large"
          style={{ width: "100%" }}
          onKeyPress={numerAllowOnlyFiveDecimal}
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
                    title={`Base rate should be greater than 0.00`}
                    placement="bottom"
                  >
                    <img src={"/images/error.svg"} />
                  </Tooltip>
                )
              }
              if (parseInt(value) > 99999.99999) {
                return Promise.reject(
                  <Tooltip
                    title={`Length cannot be greater than 2 digits.`}
                    placement="bottom"
                  >
                    <img src={"/images/error.svg"} />
                  </Tooltip>
                )
              }
              if (value.substr(value.indexOf("."), 7).length === 7) {
                return Promise.reject(
                  <Tooltip
                    title={`Invalid number (Allow only numbers with 5 decimal places)`}
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
    if (dataIndex === "validFrom") {
      inputNode = (
        <>
          <Input.Group
            compact
            className="dateTime"
            style={{ display: "flex", width: "135%" }}
          >
            <DatePicker
              name="validFrom"
              size="large"
              className="datepicker-custom"
              style={{ width: "70%", zIndex: 1 }}
              placeholder="DD MM YYYY"
              format="D MMM YYYY"
              onChange={validFromDateEdit}
              value={
                selectedDateEdit !== ""
                  ? selectedDateEdit === null
                    ? null
                    : dayjs(selectedDateEdit)
                  : dayjs(record.validFrom)
              }
              disabledDate={disabledFromDate}
              inputReadOnly={true}
              allowClear={false}
              ref={datePickerRef}
            />
            <TimePicker
              size="large"
              style={{ width: "65%", zIndex: 1 }}
              disabledHours={() => disabledHours(dayjs(record.validFrom))}
              disabledMinutes={(value) =>
                disabledMinutes(value, dayjs(record.validFrom))
              }
              placeholder="HH:MM:SS"
              format={"HH:mm:ss"}
              value={
                selectedTimeEdit != ""
                  ? selectedTimeEdit == null
                    ? null
                    : dayjs(selectedTimeEdit)
                  : dayjs(record.validFrom)
              }
              allowClear={false}
              inputReadOnly={true}
              onChange={validFromTimeEdit}
              ref={timePickerRef}
            />
          </Input.Group>
        </>
      )

      rules = []
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

  const cancel = () => {
    setEditingKey("")
  }

  const save = async (key: React.Key) => {
    try {
      const row = (await form.validateFields()) as Item
      const newData = [...cdata]
      const index = newData.findIndex((item) => key === item.key)
      if (index > -1) {
        const item = newData[index]

        editConversionRate({
          id: item.key,
          currencyFrom: upperCase(row.currencyFrom),
          currencyTo: upperCase(row.currencyTo),
          baseRate: row.baseRate,
          validFrom:
            validFromEdit !== ""
              ? validFromEdit === null
                ? moment(currentDate()).format("YYYY-MM-DD[T]HH:mm:ssZ")
                : validFromEdit
              : row.validFrom,
          isActive: true
        })

        // clear
        setSelectedDateEdit("")
        setSelectedTimeEdit("")
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
            Currency From{" "}
            <Tooltip
              title={
                item?.sortColumn?.dataIndex === "currencyFrom" &&
                item?.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "currencyFrom" &&
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
      dataIndex: "currencyFrom",
      width: "16%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.currencyFrom.localeCompare(b.currencyFrom),
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
            Currency To{" "}
            <Tooltip
              title={
                item?.sortColumn?.dataIndex === "currencyTo" &&
                item?.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "currencyTo" &&
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
      dataIndex: "currencyTo",
      width: "17%",
      render: (currencyTo: string) => (
        <>
          {currencyTo === "SART"
            ? /* currencyTo.replace("T", "*") */ "xQAR"
            : currencyTo}
        </>
      ),
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.currencyTo.localeCompare(b.currencyTo),
      showSorterTooltip: false
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Base Rate{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "baseRate" &&
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
      dataIndex: "baseRate",
      width: "17%",
      editable: true,
      defaultSortOrder: "",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.baseRate - b.baseRate,
      showSorterTooltip: false
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Valid From{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "validFrom" &&
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
      dataIndex: "validFrom",
      width: "18%",
      render: (validFrom: string) => (
        <>{moment(validFrom).format("D MMM YYYY, HH:mm:ss")}</>
      ),
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.validFrom.localeCompare(b.validFrom),
      showSorterTooltip: false
    },
    {
      title: (item: any) => (
        <>
          <div style={{ display: "flex", alignItems: "center" }}>
            Created Date{" "}
            <Tooltip
              title={
                item.sortOrder != undefined && item.sortOrder === "ascend"
                  ? "Click to sort descending"
                  : "Click to sort ascending"
              }
            >
              <img
                src={
                  item?.sortColumn?.dataIndex === "createdAt" &&
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
      dataIndex: "createdAt",
      width: "18%",
      editable: true,
      defaultSortOrder: "ascend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a: any, b: any) => a.createdAt.localeCompare(b.createdAt),
      showSorterTooltip: false,
      render: (createdAt: string) => (
        <>{moment(createdAt).format("D MMM YYYY, HH:mm:ss")}</>
      )
    },
    {
      dataIndex: "operation",
      right: 0,
      width: "13%",
      render: (_: any, record: Item) => {
        const editable = isEditing(record)

        return editable ? (
          <span>
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
            {!record.currentActive ? (
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
            ) : null}
            &nbsp;&nbsp;&nbsp;&nbsp;
            {record.currentActive ? (
              <>
                <Space direction="vertical">
                  <Badge color="#13AB50" />
                </Space>
                &nbsp;
              </>
            ) : null}
            {!record.currentActive ? (
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
            ) : null}
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
          onConfirm={() => deactivateConverstionRates()}
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

  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  return (
    <div className="configurations conversion-rate">
      <PageHeading title={`Conversion Rates`} />
      <div className="p-2 ms-1">
        <Form
          name="basic"
          labelCol={{ span: 8 }}
          wrapperCol={{ span: 24 }}
          onFinish={onFinish}
          autoComplete="off"
          form={addform}
        >
          <div className="row bg-white boxShadow rounded WalletFilters">
            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                name="currencyFrom"
                rules={[{ required: true, message: "" }]}
              >
                <Select
                  size="large"
                  placeholder="Currency From"
                  value={currencyFrom == "" ? null : currencyFrom}
                  suffixIcon={
                    <img
                      style={{ zIndex: 99 }}
                      src={"/images/down-arrow.svg"}
                    />
                  }
                  options={[
                    {
                      value: `${defaultFiatCurrency}`,
                      label: `${defaultFiatCurrency}`
                    }
                  ]}
                  onChange={(e) => handleChange(e, "currencyFrom")}
                />
              </Form.Item>
            </div>

            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                name="currencyTo"
                rules={[{ required: true, message: "" }]}
              >
                <Select
                  size="large"
                  placeholder="Currency To"
                  value={currencyTo == "" ? null : currencyTo}
                  suffixIcon={
                    <img
                      style={{ zIndex: 99 }}
                      src={"/images/down-arrow.svg"}
                    />
                  }
                  options={[
                    {
                      value: `${destinationFiatCurrency}`,
                      label:
                        destinationFiatCurrency === "SART"
                          ? /* destinationFiatCurrency.replace("T", "*") */ "xQAR"
                          : `${destinationFiatCurrency}`
                    }
                  ]}
                  onChange={(e) => handleChange(e, "currencyTo")}
                />
              </Form.Item>
            </div>
            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Form.Item
                name="baseRate"
                initialValue={baseRate}
                rules={[
                  { required: true, message: "" },
                  {
                    type: "number",
                    min: 0.000001,
                    message: (
                      <Tooltip
                        title={`Base Rate should be greater than 0.00`}
                        placement="bottom"
                      >
                        <img src={"/images/error.svg"} />
                      </Tooltip>
                    )
                  },
                  {
                    type: "number",
                    max: 99999.99999,
                    message: (
                      <Tooltip
                        title={`Base Rate Length cannot be greater than 5 digits.`}
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
                        if (value.substr(value.indexOf("."), 7).length === 7) {
                          return Promise.reject(
                            <Tooltip
                              title={`Invalid number (Allow only numbers with 5 decimal places)`}
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
                  placeholder="Base Rate"
                  onChange={(e) => handleChange(e, "baseRate")}
                  style={{ width: "100%" }}
                  min={0}
                  onKeyPress={numerAllowOnlyFiveDecimal}
                />
              </Form.Item>
            </div>
            <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
              <Input.Group
                compact
                className="dateTime"
                style={{ display: "flex", width: "135%" }}
              >
                <DatePicker
                  name="selectedDate"
                  size="large"
                  className="datepicker-custom"
                  style={{ width: "70%", zIndex: 1 }}
                  placeholder="DD MM YYYY"
                  format="D MMM YYYY"
                  onSelect={(value: any) => setValidFromNew(value)}
                  onChange={validFromDate}
                  value={
                    selectedDate === "" || selectedDate === null
                      ? null
                      : dayjs(dateTimeFormat(selectedDate))
                  }
                  disabledDate={disabledFromDate}
                  inputReadOnly={true}
                  allowClear={false}
                  ref={datePickerRef}
                />
                <TimePicker
                  size="large"
                  style={{ width: "65%", zIndex: 1 }}
                  disabledHours={() => disabledHours()}
                  disabledMinutes={(value: any) => disabledMinutes(value, null)}
                  placeholder="HH:MM:SS"
                  format={"HH:mm:ss"}
                  value={
                    selectedTime === "" || selectedTime === null
                      ? null
                      : dayjs(dateTimeFormat(selectedTime))
                  }
                  allowClear={false}
                  inputReadOnly={true}
                  onChange={validFromTimeFun}
                  ref={timePickerRef}
                />
              </Input.Group>
            </div>
            <div className="col-xl-3 col-lg-6 col-sm-3 mt-3">&nbsp;</div>
            <div className="col-xl-1 col-lg-6 col-sm-3 mt-3">
              <Button
                htmlType="submit"
                style={{
                  background: isButtonDisabled ? "#F6F6F6" : buttonColor,
                  color: isButtonDisabled ? "#B6B6B6" : "#000",
                  width: "100%",
                  border: "none"
                }}
                className="button"
                size="large"
                disabled={isButtonDisabled}
              >
                Add
              </Button>
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
              className="table-custom conversion-rates language"
              components={{
                body: {
                  cell: EditableCell
                }
              }}
              dataSource={!search ? cdata : filterdData}
              columns={mergedColumns}
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
              rowClassName="editable-row default-row"
              pagination={false}
              locale={{ emptyText: "No conversion rates found!" }}
            />
          </Form>
        </div>
      </div>
    </div>
  )
}

export default ConversionRates

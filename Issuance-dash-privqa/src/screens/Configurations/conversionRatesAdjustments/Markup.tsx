import React, { useState, useEffect, useRef, useContext, useMemo } from "react"
import {
  Input,
  InputNumber,
  Select,
  Form,
  Table,
  Button,
  message as antMessage,
  Popconfirm,
  Typography,
  Dropdown,
  Space,
  Badge,
  MenuProps,
  DatePicker,
  TimePicker,
  DatePickerProps,
  Tooltip,
  notification
} from "antd"
import type { RangePickerProps } from "antd/es/date-picker"
import { upperCase, trim } from "lodash"
import { ThreeDots } from "react-loader-spinner"
import moment from "moment"
import dayjs from "dayjs"
import {
  useAddConversionRatesAdjustment,
  useEditConversionRatesAdjustment,
  useConversionRatesAdjustment
} from "src/api/user"
import { IS_INTEGER_REGEX } from "src/constants/Defaults"
import {
  ConversionRatesList,
  ConversionRatesAdjustmentItems,
  GetConversionRatesAdjustment
} from "src/api/models"
import { IssuanceContext } from "src/context/Merchant"
import { numerAllowOnlyTwoDecimal } from "src/utils"

const originData: any = []
interface EditableCellProps extends React.HTMLAttributes<HTMLElement> {
  editing: boolean
  dataIndex: string
  title: any
  inputType: "number" | "text"
  record: any
  index: number
  children: React.ReactNode
}

type Props = {
  buttonColor: string
}

const ConversionRates: React.FC<Props> = (Props) => {
  const { buttonColor } = Props
  const datePickerRef = useRef<any>(null)
  const timePickerRef = useRef<any>()
  const { issuanceDetails } = useContext(IssuanceContext)
  const defaultFiatCurrency = issuanceDetails?.fiatCurrency
    ? issuanceDetails?.fiatCurrency
    : "MYR"
  const destinationFiatCurrency = issuanceDetails?.defaultCurrency
  const [loading, setLoading] = useState<boolean>(true)
  const [currencyFrom, setCurrencyFrom] = useState<string>("")
  const [currencyTo, setCurrencyTo] = useState<string>("")
  const [percentage, setPercentage] = useState<number>(0)
  const [validFrom, setValidFrom] = useState<string | Date>("")
  const [selectedDate, setSelectedDate] = useState<string | Date>("")
  const [selectedTime, setSelectedTime] = useState<string | Date | null>("")
  const [selectedTimeString, setSelectedTimeString] = useState<string | Date>(
    ""
  )
  const [validFromNew, setValidFromNew] = useState(moment())
  const [checkDate, setCheckDate] = useState<boolean>(true)
  const [selectedDateEdit, setSelectedDateEdit] = useState<string | Date>("")
  const [selectedTimeEdit, setSelectedTimeEdit] = useState<string | Date>("")
  const [selectedTimeStringEdit, setSelectedTimeStringEdit] = useState<
    string | Date
  >("")
  const [validFromEdit, setValidFromEdit] = useState<string | Date>("")
  const [isButtonDisabled, setIsButtonDisabled] = useState<boolean>(false)
  const [form] = Form.useForm()
  const [addform] = Form.useForm()
  const [cdata, setData] = useState<ConversionRatesList | any>(originData)
  const [filterdData, setFilterdData] = useState<ConversionRatesList | any>([])
  const [search, setSearch] = useState<boolean>(false)
  const [editingKey, setEditingKey] = useState("")
  const [deactivateRow, setDeactivateRow] = useState<any>()
  const [isDeactive, setIsDeactive] = useState<boolean>(false)

  // API Call Add Conversion Rates
  const {
    mutate: addConversionRates,
    error: addConversionRatesError,
    isSuccess: isSuccess
  } = useAddConversionRatesAdjustment()

  // API Call Edit Transaction Limit
  const {
    mutate: editConversionRate,
    error: editConversionRateError,
    isSuccess: editConversionRateSuccess
  } = useEditConversionRatesAdjustment()

  // get Conversion Rates Adjustment list API
  const {
    mutate: conversionRatesAdjustment,
    data,
    error
  } = useConversionRatesAdjustment()

  const handleChange = (e: any, name: string) => {
    if (name === "currencyFrom") {
      setCurrencyFrom(e)
    }
    if (name === "currencyTo") {
      setCurrencyTo(e)
    }
    if (name === "percentage") {
      if (e === "" || IS_INTEGER_REGEX.test(e)) {
        setPercentage(e === null ? 0 : e)
      }
    }
    if (name === "search") {
      setSearch(e.target.value === "" ? false : true)

      const data = cdata.map((element: any, key: number) => {
        return {
          // key: element.key,
          currencyFrom: element.currencyFrom,
          currencyTo: element.currencyTo,
          percentage: element.percentage,
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
      const currentTimeFormatted = moment().format("HH:mm:ss")

      setSelectedTime(new Date())
      setSelectedTimeString(`${currentTimeFormatted}`)
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

  const onFinish = () => {
    addConversionRates({
      currencyFrom,
      currencyTo,
      percentage,
      isActive: true,
      markType: "UP",
      validFrom:
        validFrom != ""
          ? validFrom
          : moment(currentDate()).format("YYYY-MM-DD[T]HH:mm:ssZ")
    })

    // Clear/Reset from
    clearFrom()
  }

  const currentDate = () => {
    return dayjs(moment().format()).format("D MMM YYYY, HH:mm:ss")
  }

  const clearFrom = () => {
    addform.resetFields()
    addform.setFieldsValue({ baseRate: 0 })

    setSelectedDate("")
    setSelectedTime("")
    setValidFrom("")
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
          defaultValue={record.currencyFrom}
          size="large"
          placeholder="Currency From"
          suffixIcon={
            <img style={{ zIndex: 99 }} src={"/images/down-arrow.svg"} />
          }
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
          defaultValue={record.currencyTo}
          size="large"
          placeholder="Currency To"
          suffixIcon={
            <img style={{ zIndex: 99 }} src={"/images/down-arrow.svg"} />
          }
          options={[
            {
              value: `${destinationFiatCurrency}`,
              label:
                destinationFiatCurrency === "SART"
                  ? /* destinationFiatCurrency.replace("T", "*") */ "xQAR"
                  : destinationFiatCurrency
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
    if (dataIndex === "percentage") {
      inputNode = (
        <InputNumber
          size="large"
          style={{ width: "100%" }}
          placeholder="Percentage"
          onKeyPress={numerAllowOnlyTwoDecimal}
        />
      )

      rules = [
        {
          required: true,
          message: `sdasdsad`
        },
        {
          min: 0,
          message: (
            <Tooltip
              title={`Percentage should be greater than 0.00`}
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
              if (parseInt(value) < 1) {
                return Promise.reject(
                  new Error("Percentage should be greater than 0 digit")
                )
              }
              if (parseInt(value) > 99.99) {
                return Promise.reject(
                  new Error("Length cannot be greater than 2 digits.")
                )
              }
              if (value.substr(value.indexOf("."), 4).length === 4) {
                return Promise.reject(
                  new Error(
                    "Invalid number (Allow only numbers with 2 decimal places)"
                  )
                )
              }
            }
          }
        }
      ]
    }
    if (dataIndex === "validFrom") {
      inputNode = (
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
                ? dayjs(selectedTimeEdit)
                : dayjs(record.validFrom)
            }
            allowClear={false}
            inputReadOnly={true}
            onChange={validFromTimeEdit}
          />
        </Input.Group>
      )
    }

    return (
      <td {...restProps}>
        {editing ? (
          <>
            <div style={{ width: "222% !important" }}></div>
            <Form.Item
              name={dataIndex}
              style={{ margin: 0 }}
              rules={[
                {
                  required: true,
                  message: ``
                }
              ]}
            >
              {inputNode}
            </Form.Item>
          </>
        ) : (
          children
        )}
      </td>
    )
  }

  const deactivateConverstionRates = () => {
    if (deactivateRow != null) {
      editConversionRate({
        id: deactivateRow.key,
        currencyFrom: deactivateRow.currencyFrom,
        currencyTo: deactivateRow.currencyTo,
        percentage: deactivateRow.percentage,
        markType: "UP",
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
    conversionRatesAdjustment({
      markType: "UP"
    })
  }, [])

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
      const markUpList: GetConversionRatesAdjustment = data
      const originData = markUpList.map((element: any, key: number) => {
        return {
          key: element.id,
          currencyFrom: element.currencyFrom,
          currencyTo: element.currencyTo,
          percentage: element.percentage,
          validFrom: element.validFrom,
          createdAt: element.createdAt,
          isActive: element.isActive,
          currentActive: element.currentActive
        }
      })

      setData(originData)
      setLoading(false)
    }
  }, [data])

  useEffect(() => {
    if (currencyFrom !== null && currencyTo !== null && percentage !== 0) {
      setIsButtonDisabled(false)
    } else {
      setIsButtonDisabled(false)
    }
  }, [currencyFrom, currencyTo, percentage, validFrom])

  useEffect(() => {
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Conversion rate adjustment saved!"
      })

      // refetch conversion rates list
      conversionRatesAdjustment({
        markType: "UP"
      })
    }
    if (addConversionRatesError) {
      antMessage.error(addConversionRatesError)
    }
  }, [isSuccess, addConversionRatesError])

  useEffect(() => {
    if (editConversionRateSuccess) {
      notification["success"]({
        message: "Notification",
        description: isDeactive
          ? "Conversion rate adjustment deactivated."
          : `Conversion rate adjustment updated successfully.`
      })
      // refetch Convertion rate list
      conversionRatesAdjustment({
        markType: "UP"
      })

      setIsDeactive(false)
    }
    if (editConversionRateError) {
      antMessage.error(editConversionRateError)
    }
  }, [editConversionRateSuccess, editConversionRateError])

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
  const isEditing = (record: ConversionRatesAdjustmentItems) =>
    record.key === editingKey

  const edit = (
    record: Partial<ConversionRatesAdjustmentItems> & { key: React.Key }
  ) => {
    form.setFieldsValue({ name: "", age: "", address: "", ...record })
    setEditingKey(record.key)
    setValidFromEdit("")
    setSelectedDateEdit(moment(record.validFrom).format("YYYY-MM-DD"))
    setSelectedTimeStringEdit(moment(record.validFrom).format("HH:mm:ss"))
  }

  const cancel = () => {
    setEditingKey("")
  }

  const save = async (key: React.Key) => {
    try {
      const row =
        (await form.validateFields()) as ConversionRatesAdjustmentItems

      const newData = [...cdata]
      const index = newData.findIndex((item) => key === item.key)
      if (index > -1) {
        const item = newData[index]

        // save Conversion Rates
        editConversionRate({
          id: item.key,
          currencyFrom: upperCase(row.currencyFrom),
          currencyTo: upperCase(row.currencyTo),
          percentage: row.percentage,
          markType: "UP",
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
      title: "Currency From",
      dataIndex: "currencyFrom",
      width: "16%",
      editable: true,
      render: (key: string) => (
        <>
          <div className="tableRows more-screen-size-600"></div>
          {key}
        </>
      )
    },
    {
      title: "Currency To",
      dataIndex: "currencyTo",
      width: "17%",
      render: (currencyTo: string) => (
        <>
          {currencyTo === "SART"
            ? /* currencyTo.replace("T", "*") */ "xQAR"
            : currencyTo}
        </>
      ),
      editable: true
    },
    {
      title: "Percentage",
      dataIndex: "percentage",
      width: "17%",
      editable: true
    },
    {
      title: "Valid From",
      dataIndex: "validFrom",
      width: "17%",
      render: (validFrom: string) => (
        <>{moment(validFrom).format("D MMM YYYY, HH:mm:ss")}</>
      ),
      editable: true
    },
    {
      title: "Created Date",
      dataIndex: "createdAt",
      width: "20%",
      render: (createdAt: string) => (
        <>{moment(createdAt).format("D MMM YYYY, HH:mm:ss")}</>
      ),
      editable: true
    },
    {
      dataIndex: "operation",
      right: 0,
      width: "13%",
      render: (_: any, record: ConversionRatesAdjustmentItems) => {
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
      onCell: (record: ConversionRatesAdjustmentItems) => ({
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

  return (
    <div className="p-1 mark-up boxShadow">
      <p style={{ color: "#000000", fontSize: "18px", fontWeight: 600 }}>
        {defaultFiatCurrency} to{" "}
        {destinationFiatCurrency === "SART"
          ? /*destinationFiatCurrency.replace("T", "*")*/ "xQAR"
          : destinationFiatCurrency}
        (-)
      </p>
      <Form
        name="basic"
        labelCol={{ span: 8 }}
        wrapperCol={{ span: 24 }}
        initialValues={{ remember: true }}
        onFinish={onFinish}
        autoComplete="off"
        form={addform}
      >
        <div className="row bg-white rounded WalletFilters">
          <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
            <Form.Item
              name="currencyFrom"
              rules={[{ required: true, message: "" }]}
            >
              <Select
                size="large"
                placeholder="Currency From"
                suffixIcon={
                  <img style={{ zIndex: 99 }} src={"/images/down-arrow.svg"} />
                }
                options={[
                  {
                    value: `${defaultFiatCurrency}`,
                    label: `${defaultFiatCurrency}`
                  }
                ]}
                onChange={(e) => handleChange(e, "currencyFrom")}
                value={currencyFrom === "" ? null : currencyFrom}
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
                suffixIcon={
                  <img style={{ zIndex: 99 }} src={"/images/down-arrow.svg"} />
                }
                options={[
                  {
                    value: `${destinationFiatCurrency}`,
                    label:
                      destinationFiatCurrency === "SART"
                        ? /*destinationFiatCurrency.replace("T", "*")*/ "xQAR"
                        : destinationFiatCurrency
                  }
                ]}
                onChange={(e) => handleChange(e, "currencyTo")}
                value={currencyTo === "" ? null : currencyTo}
              />
            </Form.Item>
          </div>
          <div className="col-xl-2 col-lg-6 col-sm-3 mt-3">
            <Form.Item
              name="percentage"
              rules={[
                { required: true, message: "" },
                {
                  type: "number",
                  min: 0,
                  message: (
                    <Tooltip
                      title={`Percentage should be greater than 0.00`}
                      placement="bottom"
                    >
                      <img src={"/images/error.svg"} />
                    </Tooltip>
                  )
                },
                {
                  type: "number",
                  max: 99.99,
                  message: (
                    <Tooltip
                      title={`Percentage Length cannot be greater than 2 digits.`}
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
              ]}
            >
              <InputNumber
                size="large"
                placeholder="Percentage"
                onChange={(e) => handleChange(e, "percentage")}
                value={percentage}
                style={{ width: "100%" }}
                onKeyPress={numerAllowOnlyTwoDecimal}
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
            {/* <Form.Item>
              <DatePicker
                size="large"
                className="datepicker-custom"
                id="fromFromDate"
                style={{ width: "120%" }}
                placeholder="DD MM YYYY, HH:MM:SS"
                // showTime={{ format: "HHH:mm:ss" }}
                showTime={{
                  format: "HH:mm:ss",
                  defaultValue: dayjs("00:00:00", "HH:mm:ss")
                }}
                format="DD MMM YYYY HH:mm:ss" // D MMM YYYY hh:mm:ss
                onSelect={(value: any) => setValidFromNew(value)}
                onChange={validFromDate}
                value={
                  validFrom === "" || validFrom === null
                    ? null
                    : dayjs(dateTimeFormat(validFrom))
                }
                disabledDate={disabledFromDate}
                disabledHours={() => disabledHours()}
                disabledMinutes={(value: any) => disabledMinutes(value, null)}
                inputReadOnly={true}
              />
            </Form.Item> */}
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
      <div
        className="row bg-white mt-2"
        style={{ borderTop: "10px solid #f6f6f6" }}
      >
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
      <div className="row bg-white mt-0">
        <div className="table-responsive">
          <Form form={form} component={false}>
            <Table
              className="table-custom conversion-rates-adjustment language"
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
              locale={{ emptyText: "No mark up found!" }}
            />
          </Form>
        </div>
      </div>
    </div>
  )
}

export default ConversionRates

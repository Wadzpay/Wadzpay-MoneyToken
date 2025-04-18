import React, { useEffect, useState } from "react"
import { capitalize } from "lodash"
import TimezoneSelect, { allTimezones } from "react-timezone-select"
import type { ITimezone } from "react-timezone-select"
import { Input, Form, Checkbox, Upload, Select, DatePicker, Button } from "antd"
import moment from "moment"

const { TextArea } = Input

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 }
]

const { Option } = Select

const PrimaryBusinessDetails = (props: any) => {
  const [form] = Form.useForm()
  const [institutionName, setInstitutionName] = useState<string>("MBSB")

  const [tz, setTz] = useState<ITimezone>({
    value: "Asia/Kolkata",
    label: "(Chennai, Kolkata, Mumbai, New Delhi)",
    offset: 8,
    abbrev: "IST",
    altName: "India Standard Time"
  })

  const onChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    console.log("Change:", e.target.value)
  }

  const disabledDate = (current: any) => {
    return current && current.isBefore(moment().subtract(1, "days"))
  }

  const handleChange = (e: any, name: string) => {
    if (name === "institutionName") {
      setInstitutionName(e.target.value)
    }
  }

  const onFinish = (values: any) => {
    const data = {
      institutionName: {
        value: values.institutionName,
        isMandatoryField: values.institutionNameConfig.includes(1),
        isShow: values.institutionNameConfig.includes(2),
        isEdit: values.institutionNameConfig.includes(3)
      },
      institutionAbbreviation: {
        value: values.institutionAbbreviation,
        isMandatoryField: values.institutionAbbreviationConfig.includes(1),
        isShow: values.institutionAbbreviationConfig.includes(2),
        isEdit: values.institutionAbbreviationConfig.includes(3)
      },
      institutionDescription: {
        value: values.institutionDescription,
        isMandatoryField: values.institutionDescriptionConfig.includes(1),
        isShow: values.institutionDescriptionConfig.includes(2),
        isEdit: values.institutionDescriptionConfig.includes(3)
      },
      institutionRegion: {
        value: values.institutionRegion,
        isMandatoryField: values.institutionRegionConfig.includes(1),
        isShow: values.institutionRegionConfig.includes(2),
        isEdit: values.institutionRegionConfig.includes(3)
      },
      institutionTimeZone: {
        value: values.institutionTimeZone.value,
        isMandatoryField: values.institutionTimeZoneConfig.includes(1),
        isShow: values.institutionTimeZoneConfig.includes(2),
        isEdit: values.institutionTimeZoneConfig.includes(3)
      },
      industryType: {
        value: values.industryType,
        isMandatoryField: values.industryTypeConfig.includes(1),
        isShow: values.industryTypeConfig.includes(2),
        isEdit: values.industryTypeConfig.includes(3)
      },
      defaultCurrency: {
        value: values.defaultCurrency,
        isMandatoryField: values.defaultCurrencyConfig.includes(1),
        isShow: values.defaultCurrencyConfig.includes(2),
        isEdit: values.industryTypeConfig.includes(3)
      },
      destinationCurrency: {
        value: values.destinationCurrency,
        isMandatoryField: values.destinationCurrencyConfig.includes(1),
        isShow: values.destinationCurrencyConfig.includes(2),
        isEdit: values.destinationCurrencyConfig.includes(3)
      },
      activationDate: {
        value: values.activationDate,
        isMandatoryField: values.activationDateConfig.includes(1),
        isShow: values.activationDateConfig.includes(2),
        isEdit: values.activationDateConfig.includes(3)
      }
    }

    // props.saveInstitutionData(data)
  }

  useEffect(() => {
    if (institutionName !== "") {
      props.handleCallBack(false)
    } else {
      props.handleCallBack(true)
    }
  }, [institutionName])

  return (
    <div>
      <Form name="basic" form={form} onFinish={onFinish} autoComplete="off">
        {/* <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              label="Institution ID"
              name="institution_id"
              rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter institution ID" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionNameConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div> */}
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name="institutionName"
              label="Institution Name"
              rules={[{ required: true, message: "" }]}
              initialValue={"MBSB"}
            >
              <Input
                placeholder="Enter Name"
                onChange={(e) => handleChange(e, "institutionName")}
                defaultValue={"MBSB"}
                value={"MBSB"}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionNameConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name="institutionAbbreviation"
              label="Institution Abbreviation"
              rules={[{ required: true, message: "" }]}
              initialValue={""}
            >
              <Input placeholder="Enter iInstitution Abbreviation" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionAbbreviationConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name={"institutionDescription"}
              label="Institution Description"
              // rules={[{ required: true, message: "" }]}
              initialValue={""}
            >
              <TextArea
                showCount
                maxLength={350}
                onChange={onChange}
                placeholder="Address Line 1"
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionDescriptionConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item label="Institution Logo" name={"institutionLogo"}>
              {/* <Input placeholder="Enter institution ID" /> */}
              <Upload
                name="avatar"
                listType="picture-card"
                className="avatar-uploader"
                // beforeUpload={beforeUpload}
                // onChange={handleChange}
              >
                <img src={"/images/attach-file.svg"} alt="avatar" />
                <span className="chooseLogo">Choose Logo</span>
              </Upload>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionLogoConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name="institutionRegion"
              label="Institution Region"
              initialValue={""}
            >
              <Input placeholder="Select region" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionRegionConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              label="Institution Time Zone"
              initialValue={""}
              name="institutionTimeZone"
            >
              <TimezoneSelect
                placeholder="Select time zone"
                value={tz}
                timezones={{
                  ...allTimezones,
                  "America/Lima": "Pittsburgh",
                  "Europe/Berlin": "Frankfurt"
                }}
                components={{
                  IndicatorSeparator: () => null
                }}
                // isDisabled={true}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionTimeZoneConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name="industryType"
              label="Institution Type"
              initialValue={""}
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Select type of the institution"
              >
                {props?.industryTypeList != undefined &&
                  props?.industryTypeList.map(
                    (element: string, key: number) => (
                      <Option key={key}>
                        {capitalize(element.replaceAll("_", " "))}
                      </Option>
                    )
                  )}
              </Select>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="industryTypeConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name="defaultCurrency"
              label="Default Domestic currency"
              initialValue={""}
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Select Default Domestic Currency"
              >
                {props?.fiatCurrencyList != undefined &&
                  props?.fiatCurrencyList.map(
                    (currency: string, key: number) => (
                      <Option key={key}>{currency}</Option>
                    )
                  )}
              </Select>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="defaultCurrencyConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name="destinationCurrency"
              label="Destination Currencies Supported"
              initialValue={""}
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Select destination currencies supported"
              >
                {props?.fiatCurrencyList != undefined &&
                  props?.fiatCurrencyList.map(
                    (currency: string, key: number) => (
                      <Option key={key}>{currency}</Option>
                    )
                  )}
              </Select>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="destinationCurrencyConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name="activationDate"
              label="Activation Date"
              // rules={[{ required: true, message: "" }]}
              initialValue={""}
            >
              <DatePicker
                style={{ width: "100%" }}
                disabledDate={disabledDate}
                format={"DD/MM/YYYY"}
                placeholder="DD/MM/YYYY"
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="activationDateConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
        </div>
        <Form.Item style={{ display: "none" }}>
          <Button
            htmlType="submit"
            ref={props?.formRef}
            onClick={() => {
              // console.log("MOHIT----------")
            }}
          >
            Save
          </Button>
        </Form.Item>
      </Form>
    </div>
  )
}

export default PrimaryBusinessDetails

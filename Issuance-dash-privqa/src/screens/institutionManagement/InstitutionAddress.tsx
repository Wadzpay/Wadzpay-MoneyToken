import React from "react"
import { Input, Form, Checkbox, Button, Select } from "antd"

import { countries } from "../../api/constants"

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 }
]

const InstitutionAddress = (props: any) => {
  const [form] = Form.useForm()

  const onFinish = (values: any) => {
    const data = {
      addressLine1: {
        value: values.addressLine1,
        isMandatoryField: values.addressLine1Config.includes(1),
        isShow: values.addressLine1Config.includes(2),
        isEdit: values.addressLine1Config.includes(3)
      },
      addressLine2: {
        value: values.addressLine2,
        isMandatoryField: values.addressLine2Config.includes(1),
        isShow: values.addressLine2Config.includes(2),
        isEdit: values.addressLine2Config.includes(3)
      },
      addressLine3: {
        value: values.addressLine3,
        isMandatoryField: values.addressLine3Config.includes(1),
        isShow: values.addressLine3Config.includes(2),
        isEdit: values.addressLine3Config.includes(3)
      },
      city: {
        value: values.city,
        isMandatoryField: values.cityConfig.includes(1),
        isShow: values.cityConfig.includes(2),
        isEdit: values.cityConfig.includes(3)
      },
      provice: {
        value: values.provice,
        isMandatoryField: values.proviceConfig.includes(1),
        isShow: values.proviceConfig.includes(2),
        isEdit: values.proviceConfig.includes(3)
      },
      country: {
        value: values.country,
        isMandatoryField: values.countryConfig.includes(1),
        isShow: values.countryConfig.includes(2),
        isEdit: values.countryConfig.includes(3)
      },
      postalCode: {
        value: values.postalCode,
        isMandatoryField: values.postalCodeConfig.includes(1),
        isShow: values.postalCodeConfig.includes(2),
        isEdit: values.postalCodeConfig.includes(3)
      }
    }

    // props.saveInstitutionData(data)
  }

  return (
    <div>
      <Form name="basic" form={form} onFinish={onFinish} autoComplete="off">
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name="addressLine1"
              label="Address Line 1"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter Address Line 1" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="addressLine1Config"
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
              name={"addressLine2"}
              label="Address Line 2"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter Address Line 2" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="addressLine2Config"
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
              name={"addressLine3"}
              label="Address Line 3"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter Address Line 3" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="addressLine3Config"
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
              name="city"
              label="City"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter City" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="cityConfig"
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
              name="province"
              label="Province / State"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter State" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="provinceConfig"
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
            <Form.Item name="country" label="Country">
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Search Country"
              >
                <Select.Option>Select</Select.Option>
                {countries.map((country: any, key: number) => (
                  <Select.Option value={country.name} key={key}>
                    {country.name}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="countryConfig"
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
            <Form.Item name="postalCode" label="Postal Code">
              <Input placeholder="Postal Code" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="postalCodeConfig"
              valuePropName="checked"
              initialValue={[1, 2, 3]}
            >
              <Checkbox.Group
                options={checkboxOptions}
                defaultValue={[1, 2, 3]}
              />
            </Form.Item>
          </div>
          <Form.Item style={{ display: "none" }}>
            <Button htmlType="submit" ref={props?.formRef}>
              Save
            </Button>
          </Form.Item>
        </div>
      </Form>
    </div>
  )
}

export default InstitutionAddress

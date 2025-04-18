import React from "react"
import { Input, Form, Checkbox, Button, Select } from "antd"

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 }
]

const ContactPersonDetails = (props: any) => {
  const [form] = Form.useForm()

  const onFinish = (values: any) => {
    const data = {
      primaryContactFirstName: {
        value: values.primaryContactFirstName,
        isMandatoryField: values.primaryContactFirstNameConfig.includes(1),
        isShow: values.primaryContactFirstNameConfig.includes(2),
        isEdit: values.primaryContactFirstNameConfig.includes(3)
      },
      primaryContactMiddleName: {
        value: values.primaryContactMiddleName,
        isMandatoryField: values.primaryContactMiddleNameConfig.includes(1),
        isShow: values.primaryContactMiddleNameConfig.includes(2),
        isEdit: values.primaryContactMiddleNameConfig.includes(3)
      },
      primaryContactLastName: {
        value: values.primaryContactLastName,
        isMandatoryField: values.primaryContactLastNameConfig.includes(1),
        isShow: values.primaryContactLastNameConfig.includes(2),
        isEdit: values.primaryContactLastNameConfig.includes(3)
      },
      primaryContactEmailId: {
        value: values.primaryContactEmailId,
        isMandatoryField: values.primaryContactEmailIdConfig.includes(1),
        isShow: values.primaryContactEmailIdConfig.includes(2),
        isEdit: values.primaryContactEmailIdConfig.includes(3)
      },
      primaryContactPhoneNumber: {
        value: values.primaryContactPhoneNumber,
        isMandatoryField: values.primaryContactPhoneNumberConfig.includes(1),
        isShow: values.primaryContactPhoneNumberConfig.includes(2),
        isEdit: values.primaryContactPhoneNumberConfig.includes(3)
      },
      primaryContactDesignation: {
        value: values.primaryContactDesignation,
        isMandatoryField: values.primaryContactDesignationConfig.includes(1),
        isShow: values.primaryContactDesignationConfig.includes(2),
        isEdit: values.primaryContactDesignationConfig.includes(3)
      },
      primaryContactDepartment: {
        value: values.primaryContactDepartment,
        isMandatoryField: values.primaryContactDepartmentConfig.includes(1),
        isShow: values.primaryContactDepartmentConfig.includes(2),
        isEdit: values.primaryContactDepartmentConfig.includes(3)
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
              name="primaryContactFirstName"
              label="First Name"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter first name" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="primaryContactFirstNameConfig"
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
              name={"primaryContactMiddleName"}
              label="Middle Name"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter middle name" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="primaryContactMiddleNameConfig"
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
              name={"primaryContactLastName"}
              label="Last Name"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter last name" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="primaryContactLastNameConfig"
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
              name={"primaryContactEmailId"}
              label="Email ID"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter email ID" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="primaryContactEmailIdConfig"
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
              name={"primaryContactPhoneNumber"}
              label="Mobile Number"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter mobile number" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="primaryContactPhoneNumberConfig"
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
              name={"primaryContactDesignation"}
              label="Designation"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter designation" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="primaryContactDesignationConfig"
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
              name="primaryContactDepartment"
              label="Department"
              initialValue={""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input placeholder="Enter department" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="primaryContactDepartmentConfig"
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

export default ContactPersonDetails

import React from "react"
import { Input, Form, Checkbox, Button } from "antd"

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 }
]

const AdminDetails = (props: any) => {
  const [form] = Form.useForm()

  const onFinish = (values: any) => {
    const data = {
      adminFirstName: {
        value: values.adminFirstName,
        isMandatoryField: values.adminFirstNameConfig.includes(1),
        isShow: values.adminFirstNameConfig.includes(2),
        isEdit: values.adminFirstNameConfig.includes(3)
      },
      adminMiddleName: {
        value: values.adminMiddleName,
        isMandatoryField: values.adminMiddleNameConfig.includes(1),
        isShow: values.adminMiddleNameConfig.includes(2),
        isEdit: values.adminMiddleNameConfig.includes(3)
      },
      adminEmailId: {
        value: values.adminEmailId,
        isMandatoryField: values.adminEmailIdConfig.includes(1),
        isShow: values.adminEmailIdConfig.includes(2),
        isEdit: values.adminEmailIdConfig.includes(3)
      },
      adminPhoneNumber: {
        value: values.adminPhoneNumber,
        isMandatoryField: values.adminPhoneNumberConfig.includes(1),
        isShow: values.adminPhoneNumberConfig.includes(2),
        isEdit: values.adminPhoneNumberConfig.includes(3)
      },
      adminDepartment: {
        value: values.adminDepartment,
        isMandatoryField: values.adminDepartmentConfig.includes(1),
        isShow: values.adminDepartmentConfig.includes(2),
        isEdit: values.adminDepartmentConfig.includes(3)
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
              name={"adminFirstName"}
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
              name="adminFirstNameConfig"
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
              name={"adminMiddleName"}
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
              name="adminMiddleNameConfig"
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
              name={"adminLastName"}
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
              name="adminLastNameConfig"
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
              name="adminEmailId"
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
              name="adminEmailIdConfig"
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
              name={"adminPhoneNumber"}
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
              name="adminPhoneNumberConfig"
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
              name="adminDepartment"
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
              name="adminDepartmentConfig"
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

export default AdminDetails

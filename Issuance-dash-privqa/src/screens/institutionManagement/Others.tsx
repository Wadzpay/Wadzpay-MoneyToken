import React from "react"
import { Radio, Form, Checkbox, Button } from "antd"

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
        <div className="form-div mb-3">
          <div className="col-lg-4">Customer Offline Transaction</div>
          <div className="col-lg-3" style={{ textAlign: "right" }}>
            <Form.Item
              name="customerOfflineTransactionConfig"
              initialValue={"1"}
            >
              <Radio.Group defaultValue={"1"}>
                <Radio value={"1"}>Enable</Radio>
                <Radio value={"2"}>Disable</Radio>
              </Radio.Group>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="customerOfflineTransactionConfig"
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
        <div className="form-div mb-3">
          <div className="col-lg-4">Merchant Offline Transaction</div>
          <div className="col-lg-3" style={{ textAlign: "right" }}>
            <Form.Item name="merchantOfflineTransaction" initialValue={"1"}>
              <Radio.Group defaultValue={"1"}>
                <Radio value={"1"}>Enable</Radio>
                <Radio value={"2"}>Disable</Radio>
              </Radio.Group>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="merchantOfflineTransactionConfig"
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
        <div className="form-div mb-3">
          <div className="col-lg-4">Institution Status</div>
          <div className="col-lg-3" style={{ textAlign: "right" }}>
            <Form.Item name="institutionStatus" initialValue={"1"}>
              <Radio.Group defaultValue={"1"}>
                <Radio value={"1"}>Enable</Radio>
                <Radio value={"2"}>Disable</Radio>
              </Radio.Group>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionStatusConfig"
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
        <div className="form-div mb-3">
          <div className="col-lg-4">Approval Work Flow</div>
          <div className="col-lg-3" style={{ textAlign: "right" }}>
            <Form.Item name="approvalWorkFlow" initialValue={"1"}>
              <Radio.Group defaultValue={"1"}>
                <Radio value={"1"}>Enable</Radio>
                <Radio value={"2"}>Disable</Radio>
              </Radio.Group>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="approvalWorkFlowConfig"
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
        <div className="form-div mb-4">
          <div className="col-lg-4">P2P Transfer</div>
          <div className="col-lg-3" style={{ textAlign: "right" }}>
            <Form.Item name="p2pTransfer" initialValue={"1"}>
              <Radio.Group defaultValue={"1"}>
                <Radio value={"1"}>Enable</Radio>
                <Radio value={"2"}>Disable</Radio>
              </Radio.Group>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="p2pTransferConfig"
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

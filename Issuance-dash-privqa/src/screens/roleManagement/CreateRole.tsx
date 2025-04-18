import React from "react"
import PageHeading from "src/components/ui/PageHeading"
import { Form, Input, Select, Divider } from "antd"

const CreateRole = () => {
  const [form] = Form.useForm()

  return (
    <div>
      <PageHeading title="Create Role" backIcon={true} />
      <div
        className="row bg-white institution-div p-4"
        style={{ minHeight: 500 }}
      >
        <div className="col-lg-3">
          <Form form={form} layout="vertical">
            <Form.Item label="Role Name">
              <Input placeholder="Enter Role Name" />
            </Form.Item>
            <Form.Item label="Create Role from">
              <Select
                placeholder="Select"
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                dropdownRender={(menu) => (
                  <div>
                    {menu}
                    <Divider style={{ margin: "4px 0" }} />
                    <div
                      style={{ padding: "4px 8px", cursor: "pointer" }}
                      onMouseDown={(e) => e.preventDefault()}
                      //   onClick={}
                    >
                      Create A Custom Role & Permissions
                    </div>
                  </div>
                )}
              >
                <Select.Option value="1">Existing role</Select.Option>
                <Select.Option value="2">Existing user</Select.Option>
              </Select>
            </Form.Item>
          </Form>
        </div>
      </div>
    </div>
  )
}

export default CreateRole

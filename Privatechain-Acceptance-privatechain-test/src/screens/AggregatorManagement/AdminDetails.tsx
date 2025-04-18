import React from "react";
import { Input, Form, Checkbox, Button } from "antd";
import { mobileRegex } from "src/constants/validationSchemas";

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];

const AdminDetails = (props: any) => {
  const [form] = Form.useForm();

  const onFinish = (values: any) => {
    const data = {
      aggregatorPreferenceId: {
        value: props.aggregatorId,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityAdminDetailsFirstName: {
        value: values.adminFirstName,
        isMandatoryField: values.adminFirstNameConfig.includes(1),
        isShow: values.adminFirstNameConfig.includes(2),
        isEdit: values.adminFirstNameConfig.includes(3),
      },
      entityAdminDetailsMiddleName: {
        value: values.adminMiddleName,
        isMandatoryField: values.adminMiddleNameConfig.includes(1),
        isShow: values.adminMiddleNameConfig.includes(2),
        isEdit: values.adminMiddleNameConfig.includes(3),
      },
      entityAdminDetailsLastName: {
        value: values.adminLastName,
        isMandatoryField: values.adminLastNameConfig.includes(1),
        isShow: values.adminLastNameConfig.includes(2),
        isEdit: values.adminLastNameConfig.includes(3),
      },
      entityAdminDetailsEmailId: {
        value: values.adminEmailId,
        isMandatoryField: values.adminEmailIdConfig.includes(1),
        isShow: values.adminEmailIdConfig.includes(2),
        isEdit: values.adminEmailIdConfig.includes(3),
      },
      entityAdminDetailsMobileNumber: {
        value: values.adminPhoneNumber,
        isMandatoryField: values.adminPhoneNumberConfig.includes(1),
        isShow: values.adminPhoneNumberConfig.includes(2),
        isEdit: values.adminPhoneNumberConfig.includes(3),
      },
      entityAdminDetailsDepartment: {
        value: values.adminDepartment,
        isMandatoryField: values.adminDepartmentConfig.includes(1),
        isShow: values.adminDepartmentConfig.includes(2),
        isEdit: values.adminDepartmentConfig.includes(3),
      },
    };
    props.saveInstitutionData(data);

    props.handleCallBack(true);
  };

  const onFinishFailed = (errorInfo: any) => {
    props.handleCallBack(false);
  };

  return (
    <div>
      <Form
              autoComplete="off"

        name="basic"
        form={props.form}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
      >
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              name={"adminFirstName"}
              label="First Name"
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.adminDetails.entityAdminDetailsFirstName : ""}
              rules={[{ required: true, message: "Please enter First Name" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter first name" /* autoComplete="true" *//>
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.adminDetails.entityAdminDetailsMiddleName : ""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter middle name" /* autoComplete="true" *//>
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.adminDetails.entityAdminDetailsLastName : ""}
              rules={[{ required: true, message: "Please enter Last Name" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter last name" /* autoComplete="true" *//>
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.adminDetails.entityAdminDetailsEmailId : ""}
              rules={[{ required: true, message: "Please enter Email ID" },{
            pattern:new RegExp(/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w\w+)+$/),
            message:'Please enter valid Email  ID'
          }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter email ID" /* autoComplete="true" *//>
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.adminDetails.entityAdminDetailsMobileNumber : ""}
              rules={[{ required: true ,message:'Please enter  Mobile Number'},                {
                  pattern: new RegExp(mobileRegex),
                  message: 'Please enter valid Mobile Number'
                }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter mobile number" /* autoComplete="true" *//>
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.adminDetails.entityAdminDetailsDepartment : ""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter department" /* autoComplete="true" *//>
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
  );
};

export default AdminDetails;

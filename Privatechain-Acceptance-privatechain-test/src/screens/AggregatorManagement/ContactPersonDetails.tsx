import React from "react";
import { Input, Form, Checkbox, Button, Select } from "antd";
import { mobileRegex, phoneRegex } from "src/constants/validationSchemas";
import { countries } from "src/api/constants";

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];

const ContactPersonDetails = (props: any) => {
  const [form] = Form.useForm();

  const onFinish = (values: any) => {
    const data = {
      aggregatorPreferenceId: {
        value: props.aggregatorId,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityContactDetailsFirstName: {
        value: values.primaryContactFirstName,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityContactDetailsMiddleName: {
        value: values.primaryContactMiddleName,
        isMandatoryField: values.primaryContactMiddleNameConfig.includes(1),
        isShow: values.primaryContactMiddleNameConfig.includes(2),
        isEdit: values.primaryContactMiddleNameConfig.includes(3),
      },
      entityContactDetailsLastName: {
        value: values.primaryContactLastName,
        isMandatoryField: values.primaryContactLastNameConfig.includes(1),
        isShow: values.primaryContactLastNameConfig.includes(2),
        isEdit: values.primaryContactLastNameConfig.includes(3),
      },
      entityContactDetailsEmailId: {
        value: values.primaryContactEmailId,
        isMandatoryField: values.primaryContactEmailIdConfig.includes(1),
        isShow: values.primaryContactEmailIdConfig.includes(2),
        isEdit: values.primaryContactEmailIdConfig.includes(3),
      },
      entityContactDetailsMobileNumber: {
        value: values.primaryContactPhoneNumber,
        isMandatoryField: values.primaryContactPhoneNumberConfig.includes(1),
        isShow: values.primaryContactPhoneNumberConfig.includes(2),
        isEdit: values.primaryContactPhoneNumberConfig.includes(3),
      },
      entityContactDetailsDesignation: {
        value: values.primaryContactDesignation,
        isMandatoryField: values.primaryContactDesignationConfig.includes(1),
        isShow: values.primaryContactDesignationConfig.includes(2),
        isEdit: values.primaryContactDesignationConfig.includes(3),
      },
      entityContactDetailsDepartment: {
        value: values.primaryContactDepartment,
        isMandatoryField: values.primaryContactDepartmentConfig.includes(1),
        isShow: values.primaryContactDepartmentConfig.includes(2),
        isEdit: values.primaryContactDepartmentConfig.includes(3),
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
              name="primaryContactFirstName"
              label="First Name"
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsFirstName : ""}
              rules={[{ required: true, message: "Please enter First Name" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter first name" /* autoComplete="true" */  />
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsMiddleName : ""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter middle name" /* autoComplete="true" */ />
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsLastName : ""}
              rules={[{ required: true, message: "Please enter Last Name" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter last name" autoComplete="true" />
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsEmailId : ""}
              rules={[{ required: true, message: "Please enter Email ID" },{pattern:new RegExp(/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w\w+)+$/),
              message:'Please enter valid Email ID'
            }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter email ID" /* autoComplete="true" */  />
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
      {/*    <Form.Item
              name="countryCode"
              label="Country Code"
              
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.address.entityAddressCountry : ""}
              rules={[{ required: true, message: "" }]}
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder=" Country Code"
              >
                <Select.Option>Select</Select.Option>
                {countries.map((country: any, key: number) => (
                  <Select.Option value={country.phone} key={key}>
                    {country.phone}
                  </Select.Option>
                ))}
              </Select>
                </Form.Item>*/}
            <Form.Item
              name={"primaryContactPhoneNumber"}
              label="Mobile Number"
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsMobileNumber : ""}
              rules={[{ required: true, message: 'Please enter  Mobile Number'
            },
                {
                  pattern: new RegExp(mobileRegex),
                  message: 'Please enter valid Mobile Number'
                }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter mobile number" /* autoComplete="true"   *//>
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsDesignation : ""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter designation" /* autoComplete="true" */ />
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.contactDetails.entityContactDetailsDepartment : ""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter department" /* autoComplete="true" */ />
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
  );
};

export default ContactPersonDetails;

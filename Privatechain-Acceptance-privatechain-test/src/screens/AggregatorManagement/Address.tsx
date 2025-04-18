import React from "react";
import { Input, Form, Checkbox, Button, Select } from "antd";

import { countries } from "../../api/constants";

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];

const Address = (props: any) => {
  const [form] = Form.useForm();

  const onFinishFailed = (errorInfo: any) => {
    props.handleCallBack(false);
  };

  const onFinish = (values: any) => {
    props.handleCallBack(true);

    const data = {
      aggregatorPreferenceId: {
        value: props.aggregatorId,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityAddressAddressLine1: {
        value: values.addressLine1,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityAddressAddressLine2: {
        value: values.addressLine2,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityAddressAddressLine3: {
        value: values.addressLine3,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityAddressCity: {
        value: values.city,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityAddressState: {
        value: values.province,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityAddressCountry: {
        value: values.country,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityAddressPostalCode: {
        value: values.postalCode,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
    };
    props.saveInstitutionData(data);
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
              name="addressLine1"
              label="Address Line 1"
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.address.entityAddressAddressLine1 : ""}
              rules={[{ required: true, message: "Please enter Address Line 1" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter Address Line 1" /* autoComplete="true" */ />
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.address.entityAddressAddressLine2 : ""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter Address Line 2" /* autoComplete="true"  *//>
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.address.entityAddressAddressLine3 : ""}
              // rules={[{ required: true, message: "" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter Address Line 3" /* autoComplete="true" */ />
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.address.entityAddressCity : ""}
              rules={[{ required: true, message: "Please enter City" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter City" /* autoComplete="true" */ />
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
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.address.entityAddressState : ""}
              rules={[{ required: true, message: "Please enter Province/State" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter State" /* autoComplete="true" */ />
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
            <Form.Item
              name="country"
              label="Country"
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.address.entityAddressCountry : ""}
              rules={[{ required: true, message: "Please enter Country" }]}
            >
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
            <Form.Item
              name="postalCode"
              label="Postal Code"
              initialValue={props?.allDetails!= undefined ? 
                props?.allDetails.address.entityAddressPostalCode : ""}
              rules={[{ required: true, message: "Please enter Postal Code" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Postal Code" /* autoComplete="true" */ />
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
  );
};

export default Address;

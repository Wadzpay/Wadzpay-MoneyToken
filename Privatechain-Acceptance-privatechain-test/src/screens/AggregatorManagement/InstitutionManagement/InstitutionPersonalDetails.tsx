import React, { useEffect, useState } from "react";
import { capitalize } from "lodash";
import TimezoneSelect, { allTimezones } from "react-timezone-select";
import type { ITimezone } from "react-timezone-select";
import {
  Input,
  Form,
  Checkbox,
  Upload,
  Select,
  DatePicker,
  Button,
  UploadFile,
} from "antd";
import moment from "moment";
import { bankAccountRegEx } from "src/constants/validationSchemas";
import DOMPurify from "dompurify";

const { TextArea } = Input;

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];

const { Option } = Select;

const InstitutionPersonalDetails = (props: any) => {
  const [randomInstitutionID, setRandomInstitutionID] = useState<string>(
    "IN" + Date.now()
  );
  const [institutionName, setInstitutionName] = useState<string>('');
  const [institutionNameError, setInstitutionNameError] = useState('')
  const [tz, setTz] = useState<ITimezone>({
    value: "Asia/Kuala_Lumpur",
    label: "(GMT+8:00) Kuala Lumpur, Singapore (Malaysia Time)",
    offset: 8,
    abbrev: "MYT",
    altName: "Malaysia Time",
  });

  const assets = [
    { label: "BTC", value: "BTC" },
    { label: "ETH", value: "ETH" },
    { label: "USDT", value: "USDT" },
    { label: "WTK", value: "WTK" },
    { label: "USDC", value: "USDC" },
    { label: "SAR*", value: "SART" },
  ];

  const onChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    console.log("Change:", e.target.value);
  };

  const disabledDate = (current: any) => {
    return current && current.isBefore(moment().subtract(1, "days"));
  };

  const handleChange = (e: any, name: string) => {
    if (name === "institutionName") {
      const sanitizedInput = DOMPurify.sanitize(e.target.value)
      setInstitutionName(sanitizedInput);
    }    
  };

   //const [fileList, setFileList] = useState<UploadFile[]>([])

   const onLogoChange = ({ fileList: newFileList }: any) => {
    console.log(newFileList);
    //setFileList(newFileList)
    props.setFileList(newFileList);
  };

  const onFinish = (values: any) => {
    if(institutionNameError==''){
    props.saveInstitutionData(values);

    props.handleCallBack(true);}
  };

  const onFinishFailed = (errorInfo: any) => {
    props.handleCallBack(false);
  };
  useEffect(() => {
    let inst:any=props.list?.institutionList?.find((e:any)=>e.insitutionName?.toLowerCase().trim()==institutionName?.toLowerCase().trim())
if(inst&&props?.institutionDetails?.insitutionName?.toLowerCase()!=institutionName?.toLowerCase()){
  setInstitutionNameError('Institution Name already exists')
    }
    else{
      setInstitutionNameError('')
    }
}, [institutionName,institutionNameError])
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
              name="aggregatorPreferenceId"
              label="Aggregator ID"
              rules={[{ required: true, message: "" }]}
              initialValue={props.aggregatorID}
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter ID"
/*                 /*autoComplete="true"*/ 
                onChange={(e) => handleChange(e, "aggregatorPreferenceId")}
                defaultValue={"Aggregator"}
                value={"Aggregator"}
                readOnly={true}
                disabled={true}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="aggregatorIdConfig"
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
              name="aggregatorName"
              label="Aggregator Name"
              rules={[{ required: true, message: "" }]}
              initialValue={props.refId}
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Aggregator ID"
/*                 /*autoComplete="true"*/ 
                onChange={(e) => handleChange(e, "aggregatorName")}
                defaultValue={"Aggregator"}
                value={"Aggregator"}
                readOnly={true}
                disabled={true}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="aggregatorNameConfig"
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
              name="institutionId"
              label="Institution Id"
              rules={[{ required: true, message: "" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails.institutionId
                  : randomInstitutionID
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" /* autoComplete="true" */ disabled={true} placeholder="Enter Institution ID" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institutionIdConfig"
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
              name="clientInstitutionId"
              label="Client Institution ID"
              rules={[{ required: true, message: "Please enter Client Institution ID" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails.insitutionPreferenceId
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" /*autoComplete="true"*/  placeholder="Enter Client Institution ID" />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="clientInstitutionIdConfig"
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
              name="institutionName"
              label="Institution Name"
              rules={[{ required: true, message: "Please enter Institution Name" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails.insitutionName
                  : ""
              }
              validateStatus={institutionNameError==''?'success':'error'}
              help={institutionNameError!=''?institutionNameError:null}

            >
              <Input aria-autocomplete='both' aria-haspopup="false" /*autoComplete="true"*/ placeholder="Enter Institution Name" onChange={(e) => handleChange(e, "institutionName")}
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
              rules={[{ required: true, message: "Please enter Institution Abbrevation" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.info?.entityInfoAbbrevation
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" /*autoComplete="true"*/ placeholder="Enter Institution Abbreviation" />
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
              name="institutionDescription"
              label="Institution Description"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.info?.entityInfoDescription
                  : ""
              }
            >
              <TextArea
                showCount
                maxLength={350}
                onChange={onChange}
                placeholder="Institution Description"
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
                maxCount={1}
                multiple={false}
                fileList={props.fileList}
                onChange={onLogoChange}

                // beforeUpload={beforeUpload}
                // onChange={handleChange}
              >
                {props.fileList?.length == 0 ? (
                  <>
                    <img src={"/images/attach-file.svg"} alt="avatar" />
                    <span className="chooseLogo">Choose Logo</span>
                  </>
                ) : null}
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
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.info?.entityInfoRegion
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Select region" /*autoComplete="true"*/  />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="institionRegionConfig"
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
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.info?.entityInfoTimezone
                  : ""
              }
              name="institutionAggregatorTimeZone"
              rules={[{ required: true, message: "Choose time zone" }]}
            >
              <TimezoneSelect
                placeholder="Select time zone"
                value={tz}
                timezones={{
                  ...allTimezones,
                  "America/Lima": "Pittsburgh",
                  "Europe/Berlin": "Frankfurt",
                }}
                components={{
                  IndicatorSeparator: () => null,
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
              name="institutionType"
              label="Institution Type"
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.info?.entityInfoType
                  : ""
              }
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Select type of the Institution"
                // showSearch
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
              name="institutionTypeConfig"
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
              label="Default Digital currency"
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.info
                      ?.entityInfoDefaultDigitalCurrency
                  : ""
              }
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Select Default Digital Currency"
              >
                {assets.map((asset) => (
                  <Option key={asset.value} value={asset.value}>
                    {asset.label}
                  </Option>
                ))}
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
              name="baseCurrency"
              label="Base Fiat Currency"
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.info?.entityInfoBaseFiatCurrency
                  : ""
              }
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Choose Fiat Currency"
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
              name="baseCurrencyConfig"
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
              name={"bankName"}
              label="Bank Name"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.bankDetails
                      ?.entityBankDetailsBankName
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Name" /*autoComplete="true"*/  />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="bankNameConfig"
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
              name={"bankAccountNumber"}
              label="Bank Account Number"
              rules={[{ pattern: new RegExp(bankAccountRegEx),
                message: 'Please enter valid account number'}]}
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.bankDetails
                      ?.entityBankDetailsBankAccountNumber
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Account Number" /*autoComplete="true"*/ />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="bankAccountNumberConfig"
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
              name={"bankHolderName"}
              label="Account Holder Name"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.bankDetails
                      ?.entityBankDetailsBankHolderName
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Account Holder Name" /*autoComplete="true"*/ />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="bankHolderNameConfig"
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
              name={"bankCode"}
              label="Bank Code"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.bankDetails
                      ?.entityBankDetailsBranchCode
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Code" /*autoComplete="true"*/ />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="bankCodeConfig"
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
              name={"bankLocation"}
              label="Bank Location"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.institutionDetails != undefined
                  ? props?.institutionDetails?.bankDetails
                      ?.entityBankDetailsBranchLocation
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Location" /*autoComplete="true"*/ />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="bankLocationConfig"
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
          <Button htmlType="submit" ref={props?.formRef}>
            Save
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default InstitutionPersonalDetails;

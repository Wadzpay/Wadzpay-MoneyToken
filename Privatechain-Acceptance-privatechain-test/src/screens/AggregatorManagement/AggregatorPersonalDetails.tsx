import React, { useEffect, useState } from "react";
import { capitalize } from "lodash";
import TimezoneSelect, { allTimezones } from "react-timezone-select";
import type { ITimezone } from "react-timezone-select";
import ReactS3Client from "react-aws-s3-typescript";
import {
  Input,
  Form,
  Checkbox,
  Upload,
  Select,
  DatePicker,
  Button,
  Space,
  UploadFile,
} from "antd";
import moment from "moment";
import { bankAccountRegEx } from "src/constants/validationSchemas";
import DOMPurify from "dompurify";
import { s3Config } from "src/api/constants";

const { TextArea } = Input;

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];

const { Option } = Select;

const AggregatorPersonalDetails = (props: any) => {
  const [randomAgregatorID, setRandomAgregatorID] = useState<string>(
    "AG" + Date.now()
  );
  const [aggregatorNameError, setAggregatorNameError] = useState('')
  const [aggregatorName, setAggregatorName] = useState('')
  const [tz, setTz] = useState<ITimezone>({
    value: "Asia/Kuala_Lumpur",
    label: "(GMT+8:00) Kuala Lumpur, Singapore (Malaysia Time)",
    offset: 8,
    abbrev: "MYT",
    altName: "Malaysia Time",
  });

  const onChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    console.log("Change:", e.target.value);
  };

  const disabledDate = (current: any) => {
    return current && current.isBefore(moment().subtract(1, "days"));
  };

  const handleChange = (e: any, name: string) => {
    if (name === "aggregatorName") {
      const sanitizedInput = DOMPurify.sanitize(e.target.value)
      setAggregatorName(sanitizedInput);
    }
  };
  const config = {...s3Config,dirName: "test"}

  const onFinishFailed = (errorInfo: any) => {
    props.handleCallBack(false);
  };

  const onFinish = (values: any) => {
    const data = {
      aggregatorPreferenceId: {
        value: values.aggregatorPreferenceId,
        isMandatoryField: values.aggregatorIdConfig.includes(1),
        isShow: values.aggregatorIdConfig.includes(2),
        isEdit: values.aggregatorIdConfig.includes(3),
      },
      clientAggregatorPreferenceId: {
        value: values.clientAggregatorPreferenceId,
        isMandatoryField: values.clientAggregatorPreferenceIdConfig.includes(1),
        isShow: values.clientAggregatorPreferenceIdConfig.includes(2),
        isEdit: values.clientAggregatorPreferenceIdConfig.includes(3),
      },
      aggregatorName: {
        value: values.aggregatorName,
        isMandatoryField: values.aggregatorNameConfig.includes(1),
        isShow: values.aggregatorNameConfig.includes(2),
        isEdit: values.aggregatorNameConfig.includes(3),
      },
      entityInfoAbbrevation: {
        value: values.aggregatorAbbreviation,
        isMandatoryField: values.aggregatorAbbreviationConfig.includes(1),
        isShow: values.aggregatorAbbreviationConfig.includes(2),
        isEdit: values.aggregatorAbbreviationConfig.includes(3),
      },
      entityInfoDescription: {
        value: values.aggregatorDescription,
        isMandatoryField: values.aggregatorDescriptionConfig.includes(1),
        isShow: values.aggregatorDescriptionConfig.includes(2),
        isEdit: values.aggregatorDescriptionConfig.includes(3),
      },
      entityInfoRegion: {
        value: values.aggregatorRegion,
        isMandatoryField: values.aggregatorRegionConfig.includes(1),
        isShow: values.aggregatorRegionConfig.includes(2),
        isEdit: values.aggregatorRegionConfig.includes(3),
      },
      entityInfoTimezone: {
        value: "",
        isMandatoryField: values.aggregatorTimeZoneConfig.includes(1),
        isShow: values.aggregatorTimeZoneConfig.includes(2),
        isEdit: values.aggregatorTimeZoneConfig.includes(3),
      },
      aggregatorType: {
        value: values.aggregatorType,
        isMandatoryField: values.aggregatorTypeConfig.includes(1),
        isShow: values.aggregatorTypeConfig.includes(2),
        isEdit: values.aggregatorTypeConfig.includes(3),
      },
      entityInfoDefaultDigitalCurrency: {
        value: values.defaultCurrency,
        isMandatoryField: values.defaultCurrencyConfig.includes(1),
        isShow: values.defaultCurrencyConfig.includes(2),
        isEdit: values.defaultCurrencyConfig.includes(3),
      },
      entityInfoBaseFiatCurrency: {
        value: values.baseCurrency,
        isMandatoryField: values.baseCurrencyConfig.includes(1),
        isShow: values.baseCurrencyConfig.includes(2),
        isEdit: values.baseCurrencyConfig.includes(3),
      },
      entityBankDetailsBankName: {
        value: values.bankName,
        isMandatoryField: values.bankNameConfig.includes(1),
        isShow: values.bankNameConfig.includes(2),
        isEdit: values.bankNameConfig.includes(3),
      },
      entityBankDetailsBankAccountNumber: {
        value: values.bankAccountNumber,
        isMandatoryField: values.bankAccountNumberConfig.includes(1),
        isShow: values.bankAccountNumberConfig.includes(2),
        isEdit: values.bankAccountNumberConfig.includes(3),
      },
      entityBankDetailsBankHolderName: {
        value: values.bankHolderName,
        isMandatoryField: values.bankHolderNameConfig.includes(1),
        isShow: values.bankHolderNameConfig.includes(2),
        isEdit: values.bankHolderNameConfig.includes(3),
      },
      entityBankDetailsBranchCode: {
        value: values.bankCode,
        isMandatoryField: values.bankCodeConfig.includes(1),
        isShow: values.bankCodeConfig.includes(2),
        isEdit: values.bankCodeConfig.includes(3),
      },
      entityBankDetailsBranchLocation: {
        value: values.bankLocation,
        isMandatoryField: values.bankLocationConfig.includes(1),
        isShow: values.bankLocationConfig.includes(2),
        isEdit: values.bankLocationConfig.includes(3),
      },
      aggregatorLogo: {
        value: values.aggregatorLogo,
      },
    };
    if(aggregatorNameError==''){
    props.saveInstitutionData(data);
    
    props.handleCallBack(true);}
  };

  //const [fileList, setFileList] = useState<UploadFile[]>([])

  const onLogoChange = ({ fileList: newFileList }: any) => {
    console.log(newFileList);
    //setFileList(newFileList)
    props.setFileList(newFileList);
  };

  const assets = [
    { label: "BTC", value: "BTC" },
    { label: "ETH", value: "ETH" },
    { label: "USDT", value: "USDT" },
    { label: "WTK", value: "WTK" },
    { label: "USDC", value: "USDC" },
    { label: "SAR*", value: "SART" },
  ];
  useEffect(() => {
    let ag:any=props.list?.aggregatorList?.find((e:any)=>e.aggregatorName?.toLowerCase().trim()==aggregatorName?.toLowerCase().trim())
if(ag&&props?.aggregatorDetails?.aggregatorName?.toLowerCase()!=aggregatorName?.toLowerCase()){
  setAggregatorNameError('Aggregator Name already exists')
}  
  else{
    if(aggregatorName==''){
      setAggregatorNameError('Aggregator Name should not be empty')

    }
      setAggregatorNameError('')
    }
  
}, [aggregatorName,aggregatorNameError])
  return (
    <div>
      <Form   autoComplete="off"

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
              initialValue={
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails?.aggregatorPreferenceId
                  : randomAgregatorID
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Aggregator ID"
                onChange={(e) => handleChange(e, "aggregatorPreferenceId")}
                disabled={true}
/*                 autoComplete="true"
 */              />
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
              name="clientAggregatorPreferenceId"
              label="Client Aggregator ID"
              rules={[{ required: true, message: "Please enter Client Aggregator ID" }]}
              initialValue={
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails?.clientAggregatorPreferenceId
                  : ""
              }
            >
              <Input
                placeholder="Enter Client Aggregator ID"
                onChange={(e) => handleChange(e, "clientAggregatorPreferenceId")}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="clientAggregatorPreferenceIdConfig"
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
              rules={[{ required: true ,message: 'Please enter Aggregator Name'
            },                {
                pattern: new RegExp(/^\s*\S+.*/
                ),
                message: 'Name should be Non Empty'
              }]}
          
              initialValue={
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.aggregatorName
                  : ""
              }
              validateStatus={aggregatorNameError==''?'success':'error'}
              help={aggregatorNameError!=''?aggregatorNameError:null}
              
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter Aggregator Name" /* autoComplete="true" */ onChange={(e) => handleChange(e, "aggregatorName")} />
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
              name="aggregatorAbbreviation"
              label="Aggregator Abbreviation"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.info.entityInfoAbbrevation
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter Aggregator Abbreviation" /* autoComplete="true" */ />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="aggregatorAbbreviationConfig"
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
              name={"aggregatorDescription"}
              label="Aggregator Description"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.info.entityInfoDescription
                  : ""
              }
            >
              <TextArea
                showCount
                maxLength={350}
                onChange={onChange}
/*                 autoComplete="true"
 */                placeholder="Aggregator Description"
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="aggregatorDescriptionConfig"
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
            <Form.Item label="Aggregator Logo" name={"aggregatorLogo"}>
              {/* <Input placeholder="Enter institution ID" /> */}
              <Upload
                name="aggregatorLogo"
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
              name="aggregatorLogoConfig"
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
              name="aggregatorRegion"
              label="Aggregator Region"
              initialValue={
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.info.entityInfoRegion
                  : ""
              }
              // rules={[{ required: true, message: "" }]}
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Select region" /* autoComplete="true" */ />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="aggregatorRegionConfig"
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
              label="Aggregator Time Zone"
              initialValue={
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.info.entityInfoTimezone
                  : ""
              }
              name="aggregatorTimeZone"
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
              name="aggregatorTimeZoneConfig"
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
              name="aggregatorType"
              label="Aggregator Type"
              initialValue={
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.info.entityInfoType
                  : ""
              }
              rules={[{ required: true, message: "" }]}
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Select type of the aggregator"
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
              name="aggregatorTypeConfig"
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
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.info
                      .entityInfoDefaultDigitalCurrency
                  : ""
              }
              rules={[{ required: true, message: "" }]}
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
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.info.entityInfoBaseFiatCurrency
                  : ""
              }
              // rules={[{ required: true, message: "" }]}
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
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.bankDetails
                      .entityBankDetailsBankName
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Name" /* autoComplete="true" */ />
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
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.bankDetails
                      .entityBankDetailsBankAccountNumber
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Account Number"/*  autoComplete="true" */ />
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
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.bankDetails
                      .entityBankDetailsBankHolderName
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Account Holder Name" /* autoComplete="true" */ />
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
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.bankDetails
                      .entityBankDetailsBranchCode
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Code" /* autoComplete="true" *//>
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
                props?.aggregatorDetails != undefined
                  ? props?.aggregatorDetails.bankDetails
                      .entityBankDetailsBranchLocation
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Location" /* autoComplete="true" *//>
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

export default AggregatorPersonalDetails;

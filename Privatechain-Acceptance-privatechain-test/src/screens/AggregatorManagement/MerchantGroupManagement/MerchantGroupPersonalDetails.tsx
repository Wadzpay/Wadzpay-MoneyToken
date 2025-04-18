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

const MerchantGroupPersonalDetails = (props: any) => {
  const [randomMerchantGroupID, setRandomMerchantGroupID] = useState<string>(
    "MG" + Date.now()
  );
  const [merchantGroupName, setMerchantGroupName] = useState('')
  const [merchantGroupNameError, setMerchantGroupNameError] = useState('')
  const [institutionName, setInstitutionName] = useState<string>("");

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
    return current && current.isBefore(moment()?.subtract(1, "days"));
  };

  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const onLogoChange = ({ fileList: newFileList }: any) => {
    console.log(newFileList);
    //setFileList(newFileList);
    props.setFileList(newFileList);
  };

  const handleChange = (e: any, name: string) => {
    if (name === "institutionName") {
      const sanitizedInput = DOMPurify.sanitize(e.target.value)
      setInstitutionName(sanitizedInput);
    }
    if (name === "merchantGroupName") {
      const sanitizedInput = DOMPurify.sanitize(e.target.value)
      setMerchantGroupName(sanitizedInput);
    }
  };

  const onFinish = (values: any) => {
    if(merchantGroupNameError==''){
    props.saveInstitutionData(values);
    props.handleCallBack(true);
    }
  };

  const onFinishFailed = (errorInfo: any) => {
    props.handleCallBack(false);
  };
  useEffect(() => {
  let mg:any=props.list?.merchantGroupList?.find((e:any)=>e?.merchantGroup?.merchantGroupName?.toLowerCase().trim()==merchantGroupName?.toLowerCase().trim())
  console.log(props.list)
if(mg&&props?.merchantDetails?.merchantGroupName?.toLowerCase()!=merchantGroupName?.toLowerCase()){
  setMerchantGroupNameError('Merchant Group Name  already exists')
    }
    else{
      setMerchantGroupNameError('')

    }
}, [merchantGroupName,merchantGroupNameError])

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
                onChange={(e) => handleChange(e, "aggregatorPreferenceId")}
                defaultValue={"Aggregator"}
                value={"Aggregator"}
                readOnly={true}
                disabled={true}
                /*/*autoComplete="true"*/
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
              initialValue={props.aggregatorName}
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter ID"
                onChange={(e) => handleChange(e, "aggregatorName")}
                defaultValue={"Aggregator"}
                value={"Aggregator"}
                readOnly={true}
                disabled={true}
                /*autoComplete="true"*/
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
              initialValue={props?.instituteID}
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Institution ID"
                onChange={(e) => handleChange(e, "institutionId")}
                defaultValue={"institutionId"}
                value={"institutionId"}
                readOnly={true}
                disabled={true}
                /*autoComplete="true"*/
              />
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
              name="institutionName"
              label="Institution Name"
              rules={[{ required: true, message: "" }]}
              initialValue={props?.insitutionName}
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Institution Name"
                onChange={(e) => handleChange(e, "institutionName")}
                defaultValue={"institutionName"}
                value={"institutionName"}
                readOnly={true}
                disabled={true}
                /*autoComplete="true"*/
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
              name="merchantGroupID"
              label="Merchant Group ID"
              rules={[{ required: true, message: "" }]}
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.merchantGroupPreferenceId
                  : randomMerchantGroupID
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter Merchant Group ID" disabled={true} /*autoComplete="true"*//>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="merchantGroupIdConfig"
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
              name="clientMerchantGroupId"
              label="Client Merchant Group ID"
               rules={[{ required: true, message: "Please enter Client Merchant Group ID" }]}
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.clientMerchantGroupId
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Enter Client Merchant Group ID" /*autoComplete="true"*//>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="clientMerchantGroupConfig"
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
              name="merchantGroupName"
              label="Merchant Group Name"
              rules={[{ required: true, message: "Please enter Merchant Group Name" }]}
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.merchantGroupName
                  : ""
              }              
              validateStatus={merchantGroupNameError==''?'success':'error'}
              help={merchantGroupNameError!=''?merchantGroupNameError:null}

            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Merchant Group Name"
                /*autoComplete="true"*/
                onChange={(e) => handleChange(e, "merchantGroupName")}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="merchantGroupNameConfig"
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
              name="merchantGroupAbbreviation"
              label="Merchant Group Abbreviation"
              rules={[{ required: true, message: "Please enter Merchant Group Abbreviation" }]}
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoAbbrevation
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Merchant Group Abbreviation"
                onChange={(e) => handleChange(e, "merchantGroupAbbreviation")}
                defaultValue={""}
                /*autoComplete="true"*/
                value={""}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="merchantGroupAbbreviationConfig"
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
              name={"merchantGroupDescription"}
              label="Merchant Group Description"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoDescription
                  : ""
              }
            >
              <TextArea
                showCount
                maxLength={350}
                onChange={onChange}
                /*autoComplete="true"*/
                placeholder="Merchant Group Description"
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="merchantGroupDescription"
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
            <Form.Item label="Merchant Group Logo" name={"merchantGroupLogo"}>
              {/* <Input placeholder="Enter institution ID" /> */}
              <Upload
                name="avatar"
                listType="picture-card"
                className="avatar-uploader"
                maxCount={1}
                multiple={false}
                fileList={props?.fileList}
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
              name="merchantGroupLogoConfig"
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
              name="merchantGroupRegion"
              label="Merchant Group Region"
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoRegion
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Select region" /*autoComplete="true"*/ />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="merchantGroupRegionConfig"
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
              label="Merchant Group Time Zone"
              name="merchantGroupTimeZone"
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoTimezone
                  : ""
              }
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
              name="merchantGroupTimeZoneConfig"
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
              name="merchantGroupType"
              label="Merchant Group Type"
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoType
                  : ""
              }
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Select type of the Merchant Group"
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
              name="merchantGroupTypeConfig"
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
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info
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
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoBaseFiatCurrency
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
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.bankDetails
                      ?.entityBankDetailsBankName
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Name" /*autoComplete="true"*/ />
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
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.bankDetails
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
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.bankDetails
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
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.bankDetails
                      ?.entityBankDetailsBranchCode
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Code" /*autoComplete="true"*//>
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
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.bankDetails
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

export default MerchantGroupPersonalDetails;

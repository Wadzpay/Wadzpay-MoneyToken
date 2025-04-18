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

const { TextArea } = Input;

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];

const { Option } = Select;

const SubMerchantPersonalDetails = (props: any) => {
  const [randomMerchantID, setRandomMerchantID] = useState<string>(
    "SM" + Date?.now()
  );
  const [institutionName, setInstitutionName] = useState<string>("");
const [subMerchantAcquirerNameError, setSubMerchantAcquirerNameError] = useState('')
const [subMerchantAcquirerName, setSubMerchantAcquirerName] = useState('')
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
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const onLogoChange = ({ fileList: newFileList }: any) => {
    console.log(newFileList);
    //setFileList(newFileList);
    props?.setFileList(newFileList);
  };

  const handleChange = (e: any, name: string) => {
    if (name === "institutionName") {
      setInstitutionName(e.target.value);
    }
    if (name === "subMerchantAcquirerName") {
      setSubMerchantAcquirerName(e.target.value);
    }
  };

  const onFinish = (values: any) => {
    if(subMerchantAcquirerNameError==''){
    props?.saveInstitutionData(values);
    props?.handleCallBack(true);
    }
  };

  const onFinishFailed = (errorInfo: any) => {
    props?.handleCallBack(false);
  };
   useEffect(() => {
    let sml=props?.list?.merchantList?.find((e:any)=>e?.subMerchantAcquirer?.subMerchantAcquirerName?.trim()?.toLowerCase()==subMerchantAcquirerName?.trim()?.toLowerCase())
    if(sml&&props?.merchantDetails?.subMerchantAcquirerName?.toLowerCase()!=subMerchantAcquirerName?.toLowerCase()){
      setSubMerchantAcquirerNameError('Sub merchant acquirer Name  already exists')
    }
    else{
      setSubMerchantAcquirerNameError('')
    }
    
  }, [subMerchantAcquirerName,subMerchantAcquirerNameError])
  return (
    <div>
      <Form
        autoComplete="off"
        name="basic"
        form={props?.form}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
       >
        <div className="form-div">
          <div className="col-lg-7">
            <Form.Item
              label="Aggregator ID & Name"
              rules={[{ required: true, message: "" }]}
            >
              <Input.Group compact>
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter ID"
                  onChange={(e) => handleChange(e, "aggregatorPreferenceId")}
                  defaultValue={props?.aggregatorID}
                  value={props?.aggregatorID}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                />
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter ID"
                  onChange={(e) => handleChange(e, "aggregatorName")}
                  defaultValue={props?.aggregatorName}
                  value={props?.aggregatorName}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                />
              </Input.Group>
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
              label="Institution Id & Name"
              rules={[{ required: true, message: "" }]}
            >
              <Input.Group compact>
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter Institution ID"
                  onChange={(e) => handleChange(e, "institutionID")}
                  defaultValue={props?.instituteID}
                  value={props?.instituteID}
                  readOnly={true}
                  disabled={true}
/*                   autoComplete="true" 
 */                  style={{ width: "50%" }}
                />
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter Institution Name"
                  onChange={(e) => handleChange(e, "institutionName")}
                  defaultValue={props?.insitutionName}
                  value={props?.insitutionName}
                  readOnly={true}
                  disabled={true}
/*                   autoComplete="true" 
 */                  style={{ width: "50%" }}
                />
              </Input.Group>
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
              label="Merchant Group Id & Name"
              rules={[{ required: true, message: "" }]}
            >
              <Input.Group compact>
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter Merchant Group ID"
                  onChange={(e) => handleChange(e, "merchantGroupPreferenceId")}
                  defaultValue={props?.merchantGroupPreferenceId}
                  value={props?.merchantGroupPreferenceId}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props?.merchantGroupPreferenceId}
                />
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter Merchant Group Name"
                  onChange={(e) => handleChange(e, "merchantGroupName")}
                  defaultValue={props?.merchantGroupName}
                  value={props?.merchantGroupName}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props?.merchantGroupName}
                />
              </Input.Group>
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
              label="Merchant Id & Name"
              rules={[{ required: true, message: "" }]}
            >
              <Input.Group compact>
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter Merchant ID"
                  onChange={(e) => handleChange(e, "merchantAcquirerId")}
                  defaultValue={props?.merchantAcquirerId}
                  value={props?.merchantAcquirerId}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props?.merchantAcquirerId}
                />
                <Input
                  placeholder="Enter Merchant Name"
                  onChange={(e) => handleChange(e, "merchantAcquirerName")}
                  defaultValue={props?.merchantAcquirerName}
                  value={props?.merchantAcquirerName}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props?.merchantAcquirerName}
                />
              </Input.Group>
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
              name="subMerchantAcquirerId"
              label="Sub Merchant ID"
              rules={[{ required: true, message: "" }]}
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.subMerchantAcquirerId
                  : randomMerchantID
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Sub Merchant ID"
                onChange={(e) => handleChange(e, "subMerchantAcquirerId")}
                disabled={true}
/*                 autoComplete="true" 
 */              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="subMerchantAcquirerIdConfig"
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
              name="clientSubMerchantAcquirerId"
              label="Client Sub Merchant ID"
              rules={[{ required: true, message: "Please enter Client Sub Merchant ID" }]}
               initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.clientSubMerchantAcquirerId:""
                  
              }
            >
              <Input
                placeholder="Enter Client Sub Merchant ID"
                onChange={(e) => handleChange(e, "clientSubMerchantAcquirerId")}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="subMerchantAcquirerIdConfig"
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
              name="subMerchantAcquirerName"
              label="Sub Merchant Name"
              validateStatus={subMerchantAcquirerNameError==''?'success':'error'}
              help={subMerchantAcquirerNameError!=''?subMerchantAcquirerNameError:null}
              rules={[{ required: true, message: "Please enter Sub Merchant Name" }]}
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.subMerchantAcquirerName
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
               placeholder="Enter Sub Merchant Name"
                onChange={(e) => handleChange(e, "subMerchantAcquirerName")}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="subMerchantAcquirerNameConfig"
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
              name="subMerchantAbbreviation"
              label="Sub Merchant Abb."
              //   rules={[{ required: true, message: "" }]}
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoAbbrevation
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Sub Merchant Abbreviation"
                onChange={(e) => handleChange(e, "subMerchantAbbreviation")}
                defaultValue={""}
                value={""}
/*                 autoComplete="true" 
 */              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="subMerchantAbbreviationConfig"
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
              name={"subMerchantDescription"}
              label="Sub Merchant Description"
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
               placeholder="Sub Merchant Description"
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="subMerchantDescriptionConfig"
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
            <Form.Item label="Sub Merchant Logo" name={"subMerchantLogo"}>
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
                {props?.fileList?.length == 0 ? (
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
              name="subMerchantLogoConfig"
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
              name="subMerchantRegion"
              label="Sub Merchant Region"
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoRegion
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Select region" autoComplete="true"  />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="subMerchantRegionConfig"
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
              label="Sub Merchant Time Zone"
              name="subMerchantTimeZone"
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
              name="subMerchantTimeZoneConfig"
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
              name="subMerchantType"
              label="Sub Merchant Type"
              initialValue={
                props?.merchantDetails != undefined
                  ? props?.merchantDetails?.info?.entityInfoType
                  : ""
              }
            >
              <Select
                suffixIcon={<img src={"/images/down-arrow.svg"} />}
                placeholder="Select type of the Sub Merchant"
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
              name="subMerchantTypeConfig"
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
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Name" autoComplete="true" />
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
              label="Account Account Number"
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
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Account Account Number" autoComplete="true"  />
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
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Account Holder Name" autoComplete="true"  />
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
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Code" autoComplete="true" />
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
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Location" autoComplete="true" />
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

export default SubMerchantPersonalDetails;

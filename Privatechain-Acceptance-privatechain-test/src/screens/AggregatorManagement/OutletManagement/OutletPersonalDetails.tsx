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
import { error } from "console";
import { Value } from "sass";
import { bankAccountRegEx } from "src/constants/validationSchemas";

const { TextArea } = Input;

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];

const { Option } = Select;

const OutletPersonalDetails = (props: any) => {
    const [randomOutletID, setRandomOutletID] = useState<string>(
    "OT" + Date.now()
  );
  const [outletName, setOutletName] = useState<string>('')
const [outletError, setOutletError] = useState('')

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
    return current && current.isBefore(moment().subtract(1, "days"));
  };
 
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const onLogoChange = ({ fileList: newFileList }: any) => {
    console.log(newFileList);
    //setFileList(newFileList);
    props.setFileList(newFileList);
  };

  const handleChange = (e: any, name: string) => {
    if (name === "institutionName") {
      setInstitutionName(e.target.value);
    }
    if (name === "outletName") {
      setOutletName(e.target.value);
    }
  };
  useEffect(() => {
    let ol=props.outletList.outletList?.find((e:any)=>e?.outlet?.outletName?.trim().toLowerCase()==outletName?.trim().toLowerCase())
    console.log(props.outletList)
if(ol&&props?.outletDetails?.outletName?.toLowerCase()!=outletName?.toLowerCase()){
  setOutletError('Outlet Name  already exists')
    }
    else{
      setOutletError('')
    }
    
    
  }, [outletName,outletError])
  const onFinish = (values: any) => {
    if(outletError==''){
    props.saveInstitutionData(values);
    props.handleCallBack(true);
    }
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
              label="Aggregator ID & Name"
              rules={[{ required: true, message: "" }]}
            >
              <Input.Group compact>
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter ID"
                  /*autoComplete="true"*/
                  onChange={(e) => handleChange(e, "aggregatorPreferenceId")}
                  defaultValue={props.aggregatorID}
                  value={props.aggregatorID}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                />
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter ID"
                  /*autoComplete="true"*/
                  onChange={(e) => handleChange(e, "aggregatorName")}
                  defaultValue={props.aggregatorName}
                  value={props.aggregatorName}
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
                  /*autoComplete="true"*/ 
                  onChange={(e) => handleChange(e, "institutionID")}
                  defaultValue={props.instituteID}
                  value={props.instituteID}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                />
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter Institution Name"
                  /*autoComplete="true"*/ 
                  onChange={(e) => handleChange(e, "institutionName")}
                  defaultValue={props.insitutionName}
                  value={props.insitutionName}
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
                  /*autoComplete="true"*/ 
                  onChange={(e) => handleChange(e, "merchantGroupPreferenceId")}
                  defaultValue={props.merchantGroupPreferenceId}
                  value={props.merchantGroupPreferenceId}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props.merchantGroupPreferenceId}
                />
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter Merchant Group Name"
                  /*autoComplete="true"*/ 
                  onChange={(e) => handleChange(e, "merchantGroupName")}
                  defaultValue={props.merchantGroupName}
                  value={props.merchantGroupName}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props.merchantGroupName}
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
                  /*autoComplete="true"*/ 
                  onChange={(e) => handleChange(e, "merchantAcquirerId")}
                  defaultValue={props.merchantAcquirerId}
                  value={props.merchantAcquirerId}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props.merchantAcquirerId}
                />
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter Merchant Name"
                  /*autoComplete="true"*/ 
                  onChange={(e) => handleChange(e, "merchantAcquirerName")}
                  defaultValue={props.merchantAcquirerName}
                  value={props.merchantAcquirerName}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props.merchantAcquirerName}
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
              label="SubMerchant Id & Name"
              rules={[{ required: true, message: "" }]}
            >
              <Input.Group compact>
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter SubMerchant ID"
                  /*autoComplete="true"*/ 
                  onChange={(e) => handleChange(e, "subMerchantId")}
                  defaultValue={props.subMerchantId}
                  value={props.subMerchantId}
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props.subMerchantId}
                />
                <Input aria-autocomplete='both' aria-haspopup="false"
                  placeholder="Enter SubMerchant Name"
                  onChange={(e) => handleChange(e, "subMerchantName")}
                  defaultValue={props.subMerchantName}
                  value={props.subMerchantName}
                  /*autoComplete="true"*/ 
                  readOnly={true}
                  disabled={true}
                  style={{ width: "50%" }}
                  title={props.subMerchantName}
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
              name="outletId"
              label="Outlet ID"
              rules={[{ required: true, message: "" }]}
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.outletId
                  : randomOutletID
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Outlet ID"
                /*autoComplete="true"*/ 
                onChange={(e) => handleChange(e, "outletId")}
                disabled={true}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="outletIdConfig"
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
              name="clientOutletId"
              label="Client Outlet ID"
              rules={[{ required: true, message: "Please enter Client Outlet Id" }]}
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.clientOutletId
                  : ""
              }
            >
              <Input
                placeholder="Enter Client Outlet ID"
                onChange={(e) => handleChange(e, "clientOutletId")}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="clientoutletIdConfig"
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
              name="outletName"
              label="Outlet Name" 
              validateStatus={outletError==''?'success':'error'}
              help={outletError!=''?outletError:null} 
              rules={[{ required: true, message: "Please enter Outlet Name"} ,              
           ]}
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.outletName
                  : ""
              }              
            >             
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter outlet Name"
                /*autoComplete="true"*/ 
                onChange={(e) => handleChange(e, "outletName")}
              />           
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="outletNameConfig"
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
              name="outletAbbreviation"
              label="Outlet Abb."
              //   rules={[{ required: true, message: "" }]}
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.info?.entityInfoAbbrevation
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false"
                placeholder="Enter Outlet Abbreviation"
                /*autoComplete="true"*/ 
                onChange={(e) => handleChange(e, "outletAbbreviation")}
                defaultValue={""}
                value={""}
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="outletAbbreviationConfig"
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
              name={"outletDescription"}
              label="Outlet Description"
              // rules={[{ required: true, message: "" }]}
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.info?.entityInfoDescription
                  : ""
              }
            >
              <TextArea
                showCount
                maxLength={350}
                onChange={onChange}
                /*autoComplete="true"*/ 
                placeholder="Outlet Description"
              />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="outletDescriptionConfig"
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
            <Form.Item label="Outlet Logo" name={"outletLogo"}>
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
              name="outletLogoConfig"
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
              name="outletRegion"
              label="Outlet Region"
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.info?.entityInfoRegion
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Select region" /*autoComplete="true"*/  />
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="outletRegionConfig"
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
              label="Outlet Time Zone"
              name="outletTimeZone"
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.info?.entityInfoTimezone
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
              name="outletTimeZoneConfig"
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
              name="outletType"
              label="Outlet Type"
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.info?.entityInfoType
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
                props?.outletDetails != undefined
                  ? props?.outletDetails?.info
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
                props?.outletDetails != undefined
                  ? props?.outletDetails?.info?.entityInfoBaseFiatCurrency
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
                props?.outletDetails != undefined
                  ? props?.outletDetails?.bankDetails
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
              // rules={[{ required: true, message: "" }]}
              rules={[{ pattern: new RegExp(bankAccountRegEx),
                message: 'Please enter valid account number'}]}
  
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.bankDetails
                      ?.entityBankDetailsBankAccountNumber
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Account Number" /*autoComplete="true"*/  />
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
                props?.outletDetails != undefined
                  ? props?.outletDetails?.bankDetails
                      ?.entityBankDetailsBankHolderName
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Account Holder Name" /*autoComplete="true"*/  />
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
              initialValue={
                props?.outletDetails != undefined
                  ? props?.outletDetails?.bankDetails
                      ?.entityBankDetailsBranchCode
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Code" /*autoComplete="true"*/  />
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
                props?.outletDetails != undefined
                  ? props?.outletDetails?.bankDetails
                      ?.entityBankDetailsBranchLocation
                  : ""
              }
            >
              <Input aria-autocomplete='both' aria-haspopup="false" placeholder="Bank Location" /*autoComplete="true"*/  />
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

export default OutletPersonalDetails;

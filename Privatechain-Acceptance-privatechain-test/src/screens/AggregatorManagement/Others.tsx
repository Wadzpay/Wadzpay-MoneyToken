import React, { useState } from "react";
import { Radio, Form, Checkbox, Button, DatePicker } from "antd";
import moment from "moment";
import dayjs from "dayjs";
import { compareDatesWithoutTime } from "src/helpers/Utils";
import useFormInstance from "antd/es/form/hooks/useFormInstance";

const checkboxOptions = [
  { label: "Mandatory Field", value: 1 },
  { label: "Show", value: 2 },
  { label: "Edit", value: 3 },
];

const AdminDetails = (props: any) => {
  const [form] = Form.useForm();
  const formInstance=useFormInstance()
  const [isValidExpiryDate, setIsValidExpiryDate] = useState(false)
  const [activationDateCurrent, setActivationDateCurrent] = useState(dayjs(
    props?.allDetails != undefined &&
      props?.allDetails?.others?.entityOthersActivationDate != null
      ? moment(
          props?.allDetails?.others?.entityOthersActivationDate
        ).format("DD/MM/YYYY")
      : moment().format("DD/MM/YYYY")))
      const [activationDateString, setActivationDateString] = useState(
        props?.allDetails != undefined &&
          props?.allDetails?.others?.entityOthersActivationDate != null
          ? moment(
              props?.allDetails?.others?.entityOthersActivationDate
            ).format("DD/MM/YYYY")
          : moment().format("DD/MM/YYYY"))
    
    const disabledDate = (current: any) => {
    return current && current.isBefore(moment().subtract(1, "days"));
  };

  const onFinish = (values: any) => {

    
    const data = {
      aggregatorPreferenceId: {
        value: values.aggregatorPreferenceId,
        isMandatoryField: values.aggregatorIdConfig?.includes(1),
        isShow: values.aggregatorIdConfig?.includes(2),
        isEdit: values.aggregatorIdConfig?.includes(3),
      },
      entityOthersCustomerOfflineTxn: {
        value: values.customerOfflineTransaction,
        isMandatoryField: values.customerOfflineTransactionConfig?.includes(1),
        isShow: values.customerOfflineTransactionConfig.includes(2),
        isEdit: values.customerOfflineTransactionConfig.includes(3),
      },
      entityOthersMerchantOfflineTxn: {
        value: values.merchantOfflineTransaction,
        isMandatoryField: values.merchantOfflineTransactionConfig?.includes(1),
        isShow: values.merchantOfflineTransactionConfig.includes(2),
        isEdit: values.merchantOfflineTransactionConfig.includes(3),
      },
      entityOthersApprovalWorkFlow: {
        value: values.activationWorkFow,
        isMandatoryField: true,
        isShow: true,
        isEdit: true,
      },
      entityOthersActivationDate: {
        value: values.activationDate,
        isMandatoryField: values.adminPhoneNumberConfig?.includes(1),
        isShow: values.adminPhoneNumberConfig?.includes(2),
        isEdit: values.adminPhoneNumberConfig?.includes(3),
      },
      entityOthersExpiryDate: {
        value: values.expiryDate,
        isMandatoryField: values.adminPhoneNumberConfig?.includes(1),
        isShow: values.adminPhoneNumberConfig?.includes(2),
        isEdit: values.adminPhoneNumberConfig?.includes(3),
      },

    };

    props.saveInstitutionData(data);

    props.handleCallBack(true);
  };

  const onFinishFailed = (errorInfo: any) => {
    props.handleCallBack(false);
  };
  const dateValidator = (activationDate:any,expiryDate:any)=> ({
    message: 'Expiry Date must be after the activation date',
    validator(rule:any, value:any) {

      if (value === null) {
        return Promise.resolve();
      }
      if (compareDatesWithoutTime(Date.parse(expiryDate),Date.parse(activationDate))<=0) {
        return Promise.reject(new Error());
      }
      
      return Promise.resolve();
    },
  })
  return (
    <div>
      <Form
      autoComplete="off"
        name="basic"
        form={props.form}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
/*         autoComplete="off"
 */      >
        <div className="form-div mb-3">
          <div className="col-lg-4">Customer Offline Transaction</div>
          <div className="col-lg-3" style={{ textAlign: "right" }}>
            <Form.Item
              name="customerOfflineTransaction"
              initialValue={
                props?.allDetails?.others?.entityOthersCustomerOfflineTxn
                  ? props?.allDetails?.others?.entityOthersCustomerOfflineTxn
                  : "1"
              }
            >
              <Radio.Group
                defaultValue={
                  props?.allDetails?.others?.entityOthersCustomerOfflineTxn
                    ? props?.allDetails?.others?.entityOthersCustomerOfflineTxn
                    : "1"
                }
              >
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
            <Form.Item
              name="merchantOfflineTransaction"
              initialValue={
                props?.allDetails?.others?.entityOthersMerchantOfflineTxn
                  ? props?.allDetails?.others?.entityOthersMerchantOfflineTxn
                  : "1"
              }
            >
              <Radio.Group
                defaultValue={
                  props?.allDetails?.others?.entityOthersMerchantOfflineTxn
                    ? props?.allDetails?.others?.entityOthersMerchantOfflineTxn
                    : "1"
                }
              >
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
          <div className="col-lg-4">Approval Work Flow</div>
          <div className="col-lg-3" style={{ textAlign: "right" }}>
            <Form.Item
              name="activationWorkFow"
              initialValue={
                props?.allDetails?.others?.entityOthersApprovalWorkFlow
                  ? props?.allDetails?.others?.entityOthersApprovalWorkFlow
                  : "1"
              }
            >
              <Radio.Group
                defaultValue={
                  props?.allDetails?.others?.entityOthersApprovalWorkFlow
                    ? props?.allDetails?.others?.entityOthersApprovalWorkFlow
                    : "1"
                }
              >
                <Radio value={"1"}>Enable</Radio>
                <Radio value={"2"}>Disable</Radio>
              </Radio.Group>
            </Form.Item>
          </div>
          <div className="col-lg-1"></div>
          <div className="col-lg-4">
            <Form.Item
              name="activationWorkFowConfig"
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
          <div className="col-lg-5" style={{ textAlign: "right" }}>
            <Form.Item
              name="activationDate"
              label="Activation Date"
              dependencies={['expiryDate']}

              rules={[ { required: true, message: "Pease choose activation date" },/* dateValidator(formInstance?.getFieldValue('activationDate'),formInstance?.getFieldValue('expiryDate')) */
              (formInstance) => ({
                message: 'Activation date must be before the expiry date',
                validator(rule, value) {
                  const expiryDate = formInstance.getFieldValue('expiryDate');
                  if (expiryDate === null||!expiryDate) {
                    return Promise.resolve();
                  }
                  if (compareDatesWithoutTime(Date.parse(value),Date.parse(expiryDate))>=0) {
                    return Promise.reject(new Error());
                  }
                  
                  return Promise.resolve();
                },
              }) 
            ]}
              initialValue={dayjs(
                props?.allDetails != undefined &&
                  props?.allDetails?.others?.entityOthersActivationDate != null
                  ? moment(
                      props?.allDetails?.others?.entityOthersActivationDate
                    ).format("DD/MM/YYYY")
                  : moment().format("DD/MM/YYYY"),
                "DD/MM/YYYY"
              )/* .tz(props?.allDetails?.info?.entityInfoTimezone) */}
            >
              <DatePicker
                style={{ width: "100%" }}
                disabledDate={disabledDate}
                 format={"DD/MM/YYYY"}
                placeholder="DD/MM/YYYY"
                onChange={(date, dateString)=>{
                       console.log("form4.getFieldValue(activationDate)",dateString,"dateString length",dateString.length)
                  var pattern = /^((0[1-9]|[12][0-9]|3[01])(\/)(0[13578]|1[02]))|((0[1-9]|[12][0-9])(\/)(02))|((0[1-9]|[12][0-9]|3[0])(\/)(0[469]|11))(\/)\d{4}$/;
              if(dateString.length>0 && pattern.test(dateString))
              {
                setActivationDateCurrent(dayjs(dateString))
                setActivationDateString(dateString)

                props?.setisActivationDateValid(true)
                //props?.handleCallback(true)

              }
              else{
                props?.setisActivationDateValid(false)
                //props?.handleCallback(false)

              }
          
                }}
/*                 format={ date => date.tz(props?.allDetails?.info?.entityInfoTimezone).format("DD/MM/YYYY")}
 */                disabled={
                  props?.status==="active"
                } 
              />
            </Form.Item>
          </div>
          <div className="col-lg-3"></div>
           <div className="col-lg-4">
            <Form.Item
              name="activationDateConfig"
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
          <div className="col-lg-5" style={{ textAlign: "right" }}>
            <Form.Item
              name="expiryDate"
              label="Expiry Date"
              dependencies={['activationDate']}

              rules={[
               (formInstance) => ({
              message: 'Expiry Date must be after the activation date',
              validator(rule, value) {
                if (value === null) {
                  return Promise.resolve();
                }
                const activationdate = formInstance.getFieldValue('activationDate');

                if (compareDatesWithoutTime(Date.parse(value),Date.parse(activationdate))<=0) {
                  return Promise.reject(new Error());
                }
                
                return Promise.resolve();
              },
            }) ,
        ]}
              initialValue={                props?.allDetails != undefined &&
                props?.allDetails?.others?.entityOthersExpiryDate != null
                ?dayjs(
 moment(
                      props?.allDetails?.others?.entityOthersExpiryDate
                    ).format("DD/MM/YYYY"),
/*                   : moment().add(90, 'days').format("DD/MM/YYYY"),
 */                "DD/MM/YYYY"
              ):undefined/* .tz(props?.allDetails?.info?.entityInfoTimezone) */}
            >
              <DatePicker
                style={{ width: "100%" }}
                disabledDate={disabledDate}
                 format={"DD/MM/YYYY"}
                placeholder="DD/MM/YYYY"
                onChange={(date, dateString)=>{

                                    var pattern = /^((0[1-9]|[12][0-9]|3[01])(\/)(0[13578]|1[02]))|((0[1-9]|[12][0-9])(\/)(02))|((0[1-9]|[12][0-9]|3[0])(\/)(0[469]|11))(\/)\d{4}$/;
              if(dateString.length>0 && pattern.test(dateString))
              {
                if(dayjs(dateString,"DD/MM/YYYY")
                <=dayjs(activationDateString,"DD/MM/YYYY")){
                  props?.setisActivationDateValid(false)
        
                }
                if(Date.parse(dateString)>Date.parse(activationDateString)){
                  {
                    props?.setisActivationDateValid(true)
              }
            }
              else{
                props?.setisExpiryDateValid(true)

              }
          
                }
                else{
                  props?.setisActivationDateValid(true)
  
                }
              }}
/*                 format={ date => date.tz(props?.allDetails?.info?.entityInfoTimezone).format("DD/MM/YYYY")}
 */             /*    disabled={
                  props?.allDetails?.others?.entityOthersExpiryDate <
                  moment().format("YYYY/MM/DD")
                    ? true
                    : false
                } */
              />
            </Form.Item>
          </div>
          <div className="col-lg-3"></div>
           <div className="col-lg-4">
            <Form.Item
              name="expiryDateConfig"
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

export default AdminDetails;

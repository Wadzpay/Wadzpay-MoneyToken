import React, { useState, useRef, useEffect, useContext } from "react";
import { Form, Steps, Tooltip, notification, UploadFile } from "antd";
import ReactS3Client from "react-aws-s3-typescript";
import moment from "moment";
import dayjs from "dayjs";
import "./Aggregator.scss";
import {
  useGetIndustryTypeList,
  useGetFiatCurrencyList,
  useSaveAggregatorDetails,
  useUpdateAggregatorDetails,
  useGetAggregatorDetails,
  useSaveAggregatorDraft,
  useUpdateAggregatorDraft,
} from "src/api/user";
import { SaveContext } from "src/context/SaveContext";
import PageHeading from "src/components/ui/PageHeading";

import AggregatorPersonalDetails from "./AggregatorPersonalDetails";
import Address from "./Address";
import ContactPersonDetails from "./ContactPersonDetails";
import AdminDetails from "./AdminDetails";
import Others from "./Others";
import { useNavigate, useLocation } from "react-router-dom";
import { RouteType } from "src/constants/routeTypes";
import { boolean } from "yup";
import { s3Config } from "src/api/constants";

const steps = [
  {
    key: 1,
    title: "Primary Business Details",
    content:
      "These details will reflect in the wadzpay system by individual institution",
  },
  {
    key: 2,
    title: "Aggregator Address",
    content: "This address will be consider as aggregator address",
  },
  {
    key: 3,
    title: "Contact Person Details",
    content: "This address will be consider as aggregator address",
  },
  {
    key: 4,
    title: "Admin Details",
    content:
      "This admin details are for future responsible for any aggregator related things",
  },
  {
    key: 5,
    title: "Others",
    content:
      "This admin details are for future responsible for any aggregator related things",
  },
];

const AggregatorRegister = () => {
  const location = useLocation();
  const [isActivationDateValid, setisActivationDateValid] = useState(true)
  const {
    mutate: getAggregatorDetails,
    data,
    error,
  } = useGetAggregatorDetails();
  const { isSave, setIsSave, isSaveAndClose, setIsSaveAndClose } =
    useContext(SaveContext);
  const [current, setCurrent] = useState(0);
  const formRef = useRef<HTMLInputElement>(null);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [imageUrl, setImageUrl] = useState("");
  const [loading, setLoading] = useState(true);
  // const [isAcitve, setIsAcitve] = useState<boolean>(true);
  const [form] = Form.useForm();
  const [form1] = Form.useForm();
  const [form2] = Form.useForm();
  const [form3] = Form.useForm();
  const [form4] = Form.useForm();
  const navigate = useNavigate();
  // get industry type list API
  const {
    data: industryTypeList,
    isFetching: isFetchingIndustry,
    error: industryError,
  } = useGetIndustryTypeList();

  // get fial currency list API
  const {
    data: fiatCurrencyList,
    isFetching: isFetchingFiatCurrency,
    error: errorFiatCurrency,
  } = useGetFiatCurrencyList();
const [isExpiryDateValid, setisExpiryDateValid] = useState(true)
  // API Call Save Institution Details
  const {
    mutate: saveInstitutionDetails,
    error: saveAggregatorDetailsError,
    isSuccess,
  } = useSaveAggregatorDetails();
  const {
    mutate: saveAggregatorDraft,
    error: saveAggregatorDraftError,
    isSuccess: draftSuccess,
  } = useSaveAggregatorDraft();

  // API Call Update Institution Details
  const {
    mutate: updateAggregatorDetails,
    error: updateAggregatorDetailsError,
    isSuccess: isSuccessUpdateAggregator,
  } = useUpdateAggregatorDetails();
  const {
    mutate: updateAggregatorDraft,
    error: updateAggregatorDraftError,
    isSuccess: isSuccessUpdatDraft,
  } = useUpdateAggregatorDraft();

  useEffect(() => {
    setIsSave(false);
  }, []);
  useEffect(() => {
    if (saveAggregatorDetailsError) {
      notification["error"]({
        message: "Notification",
        description: saveAggregatorDetailsError?.message,
      });
    }
  }, [saveAggregatorDetailsError]);
  useEffect(() => {
    if (saveAggregatorDraftError) {
      notification["error"]({
        message: "Notification",
        description: saveAggregatorDraftError?.message,
      });
    }
  }, [saveAggregatorDraftError]);
  useEffect(() => {
    if (updateAggregatorDetailsError) {
      notification["error"]({
        message: "Notification",
        description: "Aggregator update failed",
      });
    }
  }, [updateAggregatorDetailsError]);
  useEffect(() => {
    if (updateAggregatorDraftError) {
      notification["error"]({
        message: "Notification",
        description: "Aggregator saving as draft failed ",
      });
    }
  }, [updateAggregatorDraftError]);

  useEffect(() => {
    if (!loading&&(isSave || isSaveAndClose)) {
      saveInstitutionData();
    }}   , [
      isSave,
      isSaveAndClose,
    ]);
    useEffect(() => {
    if (draftSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Aggregator have been saved successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
    if (isSuccessUpdatDraft) {
      notification["success"]({
        message: "Notification",
        description: "Aggregator have been updated successfully.",
        placement: "bottomRight",
      });
      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Aggregator have been created successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }

    if (isSuccessUpdateAggregator) {
      notification["success"]({
        message: "Notification",
        description: "Aggregator deatals have been updated successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
  }, [
    isSuccessUpdatDraft,
    isSuccess,
    draftSuccess,
    isSuccessUpdateAggregator,
  ]);

  const handleCallBack = (value: boolean) => {
    // setIsAcitve(value);
    if (value) {
      next();
    }
  };

  const submitFrom = () => {
    formRef.current?.click();
  };
  const config = {...s3Config,dirName: "test"}


  const next = () => {
    setCurrent(current + 1);
  };

  const prev = () => {
    setCurrent(current - 1);
  };

  const items = steps.map((item: any, key: number) => ({
    key: item.title,
    title: item.title,
    description: key < current ? "Completed" : "",
  }));

  const s3 = new ReactS3Client(config);

  const onLogoChange = async ({ fileList: newFileList }: any) => {
    //   const s3 = new ReactS3Client(config);
    //   console.log(newFileList)
    //   setFileList(newFileList)
    //   if(fileList){
    //   const res = s3.uploadFile(fileList[0]?.originFileObj as File, "agregtor").then(data => console.log(data))
    //   .catch(err => console.error(err)) ;
    // }
    if (fileList[0]?.originFileObj) {
      var url = "";
      const res = await s3
        .uploadFile(
          fileList[0]?.originFileObj as File,
          form.getFieldValue("aggregatorPreferenceId")
        )
        .then((data: any) => {
          console.log(data)
          return data.location;
        })
        .catch((err: any) => console.error(err));
      return res ?? "";
    }
  };

  useEffect(() => {
    // setFileList([
    //   {
    //     uid: '1',
    //     name: 'logo',
    //     status: 'done',
    //     url: location?.state != undefined
    //     ? location?.state?.aggregatorLogo
    //     : null
    //   }])
    if (location?.state?.aggregatorLogo)
      setFileList(
        location?.state?.aggregatorLogo
          ? [
              {
                uid: "1",
                name: "logo",
                status: "done",
                url: location?.state?.aggregatorLogo ?? null,
              },
            ]
          : []
      );
  }, [location?.state?.aggregatorLogo]);
  
  const dateTimeFormat = (time: any,zone:string) => {
    if (!time) {
      return null;
    }

    return dayjs(time). tz(/* zone?? */"Asia/Kuala_Lumpur"). format("YYYY/MM/DD");
  };

  const saveInstitutionData = async () => {
    if (isSave || isSaveAndClose) {
      var activationDate =
        dateTimeFormat((form4.getFieldValue("activationDate") ||  location?.state?.others?.entityOthersActivationDate),/* (form.getFieldValue("aggregatorTimeZone")?.value ||
        location?.state?.info?.entityInfoTimezone) */"")
        var expiryDate =
        dateTimeFormat((form4.getFieldValue("expiryDate") ||  location?.state?.others?.entityOthersExpiryDate),"")
        var pattern = /^((0[1-9]|[12][0-9]|3[01])(\/)(0[13578]|1[02]))|((0[1-9]|[12][0-9])(\/)(02))|((0[1-9]|[12][0-9]|3[0])(\/)(0[469]|11))(\/)\d{4}$/;
    if(activationDate&&pattern.test(activationDate))
    {
      setisActivationDateValid(true)

    }
    else{
      setisActivationDateValid(false)
     // props.handleCallBack(true);

    }
    if(expiryDate&&pattern.test(expiryDate))
    {
      setisExpiryDateValid(true)

    }
    else{
      setisExpiryDateValid(false)
     // props.handleCallBack(true);

    }
      var s2url = await onLogoChange(fileList);
      var request = {
        aggregatorPreferenceId:
          location?.state?.aggregatorPreferenceId ||
          form.getFieldValue("aggregatorPreferenceId"),
          clientAggregatorPreferenceId:form.getFieldValue("clientAggregatorPreferenceId") || location?.state?.aggregatorPreferenceId,
        aggregatorName: form.getFieldValue("aggregatorName") || location?.state?.aggregatorName,
        aggregatorLogo: s2url || location?.state?.aggregatorLogo,
        address: {
          entityAddressAddressLine1:
            form1.getFieldValue("addressLine1") ||
            location?.state?.address?.entityAddressAddressLine1,
          entityAddressAddressLine2:
            form1.getFieldValue("addressLine2") ||
            location?.state?.address?.entityAddressAddressLine2,
          entityAddressAddressLine3:
            form1.getFieldValue("addressLine3") ||
            location?.state?.address?.entityAddressAddressLine3,
          entityAddressCity:
            form1.getFieldValue("city") ||
            location?.state?.address?.entityAddressCity,
          entityAddressState:
            form1.getFieldValue("province") ||
            location?.state?.address?.entityAddressState,
          entityAddressCountry:
            form1.getFieldValue("country") ||
            location?.state?.address?.entityAddressCountry,
          entityAddressPostalCode:
            form1.getFieldValue("postalCode") ||
            location?.state?.address?.entityAddressPostalCode,
        },
        adminDetails: {
          entityAdminDetailsFirstName:
            form3.getFieldValue("adminFirstName") ||
            location?.state?.adminDetails?.entityAdminDetailsFirstName,
          entityAdminDetailsMiddleName:
            form3.getFieldValue("adminMiddleName") ||
            location?.state?.adminDetails?.entityAdminDetailsMiddleName,
          entityAdminDetailsLastName:
            form3.getFieldValue("adminLastName") ||
            location?.state?.adminDetails?.entityAdminDetailsLastName,
          entityAdminDetailsEmailId:
            form3.getFieldValue("adminEmailId") ||
            location?.state?.adminDetails?.entityAdminDetailsEmailId,
          entityAdminDetailsMobileNumber:
            form3.getFieldValue("adminPhoneNumber") ||
            location?.state?.adminDetails?.entityAdminDetailsMobileNumber,
          entityAdminDetailsDepartment:
            form3.getFieldValue("adminDepartment") ||
            location?.state?.adminDetails?.entityAdminDetailsDepartment,
        },
        bankDetails: {
          entityBankDetailsBankName: form.getFieldValue("bankName"),
          entityBankDetailsBankAccountNumber:
            form.getFieldValue("bankAccountNumber"),
          entityBankDetailsBankHolderName: form.getFieldValue("bankHolderName"),
          entityBankDetailsBranchCode: form.getFieldValue("bankCode"),
          entityBankDetailsBranchLocation: form.getFieldValue("bankLocation"),
        },
        contactDetails: {
          entityContactDetailsFirstName:
            form2.getFieldValue("primaryContactFirstName") ||
            location?.state?.contactDetails?.entityContactDetailsFirstName,
          entityContactDetailsMiddleName:
            form2.getFieldValue("primaryContactMiddleName") ||
            location?.state?.contactDetails?.entityContactDetailsMiddleName,
          entityContactDetailsLastName:
            form2.getFieldValue("primaryContactLastName") ||
            location?.state?.contactDetails?.entityContactDetailsLastName,
          entityContactDetailsEmailId:
            form2.getFieldValue("primaryContactEmailId")  ||
            location?.state?.contactDetails?.entityContactDetailsEmailId ,
          entityContactDetailsMobileNumber:
            form2.getFieldValue("primaryContactPhoneNumber") ||
            location?.state?.contactDetails?.entityContactDetailsMobileNumber,
          entityContactDetailsDesignation:
            form2.getFieldValue("primaryContactDesignation") ||
            location?.state?.contactDetails?.entityContactDetailsDesignation,
          entityContactDetailsDepartment:
            form2.getFieldValue("primaryContactDepartment") ||
            location?.state?.contactDetails?.entityContactDetailsDepartment,
        },
        info: {
          entityInfoAbbrevation: form.getFieldValue("aggregatorAbbreviation"),
          entityInfoDescription: form.getFieldValue("aggregatorDescription"),
          entityInfoLogo: "",
          entityInfoRegion: form.getFieldValue("aggregatorRegion"),
          entityInfoTimezone:
            form.getFieldValue("aggregatorTimeZone")?.value ||
            location?.state?.info?.entityInfoTimezone,
          entityInfoType: form.getFieldValue("aggregatorType"),
          entityInfoDefaultDigitalCurrency:
            form.getFieldValue("defaultCurrency"),
          entityInfoBaseFiatCurrency: form.getFieldValue("baseCurrency"),
        },
        others: {
          entityOthersCustomerOfflineTxn:
            form4.getFieldValue("customerOfflineTransaction") ||
            location?.state?.others?.entityOthersCustomerOfflineTxn,
          entityOthersMerchantOfflineTxn:
            form4.getFieldValue("merchantOfflineTransaction") ||
            location?.state?.others?.entityOthersMerchantOfflineTxn,
          entityOthersApprovalWorkFlow:
            form4.getFieldValue("activationWorkFow") ||
            location?.state?.others?.entityOthersApprovalWorkFlow,
          entityOthersActivationDate:
            activationDate ||
            location?.state?.others?.entityOthersActivationDate,
          entityOthersExpiryDate:
            expiryDate ||
            location?.state?.others?.entityOthersExpiryDate,

        },
        aggregatorStatus: !activationDate
          ? "draft"
          : activationDate > moment().zone(form.getFieldValue("aggregatorTimeZone")?.value ||
          location?.state?.info?.entityInfoTimezone).format("YYYY/MM/DD")
          ? "pending"
          : "active" || location?.state?.aggregatorStatus,
      };
      if (location?.state) {
        console.log(
          "===location?.state?.aggregatorStatus=",
          location?.state?.aggregatorStatus,isSaveAndClose,isSave
        );
        if (location?.state?.aggregatorStatus == "draft") {  
          updateAggregatorDraft(request);
          setIsSaveAndClose(false);
        } else {
          updateAggregatorDetails(request);
          setIsSave(false);
        }
      } else {
        if (isSaveAndClose) {
          saveAggregatorDraft(request);
          setIsSaveAndClose(false);
        } else if (isSave) {
          saveInstitutionDetails(request);
          setIsSave(false);
        }
      }
    }
  };
  const requestParams: any = {
    page: 0 || 1,
    // sortBy: "STATUS",
    // sortDirection: "DESC",
    limit: 10000,
  };
  useEffect(() => {
    if (loading) {
      getAggregatorDetails(requestParams);
      setLoading(false);
    }
  }, [loading]);
  useEffect(() => {
  }, [isActivationDateValid])
  return (
    <>
      {data && (
        <div style={{ width: "103%" }}>
          {location?.state ? (
            <PageHeading
              topTitle="Aggregator management"
              backIcon={true}
              title={"Update Aggregator"}
              submitButton="Update Aggregator"
              cancelTitle="Cancel"
              current={current}
              max={4}
              isEdit={true} 
              isActivationDateValid={isActivationDateValid}
              isExpiryDateValid={isExpiryDateValid}

            />
          ) : (
            <PageHeading
              topTitle="Aggregator management"
              backIcon={true}
              title="Register New Aggregator"
              cancelTitle="Cancel"
              buttonTitle="Save & Close"
              submitButton="Register Aggregator"
              current={current}
              isActivationDateValid={isActivationDateValid}
              isExpiryDateValid={isExpiryDateValid}
              max={4}
            />
          )}
          <div className="row bg-white institution-div">
            <div className="col-lg-3 mt-5 mb-5 steps-div">
              <Steps direction="vertical" current={current} items={items} />
            </div>
            <div className="col-lg-8 mt-5 mb-5">
              <div className="title">{steps[current]?.title}</div>
              <div className="content mb-4">{steps[current]?.content}</div>

              {current === 0 ? (
                <AggregatorPersonalDetails
                  industryTypeList={industryTypeList}
                  fiatCurrencyList={fiatCurrencyList}
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveInstitutionData}
                  setFileList={setFileList}
                  fileList={fileList}
                  formRef={formRef}
                  form={form}
                  list={data}
                  aggregatorDetails={location?.state}
                />
              ) : null}
              {current === 1 ? (
                <Address
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveInstitutionData}
                  formRef={formRef}
                  form={form1}
                  allDetails={location?.state}
                />
              ) : null}
              {current === 2 ? (
                <ContactPersonDetails
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveInstitutionData}
                  formRef={formRef}
                  form={form2}
                  allDetails={location?.state}
                />
              ) : null}
              {current === 3 ? (
                <AdminDetails
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveInstitutionData}
                  formRef={formRef}
                  form={form3}
                  allDetails={location?.state}
                />
              ) : null}
              {current === 4 ? (
                <Others
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveInstitutionData}
                  formRef={formRef}
                  form={form4}
                  allDetails={location?.state}
                  status={location?.state?.aggregatorStatus}
                  setisActivationDateValid={setisActivationDateValid}
                  setisExpiryDateValid={setisExpiryDateValid}

                />
              ) : null}

              <div className="buttons d-sm-flex align-items-center justify-content-between">
                {current > 0 && (
                  <span className="back" onClick={() => prev()}>
                    <img src="/images/circle-back.svg" />
                    <span>Back</span>
                  </span>
                )}
                {current <= steps.length - 2 && (
                  <>
                    <span></span>
                    <button
                      style={{
                        justifyContent: "right",
                        border: "none",
                        backgroundColor: "#fff",
                      }}
                      className="next"
                      onClick={() => submitFrom()}
                    >
                      <img src="/images/circle-next.svg" />
                      <span>Next</span>
                    </button>
                  </>
                )}
              </div>
            </div>
            <div className="col-lg-1 mt-5">
              <div className="mt-5">
                <Tooltip
                  placement="topRight"
                  title="Mandatory Field, Show & Edit Configurations are applied for institution admin"
                ></Tooltip>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default AggregatorRegister;

import React, { useState, useRef, useEffect, useContext } from "react";
import { useParams, useLocation } from "react-router-dom";
import { message, Form, Steps, Tooltip, notification, UploadFile } from "antd";
import ReactS3Client from "react-aws-s3-typescript";
import moment from "moment";
import dayjs from "dayjs";
import "./../Aggregator.scss";
import {
  useGetIndustryTypeList,
  useGetFiatCurrencyList,
  useCreateMerchantGroup,
  useUpdateMerchantGroup,
  useGetMerchantGroupList,
  useAggregator,
  useSaveMerchantGroup,
  useUpdateMerchantGroupDraft,
  useMerchantGroup,
} from "src/api/user";
import { SaveContext } from "src/context/SaveContext";
import PageHeading from "src/components/ui/PageHeading";
import { Navigate, useNavigate } from "react-router-dom";
import Address from "../Address";
import ContactPersonDetails from "../ContactPersonDetails";
import AdminDetails from "../AdminDetails";
import Others from "../Others";
import InstitutionPersonalDetails from "../InstitutionManagement/InstitutionPersonalDetails";
import { RouteType } from "src/constants/routeTypes";
import MerchantGroupPersonalDetails from "./MerchantGroupPersonalDetails";
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
    title: "Merchant Group Address",
    content: "This address will be consider as merchant group address",
  },
  {
    key: 3,
    title: "Contact Person Details",
    content: "This address will be consider as merchant group address",
  },
  {
    key: 4,
    title: "Admin Details",
    content:
      "This admin details are for future responsible for any merchant Group related things",
  },
  {
    key: 5,
    title: "Others",
    content:
      "This admin details are for future responsible for any merchant Group related things",
  },
];

const MerchantGroupRegister = (props: any) => {
  const location = useLocation();
  const { isSave, setIsSave, isSaveAndClose, setIsSaveAndClose } =
    useContext(SaveContext);
  const [current, setCurrent] = useState(0);
  const formRef = useRef<HTMLInputElement>(null);
  const [isAcitve, setIsAcitve] = useState<boolean>(true);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [form] = Form.useForm();
  const [form1] = Form.useForm();
  const [form2] = Form.useForm();
  const [form3] = Form.useForm();
  const [form4] = Form.useForm();
  const { aggregatorID, aggregatorName, instituteID, instituteName } =
    useParams();
  const navigate = useNavigate();
  // get industry type list API
  const {
    data: industryTypeList,
    isFetching: isFetchingIndustry,
    error: industryError,
  } = useGetIndustryTypeList();
  const [isActivationDateValid, setisActivationDateValid] = useState(true)

  // get fial currency list API
  const {
    data: fiatCurrencyList,
    isFetching: isFetchingFiatCurrency,
    error: errorFiatCurrency,
  } = useGetFiatCurrencyList();
  const [loading, setLoading] = useState(true);

  const locationArray = location.pathname.split("/");
  const requestParams: any = {
    aggregatorPreferenceId: locationArray[3],
    institutionPreferenceId: locationArray[5],
  };
  const {
    mutate: getMerchantGroupList,
    data,
    error,
  } = useGetMerchantGroupList();
  
  const {
    mutate: createMerchantGroupDetails,
    error: createMerchantGroupDetailsError,
    isSuccess,
  } = useCreateMerchantGroup();

  // API Call Update merchantGroup Details
  const {
    mutate: updateMerchantGroupDetails,
    error: updateMerchantGroupDetailsError,
    isSuccess: isSuccessUpdate,
  } = useUpdateMerchantGroup();
  const {
    mutate: saveMerchantGroupDetails,
    error: saveMerchantGroupDetailsError,
    isSuccess: isSuccessSave,
  } = useSaveMerchantGroup();

  // API Call Update Institution Details
  const {
    mutate: uodateMerchantGroupDraft,
    error: updateMerchantGroupDraftError,
    isSuccess: isSuccessUpdateDraft,
  } = useUpdateMerchantGroupDraft();

  const {
    data: aggData,
    error: aggError,
    isSuccess: aggSuccess,
  } = useAggregator(locationArray[3]);
  useEffect(() => {
    setIsSave(false);
  }, []);
  useEffect(() => {
    if (createMerchantGroupDetailsError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to create Merchant group.",
      });
    }
    if (saveMerchantGroupDetailsError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to save Merchant group.",
      });
    }
    if (updateMerchantGroupDraftError || updateMerchantGroupDetailsError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to update Merchant group ",
      });
    }
  }, [
    createMerchantGroupDetailsError,
    saveMerchantGroupDetailsError,
    updateMerchantGroupDraftError,
    updateMerchantGroupDetailsError,
  ]);

  useEffect(() => {
    if (!loading&&(isSave || isSaveAndClose)) {
      saveMerchantGroupData();
    }
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Merchant group have been created successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
    if (isSuccessSave) {
      notification["success"]({
        message: "Notification",
        description: "Merchant group have been saved successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }

    if (isSuccessUpdate || isSuccessUpdateDraft) {
      notification["success"]({
        message: "Notification",
        description: "Merchant group details have been updated successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
  }, [
    isSave,
    isSuccess,
    isSuccessSave,
    isSuccessUpdateDraft,
    isSaveAndClose,
    isSuccessUpdate,
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

  const config = {...s3Config,dirName: "merchantGroup"}
  
  /* {
    bucketName: `${process.env.NODE_ENV === 'test'}`?"wadzpay-tf-app-bucket-privatechain-test":'wadzpay-app-bucket-privatechain-dev',
    dirName: "merchantGroup" /* optional ,
    region: "me-south-1",
    accessKeyId: `${process.env.NODE_ENV === 'test'}`?"AKIAR3T64BWVHHQXJLTH":'AKIAWG6SXYEE2ZMPWN7S',
    secretAccessKey: `${process.env.NODE_ENV === 'test'}`?"vWIjTwI0+u8KpnNhDEXevZbpcS1k+PjuMhtyeaWU":'zcAg26bUU691Zum9nUSOcZFod5HGWztON4yYEevm',
    //s3Url: 'https:/your-custom-s3-url.com/', /* optional 
  };*/
 
  const next = () => {
    setCurrent(current + 1);
  };

  const prev = () => {
    setCurrent(current - 1);
  };

  const items = steps?.map((item: any, key: number) => ({
    key: item?.title,
    title: item?.title,
    description: key < current ? "Completed" : "",
  }));

  const s3 = new ReactS3Client(config);

  const onLogoChange = async ({ fileList: newFileList }: any) => {
    var url = "";
    if(fileList[0]?.originFileObj as File){
    const res = await s3
      .uploadFile(
        fileList[0]?.originFileObj as File,
        form?.getFieldValue("merchantGroupID")
      )
      .then((data: any) => {
        return data?.location;
      })
      .catch((err: any) => console.error(err));
    console.log("res 11111 == > " + res);
    return res ?? "";}
  };

  useEffect(() => {
    setFileList(
      location?.state?.merchantGroupLogo
        ? [
            {
              uid: "1",
              name: "logo",
              status: "done",
              url: location?.state?.merchantGroupLogo ?? null,
            },
          ]
        : []
    );
  }, [location?.state?.merchantGroupLogo]);

  const dateTimeFormat = (time: any,zone:string) => {

    if (!time) {

      return null;
    }

    return dayjs(time)?.tz(/* zone?? */"Asia/Kuala_Lumpur").format("YYYY/MM/DD");
  };

  const saveMerchantGroupData = async () => {
    if (current === 4 || isSave || isSaveAndClose) {
      var activationDate = dateTimeFormat((form4.getFieldValue("activationDate") ||  location?.state?.others?.entityOthersActivationDate),(form.getFieldValue("aggregatorTimeZone")?.value ||
      location?.state?.info?.entityInfoTimezone))
      var expiryDate =
      dateTimeFormat((form4.getFieldValue("expiryDate") ||  location?.state?.others?.entityOthersExpiryDate),(form.getFieldValue("aggregatorTimeZone")?.value ||
      location?.state?.info?.entityInfoTimezone)) 
      var s2url = await onLogoChange(fileList);

      var request = {
        isDirect: location.state?.direct ? true : false,
        parentType: location.state?.parentType,
        merchantGroup: {
          aggregatorPreferenceId: form.getFieldValue("aggregatorPreferenceId"),
          insitutionPreferenceId: form.getFieldValue("institutionId"),
          merchantGroupPreferenceId: form.getFieldValue("merchantGroupID"),
          clientMerchantGroupId: form.getFieldValue("clientMerchantGroupId"),
          isDirect: location.state?.direct ? true : false,
          parentType: location.state?.parentType,
          merchantGroupName: form.getFieldValue("merchantGroupName"),
          merchantGroupLogo: s2url || location?.state?.merchantAcquirerLogo,
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
            entityBankDetailsBankHolderName:
              form.getFieldValue("bankHolderName"),
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
              form2.getFieldValue("primaryContactEmailId") ||
              location?.state?.contactDetails?.entityContactDetailsEmailId,
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
            entityInfoAbbrevation: form.getFieldValue(
              "merchantGroupAbbreviation"
            ),
            entityInfoDescription: form.getFieldValue(
              "merchantGroupDescription"
            ),
            entityInfoLogo: "",
            entityInfoRegion: form.getFieldValue("merchantGroupRegion"),
            entityInfoTimezone:
              form.getFieldValue("merchantGroupTimeZone").value ||
              location?.state?.info?.entityInfoTimezone,
            entityInfoType: form.getFieldValue("merchantGroupType"),
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
              entityOthersExpiryDate:expiryDate ||location?.state?.others?.entityOthersExpiryDate

          },
          merchantGroupStatus: !activationDate
            ? "draft"
            : activationDate > moment().format("YYYY/MM/DD")
            ? "pending"
            : "active" || location?.state?.merchantGroupStatus,
        },
      };
      if (location?.state && !location.state?.direct) {
        if (location.state.merchantGroupStatus == "draft") {
          uodateMerchantGroupDraft(request);
          setIsSaveAndClose(false);
        } else {
          updateMerchantGroupDetails(request);
          setIsSave(false);
        }
      } else {
        if (location.state?.parentType == "aggregator") {
          Object.assign(request, { parentDataAggregator: aggData });
        }
        if (isSaveAndClose) {
          saveMerchantGroupDetails(request);
          setIsSaveAndClose(false);
        } else {
          createMerchantGroupDetails(request);
          setIsSave(false);
        }
      }
    }
  };
  useEffect(() => {
    if (loading) {
      getMerchantGroupList(requestParams);
      setLoading(false);
    }
  }, [loading]);
  useEffect(() => {
  }, [isActivationDateValid])

  return (
    <>
      {data && (
        <>
          {location?.state && !location.state.direct ? (
            <PageHeading
              topTitle="Merchant group management"
              backIcon={true}
              title={"Update Merchant Group"}
              submitButton="Update Merchant Group"
              cancelTitle="Cancel"
              current={current}
              max={4}
              isEdit={true}
              isActivationDateValid={isActivationDateValid}
            />
          ) : (
            <PageHeading
              backIcon={true}
              title="Register New Merchant Group"
              cancelTitle="Cancel"
              buttonTitle="Save & Close"
              submitButton="Register Merchant Group"
              current={current}
              max={4}
              isActivationDateValid={isActivationDateValid}
            />
          )}
          <div
            className="row bg-white institution-div"
            style={{ width: "105%" }}
          >
            <div className="col-lg-3 mt-5 mb-5 steps-div">
              <Steps direction="vertical" current={current} items={items} />
            </div>
            <div className="col-lg-8 mt-5 mb-5">
              <div className="title">{steps[current]?.title}</div>
              <div className="content mb-4">{steps[current]?.content}</div>

              {current === 0 ? (
                <MerchantGroupPersonalDetails
                  industryTypeList={industryTypeList}
                  fiatCurrencyList={fiatCurrencyList}
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveMerchantGroupData}
                  formRef={formRef}
                  form={form}
                  setFileList={setFileList}
                  fileList={fileList}
                  aggregatorID={aggregatorID}
                  aggregatorName={aggregatorName}
                  instituteID={instituteID}
                  insitutionName={instituteName}
                  merchantDetails={
                    location.state?.direct ? undefined : location?.state
                  }
                  list={data}
                />
              ) : null}
              {current === 1 ? (
                <Address
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveMerchantGroupData}
                  formRef={formRef}
                  form={form1}
                  allDetails={
                    location.state?.direct ? undefined : location?.state
                  }
                />
              ) : null}
              {current === 2 ? (
                <ContactPersonDetails
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveMerchantGroupData}
                  formRef={formRef}
                  form={form2}
                  allDetails={
                    location.state?.direct ? undefined : location?.state
                  }
                />
              ) : null}
              {current === 3 ? (
                <AdminDetails
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveMerchantGroupData}
                  formRef={formRef}
                  form={form3}
                  allDetails={
                    location.state?.direct ? undefined : location?.state
                  }
                />
              ) : null}
              {current === 4 ? (
                <Others
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveMerchantGroupData}
                  formRef={formRef}
                  form={form4}
                  status={location?.state?.merchantGroupStatus}
                  allDetails={
                    location.state?.direct ? undefined : location?.state
                  }
                  setisActivationDateValid={setisActivationDateValid}
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
        </>
      )}
    </>
  );
};

export default MerchantGroupRegister;

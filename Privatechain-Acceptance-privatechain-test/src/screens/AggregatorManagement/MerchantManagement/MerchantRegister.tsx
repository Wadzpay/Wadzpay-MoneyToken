import React, { useState, useRef, useEffect, useContext } from "react";
import { useParams, useLocation } from "react-router-dom";
import {
  message,
  Form,
  Steps,
  Tooltip,
  notification,
  UploadFile,
  DatePickerProps,
} from "antd";
import dayjs from "dayjs";
import ReactS3Client from "react-aws-s3-typescript";
import moment from "moment";
import "./../Aggregator.scss";
import {
  useGetIndustryTypeList,
  useGetFiatCurrencyList,
  useCreateMerchantAcquirer,
  useUpdateMerchantAcquirer,
  useGetMerchantAcquirerList,
  useAggregator,
  useInstitution,
  useSaveMerchantAcquirer,
  useUpdateMerchantAcquirerDraft,
} from "src/api/user";
import { SaveContext } from "src/context/SaveContext";
import PageHeading from "src/components/ui/PageHeading";
import { Navigate, useNavigate } from "react-router-dom";
import Address from "./../Address";
import ContactPersonDetails from "./../ContactPersonDetails";
import AdminDetails from "./../AdminDetails";
import Others from "./../Others";
import InstitutionPersonalDetails from "../InstitutionManagement/InstitutionPersonalDetails";
import { RouteType } from "src/constants/routeTypes";
import MerchantPersonalDetails from "./MerchantPersonalDetails";
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
    title: "Merchant Address",
    content: "This address will be consider as merchant address",
  },
  {
    key: 3,
    title: "Contact Person Details",
    content: "This address will be consider as merchant address",
  },
  {
    key: 4,
    title: "Admin Details",
    content:
      "This admin details are for future responsible for any merchant related things",
  },
  {
    key: 5,
    title: "Others",
    content:
      "This admin details are for future responsible for any merchant related things",
  },
];

const MerchantRegister = () => {
  const location = useLocation();
  const { isSave, setIsSave, isSaveAndClose, setIsSaveAndClose } =
    useContext(SaveContext);
  const [current, setCurrent] = useState(0);
  const formRef = useRef<HTMLInputElement>(null);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [activateDate, setActivateDate] = useState<string>("");
  const [form] = Form.useForm();
  const [form1] = Form.useForm();
  const [form2] = Form.useForm();
  const [form3] = Form.useForm();
  const [form4] = Form.useForm();
  const {
    aggregatorID,
    aggregatorName,
    instituteID,
    instituteName,
    merchantGroupID,
    merchantGroupName,
  } = useParams();
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

  // API Call Save merchant Details
  const {
    mutate: createMerchantDetails,
    error: createMerchantDetailsError,
    isSuccess,
  } = useCreateMerchantAcquirer();

  // API Call Save merchant Details
  const {
    mutate: updateMerchantDetails,
    error: updateMerchantDetailsError,
    isSuccess: isSuccessUpdate,
  } = useUpdateMerchantAcquirer();
  const {
    mutate: saveMerchantDetails,
    error: saveMerchantDetailsError,
    isSuccess: isSuccessSave,
  } = useSaveMerchantAcquirer();
  const [isActivationDateValid, setisActivationDateValid] = useState(true)

  // API Call Save merchant Details
  const {
    mutate: updateMerchantDetailsDraft,
    error: updateMerchantDetailsDraftError,
    isSuccess: isSuccessUpdateDraft,
  } = useUpdateMerchantAcquirerDraft();
  const [loading, setLoading] = useState(true);

  const locationArray = location.pathname.split("/");
  const {
    data: aggData,
    error: aggError,
    isSuccess: aggSuccess,
  } = useAggregator(locationArray[3]);
  const {
    data: instData,
    error: instError,
    isSuccess: instSuccess,
  } = useInstitution(
    locationArray[5],
    location.state?.parentType == "institution"
  );
  const requestParams: any = {
    aggregatorPreferenceId: locationArray[3],
    institutionPreferenceId: locationArray[5],
    merchantGroupPreferenceId: locationArray[7],
  };
  const { mutate: getMerchantList, data, error } = useGetMerchantAcquirerList();
  useEffect(() => {
    setIsSave(false);
  }, []);
  useEffect(() => {
    if (updateMerchantDetailsError || createMerchantDetailsError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to update merchnat",
      });
    }
    if (saveMerchantDetailsError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to save  merchnat",
      });
    }
    if (createMerchantDetailsError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to create merchnat",
      });
    }
  }, [
    updateMerchantDetailsError,
    updateMerchantDetailsDraftError,
    saveMerchantDetailsError,
    createMerchantDetailsError,
  ]);

  useEffect(() => {
    if (!loading&&isSave || isSaveAndClose) {
      saveMerchantData();
    }
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Merchant have been created successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
    if (isSuccessSave) {
      notification["success"]({
        message: "Notification",
        description: "Merchant have been saved successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }

    if (isSuccessUpdate || isSuccessUpdateDraft) {
      notification["success"]({
        message: "Notification",
        description: "Merchant  details have been updated successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
  }, [
    isSave,
    isSaveAndClose,
    isSuccessSave,
    isSuccessUpdateDraft,
    isSuccess,
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
  const config = {...s3Config,dirName: "merchant"}

/*   const config = {
    bucketName: "wadzpay-tf-app-bucket-geidea-dev",
    dirName: "merchant" /* optional ,
    region: "me-south-1",
    accessKeyId: "AKIAWG6SXYEE35ETM7QR",
    secretAccessKey: "SaRlq+dwctmqxWpmxXITidCnisQl8eh7Lgzmr6ai",
    //s3Url: 'https:/your-custom-s3-url.com/', /* optional 
  };
 */
  const next = () => {
    setCurrent(current + 1);

    if (current === steps.length - 1) {
      notification["success"]({
        message: "Notification",
        description: "You have successfully registered an mechant.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
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
    console.log("inside res ");
    var url = "";
    if (fileList.length > 0 && fileList[0]?.originFileObj) {
      const res = await s3
        .uploadFile(
          fileList[0]?.originFileObj as File,
          form.getFieldValue("merchantAcquirerId")
        )
        .then((data: any) => {
          return data.location;
        })
        .catch((err: any) => console.error(err));
      return res ?? "";
    }
  };

  useEffect(() => {
    setFileList(
      location?.state?.merchantAcquirerLogo
        ? [
            {
              uid: "1",
              name: "logo",
              status: "done",
              url: location?.state?.merchantAcquirerLogo ?? null,
            },
          ]
        : []
    );
  }, [location?.state?.merchantAcquirerLogo]);

  const dateTimeFormat = (time: any) => {
    if (!time) {
      return null;
    }

    return dayjs(time).tz("Asia/Kuala_Lumpur").format("YYYY/MM/DD");
  };

  const saveMerchantData = async () => {
    if (current === 4 || isSave || isSaveAndClose) {
     /*  var activationDate =
        dateTimeFormat(form4.getFieldValue("activationDate")) ||
        location?.state?.others?.entityOthersActivationDate; */
 var activationDate = dateTimeFormat((form4.getFieldValue("activationDate") ||  location?.state?.others?.entityOthersActivationDate),
 /* (form.getFieldValue("aggregatorTimeZone")?.value ||
        location?.state?.info?.entityInfoTimezone) */)
        var expiryDate = dateTimeFormat(form4.getFieldValue("expiryDate") ||  location?.state?.others?.entityOthersExpiryDate) 
        console.log("form4.getFieldValue(activationDate)",activationDate)
        var pattern = /^((0[1-9]|[12][0-9]|3[01])(\/)(0[13578]|1[02]))|((0[1-9]|[12][0-9])(\/)(02))|((0[1-9]|[12][0-9]|3[0])(\/)(0[469]|11))(\/)\d{4}$/;
    if(activationDate&&pattern.test(activationDate))
    {
      setisActivationDateValid(true)

    }
    else{
console.log("else validation false")
      setisActivationDateValid(false)
     // props.handleCallBack(true);

    }

      var s2url = await onLogoChange(fileList);
      var request = {
        isDirect: location.state?.direct ? true : false,
        parentType: location.state?.parentType,
        merchant: {
          merchantAcquirerId: form.getFieldValue("merchantAcquirerId"),
          aggregatorPreferenceId: aggregatorID,
          insitutionPreferenceId: instituteID,
          merchantGroupPreferenceId: merchantGroupID,
          merchantAcquirerName: form.getFieldValue("merchantAcquirerName"),
          clientMerchantAcquirerId: form.getFieldValue("clientMerchantAcquirerId"),
          merchantAcquirerLogo: s2url || location?.state?.merchantAcquirerLogo,
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
            entityInfoAbbrevation: form.getFieldValue("merchantAbbreviation"),
            entityInfoDescription: form.getFieldValue(
              "merchantGroupDescription"
            ),
            entityInfoLogo: "",
            entityInfoRegion: form.getFieldValue("merchantGroupRegion"),
            entityInfoTimezone:
              form.getFieldValue("merchantTimeZone")?.value ||
              location?.state?.info?.entityInfoTimezone,
            entityInfoType: form.getFieldValue("merchantType"),
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
          merchantAcquirerStatus: !activationDate
            ? "draft"
            : activationDate > moment().format("YYYY/MM/DD")
            ? "pending"
            : "active" || location?.state?.merchantAcquirerStatus,
        },
      };
      if (location?.state && !location.state?.direct) {
        if (location?.state.merchantAcquirerStatus == "draft") {
          updateMerchantDetailsDraft(request);
          setIsSaveAndClose(false);
        } else {
          updateMerchantDetails(request);
          setIsSave(false);
        }
      } else {
        if (location.state?.parentType == "aggregator") {
          Object.assign(request, { parentDataAggregator: aggData });
        }
        if (location.state?.parentType === "institution") {
          Object.assign(request, { parentDataInstitution: instData });
        }
        if (isSaveAndClose) {
          saveMerchantDetails(request);
          setIsSaveAndClose(false);
        } else {
          createMerchantDetails(request);
          setIsSave(false);
        }
      }
    }
  };

  console.log(">>>>>>>> mohit location?.state", location?.state);
  useEffect(() => {
    if (loading) {
      getMerchantList(requestParams);
      setLoading(false);
    }
  }, [loading]);
  useEffect(() => {
  }, [isActivationDateValid])

  return (
    <>
      {data && (
        <div
          className="row bg-white institution-div p-1"
          style={{ width: "105%" }}
        >
          {location?.state && !location.state.direct ? (
            <PageHeading
              topTitle="Merchant management"
              backIcon={true}
              title={"Update Merchant"}
              submitButton="Update Merchant"
              cancelTitle="Cancel"
              current={current}
              max={4}
              isEdit={true}
              isActivationDateValid={isActivationDateValid}

            />
          ) : (
            <PageHeading
              topTitle="Merchant management"
              backIcon={true}
              title="Register New Merchant"
              cancelTitle="Cancel"
              buttonTitle="Save & Close"
              submitButton="Register Merchant"
              current={current}
              max={4}
              isActivationDateValid={isActivationDateValid}

            />
          )}
          <hr className="mt-2" style={{ color: "#b9b9b9" }} />
          <div className="col-lg-3 mt-3 mb-5 steps-div">
            <Steps direction="vertical" current={current} items={items} />
          </div>
          <div className="col-lg-8 mt-5 mb-5">
            <div className="title">{steps[current]?.title}</div>
            <div className="content mb-4">{steps[current]?.content}</div>

            {current === 0 ? (
              <MerchantPersonalDetails
                industryTypeList={industryTypeList}
                fiatCurrencyList={fiatCurrencyList}
                handleCallBack={handleCallBack}
                saveInstitutionData={saveMerchantData}
                formRef={formRef}
                form={form}
                setFileList={setFileList}
                fileList={fileList}
                aggregatorID={aggregatorID}
                aggregatorName={aggregatorName}
                instituteID={instituteID}
                insitutionName={instituteName}
                merchantGroupPreferenceId={merchantGroupID}
                merchantGroupName={merchantGroupName}
                merchantDetails={
                  location.state?.direct ? undefined : location?.state
                }
                list={data}
              />
            ) : null}
            {current === 1 ? (
              <Address
                handleCallBack={handleCallBack}
                saveInstitutionData={saveMerchantData}
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
                saveInstitutionData={saveMerchantData}
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
                saveInstitutionData={saveMerchantData}
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
                saveInstitutionData={saveMerchantData}
                formRef={formRef}
                form={form4}
                status={location?.state?.merchantAcquirerStatus}
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
              >
                <img src="/images/info-icon.svg" />
              </Tooltip>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default MerchantRegister;

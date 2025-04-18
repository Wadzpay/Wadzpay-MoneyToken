import React, { useState, useRef, useEffect, useContext } from "react";
import { useParams } from "react-router-dom";
import { message, Form, Steps, Tooltip, notification, UploadFile } from "antd";
import { InfoCircleOutlined } from "@ant-design/icons";
import ReactS3Client from "react-aws-s3-typescript";
import moment from "moment";
import dayjs from "dayjs";
import "./../Aggregator.scss";
import {
  useGetIndustryTypeList,
  useGetFiatCurrencyList,
  useCreateInstitution,
  useUpdateInstitutionDetails,
  useGetInstitutionList,
  useSaveInstitution,
  useUpdateInstitutionDraft,
} from "src/api/user";
import { SaveContext } from "src/context/SaveContext";
import PageHeading from "src/components/ui/PageHeading";
import { Navigate, useNavigate, useLocation } from "react-router-dom";
import Address from "../Address";
import ContactPersonDetails from "../ContactPersonDetails";
import AdminDetails from "../AdminDetails";
import Others from "../Others";
import InstitutionPersonalDetails from "./InstitutionPersonalDetails";
import { RouteType } from "src/constants/routeTypes";
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
    title: "Institution Address",
    content: "This address will be consider as institution address",
  },
  {
    key: 3,
    title: "Contact Person Details",
    content: "This address will be consider as institution address",
  },
  {
    key: 4,
    title: "Admin Details",
    content:
      "This admin details are for future responsible for any institutional related things",
  },
  {
    key: 5,
    title: "Others",
    content:
      "This admin details are for future responsible for any institutional related things",
  },
];

const InstitutionRegister = () => {
  const location = useLocation();
  const { isSave, setIsSave, isSaveAndClose, setIsSaveAndClose } =
    useContext(SaveContext);

  const navigate = useNavigate();
  const [current, setCurrent] = useState(0);
  const [loading, setLoading] = useState(true);

  const locationArray = location.pathname.split("/");
  const requestParams: any = {
    aggregatorPreferenceId: locationArray[3],
  };
  const { mutate: getInstiutionList, data, error } = useGetInstitutionList();
  const formRef = useRef<HTMLInputElement>(null);
  const [isAcitve, setIsAcitve] = useState<boolean>(true);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [form] = Form.useForm();
  const [form1] = Form.useForm();
  const [form2] = Form.useForm();
  const [form3] = Form.useForm();
  const [form4] = Form.useForm();
  const { aggregatorID, refId } = useParams();
  const [isActivationDateValid, setisActivationDateValid] = useState(true)


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

  // API Call Save Institution Details
  const {
    mutate: createInstitution,
    error: createInstitutionError,
    isSuccess,
  } = useCreateInstitution();
  const {
    mutate: saveInstitution,
    error: saveInstitutionError,
    isSuccess: isSuccessSave,
  } = useSaveInstitution();

  // API Call Update Institution Details
  const {
    mutate: updateInstitution,
    error: updateInstitutionError,
    isSuccess: isSuccessUpdate,
  } = useUpdateInstitutionDetails();
  const {
    mutate: updateInstitutionDraft,
    error: updateInstitutionDraftError,
    isSuccess: isSuccessUpdateDraft,
  } = useUpdateInstitutionDraft();

  useEffect(() => {
    setIsSave(false);
  }, []);
  useEffect(() => {
    if (updateInstitutionError || updateInstitutionDraftError) {
      notification["error"]({
        message: "Notification",
        description: "Update Failed",
      });
    }
    if (createInstitutionError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to create Institution",
      });
    }
    if (saveInstitutionError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to save Institution",
      });
    }
  }, [
    updateInstitutionError,
    createInstitutionError,
    updateInstitutionDraftError,
    saveInstitutionError,
  ]);

  useEffect(() => {
    if (!loading&&(isSave || isSaveAndClose)) {
      saveMerchantGroupData();
    }
    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Institution have been created successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
    if (isSuccessSave) {
      notification["success"]({
        message: "Notification",
        description: "Institution have been saved successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
    if (isSuccessUpdate || isSuccessUpdateDraft) {
      notification["success"]({
        message: "Notification",
        description: "Institution details have been updated successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
  }, [
    isSave,
    isSuccess,
    isSuccessSave,
    isSuccessUpdateDraft,
    isSuccessUpdate,
    isSaveAndClose,
  ]);

  const handleCallBack = (value: boolean) => {
    // setIsAcitve(value);
    console.log(">>>>> mohit value", value);
    if (value) {
      next();
    }
  };

  const submitFrom = () => {
    formRef.current?.click();
  };
  const config = {...s3Config,dirName: "institutions"}


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
    var url = "";
    const res = await s3
      .uploadFile(
        fileList[0]?.originFileObj as File,
        form.getFieldValue("institutionId")
      )
      .then((data: any) => {
        return data.location;
      })
      .catch((err: any) => console.error(err));
    return res ?? "";
  };

  useEffect(() => {
    setFileList(
      location?.state?.institutionLogo
        ? [
            {
              uid: "1",
              name: "logo",
              status: "done",
              url: location?.state?.institutionLogo ?? null,
            },
          ]
        : []
    );
  }, [location?.state?.institutionLogo]);

  const dateTimeFormat = (time: any,zone:string) => {

    if (!time) {

      return null;
    }

    return dayjs(time)?.tz(/* zone?? */"Asia/Kuala_Lumpur").format("YYYY/MM/DD");
  };


  const saveMerchantGroupData = async () => {
    if (current === 4 || isSave || isSaveAndClose) {
      var activationDate =
      dateTimeFormat((form4.getFieldValue("activationDate") ||  location?.state?.others?.entityOthersActivationDate),(form.getFieldValue("aggregatorTimeZone")?.value ||
      location?.state?.info?.entityInfoTimezone)) 
      var expiryDate =
      dateTimeFormat((form4.getFieldValue("expiryDate") ||  location?.state?.others?.entityOthersExpiryDate),(form.getFieldValue("aggregatorTimeZone")?.value ||
      location?.state?.info?.entityInfoTimezone)) 

      //||        location?.state?.others?.entityOthersActivationDate;

      var s2url = await onLogoChange(fileList);

      var request = {
        aggregatorPreferenceId:
          form.getFieldValue("aggregatorPreferenceId") ||
          location?.state?.aggregatorPreferenceId,
        institutionId:
          form.getFieldValue("institutionId") || location?.state?.institutionId,
        insitutionPreferenceId: form.getFieldValue("clientInstitutionId"),
        insitutionName: form.getFieldValue("institutionName"),
        institutionLogo: s2url || location?.state?.institutionLogo,
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
          entityInfoAbbrevation: form.getFieldValue("institutionAbbreviation"),
          entityInfoDescription: form.getFieldValue("institutionDescription"),
          entityInfoLogo: "",
          entityInfoRegion: form.getFieldValue("institutionRegion"),
          entityInfoTimezone:
            form.getFieldValue("institutionAggregatorTimeZone").value ||
            location?.state?.info?.entityInfoTimezone,
          entityInfoType: form.getFieldValue("institutionType"),
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
        insitutionStatus: !activationDate
          ? "draft"
          : activationDate > moment().format("YYYY/MM/DD")
          ? "pending"
          : "active" || location?.state?.insitutionStatus,
      };
      // console.log(request);
      // return;
      console.log(location.state);
      if (location?.state && !location?.state.direct) {
        if (location.state?.insitutionStatus == "draft") {
          updateInstitutionDraft(request);
        } else {
          updateInstitution(request);
          setIsSave(false);
        }
      } else {
        if (isSaveAndClose) {
          saveInstitution(request);
          setIsSaveAndClose(false);
        } else {
          createInstitution(request);
          setIsSave(false);
        }
      }
    }
  };
  useEffect(() => {
    if (loading) {
      getInstiutionList(requestParams);
      setLoading(false);
    }
  }, [loading]);

  return (
    <>
      {data && (
        <>
          {location?.state && !location.state.direct ? (
            <PageHeading
              topTitle="Institution management"
              backIcon={true}
              title={"Update Institution"}
              submitButton="Update Institution"
              cancelTitle="Cancel"
              current={current}
              max={4}
              isEdit={true}
              isActivationDateValid={isActivationDateValid}

            />
          ) : (
            <PageHeading
              topTitle="Institution management"
              backIcon={true}
              title="Register New Institution"
              cancelTitle="Cancel"
              buttonTitle="Save & Close"
              submitButton="Register Institution"
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
                <InstitutionPersonalDetails
                  industryTypeList={industryTypeList}
                  fiatCurrencyList={fiatCurrencyList}
                  handleCallBack={handleCallBack}
                  saveInstitutionData={saveMerchantGroupData}
                  setFileList={setFileList}
                  fileList={fileList}
                  formRef={formRef}
                  list={data}
                  form={form}
                  aggregatorID={aggregatorID}
                  refId={refId}
                  institutionDetails={
                    location.state?.direct ? undefined : location?.state
                  }
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
                  allDetails={
                    location.state?.direct ? undefined : location?.state
                  }
                  status={location?.state?.insitutionStatus}
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

export default InstitutionRegister;

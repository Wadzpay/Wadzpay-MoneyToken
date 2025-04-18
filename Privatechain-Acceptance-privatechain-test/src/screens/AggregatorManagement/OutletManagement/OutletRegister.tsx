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
  Button,
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
  useCreateSubMerchantAcquirer,
  useUpdateSubMerchantAcquirer,
  useUpdateOutlet,
  useCreateOutlet,
  useGetOutletList,
  useAggregator,
  useInstitution,
  useMerchantGroup,
  useMerchant,
  useMerchantAcquirer,
  useSaveOutlet,
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
import OutletPersonalDetails from "./OutletPersonalDetails";
import AddPos from "../PosManagement/Pos";
import { Pos } from "src/api/models";
import { useValidationSchemas } from "src/constants/validationSchemas";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { OutletContext, OutletContextProvider } from "./context/OutletContext";
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
    title: "Outlet Address",
    content: "This address will be consider as Outlet address",
  },
  {
    key: 3,
    title: "Contact Person Details",
    content: "This address will be consider as outlet address",
  },
  {
    key: 4,
    title: "Admin Details",
    content:
      "This admin details are for future responsible for any outlet related things",
  },
  {
    key: 5,
   /*  title: "Pos Terminals",
    content:
      "This admin details are for future responsible for any outlet related things",
  },
  {
    key: 6,
    */ title: "Others",
    content:
      "This admin details are for future responsible for any outlet related things",
  },
];

const OutletRegister = () => {
  const location = useLocation();
  const { isSave, setIsSave, isSaveAndClose, setIsSaveAndClose } =
    useContext(SaveContext);
  //const { isSavePos, setIsSavePos,isListing,setIsListing } = useContext(OutletContext);
  const [current, setCurrent] = useState(0);
  const formRef = useRef<HTMLInputElement>(null);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [activateDate, setActivateDate] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [isExpiryDateValid, setisExpiryDateValid] = useState(true)
/*   const [posArray, setPosArray] = useState<Array<Pos>>(
    location?.state?.posList ?? []
  );
 */  const locationArray = location.pathname.split("/");
  const [instEnabled, setInstEnabled] = useState(false);
  const [mgroupEnabled, setMGroupEnabled] = useState(false);
  const [merchantEnabled, setMerchantEnabled] = useState(false);
  const requestParams: any = {
    aggregatorPreferenceId: locationArray[3],
    institutionPreferenceId: locationArray[5],
    merchantGroupPreferenceId: locationArray[7],
    merchantAcquirerPreferenceId: locationArray[9],
    subMerchantPreferenceId: locationArray[11],
  };
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
  const {
    data: mgData,
    error: mgError,
    isSuccess: mgSuccess,
  } = useMerchantGroup(
    locationArray[7],
    location.state?.parentType == "merchantGroup"
  );
  const {
    data: mcData,
    error: mcError,
    isSuccess: mcSuccess,
  } = useMerchantAcquirer(
    locationArray[9],
    location.state?.parentType == "merchant"
  );
  const [isActivationDateValid, setisActivationDateValid] = useState(true)

  const {
    mutate: getOutletDetails,
    data,
    isSuccess: done,
    error,
  } = useGetOutletList();
  const [form] = Form.useForm();
  const [form1] = Form.useForm();
  const [form2] = Form.useForm();
  const [form3] = Form.useForm();
  const [form4] = Form.useForm();
/*   const [form5] = Form.useForm();
 */
  const {
    aggregatorID,
    aggregatorName,
    instituteID,
    instituteName,
    merchantGroupID,
    merchantGroupName,
    merchantAcquirerId,
    merchantAcquirerName,
    subMerchantId,
    subMerchantName,
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
    mutate: createOutletDetails,
    error: createOutletError,
    isSuccess,
  } = useCreateOutlet();
  const {
    mutate: saveOutletDetails,
    error: saveOutletError,
    isSuccess: isSuccessSave,
  } = useSaveOutlet();

  // API Call Save merchant Details
  const {
    mutate: updateOutletDetails,
    error: updatOutletError,
    isSuccess: isSuccessUpdate,
  } = useUpdateOutlet();
  const {
    mutate: updateOutletDetailsDraft,
    error: updateOutletErrorDraft,
    isSuccess: isSuccessUpdateDraft,
  } = useUpdateOutlet();

  useEffect(() => {
    setIsSave(false);
  }, []);
  useEffect(() => {
    if (loading) {
      getOutletDetails(requestParams);
      setLoading(false);
    }
  }, [loading]);
  useEffect(() => {
    if (updatOutletError || updateOutletErrorDraft) {
      notification["error"]({
        message: "Notification",
        description: "Failed to update outlet",
      });
    }
    if (createOutletError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to created outlet",
      });
    }
    if (saveOutletError) {
      notification["error"]({
        message: "Notification",
        description: "Failed to save outlet",
      });
    }
  }, [
    createOutletError,
    updatOutletError,
    saveOutletError,
    updateOutletErrorDraft,
  ]);

  useEffect(() => {
    if (!loading&&(isSave || isSaveAndClose)) {
      saveMerchantData();
    }
    console.log(createOutletError);

    if (isSuccess) {
      notification["success"]({
        message: "Notification",
        description: "Outlet have been created successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
    if (isSuccessSave) {
      notification["success"]({
        message: "Notification",
        description: "Outlet have been save successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
    if (isSuccessUpdate || isSuccessUpdateDraft) {
      notification["success"]({
        message: "Notification",
        description: "Outlet details have been updated successfully.",
        placement: "bottomRight",
      });

      navigate(RouteType.AGGREGATOR_MANAGEMENT);
    }
  }, [
    isSave,
    isSaveAndClose,
    isSuccessUpdateDraft,
    isSuccessSave,
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
    if (showForm) {
      setShowForm(false);
    }
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
  };

  useEffect(() => {
    setFileList(
      location?.state?.subMerchantAcquirerLogo
        ? [
            {
              uid: "1",
              name: "logo",
              status: "done",
              url: location?.state?.subMerchantAcquirerLogo ?? null,
            },
          ]
        : []
    );
  }, [location?.state?.subMerchantAcquirerLogo]);

  const dateTimeFormat = (time: any) => {
    if (!time) {
      return null;
    }

    return dayjs(time).tz("Asia/Kuala_Lumpur").format("YYYY/MM/DD");
  };
  const savePosToList = (pos: Pos) => {};
  const saveMerchantData = async () => {
    if (current === 4 || isSave || isSaveAndClose) {
      var activationDate =
        dateTimeFormat(form4.getFieldValue("activationDate")) ||
        location?.state?.others?.entityOthersActivationDate;
        var expiryDate =
        dateTimeFormat((form4.getFieldValue("expiryDate") ||  location?.state?.others?.entityOthersExpiryDate)) 
      var s2url = await onLogoChange(fileList);
      var request = {
        isDirect: location.state?.direct ? true : false,
        parentType: location.state?.parentType,
        aggregatorName,
        instituteName,
        merchantGroupName,
        merchantAcquirerName,
        outlet: {
          outletId: form.getFieldValue("outletId"),
          clientOutletId:form.getFieldValue("clientOutletId"),
          outletName: form.getFieldValue("outletName"),
          outletLogo: s2url || location?.state?.outletLogo,
          subMerchantPreferenceId: subMerchantId,
          aggregatorPreferenceId: aggregatorID,
          insitutionPreferenceId: instituteID,
          merchantGroupPreferenceId: merchantGroupID,
          merchantAcquirerPreferenceId: merchantAcquirerId,
          subMerchantAcquirerLogo:
            s2url || location?.state?.merchantAcquirerLogo,
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
            entityInfoAbbrevation: form.getFieldValue("outletAbbreviation"),
            entityInfoDescription: form.getFieldValue("outletDescription"),
            entityInfoLogo: "",
            entityInfoRegion: form.getFieldValue("outletRegion"),
            entityInfoTimezone:
              form.getFieldValue("outletTimeZone").value ||
              location?.state?.info?.entityInfoTimezone,
            entityInfoType: form.getFieldValue("outletType"),
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
          outletStatus: !activationDate
            ? "draft"
            : activationDate > moment().format("YYYY/MM/DD")
            ? "pending"
            : "active" || location?.state?.outletStatus
        },
      };

      if (location?.state && !location.state?.direct) {
        if (location?.state?.outletStatus=="draft") {
          updateOutletDetailsDraft(request);
          setIsSaveAndClose(false);
        } else {
          updateOutletDetails(request);
          setIsSave(false);
        }
      } else {
        if (location.state?.parentType == "aggregator") {
          Object.assign(request, { parentDataAggregator: aggData });
        }
        if (location.state?.parentType === "institution") {
          Object.assign(request, { parentDataInstitution: instData });
        }
        if (location.state?.parentType === "merchantGroup") {
          Object.assign(request, { parentDataMerchantGroup: mgData });
        }
        if (location.state?.parentType === "merchant") {
          Object.assign(request, { parentDataMerchantAcquirer: mcData });
        }
        if (isSaveAndClose) {
          saveOutletDetails(request);
          setIsSaveAndClose(false);
        } else {
          createOutletDetails(request);
          setIsSave(false);
        }
      }
    }
  };
 /*  const removePosInOutlet = (pos: Pos, remove: boolean) => {
    if (remove) {
      setPosArray([...posArray.filter((obj) => obj.posKey !== pos.posKey)]);
    } else {
      setPosArray([
        ...posArray.filter((obj) => obj.posKey !== pos.posKey),
        { ...pos },
      ]);
    }
  }; */
/*   const addPosToOutlet = (pos: Pos, add: boolean) => {
    //setIsSavePos(true)
    pos.outletPreferenceId = form.getFieldValue("outletId");
    if (!pos.status) {
      pos.status = "active";
    }
    if (add) {
      pos.posKey = Date.now().toString();
      setPosArray([
        ...posArray 
        { ...pos },
      ]);
    } else {
      setPosArray([
        ...posArray.filter((obj) => obj.posKey !== pos.posKey),
        { ...pos },
      ]);
    }
  };
 */
  return (
    <>
      {data && (
        <div
          className="row bg-white institution-div p-1"
          style={{ width: "105%" }}
        >
          {location?.state && !location.state.direct ? (
            <PageHeading
              topTitle="Outlet management"
              backIcon={true}
              title={"Update Outlet"}
              submitButton="Update Outlet"
              cancelTitle="Cancel"
              current={current}
              max={4}
              isEdit={true}
              isActivationDateValid={isActivationDateValid}
              isExpiryDateValid={isExpiryDateValid}
            />
          ) : (
            <PageHeading
              topTitle="Outlet management"
              backIcon={true}
              title="Register New Outlet"
              cancelTitle="Cancel"
              buttonTitle="Save & Close"
              submitButton="Register New Outlet"
              current={current}
              max={4}
              isActivationDateValid={isActivationDateValid}
              isExpiryDateValid={isExpiryDateValid}

            />
          )}
          <hr className="mt-2" style={{ color: "#b9b9b9" }} />
          <div className="col-lg-3 mt-3 mb-5 steps-div">
            <Steps direction="vertical" current={current} items={items} />
          </div>
          <div className="col-lg-8 mt-5 mb-5">
            <div className="title">
              {steps[current]?.title}
              {current === 4 && showForm && (
                <Button
                  style={{ background: "#ffffff", float: "right" }}
                  onClick={(e) => setShowForm(false)}
                >
                  {" "}
                  Close
                </Button>
              )}
            </div>
            <div className="content mb-4">{steps[current]?.content}</div>
            {current === 0 ? (
              <OutletPersonalDetails
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
                merchantAcquirerId={merchantAcquirerId}
                merchantAcquirerName={merchantAcquirerName}
                subMerchantId={subMerchantId}
                subMerchantName={subMerchantName}
                outletDetails={
                  location.state?.direct ? undefined : location?.state
                }
                outletList={data}
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

            {current === 4 ? 
              /*{ <AddPos
                handleCallBack={handleCallBack}
                posDetails={
                  location.state?.direct ? undefined : location?.state
                }
                editMode={false}
                /* isSavePos={isSavePos}
                setIsSavePos={setIsSavePos} */
              //  addMode={location?.state?.update ? false : true}
                // saveInstitutionData={saveMerchantData}
             //   formRef={formRef}
              //  form={form4}
              //  addPosToOutlet={addPosToOutlet}
               // removePosInOutlet={removePosInOutlet}
               // posList={posArray}
                /*  setIsListing={setIsListing}
                 isListing={isListing} */
               // showFormProps={showForm}
               // setShowFormProps={setShowForm}
                //allDetails={location?.state}
            //  />
            //) : null}
          //  {current === 5 ? ( */}
              (<Others
                handleCallBack={handleCallBack}
                saveInstitutionData={saveMerchantData}
                formRef={formRef}
                form={form4}
                status={location?.state?.outletStatus}
                allDetails={
                  location.state?.direct ? undefined : location?.state
                }
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

export default OutletRegister;

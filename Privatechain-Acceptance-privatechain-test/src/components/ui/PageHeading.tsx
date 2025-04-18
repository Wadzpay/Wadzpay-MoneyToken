import React, { Ref, useContext, useEffect, useMemo, useState } from "react";
import { Button, Divider, Upload, UploadFile, message } from "antd";
import { Link, useNavigate } from "react-router-dom";
import { RouteType } from "src/constants/routeTypes";
import { SaveContext } from "src/context/SaveContext";
import "./PageHeading.scss";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import { getIdTokenAsync } from "src/auth/AuthManager";
import env from "src/env";
import request, {
  SaveAggregatorFromFile,
  requestBlob,
  downloadFile,
  useFileAggregator,
} from "src/api/user";
import { REACT_APP_S3_BUCKET_URL_TEST,REACT_APP_S3_BUCKET_URL_DEV, UrlUpload } from "src/api/constants";
import { autoShowTooltip } from "aws-amplify";
import { Http2ServerResponse } from "http2";
type UploadType =
  | "AGGREGATOR_UPLOAD"
  | "INSTITUTION_UPLOAD"
  | "MERCHANTGROUP_UPLOAD"
  | "MERCHANT_UPLOAD"
  | "SUBMERCHANT_UPLOAD"
  | "OUTLET_UPLOAD";
type Props = {
  topTitle?: undefined | string;
  backIcon?: undefined | boolean;
  title: string;
  uploadType?: UploadType;
  buttonTitle?: undefined | string;
  linkData?: undefined | any;
  cancelTitle?: undefined | string;
  submitButton?: undefined | string;
  current?: undefined | number;
  max?: undefined | number;
  isEdit?: undefined | boolean;
  bulkUpload?: (e: any) => void;
  bulkprops?: any;
  setFiles?: (e: any) => void;
  parentRefetch?: (arg: boolean) => void;
  showRegister?:boolean;
  isActivationDateValid?:boolean
  isExpiryDateValid?:boolean  
};

const PageHeading: React.FC<Props> = ({
  topTitle,
  backIcon,
  title,
  buttonTitle,
  linkData,
  cancelTitle,
  submitButton,
  current,
  max,
  isEdit,
  uploadType,
  bulkUpload,
  bulkprops,
  setFiles,
  parentRefetch,
  showRegister,
  isActivationDateValid,
  isExpiryDateValid
}: Props) => {
  const navigate = useNavigate();
  const { isSave, setIsSave, isSaveAndClose, setIsSaveAndClose } =
    useContext(SaveContext);
  const {
    mutate: SaveAggregatorFile,
    error: saveAggregatorError,
    isSuccess,
  } = useFileAggregator();
  const [isSaveClose, setIsSaveClose] = useState(false);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [bulkArray, setBulkArray] = useState([]);
  const [uploadFiletype, setUploadFiletype] = useState(uploadType);
  const [success, setSuccess] = useState(false);
  useEffect(() => {
    
    if (current == 1 && !isSaveClose) {
      setIsSaveClose(true);
    }
  }, [current]);

  const validateFileType = (
    { type, name }: UploadFile,
    allowedTypes?: string
  ) => {
    if (!allowedTypes) {
      return true;
    }

    if (type) {
      return allowedTypes.includes(type);
    }
  };
  useEffect(() => {
    console.log("isActivationDateValid",isActivationDateValid,isExpiryDateValid)
  }, [isActivationDateValid,isExpiryDateValid])
  

  const uploadProps = useMemo(
    () => ({
      customRequest: async (componentsData: any) => {
        let formData = new FormData();
        formData.append("file", componentsData.file);
        formData.append("domain", "POST");
        formData.append("filename", componentsData.file.name);
        await requestBlob(
          formData,
          UrlUpload[uploadType as keyof typeof UrlUpload]
        )
          .then(async (response) => {
            if (response.status == 200) {
              if (parentRefetch) parentRefetch(true);
              message.success("File upload successful");
              componentsData.onSuccess();
            } else {
              let blob = await response.blob();
              downloadFile(blob, `Error-${uploadType?.toLowerCase()}.`);
              if (response.status == 207) {
                if (parentRefetch) parentRefetch(true);
                message.error(
                  `${componentsData.file.name} Not all reacords uploaded,Verify downloaded Error report `
                );
                componentsData.onError();
              }
              if (response.status == 400) {
                message.error(
                  `${componentsData.file.name} File upload failed , Verify downloaded Error report`
                );
                componentsData.onError();
              }
            }
          })
          .catch((error) => {
            componentsData.onError("Error 400 ");
            message.error(
              `${componentsData.file.name} File upload failed , Error report downloaded  `
            );
          });
      },

      beforeUpload: (file: UploadFile) => {
        const isAllowedType = validateFileType(file, "text/csv");
        if (!isAllowedType) {
          setFileList((state) => [...state, file]);
          message.error(`${file.name} is not csv file`);
          return false;
        }
        setFileList((state) => [...state, file]);
        const reader = new FileReader();

        reader.onload = (e: any) => {
          console.log(e.target.result);
        };
      },
      onRemove: (file: UploadFile) => {
        if (fileList.some((item) => item.uid === file.uid)) {
          setFileList((fileList) =>
            fileList.filter((item) => item.uid !== file.uid)
          );
          return true;
        }
        return false;
      },
    }),
    [uploadType, fileList]
  );

  return (
    <>
      {topTitle !== undefined ? (
        <div className="d-sm-flex align-items-center justify-content-between">
          <p className="mt-1" style={{ fontSize: "13px", color: "#8E8E8E" }}>
            <img src="/images/home-icon.svg" />
            <span style={{ position: "absolute", top: "112px" }}>
              &nbsp;&nbsp;{topTitle}&nbsp;/&nbsp;
              <span style={{ color: "#000" }}>{title}</span>
            </span>
          </p>
        </div>
      ) : null}
      <div className="d-sm-flex align-items-center justify-content-between">
        <h5
          className="h5 mt-3"
          style={{ letterSpacing: "1px", fontSize: "21px" }}
        >
          {backIcon !== undefined ? (
            <img
              style={{ cursor: "pointer" }}
              onClick={() => navigate(-1)}
              src="/images/back-icon.svg"
            />
          ) : null}
          &nbsp;<b>{title}</b>
        </h5>
        {uploadType && (
          <div
            style={{
              marginLeft: "auto",
              justifyContent: "end",
              display: "flex",
              flexDirection: "row",
              width: "100%",
              alignItems: "center",
            }}
          >
            {" "}
            <span style={{ width: "fit-content" }}>Bulk Upload </span>
            <ExclamationCircleOutlined rev={1}
              style={{ color: "#1677ff" }}
/*               rev={"none"}
 */            />
            :
            <Button
              type="link"
              href={`${process.env.NODE_ENV === 'test'?REACT_APP_S3_BUCKET_URL_TEST:REACT_APP_S3_BUCKET_URL_DEV}${uploadType?.toLowerCase()}.csv`}
            >
              Download
            </Button>
            |
            {uploadType && (
              <Upload
                {...uploadProps}

                /*fileList={fileList}   /* onChange={bulkUpload} */
              >
                <Button type="link" /* onClick={bulkprops.onChange} */>
                  Upload
                </Button>
              </Upload>
            )}{" "}
          </div>
        )}
        <div>
          {cancelTitle !== undefined ? (
            <>
              <Button
                style={{
                  background: "#ffffff",
                  color: "#212529",
                }}
                onClick={() => navigate(-1)}
              >
                {cancelTitle}
              </Button>
              &nbsp;
            </>
          ) : null}
          {buttonTitle !== undefined ? (
            <>
              <Button
                style={{
                  background: isSaveClose ? "#ffffff" : "",
                  color: "#212529",
                }}
                onClick={() => setIsSaveAndClose(true)}
                disabled={isSaveClose ? false : true}
              >
                {buttonTitle}
              </Button>
              &nbsp;
            </>
          ) : null}
          {submitButton !== undefined /* && ( isActivationDateValid) */ ? (
            <>
              <Button
                style={{
                  background: isEdit || current == max ? "#faad14" : "",
                  color: isEdit || current == max ? "#000000" : "#000000",
/*                   display:isActivationDateValid?'inline':'none'
 */                }}
                onClick={() => setIsSave(true)}
                disabled={(((isEdit || current == max )&&(isActivationDateValid))||max===0) ? false : true}
              >
                {submitButton}
              </Button>
              &nbsp;
            </>
          ) : null}
          {(linkData !== undefined&&showRegister) ? (
            <Link to={`${linkData.url}`}>
              <Button
                style={{
                  background: "#faad14",
                  color: "#000000",
                }}
              >
                {linkData.label}
              </Button>
            </Link>
          ) : null}
        </div>
      </div>
    </>
  );
};

export default PageHeading;

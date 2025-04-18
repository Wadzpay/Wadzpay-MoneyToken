import React, { useState, useRef } from "react"
import { message, Steps, Tooltip, notification } from "antd"
import { InfoCircleOutlined } from "@ant-design/icons"
import "./Institution.scss"
import {
  useGetIndustryTypeList,
  useGetFiatCurrencyList,
  useSaveInstitutionDetails
} from "src/api/user"
import PageHeading from "src/components/ui/PageHeading"

import PrimaryBusinessDetails from "./PrimaryBusinessDetails"
import InstitutionAddress from "./InstitutionAddress"
import ContactPersonDetails from "./ContactPersonDetails"
import AdminDetails from "./AdminDetails"
import Others from "./Others"

const steps = [
  {
    key: 1,
    title: "Primary Business Details",
    content:
      "These details will reflect in the wadzpay system by individual institution"
  },
  {
    key: 2,
    title: "Institution Address",
    content: "This address will be consider as institution address"
  },
  {
    key: 3,
    title: "Contact Person Details",
    content: "This address will be consider as institution address"
  },
  {
    key: 4,
    title: "Admin Details",
    content:
      "This admin details are for future responsible for any institutional related things"
  },
  {
    key: 5,
    title: "Others",
    content:
      "This admin details are for future responsible for any institutional related things"
  }
]

const InstitutionRegister = () => {
  const [current, setCurrent] = useState(0)
  const formRef = useRef<HTMLInputElement>(null)
  const [isAcitve, setIsAcitve] = useState<boolean>(true)

  // get industry type list API
  const {
    data: industryTypeList,
    isFetching: isFetchingIndustry,
    error: industryError
  } = useGetIndustryTypeList()

  // get fial currency list API
  const {
    data: fiatCurrencyList,
    isFetching: isFetchingFiatCurrency,
    error: errorFiatCurrency
  } = useGetFiatCurrencyList()

  // API Call Save Institution Details
  const {
    mutate: saveInstitutionDetails,
    error: saveInstitutionDetailsError,
    isSuccess
  } = useSaveInstitutionDetails()

  const handleCallBack = (value: boolean) => {
    setIsAcitve(value)
  }

  const next = () => {
    formRef.current?.click()
    // setCurrent(current + 1)

    // if (current === steps.length - 1) {
    //   notification["success"]({
    //     message: "Notification",
    //     description:
    //       "You have successfully registered the ‘MBSB Institution onboarding’ & notification sent to the institute admin (contactperson@gmail.com).",
    //     placement: "bottomRight"
    //   })
    // }
  }

  const prev = () => {
    setCurrent(current - 1)
  }

  const items = steps.map((item: any, key: number) => ({
    key: item.title,
    title: item.title,
    description: key < current ? "Completed" : ""
  }))

  const saveInstitutionData = (data: any) => {
    data["id"] = 0
    saveInstitutionDetails(data)
  }

  return (
    <>
      <PageHeading
        title="Register New Institute"
        buttonTitle="Register Institute"
      />
      <div className="row bg-white institution-div">
        <div className="col-lg-3 mt-5 mb-5 steps-div">
          <Steps direction="vertical" current={current} items={items} />
        </div>
        <div className="col-lg-8 mt-5 mb-5">
          <div className="title">{steps[current].title}</div>
          <div className="content mb-4">{steps[current].content}</div>

          {current === 0 ? (
            <PrimaryBusinessDetails
              industryTypeList={industryTypeList}
              fiatCurrencyList={fiatCurrencyList}
              handleCallBack={handleCallBack}
              saveInstitutionData={saveInstitutionData}
              formRef={formRef}
            />
          ) : null}
          {current === 1 ? (
            <InstitutionAddress
              handleCallBack={handleCallBack}
              saveInstitutionData={saveInstitutionData}
              formRef={formRef}
            />
          ) : null}
          {current === 2 ? (
            <ContactPersonDetails
              handleCallBack={handleCallBack}
              saveInstitutionData={saveInstitutionData}
              formRef={formRef}
            />
          ) : null}
          {current === 3 ? (
            <AdminDetails
              handleCallBack={handleCallBack}
              saveInstitutionData={saveInstitutionData}
              formRef={formRef}
            />
          ) : null}
          {current === 4 ? (
            <Others
              handleCallBack={handleCallBack}
              saveInstitutionData={saveInstitutionData}
              formRef={formRef}
            />
          ) : null}

          <div className="buttons d-sm-flex align-items-center justify-content-between">
            {current > 0 && (
              <span className="back" onClick={() => prev()}>
                <img src="/images/circle-back.svg" />
                <span>Back</span>
              </span>
            )}
            {current <= steps.length - 1 && (
              <>
                <span></span>
                <button
                  style={{
                    justifyContent: "right",
                    border: "none",
                    backgroundColor: "#fff"
                  }}
                  className="next"
                  onClick={() => next()}
                  disabled={isAcitve}
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
              <InfoCircleOutlined className="mt-4" />
            </Tooltip>
          </div>
        </div>
      </div>
    </>
  )
}

export default InstitutionRegister

import React, { useContext, useState } from "react"
import { useLocation } from "react-router-dom"
import { Button, Form, InputNumber } from "antd"
import { Link } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { IssuanceContext } from "src/context/Merchant"

import PageHeading from "../../../components/ui/PageHeading"

type SizeType = Parameters<typeof Form>[0]["size"]

function FiatCurrentLimits(): JSX.Element {
  const { issuanceDetails } = useContext(IssuanceContext)
  const defaultLimit = (1000000).toLocaleString()
  const [componentSize, setComponentSize] = useState<SizeType | "default">(
    "default"
  )

  const onFormLayoutChange = ({ size }: { size: SizeType }) => {
    setComponentSize(size)
  }

  return (
    <>
      <PageHeading title="Settings" />
      <div className="p-2 ms-2">
        <div
          className="row bg-white boxShadow rounded mt-2"
          style={{ minHeight: "450px" }}
        >
          <div className="col-xl-12 col-lg-6 col-sm-6 fiatCurrentLimits">
            <div className="card-body">
              <div
                className="table-responsive"
                style={{ overflowX: "visible" }}
              >
                <div className="col-lg-3 col-sm-12 mt-2 ml-2">
                  <div>
                    <p>
                      <b>Set / Update</b>
                    </p>
                  </div>
                  <Form>
                    <Form.Item>
                      <Button
                        className="wdz-main-bg-color"
                        style={{
                          color: "#ffffff",
                          padding: "0px",
                          fontSize: "11px",
                          width: "12%"
                        }}
                      >
                        {issuanceDetails?.defaultCurrency === "SART"
                          ? /*issuanceDetails?.defaultCurrency.replace("T", "*")*/ "xQAR"
                          : issuanceDetails?.defaultCurrency}
                      </Button>{" "}
                      Saudi Riyal
                      <InputNumber
                        defaultValue={defaultLimit}
                        style={{ float: "right", width: "35%" }}
                      />
                    </Form.Item>
                    <Form.Item>
                      <Link to={RouteType.SETTINGS}>
                        <Button
                          style={{ border: "none", color: "#ffffff" }}
                          className="wdz-grey-bg-color"
                        >
                          Back
                        </Button>
                      </Link>
                      <Button
                        className="ms-2"
                        type="primary"
                        htmlType="submit"
                        style={{ background: "#1e4b83" }}
                      >
                        Set / Update
                      </Button>
                    </Form.Item>
                  </Form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default FiatCurrentLimits

import React, { useState, useEffect } from "react"
import { Spin } from "antd"
import { LoadingOutlined } from "@ant-design/icons"

const antIcon = <LoadingOutlined style={{ fontSize: 24 }} spin />

type Props = {
  walletSummary: any
}

const WalletsSummary: React.FC<Props> = ({ walletSummary }: Props) => {
  const [loading, setLoading] = useState<boolean>(true)

  useEffect(() => {
    if (walletSummary) {
      setLoading(false)
    }
  }, [walletSummary])

  return (
    <>
      <div className="card-body py-0 mt-2">
        <div className="row">
          <div className="col-xl-4 col-md-6 mb-4">
            <div className="card walletsSummaryCard h-100 py-2">
              <div className="card-body">
                <div className="row no-gutters align-items-center">
                  {loading ? (
                    <Spin
                      indicator={antIcon}
                      style={{ position: "absolute", top: "35%" }}
                    />
                  ) : (
                    <>
                      <div className="col mr-2">
                        <div className="first">Total Wallets</div>
                        <div className="h4 mb-1 mt-1 second wdz-font-color">
                          {walletSummary?.totalWallets}
                        </div>
                      </div>
                      <div className="col-auto third">
                        <b style={{ color: "#000" }}>
                          {walletSummary?.totalWalletsInLastThirtyDays}
                        </b>{" "}
                        wallets added in last 30 days
                      </div>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className="col-xl-4 col-md-6 mb-4">
            <div className="card walletsSummaryCard h-100 py-2">
              <div className="card-body">
                <div className="row no-gutters align-items-center">
                  {loading ? (
                    <Spin
                      indicator={antIcon}
                      style={{ position: "absolute", top: "35%" }}
                    />
                  ) : (
                    <>
                      <div className="col mr-2">
                        <div className="first">Enabled Wallets</div>
                        <div className="h4 mb-1 mt-1 second wdz-font-color-green">
                          {walletSummary?.enableWallets}
                        </div>
                      </div>
                      <div className="col-auto third">
                        <b style={{ color: "#000" }}>
                          {walletSummary?.enableWalletsInLastThirtyDays}{" "}
                        </b>
                        wallets added in last 30 days
                      </div>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>
          <div className="col-xl-4 col-md-6 mb-4">
            <div className="card walletsSummaryCard h-100 py-2">
              <div className="card-body">
                <div className="row no-gutters align-items-center">
                  {loading ? (
                    <Spin
                      indicator={antIcon}
                      style={{
                        position: "absolute",
                        top: "35%"
                      }}
                    />
                  ) : (
                    <>
                      <div className="col mr-2">
                        <div className="first">Total Deposits</div>
                        <div className="h4 mb-1 mt-1 second wdz-font-color-yellow">
                          {walletSummary?.totalDeposits}
                        </div>
                      </div>
                      <div className="col-auto third">
                        <b style={{ color: "#000" }}>
                          {walletSummary?.totalDepositsInLastThirtyDays}
                        </b>{" "}
                        number of deposits in last 30 days
                      </div>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default WalletsSummary

import React, { useContext, useState, useEffect } from "react"
import { Skeleton } from "antd"
import { IssuanceContext } from "src/context/Merchant"

type Props = {
  walletRefund: any
  refundLoading: boolean
  balanceLoading: boolean
  walletBalance: any
}
const WalletRefunds: React.FC<Props> = ({
  walletRefund,
  refundLoading,
  balanceLoading,
  walletBalance
}: Props) => {
  const { issuanceDetails, institutionDetails } = useContext(IssuanceContext)
  const [error, setError] = useState<string | null>(null)

  const getWalletBalancePercentage = (balance: number) => {
    return (
      (balance /
        (parseInt(walletBalance?.totalDepositBalance) +
          parseInt(walletBalance?.refundedBalance))) *
      100
    )
  }

  useEffect(() => {
    console.log("institutionDetails", institutionDetails)
    if (walletBalance === undefined) {
      setError("Error fetching wallet balance. Please try again later.")
    } else {
      setError(null)
    }
  }, [walletBalance])

  return (
    <div className="col-lg-3">
      <div className="col-lg-12">
        <div className="card shadow">
          <div className="card-header custom-header py-3">
            <h5 className="m-0 ">Wallets Refunds</h5>
          </div>
          <div className="card-body walletRefunds">
            <div className="col-lg-6 pe-4 float-start">
              <Skeleton loading={refundLoading} active>
                <span style={{ fontSize: "13px", color: "#717171" }}>
                  Refund Requests
                </span>
                <h5 style={{ color: "#D71034" }}>
                  <b>{walletRefund?.totalRefundRequest}</b>
                </h5>
                <p style={{ fontSize: "13px", color: "#1E1E1E" }}>
                  <b>{walletRefund?.totalRefundRequestInLastThirtyDays}</b>{" "}
                  refund requests in last 30 days
                </p>
              </Skeleton>
            </div>

            <div
              className="col-lg-6 ps-2 float-end"
              style={{ borderLeft: "1px solid #E0E0E0" }}
            >
              <Skeleton loading={refundLoading} active>
                <span style={{ fontSize: "13px", color: "#717171" }}>
                  Refunded (
                  {institutionDetails?.issuingCurrency
                    ? institutionDetails?.issuingCurrency + "*"
                    : ""}
                  )
                </span>
                <h5 style={{ color: "#D71034" }}>
                  <b>{walletRefund?.totalRefunded.toLocaleString()}</b>
                </h5>
                <p style={{ fontSize: "13px", color: "#1E1E1E" }}>
                  {institutionDetails?.issuingCurrency
                    ? institutionDetails?.issuingCurrency + "*"
                    : ""}
                  <b>
                    {walletRefund?.totalRefundedInLastThirtyDays.toLocaleString()}
                  </b>{" "}
                  refunded in last 30 days
                  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                </p>
              </Skeleton>
            </div>
          </div>
        </div>
      </div>
      <div className="col-lg-12 mb-4 mt-2">
        <div className="card shadow mb-2">
          <div className="card-header custom-header py-2">
            <h5 className="m-0 mt-2">
              Wallet Balance (
              {institutionDetails?.issuingCurrency
                ? institutionDetails?.issuingCurrency + "*"
                : ""}
              )
            </h5>
          </div>
          <hr className="loginBorderLine" />
          <div className={`card-body ${error ? "error-border" : ""}`}>
            {balanceLoading ? (
              <Skeleton loading={balanceLoading} active></Skeleton>
            ) : error ? (
              <div className="error-banner">
                <p>{error}</p>
              </div>
            ) : (
              <>
                <h4 className="small">
                  Total Wallets
                  <span className="float-end">
                    <b>
                      {walletBalance?.totalDepositBalance === undefined
                        ? 0
                        : walletBalance?.totalWalletBalance}
                    </b>
                  </span>
                </h4>
                <div className="progress mb-4" style={{ width: "100%" }}>
                  <div
                    className="progress-bar wdz-main-bg-color"
                    role="progressbar"
                    style={{ width: "100%" }}
                  ></div>
                </div>
                <hr className="dasboardBorderLine" />
                <h4 className="small">
                  Enabled Wallets
                  <span className="float-end">
                    <b>
                      {walletBalance?.enableWalletBalance.toLocaleString()}{" "}
                    </b>
                  </span>
                </h4>
                <div
                  className="progress mb-4"
                  style={{
                    width: `${
                      getWalletBalancePercentage(
                        walletBalance?.enableWalletBalance
                      ) < 10
                        ? 10
                        : getWalletBalancePercentage(
                            walletBalance?.enableWalletBalance
                          )
                    }%`
                  }}
                >
                  <div
                    className="progress-bar wdz-green-bg-color"
                    role="progressbar"
                    style={{ width: "100%" }}
                  ></div>
                </div>
                <hr className="dasboardBorderLine" />
                <h4 className="small">
                  Total Deposits
                  <span className="float-end">
                    <b>
                      {walletBalance?.totalDepositBalance.toLocaleString()}{" "}
                    </b>
                  </span>
                </h4>
                <div
                  className="progress mb-4"
                  style={{
                    width: `${
                      getWalletBalancePercentage(
                        walletBalance?.totalDepositBalance
                      ) < 12
                        ? 12
                        : getWalletBalancePercentage(
                            walletBalance?.totalDepositBalance
                          )
                    }%`
                  }}
                >
                  <div
                    className="progress-bar wdz-yellow-bg-color"
                    role="progressbar"
                    style={{ width: "100%" }}
                  ></div>
                </div>
                <hr className="dasboardBorderLine" />
                <h4 className="small">
                  Refunded Balance
                  <span className="float-end">
                    <b>{walletBalance?.refundedBalance.toLocaleString()} </b>
                  </span>
                </h4>
                <div
                  className="progress mb-4"
                  style={{
                    width: `${
                      getWalletBalancePercentage(
                        walletBalance?.refundedBalance
                      ) < 8
                        ? 8
                        : getWalletBalancePercentage(
                            walletBalance?.refundedBalance
                          )
                    }%`
                  }}
                >
                  <div
                    className="progress-bar wdz-grey-bg-color"
                    role="progressbar"
                    style={{ width: "100%" }}
                  ></div>
                </div>
                <hr className="dasboardBorderLine" />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default WalletRefunds

import React, { useContext, useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { useDispatch, useSelector } from "react-redux"
import { RouteType } from "src/constants/routeTypes"
import { ROOTSTATE } from "src/utils/modules"

import PageHeading from "../../components/ui/PageHeading"
import WalletTransactionsChart from "../Dashboard/charts/WalletTransactionsChart"
import WalletsSummaryChart from "../Dashboard/charts/WalletsSummaryChart"
import EnabledWallets from "./wallets/EnabledWallets"
import WalletRefunds from "./WalletRefunds"
import { useWalletBalance, useWalletRefund } from "../../api/user"
import { setAppConfig } from "../../utils/appConfigSlice"

function Dashboard(): JSX.Element {
  const dispatch = useDispatch()
  const [walletBalance, setWalletBalance] = useState<any>()
  const [walletRefund, setWalletRefund] = useState<any>()
  const [refundLoading, setRefundLoading] = useState<boolean>(true)
  const [balanceLoading, setBalanceLoading] = useState<boolean>(true)
  // get appConfig
  const appConfig = useSelector((store: ROOTSTATE) => store.appConfig)

  useEffect(() => {
    // Dispatch action to update appConfig
    dispatch(
      setAppConfig({
        ...appConfig,
        activeNavigation: "/"
      })
    )
  }, [])
  // api's call to get wallet balance
  const {
    data: walletBalanceData,
    isFetching: isFetchingWalletBalance,
    error: errorWalletBalance
  } = useWalletBalance()

  // api's call to get wallet efund
  const {
    data: walletRefundData,
    isFetching: isFetchingWalletRefund,
    error: errorWalletRefund
  } = useWalletRefund()

  useEffect(() => {
    if (walletBalanceData) {
      setWalletBalance(walletBalanceData)
      setBalanceLoading(false)
    }
  }, [walletBalanceData])

  useEffect(() => {
    if (walletRefundData) {
      setWalletRefund(walletRefundData)
      setRefundLoading(false)
    }
  }, [walletRefundData])

  const handleEnabledWallets = () => {
    try {
      // Dispatch action to update appConfig
      dispatch(
        setAppConfig({
          ...appConfig,
          activeNavigation: "/wallets"
        })
      )
    } catch (error) {
      console.error("Error updating appConfig:", error)
    }
  }

  return (
    <>
      <PageHeading title="Dashboard" />
      <div className="">
        <div className="row">
          <div className="col-lg-9 mb-4 rounded-5">
            <div className="card shadow mb-4">
              <WalletsSummaryChart />
            </div>
          </div>
          {/* WalletRefunds */}
          <WalletRefunds
            walletRefund={walletRefund}
            refundLoading={refundLoading}
            balanceLoading={balanceLoading}
            walletBalance={walletBalance}
          />
        </div>
        <div className="row">
          <div className="col-lg-6 mb-4">
            <div className="card shadow mb-4">
              <div className="card-header custom-header py-3">
                <h5 className="m-0 font-weight-bold float-start">
                  Enabled Wallets
                </h5>
                <Link to={RouteType.WALLETSLIST}>
                  <h6
                    onClick={handleEnabledWallets}
                    className="m-0 float-end wdz-font-color View All"
                  >
                    View all Wallets
                  </h6>
                </Link>
              </div>
              <div className="card-body mt-2">
                <EnabledWallets />
              </div>
            </div>
          </div>

          <div className="col-lg-6 mb-4">
            <div className="card shadow mb-4">
              <WalletTransactionsChart />
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Dashboard

import React, { useContext, useEffect, useState } from "react"
import { Spin } from "antd"
import { LoadingOutlined } from "@ant-design/icons"
import dayjs from "dayjs"
import utc from "dayjs/plugin/utc"
import tz from "dayjs/plugin/timezone"
import { IssuanceContext } from "src/context/Merchant"
import { WalletsUserListdata } from "src/api/models"
import { useWalletUserList } from "src/api/user"

const antIcon = <LoadingOutlined style={{ fontSize: 24 }} spin />

const EnabledWallets: React.FC = () => {
  const { issuanceDetails, institutionDetails } = useContext(IssuanceContext)
  const [WalletUser, setWalletsList] = useState<WalletsUserListdata | any>()
  const [loading, setLoading] = useState<boolean>(true)

  dayjs.extend(utc)
  dayjs.extend(tz)

  // get wallets user list API
  const { mutate: getWalletList, data, error } = useWalletUserList()

  useEffect(() => {
    const requestParams: any = {
      page: 1,
      sortBy: "STATUS",
      sortDirection: "DESC",
      limit: 5,
      type: ["ENABLE"]
    }
    getWalletList(requestParams)
  }, [])

  useEffect(() => {
    if (data) {
      setLoading(false)
      setWalletsList(data)
    }
  }, [data])

  useEffect(() => {
    if (error) {
      setLoading(false)
    }
  }, [error])

  const dateTimeFormat = (time: any) => {
    if (!time) {
      return null
    }

    return dayjs(time).tz("Asia/Kolkata").format("D MMM YYYY, hh:mma")
  }

  const EnableWalletList = () => {
    return WalletUser?.walletList.length > 0 ? (
      WalletUser?.walletList.map((obj: any) => {
        const { walletId, email, tokenBalance, createdAt } = obj
        return (
          <tr key={walletId}>
            <td>{walletId}</td>
            <td>{email}</td>
            <td className="text-right" style={{ textAlign: "right" }}>
              {tokenBalance.toLocaleString()}
            </td>
            <td>{dateTimeFormat(createdAt)}</td>
          </tr>
        )
      })
    ) : !loading ? (
      <tr>
        <td colSpan={4} style={{ textAlign: "center" }}>
          No wallets found
        </td>
      </tr>
    ) : (
      <tr>
        <td colSpan={4} style={{ textAlign: "center" }}>
          <Spin indicator={antIcon} />
        </td>
      </tr>
    )
  }

  return (
    <div className="table-responsive">
      <table className="customTable table">
        <thead>
          <tr>
            <th style={{ width: "14%" }}>ID</th>
            <th style={{ width: "38%" }}>User ID</th>
            <th style={{ width: "20%", textAlign: "right" }}>
              Balance{" "}
              {institutionDetails?.issuingCurrency
                ? "(" + institutionDetails?.issuingCurrency + "*)"
                : ""}
              {/* (
              {issuanceDetails?.defaultCurrency === "SART"
                ? issuanceDetails?.defaultCurrency.replace("T", "*")
                : issuanceDetails?.defaultCurrency}
              ) */}
            </th>
            <th style={{ width: "28%" }}>Activated On</th>
          </tr>
        </thead>
        <tbody>
          <EnableWalletList />
        </tbody>
      </table>
    </div>
  )
}

export default EnabledWallets
